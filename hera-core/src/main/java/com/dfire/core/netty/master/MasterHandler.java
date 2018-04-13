package com.dfire.core.netty.master;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:34 2018/1/4
 * @desc  SocketMessage为rpc消息体
 */
@Slf4j
@ChannelHandler.Sharable

public class MasterHandler extends ChannelInboundHandlerAdapter {

    private MasterContext context;

    public  MasterHandler(MasterContext masterContext) {
        this.context = masterContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("work active :" + JSONObject.toJSONString(msg));
        ctx.writeAndFlush(msg);
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
