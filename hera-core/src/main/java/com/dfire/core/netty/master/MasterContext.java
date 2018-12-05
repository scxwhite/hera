package com.dfire.core.netty.master;

import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.common.service.*;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.quartz.QuartzSchedulerService;
import com.dfire.core.queue.JobElement;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private HeraHostGroupService heraHostGroupService;
    @Autowired
    private HeraFileService heraFileService;
    @Autowired
    private QuartzSchedulerService quartzSchedulerService;
    @Autowired
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    private HeraUserService heraUserService;
    @Autowired
    private HeraJobMonitorService heraJobMonitorService;
    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;
    @Autowired
    private HeraDebugHistoryService heraDebugHistoryService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    private EmailService emailService;

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
                0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("master-wait-response"), new ThreadPoolExecutor.AbortPolicy());
        masterSchedule = new ScheduledThreadPoolExecutor(5, new NamedThreadFactory("master-schedule", false));
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
        if (quartzSchedulerService != null) {
            try {
                quartzSchedulerService.shutdown();
                HeraLog.info("quartz schedule shutdown success");
            } catch (Exception e) {
                e.printStackTrace();
                ErrorLog.error("quartz schedule shutdown error");
            }
        }
        HeraLog.info("destroy master context success");
    }

    public synchronized Map<Integer, HeraHostGroupVo> getHostGroupCache() {
        return hostGroupCache;
    }


    public synchronized void refreshHostGroupCache() {
        try {
            hostGroupCache = getHeraHostGroupService().getAllHostGroupInfo();
        } catch (Exception e) {
            HeraLog.info("refresh host group error");
        }
    }


}
