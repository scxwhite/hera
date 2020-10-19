package com.dfire.core.netty.master.response;

import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.logs.ErrorLog;
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

    public Future<Response> executeJob(final MasterContext context, final MasterWorkHolder holder, TriggerTypeEnum triggerTypeEnum, final Long id) {
        switch (triggerTypeEnum) {
            case MANUAL_RECOVER:
            case SCHEDULE:
                return executeScheduleJob(context, holder, id, triggerTypeEnum);
            case SUPER_RECOVER:
                return executeSuperRecoveryJob(context, holder, id, triggerTypeEnum);
            case MANUAL:
                return executeManualJob(context, holder, id, triggerTypeEnum);
            case DEBUG:
                return executeDebugJob(context, holder, id, triggerTypeEnum);
            case AUTO_RERUN:
                return executeRerunJob(context, holder, id, triggerTypeEnum);
            default:
                return null;
        }
    }


    /**
     * 请求work 执行手动任务
     *
     * @param context         MasterContext
     * @param workHolder      MasterWorkHolder
     * @param triggerTypeEnum
     * @return Future
     */
    private Future<Response> executeManualJob(MasterContext context, MasterWorkHolder workHolder, Long actionId, TriggerTypeEnum triggerTypeEnum) {
        workHolder.getManningRunning().add(actionId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Manual)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(String.valueOf(actionId))
                        .build().toByteString())
                .build(), workHolder, actionId, triggerTypeEnum);
    }

    /**
     * 请求work 执行自动重跑任务
     *
     * @param context    MasterContext
     * @param workHolder MasterWorkHolder
     * @return Future
     */
    private Future<Response> executeSuperRecoveryJob(MasterContext context, MasterWorkHolder workHolder, Long actionId, TriggerTypeEnum triggerTypeEnum) {
        workHolder.getSuperRunning().add(actionId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.SuperRecovery)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(String.valueOf(actionId))
                        .build().toByteString())
                .build(), workHolder, actionId, triggerTypeEnum);
    }


    /**
     * 请求work 执行自动重跑任务
     *
     * @param context         MasterContext
     * @param workHolder      MasterWorkHolder
     * @param triggerTypeEnum
     * @return Future
     */
    private Future<Response> executeRerunJob(MasterContext context, MasterWorkHolder workHolder, Long actionId, TriggerTypeEnum triggerTypeEnum) {
        workHolder.getRerunRunning().add(actionId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Rerun)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(String.valueOf(actionId))
                        .build().toByteString())
                .build(), workHolder, actionId, triggerTypeEnum);
    }

    /**
     * 请求work 执行调度任务/恢复任务
     *
     * @param context         MasterContext
     * @param workHolder      MasterWorkHolder
     * @param actionId        String
     * @param triggerTypeEnum
     * @return Future
     */
    private Future<Response> executeScheduleJob(MasterContext context, MasterWorkHolder workHolder, Long actionId, TriggerTypeEnum triggerTypeEnum) {
        workHolder.getRunning().add(actionId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Schedule)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(String.valueOf(actionId))
                        .build().toByteString())
                .build(), workHolder, actionId, triggerTypeEnum);

    }


    /**
     * 请求work 执行开发中心任务
     *
     * @param context         MasterContext
     * @param workHolder      MasterWorkHolder
     * @param id              String
     * @param triggerTypeEnum
     * @return Future
     */
    private Future<Response> executeDebugJob(MasterContext context, MasterWorkHolder workHolder, Long id, TriggerTypeEnum triggerTypeEnum) {
        workHolder.getDebugRunning().add(id);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Debug)
                .setBody(DebugMessage
                        .newBuilder()
                        .setDebugId(String.valueOf(id))
                        .build().toByteString())
                .build(), workHolder, id, triggerTypeEnum);

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

    private Future<Response> buildFuture(MasterContext context, Request request, MasterWorkHolder holder, Long actionId, TriggerTypeEnum typeEnum) {
        final CountDownLatch latch = new CountDownLatch(1);
        MasterResponseListener responseListener = new MasterResponseListener(request, false, latch, null);
        context.getHandler().addListener(responseListener);
        Future<Response> future = context.getThreadPool().submit(() -> {
            try {
                latch.await(HeraGlobalEnv.getTaskTimeout(), TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    ErrorLog.error("任务({})信号丢失，{}小时未收到work返回：{}", typeEnum.toName(), HeraGlobalEnv.getTaskTimeout(), actionId);
                }
            } finally {
                context.getHandler().removeListener(responseListener);
                switch (typeEnum) {
                    case MANUAL:
                        holder.getManningRunning().remove(actionId);
                        break;
                    case SCHEDULE:
                    case MANUAL_RECOVER:
                        holder.getRunning().remove(actionId);
                        break;
                    case DEBUG:
                        holder.getDebugRunning().remove(actionId);
                        break;
                    case AUTO_RERUN:
                        holder.getRerunRunning().remove(actionId);
                        break;
                    case SUPER_RECOVER:
                        holder.getSuperRunning().remove(actionId);
                        break;
                    default:
                        ErrorLog.warn("未识别的任务执行类型{}", typeEnum);
                }
            }
            return responseListener.getResponse();
        });
        try {
            holder.getChannel().writeAndFlush(SocketMessage
                    .newBuilder()
                    .setKind(SocketMessage.Kind.REQUEST)
                    .setBody(request.toByteString())
                    .build());
            TaskLog.info("5.MasterExecuteJob:master send debug command to worker,rid = " + request.getRid() + ",actionId = " + actionId + ",address " + holder.getChannel().getRemoteAddress());
        } catch (RemotingException e) {
            context.getHandler().removeListener(responseListener);
            ErrorLog.error("5.MasterExecuteJob:master send debug command to worker exception,rid = " + request.getRid() + ",actionId = " + actionId + ",address " + holder.getChannel().getRemoteAddress(), e);
        }
        return future;

    }
}
