package com.dfire.core.netty.worker;


import com.dfire.common.constants.Constants;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.common.vo.MachineInfoVo;
import com.dfire.common.vo.OSInfoVo;
import com.dfire.common.vo.ProcessMonitorVo;
import com.dfire.common.vo.WorkInfoVo;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.job.Job;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.cluster.FailFastCluster;
import com.dfire.core.netty.worker.request.WorkerHandleWebRequest;
import com.dfire.core.netty.worker.request.WorkerHandlerHeartBeat;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeartLog;
import com.dfire.logs.HeraLog;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcHeartBeatMessage.AllHeartBeatInfoMessage;
import com.dfire.protocol.RpcHeartBeatMessage.HeartBeatMessage;
import com.dfire.protocol.RpcSocketMessage;
import com.dfire.protocol.RpcWebResponse;
import com.dfire.protocol.RpcWorkInfo.*;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
@Data
@Component
public class WorkClient {

    public ScheduledThreadPoolExecutor workSchedule;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    @Autowired
    private WorkContext workContext;
    private ScheduledExecutorService service;
    private AtomicBoolean clientSwitch = new AtomicBoolean(false);

    {
        workSchedule = new ScheduledThreadPoolExecutor(3, new NamedThreadFactory("work-schedule", false));
        workSchedule.setKeepAliveTime(5, TimeUnit.MINUTES);
        workSchedule.allowCoreThreadTimeOut(true);
    }

