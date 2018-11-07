package com.dfire.core.netty.worker.request;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.*;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午10:57 2018/5/11
 * @desc worker端执行接受到master hander端的取消任务指令的时候，开始执行取消任务逻辑
 */
public class WorkHandleCancel {

    public Future<RpcResponse.Response> handleCancel(final WorkContext workContext, final RpcRequest.Request request) {
        try {
            RpcCancelMessage.CancelMessage cancelMessage = RpcCancelMessage.CancelMessage.newBuilder()
                    .mergeFrom(request.getBody())
                    .build();
            if (cancelMessage.getEk() == JobExecuteKind.ExecuteKind.DebugKind) {
                return cancelDebug(workContext, request, cancelMessage.getId());
            } else if (cancelMessage.getEk() == JobExecuteKind.ExecuteKind.ScheduleKind) {
                return cancelSchedule(workContext, request, cancelMessage.getId());
            } else if (cancelMessage.getEk() == JobExecuteKind.ExecuteKind.ManualKind) {
                return cancelManual(workContext, request, cancelMessage.getId());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 取消执行手动任务，先判断任务是否在运行队列中，再执行取消任务逻辑
     *
     * @param workContext
     * @param request
     * @param historyId
     * @return
     */
    private Future<RpcResponse.Response> cancelManual(WorkContext workContext, RpcRequest.Request request, String historyId) {
        HeraJobHistory heraJobHistory = workContext.getJobHistoryService().findById(historyId);
        HeraJobHistoryVo history = BeanConvertUtils.convert(heraJobHistory);
        final String jobId = history.getJobId();
        SocketLog.info("worker receive cancel manual job, actionId =" + jobId);
        if (!workContext.getManualRunning().containsKey(history.getId())) {
            return workContext.getWorkThreadPool().submit(() -> RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.ERROR)
                    .setErrorText("运行任务中查无此任务")
                    .build());
        }
        return workContext.getWorkThreadPool().submit(() -> {
            workContext.getWorkClient().cancelManualJob(historyId);
            return RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.OK)
                    .build();
        });
    }

    /**
     * 取消执行调度任务，先判断任务是否在运行队列中，再执行取消任务逻辑
     *
     * @param workContext
     * @param request
     * @param historyId
     * @return
     */
    private Future<RpcResponse.Response> cancelSchedule(WorkContext workContext, RpcRequest.Request request, String historyId) {
        HeraJobHistory heraJobHistory = workContext.getJobHistoryService().findById(historyId);
        String actionId = heraJobHistory.getActionId();
        SocketLog.info("worker receive cancel schedule job, actionId =" + actionId);
        if (!workContext.getRunning().containsKey(actionId)) {
            return workContext.getWorkThreadPool().submit(() -> RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.ERROR)
                    .setErrorText("运行任务中查无此任务")
                    .build());
        }
        return workContext.getWorkThreadPool().submit(() -> {
            workContext.getWorkClient().cancelScheduleJob(actionId);
            return RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.OK)
                    .build();
        });
    }

    /**
     * 取消执行开发中心任务，先判断任务是否在运行队列中，再执行取消任务逻辑
     *
     * @param workContext
     * @param request
     * @param debugId
     * @return
     */
    private Future<RpcResponse.Response> cancelDebug(WorkContext workContext, RpcRequest.Request request, String debugId) {
        Future<RpcResponse.Response> future = null;
        if (!workContext.getDebugRunning().containsKey(debugId)) {
            future = workContext.getWorkThreadPool().submit(() -> RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.ERROR)
                    .setErrorText("运行任务中查无此任务")
                    .build());
            HeraDebugHistoryVo debugHistory = workContext.getDebugHistoryService().findById(debugId);
            debugHistory.setStatus(StatusEnum.FAILED);
            debugHistory.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            workContext.getDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        } else {
            future = workContext.getWorkThreadPool().submit(() -> {
                workContext.getWorkClient().cancelDebugJob(debugId);
                return RpcResponse.Response.newBuilder()
                        .setRid(request.getRid())
                        .setOperate(RpcOperate.Operate.Cancel)
                        .setStatusEnum(ResponseStatus.Status.OK)
                        .build();
            });
        }
        return future;
    }

}
