package com.dfire.core.netty.listener;

import com.dfire.core.message.Protocol.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:48 2018/1/10
 * @desc  web请求在netty中的handler处理响应监听
 */
public interface ResponseListener {

     abstract void onResponse(Response response);

     abstract void onWebResponse(WebResponse webResponse);
}