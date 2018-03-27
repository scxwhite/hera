package com.dfire.core.job;

import com.dfire.common.util.HierarchyProperties;
import org.apache.commons.lang.StringUtils;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:49 2018/1/10
 * @desc
 */
public abstract class AbstractJob implements Job {

    protected JobContext jobContext;

    protected boolean canceled = false;

    public AbstractJob(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public JobContext getJobContext() {
        return jobContext;
    }

    public HierarchyProperties getProperties(){
        return jobContext.getProperties();
    }

    protected String getProperty(String key, String defaultValue) {
        return StringUtils.isBlank(jobContext.getProperties().getProperty(key)) ? defaultValue : jobContext.getProperties().getProperty(key);
    }

    protected void logConsole(String log){
        if(jobContext.getHeraJobHistory()!=null){
            jobContext.getHeraJobHistory().getLog().appendConsole(log);
        }
        if(jobContext.getDebugHistory()!=null){
            jobContext.getDebugHistory().getLog().appendConsole(log);
        }
    }

    protected void log(String log){
        if(jobContext.getHeraJobHistory()!=null){
            jobContext.getHeraJobHistory().getLog().appendHera(log);
        }
        if(jobContext.getDebugHistory()!=null){
            jobContext.getDebugHistory().getLog().appendHera(log);
        }
    }
    protected void log(Exception e){
        if(jobContext.getHeraJobHistory()!=null){
            jobContext.getHeraJobHistory().getLog().appendHeraException(e);
        }
        if(jobContext.getDebugHistory()!=null){
            jobContext.getDebugHistory().getLog().appendHeraException(e);
        }
    }
}
