package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.config.HeraGlobalEnvironment;
import org.apache.commons.lang.ArrayUtils;
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

    public HierarchyProperties getProperties() {
        return jobContext.getProperties();
    }

    protected String getProperty(String key, String defaultValue) {
        return StringUtils.isBlank(jobContext.getProperties().getProperty(key)) ? defaultValue : jobContext.getProperties().getProperty(key);
    }

    protected String getJobPrefix() {
        String shellPrefix = null;
        if (jobContext.getRunType() == JobContext.SCHEDULE_RUN || jobContext.getRunType() == JobContext.MANUAL_RUN) {
            shellPrefix = "sudo -u " + jobContext.getHeraJobHistory().getOperator();
        } else if (jobContext.getRunType() == JobContext.DEBUG_RUN) {
            shellPrefix = "sudo -u " + jobContext.getDebugHistory().getOwner();
        } else if (jobContext.getRunType() == JobContext.SYSTEM_RUN) {
            shellPrefix = "";
        } else {
            log("没有RunType=" + jobContext.getRunType() + " 的执行类别");
        }
        return shellPrefix;
    }

    protected boolean checkDosToUnix(String filePath) {
        String[] excludeFile = HeraGlobalEnvironment.excludeFile.split(Constants.SEMICOLON);
        if (!ArrayUtils.isEmpty(excludeFile)) {
            String lowCaseShellPath = filePath.toLowerCase();
            for (String exclude : excludeFile) {
                if (lowCaseShellPath.endsWith(Constants.POINT + exclude)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void logConsole(String log) {
        if (jobContext.getHeraJobHistory() != null) {
            jobContext.getHeraJobHistory().getLog().appendConsole(log);
        }
        if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendConsole(log);
        }
    }

    protected void log(String log) {
        if (jobContext.getHeraJobHistory() != null) {
            jobContext.getHeraJobHistory().getLog().appendHera(log);
        }
        if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendHera(log);
        }
    }

    protected void log(Exception e) {
        if (jobContext.getHeraJobHistory() != null) {
            jobContext.getHeraJobHistory().getLog().appendHeraException(e);
        }
        if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendHeraException(e);
        }
    }
}
