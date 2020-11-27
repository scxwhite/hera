package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.master.RunJobThreadPool;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.*;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 取消任务统一管理类
 *
 * @author xiaosuda
 * @date 2018/11/9
 */
public class MasterCancelJob {

    private static final String CANCEN_MSG = "取消成功";

    public static RpcWebResponse.WebResponse cancel(JobExecuteKind.ExecuteKind kind, MasterContext context, String id, Integer rid, RpcWebOperate.WebOperate operate) {
        RpcWebResponse.WebResponse webResponse = null;
        Long historyId = Long.parseLong(id);
        Long actionId = historyId;
        Iterator<JobElement> iterator = null;
        TriggerTypeEnum typeEnum;
        HeraJobHistory jobHistory = null;
        HeraDebugHistory debugHistory = null;
        if (kind == JobExecuteKind.ExecuteKind.DebugKind) {
            debugHistory = context.getHeraDebugHistoryService().findLogById(historyId);
            typeEnum = TriggerTypeEnum.DEBUG;
        } else {
            jobHistory = context.getHeraJobHistoryService().findById(historyId);
            actionId = jobHistory.getActionId();
            typeEnum = TriggerTypeEnum.parser(jobHistory.getTriggerType());
        }
        switch (Objects.requireNonNull(typeEnum)) {
            case SUPER_RECOVER:
                iterator = context.getSuperRecovery().iterator();
                kind = JobExecuteKind.ExecuteKind.SuperRecoveryKind;
                break;
            case MANUAL_RECOVER:
            case SCHEDULE:
                kind = JobExecuteKind.ExecuteKind.ScheduleKind;
                iterator = context.getScheduleQueue().iterator();
                break;
            case MANUAL:
                kind = JobExecuteKind.ExecuteKind.ManualKind;

                iterator = context.getManualQueue().iterator();
                break;
            case AUTO_RERUN:
                kind = JobExecuteKind.ExecuteKind.AutoRerunKind;
                iterator = context.getRerunQueue().iterator();
                break;
            case DEBUG:
                kind = JobExecuteKind.ExecuteKind.DebugKind;
                iterator = context.getDebugQueue().iterator();
                break;
            default:
                ErrorLog.error("无法识别的触发类型:" + typeEnum);
                break;
        }
        if (RunJobThreadPool.cancelJob(actionId, typeEnum)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(rid)
                    .setOperate(operate)
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("任务在等待创建集群中,已取消{}", actionId);
        }
        //首先在队列中查找该job是否存在
        if (remove(iterator, actionId)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(rid)
                    .setOperate(operate)
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("任务仍在手动队列中，从队列删除该任务{}", actionId);
        } else {
            //队列不存在，说明已经分发给work了
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                boolean exists = false;

                switch (typeEnum) {
                    case DEBUG:
                        exists = workHolder.getDebugRunning().contains(actionId);
                        break;
                    case SCHEDULE:
                    case MANUAL_RECOVER:
                        exists = workHolder.getRunning().contains(actionId);
                        break;
                    case MANUAL:
                        exists = workHolder.getManningRunning().contains(actionId);
                        break;
                    case AUTO_RERUN:
                        exists = workHolder.getRerunRunning().contains(actionId);
                        break;
                    case SUPER_RECOVER:
                        exists = workHolder.getSuperRunning().contains(actionId);
                        break;
                    default:
                        ErrorLog.warn("不支持的触发类型:" + typeEnum);
                }
                if (exists) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), kind, historyId);
                    try {
                        future.get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        ErrorLog.error("请求超时 ", e);
                    } finally {
                        webResponse = RpcWebResponse.WebResponse.newBuilder()
                                .setRid(rid)
                                .setOperate(operate)
                                .setStatus(ResponseStatus.Status.OK)
                                .build();
                        SocketLog.info("send web cancel response, actionId = " + historyId);
                    }
                    break;
                }
            }
        }

        if (webResponse == null) {
            if (jobHistory != null) {
                jobHistory.setStatus(StatusEnum.FAILED.toString());
                jobHistory.setEndTime(new Date());
                jobHistory.setIllustrate(CANCEN_MSG);
                context.getHeraJobHistoryService().update(jobHistory);
            } else if (debugHistory != null) {
                debugHistory.setStatus(StatusEnum.FAILED.toString());
                debugHistory.setEndTime(new Date());
                context.getHeraDebugHistoryService().update(debugHistory);
            }

            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(rid)
                    .setOperate(operate)
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText(CANCEN_MSG)
                    .build();
        }
        return webResponse;
    }


    private static boolean remove(Iterator<JobElement> iterator, Long id) {
        if (iterator == null) {
            return false;
        }
        JobElement jobElement;
        while (iterator.hasNext()) {
            jobElement = iterator.next();
            if (jobElement.getJobId().equals(id)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
