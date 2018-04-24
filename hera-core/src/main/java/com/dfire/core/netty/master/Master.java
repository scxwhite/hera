package com.dfire.core.netty.master;


import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraGroup;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.listenter.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc
 */
@Component
@Slf4j
public class Master {

    private MasterContext masterContext;
    private Map<Long, HeraAction>  heraActionMap;

    public Master(final MasterContext masterContext) {
        this.masterContext = masterContext;
        HeraGroup globalGroup = masterContext.getHeraGroupService().getGlobalGroup();

        if(HeraGlobalEnvironment.env.equalsIgnoreCase("pre")) {
            //预发环境不执行调度
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }

        masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));


    }
}