    /**
     * ProtobufVarint32FrameDecoder:  针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器
     * <pre>
     *  * BEFORE DECODE (302 bytes)       AFTER DECODE (300 bytes)
     *  * +--------+---------------+      +---------------+
     *  * | Length | Protobuf Data |----->| Protobuf Data |
     *  * | 0xAC02 |  (300 bytes)  |      |  (300 bytes)  |
     *  * +--------+---------------+      +---------------+
     * </pre>
     * <p>
     * ProtobufVarint32LengthFieldPrepender: 对protobuf协议的的消息头上加上一个长度为32的整形字段,用于标志这个消息的长度。
     * <pre>
     * * BEFORE DECODE (300 bytes)       AFTER DECODE (302 bytes)
     *  * +---------------+               +--------+---------------+
     *  * | Protobuf Data |-------------->| Length | Protobuf Data |
     *  * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
     *  * +---------------+               +--------+---------------+
     * </pre>
     */
    public void init() {
        if (!clientSwitch.compareAndSet(false, true)) {
            return;
        }
        workContext.setWorkClient(this);
        workContext.init();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS))
                                .addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                                .addLast("decoder", new ProtobufDecoder(RpcSocketMessage.SocketMessage.getDefaultInstance()))
                                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                                .addLast("encoder", new ProtobufEncoder())
                                .addLast(new WorkHandler(workContext));
                    }
                });
        HeraLog.info("init work client success ");

        workSchedule.schedule(new Runnable() {

            private WorkerHandlerHeartBeat workerHandlerHeartBeat = new WorkerHandlerHeartBeat();
            private int failCount = 0;

            @Override
            public void run() {
                try {
                    if (workContext.getServerChannel() != null) {
                        boolean send = workerHandlerHeartBeat.send(workContext);
                        if (!send) {
                            failCount++;
                            ErrorLog.error("send heart beat failed ,failCount :" + failCount);
                        } else {
                            failCount = 0;
                            HeartLog.debug("send heart beat success:{}", workContext.getServerChannel().getRemoteAddress());
                        }
                    } else {
                        ErrorLog.error("server channel can not find on " + WorkContext.host);
                    }
                } catch (Exception e) {
                    ErrorLog.error("heart beat error:", e);
                } finally {
                    workSchedule.schedule(this, (failCount + 1) * HeraGlobalEnv.getHeartBeat(), TimeUnit.SECONDS);
                }
            }
        }, HeraGlobalEnv.getHeartBeat(), TimeUnit.SECONDS);

        /**
         * 定时 刷新日志到数据库
         */
        workSchedule.scheduleWithFixedDelay(new Runnable() {
            /**
             * 处理任务调度的异常日志
             * @param job
             * @param e
             */
            private void printScheduleLog(Job job, Exception e) {
                try {
                    HeraJobHistoryVo his = job.getJobContext().getHeraJobHistory();
                    String logContent = his.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    ErrorLog.error("log output error!\n" +
                            "[actionId:" + his.getJobId() +
                            ", hisId:" + his.getId() +
                            ", logLength:" +
                            logContent.length() + "]", e);
                } catch (Exception ex) {
                    ErrorLog.error("log exception error!", ex);
                }
            }

            /**
             * 处理 开发中心的日志
             * @param job
             * @param e
             */
            private void printDebugLog(Job job, Exception e) {
                try {
                    HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
                    String logContent = history.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    ErrorLog.error("log output error!\n" +
                            "[fileId:" + history.getFileId() +
                            ", hisId:" + history.getId() +
                            ", logLength:" +
                            logContent.length() + "]", e);
                } catch (Exception ex) {
                    ErrorLog.error("log exception error!", ex);
                }
            }

            @Override
            public void run() {
                try {
                    for (Job job : new ArrayList<>(workContext.getRunning().values())) {
                        try {
                            HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                            workContext.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            printScheduleLog(job, e);
                        }
                    }

                    for (Job job : new ArrayList<>(workContext.getManualRunning().values())) {
                        try {
                            HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                            workContext.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            printScheduleLog(job, e);
                        }
                    }

                    for (Job job : new ArrayList<>(workContext.getDebugRunning().values())) {
                        try {
                            HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
                            workContext.getHeraDebugHistoryService().updateLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            printDebugLog(job, e);
                        }
                    }
                } catch (Exception e) {
                    ErrorLog.error("job log flush exception:{}", e.toString());
                }

            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 机器启动spring-boot时，worker向主节点发起netty请求连接，成功之后，worker异步获取channel,并设置在work context中
     *
     * @param host
     * @throws Exception
     */
    public synchronized void connect(String host) throws Exception {
        if (workContext.getServerChannel() != null) {
            if (workContext.getServerHost().equals(host)) {
                return;
            } else {
                workContext.getServerChannel().close();
                workContext.setServerChannel(null);
            }
        }
        workContext.setServerHost(host);
        CountDownLatch latch = new CountDownLatch(1);
        ChannelFutureListener futureListener = (future) -> {
            try {
                if (future.isSuccess()) {
                    workContext.setServerChannel(FailFastCluster.wrap(future.channel()));
                    SocketLog.info(workContext.getServerChannel().toString());
                }
            } catch (Exception e) {
                ErrorLog.error("连接master异常", e);
            } finally {
                latch.countDown();
            }
        };
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, HeraGlobalEnv.getConnectPort()));
        connectFuture.addListener(futureListener);
        if (!latch.await(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS)) {
            connectFuture.removeListener(futureListener);
            connectFuture.cancel(true);
            throw new ExecutionException(new TimeoutException("connect server consumption of 2 seconds"));
        }
        if (!connectFuture.isSuccess()) {
            throw new RuntimeException("connect server failed " + host,
                    connectFuture.cause());
        }
        SocketLog.info("connect server success");
    }

    /**
     * 取消执行开发中心任务
     *
     * @param debugId
     */
    public void cancelDebugJob(Long debugId) {
        Job job = workContext.getDebugRunning().remove(debugId);
        job.getJobContext().getDebugHistory().getLog().appendHera(Constants.CANCEL_JOB_MESSAGE);
        job.cancel();
    }


    public void cancelJob(Job job) {
        if (job == null) {
            return;
        }
        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
        String illustrate = history.getIllustrate();
        if (illustrate != null && illustrate.trim().length() > 0) {
            history.setIllustrate(illustrate + Constants.SEMICOLON + Constants.CANCEL_JOB_MESSAGE);
        } else {
            history.setIllustrate(Constants.CANCEL_JOB_MESSAGE);
        }
        job.cancel();
    }


    /**
     * 页面开发中心发动执行脚本时，发起请求，
     *
     * @param kind
     * @param id
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void executeJobFromWeb(ExecuteKind kind, Long id) throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.handleWebExecute(workContext, kind, id).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error(response.getErrorText());
        }
    }

    public String cancelJobFromWeb(ExecuteKind kind, Long id) throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse webResponse = WorkerHandleWebRequest.handleCancel(workContext, kind, id).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (webResponse.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error(webResponse.getErrorText());
            return webResponse.getErrorText();
        }
        return "取消任务成功";
    }

    public void updateJobFromWeb(String jobId) throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse webResponse = WorkerHandleWebRequest.handleUpdate(workContext, jobId).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (webResponse.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error(webResponse.getErrorText());
        }
    }

    public String generateActionFromWeb(ExecuteKind kind, Long id) throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.handleWebAction(workContext, kind, id).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.MINUTES);
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error("generate action error");
            return "生成版本失败";
        }
        return "生成版本成功";
    }

    public Map<String, HeartBeatInfo> getJobQueueInfoFromWeb() throws ExecutionException, InterruptedException, InvalidProtocolBufferException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.getJobQueueInfoFromMaster(workContext).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error("获取心跳信息失败:{}", response.getErrorText());
            return null;
        }
        Map<String, HeartBeatMessage> map = AllHeartBeatInfoMessage.parseFrom(response.getBody()).getValuesMap();
        Map<String, HeartBeatInfo> infoMap = new HashMap<>(map.size());
        for (Map.Entry<String, HeartBeatMessage> entry : map.entrySet()) {
            HeartBeatMessage beatMessage = entry.getValue();
            infoMap.put(entry.getKey(), HeartBeatInfo.builder()
                    .cpuLoadPerCore(beatMessage.getCpuLoadPerCore())
                    .debugRunning(beatMessage.getDebugRunningList())
                    .manualRunning(beatMessage.getManualRunningList())
                    .running(beatMessage.getRunningList())
                    .rerunRunning(beatMessage.getRerunRunningList())
                    .superRunning(beatMessage.getSuperRunningList())
                    .memRate(beatMessage.getMemRate())
                    .memTotal(beatMessage.getMemTotal())
                    .host(beatMessage.getHost())
                    .cores(beatMessage.getCores())
                    .timestamp(beatMessage.getTimestamp())
                    .date(ActionUtil.getDefaultFormatterDate(new Date(beatMessage.getTimestamp())))
                    .build());
        }

        return infoMap;
    }


    public String increaseActionPriority(Long actionId) throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.increaseActionPriority(workContext, actionId).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            return "优先级修改异常";
        }
        return "优先级修改成功";
    }

    public HashMap<String, WorkInfoVo> getAllWorkInfo() throws ExecutionException, InterruptedException, InvalidProtocolBufferException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.getAllWorkInfoFromMaster(workContext).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (response == null || response.getStatus() == ResponseStatus.Status.ERROR) {
            ErrorLog.error("获取work信息失败:{}", response.getErrorText());
            return null;
        }
        Map<String, WorkInfo> allWorkInfo = AllWorkInfo.parseFrom(response.getBody()).getValuesMap();

        HashMap<String, WorkInfoVo> workInfoHashMap = new HashMap<>(allWorkInfo.size());

        allWorkInfo.forEach((ip, workInfo) -> {
            WorkInfoVo workInfoVo = new WorkInfoVo();
            List<ProcessMonitor> monitorList = workInfo.getProcessMonitorList();
            if (monitorList != null && monitorList.size() > 0) {
                List<ProcessMonitorVo> monitorVoList = new ArrayList<>(monitorList.size());
                monitorList.forEach(m -> {
                    ProcessMonitorVo monitorVo = new ProcessMonitorVo();
                    monitorVo.setUser(m.getUser());
                    monitorVo.setViri(m.getViri());
                    monitorVo.setTime(m.getTime());
                    monitorVo.setRes(m.getRes());
                    monitorVo.setMem(m.getMem());
                    monitorVo.setPid(m.getPid());
                    monitorVo.setCommand(m.getCommand());
                    monitorVo.setCpu(m.getCpu());
                    monitorVoList.add(monitorVo);
                });
                workInfoVo.setProcessMonitor(monitorVoList);
            }
            OSInfo osInfo = workInfo.getOSInfo();
            if (osInfo != null) {
                OSInfoVo osInfoVo = new OSInfoVo();
                osInfoVo.setCpu(osInfo.getCpu());
                osInfoVo.setSwap(osInfo.getSwap());
                osInfoVo.setSystem(osInfo.getSystem());
                osInfoVo.setUser(osInfo.getUser());
                osInfoVo.setMem(osInfo.getMem());
                workInfoVo.setOsInfo(osInfoVo);
            }
            List<MachineInfo> machineInfoList = workInfo.getMachineInfoList();
            if (machineInfoList != null && machineInfoList.size() > 0) {
                List<MachineInfoVo> machineInfoVoList = new ArrayList<>(machineInfoList.size());
                machineInfoList.forEach(m -> {
                    MachineInfoVo machineInfoVo = new MachineInfoVo();
                    machineInfoVo.setSize(m.getSize());
                    machineInfoVo.setMountedOn(m.getMountedOn());
                    machineInfoVo.setType(m.getType());
                    machineInfoVo.setFilesystem(m.getFilesystem());
                    machineInfoVo.setAvail(m.getAvail());
                    machineInfoVo.setUse(m.getUse());
                    machineInfoVoList.add(machineInfoVo);
                });
                workInfoVo.setMachineInfo(machineInfoVoList);
            }

            workInfoHashMap.put(ip, workInfoVo);
        });

        return workInfoHashMap;
    }

    public String updateWorkFromWeb() throws ExecutionException, InterruptedException, TimeoutException {
        RpcWebResponse.WebResponse response = WorkerHandleWebRequest.updateWorkFromWeb(workContext).get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            return "更新work失败";
        }
        return "更新work成功";

    }
}
