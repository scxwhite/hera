package com.dfire.core.netty.master.response;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.event.HeraJobMaintenanceEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.NettyChannel;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.queue.JobElement;
import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.*;
import com.dfire.protocol.ResponseStatus.Status;
import com.dfire.protocol.RpcHeartBeatMessage.AllHeartBeatInfoMessage;
import com.dfire.protocol.RpcHeartBeatMessage.HeartBeatMessage;
import com.dfire.protocol.RpcWebOperate.WebOperate;
import com.dfire.protocol.RpcWebRequest.WebRequest;
import com.dfire.protocol.RpcWebResponse.WebResponse;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * master处理work发起的web请求
 *
 * @author xiaosuda
 * @date 2018/11/9
 */
public class MasterHandlerWebResponse {


    private static volatile boolean workReady = false;

    /**
     * 处理work发起的调度中心任务执行 操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse handleWebExecute(MasterContext context, WebRequest request) {
        if (request.getEk() == ExecuteKind.ManualKind || request.getEk() == ExecuteKind.ScheduleKind) {
            String historyId = request.getId();

            HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
            HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
            context.getMaster().run(history);
            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(WebOperate.ExecuteJob)
                    .setStatus(Status.OK)
                    .build();
            TaskLog.info("MasterHandlerWebResponse: send web execute response, actionId = {} ", history.getJobId());
            return webResponse;
        } else if (request.getEk() == ExecuteKind.DebugKind) {
            String debugId = request.getId();
            HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
            TaskLog.info("2-1.MasterHandlerWebResponse: receive web debug response, debugId = " + debugId);
            context.getMaster().debug(debugHistory);

            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(WebOperate.ExecuteJob)
                    .setStatus(Status.OK)
                    .build();
            TaskLog.info("2-2.MasterHandlerWebResponse : send web debug response, debugId = {}", debugId);
            return webResponse;
        }
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setErrorText("未识别的操作类型" + request.getEk())
                .setStatus(Status.ERROR)
                .build();
    }

    /**
     * 处理work发起的开发中心任务执行 操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse handleWebDebug(MasterContext context, WebRequest request) {
        String debugId = request.getId();
        Queue<JobElement> queue = context.getDebugQueue();
        WebResponse response;
        for (JobElement jobElement : queue) {
            if (jobElement.getJobId().equals(debugId)) {
                response = WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(WebOperate.ExecuteDebug)
                        .setStatus(Status.ERROR)
                        .setErrorText("任务已经在队列中")
                        .build();
                return response;
            }
        }
        HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        context.getMaster().debug(debugHistory);
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(WebOperate.ExecuteDebug)
                .setStatus(Status.OK)
                .build();
    }

    /**
     * 处理work发起的任务更新 操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse handleWebUpdate(MasterContext context, WebRequest request) {
        String id = request.getId();
        context.getMaster().generateSingleAction(Integer.parseInt(id));
        context.getDispatcher().forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateJob, id));
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(WebOperate.UpdateJob)
                .setStatus(Status.OK)
                .build();

    }

    /**
     * 处理work发起的生成版本 操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse generateActionByJobId(MasterContext context, WebRequest request) {
        boolean result = Constants.ALL_JOB_ID.equals(request.getId()) ? context.getMaster().generateBatchAction() : context.getMaster().generateSingleAction(Integer.parseInt(request.getId()));
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(WebOperate.ExecuteJob)
                .setStatus(result ? Status.OK : Status.ERROR)
                .build();

    }

    /**
     * 处理work发起的任务取消操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse handleWebCancel(MasterContext context, WebRequest request) {
        if (request.getEk() == ExecuteKind.ScheduleKind) {
            return MasterCancelJob.handleScheduleCancel(context, request);
        } else if (request.getEk() == ExecuteKind.ManualKind) {
            return MasterCancelJob.handleManualCancel(context, request);
        } else if (request.getEk() == ExecuteKind.DebugKind) {
            return MasterCancelJob.handleDebugCancel(context, request);
        }
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(request.getOperate())
                .setStatus(ResponseStatus.Status.ERROR)
                .setErrorText("无法识别的任务取消类型：" + request.getEk())
                .build();
    }

    /**
     * 处理work发起的任务执行 操作
     *
     * @param context MasterContext
     * @param request WebRequest
     * @return WebResponse
     */
    public static WebResponse buildJobQueueInfo(MasterContext context, WebRequest request) {
        //输出线程池信息
        context.getMaster().printThreadPoolLog();
        Map<String, HeartBeatMessage> allInfo = new HashMap<>(context.getWorkMap().size());
        context.getWorkMap().values().forEach(workHolder -> {
            HeartBeatInfo beatInfo = workHolder.getHeartBeatInfo();
            if (beatInfo != null) {
                allInfo.put(Constants.WORK_PREFIX + beatInfo.getHost(), HeartBeatMessage.newBuilder()
                        .addAllDebugRunnings(beatInfo.getDebugRunning())
                        .addAllRunnings(beatInfo.getRunning())
                        .addAllManualRunnings(beatInfo.getManualRunning())
                        .setMemRate(beatInfo.getMemRate())
                        .setMemTotal(beatInfo.getMemTotal())
                        .setCpuLoadPerCore(beatInfo.getCpuLoadPerCore())
                        .setTimestamp(beatInfo.getTimestamp())
                        .setHost(beatInfo.getHost())
                        .setCores(beatInfo.getCores())
                        .build());

            }
        });

        //debug任务队列
        List<String> masterDebugQueue = new ArrayList<>(context.getDebugQueue().size());
        context.getDebugQueue().forEach(jobElement -> masterDebugQueue.add(jobElement.getJobId()));
        //自动任务队列
        List<String> masterScheduleQueue = new ArrayList<>(context.getScheduleQueue().size());
        context.getScheduleQueue().forEach(jobElement -> masterScheduleQueue.add(jobElement.getJobId()));
        //手动任务队列
        List<String> masterManualQueue = new ArrayList<>(context.getManualQueue().size());
        context.getManualQueue().forEach(jobElement -> masterManualQueue.add(jobElement.getJobId()));

        MemUseRateJob memUseRateJob = new MemUseRateJob(1);
        memUseRateJob.readMemUsed();
        CpuLoadPerCoreJob loadPerCoreJob = new CpuLoadPerCoreJob();
        loadPerCoreJob.run();

        allInfo.put(Constants.MASTER_PREFIX + WorkContext.host, HeartBeatMessage.newBuilder()
                .addAllDebugRunnings(masterDebugQueue)
                .addAllRunnings(masterScheduleQueue)
                .addAllManualRunnings(masterManualQueue)
                .setMemRate(memUseRateJob.getRate())
                .setMemTotal(memUseRateJob.getMemTotal())
                .setCpuLoadPerCore(loadPerCoreJob.getLoadPerCore())
                .setTimestamp(System.currentTimeMillis())
                .setHost(WorkContext.host)
                .setCores(WorkContext.cpuCoreNum)
                .build());

        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(request.getOperate())
                .setStatus(Status.OK)
                .setBody(AllHeartBeatInfoMessage.newBuilder().putAllValues(allInfo).build().toByteString())
                .build();
    }

