package com.dfire.core.netty.worker.request;

import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午8:00 2018/4/16
 * @desc
 */
@Slf4j
public class WorkerHandleWebExecute {

    public Future<WebResponse> handleWebExecute(final WorkContext workContext, ExecuteKind kind, String id) {

        final WebRequest request = WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(WebOperate.ExecuteJob)
                .setEk(kind)
                .setId(id)
                .build();
        SocketMessage socketMessage = SocketMessage.newBuilder()
                .setKind(SocketMessage.Kind.WEB_REUQEST)
                .setBody(request.toByteString())
                .build();
        Future<WebResponse> future = workContext.getWorkThreadPool().submit(new Callable<WebResponse>() {
            private WebResponse webResponse;

            @Override
            public WebResponse call() throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);
                log.info("Worker start Handle Web Execute, requestId = " + request.getRid());
                workContext.getHandler().addListener(new ResponseListener() {
                    @Override
                    public void onResponse(Response response) { }
                    @Override
                    public void onWebResponse(WebResponse response) {
                        if (request.getRid() == request.getRid()) {
                            workContext.getHandler().removeListener(this);
                            webResponse = response;
                            latch.countDown();
                            log.info("Worker end Handle Web Execute " + request.getRid());
                        }
                    }
                });
                latch.await();
                return webResponse;
            }
        });
        workContext.getServerChannel().write(socketMessage);
        log.info("send web execute request" + request.getRid() + "kind= " + kind + "id = " + id);
        return future;
    }
}
