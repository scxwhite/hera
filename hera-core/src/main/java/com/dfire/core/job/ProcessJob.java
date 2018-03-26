package com.dfire.core.job;

import com.dfire.common.constant.RunningJobKeys;
import com.dfire.common.util.ConfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:01 2018/3/23
 * @desc 通过操作系统创建进程Process的Job任务
 */
@Slf4j
public abstract class ProcessJob extends AbstractJob implements Job {

    protected volatile Process process;
    protected volatile Map<String, String> envMap;

    public ProcessJob(JobContext jobContext) {
        super(jobContext);
        envMap = new HashMap<>(System.getenv());
    }

    public abstract List<String> getCommandList();

    private void buildHadoopConf(String jobType) {
        File dir = new File(jobContext.getWorkDir() + File.separator + "hadoop_conf");
        if (!dir.exists()) {
            dir.mkdir();
        }
        Map<String, String> core = new HashMap<>();
        Map<String, String> hdfs = new HashMap<>();
        Map<String, String> mapred = new HashMap<>();
        Map<String, String> yarn = new HashMap<>();
        jobContext.getProperties().getAllProperties().keySet().stream().forEach(key -> {
            if (key.startsWith("core-site.")) {
                core.put(key.substring("core-site.".length()), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith("hdfs-site.")) {
                hdfs.put(key.substring("hdfs-site.".length()), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith("mapred-site.")) {
                mapred.put(key.substring("mapred-site.".length()), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith("yarn-site.")) {
                yarn.put(key.substring("yarn-site.".length()), jobContext.getProperties().getProperty(key));
            }
        });

        if (jobType != null && jobType.equals("HiveJob")) {
            Configuration coreC = ConfUtil.getDefaultCoreSite();
            for (String key : core.keySet()) {
                coreC.set(key, core.get(key));
            }
            try {
                File xml = new File(dir.getAbsolutePath() + File.separator + "core-site.xml");
                if (xml.exists()) {
                    xml.delete();
                }
                xml.createNewFile();
                coreC.writeXml(new FileOutputStream(xml));
            } catch (Exception e) {
                log.error("create file core-site.xml error", e);
            }

            Configuration hdfsC = ConfUtil.getDefaultHdfsSite();
            for (String key : hdfs.keySet()) {
                hdfsC.set(key, hdfs.get(key));
            }
            try {
                File xml = new File(dir.getAbsolutePath() + File.separator + "hdfs-site.xml");
                if (xml.exists()) {
                    xml.delete();
                }
                xml.createNewFile();
                hdfsC.writeXml(new FileOutputStream(xml));
            } catch (Exception e) {
                log.error("create file hdfs-site.xml error", e);
            }


            Configuration mapredC = ConfUtil.getDefaultMapredSite();
            for (String key : mapred.keySet()) {
                mapredC.set(key, mapred.get(key));
            }
            try {
                File xml = new File(dir.getAbsolutePath() + File.separator + "mapred-site.xml");
                if (xml.exists()) {
                    xml.delete();
                }
                xml.createNewFile();
                mapredC.writeXml(new FileOutputStream(xml));
            } catch (Exception e) {
                log.error("create file mapred-site.xml error", e);
            }
            Configuration yarnC = ConfUtil.getDefaultYarnSite();
            for (String key : yarn.keySet()) {
                yarnC.set(key, mapred.get(key));
            }
            try {
                File xml = new File(dir.getAbsolutePath() + File.separator + "yarn-site.xml");
                if (xml.exists()) {
                    xml.delete();
                }
                xml.createNewFile();
                yarnC.writeXml(new FileOutputStream(xml));
            } catch (Exception e) {
                log.error("create file yarn-site.xml error", e);
            }
        }

        //HADOOP_CONF_DIR添加2个路径，分别为 WorkDir/hadoop_conf 和 HADOOP_HOME/conf
        String HADOOP_CONF_DIR = jobContext.getWorkDir() + File.separator + "hadoop_conf" + File.pathSeparator
                + ConfUtil.getHadoopConfDir();
        envMap.put("HADOOP_CONF_DIR", HADOOP_CONF_DIR);


    }

    private void buildHiveConf(String jobType) {
        File dir = new File(jobContext.getWorkDir() + File.separator + "hive_conf");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Map<String, String> hive = new HashMap<String, String>();
        for (String key : jobContext.getProperties().getAllProperties().keySet()) {
            if (key.startsWith("hive-site.")) {
                hive.put(key.substring("hive-site.".length()), jobContext.getProperties().getProperty(key));
            }
        }
        if (jobType != null && jobType.equals("HiveJob")) {
            Configuration hiveC = ConfUtil.getDefaultHiveSie();
            for (String key : hive.keySet()) {
                hiveC.set(key, hive.get(key));
            }
            try {
                File xml = new File(dir.getAbsolutePath() + File.separator + "hive-site.xml");
                if (xml.exists()) {
                    xml.delete();
                }
                xml.createNewFile();
                hiveC.writeXml(new FileOutputStream(xml));
            } catch (Exception e) {
                log.error("create file hive-site.xml error", e);
            }
        }
        String HIVE_CONF_DIR = jobContext.getWorkDir() + File.separator + "hive_conf" + File.pathSeparator +
                ConfUtil.getHiveConfDir();
        envMap.put("HIVE_CONF_DIR", HIVE_CONF_DIR);

    }

    @Override
    public int run() {
        int exitCode = -999;
        String jobType = jobContext.getProperties().getAllProperties().get(RunningJobKeys.JOB_RUN_TYPE);
        buildHadoopConf(jobType);
        buildHiveConf(jobType);
        jobContext.getProperties().getAllProperties().keySet().stream()
                  .filter(key -> jobContext.getProperties().getProperty(key) != null && (key.startsWith("instance.") || key.startsWith("secret.")))
                  .forEach(k -> envMap.put(k, jobContext.getProperties().getProperty(k)));
        envMap.put("instance.workDir", jobContext.getWorkDir());
        log.info("获取命令");

        List<String> commands = getCommandList();
        commands.stream().forEach(command -> {

        });

        return 0;
    }

    @Override
    public void cancel() {

    }
}
