package com.dfire.core.netty.listener;

import com.dfire.core.netty.listener.adapter.ResponseListenerAdapter;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.TaskLog;
import com.dfire.protocol.RpcRequest;
import com.dfire.protocol.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.concurrent.CountDownLatch;

/**
 * @author xiaosuda
 * @date 2018/7/31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MasterResponseListener extends ResponseListenerAdapter {

    private RpcRequest.Request request;
    private volatile Boolean receiveResult;
    private CountDownLatch latch;
    private RpcResponse.Response response;

    @Override
    public void onResponse(RpcResponse.Response response) {
        if (response.getRid() == request.getRid()) {
            try {
                this.response = response;
                receiveResult = true;
            } catch (Exception e) {
                ErrorLog.error("release lock exception {}", e);
            } finally {
                latch.countDown();
            }
        }
    }

}
