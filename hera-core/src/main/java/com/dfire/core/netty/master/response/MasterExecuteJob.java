package com.dfire.core.netty.master.response;

import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.ActionUtil;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.RpcDebugMessage.DebugMessage;
import com.dfire.protocol.RpcExecuteMessage.ExecuteMessage;
import com.dfire.protocol.RpcOperate.Operate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse.Response;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import io.netty.channel.ChannelFuture;

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
     * @param context    MasterContext
     * @param workHolder MasterWorkHolder
     * @param actionId   String
     * @return Future
     */
    private Future<Response> executeManualJob(MasterContext context, MasterWorkHolder workHolder, String actionId) {
        String jobId = ActionUtil.getJobId(actionId);
        workHolder.getManningRunning().add(jobId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Manual)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(actionId)
                        .build().toByteString())
                .build(), workHolder, actionId, TriggerTypeEnum.MANUAL, jobId);
    }


    /**
     * 请求work 执行调度任务/恢复任务
     *
     * @param context    MasterContext
     * @param workHolder MasterWorkHolder
     * @param actionId   String
     * @return Future
     */
    private Future<Response> executeScheduleJob(MasterContext context, MasterWorkHolder workHolder, String actionId) {
        String jobId = ActionUtil.getJobId(actionId);
        workHolder.getRunning().add(jobId);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Schedule)
                .setBody(ExecuteMessage
                        .newBuilder()
                        .setActionId(actionId)
                        .build().toByteString())
                .build(), workHolder, actionId, TriggerTypeEnum.SCHEDULE, jobId);

    }


    /**
     * 请求work 执行开发中心任务
     *
     * @param context MasterContext
     * @param workHolder  MasterWorkHolder
     * @param id      String
     * @return Future
     */
    private Future<Response> executeDebugJob(MasterContext context, MasterWorkHolder workHolder, String id) {
        workHolder.getDebugRunning().add(id);
        return buildFuture(context, Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Debug)
                .setBody(DebugMessage
                        .newBuilder()
                        .setDebugId(id)
                        .build().toByteString())
                .build(), workHolder, id, TriggerTypeEnum.DEBUG, id);

    }

    /**
     * 向work发送执行任务的命令 并等待work返回结果
     *
     * @param context  MasterContext
     * @param request  Request
     * @param holder   MasterWorkHolder
     * @param actionId String
     * @param typeEnum TriggerTypeEnum
     * @param jobId    jobId
     * @return Future
     */

    private Future<Response> buildFuture(MasterContext context, Request request, MasterWorkHolder holder, String actionId, TriggerTypeEnum typeEnum, String jobId) {
        final CountDownLatch latch = new CountDownLatch(1);
        MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
        context.getHandler().addListener(responseListener);
        Future<Response> future = context.getThreadPool().submit(() -> {
            try {
                latch.await(3, TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    TaskLog.error("任务({})信号丢失，3小时未收到work返回：{}", typeEnum.toName(), actionId);
                }
            } finally {
                switch (typeEnum) {
                    case MANUAL:
                        holder.getManningRunning().remove(jobId);
                        break;
                    case SCHEDULE:
                        holder.getRunning().remove(jobId);
                        break;
                    case MANUAL_RECOVER:
                        holder.getRunning().remove(jobId);
                        break;
                    case DEBUG:
                        holder.getDebugRunning().remove(jobId);
                        break;
                    default:
                        TaskLog.error("未识别的任务执行类型{}", typeEnum);
                }
            }
            return responseListener.getResponse();
        });

        ChannelFuture channelFuture = holder.getChannel().writeAndFlush(SocketMessage
                .newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build());

        boolean success = false;
        try {
            success = channelFuture.await(HeraGlobalEnvironment.getChannelTimeout());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Throwable cause = channelFuture.cause();
        if (cause != null) {
            TaskLog.error("send execute job exception {}", cause);
            return null;
        } else if (success) {
            TaskLog.info("5.MasterExecuteJob:master send debug command to worker,rid = " + request.getRid() + ",actionId = " + actionId + ",address " + holder.getChannel().remoteAddress());
        } else {
            TaskLog.error("5.MasterExecuteJob:master send debug command to worker timeout,rid = " + request.getRid() + ",actionId = " + actionId + ",address " + holder.getChannel().remoteAddress());
            return null;
        }
        return future;

    }
}
