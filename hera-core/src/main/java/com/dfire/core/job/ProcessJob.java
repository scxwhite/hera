package com.dfire.core.job;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.core.exception.HeraCaughtExceptionHandler;
import com.dfire.logs.HeraLog;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:01 2018/3/23
 * @desc 通过操作系统创建进程Process的Job任务
 */
public abstract class ProcessJob extends AbstractJob implements Job {

    protected volatile Process             process;
    protected final    Map<String, String> envMap;

    public ProcessJob(JobContext jobContext) {
        super(jobContext);
        envMap = new HashMap<>(System.getenv());
    }

    /**
     * 组装脚本执行命令
     *
     * @return
     */
    public abstract List<String> getCommandList();

    @Override
    public int run() throws Exception {
        int exitCode = -999;
        jobContext.getProperties().getAllProperties().keySet().stream()
                .filter(key -> jobContext.getProperties().getProperty(key) != null && ((key.startsWith("instance.") || key.startsWith("secret."))))
                .forEach(k -> envMap.put(k, jobContext.getProperties().getProperty(k)));
        envMap.put("instance.workDir", jobContext.getWorkDir());

        List<String> commands = getCommandList();

        for (String command : commands) {

            String[] splitCommand = partitionCommandLine(command);
            ProcessBuilder builder = new ProcessBuilder(splitCommand);
            builder.directory(new File(jobContext.getWorkDir()));
            builder.environment().clear();
            builder.environment().putAll(envMap);
            try {
                process = builder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String threadName;
            if (jobContext.getHeraJobHistory() != null && jobContext.getHeraJobHistory().getJobId() != null) {
                threadName = "actionId=" + jobContext.getHeraJobHistory().getJobId();
            } else if (jobContext.getDebugHistory() != null && jobContext.getDebugHistory().getId() != null) {
                threadName = "debugId=" + jobContext.getDebugHistory().getId();
            } else {
                threadName = "not-normal-job";
            }
            CountDownLatch latch = new CountDownLatch(2);
            Thread inputThread = new StreamThread(process.getInputStream(), threadName, latch);
            Thread outputThread = new StreamThread(process.getErrorStream(), threadName, latch);
            inputThread.setUncaughtExceptionHandler(new HeraCaughtExceptionHandler());
            outputThread.setUncaughtExceptionHandler(new HeraCaughtExceptionHandler());
            inputThread.start();
            outputThread.start();
            exitCode = -999;
            try {
                exitCode = process.waitFor();
                latch.await();
            } catch (InterruptedException e) {
                log(e);
            } finally {
                process = null;
            }
            if (exitCode != 0) {
                return exitCode;
            }
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
        HeraLog.info("组装后的命令为：{}", JSONObject.toJSONString(commands));
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
                processBuilder.environment().clear();
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
    public class StreamThread extends Thread {
        private InputStream    inputStream;
        private String         threadName;
        private CountDownLatch latch;

        public StreamThread(InputStream inputStream, String threadName, CountDownLatch latch) {
            this.inputStream = inputStream;
            this.threadName = threadName;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    logConsole(line);
                }
            } catch (Exception e) {
                log(e);
                log(threadName + ": 接收日志出错，退出日志接收");
            } finally {
                latch.countDown();
            }
        }
    }

}
