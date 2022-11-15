package com.dfire.core.netty.master;

import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
public class MasterServer {

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private MasterHandler handler;

    /**
     * ProtobufVarint32LengthFieldPrepender:对protobuf协议的的消息头上加上一个长度为32的整形字段,用于标志这个消息的长度。
     * ProtobufVarint32FrameDecoder:针对protobuf协议的ProtobufVarint32LengthFieldPrepender()所加的长度属性的解码器
     *
     * @param handler
     */
    public MasterServer(final MasterHandler handler) {
        serverBootstrap = new ServerBootstrap();
        //服务端接受客户端的连接， Reactor线程组
        bossGroup = new NioEventLoopGroup(1);
        //SocketChannel的网络读写
        workGroup = new NioEventLoopGroup(4);
        this.handler = handler;
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder())
                                .addLast("decoder", new ProtobufDecoder(RpcSocketMessage.SocketMessage.getDefaultInstance()))
                                .addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender())
                                .addLast("encoder", new ProtobufEncoder())
                                .addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS))
                                .addLast("handler", handler);
                    }
                });
    }

    public synchronized boolean start(int port) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            ErrorLog.error("InterruptedException", e);
        }
        if (channelFuture != null && channelFuture.isSuccess()) {
            HeraLog.info("start master server success");
        } else {
            ErrorLog.error("start master server failed");
        }

        return true;
    }

    public synchronized boolean shutdown() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        handler.shutdown();
        HeraLog.info("stop master server gracefully");
        return true;
    }


}
