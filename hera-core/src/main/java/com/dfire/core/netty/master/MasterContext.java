package com.dfire.core.netty.master;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.common.service.*;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.quartz.QuartzSchedulerService;
import com.dfire.core.queue.JobElement;
import com.dfire.logs.HeraLog;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@Order(2)
public class MasterContext {

    @Autowired
    private Master master;

    private Map<Channel, MasterWorkHolder> workMap = new ConcurrentHashMap<>();
    @Autowired
    private ApplicationContext applicationContext;


    private Dispatcher dispatcher;
    private Map<Integer, HeraHostGroupVo> hostGroupCache;
    private Queue<JobElement> scheduleQueue = new PriorityBlockingQueue<>(10000, Comparator.comparing(JobElement::getPriorityLevel));
    private Queue<JobElement> debugQueue = new LinkedBlockingQueue<>(1000);
    private Queue<JobElement> manualQueue = new LinkedBlockingQueue<>(1000);

    private MasterHandler handler;
    private MasterServer masterServer;
    private ExecutorService threadPool;

    /**
     * todo 参数可配置
     */

    protected ScheduledThreadPoolExecutor masterSchedule;

    public void init() {
        threadPool = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("master-wait-response-thread"), new ThreadPoolExecutor.AbortPolicy());
        masterSchedule = new ScheduledThreadPoolExecutor(3, new NamedThreadFactory("master-schedule-thread", true));
        masterSchedule.setKeepAliveTime(5, TimeUnit.MINUTES);
        masterSchedule.allowCoreThreadTimeOut(true);
        this.getQuartzSchedulerService().start();
        dispatcher = new Dispatcher();
        handler = new MasterHandler(this);
        masterServer = new MasterServer(handler);
        masterServer.start(HeraGlobalEnvironment.getConnectPort());
        master.init(this);
        HeraLog.info("end init master content success ");
    }

    public void destroy() {
        threadPool.shutdown();
        masterSchedule.shutdown();
        if (masterServer != null) {
            masterServer.shutdown();
        }
        if (this.getQuartzSchedulerService() != null) {
            try {
                this.getQuartzSchedulerService().shutdown();
                HeraLog.info("quartz schedule shutdown success");
            } catch (Exception e) {
                e.printStackTrace();
                HeraLog.error("quartz schedule shutdown error");
            }
        }
        HeraLog.info("destroy master context success");
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
            HeraLog.info("refresh host group error");
        }
    }


}
