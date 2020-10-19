package com.dfire.core.netty.master.response;

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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 取消任务统一管理类
 *
 * @author xiaosuda
 * @date 2018/11/9
 */
public class MasterCancelJob {

    private static final String NOT_FOUNT_MSG = "任务已结束";

    public static RpcWebResponse.WebResponse cancel(JobExecuteKind.ExecuteKind ek, MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        Long historyId = Long.parseLong(request.getId());
        Long actionId = historyId;
        Iterator<JobElement> iterator;
        TriggerTypeEnum typeEnum;
        HeraJobHistory scheduleHistory = null, manualHistory = null;
        if (ek == JobExecuteKind.ExecuteKind.ScheduleKind) {
            scheduleHistory = context.getHeraJobHistoryService().findById(historyId);
            actionId = scheduleHistory.getActionId();
            iterator = context.getManualQueue().iterator();
            typeEnum = TriggerTypeEnum.MANUAL_RECOVER;
        } else if (ek == JobExecuteKind.ExecuteKind.ManualKind) {
            manualHistory = context.getHeraJobHistoryService().findById(historyId);
            actionId = manualHistory.getActionId();
            iterator = context.getScheduleQueue().iterator();
            typeEnum = TriggerTypeEnum.MANUAL;

        } else {
            iterator = context.getDebugQueue().iterator();
            typeEnum = TriggerTypeEnum.DEBUG;
        }

        if (RunJobThreadPool.cancelJob(actionId, typeEnum)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("任务在等待创建集群中,已取消{}", actionId);
        }
        //首先在队列中查找该job是否存在
        if (remove(iterator, actionId)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("任务仍在手动队列中，从队列删除该任务{}", actionId);
        } else {
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                boolean exists = false;
                if (ek == JobExecuteKind.ExecuteKind.ScheduleKind && workHolder.getRunning().contains(actionId)) {
                    exists = true;
                } else if (ek == JobExecuteKind.ExecuteKind.ManualKind && workHolder.getManningRunning().contains(actionId)) {
                    exists = true;
                } else if (ek == JobExecuteKind.ExecuteKind.DebugKind && workHolder.getDebugRunning().contains(actionId)) {
                    exists = true;
                }
                if (exists) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), ek, historyId);
                    try {
                        future.get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        ErrorLog.error("请求超时 ", e);
                    } finally {
                        webResponse = RpcWebResponse.WebResponse.newBuilder()
                                .setRid(request.getRid())
                                .setOperate(request.getOperate())
                                .setStatus(ResponseStatus.Status.OK)
                                .build();
                        SocketLog.info("send web cancel response, actionId = " + historyId);
                    }
                }
            }
        }
        if (webResponse == null) {

            if (manualHistory != null) {
                manualHistory.setStatus(StatusEnum.FAILED.toString());
                manualHistory.setEndTime(new Date());
                manualHistory.setIllustrate(NOT_FOUNT_MSG);
                context.getHeraJobHistoryService().update(manualHistory);
            } else if (scheduleHistory != null) {
                scheduleHistory.setStatus(StatusEnum.FAILED.toString());
                scheduleHistory.setEndTime(new Date());
                scheduleHistory.setIllustrate(NOT_FOUNT_MSG);
                context.getHeraJobHistoryService().update(scheduleHistory);
            }

            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText(NOT_FOUNT_MSG)
                    .build();
        }
        return webResponse;
    }


    private static boolean remove(Iterator<JobElement> iterator, Long id) {
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
