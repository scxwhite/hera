package com.dfire.core.netty.master.response;

import com.alibaba.fastjson.JSONObject;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeartLog;
import com.dfire.logs.HeraLog;
import com.dfire.protocol.RpcHeartBeatMessage.HeartBeatMessage;
import com.dfire.protocol.RpcRequest.Request;
import com.dfire.protocol.RpcWorkInfo.WorkInfo;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;


/**
 * @author xiaosuda
 * @date 2018/4/13
 */
public class MasterHandleRequest {

    public static void handleHeartBeat(MasterContext masterContext, Channel channel, Request request) {
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        HeartBeatInfo heartBeatInfo = new HeartBeatInfo();
        HeartBeatMessage heartBeatMessage;
        try {
            heartBeatMessage = HeartBeatMessage.newBuilder().mergeFrom(request.getBody()).build();
            heartBeatInfo.setHost(heartBeatMessage.getHost());
            heartBeatInfo.setMemRate(heartBeatMessage.getMemRate());
            heartBeatInfo.setMemTotal(heartBeatMessage.getMemTotal());
            heartBeatInfo.setCpuLoadPerCore(heartBeatMessage.getCpuLoadPerCore());
            heartBeatInfo.setRunning(heartBeatMessage.getRunningList());
            heartBeatInfo.setDebugRunning(heartBeatMessage.getDebugRunningList());
            heartBeatInfo.setManualRunning(heartBeatMessage.getManualRunningList());
            heartBeatInfo.setTimestamp(heartBeatMessage.getTimestamp());
            heartBeatInfo.setCores(heartBeatMessage.getCores());
            workHolder.setHeartBeatInfo(heartBeatInfo);
            HeartLog.debug("received heart beat from {} : {}", heartBeatMessage.getHost(), JSONObject.toJSONString(heartBeatInfo));
        } catch (InvalidProtocolBufferException e) {
            ErrorLog.error("解析消息异常", e);
        }
    }

    public static void setWorkInfo(MasterContext masterContext, Channel channel, Request request) {
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        try {
            WorkInfo workInfo = WorkInfo.parseFrom(request.getBody());
            workHolder.setWorkInfo(workInfo);
            HeraLog.info("set workInfo success,{}", channel.remoteAddress());
        } catch (InvalidProtocolBufferException e) {
            ErrorLog.error("解析消息异常", e);
        }
    }
}
