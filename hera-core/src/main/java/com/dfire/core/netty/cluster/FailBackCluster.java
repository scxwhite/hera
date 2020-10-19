package com.dfire.core.netty.cluster;

import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.core.netty.NettyChannel;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * 集群容错之失败重试
 *
 * @author xiaosuda
 * @date 2019/2/23
 */
public class FailBackCluster extends AbstractCluster {


    /**
     * 最大重试次数
     */
    private static final int RETRY_TIMES = 100;
    /**
     * 重试 delay 时间
     */
    private static final int RETRY_DELAY = 5;

    private volatile Timer retryTimer;


    public static HeraChannel wrap(HeraChannel channel) {
        return new FailBackCluster(channel);
    }

    public static HeraChannel wrap(Channel channel) {
        return wrap(new NettyChannel(channel));
    }

    private FailBackCluster(HeraChannel channel) {
        super(channel);
    }

    @Override
    public void writeAndFlush(SocketMessage msg) {
        try {
            channel.writeAndFlush(msg);
        } catch (RemotingException e) {
            SocketLog.error("send netty msg cause exception, retry it", e);
            addFailed(channel, msg);
        }
    }

    private void addFailed(HeraChannel channel, SocketMessage msg) {
        if (retryTimer == null) {
            synchronized (this) {
                if (retryTimer == null) {
                    retryTimer = new HashedWheelTimer(
                            new NamedThreadFactory("failback-cluster-timer", true),
                            1,
                            TimeUnit.SECONDS);
                }
            }
        }
        retryTimer.newTimeout(new RetryTimerTask(channel, msg, RETRY_TIMES, RETRY_DELAY), RETRY_DELAY, TimeUnit.SECONDS);
    }


}
