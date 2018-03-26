package com.dfire.core.job;

import com.dfire.common.util.JobUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        String logContent = null;
        if(jobContext.getZeusJobHistory() != null) {
            logContent = jobContext.getZeusJobHistory().getLog().getContent();
        } else if(jobContext.getDebugHistory() != null) {
            logContent = jobContext.getDebugHistory().getLog().getContent();
        }
        if(logContent != null) {
            String hadoopCmd = JobUtils.getHadoopCmd(envMap);
            commands = Arrays.asList(logContent.split("\n")).stream().filter(line-> line.startsWith("Starting Job ="))
                    .map(line -> {
                        String jobId = line.substring(line.lastIndexOf("job_"));
                        return hadoopCmd + " job -kill " + jobId;
                    }).collect(Collectors.toList());

        }
        return commands;
    }
}
