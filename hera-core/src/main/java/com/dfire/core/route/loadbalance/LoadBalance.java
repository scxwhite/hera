package com.dfire.core.route.loadbalance;


import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.queue.JobElement;

/**
 * 负载均衡器
 * @author xiaosuda
 */
public interface LoadBalance {

    /**
     * 选择work
     * @param jobElement
     * @param masterContext
     * @return
     */
    MasterWorkHolder select(JobElement jobElement, MasterContext masterContext);

}
