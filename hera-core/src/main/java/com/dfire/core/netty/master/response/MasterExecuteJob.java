package com.dfire.core.netty.master.response;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.protocol.*;
import com.dfire.protocol.JobExecuteKind.ExecuteKind;
import com.dfire.protocol.RpcResponse.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:26 2018/4/25
 * @desc master向worker发送执行job的指令
 */
@Slf4j
public class MasterExecuteJob {

    public Future<Response> executeJob(final MasterContext context, final MasterWorkHolder holder, ExecuteKind kind, final String id) {
        if (kind == ExecuteKind.DebugKind) {
            return executeDebugJob(context, holder, id);
        } else if (kind == ExecuteKind.ScheduleKind) {
            return executeScheduleJob(context, holder, id);
        } else if (kind == ExecuteKind.ManualKind) {
            return executeManualJob(context, holder, id);
        }
        return null;
    }

    /**
     * 执行手动任务，向channel发送执行job命令，等待worker响应，响应ok,则添加监听器，继续等待任务完成消息，响应失败，返回失败退出码
     *
     * @param context
     * @param holder
     * @param jobId
     * @return
     */
    private Future<Response> executeManualJob(MasterContext context, MasterWorkHolder holder, String jobId) {
        holder.getManningRunning().put(jobId, false);

        RpcExecuteMessage.ExecuteMessage message = RpcExecuteMessage.ExecuteMessage.newBuilder().setActionId(jobId).build();
        final RpcRequest.Request request = RpcRequest.Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcOperate.Operate.Manual)
                .setBody(message.toByteString())
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage.newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(() -> {
            final CountDownLatch latch = new CountDownLatch(1);
            MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
            context.getHandler().addListener(responseListener);
            try {
                latch.await(3, TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    log.error("手动任务信号丢失，三小时未收到work返回：{}", jobId);
                }
            } finally {
                holder.getRunning().remove(jobId);
            }
            return responseListener.getResponse();
        });
        holder.getChannel().writeAndFlush(socketMessage);
        return future;
    }

    /**
     * 执行自动调度任务，向master端channel发送执行job命令，添加请求监听器，继续等待任务完成消息，响应失败，返回失败退出码
     *
     * @param context
     * @param holder
     * @param actionHistoryId
     * @return
     */
    private Future<Response> executeScheduleJob(MasterContext context, MasterWorkHolder holder, String actionHistoryId) {

        HeraJobHistory heraJobHistory = context.getHeraJobHistoryService().findById(actionHistoryId);
        final String actionId = heraJobHistory.getActionId();
        holder.getRunning().put(actionId, false);
        RpcExecuteMessage.ExecuteMessage message = RpcExecuteMessage.ExecuteMessage.newBuilder().setActionId(actionId).build();
        final RpcRequest.Request request = RpcRequest.Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcOperate.Operate.Schedule)
                .setBody(message.toByteString())
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage.newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(() -> {
            final CountDownLatch latch = new CountDownLatch(1);
            MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
            context.getHandler().addListener(responseListener);
            try {
                latch.await(3, TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    log.error("自动调度任务信号丢失，三小时未收到work返回：{}", actionHistoryId);
                    //TODO 可以做一些处理
                }
            } finally {
                holder.getRunning().remove(actionId);
            }
            return responseListener.getResponse();
        });
        holder.getChannel().writeAndFlush(socketMessage);
        return future;

    }

    /**
     * 执行开发中心脚本，向master端channel发送执行job命令，添加请求监听器，继续等待任务完成消息，响应失败，返回失败退出码
     *
     * @param context
     * @param holder
     * @param id
     * @return
     */
    private Future<Response> executeDebugJob(MasterContext context, MasterWorkHolder holder, String id) {
        holder.getDebugRunning().put(id, false);
        RpcDebugMessage.DebugMessage message = RpcDebugMessage.DebugMessage
                .newBuilder()
                .setDebugId(id)
                .build();
        final RpcRequest.Request request = RpcRequest.Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcOperate.Operate.Debug)
                .setBody(message.toByteString())
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage
                .newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(() -> {
            final CountDownLatch latch = new CountDownLatch(1);
            MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
            context.getHandler().addListener(responseListener);
            try {
                latch.await(3, TimeUnit.HOURS);
                if (!responseListener.getReceiveResult()) {
                    log.error("debug任务信号丢失，3小时未收到work返回：{}", id);
                }
            } finally {
                holder.getDebugRunning().remove(id);
            }
            return responseListener.getResponse();
        });

        /**
         * writeAndFlush 和 write有和区别，为何使用write workerHandler无法接收数据
         */
        holder.getChannel().writeAndFlush(socketMessage);
        log.info("master send debug command to worker,rid = " + request.getRid() + ",debugId = " + id);
        return future;
    }
}
