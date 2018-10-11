package com.dfire.core.netty.master.response;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.util.DateUtil;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.protocol.RpcHeartBeatMessage;
import com.dfire.protocol.RpcRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


/**
 * @author xiaosuda
 * @date 2018/4/13
 */
@Slf4j
public class MasterHandleHeartBeat {

    public void handleHeartBeat(MasterContext masterContext, Channel channel, RpcRequest.Request request) {
        MasterWorkHolder worker = masterContext.getWorkMap().get(channel);
        HeartBeatInfo heartBeatInfo = new HeartBeatInfo();
        RpcHeartBeatMessage.HeartBeatMessage heartBeatMessage;
        try {
            heartBeatMessage = RpcHeartBeatMessage.HeartBeatMessage.newBuilder().mergeFrom(request.getBody()).build();
            heartBeatInfo.setHost(heartBeatMessage.getHost());
            heartBeatInfo.setMemRate(heartBeatMessage.getMemRate());
            heartBeatInfo.setMemTotal(heartBeatMessage.getMemTotal());
            heartBeatInfo.setCpuLoadPerCore(heartBeatMessage.getCpuLoadPerCore());
            heartBeatInfo.setRunning(heartBeatMessage.getRunningsList());
            heartBeatInfo.setDebugRunning(heartBeatMessage.getDebugRunningsList());
            heartBeatInfo.setManualRunning(heartBeatMessage.getManualRunningsList());
            heartBeatInfo.setTimestamp(DateUtil.longToDate(heartBeatMessage.getTimestamp()));
            worker.setHeartBeatInfo(heartBeatInfo);
            log.info("received heart beat from {} : {}", heartBeatMessage.getHost(), JSONObject.toJSONString(heartBeatInfo));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }
}
