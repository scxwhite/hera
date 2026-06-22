package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: lihm
 * @time: Created in 上午9:59 2023/8/16
 * @desc
 */
public class MaxComputerJob extends ProcessJob {


    public MaxComputerJob(JobContext jobContext) {
        super(jobContext);
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "MaxComputerJob");
    }

    @Override
    public int run() throws Exception {
        return runInner();
    }

    private Integer runInner() throws Exception {
        String script = getProperties().getLocalProperty(RunningJobKeyConstant.JOB_SCRIPT);
        File file = new File(jobContext.getWorkDir() + File.separator + System.currentTimeMillis() + ".osql");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new HeraException("创建.osql失败");
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")))) {
            writer.write(dosToUnix(script.replaceAll("^--.*", "--")));
        } catch (Exception e) {
            throw new HeraException("脚本写入文件失败:" + script);
        }

        getProperties().setProperty(RunningJobKeyConstant.RUN_MAXCOMPUTER_PATH, file.getAbsolutePath());
        return super.run();
    }

    @Override
    public List<String> getCommandList() throws HeraException {
        String maxComputerFilePath = getProperty(RunningJobKeyConstant.RUN_MAXCOMPUTER_PATH, "");
        List<String> list = new ArrayList<>();
        String shellPrefix = getJobPrefix();
        boolean isDocToUnix = checkDosToUnix(maxComputerFilePath);
        if (isDocToUnix) {
            list.add("dos2unix " + maxComputerFilePath);
            log("dos2unix file" + maxComputerFilePath);
        }
        String maxComputerCommand = " -f " + maxComputerFilePath;
        String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
        File tmpFile = new File(tmpFilePath);
        OutputStreamWriter tmpWriter = null;
        if (!tmpFile.exists()) {
            try {
                tmpFile.createNewFile();
                tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                        Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                tmpWriter.write(generateRunCommand(JobRunTypeEnum.MaxComputer, "", maxComputerFilePath));
            } catch (Exception e) {
                throw new HeraException("组装命令异常", e);
            } finally {
                if (tmpWriter != null) {
                    try {
                        tmpWriter.close();
                    } catch (IOException e) {
                        ErrorLog.error("关闭输出流失败", e);
                    }
                }
            }
            list.add("chmod -R 777 " + tmpFilePath);
            list.add(shellPrefix + " sh " + tmpFilePath);
        } else {
            list.add("chmod -R 777 " + tmpFilePath);
            list.add(shellPrefix + HeraGlobalEnv.getJobMaxComputerBin() + maxComputerCommand);

        }
        return list;
    }
}
