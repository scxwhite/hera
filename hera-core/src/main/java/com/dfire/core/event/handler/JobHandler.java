package com.dfire.core.event.handler;

import com.dfire.common.constants.LogConstant;
import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.model.JobGroupCache;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.util.BeanConvertUtils;
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
 * @desc job执行生命周期事件处理器, 每种任务执行状态都对应相应事件
 */

@Builder
@Slf4j
@AllArgsConstructor
public class JobHandler extends AbstractHandler {

    @Getter
    private final String actionId;

    private JobGroupCache cache;
    private HeraJobHistoryService jobHistoryService;
    private HeraGroupService heraGroupService;
    private HeraJobActionService heraJobActionService;

    private Master master;
    private MasterContext masterContext;

    public JobHandler(String actionId, Master master, MasterContext masterContext) {
        this.actionId = actionId;
        this.jobHistoryService = masterContext.getHeraJobHistoryService();
        this.heraGroupService = masterContext.getHeraGroupService();
        this.heraJobActionService = masterContext.getHeraJobActionService();
        this.cache = JobGroupCache.builder().actionId(actionId).heraJobActionService(heraJobActionService).build();
        this.master = master;
        this.masterContext = masterContext;
        registerEventType(Events.Initialize);
    }


    @Override
    public void handleEvent(ApplicationEvent event) {
        if (event instanceof HeraJobSuccessEvent) {
            handleSuccessEvent((HeraJobSuccessEvent) event);
        } else if (event instanceof HeraJobFailedEvent) {
            handleFailedEvent();
        } else if (event instanceof HeraScheduleTriggerEvent) {
            handleTriggerEvent((HeraScheduleTriggerEvent) event);
        } else if (event instanceof HeraJobMaintenanceEvent) {
            handleMaintenanceEvent((HeraJobMaintenanceEvent) event);
        } else if (event instanceof HeraJobLostEvent) {
            handleLostEvent((HeraJobLostEvent) event);
        } else if (event.getType() == Events.Initialize) {
            handleInitialEvent();
        }

    }

