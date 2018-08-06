package com.dfire.core.netty.worker;

import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.job.Job;
import com.dfire.core.job.ShellJob;
import com.dfire.core.tool.RunShell;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:30 2018/1/10
 * @desc
 */
@Data
@NoArgsConstructor
public class WorkContext {

    public static String host;
    public static Integer cpuCoreNum;
    public String serverHost;
    private Channel serverChannel;
    private Map<String, Job> running = new ConcurrentHashMap<>();
    private Map<String, Job> manualRunning = new ConcurrentHashMap<>();
    private Map<String, Job> debugRunning = new ConcurrentHashMap<>();
    private WorkHandler handler;
    private WorkClient workClient;
    private ExecutorService workThreadPool = Executors.newCachedThreadPool();
    private ApplicationContext applicationContext;
    private static final String loadStr = "cat /proc/cpuinfo |grep processor | wc -l";

    static {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (HeraGlobalEnvironment.isLinuxSystem()) {
            RunShell shell = new RunShell(loadStr);
            Integer exitCode = shell.run();
            if (exitCode == 0) {
                try {
                    cpuCoreNum = Integer.parseInt(shell.getResult());
                    System.out.println("机器核数：" + cpuCoreNum);
                } catch (IOException e) {
                    e.printStackTrace();
                    cpuCoreNum = 4;
                }
            }
        } else {
            cpuCoreNum = 4;
        }

    }

    public HeraDebugHistoryService getDebugHistoryService() {
        return (HeraDebugHistoryService) applicationContext.getBean("heraDebugHistoryService");
    }

    public HeraJobHistoryService getJobHistoryService() {
        return (HeraJobHistoryService) applicationContext.getBean("heraJobHistoryService");
    }

    public HeraGroupService getHeraGroupService() {
        return (HeraGroupService) applicationContext.getBean("heraGroupService");
    }

    public HeraJobActionService getHeraJobActionService() {
        return (HeraJobActionService) applicationContext.getBean("heraJobActionService");
    }


}
