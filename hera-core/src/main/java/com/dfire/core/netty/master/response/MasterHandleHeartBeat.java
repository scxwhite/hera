package com.dfire.core.netty.master.response;

import com.alibaba.fastjson.JSONObject;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.message.Protocol.*;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 *
 * @author xiaosuda
 * @date 2018/4/13
 */
@Slf4j
public class MasterHandleHeartBeat {

    public void dealHeartBeat(MasterContext masterContext, Channel channel, Request request) {
        MasterWorkHolder worker = masterContext.getWorkMap().get(channel);
        HeartBeatInfo heartBeatInfo = new HeartBeatInfo();
        HeartBeatMessage heartBeatMessage;
        try {
            heartBeatMessage = HeartBeatMessage.newBuilder().mergeFrom(request.getBody()).build();
            heartBeatInfo.setHost(heartBeatMessage.getHost());
            heartBeatInfo.setMemRate(heartBeatMessage.getMemRate());
            heartBeatInfo.setMemTotal(heartBeatMessage.getMemTotal());
            worker.setHeartBeatInfo(heartBeatInfo);
            log.info("received heart beat from {} : {}", heartBeatMessage.getHost(), JSONObject.toJSONString(heartBeatInfo));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }
}
