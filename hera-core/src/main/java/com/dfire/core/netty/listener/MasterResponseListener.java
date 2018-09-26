package com.dfire.core.netty.listener;

import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.listener.adapter.ResponseListenerAdapter;
import com.dfire.core.netty.master.MasterContext;
import lombok.AllArgsConstructor;
import lombok.Data;
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
public class MasterResponseListener extends ResponseListenerAdapter {

    private Request request;
    private MasterContext context;
    private Boolean receiveResult;
    private CountDownLatch latch;
    private Response response;

    @Override
    public void onResponse(Response response) {
        if (response.getRid() == request.getRid()) {
            context.getHandler().removeListener(this);
            this.response = response;
            receiveResult = true;
            latch.countDown();
        }
    }

}
