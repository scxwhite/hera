package com.dfire.core.quartz;

import com.dfire.common.constants.Constants;
import com.dfire.logs.HeraLog;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 1:19 2018/1/14
 * @desc quartz调度器初始化
 */
@Configuration
@Service("quartzSchedulerService")
public class QuartzSchedulerService {

    private Scheduler scheduler;

    /**
     * 设置quartz配置: @Constructor 先于  @PostConstruct执行
     *
     * @throws IOException 2018年1月15日下午2:39:05
     */
    @Constructor
    public Properties setQuartzProperties() throws IOException {
        HeraLog.info("start init quartz properties");
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceName", "heraQuartzScheduler");
        prop.put("org.quartz.scheduler.rmi.export", "false");
        prop.put("org.quartz.scheduler.rmi.proxy", "false");
        prop.put("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "40");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        prop.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
        prop.put("org.quartz.jobStore.misfireThreshold", "60000");
        prop.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        return prop;
    }

    public void start() {
        HeraLog.info("start init quartz schedule");
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory(setQuartzProperties());
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            HeraLog.info("start init quartz scheduler");
        } catch (SchedulerException | IOException e) {
            e.printStackTrace();
            HeraLog.error("failed init quartz scheduler");
        }
    }


    public void shutdown() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                HeraLog.info("worker shutdown quartz service");
            } catch (SchedulerException e) {
                e.printStackTrace();
                HeraLog.error("failed shutdown quartz scheduler");
            }
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void deleteJob(String actionId) {
        try {
            JobKey jobKey = new JobKey(actionId, Constants.HERA_GROUP);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if(jobDetail != null) {
                scheduler.deleteJob(jobKey);
            }
            HeraLog.warn("remove action {} from quartz", actionId);
        } catch (SchedulerException e) {
            HeraLog.error("remove quartz schedule error : " + actionId);
        }

    }


}
