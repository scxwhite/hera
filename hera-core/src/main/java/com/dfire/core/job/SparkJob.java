package com.dfire.core.job;

import com.dfire.common.constants.Constants;
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
 * @Description ： SparkJob
 * @Author ： HeGuoZi
 * @Date ： 15:53 2018/8/20
 * @Modified :
 */
public class SparkJob extends ProcessJob {


    public SparkJob(JobContext jobContext) {
        super(jobContext);
        jobContext.getProperties().setProperty(RunningJobKeyConstant.JOB_RUN_TYPE, "SparkJob");
    }

    @Override
    public int run() throws Exception {
        return runInner();
    }

    private Integer runInner() throws Exception {
        String script = getProperties().getLocalProperty(RunningJobKeyConstant.JOB_SCRIPT);
        File file = new File(jobContext.getWorkDir() + File.separator + System.currentTimeMillis() + ".spark");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException e) {
                ErrorLog.error("创建.spark失败", e);
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")))) {
            writer.write(dosToUnix(script.replaceAll("^--.*", "--")));
        } catch (Exception e) {
            throw new HeraException("写入文件失败:", e);
        }

        getProperties().setProperty(RunningJobKeyConstant.RUN_SPARK_PATH, file.getAbsolutePath());

        return super.run();
    }

    @Override
    public List<String> getCommandList() throws HeraException {
        String sparkFilePath = getProperty(RunningJobKeyConstant.RUN_SPARK_PATH, "");

        String shellPrefix = getJobPrefix();
        boolean isDocToUnix = checkDosToUnix(sparkFilePath);
        List<String> list = new ArrayList<>();
        if (isDocToUnix) {
            list.add("dos2unix " + sparkFilePath);
            log("dos2unix file" + sparkFilePath);
        } else {
            log("file path :" + sparkFilePath);
        }


        String prefix = getProperty(Constants.HERA_SPARK_CONF,
                getProperty(HeraGlobalEnv.getArea() + Constants.POINT + Constants.HERA_SPARK_CONF,
                        HeraGlobalEnv.getSparkMaster()
                                + " "
                                + HeraGlobalEnv.getSparkDriverCores()
                                + " "
                                + HeraGlobalEnv.getSparkDriverMemory()));
        String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
        File tmpFile = new File(tmpFilePath);
        OutputStreamWriter tmpWriter = null;
        if (!tmpFile.exists()) {
            try {
                if (!tmpFile.createNewFile()) {
                    throw new HeraException("创建临时文件失败" + tmpFile.getAbsolutePath());
                }
                tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                        Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                tmpWriter.write(generateRunCommand(JobRunTypeEnum.Spark, prefix, sparkFilePath));
            } catch (Exception e) {
                throw new HeraException("组装命令异常", e);
            } finally {
                if (tmpWriter != null) {
                    try {
                        tmpWriter.close();
                    } catch (IOException e) {
                        ErrorLog.error("关闭输出流异常", e);
                    }
                }
            }
            list.add("chmod -R 777 " + tmpFilePath);
            list.add(shellPrefix + " sh " + tmpFilePath);
        } else {
            list.add("chmod -R 777 " + tmpFilePath);
            list.add(shellPrefix + " " + HeraGlobalEnv.getJobSparkSqlBin() + "-f" + sparkFilePath);
        }

        return list;
    }
}
