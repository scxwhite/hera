package com.dfire.core.bo;

import com.dfire.core.job.JobContext;
import com.dfire.core.job.ShellJob;
import com.dfire.core.netty.util.RunningJobKeys;

/**
 *
 * @author xiaosuda
 * @date 2018/4/13
 */
public class MemUseRateJob extends ShellJob {

    private double rate;
    private double totalCore;

    public MemUseRateJob(JobContext jobContext, double rate) {
        super(jobContext,"free -m | grep buffers/cache");
        jobContext.getProperties().getAllProperties().put(RunningJobKeys.JOB_RUN_TYPE, "MemUseRateJob");
        this.rate = rate;
    }

}
