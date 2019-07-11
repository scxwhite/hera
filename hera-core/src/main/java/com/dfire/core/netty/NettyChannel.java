package com.dfire.core.netty;

import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.exception.RemotingException;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;


/**
 * @author xiaosuda
 * @date 2018/11/16
 */
public class NettyChannel implements HeraChannel {


    private Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }


    @Override
    public void writeAndFlush(RpcSocketMessage.SocketMessage message) throws RemotingException {
        ChannelFuture channelFuture = channel.writeAndFlush(message);
        boolean success;
        long timeout = HeraGlobalEnv.getChannelTimeout();
        try {
            success = channelFuture.await(timeout);
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            throw new RemotingException(this, getLocalAddress() + "Failed to send message to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
        if (!success) {
            throw new RemotingException(this, getLocalAddress() + "Failed to send message to " + getRemoteAddress()
                    + "in timeout(" + timeout + "ms) limit");
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return channel.localAddress();
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "NettyChannel{" +
                "channel=" + channel +
                '}';
    }
}
