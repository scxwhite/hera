package com.dfire.core.job;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.exception.HeraException;
import com.dfire.common.util.Pair;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.util.CommandUtils;
import com.dfire.logs.MonitorLog;

import java.util.ArrayList;
import java.util.List;


/**
 * @author scx
 * @desc 先从hera机器 上传到emr集群 上传到hadoop
 */
public class UploadEmrFileJob extends ProcessJob {


    private String sourcePath;


    private String fileName;

    private String ip;

    private String targetPath;

    private String prefix;

    private String host;
    /**
     * 是否上传到hdfs 默认是，否则保存到/tmp
     */
    private boolean moveHadoop;

    public UploadEmrFileJob(JobContext jobContext, String sourcePath, String fileName, String ip) {
        super(jobContext);
        this.sourcePath = sourcePath;
        this.fileName = fileName;
        this.ip = ip;
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
    public List<String> getCommandList() throws HeraException {
        List<String> commands = new ArrayList<>();
        //从固定集群上传到hadoop
        if (moveHadoop) {
            String fixLogin = emr.getFixLogin(ip);
            Pair<String, String> pair = CommandUtils.parseCmd(fixLogin);
            commands.add(pair.fst() + sourcePath + " " + pair.snd() + ":/tmp/");
            commands.add(fixLogin + " hadoop fs -moveFromLocal /tmp/" + fileName + " " + HeraGlobalEnv.getHdfsUploadPath());
        } else {//上传到机器上即可
            commands.add(prefix + " " + sourcePath + " " + host + ":" + targetPath);
        }
        MonitorLog.info("组装后的命令为:" + JSONObject.toJSONString(commands));
        return commands;
    }
}
