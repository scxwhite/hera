package com.dfire.core.netty.worker;

import com.dfire.common.util.SpringContextHolder;
import com.dfire.core.config.HeraGlobalEnvironment;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午2:43 2018/6/26
 * @desc 链路检测handler, server断线重启情况下，worker恢复断线重连
 */
@Slf4j
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter
        implements TimerTask , ChannelHandlerHolder{

    private final Bootstrap bootstrap;
    private final Timer timer;
    private final String host;
    private volatile boolean reconnection = true;
    private int attempt;
    private final static int count = 12;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, String host, boolean reconnection, int attempt) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.host = host;
        this.reconnection = reconnection;
        this.attempt = attempt;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("this channel is active");
        attempt = 0;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        log.info("this channel is closed");
        if(reconnection) {
            if(attempt < count) {
                attempt ++;
            }
            log.info("this channel is inactive, try to reconnect, attempt = {}", attempt);
            int timeout = 2 >> count;
            timer.newTimeout(this, timeout, TimeUnit.SECONDS);
        }
        ctx.fireChannelInactive();
    }


    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture channelFuture;
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers());
                }
            });
            channelFuture = bootstrap.connect(host, HeraGlobalEnvironment.getConnectPort());
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boolean success = future.isSuccess();
                if(!success) {
                    log.info("reconnect unsuccessful");
                    future.channel().pipeline().fireChannelInactive();
                } else {

                    Channel serverChannel = future.channel();
                    ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
                    WorkClient workClient = (WorkClient) applicationContext.getBean("workClient");
                    workClient.getWorkContext().setServerChannel(serverChannel);
                    log.info("reconnect success");
                }
            }
        });
    }
}
