package com.dfire.core.netty.listener;

import com.dfire.core.netty.listener.adapter.ResponseListenerAdapter;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.RpcRequest;
import com.dfire.protocol.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author xiaosuda
 * @date 2018/7/31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MasterResponseListener extends ResponseListenerAdapter {

    private RpcRequest.Request request;
    private MasterContext context;
    private Boolean receiveResult;
    private CountDownLatch latch;
    private RpcResponse.Response response;

    @Override
    public void onResponse(RpcResponse.Response response) {
        TaskLog.info("MasterResponseListener id1,id2:{},{}",response.getRid(),request.getRid());
        if (response.getRid() == request.getRid()) {
            TaskLog.info("master release lock for request :{}", response.getRid());
            context.getHandler().removeListener(this);
            this.response = response;
            receiveResult = true;
            latch.countDown();
        }
    }

}
