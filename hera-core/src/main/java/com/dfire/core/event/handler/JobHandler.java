package com.dfire.core.event.handler;

import com.dfire.common.constant.JobScheduleType;
import com.dfire.common.constant.RunningJobKeys;
import com.dfire.common.constant.Status;
import com.dfire.common.constant.TriggerType;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.model.JobGroupCache;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.util.DateUtil;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.event.*;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.job.CancelHadoopJob;
import com.dfire.core.job.JobContext;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:24 2018/4/19
 * @desc 任务事件处理器
 */
@Builder
@Slf4j
@AllArgsConstructor
public class JobHandler extends AbstractHandler {

    @Getter
    private final String jobId;

    private JobGroupCache cache;
    private HeraJobHistoryService jobHistoryService;
    private HeraGroupService heraGroupService;

    private Master master;
    private MasterContext masterContext;

    public JobHandler(String jobId, Master master, MasterContext masterContext) {
        this.jobId = jobId;
        this.jobHistoryService = masterContext.getHeraJobHistoryService();
        this.heraGroupService = masterContext.getHeraGroupService();
        this.cache = JobGroupCache.builder().jobId(jobId).heraGroupService(heraGroupService).build();
        this.master = master;
        this.masterContext = masterContext;
        registerEventType(Events.Initialize);
    }


    @Override
    public void handleEvent(ApplicationEvent event) {
        if(event instanceof HeraJobSuccessEvent) {
            handleSuccessEvent((HeraJobSuccessEvent) event);
        } else if (event instanceof HeraJobFailedEvent) {
            handleFailedEvent((HeraJobFailedEvent) event);
        } else if(event instanceof HeraScheduleTriggerEvent) {
            handleTriggerEvent((HeraScheduleTriggerEvent) event);
        } else if(event instanceof HeraJobMaintenanceEvent) {
            handleMaintenanceEvent((HeraJobMaintenanceEvent) event);
        } else if(event instanceof HeraJobLostEvent) {
            handleLostEvent((HeraJobLostEvent) event);
        } else if(event.getType() == Events.Initialize){
            handleInitialEvent();
        }

    }

    public void handleInitialEvent() {
        JobStatus jobStatus = heraGroupService.getJobStatus(jobId);
        if(jobStatus != null) {
            if(jobStatus.getStatus() == Status.RUNNING) {
                log.error("jobId" + jobId + "处于RUNNING状态，说明该job状态丢失，立即进行重试操作。。。");
                if(jobStatus.getHistoryId() != null) {
                    HeraJobHistory heraJobHistory = jobHistoryService.findJobHistory(jobId);
                    if(heraJobHistory != null && heraJobHistory.getStatus().equals(Status.RUNNING)) {
                        JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                        heraJobHistory.setIllustrate("启动服务器发现正在running状态，判断状态已经丢失，进行重试操作");
                        tmp.setHeraJobHistory(heraJobHistory);
                        new CancelHadoopJob(tmp).run();
                        master.run(heraJobHistory);
                        log.info("重启running job success");
                    } else if(heraJobHistory != null && heraJobHistory.getStatus().equals(Status.FAILED) &&
                            heraJobHistory.getIllustrate().equals("work断开连接，主动取消该任务")) {
                        JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                        heraJobHistory.setIllustrate("启动服务器发现worker与master断开连接，worker主动取消任务，进行重试操作");
                        tmp.setHeraJobHistory(heraJobHistory);
                        new CancelHadoopJob(tmp).run();
                        master.run(heraJobHistory);
                        log.info("重启running job success");
                    }
                } else {
                    HeraJobVo heraJobVo = heraGroupService.getUpstreamJobBean(jobId).getHeraJobVo();
                    HeraJobHistory history = HeraJobHistory.builder()
                            .id(jobId)
                            .jobId(heraJobVo.getToJobId())
                            .triggerType(TriggerType.MANUAL_RECOVER)
                            .illustrate("启动服务器发现正在running状态，判断状态已经丢失，进行重试操作")
                            .operator(heraJobVo.getOwner())
                            .hostGroupId(heraJobVo.getHostGroupId())
                            .build();
                    masterContext.getHeraJobHistoryService().addHeraJobHistory(history);
                    master.run(history);

                }
            }
        }

        HeraJobVo heraJobVo = cache.getHeraJobVo();
        if(heraJobVo.getAuto() && heraJobVo.getJobScheduleType() == JobScheduleType.Independent) {
            try {
                createScheduleJob(masterContext.getDispatcher(), heraJobVo);
                log.info("启动服务器，创建任务的quartz定时调度");
            } catch (Exception e) {
                if(e instanceof SchedulerException) {
                    heraJobVo.setAuto(false);
                    log.error("创建任务的quartz定时调度失败");
                    heraGroupService.updateJob(heraJobVo);
                }
            }

        }

    }


