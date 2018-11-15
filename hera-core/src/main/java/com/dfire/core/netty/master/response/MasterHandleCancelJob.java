package com.dfire.core.netty.master.response;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.logs.SocketLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/5/11
 * @desc master接收到worker端取消任务执行请求时，处理逻辑
 */
public class MasterHandleCancelJob {

    public Future<RpcResponse.Response> cancel(final MasterContext context, Channel channel, JobExecuteKind.ExecuteKind kind, String jobId) {
        RpcCancelMessage.CancelMessage cancelMessage = RpcCancelMessage.CancelMessage.newBuilder()
                .setEk(kind)
                .setId(jobId)
                .build();
        final RpcRequest.Request request = RpcRequest.Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcOperate.Operate.Cancel)
                .setBody(cancelMessage.toByteString())
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage.newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
        context.getHandler().addListener(responseListener);
        Future<RpcResponse.Response> future = context.getThreadPool().submit(() -> {
            latch.await(HeraGlobalEnvironment.getRequestTimeout(), TimeUnit.SECONDS);
            if (!responseListener.getReceiveResult()) {
                SocketLog.error("取消任务信号消失，三小时未收到work返回：{}", jobId);
            }
            return responseListener.getResponse();
        });
        ChannelFuture channelFuture = channel.writeAndFlush(socketMessage);
        boolean success = false;
        try {
            success = channelFuture.await(HeraGlobalEnvironment.getChannelTimeout());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Throwable cause = channelFuture.cause();
        if (cause != null) {
            SocketLog.error("send cancel job exception {}", cause);
            return null;
        } else if (success) {
            SocketLog.info("send cancel job success {}", request.getRid());
        } else {
            SocketLog.info("send cancel job timeout {}", request.getRid());
            return null;
        }
        return future;
    }
}
