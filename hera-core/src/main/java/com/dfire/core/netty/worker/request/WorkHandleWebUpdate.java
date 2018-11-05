package com.dfire.core.netty.worker.request;

import com.dfire.core.netty.listener.WorkResponseListener;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午1:07 2018/5/12
 * @desc
 */
public class WorkHandleWebUpdate {

    public Future<RpcWebResponse.WebResponse> handleUpdate(final WorkContext workContext, String jobId) {

        final RpcWebRequest.WebRequest request = RpcWebRequest.WebRequest.newBuilder()
                .setRid(AtomicIncrease.getAndIncrement())
                .setOperate(RpcWebOperate.WebOperate.UpdateJob)
                .setEk(JobExecuteKind.ExecuteKind.ManualKind)
                .setId(jobId)
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
                SocketLog.error("更新job超出3小时未得到master消息返回：{}", jobId);
            }
            return responseListener.getWebResponse();
        });
        workContext.getServerChannel().writeAndFlush(socketMessage);
        return future;
    }


}
