package com.dfire.core.netty.master.response;

import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.logs.HeraLog;
import com.dfire.logs.SocketLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.RpcDebugMessage.DebugMessage;
import com.dfire.protocol.RpcExecuteMessage.ExecuteMessage;
import com.dfire.protocol.RpcOperate.Operate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse.Response;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaosuda
 * @date 2018/11/10
 */
public class MasterExecuteJob {

    public Future<Response> executeJob(final MasterContext context, final MasterWorkHolder holder, ExecuteKind kind, final String id) {
        switch (kind) {
            case ScheduleKind:
                return executeScheduleJob(context, holder, id);
            case ManualKind:
                return executeManualJob(context, holder, id);
            case DebugKind:
                return executeDebugJob(context, holder, id);
            default:
                return null;
        }
    }


    /**
     * 请求work 执行手动任务
     *
     * @param context MasterContext
     * @param holder  MasterWorkHolder
     * @param jobId   String
     * @return Future
     */
    private Future<Response> executeManualJob(MasterContext context, MasterWorkHolder holder, String jobId) {
        holder.getManningRunning().put(jobId, false);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Manual)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(jobId)
                        .build().toByteString())
                .build(), holder, jobId, TriggerTypeEnum.MANUAL);
    }


    /**
     * 请求work 执行调度任务/恢复任务
     *
     * @param context         MasterContext
     * @param holder          MasterWorkHolder
     * @param actionHistoryId String
     * @return Future
     */
    private Future<Response> executeScheduleJob(MasterContext context, MasterWorkHolder holder, String actionHistoryId) {
        final String actionId = context.getHeraJobHistoryService().findById(actionHistoryId).getActionId();
        holder.getRunning().put(actionId, false);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Schedule)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(actionId)
                        .build().toByteString())
                .build(), holder, actionId, TriggerTypeEnum.SCHEDULE);

    }


    /**
     * 请求work 执行开发中心任务
     *
     * @param context MasterContext
     * @param holder  MasterWorkHolder
     * @param id      String
     * @return Future
     */
    private Future<Response> executeDebugJob(MasterContext context, MasterWorkHolder holder, String id) {
        holder.getDebugRunning().put(id, false);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Debug)
                .setBody(DebugMessage
                        .newBuilder()
                        .setDebugId(id)
                        .build().toByteString())
                .build(), holder, id, TriggerTypeEnum.DEBUG);

    }

    /**
     * 向work发送执行任务的命令 并等待work返回结果
     *
     * @param context  MasterContext
     * @param request  Request
     * @param holder   MasterWorkHolder
     * @param actionId String
     * @param typeEnum TriggerTypeEnum
     * @return Future
     */

    private Future<Response> buildFuture(MasterContext context, Request request, MasterWorkHolder holder, String actionId, TriggerTypeEnum typeEnum) {
        Future<Response> future = context.getThreadPool().submit(() -> {
            final CountDownLatch latch = new CountDownLatch(1);
            MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
            context.getHandler().addListener(responseListener);
            try {
                latch.await(3, TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    SocketLog.error("任务({})信号丢失，3小时未收到work返回：{}", typeEnum.toName(), actionId);
                }
            } finally {
                switch (typeEnum) {
                    case MANUAL:
                        holder.getManningRunning().remove(actionId);
                        break;
                    case SCHEDULE:
                        holder.getRunning().remove(actionId);
                        break;
                    case MANUAL_RECOVER:
                        holder.getRunning().remove(actionId);
                        break;
                    case DEBUG:
                        holder.getDebugRunning().remove(actionId);
                        break;
                    default:
                        HeraLog.error("未识别的任务执行类型{}", typeEnum);
                }
            }
            return responseListener.getResponse();
        });
        holder.getChannel().writeAndFlush(SocketMessage
                .newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build());
        TaskLog.info("5.MasterExecuteJob:master send debug command to worker,rid = " + request.getRid() + ",actionId = " + actionId);
        return future;

    }
}
