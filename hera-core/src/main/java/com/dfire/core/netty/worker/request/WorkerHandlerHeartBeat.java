package com.dfire.core.netty.worker.request;

import com.dfire.core.lock.DistributeLock;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.protocol.RpcHeartBeatMessage;
import com.dfire.protocol.RpcOperate;
import com.dfire.protocol.RpcRequest;
import com.dfire.protocol.RpcSocketMessage;
import io.netty.channel.ChannelFuture;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
public class WorkerHandlerHeartBeat {


    public ChannelFuture send(WorkContext context) {
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
                .addAllDebugRunnings(context.getDebugRunning().keySet())
                .addAllManualRunnings(context.getManualRunning().keySet())
                .addAllRunnings(context.getRunning().keySet())
                .setCores(WorkContext.cpuCoreNum)
                .build();
        RpcRequest.Request request = RpcRequest.Request.newBuilder().
                        setRid(AtomicIncrease.getAndIncrement()).
                        setOperate(RpcOperate.Operate.HeartBeat).
                        setBody(hbm.toByteString()).
                        build();
        RpcSocketMessage.SocketMessage message = RpcSocketMessage.SocketMessage.newBuilder().
                setKind(RpcSocketMessage.SocketMessage.Kind.REQUEST).
                setBody(request.toByteString()).
                build();
        return context.getServerChannel().writeAndFlush(message);
    }

}
