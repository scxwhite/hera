package com.dfire.core.netty.master;

import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.message.Protocol.*;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author xiaosuda
 * @date 2018/4/13
 */
@Slf4j
public class MasterDoHeartBeat {

    public void dealHeartBeat(MasterContext masterContext, Channel channel, Request request) {
        MasterWorkHolder worker = masterContext.getWorkMap().get(channel);
        HeartBeatInfo heartBeatInfo = new HeartBeatInfo();
        HeartBeatMessage heartBeatMessage = null;
        try {
            heartBeatMessage = HeartBeatMessage.newBuilder().mergeFrom(request.getBody()).build();
            heartBeatInfo.setHost(heartBeatMessage.getHost());
            worker.setHeartBeatInfo(heartBeatInfo);
            log.info("received heart beat from :{}", heartBeatMessage.getHost());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }
}
