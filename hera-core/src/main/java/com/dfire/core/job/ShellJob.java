package com.dfire.core.job;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.RunningJobKeys;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.util.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author xiaosuda
 * @date 2018/4/13
 */
@Slf4j
public class ShellJob extends ProcessJob {

    private String shell;


    public ShellJob(JobContext jobContext) {
        super(jobContext);
    }

    public ShellJob(JobContext jobContext, String shell) {
        this(jobContext);
        this.shell = shell;
    }

    /**
     * 脚本执行命令集合
     * 主要包括：切换用户，修改文件权限，执行制定脚本
     * @return  命令集合
     */
    @Override
    public List<String> getCommandList() {
        String script;
        if (shell != null) {
            script = shell;
        } else {
            script = getProperties().getLocalProperty(RunningJobKeys.JOB_SCRIPT);
        }
        OutputStreamWriter outputStreamWriter = null;
        try {
            File f = new File(jobContext.getWorkDir() + File.separator + (System.currentTimeMillis()) + ".sh");
            if (!f.exists()) {
                f.createNewFile();
            }
            outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream(f),
                    Charset.forName(jobContext.getProperties().getProperty("hera.encode", "utf-8")));

            outputStreamWriter.write(script);

            getProperties().setProperty(RunningJobKeys.RUN_SHELLPATH, f.getAbsolutePath());

        } catch (IOException e) {
            jobContext.getHeraJobHistory().getLog().appendHeraException(e);
        } finally {
            IOUtils.closeQuietly(outputStreamWriter);
        }
        String shellFilePath = getProperty(RunningJobKeys.RUN_SHELLPATH, "");
        List<String> list = new ArrayList<>();
        //修改权限
        String shellPrefix = "";
        String user = "";
        if (jobContext.getRunType() == JobContext.SCHEDULE_RUN || jobContext.getRunType() == JobContext.MANUAL_RUN) {
            user = jobContext.getHeraJobHistory().getOperator();
            shellPrefix = "sudo -u " + user;
        } else if (jobContext.getRunType() == JobContext.DEBUG_RUN) {
            user = jobContext.getDebugHistory().getOwner();
            shellPrefix = "sudo -u " + user;
        } else if (jobContext.getRunType() == JobContext.SYSTEM_RUN) {
            shellPrefix = "";
        } else {
            log("没有RunType=" + jobContext.getRunType() + " 的执行类别");
        }
        //过滤不需要转化的后缀名
        String[] excludes = HeraGlobalEnvironment.excludeFile.split(";");
        boolean isDocToUnix = true;
        if (excludes != null && excludes.length > 0) {
            String lowCaseShellPath = shellFilePath.toLowerCase();
            for (String exclude : excludes) {
                if (lowCaseShellPath.endsWith("." + exclude)) {
                    isDocToUnix = false;
                    break;
                }
            }
        }

        if (isDocToUnix) {
            list.add("dos2unix " + shellFilePath);
            log("dos2unix file:" + shellFilePath);
        }
        //以当前用户执行shell
        if (shellPrefix.trim().length() > 0) {
            String envFilePath = this.getClass().getClassLoader().getResource("/").getPath() + "env.sh";
            String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
            String localEnvFilePath = jobContext.getWorkDir() + File.separator + "env.sh";
            File f = new File(envFilePath);
            if (f.exists()) {
                list.add("cp " + envFilePath + " " + jobContext.getWorkDir());
                File tmpFile = new File(tmpFilePath);
                OutputStreamWriter tmpWriter = null;
                try {
                    if (!tmpFile.exists()) {
                        tmpFile.createNewFile();
                    }
                    tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                            Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                    tmpWriter.write("source " + localEnvFilePath + "; source" + shellFilePath);
                } catch (Exception e) {
                    jobContext.getHeraJobHistory().getLog().appendHeraException(e);
                } finally {
                    IOUtils.closeQuietly(tmpWriter);
                }
                list.add(CommandUtils.changeFileAuthority(jobContext.getWorkDir()));
                list.add(CommandUtils.getRunShCommand(shellPrefix,tmpFilePath));
            } else {
                list.add(CommandUtils.changeFileAuthority(jobContext.getWorkDir()));
                list.add(CommandUtils.getRunShCommand(shellPrefix,shellFilePath));
            }
        } else {
            list.add("sh " + shellFilePath);
        }
        log.info("命令：{}", JSONObject.toJSONString(list));
        return list;
    }

    @Override
    public int run() {
        return super.run();
    }
}
