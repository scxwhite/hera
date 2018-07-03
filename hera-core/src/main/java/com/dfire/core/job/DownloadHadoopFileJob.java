package com.dfire.core.job;


import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:18 2018/5/1
 * @desc
 */
@Slf4j
public class DownloadHadoopFileJob extends ProcessJob {

    private String hadoopPath;
    private String localPath;

    public DownloadHadoopFileJob(JobContext jobContext, String hadoopPath, String localPath) {
        super(jobContext);
        this.hadoopPath = hadoopPath;
        this.localPath = localPath;
    }

    @Override
    public List<String> getCommandList() {
        String hadoopCommand = CancelHadoopJob.getHadoopCmd(envMap);
        List<String> commands = new ArrayList<>();
        commands.add(hadoopCommand + "fs -copyToLocal" + hadoopPath + " " + localPath);
        //格式转换
        commands.add("dos2unix " + localPath);
        log.info("dos2unix file: " + localPath);
        return commands;
    }
}
