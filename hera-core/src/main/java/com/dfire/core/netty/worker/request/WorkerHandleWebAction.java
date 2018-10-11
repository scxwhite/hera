package com.dfire.core.netty.worker.request;

import com.dfire.core.netty.listener.WorkResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.protocol.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaosuda
 * @date 2018/7/6
 */
@Slf4j
public class WorkerHandleWebAction {

    public Future<RpcWebResponse.WebResponse> handleWebAction(final WorkContext workContext, JobExecuteKind.ExecuteKind kind, String id) {

        final RpcWebRequest.WebRequest request = RpcWebRequest.WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcWebOperate.WebOperate.GenerateAction)
                .setEk(kind)
                .setId(id)
                .build();
        RpcSocketMessage.SocketMessage socketMessage = RpcSocketMessage.SocketMessage.newBuilder()
                .setKind(RpcSocketMessage.SocketMessage.Kind.WEB_REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<RpcWebResponse.WebResponse> future = workContext.getWorkThreadPool().submit(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            WorkResponseListener responseListener = new WorkResponseListener(request, workContext, false, latch, null);
            workContext.getHandler().addListener(responseListener);
            latch.await(3, TimeUnit.HOURS);
            if (!responseListener.getReceiveResult()) {
                log.error("更新action超出3小时未得到master消息返回：{}", id);
            }
            return responseListener.getWebResponse();
        });
        workContext.getServerChannel().write(socketMessage);
        return future;
    }
}
