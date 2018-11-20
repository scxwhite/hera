package com.dfire.core.netty.worker.request;

import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.tool.OsProcessJob;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcResponse;
import com.dfire.protocol.RpcResponse.Response;

import java.util.concurrent.Future;

/**
 * @author xiaosuda
 * @date 2018/11/20
 */
public class WorkHandlerRequest {

    public Future<Response> getWorkInfo(WorkContext workContext, Request request) {
        return workContext.getWorkExecuteThreadPool().submit(() -> {

            OsProcessJob processJob = new OsProcessJob();
            processJob.run();
            return RpcResponse.Response.newBuilder()
                    .setRid(request.getRid())
                    .setBody(processJob.getRes().toByteString())
                    .setOperate(RpcOperate.Operate.SetWorkInfo)
                    .build();
        });

    }
}
