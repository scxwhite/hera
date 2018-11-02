package com.dfire.core.netty.master;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.common.service.*;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.quartz.QuartzSchedulerService;
import com.dfire.core.queue.JobElement;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:10 2018/1/12
 * @desc hera调度器执行上下文
 */

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MasterContext {

    private Master master;

    private Map<Channel, MasterWorkHolder> workMap = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;


    private Dispatcher dispatcher;
    private Map<Integer, HeraHostGroupVo> hostGroupCache;
    private Queue<JobElement> scheduleQueue = new PriorityBlockingQueue<>(10000, Comparator.comparing(JobElement::getPriorityLevel));
    private Queue<JobElement> debugQueue = new ArrayBlockingQueue<>(1000);
    private Queue<JobElement> manualQueue = new ArrayBlockingQueue<>(1000);

    private MasterHandler handler;
    private MasterServer masterServer;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * todo 参数可配置
     *
     */
    private Timer masterTimer = null;

    public MasterContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        dispatcher = new Dispatcher();
        handler = new MasterHandler(this);
        masterServer = new MasterServer(handler);
        masterServer.start(HeraGlobalEnvironment.getConnectPort());
        master = new Master(this);
        masterTimer = new HashedWheelTimer(Executors.defaultThreadFactory(), 1, TimeUnit.SECONDS);
        this.getQuartzSchedulerService().start();
        log.info("end init master content success ");
    }

    public void destroy() {
        threadPool.shutdown();
        masterTimer.stop();
        if (masterServer != null) {
            masterServer.shutdown();
        }
        if (this.getQuartzSchedulerService() != null) {
            try {
                this.getQuartzSchedulerService().shutdown();
                log.info("quartz schedule shutdown success");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("quartz schedule shutdown error");
            }
        }
        log.info("destroy master context success");
    }

    public synchronized Map<Integer, HeraHostGroupVo> getHostGroupCache() {
        return hostGroupCache;
    }

    public HeraHostGroupService getHeraHostGroupService() {
        return (HeraHostGroupService) applicationContext.getBean("heraHostGroupService");
    }

    public HeraFileService getHeraFileService() {
        return (HeraFileService) applicationContext.getBean("heraFileService");
    }

    public QuartzSchedulerService getQuartzSchedulerService() {
        return (QuartzSchedulerService) applicationContext.getBean("quartzSchedulerService");
    }

    public HeraGroupService getHeraGroupService() {
        return (HeraGroupService) applicationContext.getBean("heraGroupService");
    }

    public HeraJobHistoryService getHeraJobHistoryService() {
        return (HeraJobHistoryService) applicationContext.getBean("heraJobHistoryService");
    }

    public HeraUserService getHeraUserService() {
        return (HeraUserService) applicationContext.getBean("heraUserService");
    }

    public HeraJobMonitorService getHeraMonitorService() {
        return (HeraJobMonitorService) applicationContext.getBean("heraJobMonitorServiceImpl");
    }

    public HeraJobService getHeraJobService() {
        return (HeraJobService) applicationContext.getBean("heraJobService");
    }

    public HeraDebugHistoryService getHeraDebugHistoryService() {
        return (HeraDebugHistoryService) applicationContext.getBean("heraDebugHistoryService");
    }

    public HeraJobActionService getHeraJobActionService() {
        return (HeraJobActionService) applicationContext.getBean("heraJobActionService");
    }
    public EmailService getEmailService() {
        return (EmailService) applicationContext.getBean("emailServiceImpl");
    }

    public synchronized void refreshHostGroupCache() {
        try {
            hostGroupCache = getHeraHostGroupService().getAllHostGroupInfo();
        } catch (Exception e) {
            log.info("refresh host group error");
        }
    }


}
