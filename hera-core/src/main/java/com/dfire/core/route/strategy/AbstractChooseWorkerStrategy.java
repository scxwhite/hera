package com.dfire.core.route.strategy;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.check.CheckResult;
import com.dfire.core.route.check.ResultReason;
import com.dfire.logs.ScheduleLog;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/10/10
 * @desc 任务执行worker选择路由
 */
public abstract class AbstractChooseWorkerStrategy implements IStrategyWorker {


    /**
     * check ip 的worker能否选择为执行机器
     *
     * @param host
     * @param worker
     * @return
     */
    public boolean checkResource(String host, MasterWorkHolder worker) {
        if(worker == null){
            ScheduleLog.warn(ResultReason.NULL_WORKER.getMsg());
            return false;
        }
        if(worker.getHeartBeatInfo() == null){
            ScheduleLog.warn(ResultReason.NULL_HEART.getMsg());
            return false;
        }
        if(!worker.getHeartBeatInfo().getHost().trim().equals(host.trim())){
            ScheduleLog.warn(ResultReason.HOSTS_ERROR.getMsg());
            return false;
        }
        HeartBeatInfo heartBeatInfo = worker.getHeartBeatInfo();
        if(heartBeatInfo.getMemRate() == null || heartBeatInfo.getMemRate() < HeraGlobalEnvironment.getMaxMemRate()){
            ScheduleLog.warn(ResultReason.MEM_LIMIT.getMsg());
            return false;
        }
        if(heartBeatInfo.getCpuLoadPerCore() == null || heartBeatInfo.getCpuLoadPerCore() < HeraGlobalEnvironment.getMaxCpuLoadPerCore()){
            ScheduleLog.warn(ResultReason.LOAD_LIMIT.getMsg());
            return false;
        }

        // 配置计算数量
        Float assignTaskNum = (heartBeatInfo.getMemTotal() - HeraGlobalEnvironment.getSystemMemUsed()) / HeraGlobalEnvironment.getPerTaskUseMem();
        int sum = heartBeatInfo.getDebugRunning().size() + heartBeatInfo.getManualRunning().size() + heartBeatInfo.getRunning().size();
        if(sum > assignTaskNum.intValue()){
            ScheduleLog.warn(ResultReason.TASK_LIMIT.getMsg());
            return false;
        }
        return true;
    }
}
