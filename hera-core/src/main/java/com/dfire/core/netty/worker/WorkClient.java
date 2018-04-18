package com.dfire.core.netty.worker;


import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol;
import com.dfire.core.netty.worker.request.WorkerHeartBeat;
import com.dfire.core.schedule.ScheduleInfoLog;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 10:34 2018/1/10
 * @desc
 */
@Slf4j
@Data
@Component
public class WorkClient {

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private WorkContext workContext;
    private ScheduledExecutorService service;
    public AtomicBoolean isShutdown = new AtomicBoolean(true);
    @PostConstruct
    public void WorkClient() {
        isShutdown.compareAndSet(true, false);
        workContext = new WorkContext();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufDecoder(Protocol.SocketMessage.getDefaultInstance()))
                                .addLast(new ProtobufVarint32LengthFieldPrepender())
                                .addLast(new ProtobufEncoder())
                                .addLast(new WorkHandler(workContext));
                    }
                });
        log.info("start work client success ");
        workContext.setWorkClient(this);
        service = Executors.newScheduledThreadPool(1);
        sendHeartBeat();

    }

    /**
     * work 向 master 发送心跳信息
     */
    private void sendHeartBeat() {
        service.scheduleAtFixedRate(new Runnable() {
            private WorkerHeartBeat heartBeat = new WorkerHeartBeat();
            private int failCount = 0;
            @Override
            public void run() {
                try{
                    if (workContext.getServerChannel() != null) {
                        ChannelFuture channelFuture = heartBeat.send(workContext);
                        channelFuture.addListener((future) -> {
                                if (!future.isSuccess()) {
                                    failCount++;
                                    log.info("send heart beat failed ,failCount :" + failCount);
                                } else {
                                    failCount = 0;
                                    log.info("send heart beat success");
                                }
                                if (failCount > 10) {
                                    future.cancel(true);
                                    log.info("cancel connect server ,failCount:" + failCount);
                                }
                        });
                    } else {
                        log.info("server channel can not find on " + DistributeLock.host);
                    }
                } catch (Exception e) {
                    log.info("heart beat send failed ："+ failCount);
                    log.error("heart beat error:", e);
                }
            }

        }, 5, 5, TimeUnit.SECONDS);

    }

    public synchronized void connect(String host, int port) throws Exception {
        //首先判断服务频道是否开启
        if (workContext.getServerChannel() != null) {
            //如果已经与服务端连接
            if (workContext.getServerHost().equals(host)) {
                log.info("server host already connected, return");
                return ;
            } else { //关闭之前通信
                workContext.getServerChannel().close();
                workContext.setServerChannel(null);
            }
        }
        workContext.setServerHost(host);
        CountDownLatch latch = new CountDownLatch(1);
        ChannelFutureListener futureListener = (future) -> {
                try {
                    if (future.isSuccess()) {
                        workContext.setServerChannel(future.channel());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
        };
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
        connectFuture.addListener(futureListener);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            connectFuture.removeListener(futureListener);
            connectFuture.cancel(true);
            throw new ExecutionException(new TimeoutException("connect server consumption of 2 seconds"));
        }
        if (!connectFuture.isSuccess()) {
            throw new RuntimeException("connect server failed " + host,
                    connectFuture.cause());
        }
        ScheduleInfoLog.info("connect server success");
    }

    public void shutdown() {
        if (!isShutdown.get()) {
            isShutdown.set(true);
            if (service != null && !service.isShutdown()) {
                service.shutdown();
            }
            if (eventLoopGroup != null && !eventLoopGroup.isShutdown()) {
                eventLoopGroup.shutdownGracefully();
            }
            workContext.shutdown();
        }
    }

}
