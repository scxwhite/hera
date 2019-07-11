package com.dfire.core.job;

import com.alibaba.fastjson.JSONObject;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author scx
 * @desc 先从hera机器 上传到emr集群 上传到hadoop
 */
public class UploadEmrFileJob extends ProcessJob {


    private String defaultOwner = "hadoop";

    private String sourcePath;

    private String owner;

    private String fileName;

    private String ip;

    private String targetPath;

    private String prefix;

    private String host;
    /**
     * 是否上传到hdfs 默认是，否则保存到/tmp
     */
    private boolean moveHadoop;

    public UploadEmrFileJob(JobContext jobContext, String sourcePath, String owner, String fileName, String ip) {
        super(jobContext);
        this.sourcePath = sourcePath;
        this.fileName = fileName;
        this.ip = ip;
        this.owner = StringUtils.isBlank(owner) ? defaultOwner : owner;
        this.moveHadoop = true;
    }


    public UploadEmrFileJob(String prefix, String sourcePath, String targetPath, String host, JobContext jobContext) {
        super(jobContext);
        this.prefix = prefix;
        this.targetPath = targetPath;
        this.sourcePath = sourcePath;
        this.moveHadoop = false;
        this.host = host;
    }


    @Override
    public List<String> getCommandList() {
        List<String> commands = new ArrayList<>();
        //从固定集群上传到hadoop
        if (moveHadoop) {
            commands.add("scp -o StrictHostKeyChecking=no -i /home/docker/conf/fixed.pem -r " + sourcePath + " " + owner + "@" + ip + ":/tmp/");
            commands.add(emr.getFixLogin(ip) + " hadoop fs -moveFromLocal /tmp/" + fileName + " " + HeraGlobalEnv.getHdfsUploadPath());
        } else {//上传到机器上即可
            commands.add(prefix + " " + sourcePath + " " + host + ":" + targetPath);
        }
        MonitorLog.debug("组装后的命令为:" + JSONObject.toJSONString(commands));
        return commands;
    }
}
