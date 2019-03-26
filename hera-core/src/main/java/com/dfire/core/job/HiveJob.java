package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.config.HeraGlobalEnvironment;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午7:59 2018/5/12
 * @desc
 */
public class HiveJob extends ProcessJob {


    public HiveJob(JobContext jobContext) {
        super(jobContext);
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "HiveJob");
    }

    @Override
    public int run() throws Exception {
        return runInner();
    }

    private Integer runInner() throws Exception {
        String script = getProperties().getLocalProperty(RunningJobKeyConstant.JOB_SCRIPT);
        File file = new File(jobContext.getWorkDir() + File.separator + System.currentTimeMillis() + ".hive");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new HeraException("创建.hive失败");
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")))) {
            writer.write(dosToUnix(script.replaceAll("^--.*", "--")));
        } catch (Exception e) {
            if (jobContext.getHeraJobHistory() != null) {
                jobContext.getHeraJobHistory().getLog().appendHeraException(e);
            } else {
                jobContext.getDebugHistory().getLog().appendHeraException(e);
            }
            throw new HeraException("脚本写入文件失败:" + script);
        }

        getProperties().setProperty(RunningJobKeyConstant.RUN_HIVE_PATH, file.getAbsolutePath());
        return super.run();
    }

    @Override
    public List<String> getCommandList() {
        String hiveFilePath = getProperty(RunningJobKeyConstant.RUN_HIVE_PATH, "");
        List<String> list = new ArrayList<>();
        String shellPrefix = getJobPrefix();
        boolean isDocToUnix = checkDosToUnix(hiveFilePath);
        if (isDocToUnix) {
            list.add("dos2unix " + hiveFilePath);
            log("dos2unix file" + hiveFilePath);
        }
        String hiveCommand = " -f " + hiveFilePath;

        if (StringUtils.isNotBlank(shellPrefix)) {
            String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
            File tmpFile = new File(tmpFilePath);
            OutputStreamWriter tmpWriter = null;
            if (!tmpFile.exists()) {
                try {
                    tmpFile.createNewFile();
                    tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                            Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                    tmpWriter.write(generateRunCommand(JobRunTypeEnum.Hive, "", hiveFilePath));
                } catch (Exception e) {
                    jobContext.getHeraJobHistory().getLog().appendHeraException(e);
                } finally {
                    if (tmpWriter != null) {
                        try {
                            tmpWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                list.add("chmod -R 777 " + jobContext.getWorkDir());
                list.add(shellPrefix + " sh " + tmpFilePath);
            } else {
                list.add("chmod -R 777 " + jobContext.getWorkDir());
                list.add(shellPrefix + HeraGlobalEnvironment.getJobHiveBin() + hiveCommand);
            }

        } else {
            list.add(HeraGlobalEnvironment.getJobHiveBin() + hiveCommand);
        }
        return list;
    }
}