    public static synchronized WebResponse buildAllWorkInfo(MasterContext context, WebRequest request) {
        if (!workReady) {
            //发送workInfo build 请求
            context.getThreadPool().submit(() -> context.getWorkMap().keySet().parallelStream().forEach(channel -> {
                try {
                    new NettyChannel(channel).writeAndFlush(RpcSocketMessage.SocketMessage.newBuilder()
                            .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                            .setBody(RpcRequest.Request.newBuilder().setOperate(RpcOperate.Operate.GetWorkInfo).build().toByteString())
                            .build());
                } catch (RemotingException e) {
                    e.printStackTrace();
                }
            }));
            CountDownLatch latch = new CountDownLatch(1);
            context.getThreadPool().submit(() -> {
                int maxTime = 1000, cnt = 0;
                boolean canExit;
                try {
                    while (cnt++ < maxTime) {
                        canExit = true;
                        for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                            if (workHolder.getWorkInfo() == null) {
                                canExit = false;
                                break;
                            }
                        }
                        if (canExit) {
                            workReady = true;
                            break;
                        }
                        TimeUnit.MILLISECONDS.sleep(10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            context.getMasterSchedule().schedule(() -> {
                workReady = false;
                context.getWorkMap().values().forEach(workHolder -> workHolder.setWorkInfo(null));
            }, 30, TimeUnit.SECONDS);
        }


        Map<String, RpcWorkInfo.WorkInfo> workInfoMap = new HashMap<>(context.getWorkMap().size());
        context.getWorkMap().values().forEach(workHolder -> {
            String host = workHolder.getHeartBeatInfo().getHost();
            if (host.equals(WorkContext.host)) {
                workInfoMap.put(Constants.MASTER_PREFIX + host, workHolder.getWorkInfo());
            } else {
                workInfoMap.put(Constants.WORK_PREFIX + host, workHolder.getWorkInfo());
            }
        });
        return WebResponse.newBuilder()
                .setRid(request.getRid())
                .setOperate(request.getOperate())
                .setStatus(Status.OK)
                .setBody(RpcWorkInfo.AllWorkInfo.newBuilder().putAllValues(workInfoMap).build().toByteString())
                .build();
    }
}