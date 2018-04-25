package com.dfire.core.netty.worker;

import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.core.job.Job;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
@Slf4j
@Builder
@Data
@Component
public class WorkContext {

    public static String host;
    public static Integer cpuCoreNum;
    public String serverHost;
    private Channel serverChannel;
    private Map<String, Job> running = new ConcurrentHashMap<String, Job>();
    private Map<String, Job> manualRunning = new ConcurrentHashMap<String, Job>();
    private Map<String, Job> debugRunning = new ConcurrentHashMap<String, Job>();
    private WorkHandler handler;
    private WorkClient workClient;
    private ExecutorService workThreadPool = Executors.newCachedThreadPool();
    private ApplicationContext applicationContext;
    @Autowired
    private HeraDebugHistoryService debugHistoryService;

    static {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        cpuCoreNum = Runtime.getRuntime().availableProcessors();
    }

    @Override
    public String toString() {
        return "WorkContext{}";
    }

    public void shutdown() {

    }
}
