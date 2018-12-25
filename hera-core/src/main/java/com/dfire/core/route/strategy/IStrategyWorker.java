package com.dfire.core.route.strategy;


import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.core.queue.JobElement;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:53 2018/10/11
 * @desc
 */
public interface IStrategyWorker {

    /**
     * 获取可执行的worker节点
     * @param jobElement
     * @param masterContext
     * @return
     */
    MasterWorkHolder chooseWorker(JobElement jobElement, MasterContext masterContext);

}
