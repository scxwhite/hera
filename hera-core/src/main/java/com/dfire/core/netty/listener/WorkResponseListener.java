package com.dfire.core.netty.listener;

import com.dfire.core.message.Protocol;
import com.dfire.core.netty.worker.WorkContext;
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
public class WorkResponseListener implements ResponseListener{


    private Protocol.WebRequest request;
    private WorkContext workContext;
    private Boolean receiveResult;
    private CountDownLatch latch;
    private Protocol.WebResponse webResponse;



    @Override
    public void onResponse(Protocol.Response response) {

    }

    @Override
    public void onWebResponse(Protocol.WebResponse response) {
        if (request.getRid() == response.getRid()) {
            workContext.getHandler().removeListener(this);
            webResponse = response;
            receiveResult = true;
            latch.countDown();
        }
    }
}
