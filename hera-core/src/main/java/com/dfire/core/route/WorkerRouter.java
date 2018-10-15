package com.dfire.core.route;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/10/10
 * @desc 任务执行worker选择路由
 */
public abstract class WorkerRouter {

    /**
     * 根据可执行机器列表与机器组id选取worker
     *
     * @param hostGroupId
     * @param masterContext
     * @return
     */
    public abstract MasterWorkHolder selectWorker(int hostGroupId, MasterContext masterContext);

    /**
     * check ip 的worker能否选择为执行机器
     *
     * @param host
     * @param worker
     * @return
     */
    public boolean checkResource(String host, MasterWorkHolder worker) {
        boolean canDispatchJob = false;
        if (worker != null && worker.getHeartBeatInfo() != null && worker.getHeartBeatInfo().getHost().trim().equals(host.trim())) {
            HeartBeatInfo heartBeatInfo = worker.getHeartBeatInfo();
            if (heartBeatInfo.getMemRate() != null && heartBeatInfo.getCpuLoadPerCore() != null
                    && heartBeatInfo.getMemRate() < HeraGlobalEnvironment.getMaxMemRate()
                    && heartBeatInfo.getCpuLoadPerCore() < HeraGlobalEnvironment.getMaxCpuLoadPerCore()) {

                Float assignTaskNum = (heartBeatInfo.getMemTotal() - HeraGlobalEnvironment.getSystemMemUsed()) / HeraGlobalEnvironment.getPerTaskUseMem();
                int sum = heartBeatInfo.getDebugRunning().size() + heartBeatInfo.getManualRunning().size() + heartBeatInfo.getRunning().size();
                if (assignTaskNum.intValue() > sum) {
                    canDispatchJob = true;
                }
            }
        }
        return canDispatchJob;
    }
}
