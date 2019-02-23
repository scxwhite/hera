package com.dfire.core.netty.cluster;

import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.netty.NettyChannel;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.channel.Channel;

/**
 *
 *  快速失败集群容错 比如心跳
 * @author xiaosuda
 * @date 2019/2/23
 */
public class FailFastCluster extends AbstractCluster {


    public static HeraChannel wrap(HeraChannel channel) {
        return new FailFastCluster(channel);
    }

    public static HeraChannel wrap(Channel channel) {
        return wrap(new NettyChannel(channel));
    }

    private FailFastCluster(HeraChannel channel) {
        super(channel);
    }

    @Override
    public void writeAndFlush(RpcSocketMessage.SocketMessage msg) throws RemotingException {
        channel.writeAndFlush(msg);
    }
}
