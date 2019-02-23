package com.dfire.core.netty.cluster;

import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.HeraChannel;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.RpcSocketMessage.SocketMessage;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * @author xiaosuda
 * @date 2019/2/23
 */
public class RetryTimerTask implements TimerTask {


    private int maxRetryTimes;

    private int retryTimes;

    private int tick;

    private HeraChannel channel;

    private SocketMessage msg;

    public RetryTimerTask(HeraChannel channel, SocketMessage msg, int maxRetryTimes, int tick) {
        this.channel = channel;
        this.maxRetryTimes = maxRetryTimes;
        this.retryTimes = 0;
        this.msg = msg;
        this.tick = tick;
    }

    private void rePut(Timeout timeout) {
        if (timeout == null) {
            return;
        }

        if (++retryTimes > maxRetryTimes) {
            SocketLog.error("send netty msg cause exception, retryTimes is {}, stop send", retryTimes);
            return;
        }

        if (timeout.isCancelled()) {
            SocketLog.error("send netty msg cause exception, retryTimes is {}, timeout is canceled, stop send", retryTimes);
            return;
        }

        SocketLog.error("send netty msg cause exception, retryTimes is {}", retryTimes);
        timeout.timer().newTimeout(this, tick, TimeUnit.SECONDS);
    }

    @Override
    public void run(Timeout timeout) {
        try {
            this.channel.writeAndFlush(msg);
        } catch (RemotingException e) {
            rePut(timeout);
        }
    }
}
