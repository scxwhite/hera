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
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午8:00 2018/4/16
 * @desc
 */
@Slf4j
public class WorkerHandleWebExecute {

    public Future<RpcWebResponse.WebResponse> handleWebExecute(final WorkContext workContext, JobExecuteKind.ExecuteKind kind, String id) {

        final RpcWebRequest.WebRequest request = RpcWebRequest.WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcWebOperate.WebOperate.ExecuteJob)
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
                log.error("执行任务超出3小时未得到master消息返回：{}", id);
            }
            return responseListener.getWebResponse();
        });
        workContext.getServerChannel().writeAndFlush(socketMessage);
        return future;
    }
}
