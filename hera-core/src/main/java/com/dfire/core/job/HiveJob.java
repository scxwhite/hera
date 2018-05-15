package com.dfire.core.job;

import com.dfire.common.constant.RunningJobKeys;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.config.HeraGlobalEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午7:59 2018/5/12
 * @desc
 */
@Slf4j
public class HiveJob extends ProcessJob {

    public final String UDF_SQL_NAME = "hera_udf.sql";

    private HeraFileService heraFileService;
    private ApplicationContext applicationContext;

    public HiveJob(JobContext jobContext, ApplicationContext applicationContext) {
        super(jobContext);
        this.applicationContext = applicationContext;
        heraFileService = (HeraFileService) applicationContext.getBean("heraFileService");
        jobContext.getProperties().setProperty(RunningJobKeys.JOB_RUN_TYPE, "HiveJob");
    }

    @Override
    public int run() {
        Integer exitCode = runInner();
        return exitCode;
    }

    private Integer runInner() {
        String script = getProperties().getLocalProperty(RunningJobKeys.JOB_SCRIPT);
        File file = new File(jobContext.getWorkDir() + File.separator + new Date().getTime() + ".hive");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("创建.hive失败");
            }

        }

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file),
                    Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
            writer.write(script.replaceAll("^--.*", "--"));

        } catch (Exception e) {
            jobContext.getHeraJobHistory().getLog().appendHeraException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }

        getProperties().setProperty(RunningJobKeys.RUN_HIVE_PATH, file.getAbsolutePath());
        return super.run();
    }

    @Override
    public List<String> getCommandList() {
        String hiveFilePath = getProperty(RunningJobKeys.RUN_HIVE_PATH, "");
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        String shellPrefix = "";
        String user = "";
        if (jobContext.getRunType() == 1 || jobContext.getRunType() == 2) {
            user = jobContext.getHeraJobHistory().getOperator();
            shellPrefix = "sudo -u" + user;
        } else if (jobContext.getRunType() == 3) {
            user = jobContext.getDebugHistory().getOwner();
            shellPrefix = "sudo -u" + user;
        } else if (jobContext.getRunType() == 4) {
            shellPrefix = "";
        } else {
            log.info("没有运行类型 runType = " + jobContext.getRunType());
        }

        String[] excludeFile = HeraGlobalEnvironment.excludeFile.split(";");
        boolean isDocToUnix = true;
        if (!ArrayUtils.isEmpty(excludeFile)) {
            String lowCaseShellPath = hiveFilePath.toLowerCase();
            for (String exclude : excludeFile) {
                if (lowCaseShellPath.endsWith("." + exclude)) {
                    isDocToUnix = false;
                    break;
                }
            }
        }

        if (isDocToUnix) {
            list.add("dos2unix " + hiveFilePath);
            list.add("dos2unix file" + hiveFilePath);
        }

        sb.append("f").append(hiveFilePath);

        if (shellPrefix.trim().length() > 0) {
            String envFilePath = this.getClass().getClassLoader().getResource("/").getPath() + "env.sh";
            String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
            String localEnvFilePath = jobContext.getWorkDir() + File.separator + "env.sh";
            File file = new File(envFilePath);
            if (file.exists()) {
                list.add("cp " + envFilePath + " " + jobContext.getWorkDir());
                File tmpFile = new File(tmpFilePath);
                OutputStreamWriter tmpWriter = null;
                try {
                    if (!tmpFile.exists()) {
                        tmpFile.createNewFile();
                    }
                    tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                            Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                    tmpWriter.write("source " + localEnvFilePath + "; source" + sb.toString());
                } catch (Exception e) {
                    jobContext.getHeraJobHistory().getLog().appendHeraException(e);
                } finally {
                    IOUtils.closeQuietly(tmpWriter);
                }
                list.add("chmod -R 777" + jobContext.getWorkDir());
                list.add(shellPrefix + " sh" + tmpFilePath);
            } else {
                list.add("chmod -R 777" + jobContext.getWorkDir());
                list.add(shellPrefix + " hive" + sb.toString());
            }
        } else {
            list.add("hive" + sb.toString());
        }
        return list;
    }
}