    private void handleSuccessEvent(HeraJobSuccessEvent event) {
        if(event.getTriggerType() == TriggerType.MANUAL) {
            return;
        }
        String jobId = event.getJobId();
        HeraJobVo heraJobVo = cache.getHeraJobVo();
        if(heraJobVo == null) {
            autoRecovery();
            return;
        }
        if(!heraJobVo.getAuto()) {
            return;
        }
        if(heraJobVo.getJobScheduleType() == JobScheduleType.Independent) {
            return;
        }
        if(!heraJobVo.getDependencies().contains(jobId)) {
            return;
        }
        JobStatus jobStatus = null;
        synchronized (this) {
            jobStatus = heraGroupService.getJobStatus(jobId);
            HeraJobBean heraJobBean = heraGroupService.getUpstreamJobBean(jobId);
            String cycle = heraJobBean.getHierarchyProperties().getProperty(RunningJobKeys.DEPENDENCY_CYCLE);
            if(StringUtils.isNotBlank(cycle)) {
                Map<String, String> dependencies = jobStatus.getReadyDependency();
                if (cycle.equals("sameday")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String now = simpleDateFormat.format(new Date());
                    for (String key : new HashSet<String>(dependencies.keySet())) {
                        String date = simpleDateFormat.format(new Date(Long.valueOf(dependencies.get(key))));
                        if( !now.equals(date)) {
                            jobStatus.getReadyDependency().remove(key);
                            log.info("去掉重复依赖任务");
                        }
                    }
                }
            }
            jobStatus.getReadyDependency().put(jobId, String.valueOf(new Date().getTime()));
            heraGroupService.updateJob(heraJobVo);//更新status
        }
        boolean allComplete = true;
        for(String key : heraJobVo.getDependencies()) {
            if(jobStatus.getReadyDependency().get(key) == null) {
                allComplete = false;
                break;
            }
        }
        if(allComplete) {
            log.info("依赖任务全部执行完毕");
            startNewJob(event.getTriggerType(), heraJobVo);
        } else {
            log.info("依赖任务为执行结束，继续waiting");
        }
    }

