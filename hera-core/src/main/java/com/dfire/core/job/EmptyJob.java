package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.config.HeraGlobalEnv;

/**
 * Created by shuangbofu on 2019-12-18 16:49
 * 前置空job，设置emr地址
 */
public class EmptyJob extends AbstractJob {

    public EmptyJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public int run() {
        // 设置emr地址
       // jobContext.getHeraJobHistory().getProperties().put(Constants.EMR_ADDRESS, getIp(jobContext.getHeraJobHistory().getOperator()) + ":" + HeraGlobalEnv.getYarnPort());
        return 0;
    }

    @Override
    public void cancel() {

    }
}
