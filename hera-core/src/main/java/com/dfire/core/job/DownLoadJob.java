package com.dfire.core.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public int run() throws Exception {
        List<Job> jobList = new ArrayList<>();
        for(Map<String, String> map : jobContext.getResources()) {
            if(map.get("uri") != null) {
                String name = map.get("name");
                String uri = map.get("uri");
                if(uri.startsWith("hdfs://")) {
                    String localPath = jobContext.getWorkDir() + File.separator + (name == null ? "" : name);
                    String hadoopPath = uri.substring(7);
                    jobList.add(new DownloadHadoopFileJob(jobContext, hadoopPath, localPath));
                }
            }
        }
        Integer exitCode = 0;
        for(Job job : jobList) {
                exitCode = job.run();
        }
        return exitCode;
    }

    @Override
    public void cancel() {
        canceled = true;
    }
}