    private void startNewJob(TriggerType triggerType, HeraJobVo heraJobVo) {
        HeraJobHistory history = HeraJobHistory.builder()
                .jobId(heraJobVo.getId())
                .illustrate("依赖任务全部到位，开始执行")
                .toJobId(heraJobVo.getToJobId() == null ? null : heraJobVo.getToJobId())
                .triggerType(TriggerType.SCHEDULE)
                .statisticsEndTime(heraJobVo.getStatisticStartTime())
                .hostGroupId(heraJobVo.getHostGroupId())
                .operator(heraJobVo.getOwner() == null ? null : heraJobVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().addHeraJobHistory(history);
        master.run(history);
        if(history.getStatus() == Status.FAILED) {
            HeraJobFailedEvent jobFailedEvent = new HeraJobFailedEvent(heraJobVo.getId(), triggerType, history);
            masterContext.getDispatcher().forwardEvent(jobFailedEvent);
            log.info("任务失败，广播消息");
        }
    }



    private void handleFailedEvent(HeraJobFailedEvent event) {
        HeraJobVo heraJobVo = cache.getHeraJobVo();
        if(heraJobVo == null) {
            autoRecovery();
            return;
        }
        if(! heraJobVo.getAuto()) {
            return;
        }

    }

    public void handleTriggerEvent(HeraScheduleTriggerEvent event) {
        String jobId = event.getJobId();
        HeraJobVo heraJobVo = cache.getHeraJobVo();
        if(heraJobVo == null) { //说明job被删除了，异常情况
            autoRecovery();
            return;
        }
        if(!jobId.equals(heraJobVo.getId())) {
            return;
        }
        
        runJob(heraJobVo);
    }

    private void runJob(HeraJobVo heraJobVo) {
        HeraJobHistory history = HeraJobHistory.builder()
                .jobId(heraJobVo.getId())
                .toJobId(heraJobVo.getToJobId() == null ? null : heraJobVo.getToJobId())
                .triggerType(TriggerType.SCHEDULE)
                .statisticsEndTime(heraJobVo.getStatisticStartTime())
                .hostGroupId(heraJobVo.getHostGroupId())
                .operator(heraJobVo.getOwner() == null ? null : heraJobVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().addHeraJobHistory(history);
        master.run(history);
    }

    private void autoRecovery() {
        cache.refresh();
        HeraJobVo heraJobVo = cache.getHeraJobVo();
        if(heraJobVo == null) {
            masterContext.getDispatcher().removeJobhandler(this);
            destroy();
            log.info("清除删除的任务的quartz调度");
            return;
        }
        JobDetail jobDetail = null;
        JobKey jobKey = new JobKey(jobId, "hera");
        try {

            jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
            log.info("清除删除的任务的quartz调度" + jobId);
        } catch (SchedulerException e) {
            log.error(e.toString());
        }

        if(!heraJobVo.getAuto()) {
            if(jobDetail != null) {
                try {
                    jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
                    log.info("清除删除的任务的quartz调度" + jobId);
                } catch (SchedulerException e) {
                    log.error(e.toString());
                }
            }
        } else if(heraJobVo.getJobScheduleType() == JobScheduleType.Independent) {//独立任务
            try {
                if(jobDetail != null) {
                    return;
                }
                createScheduleJob(masterContext.getDispatcher(), heraJobVo);
            } catch (SchedulerException e) {
                log.error(e.toString());
            }
        }



    }

    public void handleMaintenanceEvent(HeraJobMaintenanceEvent event) {
        if(event.getType() == Events.UpdateJob && jobId.equals(event.getId())) {
            autoRecovery();
        }
        if(event.getType() == Events.UpdateActions && StringUtil.actionIdToJobId(event.getId(), jobId)) {
            autoRecovery();
        }

    }

    public void handleLostEvent(HeraJobLostEvent event) {
        if(event.getType() == Events.UpdateJob && jobId.equals(jobId)) {
            HeraJobVo heraJobVo = cache.getHeraJobVo();
            if(heraJobVo != null) {
                JobStatus jobStatus = heraGroupService.getJobStatus(jobId);
                if(jobStatus != null) {
                    String currentDate = DateUtil.getTodayStringForAction();
                    if(Long.parseLong(jobId) < Long.parseLong(currentDate)) {
                        HeraJobHistory history = HeraJobHistory.builder()
                                .illustrate("漏跑任务,自动恢复执行")
                                .jobId(heraJobVo.getId())
                                .toJobId(heraJobVo.getToJobId() == null ? null : heraJobVo.getToJobId())
                                .triggerType(TriggerType.SCHEDULE)
                                .statisticsEndTime(heraJobVo.getStatisticStartTime())
                                .hostGroupId(heraJobVo.getHostGroupId())
                                .operator(heraJobVo.getOwner() == null ? null : heraJobVo.getOwner())
                                .build();
                        masterContext.getHeraJobHistoryService().addHeraJobHistory(history);
                        master.run(history);
                        log.info("漏跑任务,自动恢复执行");
                    }
                }
            }
        }


    }

    /**
     * 创建定时任务
     *
     * @param dispatcher the scheduler
     * @param heraJob the job name
     */

    public void createScheduleJob(Dispatcher dispatcher, HeraJobVo heraJob) throws SchedulerException{

        JobDetail jobDetail = JobBuilder.newJob(HeraQuartzJob.class).withIdentity("hera").build();
        jobDetail.getJobDataMap().put("jobId", heraJob.getId());
        jobDetail.getJobDataMap().put("dispatcher", dispatcher);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(heraJob.getCronExpression());
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("hera").withSchedule(scheduleBuilder).build();
        masterContext.getQuartzSchedulerService().getScheduler().scheduleJob(jobDetail, trigger);

    }

    public class HeraQuartzJob implements Job {

        @Override
        public void execute(JobExecutionContext context)  {
            String jobId = context.getJobDetail().getJobDataMap().getString("jobId");
            Dispatcher dispatcher = (Dispatcher) context.getJobDetail().getJobDataMap().get("dispatcher");
            HeraScheduleTriggerEvent scheduledEvent = HeraScheduleTriggerEvent.builder().jobId(jobId).build();
            dispatcher.forwardEvent(scheduledEvent);
        }
    }

    @Override
    public void destroy() {
        try {
            JobKey jobKey = new JobKey(jobId, "hera");
            JobDetail jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
            if(jobDetail != null) {
                masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error(e.toString());
        }

    }
}
