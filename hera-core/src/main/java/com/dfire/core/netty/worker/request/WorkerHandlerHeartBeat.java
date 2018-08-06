package com.dfire.core.netty.worker.request;

import com.dfire.core.tool.CpuLoadPerCoreJob;
import com.dfire.core.tool.MemUseRateJob;
import com.dfire.core.job.JobContext;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol.HeartBeatMessage;
import com.dfire.core.message.Protocol.Operate;
import com.dfire.core.message.Protocol.Request;
import com.dfire.core.message.Protocol.SocketMessage;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.netty.worker.WorkContext;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
@Slf4j
public class WorkerHandlerHeartBeat {


    public ChannelFuture send(WorkContext context) {
        MemUseRateJob memUseRateJob = new MemUseRateJob(1);
        memUseRateJob.readMemUsed();
        CpuLoadPerCoreJob loadPerCoreJob = new CpuLoadPerCoreJob();
        loadPerCoreJob.run();
        HeartBeatMessage hbm = HeartBeatMessage.newBuilder()
                .setHost(DistributeLock.host)
                .setMemTotal(memUseRateJob.getMemTotal())
                .setMemRate(memUseRateJob.getRate())
                .setCpuLoadPerCore(loadPerCoreJob.getLoadPerCore())
                .setTimestamp(System.currentTimeMillis())
                .addAllDebugRunnings(context.getDebugRunning().keySet())
                .addAllManualRunnings(context.getManualRunning().keySet())
                .addAllRunnings(context.getRunning().keySet())
                .build();
        Request request = Request.newBuilder().
                        setRid(AtomicIncrease.getAndIncrement()).
                        setOperate(Operate.HeartBeat).
                        setBody(hbm.toByteString()).
                        build();
        SocketMessage message = SocketMessage.newBuilder().
                setKind(SocketMessage.Kind.REQUEST).
                setBody(request.toByteString()).
                build();
        return context.getServerChannel().writeAndFlush(message);
    }

}
