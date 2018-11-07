package com.dfire.core.route.strategy;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.WorkerRouter;
import com.dfire.logs.ScheduleLog;
import io.netty.util.internal.PlatformDependent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:11 2018/10/12
 * @desc 随机选择一台机器分发任务
 */
public class WorkerRouterRandom extends WorkerRouter {

    private static final AtomicIntegerFieldUpdater<HeraHostGroupVo> updater;

    static {
        AtomicIntegerFieldUpdater<HeraHostGroupVo> refCntUpdater =
                PlatformDependent.newAtomicIntegerFieldUpdater(HeraHostGroupVo.class, "currentPosition");
        if (refCntUpdater == null) {
            refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(HeraHostGroupVo.class, "currentPosition");
        }
        updater = refCntUpdater;
    }

    @Override
    public MasterWorkHolder selectWorker(int hostGroupId, MasterContext masterContext) {
        MasterWorkHolder workHolder = null;
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroupCache = masterContext.getHostGroupCache().get(hostGroupId);
            List<String> hosts = hostGroupCache.getHosts();
            if (hosts != null && hosts.size() > 0) {
                int size = hosts.size();
                int position = new Random().nextInt(size);
                updater.compareAndSet(hostGroupCache, hostGroupCache.getCurrentPosition(), position);
                for (int i = 0; i < size && workHolder == null; i++) {
                    String host = hostGroupCache.selectHost();
                    for (MasterWorkHolder worker : masterContext.getWorkMap().values()) {
                        if (checkResource(host, worker)) {
                            workHolder = worker;
                        }
                    }
                }
            }
        }
        if (workHolder != null) {
            ScheduleLog.warn("select work is :{}", workHolder.getChannel().remoteAddress());
        }
        return workHolder;
    }
}
