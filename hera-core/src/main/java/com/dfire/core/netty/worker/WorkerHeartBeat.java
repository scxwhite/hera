package com.dfire.core.netty.worker;

import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol;
import io.netty.channel.ChannelFuture;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
public class WorkerHeartBeat {


    public ChannelFuture send(WorkContext context) {
        Protocol.HeartBeatMessage hbm = Protocol.HeartBeatMessage.newBuilder().
                setHost(DistributeLock.host).
                build();
        Protocol.Request request = Protocol.Request.newBuilder().
                        setRid(100).
                        setOperate(Protocol.Operate.HeartBeat).
                        setBody(hbm.toByteString()).
                        build();
        Protocol.SocketMessage message = Protocol.SocketMessage.newBuilder().
                setKind(Protocol.SocketMessage.Kind.REQUEST).
                setBody(request.toByteString()).
                build();
        return context.getServerChannel().write(message);
    }
}
