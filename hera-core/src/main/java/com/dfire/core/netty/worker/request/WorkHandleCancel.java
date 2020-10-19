package com.dfire.core.netty.worker.request;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.core.job.Job;
import com.dfire.core.netty.worker.HistoryPair;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.*;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Map;
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

            Long id = Long.parseLong(cancelMessage.getId());
            switch (cancelMessage.getEk()) {
                case ManualKind:
                    return cancelJob(workContext, request, id, workContext.getManualRunning());
                case ScheduleKind:
                    return cancelJob(workContext, request, id, workContext.getRunning());
                case SuperRecoveryKind:
                    return cancelJob(workContext, request, id, workContext.getSuperRunning());
                case AutoRerunKind:
                    return cancelJob(workContext, request, id, workContext.getRerunRunning());
                case DebugKind:
                    return cancelDebug(workContext, request, id);
                default:
                    ErrorLog.error("不支持的取消类型:" + cancelMessage.getEk().name());
            }
        } catch (InvalidProtocolBufferException e) {
            ErrorLog.error("解析异常", e);
        }
        return null;
    }


    private Future<RpcResponse.Response> cancelJob(WorkContext workContext, RpcRequest.Request request, Long historyId, Map<HistoryPair, Job> running) {
        HeraJobHistory heraJobHistory = workContext.getHeraJobHistoryService().findById(historyId);
        Long actionId = heraJobHistory.getActionId();
        SocketLog.info("worker receive cancel job, actionId =" + actionId);
        return workContext.getWorkExecuteThreadPool().submit(() -> {
            workContext.getWorkClient().cancelJob(running.get(new HistoryPair(actionId, historyId)));
            return RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.OK)
                    .build();
        });
    }

    /**
     * 取消执行开发中心任务，先判断任务是否在运行队列中，再执行取消任务逻辑
     */
    private Future<RpcResponse.Response> cancelDebug(WorkContext workContext, RpcRequest.Request request, Long debugId) {
        return workContext.getWorkExecuteThreadPool().submit(() -> {
            workContext.getWorkClient().cancelDebugJob(debugId);
            return RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setOperate(RpcOperate.Operate.Cancel)
                    .setStatusEnum(ResponseStatus.Status.OK)
                    .build();
        });
    }

}
