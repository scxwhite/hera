package com.dfire.core.netty.worker;

import com.dfire.core.bo.MemUseRateJob;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol;
import com.dfire.core.message.Protocol.HeartBeatMessage;
import com.dfire.core.message.Protocol.SocketMessage;
import com.dfire.core.netty.util.AtomicIncrease;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:20 2018/6/26
 * @desc
 */
@Slf4j
@ChannelHandler.Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    private static final ByteBuf HEART_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("heart beat", CharsetUtil.UTF_8));

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        MemUseRateJob memUseRateJob = new MemUseRateJob(1);
        HeartBeatMessage hbm = HeartBeatMessage.newBuilder()
                .setHost(DistributeLock.host)
                .setMemTotal(memUseRateJob.getMemTotal())
                .setMemRate(memUseRateJob.getRate())
                .setCpuLoadPerCore(1.5f)
                .setTimestamp(System.currentTimeMillis())
                .build();
        Protocol.Request request = Protocol.Request.newBuilder().
                setRid(AtomicIncrease.getAndIncrement()).
                setOperate(Protocol.Operate.HeartBeat).
                setBody(hbm.toByteString()).
                build();
        SocketMessage message = SocketMessage.newBuilder().
                setKind(SocketMessage.Kind.REQUEST).
                setBody(request.toByteString()).
                build();
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("channel idle");
                ctx.writeAndFlush(message);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
