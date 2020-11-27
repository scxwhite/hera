package com.dfire.core.netty.master.response;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobElement;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.master.RunJobThreadPool;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.event.Events;
import com.dfire.event.HeraJobMaintenanceEvent;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;
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
import java.util.stream.Collectors;

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
            Long historyId = Long.parseLong(request.getId());
            HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
            HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
            context.getMaster().run(history, context.getHeraJobService().findById(history.getJobId()));
            WebResponse webResponse = WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(WebOperate.ExecuteJob)
                    .setStatus(Status.OK)
                    .build();
            TaskLog.info("MasterHandlerWebResponse: send web execute response, actionId = {} ", history.getJobId());
            return webResponse;
        } else if (request.getEk() == ExecuteKind.DebugKind) {
            Long debugId = Long.parseLong(request.getId());
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
        Long debugId = Long.parseLong(request.getId());
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
        Long id = Long.parseLong(request.getId());
        context.getMaster().generateSingleAction(Integer.parseInt(request.getId()));
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
        boolean result = String.valueOf(Constants.ALL_JOB_ID).equals(request.getId()) ? context.getMaster().generateBatchAction(true) : context.getMaster().generateSingleAction(Integer.parseInt(request.getId()));
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
        switch (request.getEk()) {
            case ManualKind:
            case DebugKind:
            case ScheduleKind:
            case AutoRerunKind:
                return MasterCancelJob.cancel(request.getEk(), context, request.getId(), request.getRid(), request.getOperate());
            default:
                return WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.ERROR)
                        .setErrorText("无法识别的任务取消类型：" + request.getEk())
                        .build();
        }
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
                        .addAllDebugRunning(beatInfo.getDebugRunning())
                        .addAllRunning(beatInfo.getRunning())
                        .addAllManualRunning(beatInfo.getManualRunning())
                        .addAllRerunRunning(beatInfo.getRerunRunning())
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
        List<Long> waitDebugQueue = RunJobThreadPool.getWaitClusterJob(TriggerTypeEnum.DEBUG);
        List<Long> masterDebugQueue = new ArrayList<>(waitDebugQueue.size() + context.getDebugQueue().size());
        context.getDebugQueue().forEach(jobElement -> masterDebugQueue.add(jobElement.getJobId()));
        masterDebugQueue.addAll(waitDebugQueue);
        //自动任务队列

        waitDebugQueue = RunJobThreadPool.getWaitClusterJob(TriggerTypeEnum.SCHEDULE, TriggerTypeEnum.MANUAL_RECOVER);
        List<Long> masterScheduleQueue = new ArrayList<>(waitDebugQueue.size() + context.getScheduleQueue().size());
        masterScheduleQueue.addAll(waitDebugQueue);
        context.getScheduleQueue().forEach(jobElement -> masterScheduleQueue.add(jobElement.getJobId()));

        //手动任务队列
        waitDebugQueue = RunJobThreadPool.getWaitClusterJob(TriggerTypeEnum.MANUAL);
        List<Long> masterManualQueue = new ArrayList<>(waitDebugQueue.size() + context.getManualQueue().size());
        masterManualQueue.addAll(waitDebugQueue);
        context.getManualQueue().forEach(jobElement -> masterManualQueue.add(jobElement.getJobId()));

        //重跑任务队列
        waitDebugQueue = RunJobThreadPool.getWaitClusterJob(TriggerTypeEnum.AUTO_RERUN);
        List<Long> masterRerunQueue = new ArrayList<>(waitDebugQueue.size() + context.getRerunQueue().size());
        masterRerunQueue.addAll(waitDebugQueue);
        context.getRerunQueue().forEach(jobElement -> masterRerunQueue.add(jobElement.getJobId()));
        //超级恢复队列
        waitDebugQueue = RunJobThreadPool.getWaitClusterJob(TriggerTypeEnum.SUPER_RECOVER);
        List<Long> masterSuperRecoveryQueue = new ArrayList<>(waitDebugQueue.size() + context.getSuperRecovery().size());
        masterSuperRecoveryQueue.addAll(waitDebugQueue);
        context.getSuperRecovery().forEach(jobElement -> masterSuperRecoveryQueue.add(jobElement.getJobId()));

        MemUseRateJob memUseRateJob = new MemUseRateJob(1);
        memUseRateJob.readMemUsed();
        CpuLoadPerCoreJob loadPerCoreJob = new CpuLoadPerCoreJob();
        loadPerCoreJob.run();

        allInfo.put(Constants.MASTER_PREFIX + WorkContext.host, HeartBeatMessage.newBuilder()
                .addAllDebugRunning(masterDebugQueue.stream().map(String::valueOf).collect(Collectors.toList()))
                .addAllRunning(masterScheduleQueue.stream().map(String::valueOf).collect(Collectors.toList()))
                .addAllManualRunning(masterManualQueue.stream().map(String::valueOf).collect(Collectors.toList()))
                .addAllRerunRunning(masterRerunQueue.stream().map(String::valueOf).collect(Collectors.toList()))
                .addAllSuperRunning(masterSuperRecoveryQueue.stream().map(String::valueOf).collect(Collectors.toList()))
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
            HeraLog.info("workInfo未准备，准备请求work组装workInfo");
            //发送workInfo build 请求
            context.getThreadPool().submit(() -> context.getWorkMap().values().parallelStream().forEach(workHolder -> {
                try {
                    workHolder.getChannel().writeAndFlush(RpcSocketMessage.SocketMessage.newBuilder()
                            .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                            .setBody(RpcRequest.Request.newBuilder().setOperate(RpcOperate.Operate.GetWorkInfo).build().toByteString())
                            .build());
                } catch (RemotingException e) {
                    ErrorLog.error("发送消息异常", e);
                }
            }));
            CountDownLatch latch = new CountDownLatch(1);
            context.getThreadPool().submit(() -> {
                int maxTime = 300, cnt = 0;
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
                            HeraLog.info("所有workInfo已准备完毕");
                            workReady = true;
                            break;
                        }
                        TimeUnit.MILLISECONDS.sleep(10);
                    }
                } catch (InterruptedException e) {
                    ErrorLog.error("InterruptedException ", e);
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                ErrorLog.error("InterruptedException", e);
            }

            context.getMasterSchedule().schedule(() -> {
                HeraLog.info("开始清理workInfo");
                workReady = false;
                context.getWorkMap().values().forEach(workHolder -> workHolder.setWorkInfo(null));
            }, 30, TimeUnit.SECONDS);
        }
        HeraLog.info("开始组装workInfo");

        Map<String, RpcWorkInfo.WorkInfo> workInfoMap = new HashMap<>(context.getWorkMap().size());
        context.getWorkMap().values().forEach(workHolder -> {
            String host = workHolder.getHeartBeatInfo().getHost();
            if (workHolder.getWorkInfo() != null) {
                if (host.equals(WorkContext.host)) {
                    workInfoMap.put(Constants.MASTER_PREFIX + host, workHolder.getWorkInfo());
                } else {
                    workInfoMap.put(Constants.WORK_PREFIX + host, workHolder.getWorkInfo());
                }
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
