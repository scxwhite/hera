package com.dfire.core.job;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.RunningJobKeys;
import com.dfire.common.util.ConfUtil;
import com.dfire.common.util.HierarchyProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:01 2018/3/23
 * @desc 通过操作系统创建进程Process的Job任务
 */
@Slf4j
public abstract class ProcessJob extends AbstractJob implements Job {

    protected volatile Process process;
    protected final Map<String, String> envMap;

    public ProcessJob(JobContext jobContext) {
        super(jobContext);
        envMap = new HashMap<>(System.getenv());
    }

    public abstract List<String> getCommandList();

    /**
     * @param jobType
     * @desc 针对hive做系统环境检测，保证任务运行环境可用，首先检测hadoop环境是否正常
     */
    private void buildHiveConf(String jobType) {
        File dir = new File(jobContext.getWorkDir() + File.separator + "hive_conf");
        if (!dir.exists()) {
            dir.mkdir();
        }
        Map<String, String> core = new HashMap<>();
        Map<String, String> hdfs = new HashMap<>();
        Map<String, String> mapred = new HashMap<>();
        Map<String, String> yarn = new HashMap<>();
        Map<String, String> hive = new HashMap<>();
        String corePrefix = "core-site.", hDFSPrefix = "hdfs-site.",
                mapPrefix = "mapred-site.", yarnPrefix = "yarn-site.", hivePrefix = "hive-site.";
        Integer coreLen = corePrefix.length(), hDFSLen = hDFSPrefix.length(),
                mapLen = mapPrefix.length(), yarnLen = yarnPrefix.length(), hiveLen = hivePrefix.length();
        jobContext.getProperties().getAllProperties().keySet().stream().forEach(key -> {
            if (key.startsWith(corePrefix)) {
                core.put(key.substring(coreLen), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith(hDFSPrefix)) {
                hdfs.put(key.substring(hDFSLen), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith(mapPrefix)) {
                mapred.put(key.substring(mapLen), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith(yarnPrefix)) {
                yarn.put(key.substring(yarnLen), jobContext.getProperties().getProperty(key));
            } else if (key.startsWith(hivePrefix)) {
                hive.put(key.substring(hiveLen), jobContext.getProperties().getProperty(key));
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
        //HADOOP_CONF_DIR添加2个路径，分别为 WorkDir/hadoop_conf 和 HADOOP_HOME/conf
        String HIVE_CONF_DIR = jobContext.getWorkDir() + File.separator + "hive_conf" + File.pathSeparator + ConfUtil.getHiveConfDir();
        envMap.put("HIVE_CONF_DIR", HIVE_CONF_DIR);
    }


    @Override
    public int run()  {
        int exitCode = -999;
        String jobType = jobContext.getProperties().getAllProperties().get(RunningJobKeys.JOB_RUN_TYPE);
        buildHiveConf(jobType);
        jobContext.getProperties().getAllProperties().keySet().stream()
                .filter(key -> jobContext.getProperties().getProperty(key) != null && (key.startsWith("instance.") || key.startsWith("secret.")))
                .forEach(k -> envMap.put(k, jobContext.getProperties().getProperty(k)));
        envMap.put("instance.workDir", jobContext.getWorkDir());
        log.info("获取命令");

        List<String> commands = getCommandList();
        ExecutorService executor = Executors.newCachedThreadPool();


        commands.stream().forEach(command -> {
            ProcessBuilder builder = new ProcessBuilder(partitionCommandLine(command));
            builder.directory(new File(jobContext.getWorkDir()));
            builder.environment().putAll(envMap);
            try {
                process = builder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String threadName = null;
            if (jobContext.getHeraJobHistory() != null && jobContext.getHeraJobHistory().getJobId() != null) {
                threadName = "jobId=" + jobContext.getHeraJobHistory().getJobId();
            } else if (jobContext.getDebugHistory() != null && jobContext.getDebugHistory().getId() != null) {
                threadName = "debugId=" + jobContext.getDebugHistory().getId();
            } else {
                threadName = "not-normal-job";
            }
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            executor.execute(new StreamThread(inputStream, threadName));
            executor.execute(new StreamThread(errorStream, threadName));
        });
        exitCode = -999;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            log(e);
        } finally {
            process = null;
            executor.shutdown();
        }
        if (exitCode != 0) {
            return exitCode;
        }

        return exitCode;
    }


    /**
     * @param command
     * @return
     * @desc 对hera中的操作系统命令进行拆分成字符串数组，方便给ProcessBuilder传命令参数，
     * 如："free -m | grep buffers/cache"，成为：{“free”，“-m”，“|”，“grep”，“buffers/cache”}
     */
    public static String[] partitionCommandLine(String command) {
        List<String> commands = new ArrayList<>();
        StringBuilder builder = new StringBuilder(command.length());
        int index = 0;
        boolean isApostrophe = false;
        boolean isQuote = false;
        while (index < command.length()) {
            char c = command.charAt(index);
            switch (c) {
                case ' ':
                    if (!isQuote && !isApostrophe) {
                        String arg = builder.toString();
                        builder = new StringBuilder(command.length() - index);
                        if (arg.length() > 0) {
                            commands.add(arg);
                        }
                    } else {
                        builder.append(c);
                    }
                    break;
                case '\'':
                    if (!isQuote) {
                        isApostrophe = !isApostrophe;
                    } else {
                        builder.append(c);
                    }
                    break;
                case '"':
                    if (!isApostrophe) {
                        isQuote = !isQuote;
                    } else {
                        builder.append(c);
                    }
                    break;
                default:
                    builder.append(c);
            }
            index++;
        }
        if (builder.length() > 0) {
            String arg = builder.toString();
            commands.add(arg);
        }
        log.info("组装后的命令为：{}",JSONObject.toJSONString(commands));
        return commands.toArray(new String[commands.size()]);
    }

    @Override
    public void cancel() {
        try {
            new CancelHadoopJob(jobContext).run();
        } catch (Exception e1) {
            log(e1);
        }
        //强制kill 进程
        if (process != null) {
            log("WARN Attempting to kill the process ");
            try {
                process.destroy();
                int pid = getProcessId();
                String st = "sudo sh -c \"cd; pstree " + pid + " -p | grep -o '([0-9]*)' | awk -F'[()]' '{print \\$2}' | xargs kill -9\"";
                String[] commands = {"sudo", "sh", "-c", st};
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                try {
                    process = processBuilder.start();
                    log("kill process tree success");
                } catch (Exception e) {
                    log(e);
                }
            } catch (Exception e) {
                log(e);
            } finally {
                process = null;
            }
        }
    }

    private int getProcessId() {
        int processId = 0;
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            processId = f.getInt(process);
        } catch (Throwable e) {
        }
        return processId;
    }

    @Override
    protected String getProperty(String key, String defaultValue) {
        String value = jobContext.getProperties().getProperty(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public HierarchyProperties getProperties() {
        return jobContext.getProperties();
    }

    @Override
    public JobContext getJobContext() {
        return jobContext;
    }

    /**
     * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
     * @time: Created in 11:01 2018/3/26
     * @desc job输出流日志接收线程
     */
    private class StreamThread extends Thread {
        private InputStream inputStream;
        private String threadName;
        public StreamThread(InputStream inputStream, String threadName) {
            this.inputStream = inputStream;
            this.threadName = threadName;
        }
        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsole(line);
                }
            } catch (Exception e) {
                log(e);
                log(threadName + ": 接收日志出错，退出日志接收");
            }
        }
    }
}
