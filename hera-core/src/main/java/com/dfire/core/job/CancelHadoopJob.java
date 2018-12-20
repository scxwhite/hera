package com.dfire.core.job;

import com.dfire.common.constants.Constants;
import com.dfire.common.vo.LogContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @return
     * @desc 从日志中解析出hadoop job 任务id, 拼接出kill job的命令,目前只考虑hive次一种任务
     */
    @Override
    public List<String> getCommandList() {
        List<String> commands = new ArrayList<>();
        LogContent logContent;
        if (jobContext.getHeraJobHistory() != null) {
            logContent = jobContext.getHeraJobHistory().getLog();
        } else if (jobContext.getDebugHistory() != null) {
            logContent = jobContext.getDebugHistory().getLog();
        } else {
            logContent = LogContent.builder().build();
        }
        String taskLog = logContent.toString();
        if (taskLog != null) {
            String hadoopCmd = getHadoopCmd(envMap);
            commands = Arrays.stream(taskLog.split(Constants.LOG_SPLIT)).filter(line -> line.contains("Starting Job ="))
                    .map(line -> {
                        String jobId = line.substring(line.indexOf("job_"), line.indexOf(Constants.COMMA));
                        return hadoopCmd + " job -kill " + jobId;
                    }).collect(Collectors.toList());
        }
        for (String command : commands) {
            logContent.append(command);
            logContent.append(Constants.LOG_SPLIT);
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
