package com.dfire.core.job;

import com.dfire.config.HeraGlobalEnv;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:57 2018/5/1
 * @desc 文件上传，先上传服务器，然后存入hadoop
 */

public class UploadLocalFileJob extends ProcessJob {

    private String hadoopPath;
    private String localPath;

    public UploadLocalFileJob(JobContext jobContext,  String localPath, String hadoopPath) {
        super(jobContext);
        this.hadoopPath = hadoopPath;
        this.localPath = localPath;
    }


    @Override
    public List<String> getCommandList() {
        List<String> commands = new ArrayList<>();
        if (StringUtils.isNotBlank(HeraGlobalEnv.getKerberosKeytabPath()) && StringUtils.isNotBlank(HeraGlobalEnv.getKerberosPrincipal())) {
            commands.add("kinit -kt " + HeraGlobalEnv.getKerberosKeytabPath() + " " + HeraGlobalEnv.getKerberosPrincipal());
        }
        commands.add("hadoop fs -copyFromLocal " + localPath + " " + hadoopPath);
        return commands;
    }
}
