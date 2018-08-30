package com.dfire.core.event.handler;

import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.model.JobGroupCache;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.*;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.DateUtil;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.*;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.job.CancelHadoopJob;
import com.dfire.core.quartz.HeraQuartzJob;
import com.dfire.core.job.JobContext;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:24 2018/4/19
 * @desc 每种任务执行状态都对应相应事件，job执行生命周期事件执行逻辑
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
    private EmailService emailService;
    private Master master;
    private MasterContext masterContext;
    private HeraUserService heraUserService;
    private HeraJobMonitorService heraJobMonitorService;

    public JobHandler(String actionId, Master master, MasterContext masterContext) {
        this.actionId = actionId;
        this.jobHistoryService = masterContext.getHeraJobHistoryService();
        this.heraGroupService = masterContext.getHeraGroupService();
        this.heraJobActionService = masterContext.getHeraJobActionService();
        this.heraUserService = masterContext.getHeraUserService();
        this.emailService = masterContext.getEmailService();
        this.heraJobMonitorService = masterContext.getHeraMonitorService();
        this.cache = JobGroupCache.builder().actionId(actionId).heraJobActionService(heraJobActionService).build();
        this.master = master;
        this.masterContext = masterContext;
        registerEventType(Events.Initialize);
    }

    @Override
    public boolean canHandle(ApplicationEvent event) {
        if (super.canHandle(event)) {
            return true;
        }
        if (event instanceof HeraJobFailedEvent || event instanceof HeraJobSuccessEvent ||
                event instanceof HeraJobLostEvent || event instanceof HeraScheduleTriggerEvent ||
                event instanceof HeraJobMaintenanceEvent) {
            return true;
        }
        return false;
    }

    /**
     * 接受到任务事件广播处理逻辑
     *
     * @param event
     */
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

    /**
     * 1.running 任务重试
     * 2.调度任务加入调度池
     */
    public void handleInitialEvent() {

        HeraAction heraAction = heraJobActionService.findById(actionId);
        if (heraAction != null) {
            //对版本表中处于running状态的任务进行重试
            if (StatusEnum.RUNNING.toString().equals(heraAction.getStatus())) {
                log.error("actionId = " + actionId + " 处于RUNNING状态，说明该job状态丢失，立即进行重试操作。。。");
                //有历史版本
                if (heraAction.getHistoryId() != null) {
                    HeraJobHistory jobHistory = jobHistoryService.findById(heraAction.getHistoryId());
                    if (jobHistory == null) {
                        return;
                    }
                    HeraJobHistoryVo heraJobHistory = BeanConvertUtils.convert(jobHistory);
                    // 搜索上一次运行的日志，从日志中提取jobId 进行kill
                    if (jobHistory.getStatus() == null || jobHistory.getStatus().equals(StatusEnum.RUNNING.toString())) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate(LogConstant.SERVER_START_JOB_LOG);
                            tmp.setHeraJobHistory(heraJobHistory);
                            new CancelHadoopJob(tmp).run();
                            master.run(heraJobHistory);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //TODO  未测试
                    } else if (heraJobHistory != null && heraJobHistory.getStatusEnum().equals(StatusEnum.FAILED) &&
                            heraJobHistory.getIllustrate().equals(LogConstant.WORK_DISCONNECT_LOG)) {
                        try {
                            JobContext tmp = JobContext.getTempJobContext(JobContext.MANUAL_RUN);
                            heraJobHistory.setIllustrate(LogConstant.WORK_DISCONNECT_LOG);
                            tmp.setHeraJobHistory(heraJobHistory);
                            new CancelHadoopJob(tmp).run();
                            master.run(heraJobHistory);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                    HeraJobHistory heraJobHistory = HeraJobHistory.builder()
                            .jobId(heraAction.getJobId())
                            .actionId(heraAction.getId())
                            .triggerType(TriggerTypeEnum.MANUAL_RECOVER.getId())
                            .illustrate(LogConstant.SERVER_START_JOB_LOG)
                            .log(LogConstant.SERVER_START_JOB_LOG)
                            .operator(heraAction.getOwner())
                            .hostGroupId(heraAction.getHostGroupId())
                            .build();
                    masterContext.getHeraJobHistoryService().insert(heraJobHistory);
                    master.run(BeanConvertUtils.convert(heraJobHistory));
                }
            }
        }

        /**
         * 如果是定时任务，启动定时程序,独立调度任务，创建quartz调度
         *
         */
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        boolean isSchedule = heraActionVo.getAuto() && Objects.equals(heraActionVo.getScheduleType(), JobScheduleTypeEnum.Independent);
        if (isSchedule) {
            try {
                createScheduleJob(masterContext.getDispatcher(), heraActionVo);
            } catch (Exception e) {
                if (e instanceof SchedulerException) {
                    heraActionVo.setAuto(false);
                    log.error("create job quartz schedule error");
                }
                throw new RuntimeException(e);
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
        if (heraActionVo.getDependencies() == null || !heraActionVo.getDependencies().contains(jobId)) {
            return;
        }
        JobStatus jobStatus;
        synchronized (this) {
            jobStatus = heraJobActionService.findJobStatus(actionId);
            HeraJobBean heraJobBean = heraGroupService.getUpstreamJobBean(actionId);
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
            log.info("received a success dependency job with actionId = " + jobId);
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
        HeraJobHistory history = HeraJobHistory.builder().
                actionId(heraActionVo.getId()).
                illustrate(LogConstant.DEPENDENT_READY_LOG).
                jobId(heraActionVo.getJobId()).
                triggerType(TriggerTypeEnum.SCHEDULE.getId()).
                operator(heraActionVo.getOwner()).
                log(LogConstant.DEPENDENT_READY_LOG).build();
        masterContext.getHeraJobHistoryService().insert(history);
        HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
        master.run(historyVo);
        if (historyVo.getStatusEnum() == StatusEnum.FAILED) {
            HeraJobFailedEvent jobFailedEvent = new HeraJobFailedEvent(heraActionVo.getId(), triggerType, historyVo);
            masterContext.getDispatcher().forwardEvent(jobFailedEvent);
            log.info("job execute error, dispatch job failed event");
        }
    }


    private void handleFailedEvent(HeraJobFailedEvent event) {
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!heraActionVo.getAuto()) {
            return;
        }
        if (heraActionVo.getAuto() && event.getActionId().equals(actionId)) {
            try {
                HeraJobMonitor monitor = heraJobMonitorService.findByJobId(Integer.parseInt(heraActionVo.getJobId()));
                if (monitor == null) {
                    log.info("任务无监控人：{}", heraActionVo.getJobId());
                } else {

                    String ids = monitor.getUserIds();
                    String[] id = ids.split(",");

                    String[] emails = new String[id.length];
                    int index = 0;
                    for (int i = 0; i < id.length; i++) {
                        if (StringUtils.isBlank(id[i])) {
                            continue;
                        }
                        HeraUser user = heraUserService.findById(HeraUser.builder().id(Integer.parseInt(id[i])).build());
                        if (user != null && user.getEmail() != null) {
                            emails[index++] = user.getEmail();
                        }
                    }
                    emailService.sendEmail("hera任务失败了(" + HeraGlobalEnvironment.getEnv() + ")", "任务Id :" + actionId, emails);

                }

            } catch (MessagingException e) {
                e.printStackTrace();
                log.error("发送邮件失败");
            }
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


        HeraJobHistory history = HeraJobHistory.builder().
                jobId(heraActionVo.getJobId()).
                triggerType(TriggerTypeEnum.SCHEDULE.getId()).
                actionId(heraActionVo.getId()).
                operator(heraActionVo.getOwner()).
                hostGroupId(heraActionVo.getHostGroupId()).
                build();
        masterContext.getHeraJobHistoryService().insert(history);
        master.run(BeanConvertUtils.convert(history));
    }

    private void autoRecovery() {
        cache.refresh();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        //任务被删除
        if (heraActionVo == null) {
            masterContext.getDispatcher().removeJobHandler(this);
            destroy();
            return;
        }
        //自动调度关闭
        if (!heraActionVo.getAuto()) {
            destroy();
            return;
        }

        /**
         * 如果是依赖任务 原来可能是独立任务，需要尝试删除原来的定时调度
         * 如果是独立任务,则重新创建quartz调度
         *
         */
        if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Dependent) {
            destroy();
        } else if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Independent) {
            try {
                createScheduleJob(masterContext.getDispatcher(), heraActionVo);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMaintenanceEvent(HeraJobMaintenanceEvent event) {
        if (event.getType() == Events.UpdateJob && StringUtil.actionIdToJobId(actionId, event.getId())) {
            autoRecovery();
        }
        if (event.getType() == Events.UpdateActions && Objects.equals(actionId, event.getId())) {
            autoRecovery();
        }

    }

    /**
     * 漏泡重新触发调度事件
     *
     * @param event
     */
    private void handleLostEvent(HeraJobLostEvent event) {
        if (event.getType() == Events.UpdateJob && actionId.equals(event.getJobId())) {
            HeraActionVo heraActionVo = cache.getHeraActionVo();
            if (heraActionVo != null) {
                HeraAction heraAction = heraJobActionService.findById(actionId);

                if (heraAction != null && StringUtils.isBlank(heraAction.getStatus()) && heraAction.getAuto() == 1) {
                    String currentDate = DateUtil.getNowStringForAction();
                    if (Long.parseLong(actionId) < Long.parseLong(currentDate)) {
                        HeraJobHistory history = HeraJobHistory.builder()
                                .illustrate(LogConstant.LOST_JOB_LOG)
                                .actionId(heraActionVo.getId())
                                .jobId(heraActionVo.getJobId())
                                .triggerType(Integer.parseInt(TriggerTypeEnum.SCHEDULE.toString()))
                                .statisticEndTime(heraActionVo.getStatisticStartTime())
                                .operator(heraActionVo.getOwner())
                                .hostGroupId(heraAction.getHostGroupId())
                                .build();
                        masterContext.getHeraJobHistoryService().insert(history);
                        master.run(BeanConvertUtils.convert(history));
                        log.info("lost job, start schedule :{}", actionId);
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

    private void createScheduleJob(Dispatcher dispatcher, HeraActionVo heraActionVo) throws SchedulerException {
        if (!DateUtil.isNow(actionId)) {
            return;
        }
        JobKey jobKey = new JobKey(actionId, Constants.HERA_GROUP);
        if (masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey) == null) {
            JobDetail jobDetail = JobBuilder.newJob(HeraQuartzJob.class).withIdentity(jobKey).build();
            jobDetail.getJobDataMap().put("actionId", heraActionVo.getId());
            jobDetail.getJobDataMap().put("dispatcher", dispatcher);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(heraActionVo.getCronExpression());
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(actionId, Constants.HERA_GROUP).withSchedule(scheduleBuilder).build();
            masterContext.getQuartzSchedulerService().getScheduler().scheduleJob(jobDetail, trigger);
            log.info("--------------------------- 添加自动调度成功:{}--------------------------", heraActionVo.getId());
        }
    }


    @Override
    public void destroy() {
        masterContext.getQuartzSchedulerService().deleteJob(actionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobHandler)) {
            return false;
        }
        JobHandler jobHandler = (JobHandler) obj;
        return actionId.equals(jobHandler.getActionId());
    }

    @Override
    public int hashCode() {
        return actionId.hashCode();
    }


}
