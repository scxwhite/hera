package com.dfire.core.netty.worker;

import com.dfire.core.bo.MemUseRateJob;
import com.dfire.core.job.JobContext;
import com.dfire.core.lock.DistributeLock;
import com.dfire.core.message.Protocol.HeartBeatMessage;
import com.dfire.core.message.Protocol.Operate;
import com.dfire.core.message.Protocol.Request;
import com.dfire.core.message.Protocol.SocketMessage;
import com.dfire.core.netty.util.AtomicIncrease;
import com.dfire.core.schedule.ScheduleInfoLog;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaosuda
 * @date 2018/4/12
 */
@Slf4j
public class WorkerHeartBeat {


    public ChannelFuture send(WorkContext context) {
        JobContext jobContext = JobContext.getTempJobContext(JobContext.SYSTEM_RUN);
        MemUseRateJob memUseRateJob = new MemUseRateJob(jobContext, 1);
        runJob(jobContext, memUseRateJob);
        HeartBeatMessage hbm = HeartBeatMessage.newBuilder().
                setHost(DistributeLock.host).
                setMemTotal((Float) jobContext.getData(MemUseRateJob.MEM_TOTAL)).
                setMemRate((Float) jobContext.getData(MemUseRateJob.MEM)).
                build();
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

    public void runJob(JobContext jobContext, MemUseRateJob memUseRateJob){
        try {
            int exitCode = -1;
            int count = 0;
            while (count < 3 && exitCode != 0) {
                count++;
                exitCode = memUseRateJob.run();
            }
            if (exitCode != 0) {
                log.error("HeartBeat Shell Error", new Exception(
                        jobContext.getHeraJobHistory().getLog().getContent()));
                // 防止后面NPE
                jobContext.putData("mem", 1.0);
            }
        } catch (Exception e) {
            ScheduleInfoLog.error("memratejob", e);
        }
    }
}
