package com.dfire.core.route.strategy.impl;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.strategy.AbstractChooseWorkerStrategy;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MasterLog;
import com.dfire.logs.ScheduleLog;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:47 2018/10/11
 * @desc 遍历，顺序获取节点
 */
public class StrategyByFirstImpl extends AbstractChooseWorkerStrategy {

    @Override
    public MasterWorkHolder chooseWorker(JobElement jobElement, MasterContext masterContext) {

        MasterWorkHolder workHolder = null;
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroupCache = masterContext.getHostGroupCache().get(jobElement.getHostGroupId());
            if (hostGroupCache == null) {
                ErrorLog.error("Not found worker-group by hostGroupId:{} ,job_id:{}", jobElement.getHostGroupId(), jobElement.getJobId());
                return null;
            }

            List<String> hosts = hostGroupCache.getHosts();
            if (hosts != null && hosts.size() > 0) {
                int size = hosts.size();
                for (int i = 0; i < size && workHolder == null; i++) {
                    String host = hostGroupCache.selectHost();
                    for (MasterWorkHolder worker : masterContext.getWorkMap().values()) {
                        if (checkResource(host, worker)) {
                            workHolder = worker;
                            break;
                        }
                    }
                }
            }
        }
        if (workHolder != null) {
            ScheduleLog.warn("select work is :{}", workHolder.getChannel().getRemoteAddress());
        }
        return workHolder;
    }
}
