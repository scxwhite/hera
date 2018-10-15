package com.dfire.core.route.strategy;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.route.WorkerRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:47 2018/10/11
 * @desc 按照列表头选择机器
 */
@Slf4j
public class WorkerRouterFirst extends WorkerRouter {


    @Override
    public MasterWorkHolder selectWorker(int hostGroupId, MasterContext masterContext) {

        MasterWorkHolder workHolder = null;
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroupCache = masterContext.getHostGroupCache().get(hostGroupId);
            List<String> hosts = hostGroupCache.getHosts();
            if (hosts != null && hosts.size() > 0) {
                int size = hosts.size();
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
            log.warn("select work is :{}", workHolder.getChannel().remoteAddress());
        }
        return workHolder;
    }
}
