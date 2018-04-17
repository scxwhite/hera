package com.dfire.core.netty.master.response;

import com.dfire.core.message.Protocol.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午9:48 2018/4/17
 * @desc 手动执行debug任务请求消息处理
 */
public class MasterHandleWebDebug {

    public WebResponse  handleWebDebug() {
        WebResponse response = WebResponse.newBuilder().build();
        return response;
    }


}
