package com.dfire.core.job;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:25 2018/4/26
 * @desc
 */
public class DownLoadJob extends AbstractJob{

    public DownLoadJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public int run() {
        return 0;
    }

    @Override
    public void cancel() {
        canceled = true;
    }
}
