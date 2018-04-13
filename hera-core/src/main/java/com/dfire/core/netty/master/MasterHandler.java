package com.dfire.core.netty.master;

import com.dfire.core.message.Protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:34 2018/1/4
 * @desc SocketMessage为rpc消息体
 */
@Slf4j
@ChannelHandler.Sharable

public class MasterHandler extends ChannelInboundHandlerAdapter {

    private MasterContext masterContext;

    public MasterHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketMessage socketMessage = (SocketMessage) msg;
        Channel channel = ctx.channel();
        switch (socketMessage.getKind()) {
            //心跳
            case REQUEST:
                Request request = Request.newBuilder().mergeFrom(socketMessage.getBody()).build();
                if (request.getOperate() == Operate.HeartBeat) {
                    masterContext.getMasterDoHeartBeat().dealHeartBeat(masterContext, channel, request);
                }
                break;

            default:
                log.error("unknow request type : {}", socketMessage.getKind() );
                break;
        }


        log.info("work active :" + socketMessage.getBody());
        ctx.writeAndFlush(msg);
        super.channelActive(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        masterContext.getWorkMap().put(channel, new MasterWorkHolder());
        SocketAddress remoteAddress = channel.remoteAddress();
        log.info("worker client connect success : {}", remoteAddress.toString());
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
