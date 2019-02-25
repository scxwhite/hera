package com.dfire.core.route.strategy;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.check.ResultReason;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MasterLog;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/10/10
 * @desc 任务执行worker选择路由
 */
public abstract class AbstractLoadBalance implements LoadBalance {


    @Override
    public MasterWorkHolder select(JobElement jobElement, MasterContext masterContext) {
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroup = masterContext.getHostGroupCache().get(jobElement.getHostGroupId());
            if (hostGroup == null || hostGroup.getHosts() == null || hostGroup.getHosts().size() == 0) {
                ErrorLog.error("机器组:{},无可执行任务的机器,任务Id:{}", jobElement.getHostGroupId(), jobElement.getJobId());
                return null;
            }
            return doSelect(hostGroup, masterContext);
        }
        return null;
    }

    protected boolean check(MasterWorkHolder worker) {
        if (worker == null) {
            MasterLog.warn(ResultReason.NULL_WORKER.getMsg());
            return false;
        }
        if (worker.getHeartBeatInfo() == null) {
            MasterLog.warn(ResultReason.NULL_HEART.getMsg());
            return false;
        }
        HeartBeatInfo heartBeatInfo = worker.getHeartBeatInfo();

        if (heartBeatInfo.getMemRate() == null || heartBeatInfo.getMemRate() > HeraGlobalEnvironment.getMaxMemRate()) {
            MasterLog.warn(ResultReason.MEM_LIMIT.getMsg() + ":{}, host:{}", heartBeatInfo.getMemRate(), heartBeatInfo.getHost());
            return false;
        }
        if (heartBeatInfo.getCpuLoadPerCore() == null || heartBeatInfo.getCpuLoadPerCore() > HeraGlobalEnvironment.getMaxCpuLoadPerCore()) {
            MasterLog.warn(ResultReason.LOAD_LIMIT.getMsg() + ":{}, host:{}", heartBeatInfo.getCpuLoadPerCore(), heartBeatInfo.getHost());
            return false;
        }

        // 配置计算数量
        Float assignTaskNum = (heartBeatInfo.getMemTotal() - HeraGlobalEnvironment.getSystemMemUsed()) / HeraGlobalEnvironment.getPerTaskUseMem();
        int sum = heartBeatInfo.getDebugRunning().size() + heartBeatInfo.getManualRunning().size() + heartBeatInfo.getRunning().size();
        if (sum > assignTaskNum.intValue()) {
            MasterLog.warn(ResultReason.TASK_LIMIT.getMsg() + ":{}, host:{}", sum, heartBeatInfo.getHost());
            return false;
        }
        return true;
    }


    protected abstract MasterWorkHolder doSelect(HeraHostGroupVo hostGroup, MasterContext masterContext);
}
