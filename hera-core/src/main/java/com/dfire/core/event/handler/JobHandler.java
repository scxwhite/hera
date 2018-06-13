package com.dfire.core.event.handler;

import com.dfire.common.constants.RunningJobKeys;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.model.JobGroupCache;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.enums.JobScheduleType;
import com.dfire.common.enums.Status;
import com.dfire.common.enums.TriggerType;
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
    private final String actionId; //版本号id

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
            handleFailedEvent((HeraJobFailedEvent) event);
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
            if (jobStatus.getStatus() == Status.RUNNING) {
                log.error("jobId" + actionId + "处于RUNNING状态，说明该job状态丢失，立即进行重试操作。。。");
                if (jobStatus.getHistoryId() != null) {
                    HeraJobHistoryVo heraJobHistory = jobHistoryService.findJobHistory(actionId);
                    if (heraJobHistory != null && heraJobHistory.getStatus().equals(Status.RUNNING)) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate("启动服务器发现正在running状态，判断状态已经丢失，进行重试操作");
                            tmp.setHeraJobHistory(heraJobHistory);
                            new CancelHadoopJob(tmp).run();
                            master.run(heraJobHistory);
                        } catch (Exception e) {
                        }

                    } else if (heraJobHistory != null && heraJobHistory.getStatus().equals(Status.FAILED) &&
                            heraJobHistory.getIllustrate().equals("work断开连接，主动取消该任务")) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate("启动服务器发现worker与master断开连接，worker主动取消任务，进行重试操作");
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
                            .triggerType(TriggerType.MANUAL_RECOVER)
                            .illustrate("启动服务器发现正在running状态，判断状态已经丢失，进行重试操作")
                            .operator(heraJobVo.getOwner())
                            .hostGroupId(heraJobVo.getHostGroupId())
                            .build();
                    masterContext.getHeraJobHistoryService().addHeraJobHistory(BeanConvertUtils.convert(history));
                    master.run(history);
                }
            }
        }

        // 如果是定时任务，启动定时程序
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo.getAuto().equals("1")
                && (heraActionVo.getScheduleType().getType() == JobScheduleType.Independent.getType())) { //独立调度任务，创建quartz调度
            try {
                createScheduleJob(masterContext.getDispatcher(), heraActionVo);
                log.info("启动服务器，创建任务的quartz定时调度");
            } catch (Exception e) {
                if (e instanceof SchedulerException) {
                    heraActionVo.setAuto(0);
                    log.error("创建任务的quartz定时调度失败");
                    heraJobActionService.updateStatus(jobStatus);
                }
            }
        }
    }


    /**
     * 收到执行任务成功的事件的处理流程
     *
     * @param event
     */
    private void handleSuccessEvent(HeraJobSuccessEvent event) {
        if (event.getTriggerType() == TriggerType.MANUAL) {
            return;
        }
        String jobId = event.getJobId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!heraActionVo.getAuto().equals("0")) {
            return;
        }
        if (heraActionVo.getScheduleType() == JobScheduleType.Independent) {
            return;
        }
        if (!heraActionVo.getDependencies().contains(jobId)) {
            return;
        }
        JobStatus jobStatus = null;
        synchronized (this) {
            jobStatus = heraJobActionService.findJobStatus(jobId);
            HeraJobBean heraJobBean = heraGroupService.getUpstreamJobBean(jobId);
            String cycle = heraJobBean.getHierarchyProperties().getProperty(RunningJobKeys.DEPENDENCY_CYCLE);
            if (StringUtils.isNotBlank(cycle)) {
                Map<String, String> dependencies = jobStatus.getReadyDependency();
                if (cycle.equals("sameday")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String now = simpleDateFormat.format(new Date());
                    for (String key : new HashSet<String>(dependencies.keySet())) {
                        String date = simpleDateFormat.format(new Date(Long.valueOf(dependencies.get(key))));
                        if (!now.equals(date)) {
                            jobStatus.getReadyDependency().remove(key);
                            log.info("去掉重复依赖任务");
                        }
                    }
                }
            }
            log.info("received a success dependency job with jobId:" + jobId);
            jobStatus.getReadyDependency().put(jobId, String.valueOf(new Date().getTime()));
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
            log.info("依赖任务全部执行完毕");
            startNewJob(event.getTriggerType(), heraActionVo);
        } else {
            log.info("依赖任务为执行结束，继续waiting");
        }
    }

    private void startNewJob(TriggerType triggerType, HeraActionVo heraActionVo) {
        HeraJobHistoryVo history = HeraJobHistoryVo.builder()
                .actionId(heraActionVo.getId())
                .illustrate("依赖任务全部到位，开始执行")
                .jobId(heraActionVo.getJobId() == null ? null : heraActionVo.getJobId())
                .triggerType(TriggerType.SCHEDULE)
                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                .hostGroupId(heraActionVo.getHostGroupId())
                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().addHeraJobHistory(BeanConvertUtils.convert(history));
        master.run(history);
        if (history.getStatus() == Status.FAILED) {
            HeraJobFailedEvent jobFailedEvent = new HeraJobFailedEvent(heraActionVo.getId(), triggerType, history);
            masterContext.getDispatcher().forwardEvent(jobFailedEvent);
            log.info("任务失败，广播消息");
        }
    }


    private void handleFailedEvent(HeraJobFailedEvent event) {
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (heraActionVo.getAuto().equals("0")) {
            return;
        }

    }

    public void handleTriggerEvent(HeraScheduleTriggerEvent event) {
        String jobId = event.getJobId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) { //说明job被删除了，异常情况
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
                .triggerType(TriggerType.SCHEDULE)
                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                .hostGroupId(heraActionVo.getHostGroupId())
                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                .build();
        masterContext.getHeraJobHistoryService().addHeraJobHistory(BeanConvertUtils.convert(history));
        master.run(history);
    }

    private void autoRecovery() {
        cache.refresh();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            masterContext.getDispatcher().removeJobHandler(this);
            destroy();
            log.info("清除删除的任务的quartz调度");
            return;
        }
        JobDetail jobDetail = null;
        JobKey jobKey = new JobKey(actionId, "hera");
        try {

            jobDetail = masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey);
            log.info("清除删除的任务的quartz调度" + actionId);
        } catch (SchedulerException e) {
            log.error(e.toString());
        }

        // 关闭任务取消自动调度
        if (!heraActionVo.getAuto().equals("1")) {
            if (jobDetail != null) {
                try {
                    masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
                    log.info("清除关闭了自动调度的任务的quartz调度" + actionId);
                } catch (SchedulerException e) {
                    log.error(e.toString());
                }
            }
            return;
        }

        // 如果是依赖任务 说明原来是独立任务，现在变成依赖任务，需要删除原来的定时调度
        if (heraActionVo.getScheduleType() == JobScheduleType.Dependent) {
            if (jobDetail != null) {
                try {
                    masterContext.getQuartzSchedulerService().getScheduler().deleteJob(jobKey);
                    log.info("清除修改为依赖调度的任务的quartz调度" + actionId);
                } catch (SchedulerException e) {
                    log.error(e.toString());
                }
            }
        } else if (heraActionVo.getScheduleType() == JobScheduleType.Independent) {//如果是独立任务
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
                                .illustrate("漏跑任务,自动恢复执行")
                                .jobId(heraActionVo.getId())
                                .triggerType(TriggerType.SCHEDULE)
                                .statisticsEndTime(heraActionVo.getStatisticStartTime())
                                .hostGroupId(heraActionVo.getHostGroupId())
                                .operator(heraActionVo.getOwner() == null ? null : heraActionVo.getOwner())
                                .build();
                        masterContext.getHeraJobHistoryService().addHeraJobHistory(BeanConvertUtils.convert(history));
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
     * @param heraJob    the job name
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
