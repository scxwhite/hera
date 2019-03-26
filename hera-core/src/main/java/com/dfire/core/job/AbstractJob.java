package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.core.util.EmrUtils;
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
            shellPrefix = "sudo -E -u " + jobContext.getHeraJobHistory().getOperator();
        } else if (jobContext.getRunType() == JobContext.DEBUG_RUN) {
            shellPrefix = "sudo -E -u " + jobContext.getDebugHistory().getOwner();
        } else if (jobContext.getRunType() == JobContext.SYSTEM_RUN) {
            shellPrefix = "";
        } else {
            log("没有RunType=" + jobContext.getRunType() + " 的执行类别");
        }
        return shellPrefix;
    }

    protected String generateRunCommand(JobRunTypeEnum runTypeEnum, String prefix, String jobPath) {
        StringBuilder command = new StringBuilder();
        // emr集群
        if (HeraGlobalEnvironment.isEmrJob()) {
            //这里的参数使用者可以自行修改，从hera机器上向emr集群分发任务
            command.append("ssh -o StrictHostKeyChecking=no").append(Constants.BLANK_SPACE);
            command.append("-i /home/docker/conf/bigdata.pem").append(Constants.BLANK_SPACE);
            command.append("hadoop@").append(EmrUtils.getIp()).append(Constants.BLANK_SPACE).append("\\").append(Constants.NEW_LINE);
            command.append("<< eeooff").append(Constants.NEW_LINE);
            switch (runTypeEnum) {
                case Spark:
                    command.append(HeraGlobalEnvironment.getJobSparkSqlBin()).append(" -e ").append("\"").append(prefix).append(" `cat ").append(jobPath).append("`\"");
                    break;
                case Hive:
                    command.append(HeraGlobalEnvironment.getJobHiveBin()).append(" -e \"`cat ").append(jobPath).append("`\"");
                    break;
                case Shell:
                    command.append("`cat ").append(jobPath).append("`");
                    break;
                default:
                    break;
            }
            command.append(Constants.NEW_LINE);
            command.append("eeooff");
        } else {
            switch (runTypeEnum) {
                case Shell:
                    command.append("source ").append(jobPath);
                    break;
                case Spark:
                    command.append(HeraGlobalEnvironment.getJobSparkSqlBin()).append(prefix).append(" -f ").append(jobPath);
                    break;
                case Hive:
                    command.append(HeraGlobalEnvironment.getJobHiveBin()).append(" -f ").append(jobPath);
                default:
                    break;
            }
        }
        return command.toString();
    }


    protected String dosToUnix(String script) {
        return script.replace("\r\n", "\n");
    }

    protected boolean checkDosToUnix(String filePath) {
        if (HeraGlobalEnvironment.isEmrJob()) {
            return false;
        }
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
