package com.dfire.core.netty.listener.adapter;

import com.dfire.core.message.Protocol;
import com.dfire.core.netty.listener.ResponseListener;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:10 2018/9/26
 * @desc 接口适配
 */
public class ResponseListenerAdapter extends ResponseListener {


    @Override
    public void onResponse(Protocol.Response response) {

    }

    @Override
    public void onWebResponse(Protocol.WebResponse webResponse) {

    }
}
