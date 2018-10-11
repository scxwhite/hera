package com.dfire.core.netty.master.response;

import com.dfire.core.netty.listener.MasterResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.protocol.*;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/5/11
 * @desc master接收到worker端取消任务执行请求时，处理逻辑
 */
@Slf4j
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
        Future<RpcResponse.Response> future = context.getThreadPool().submit(() -> {
            final CountDownLatch latch = new CountDownLatch(1);
            MasterResponseListener responseListener = new MasterResponseListener(request, context, false, latch, null);
            context.getHandler().addListener(responseListener);
            latch.await(3, TimeUnit.HOURS);
            if (!responseListener.getReceiveResult()) {
                log.error("取消任务信号消失，三小时未收到work返回：{}", jobId);
            }
            return responseListener.getResponse();
        });
        channel.writeAndFlush(socketMessage);
        return future;
    }
}
