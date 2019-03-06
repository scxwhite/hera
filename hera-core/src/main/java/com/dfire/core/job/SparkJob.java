package com.dfire.core.job;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.logs.ErrorLog;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

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

    public final String UDF_SQL_NAME = "hera_udf.sql";


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
                file.createNewFile();
            } catch (IOException e) {
                ErrorLog.error("创建.spark失败");
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
            if (writer != null) {
                writer.close();
            }
        }

        getProperties().setProperty(RunningJobKeyConstant.RUN_SPARK_PATH, file.getAbsolutePath());
        return super.run();
    }

    @Override
    public List<String> getCommandList() {
        String sparkFilePath = getProperty(RunningJobKeyConstant.RUN_SPARK_PATH, "");
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        String shellPrefix = getJobPrefix();
        boolean isDocToUnix = checkDosToUnix(sparkFilePath);

        if (isDocToUnix) {
            list.add("dos2unix " + sparkFilePath);
            log("dos2unix file" + sparkFilePath);
        }

        sb.append(" -f " + sparkFilePath + " " +
                HeraGlobalEnvironment.getSparkMaster() + " " +
                HeraGlobalEnvironment.getSparkDriverCores() + " " +
                HeraGlobalEnvironment.getSparkDriverMemory());
//                HeraGlobalEnvironment.getSparkExecutorCores() + " " +
//                HeraGlobalEnvironment.getSparkExecutorMemory());

        if (StringUtils.isNotBlank(shellPrefix)) {

            String tmpFilePath = jobContext.getWorkDir() + File.separator + "tmp.sh";
            File tmpFile = new File(tmpFilePath);
            OutputStreamWriter tmpWriter = null;
            if (!tmpFile.exists()) {
                try {
                    tmpFile.createNewFile();
                    tmpWriter = new OutputStreamWriter(new FileOutputStream(tmpFile),
                            Charset.forName(jobContext.getProperties().getProperty("hera.fs.encode", "utf-8")));
                    tmpWriter.write(HeraGlobalEnvironment.getSparkBaseDir() +
                            "/bin/spark-sql " + sb.toString());
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
                list.add(shellPrefix + " " + HeraGlobalEnvironment.getSparkBaseDir() +
                        "/bin/spark-sql " + sb.toString());
            }

        } else {
            list.add(HeraGlobalEnvironment.getSparkBaseDir() + "/bin/spark-sql " + sb.toString());
        }
        return list;
    }
}
