package com.dfire.core.netty.worker.request;

import com.dfire.common.exception.HeraException;
import com.dfire.core.exception.RemotingException;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.HistoryPair;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.protocol.RpcHeartBeatMessage;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest;
import com.dfire.protocol.RpcSocketMessage;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
public class WorkerHandlerHeartBeat {


    public boolean send(WorkContext context) throws HeraException {
        try {
            MemUseRateJob memUseRateJob = new MemUseRateJob(1);
            memUseRateJob.readMemUsed();
            CpuLoadPerCoreJob loadPerCoreJob = new CpuLoadPerCoreJob();
            loadPerCoreJob.run();

            RpcHeartBeatMessage.HeartBeatMessage hbm = RpcHeartBeatMessage.HeartBeatMessage.newBuilder()
                    .setHost(WorkContext.host)
                    .setMemTotal(memUseRateJob.getMemTotal())
                    .setMemRate(memUseRateJob.getRate())
                    .setCpuLoadPerCore(loadPerCoreJob.getLoadPerCore())
                    .setTimestamp(System.currentTimeMillis())
                    .addAllDebugRunning(context.getDebugRunning().keySet().stream().map(String::valueOf).collect(Collectors.toList()))
                    .addAllManualRunning(context.getManualRunning().keySet().stream().map(HistoryPair::getActionId).map(String::valueOf).collect(Collectors.toList()))
                    .addAllRunning(context.getRunning().keySet().stream().map(HistoryPair::getActionId).map(String::valueOf).collect(Collectors.toList()))
                    .addAllRerunRunning(context.getRerunRunning().keySet().stream().map(HistoryPair::getActionId).map(String::valueOf).collect(Collectors.toList()))
                    .addAllSuperRunning(context.getSuperRunning().keySet().stream().map(HistoryPair::getActionId).map(String::valueOf).collect(Collectors.toList()))
                    .setCores(WorkContext.cpuCoreNum)
                    .build();
            context.getServerChannel().writeAndFlush(RpcSocketMessage.SocketMessage.newBuilder().
                    setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST).
                    setBody(RpcRequest.Request.newBuilder().
                            setRid(AtomicIncrease.getAndIncrement()).
                            setOperate(RpcOperate.Operate.HeartBeat).
                            setBody(hbm.toByteString()).
                            build().toByteString()).
                    build());
        } catch (RemotingException e) {
            throw new HeraException("发送心跳消息失败", e);
        }
        return true;
    }

}
