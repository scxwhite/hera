package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/5/1
 * @desc
 */
public class HadoopShellJob extends ShellJob {

    public HadoopShellJob(JobContext jobContext) {
        super(jobContext);
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "HadoopShellJob");
    }

    @Override
    public int run() throws Exception {
        return super.run();
    }
}
