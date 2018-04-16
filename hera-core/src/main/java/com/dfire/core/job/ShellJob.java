package com.dfire.core.job;

import java.util.List;

/**
 * Created by xiaosuda on 2018/4/13.
 */
public class ShellJob extends ProcessJob {

    private String shell;

    public ShellJob(JobContext jobContext) {
        super(jobContext);
    }
    public ShellJob(JobContext jobContext,String shell){
        this(jobContext);
        this.shell=shell;
    }

    @Override
    public List<String> getCommandList() {
        return null;
    }
}
