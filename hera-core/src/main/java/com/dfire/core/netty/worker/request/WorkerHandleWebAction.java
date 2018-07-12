package com.dfire.core.netty.worker.request;

import com.dfire.core.message.Protocol;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 *
 * @author xiaosuda
 * @date 2018/7/6
 */
@Slf4j
public class WorkerHandleWebAction {

    public Future<Protocol.WebResponse> handleWebAction(final WorkContext workContext, Protocol.ExecuteKind kind, String id) {

        final Protocol.WebRequest request = Protocol.WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(Protocol.WebOperate.GenerateAction)
                .setEk(kind)
                .setId(id)
                .build();
        Protocol.SocketMessage socketMessage = Protocol.SocketMessage.newBuilder()
                .setKind(Protocol.SocketMessage.Kind.WEB_REQUEST)
                .setBody(request.toByteString())
                .build();
        Future<Protocol.WebResponse> future = workContext.getWorkThreadPool().submit(new Callable<Protocol.WebResponse>() {
            private Protocol.WebResponse webResponse;
            @Override
            public Protocol.WebResponse call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                log.info("Worker start Handle Web generate action, requestId = " + request.getRid());
                workContext.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onResponse(Protocol.Response response) {
                    }

                    @Override
                    public void onWebResponse(Protocol.WebResponse response) {
                        if (request.getRid() == request.getRid()) {
                            workContext.getHandler().removeListener(this);
                            webResponse = response;
                            latch.countDown();
                            log.info("Worker end Handle Web generate action " + request.getRid());
                        }
                    }
                });
                latch.await();
                return webResponse;
            }
        });
        workContext.getServerChannel().write(socketMessage);
        log.info("send web generate action request" + request.getRid() + "kind= " + kind + "id = " + id);
        return future;
    }
}