    public void handleInitialEvent() {
        JobStatus jobStatus = heraJobActionService.findJobStatus(actionId);

        if (jobStatus != null) {
            if (jobStatus.getStatus() == StatusEnum.RUNNING) {
                log.error("actionId = " + actionId + " 处于RUNNING状态，说明该job状态丢失，立即进行重试操作。。。");
                if (jobStatus.getHistoryId() != null) {
                    HeraJobHistory jobHistory = jobHistoryService.findById(actionId);
                    HeraJobHistoryVo heraJobHistory = BeanConvertUtils.convert(jobHistory);

                    if (heraJobHistory != null && heraJobHistory.getStatusEnum().equals(StatusEnum.RUNNING)) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate(LogConstant.SERVER_START_JOB_LOG);
                            tmp.setHeraJobHistory(heraJobHistory);
                            new CancelHadoopJob(tmp).run();
                            master.run(heraJobHistory);
                        } catch (Exception e) {
                        }

                    } else if (heraJobHistory != null && heraJobHistory.getStatusEnum().equals(StatusEnum.FAILED) &&
                            heraJobHistory.getIllustrate().equals(LogConstant.WORK_DISCONNECT_LOG)) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate(LogConstant.WORK_DISCONNECT_LOG);
                            tmp.setHeraJobHistory(heraJobHistory);
                            new CancelHadoopJob(tmp).run();
                            master.run(heraJobHistory);
                        } catch (Exception e) {
                        }
                    }
                } else {
                    HeraJobVo heraJobVo = heraGroupService.getUpstreamJobBean(actionId).getHeraJobVo();
                    HeraJobHistoryVo history = HeraJobHistoryVo.builder()
                            .id(actionId)
                            .jobId(heraJobVo.getId())
                            .triggerType(TriggerTypeEnum.MANUAL_RECOVER)
                            .illustrate(LogConstant.SERVER_START_JOB_LOG)
                            .operator(heraJobVo.getOwner())
                            .hostGroupId(heraJobVo.getHostGroupId())
                            .build();
                    masterContext.getHeraJobHistoryService().insert(BeanConvertUtils.convert(history));
                    master.run(history);
                }
            }
        }

        /**
         * 如果是定时任务，启动定时程序,独立调度任务，创建quartz调度
         *
         */
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo.getAuto()
                && (heraActionVo.getScheduleType().getType().equals(JobScheduleTypeEnum.Independent.getType()))) {
            try {
                createScheduleJob(masterContext.getDispatcher(), heraActionVo);
                log.info("start server, create job quartz schedule");
            } catch (Exception e) {
                if (e instanceof SchedulerException) {
                    heraActionVo.setAuto(false);
                    log.error("create job quartz schedule error");
                    heraJobActionService.updateStatus(jobStatus);
                }
            }
        }
    }


    /**
     * 收到广播的任务成功事件的处理流程，每次自动调度任务成功执行，会进行一次全局的SuccessEvent广播，使得依赖任务可以更新readyDependent
     *
     * @param event
     */
    private void handleSuccessEvent(HeraJobSuccessEvent event) {
        if (event.getTriggerType() == TriggerTypeEnum.MANUAL) {
            return;
        }
        String jobId = event.getJobId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!heraActionVo.getAuto()) {
            return;
        }
        if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Independent) {
            return;
        }
        if (!heraActionVo.getDependencies().contains(jobId)) {
            return;
        }
        JobStatus jobStatus;
        synchronized (this) {
            jobStatus = heraJobActionService.findJobStatus(jobId);
            HeraJobBean heraJobBean = heraGroupService.getUpstreamJobBean(jobId);
            String cycle = heraJobBean.getHierarchyProperties().getProperty(RunningJobKeyConstant.DEPENDENCY_CYCLE);
            if (StringUtils.isNotBlank(cycle)) {
                Map<String, String> dependencies = jobStatus.getReadyDependency();
                if (cycle.equals(RunningJobKeyConstant.DEPENDENCY_CYCLE_VALUE)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String now = simpleDateFormat.format(new Date());
                    for (String key : new HashSet<>(dependencies.keySet())) {
                        String date = simpleDateFormat.format(new Date(Long.valueOf(dependencies.get(key))));
                        if (!now.equals(date)) {
                            jobStatus.getReadyDependency().remove(key);
                            log.info("remove overwrite dependency");
                        }
                    }
                }
            }
            log.info("received a success dependency job with jobId = " + jobId);
            jobStatus.getReadyDependency().put(jobId, String.valueOf(System.currentTimeMillis()));
            heraJobActionService.updateStatus(jobStatus);
        }
        boolean allComplete = true;
        for (String key : heraActionVo.getDependencies()) {
            if (jobStatus.getReadyDependency().get(key) == null) {
                allComplete = false;
                break;
            }
        }
        if (allComplete) {
            log.info("JobId:" + jobId + " all dependency jobs is ready,run!");
            startNewJob(event.getTriggerType(), heraActionVo);
        } else {
            log.info("some of dependency is not ready, waiting");
        }
    }

    private void startNewJob(TriggerTypeEnum triggerType, HeraActionVo heraActionVo) {
        HeraJobHistoryVo history = HeraJobHistoryVo.builder()
                .actionId(heraActionVo.getId())
                .illustrate(LogConstant.DEPENDENT_READY_LOG)
                .jobId(heraActionVo.getJobId() == null ? null : heraActionVo.getJobId())
                .triggerType(TriggerTypeEnum.SCHEDULE)
                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                .hostGroupId(heraActionVo.getHostGroupId())
                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().insert(BeanConvertUtils.convert(history));
        master.run(history);
        if (history.getStatusEnum() == StatusEnum.FAILED) {
            HeraJobFailedEvent jobFailedEvent = new HeraJobFailedEvent(heraActionVo.getId(), triggerType, history);
            masterContext.getDispatcher().forwardEvent(jobFailedEvent);
            log.info("job execute error, dispatch job failed event");
        }
    }


    private void handleFailedEvent() {
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!heraActionVo.getAuto()) {
            return;
        }

    }

    /**
     * 自动调度执行逻辑，如果没有版本，说明job被删除了，异常情况
     *
     * @param event
     */
    public void handleTriggerEvent(HeraScheduleTriggerEvent event) {
        String jobId = event.getJobId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!jobId.equals(heraActionVo.getId())) {
            return;
        }
        runJob(heraActionVo);
    }

    private void runJob(HeraActionVo heraActionVo) {
        HeraJobHistoryVo history = HeraJobHistoryVo.builder()
                .jobId(heraActionVo.getId())
                .triggerType(TriggerTypeEnum.SCHEDULE)
                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                .hostGroupId(heraActionVo.getHostGroupId())
                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().insert(BeanConvertUtils.convert(history));
        master.run(history);
    }

    private void autoRecovery() {
        cache.refresh();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            masterContext.getDispatcher().removeJobHandler(this);
            destroy();
            log.info("remove quartz schedule, actionId = ", heraActionVo.getId());
            return;
        }
        JobDetail jobDetail = null;
        JobKey jobKey = new JobKey(actionId, "hera");
        try {

            jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
        } catch (SchedulerException e) {
            log.error(e.toString());
        }

        if (!heraActionVo.getAuto()) {
            if (jobDetail != null) {
                try {
                    masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
                    log.info("remove close job quartz schedule, actionId = ", heraActionVo.getId());
                } catch (SchedulerException e) {
                    log.error(e.toString());
                }
            }
            return;
        }

        /**
         * 如果是依赖任务 说明原来是独立任务，现在变成依赖任务，需要删除原来的定时调度
         * 如果是独立任务,则重新创建quartz调度
         *
         */
        if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Dependent) {
            if (jobDetail != null) {
                try {
                    masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
                    log.info("clear dependent job quartz schedule, actionId = ", actionId);
                } catch (SchedulerException e) {
                    log.error(e.toString());
                }
            }
        } else if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Independent) {
            try {
                if (jobDetail != null) {
                    return;
                }
                createScheduleJob(masterContext.getDispatcher(), heraActionVo);
            } catch (SchedulerException e) {
                log.error(e.toString());
            }
        }


    }

    public void handleMaintenanceEvent(HeraJobMaintenanceEvent event) {
        if (event.getType() == Events.UpdateJob && actionId.equals(event.getId())) {
            autoRecovery();
        }
        if (event.getType() == Events.UpdateActions && StringUtil.actionIdToJobId(event.getId(), actionId)) {
            autoRecovery();
        }

    }

    public void handleLostEvent(HeraJobLostEvent event) {
        if (event.getType() == Events.UpdateJob && actionId.equals(actionId)) {
            HeraActionVo heraActionVo = cache.getHeraActionVo();
            if (heraActionVo != null) {
                JobStatus jobStatus = heraJobActionService.findJobStatus(actionId);

                if (jobStatus != null) {
                    String currentDate = DateUtil.getTodayStringForAction();
                    if (Long.parseLong(actionId) < Long.parseLong(currentDate)) {
                        HeraJobHistoryVo history = HeraJobHistoryVo.builder()
                                .illustrate(LogConstant.LOST_JOB_LOG)
                                .jobId(heraActionVo.getId())
                                .triggerType(TriggerTypeEnum.SCHEDULE)
                                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                                .hostGroupId(heraActionVo.getHostGroupId())
                                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                                .build();
                        masterContext.getHeraJobHistoryService().insert(BeanConvertUtils.convert(history));
                        master.run(history);
                        log.info("lost job, start schedule");
                    }
                }
            }
        }
    }

    /**
     * 创建定时任务
     *
     * @param dispatcher   the scheduler
     * @param heraActionVo the job name
     */

    public void createScheduleJob(Dispatcher dispatcher, HeraActionVo heraActionVo) throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(HeraQuartzJob.class).withIdentity("hera").build();
        jobDetail.getJobDataMap().put("jobId", heraActionVo.getId());
        jobDetail.getJobDataMap().put("dispatcher", dispatcher);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(heraActionVo.getCronExpression());
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("hera").withSchedule(scheduleBuilder).build();
        masterContext.getQuartzSchedulerService().getScheduler().scheduleJob(jobDetail, trigger);

    }

    public class HeraQuartzJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            String jobId = context.getJobDetail().getJobDataMap().getString("jobId");
            Dispatcher dispatcher = (Dispatcher) context.getJobDetail().getJobDataMap().get("dispatcher");
            HeraScheduleTriggerEvent scheduledEvent = HeraScheduleTriggerEvent.builder().jobId(jobId).build();
            dispatcher.forwardEvent(scheduledEvent);
        }
    }

    @Override
    public void destroy() {
        try {
            JobKey jobKey = new JobKey(actionId, "hera");
            JobDetail jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
            if (jobDetail != null) {
                masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            log.error(e.toString());
        }

    }
}
