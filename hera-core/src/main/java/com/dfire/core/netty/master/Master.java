package com.dfire.core.netty.master;


import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraGroup;
import com.dfire.core.config.HeraGlobalEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc
 */

@Slf4j
public class Master {

    private MasterContext masterContext;
    private Map<Long, HeraAction>  heraActionMap;

    public Master(final MasterContext masterContext) {
        this.masterContext = masterContext;
        HeraGroup globalGroup = masterContext.getHeraGroupService().getGlobalGroup();

        if(HeraGlobalEnvironment.env.equalsIgnoreCase("pre")) {
            //预发环境不执行调度
        }
    }
}
