package com.dfire.core.netty.worker.request;

import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.NettyChannel;
import com.dfire.core.tool.OsProcessJob;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.channel.Channel;

/**
 * @author xiaosuda
 * @date 2018/11/20
 */
public class WorkHandlerRequest {

    public void getWorkInfo(Channel channel) {
        OsProcessJob processJob = new OsProcessJob();
        processJob.run();
        try {
            new NettyChannel(channel).writeAndFlush(
                    RpcSocketMessage.SocketMessage.newBuilder()
                            .setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST)
                            .setBody(Request.newBuilder()
                                    .setBody(processJob.getRes().toByteString())
                                    .setOperate(RpcOperate.Operate.SetWorkInfo)
                                    .build()
                                    .toByteString())
                            .build());
        } catch (RemotingException e) {
            e.printStackTrace();
        }

    }
}
