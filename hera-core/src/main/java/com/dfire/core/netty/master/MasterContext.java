package com.dfire.core.netty.master;

import com.dfire.common.service.HeraFileService;
import com.dfire.common.service.HeraHostGroupService;
import com.dfire.common.service.HeraProfileService;
import com.dfire.common.vo.HeraHostGroupVo;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.quartz.QuartzSchedulerService;
import com.dfire.core.queue.JobElement;
import com.dfire.core.queue.JobPriorityBlockingDeque;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:10 2018/1/12
 * @desc
 */
@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MasterContext {

    @Autowired
    private HeraHostGroupService heraHostGroupService;
    @Autowired
    private HeraFileService heraFileService;
    @Autowired
    private HeraProfileService heraProfileService;
    @Autowired
    private QuartzSchedulerService quartzSchedulerService;

    private Master master;

    private Map<Channel, MasterWorkHolder> workMap = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;
    private MasterDoHeartBeat masterDoHeartBeat = new MasterDoHeartBeat();

    private Dispatcher dispatcher;
    private Map<String, HeraHostGroupVo> hostGroupCache;
    /**
     * @desc
     *      1. quartz发生任务调度的时候，任务会先进入到exceptionQueue队列，等待被扫描调度，随后进入调度队列
     *      2. 手动执行任务，manualQueue，等待被扫描调度，随后进入调度队列
     *      3. debugQueue，任务会先进入到exceptionQueue队列，等待被扫描调度，随后进入调度队列
     */
    private JobPriorityBlockingDeque scheduleQueue = new JobPriorityBlockingDeque();
    private Queue<JobElement> exceptionQueue = new LinkedBlockingQueue();
    private Queue<JobElement> debugQueue = new ArrayBlockingQueue(1000);
    private Queue<JobElement> manualQueue = new ArrayBlockingQueue(1000);

    private MasterHandler handler;
    private MasterServer masterServer;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    /**
     *     后面成可配置的
     */
    private ScheduledExecutorService schedulePool = Executors.newScheduledThreadPool(12);



    public void init(int port) {
        dispatcher = new Dispatcher();
        handler = new MasterHandler(this);
        masterServer = new MasterServer(handler);
        masterServer.start(port);
        master = new Master(this);
        log.info("end init master content success ");
    }

    public void destroy() {
        threadPool.shutdown();
        schedulePool.shutdown();
        if(masterServer != null) {
            masterServer.shutdown();
        }
        if(quartzSchedulerService != null) {
            try {
                quartzSchedulerService.shutdown();
                log.info("quartz schedule shutdown success");
            } catch (Exception e) {
                e.printStackTrace();
                log.info("quartz schedule shutdown error");
            }
        }
        log.info("destroy master context success");
    }

    public synchronized Map<String,HeraHostGroupVo> getHostGroupCache() {
        return hostGroupCache;
    }

    public synchronized void refreshHostGroupCache() {
        try {
            hostGroupCache = heraHostGroupService.getAllHostGroupInfo();
        } catch (Exception e) {
            log.info("refresh host group error");
        }

    }
}
