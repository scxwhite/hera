package com.dfire.core.netty.worker.request;

import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.listener.WorkResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:56 2018/5/12
 * @desc 接收到web cancel job 请求的时候,发起netty master handler 取消任务请求
 */
@Slf4j
public class WorkHandleWebCancel {

    public Future<WebResponse> handleCancel(final WorkContext workContext, ExecuteKind kind, String id) {

        final WebRequest request = WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.CancelJob)
                .setEk(kind)
                .setId(id)
                .build();
        SocketMessage socketMessage = SocketMessage.newBuilder()
                .setKind(SocketMessage.Kind.WEB_REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<WebResponse> future = workContext.getWorkThreadPool().submit(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            WorkResponseListener responseListener = new WorkResponseListener(request, workContext, false, latch, null);
            workContext.getHandler().addListener(responseListener);
            latch.await(3, TimeUnit.HOURS);
            if (!responseListener.getReceiveResult()) {
                log.error("取消任务超出3小时未得到master消息返回：{}", id);
            }
            return responseListener.getWebResponse();
        });
        workContext.getServerChannel().writeAndFlush(socketMessage);
        return future;
    }
}
