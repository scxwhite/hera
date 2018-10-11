package com.dfire.core.netty.listener.adapter;

import com.dfire.core.netty.listener.ResponseListener;
import com.dfire.protocol.RpcResponse;
import com.dfire.protocol.RpcWebResponse;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:10 2018/9/26
 * @desc 接口适配
 */
public class ResponseListenerAdapter extends ResponseListener {


    @Override
    public void onResponse(RpcResponse.Response response) {

    }

    @Override
    public void onWebResponse(RpcWebResponse.WebResponse webResponse) {

    }
}
