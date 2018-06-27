package com.dfire.core.netty.worker;

import io.netty.channel.ChannelHandler;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:10 2018/6/26
 * @desc
 */
public interface ChannelHandlerHolder {

    /**
     * 持有所有的业务处理handler
     *
     * @return
     */
    ChannelHandler[] handlers();
}
