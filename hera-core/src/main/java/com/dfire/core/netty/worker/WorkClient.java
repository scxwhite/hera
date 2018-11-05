package com.dfire.core.netty.worker;


import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.Job;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.netty.worker.request.*;
import com.dfire.core.schedule.ScheduleInfoLog;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcSocketMessage;
import com.dfire.protocol.RpcWebResponse;
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
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
@Slf4j
@Data
@Component
public class WorkClient {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private WorkContext workContext = new WorkContext();
    private ScheduledExecutorService service;
    public final Timer workClientTimer = new HashedWheelTimer(Executors.defaultThreadFactory(), 1, TimeUnit.SECONDS);
    private AtomicBoolean clientSwitch = new AtomicBoolean(false);
    /**
     * ProtobufVarint32FrameDecoder:  针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器
     * <pre>
     *  * BEFORE DECODE (302 bytes)       AFTER DECODE (300 bytes)
     *  * +--------+---------------+      +---------------+
     *  * | Length | Protobuf Data |----->| Protobuf Data |
     *  * | 0xAC02 |  (300 bytes)  |      |  (300 bytes)  |
     *  * +--------+---------------+      +---------------+
     * </pre>
     *
     * ProtobufVarint32LengthFieldPrepender: 对protobuf协议的的消息头上加上一个长度为32的整形字段,用于标志这个消息的长度。
     * <pre>
     * * BEFORE DECODE (300 bytes)       AFTER DECODE (302 bytes)
     *  * +---------------+               +--------+---------------+
     *  * | Protobuf Data |-------------->| Length | Protobuf Data |
     *  * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
     *  * +---------------+               +--------+---------------+
     * </pre>
     */
    public void init(ApplicationContext applicationContext) {
        if (!clientSwitch.compareAndSet(false, true)) {
            return ;
        }
        workContext.setWorkClient(this);
        workContext.setApplicationContext(applicationContext);
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
        log.info("init work client success ");

        workClientTimer.newTimeout(new TimerTask() {

            private WorkerHandlerHeartBeat workerHandlerHeartBeat = new WorkerHandlerHeartBeat();
            private int failCount = 0;

            @Override
            public void run(Timeout timeout) {
                try {
                    if (workContext.getServerChannel() != null) {
                        ChannelFuture channelFuture = workerHandlerHeartBeat.send(workContext);
                        channelFuture.addListener((future) -> {
                            if (!future.isSuccess()) {
                                failCount++;
                                log.error("send heart beat failed ,failCount :" + failCount);
                            } else {
                                failCount = 0;
                                log.debug("send heart beat success:{}", workContext.getServerChannel().remoteAddress());
                            }
                            if (failCount > 10) {
                                future.cancel(true);
                                log.debug("cancel connect server ,failCount:" + failCount);
                            }
                        });
                    } else {
                        log.info("server channel can not find on " + DistributeLock.host);
                    }
                } catch (Exception e) {
                    log.info("heart beat send failed ：" + failCount);
                    log.error("heart beat error:", e);
                } finally {
                    workClientTimer.newTimeout(this, (failCount + 1) * HeraGlobalEnvironment.getHeartBeat(), TimeUnit.SECONDS);
                }
            }
        }, HeraGlobalEnvironment.getHeartBeat(), TimeUnit.SECONDS);

        workClientTimer.newTimeout(new TimerTask() {
            private void editLog(Job job, Exception e) {
                try {
                    HeraJobHistoryVo his = job.getJobContext().getHeraJobHistory();
                    String logContent = his.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    log.error(new StringBuilder("log output error!\n")
                            .append("[actionId:").append(his.getJobId())
                            .append(", hisId:").append(his.getId())
                            .append(", logLength:")
                            .append(logContent.length()).append("]")
                            .toString(), e);
                } catch (Exception ex) {
                    log.error("log exception error!");
                }
            }


            private void editDebugLog(Job job, Exception e) {
                try {
                    HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
                    String logContent = history.getLog().getContent();
                    if (logContent == null) {
                        logContent = "";
                    }
                    log.error(new StringBuilder("log output error!\n")
                            .append("[fileId:").append(history.getFileId())
                            .append(", hisId:").append(history.getId())
                            .append(", logLength:")
                            .append(logContent.length()).append("]")
                            .toString(), e);
                } catch (Exception ex) {
                    log.error("log exception error!");
                }
            }

            @Override
            public void run(Timeout timeout) {

                try {
                    for (Job job : new HashSet<>(workContext.getRunning().values())) {
                        try {
                            HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                            workContext.getJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            editDebugLog(job, e);
                        }
                    }

                    for (Job job : new HashSet<>(workContext.getManualRunning().values())) {
                        try {
                            HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
                            workContext.getJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            editLog(job, e);
                        }
                    }

                    for (Job job : new HashSet<>(workContext.getDebugRunning().values())) {
                        try {
                            HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
                            workContext.getDebugHistoryService().updateLog(BeanConvertUtils.convert(history));
                        } catch (Exception e) {
                            editDebugLog(job, e);
                        }
                    }
                } catch (Exception e) {
                    log.error(JSONObject.toJSONString(e));
                    throw new RuntimeException(e);
                } finally {
                    workClientTimer.newTimeout(this, 5, TimeUnit.SECONDS);
                }

            }
        }, 5, TimeUnit.SECONDS);
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
                    workContext.setServerChannel(future.channel());
                    log.info(workContext.getServerChannel().toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, HeraGlobalEnvironment.getConnectPort()));
        connectFuture.addListener(futureListener);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            connectFuture.removeListener(futureListener);
            connectFuture.cancel(true);
            throw new ExecutionException(new TimeoutException("connect server consumption of 2 seconds"));
        }
        if (!connectFuture.isSuccess()) {
            throw new RuntimeException("connect server failed " + host,
                    connectFuture.cause());
        }
        ScheduleInfoLog.info("connect server success");
    }

    /**
     * 取消执行开发中心任务
     *
     * @param debugId
     */
    public void cancelDebugJob(String debugId) {
        Job job = workContext.getDebugRunning().get(debugId);
        job.cancel();
        workContext.getDebugRunning().remove(debugId);

        HeraDebugHistoryVo history = job.getJobContext().getDebugHistory();
        history.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        history.setStatus(StatusEnum.FAILED);
        workContext.getDebugHistoryService().update(BeanConvertUtils.convert(history));
        history.getLog().appendHera("任务被取消");
        workContext.getDebugHistoryService().update(BeanConvertUtils.convert(history));


    }

    /**
     * 取消手动执行的任务
     *
     * @param historyId
     */
    public void cancelManualJob(String historyId) {
        Job job = workContext.getManualRunning().get(historyId);
        workContext.getManualRunning().remove(historyId);
        job.cancel();

        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if (illustrate != null && illustrate.trim().length() > 0) {
            history.setIllustrate(illustrate + "；手动取消该任务");
        } else {
            history.setIllustrate("手动取消该任务");
        }
        history.setStatusEnum(StatusEnum.FAILED);
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(history));

    }

    /**
     * 取消自动调度执行的任务
     *
     * @param actionId
     */
    public void cancelScheduleJob(String actionId) {
        Job job = workContext.getRunning().get(actionId);
        workContext.getRunning().remove(actionId);
        job.cancel();

        HeraJobHistoryVo history = job.getJobContext().getHeraJobHistory();
        history.setEndTime(new Date());
        String illustrate = history.getIllustrate();
        if (illustrate != null && illustrate.trim().length() > 0) {
            history.setIllustrate(illustrate + "；手动取消该任务");
        } else {
            history.setIllustrate("手动取消该任务");
        }
        history.setStatusEnum(StatusEnum.FAILED);
        workContext.getJobHistoryService().update(BeanConvertUtils.convert(history));
        history.getLog().appendHera("任务被取消");
        workContext.getJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(history));

    }


    /**
     * 页面开发中心发动执行脚本时，发起请求，
     *
     * @param kind
     * @param id
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void executeJobFromWeb(JobExecuteKind.ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        RpcWebResponse.WebResponse response = new WorkerHandleWebExecute().handleWebExecute(workContext, kind, id).get();
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            log.error("netty manual web request get jobStatus error");
        }
    }

    public String cancelJobFromWeb(JobExecuteKind.ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        RpcWebResponse.WebResponse webResponse = new WorkHandleWebCancel().handleCancel(workContext, kind, id).get();
        if (webResponse.getStatus() == ResponseStatus.Status.ERROR) {
            log.error("cancel from web exception");
        }
        return "cancel job success";
    }

    public void updateJobFromWeb(String jobId) throws ExecutionException, InterruptedException {
        RpcWebResponse.WebResponse webResponse = new WorkHandleWebUpdate().handleUpdate(workContext, jobId).get();
        if (webResponse.getStatus() == ResponseStatus.Status.ERROR) {
            log.error("cancel from web exception");
        }
    }

    public String generateActionFromWeb(JobExecuteKind.ExecuteKind kind, String id) throws ExecutionException, InterruptedException {
        RpcWebResponse.WebResponse response = new WorkerHandleWebAction().handleWebAction(workContext, kind, id).get();
        if (response.getStatus() == ResponseStatus.Status.ERROR) {
            log.error("generate action error");
            return "生成版本失败";
        }
        return "生成版本成功";
    }


}
