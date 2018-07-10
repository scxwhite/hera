package com.dfire.core.netty.master.response;

import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.util.AtomicIncrease;
import io.netty.channel.Channel;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:42 2018/5/11
 * @desc master接收到worker端取消任务执行请求时，处理逻辑
 */
public class MasterHandleCancelJob {

    public Future<Response> cancel(final MasterContext context, Channel channel, ExecuteKind kind, String jobId) {
        CancelMessage cancelMessage = CancelMessage.newBuilder()
                .setEk(kind)
                .setId(jobId)
                .build();
        final Request request = Request.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Operate.Cancel)
                .setBody(cancelMessage.toByteString())
                .build();
        SocketMessage socketMessage = SocketMessage.newBuilder()
                .setKind(SocketMessage.Kind.REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Response> future = context.getThreadPool().submit(new Callable<Response>() {
            private Response result;

            @Override
            public Response call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                context.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        if (request.getRid() == response.getRid()) {
                            context.getHandler().removeListener(this);
                            result = response;
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onWebResponse(WebResponse webResponse) {

                    }
                });
                latch.await();
                return result;
            }
        });
        channel.writeAndFlush(socketMessage);
        return future;
    }
}
