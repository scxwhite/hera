package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.config.HeraGlobalEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:55 2018/3/26
 * @desc 扫描日志，揪出hadoop任务id，进行kill
 */
public class CancelHadoopJob extends ProcessJob {

    public CancelHadoopJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    /**
     * 解析日志  查找map-reduce 的jobId 根据id进行kill
     *
     * @return
     */
    @Override
    public List<String> getCommandList() {
        List<String> commands = new ArrayList<>();
        String logContent = null;
        if (jobContext.getHeraJobHistory() != null) {
            logContent = jobContext.getHeraJobHistory().getLog().toString();
        } else if (jobContext.getDebugHistory() != null) {
            logContent = jobContext.getDebugHistory().getLog().toString();
        }
        if (logContent != null) {
            String hadoopCmd = getHadoopCmd(envMap);
            String[] logLine = logContent.split(Constants.LOG_SPLIT);
            for (String line : logLine) {
                if (line.contains("Starting Job =")) {
                    String jobId = line.substring(line.indexOf("job_"), line.indexOf(Constants.COMMA));
                    String killCommand = hadoopCmd + " job -kill " + jobId;
                    if (HeraGlobalEnv.isEmrJob()) {
                        killCommand = getLoginCmd() + " " + killCommand;
                    }
                    commands.add(killCommand);
                    log(killCommand);
                } else if (line.contains("Submitted application")) {
                    String appId = line.substring(line.indexOf("Submitted application") + "Submitted application".length() + 1).replace(Constants.LOG_SPLIT, "");
                    String killCommand = "yarn application -kill " + appId;
                    if (HeraGlobalEnv.isEmrJob()) {
                        killCommand = getLoginCmd() + " " + killCommand;
                    }
                    commands.add(killCommand);
                    log(killCommand);
                }
            }
        }
        return commands;
    }

    /**
     * @param evenMap
     * @return
     * @desc 获取系统环境的hadoop命令
     */
    public static String getHadoopCmd(Map<String, String> evenMap) {
        StringBuilder cmd = new StringBuilder(64);
        String hadoopHome = evenMap.get("HADOOP_HOME");
        if (hadoopHome != null) {
            cmd.append(hadoopHome).append("/bin/");
        }
        cmd.append("hadoop ");
        return cmd.toString();
    }
}
