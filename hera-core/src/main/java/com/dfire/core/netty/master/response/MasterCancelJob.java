package com.dfire.core.netty.master.response;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.*;

import java.text.SimpleDateFormat;
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
    public static RpcWebResponse.WebResponse handleDebugCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        Integer debugId = Integer.parseInt(request.getId());
        HeraDebugHistoryVo debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        for (JobElement element : context.getDebugQueue()) {
            if (element.getJobId().equals(String.valueOf(debugId))) {
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();
                debugHistory.getLog().appendHera("任务取消");
                context.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
                break;

            }
        }

        for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
            if (workHolder.getDebugRunning().contains(debugId)) {
                Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                        workHolder.getChannel(), JobExecuteKind.ExecuteKind.DebugKind, String.valueOf(debugId));
                workHolder.getDebugRunning().remove(debugId);
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    ErrorLog.error("请求超时 ", e);
                }
                webResponse = RpcWebResponse.WebResponse.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(request.getOperate())
                        .setStatus(ResponseStatus.Status.OK)
                        .build();

                SocketLog.info("send web cancel response, actionId = " + debugId);
                break;
            }
        }

        if (webResponse == null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("开发中心任务中找不到匹配的job(" + debugId + ")，无法执行取消命令")
                    .build();
        }
        debugHistory = context.getHeraDebugHistoryService().findById(debugId);
        debugHistory.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        debugHistory.setStatus(StatusEnum.FAILED);
        context.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        return webResponse;


    }

    public static RpcWebResponse.WebResponse handleManualCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String historyId = request.getId();
        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        String actionId = heraJobHistory.getActionId();
        Integer jobId = heraJobHistory.getJobId();
        //手动执行队列 查找该job是否存在
        if (remove(context.getManualQueue().iterator(), actionId)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.OK)
                    .build();

            SocketLog.info("任务仍在手动队列中，从队列删除该任务{}", heraJobHistory.getJobId());
        } else {
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                if (workHolder.getManningRunning().contains(jobId)) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), JobExecuteKind.ExecuteKind.ManualKind, historyId);
                    workHolder.getManningRunning().remove(jobId);
                    try {
                        future.get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        ErrorLog.error("请求超时 ", e);
                    }
                    SocketLog.info("远程从删除该任务{}", heraJobHistory.getJobId());
                    webResponse = RpcWebResponse.WebResponse.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(request.getOperate())
                            .setStatus(ResponseStatus.Status.OK)
                            .build();
                    SocketLog.info("send web cancel response, actionId = " + historyId);
                }
            }
        }

        if (webResponse == null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("手动执行任务中找不到匹配的job(" + heraJobHistory.getJobId() + "," + actionId + ")，无法执行取消命令")
                    .build();
        }
        heraJobHistory.setIllustrate(Constants.CANCEL_JOB_MESSAGE);
        heraJobHistory.setEndTime(new Date());
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        context.getHeraJobHistoryService().update(heraJobHistory);
        HeraAction heraAction = context.getMaster().getHeraActionMap().get(Long.parseLong(actionId));
        if (heraAction != null) {
            heraAction.setStatus(StatusEnum.FAILED.toString());
        }
        context.getHeraJobActionService().updateStatus(HeraAction.builder().id(Long.parseLong(actionId)).status(StatusEnum.FAILED.toString()).build());
        return webResponse;
    }

    public static RpcWebResponse.WebResponse handleScheduleCancel(MasterContext context, RpcWebRequest.WebRequest request) {
        RpcWebResponse.WebResponse webResponse = null;
        String historyId = request.getId();
        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(historyId);
        Integer jobId = heraJobHistory.getJobId();
        String actionId = heraJobHistory.getActionId();

        if (remove(context.getScheduleQueue().iterator(), actionId)) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.OK)
                    .build();
            SocketLog.info("任务仍在调度队列中，从队列删除该任务{}", actionId);

        } else {
            for (MasterWorkHolder workHolder : context.getWorkMap().values()) {
                if (workHolder.getRunning().contains(jobId)) {
                    Future<RpcResponse.Response> future = new MasterHandleCancelJob().cancel(context,
                            workHolder.getChannel(), JobExecuteKind.ExecuteKind.ScheduleKind, historyId);
                    workHolder.getRunning().remove(jobId);
                    try {
                        future.get(HeraGlobalEnv.getRequestTimeout(), TimeUnit.SECONDS);
                    } catch (Exception e) {
                        ErrorLog.error("请求超时 ", e);
                    }
                    SocketLog.info("远程删除该任务{}", actionId);
                    webResponse = RpcWebResponse.WebResponse.newBuilder()
                            .setRid(request.getRid())
                            .setOperate(request.getOperate())
                            .setStatus(ResponseStatus.Status.OK)
                            .build();
                    SocketLog.info("send web cancel response, actionId = " + jobId);
                }
            }
        }

        if (webResponse == null) {
            webResponse = RpcWebResponse.WebResponse.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(request.getOperate())
                    .setStatus(ResponseStatus.Status.ERROR)
                    .setErrorText("调度队列中找不到匹配的job(" + heraJobHistory.getJobId() + "," + heraJobHistory.getActionId() + ")，无法执行取消命令")
                    .build();
        }
        heraJobHistory.setEndTime(new Date());
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        heraJobHistory.setIllustrate(Constants.CANCEL_JOB_MESSAGE);
        HeraAction heraAction = context.getMaster().getHeraActionMap().get(Long.parseLong(actionId));
        if (heraAction != null) {
            heraAction.setStatus(StatusEnum.FAILED.toString());
        }
        context.getHeraJobHistoryService().update(heraJobHistory);
        context.getHeraJobActionService().updateStatus(HeraAction.builder().id(Long.parseLong(actionId)).status(StatusEnum.FAILED.toString()).build());
        return webResponse;
    }


    private static boolean remove(Iterator<JobElement> iterator, String id) {
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
