package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.emr.Emr;
import com.dfire.core.emr.WrapEmr;
import com.dfire.logs.HeraLog;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:49 2018/1/10
 * @desc
 */
public abstract class AbstractJob implements Job {

    protected JobContext jobContext;

    protected boolean canceled = false;


    private static Pattern hostPattern = Pattern.compile("\\w+@[\\w\\.-]+\\s*");

    protected Emr emr;

    public AbstractJob(JobContext jobContext) {
        this.jobContext = jobContext;
        emr = new WrapEmr();
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
        String val;
        return StringUtils.isBlank(val = jobContext.getProperties().getProperty(key)) ? defaultValue : val;
    }

    protected String getProperty(String key) throws NullPointerException {
        String val;
        if ((val = jobContext.getProperties().getProperty(key)) == null) {
            throw new NullPointerException("找不到" + key + "的值");
        }
        return val;
    }

    protected String getJobPrefix() {
        return Optional.ofNullable(buildPrefix()).orElse(Constants.BLANK_SPACE);
    }

    private String buildPrefix() {
        if (HeraGlobalEnv.isEmrJob()) {
            return null;
        }

        String user = getUser();
        if (StringUtils.isBlank(user)) {
            return null;
        }
        if (HeraGlobalEnv.isMacOS()) {
            return "sudo -u " + user;
        }
        return "sudo -E -i -s -u " + user;
    }

    protected String generateRunCommand(JobRunTypeEnum runTypeEnum, String prefix, String jobPath) throws Exception {
        StringBuilder command = new StringBuilder();
        // emr集群
        if (HeraGlobalEnv.isEmrJob()) {
            File file = new File(jobPath);
            if (!file.exists()) {
                throw new HeraException("找不到脚本:" + jobPath);
            }
            String loginCmd = getProperty(Constants.EMR_SELECT_WORK);
            String targetPath = Constants.TMP_PATH;
            uploadFile(loginCmd, targetPath, file.getParent());
            String runPath = targetPath + File.separator + file.getParentFile().getName() + File.separator + file.getAbsoluteFile().getName();
            //这里的参数使用者可以自行修改，从hera机器上向emr集群分发任务
            command.append(loginCmd).append(Constants.BLANK_SPACE).append("\\").append(Constants.NEW_LINE);
            command.append(Constants.SSH_PREFIX).append(Constants.NEW_LINE);
            switch (runTypeEnum) {
                case Spark:
                    command.append(HeraGlobalEnv.getJobSparkSqlBin()).append(prefix).append(" -f ").append(runPath);
                    break;
                case Hive:
                    command.append(HeraGlobalEnv.getJobHiveBin()).append(" -f ").append(runPath);
                    break;
                case Shell:
                    command.append("bash ").append(runPath);
                    break;
                default:
                    break;
            }
            command.append(Constants.NEW_LINE);
            command.append(Constants.SSH_SUFFIX);
        } else {
            switch (runTypeEnum) {
                case Shell:
                    command.append("bash ").append(jobPath);
                    break;
                case Spark:
                    command.append(HeraGlobalEnv.getJobSparkSqlBin()).append(prefix).append(" -f ").append(jobPath);
                    break;
                case Hive:
                    command.append(HeraGlobalEnv.getJobHiveBin()).append(" -f ").append(jobPath);
                default:
                    break;
            }
        }
        return command.toString();
    }


    protected String getLoginCmd() {
        if (!HeraGlobalEnv.isEmrJob()) {
            return "localhost";
        }
        String host;
        return isFixedEmrJob() && StringUtils.isNotBlank(host = getFixedHost()) ? emr.getFixLogin(host) : emr.getLogin(getUser());
    }

    private void uploadFile(String loginCmd, String targetPath, String parentPath) throws Exception {
        String scpCmd = loginCmd.replace("ssh", "scp");
        Matcher matcher = hostPattern.matcher(scpCmd);
        String loginStr;
        if (matcher.find()) {
            loginStr = matcher.group(0);
        } else {
            throw new HeraException("查找ip失败" + scpCmd);
        }
        String prefix = scpCmd.replace(loginStr, "").replace("-p", "-P") + " -r ";
        UploadEmrFileJob uploadJob = new UploadEmrFileJob(prefix,
                parentPath, targetPath, loginStr, jobContext);
        uploadJob.run();
    }


    /**
     * 判断是否为固定集群任务
     *
     * @return boolean
     */
    protected boolean isFixedEmrJob() {
        return Boolean.valueOf(getProperty(HeraGlobalEnv.getArea() + Constants.POINT + Constants.HERA_EMR_FIXED, getProperty(Constants.HERA_EMR_FIXED, "false")).trim().toLowerCase());
    }

    /**
     * 获得固定集群的ip
     *
     * @return boolean
     */
    protected String getFixedHost() {
        return getProperty(HeraGlobalEnv.getArea() + "." + Constants.HERA_EMR_FIXED_HOST, HeraGlobalEnv.emrFixedHost).trim();
    }

    /**
     * 判断是否为动态emr集群任务
     *
     * @return boolean
     */
    protected boolean isDynamicEmrJob() {
        return HeraGlobalEnv.isEmrJob() && !isFixedEmrJob();
    }

    protected String getUser() {
        if (jobContext.getRunType() == JobContext.SCHEDULE_RUN || jobContext.getRunType() == JobContext.MANUAL_RUN) {
            return jobContext.getHeraJobHistory().getOperator();
        } else if (jobContext.getRunType() == JobContext.DEBUG_RUN) {
            return jobContext.getDebugHistory().getOwner();
        } else if (jobContext.getRunType() == JobContext.SYSTEM_RUN) {
            return "";
        } else {
            log("没有RunType=" + jobContext.getRunType() + " 的执行类别");
        }
        return null;
    }


    protected String dosToUnix(String script) {
        return script.replace("\r\n", "\n");
    }

    protected boolean checkDosToUnix(String filePath) {
        if (HeraGlobalEnv.isEmrJob()) {
            return false;
        }
        String[] excludeFile = HeraGlobalEnv.excludeFile.split(Constants.SEMICOLON);
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
        } else if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendConsole(log);
        } else {
            HeraLog.info(log);
        }
    }

    protected void log(String log) {
        if (jobContext.getHeraJobHistory() != null) {
            jobContext.getHeraJobHistory().getLog().appendHera(log);
        } else if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendHera(log);
        } else {
            HeraLog.warn(log);
        }
    }

    protected void log(Exception e) {
        if (jobContext.getHeraJobHistory() != null) {
            jobContext.getHeraJobHistory().getLog().appendHeraException(e);
        } else if (jobContext.getDebugHistory() != null) {
            jobContext.getDebugHistory().getLog().appendHeraException(e);
        } else {
            HeraLog.error(e.getMessage(), e);
        }
    }
}
