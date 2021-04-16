package com.dfire.core.event.handler;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.model.JobGroupCache;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.service.HeraGroupService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.job.CancelHadoopJob;
import com.dfire.core.job.JobContext;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.core.netty.master.response.MasterCancelJob;
import com.dfire.core.quartz.HeraQuartzJob;
import com.dfire.event.*;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.ScheduleLog;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.RpcWebOperate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:24 2018/4/19
 * @desc 每种任务执行状态都对应相应事件，job执行生命周期事件执行逻辑
 */

@Builder
@AllArgsConstructor
public class JobHandler extends AbstractHandler {

    @Getter
    private final Long actionId;
    private JobGroupCache cache;
    private HeraJobHistoryService jobHistoryService;
    private HeraGroupService heraGroupService;
    private HeraJobActionService heraJobActionService;
    private Master master;
    private MasterContext masterContext;
    private HeraUserService heraUserService;

    public JobHandler(Long actionId, Master master, MasterContext masterContext) {
        this.actionId = actionId;
        this.jobHistoryService = masterContext.getHeraJobHistoryService();
        this.heraGroupService = masterContext.getHeraGroupService();
        this.heraJobActionService = masterContext.getHeraJobActionService();
        this.heraUserService = masterContext.getHeraUserService();
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
        return event instanceof HeraJobFailedEvent || event instanceof HeraJobSuccessEvent ||
                event instanceof HeraJobLostEvent || event instanceof HeraScheduleTriggerEvent ||
                event instanceof HeraJobMaintenanceEvent;
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
                ScheduleLog.warn("actionId = " + actionId + " 处于RUNNING状态，说明该job状态丢失，立即进行重试操作。。。");
                //有历史版本
                if (heraAction.getHistoryId() != null) {
                    HeraJobHistory jobHistory = jobHistoryService.findById(heraAction.getHistoryId());
                    if (jobHistory == null) {
                        return;
                    }
                    //如果已经成功，直接广播即可
                    if (StatusEnum.SUCCESS.toString().equals(jobHistory.getStatus())) {
                        heraJobActionService.updateStatus(heraAction.getId(), StatusEnum.SUCCESS.toString());
                        HeraJobHistoryVo historyVo = BeanConvertUtils.convert(jobHistory);
                        HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, historyVo.getTriggerType(), historyVo);
                        masterContext.getDispatcher().forwardEvent(successEvent);
                    } else {
                        // 搜索上一次运行的日志，从日志中提取jobId 进行kill
                        try {
                            MasterCancelJob.cancel(JobExecuteKind.ExecuteKind.ManualKind, masterContext,
                                    String.valueOf(jobHistory.getId()), 0, RpcWebOperate.WebOperate.CancelJob);
                        } catch (Exception e) {
                            ErrorLog.error("取消任务异常", e);
                        }
                        jobHistory.setStatus(StatusEnum.FAILED.toString());
                        jobHistory.setIllustrate("任务历史丢失");
                        jobHistory.setEndTime(new Date());
                        jobHistoryService.update(jobHistory);
                        startNewJob(BeanConvertUtils.transform(heraAction), LogConstant.SERVER_START_JOB_LOG, TriggerTypeEnum.SCHEDULE);
                    }
                } else {
                    startNewJob(BeanConvertUtils.transform(heraAction), LogConstant.SERVER_START_JOB_LOG, TriggerTypeEnum.SCHEDULE);
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
                    ErrorLog.error("create job quartz schedule error", e);
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
        if (event.getTriggerType() == TriggerTypeEnum.MANUAL || event.getTriggerType() == TriggerTypeEnum.AUTO_RERUN) {
            return;
        }
        Long jobId = event.getJobId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }

        if (!heraActionVo.getAuto() || jobId.equals(heraActionVo.getId())) {
            return;
        }
        if (heraActionVo.getScheduleType() == JobScheduleTypeEnum.Independent) {
            return;
        }
        //依赖的任务包含该广播的任务ID
        if (heraActionVo.getDependencies() == null || !heraActionVo.getDependencies().contains(jobId)) {
            return;
        }
        JobStatus jobStatus;

        //如果是超级恢复
        if (event.getTriggerType() == TriggerTypeEnum.SUPER_RECOVER) {
            //检测依赖任务是否已经执行成功，如果不是成功状态，则取消下游触发
            List<Long> dependencies = heraActionVo.getDependencies();
            boolean canRun = true;
            for (Long actionId : dependencies) {
                HeraAction heraAction = heraJobActionService.findById(actionId);
                if (heraAction == null || !StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                    canRun = false;
                    ScheduleLog.info(String.format("cancel %s super recovery,job %s not success ", heraActionVo.getId(), actionId));
                    break;
                }
            }
            if (canRun) {
                ScheduleLog.info("JobId:" + actionId + " trigger super recovery dependency,run!");
                startNewJob(heraActionVo, LogConstant.SUPER_RECOVER_LOG, TriggerTypeEnum.SUPER_RECOVER);
            }
        } else if (event.getTriggerType() == TriggerTypeEnum.SCHEDULE || event.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER) {
            //必须同步
            synchronized (this) {
                jobStatus = heraJobActionService.findJobStatus(actionId);
                ScheduleLog.info(actionId + "received a success dependency job with actionId = " + jobId);
                jobStatus.getReadyDependency().put(String.valueOf(jobId), String.valueOf(System.currentTimeMillis()));
                heraJobActionService.updateStatus(jobStatus);
            }
            boolean allComplete = true;
            for (Long key : heraActionVo.getDependencies()) {
                if (jobStatus.getReadyDependency().get(String.valueOf(key)) == null) {
                    allComplete = false;
                    break;
                }
            }
            if (allComplete) {
                ScheduleLog.info("JobId:" + actionId + " all dependency jobs is ready,run!");
                startNewJob(heraActionVo, LogConstant.DEPENDENT_READY_LOG, TriggerTypeEnum.SCHEDULE);
            } else {
                ScheduleLog.info(actionId + "some of dependency is not ready, waiting" + JSONObject.toJSONString(jobStatus.getReadyDependency().keySet()));
            }
        }
    }


    private void handleFailedEvent(HeraJobFailedEvent event) {
        //处理任务失败的事件

    }

    /**
     * 自动调度执行逻辑，如果没有版本，说明job被删除了，异常情况
     *
     * @param event
     */
    public void handleTriggerEvent(HeraScheduleTriggerEvent event) {
        Long actionId = event.getActionId();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        if (heraActionVo == null) {
            autoRecovery();
            return;
        }
        if (!actionId.equals(heraActionVo.getId())) {
            return;
        }
        startNewJob(heraActionVo, "自动调度", TriggerTypeEnum.SCHEDULE);
    }


    private void startNewJob(HeraActionVo heraActionVo, String illustrate, TriggerTypeEnum triggerTypeEnum) {
        HeraJob heraJob = masterContext.getHeraJobService().findById(heraActionVo.getJobId());
        if (!master.checkJobRun(heraJob)) {
            return;
        }
        HeraJobHistory history = HeraJobHistory.builder().
                actionId(heraActionVo.getId()).
                illustrate(illustrate).
                jobId(heraActionVo.getJobId()).
                triggerType(triggerTypeEnum.getId()).
                operator(heraActionVo.getOwner()).
                hostGroupId(heraActionVo.getHostGroupId()).
                batchId(heraActionVo.getBatchId()).
                bizLabel(heraJob.getBizLabel()).
                build();
        masterContext.getHeraJobHistoryService().insert(history);
        HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
        master.run(historyVo, heraJob);
    }

    private void autoRecovery() {
        cache.refresh();
        HeraActionVo heraActionVo = cache.getHeraActionVo();
        //任务被删除
        if (heraActionVo == null) {
            masterContext.getDispatcher().removeJobHandler(this);
            destroy();
            ScheduleLog.info("heraAction 为空， 删除{}", actionId);
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
                ErrorLog.error("创建调度任务异常", e);
            }
        }
    }

    private void handleMaintenanceEvent(HeraJobMaintenanceEvent event) {
        if (event.getType() == Events.UpdateJob && Objects.equals(Integer.parseInt(String.valueOf(event.getId())), ActionUtil.getJobId(actionId))) {
            autoRecovery();
        }
        if (event.getType() == Events.UpdateActions && Objects.equals(actionId, event.getId())) {
            autoRecovery();
        }

    }

    /**
     * 漏跑重新触发调度事件
     *
     * @param event
     */
    private void handleLostEvent(HeraJobLostEvent event) {
        if (event.getType() == Events.UpdateJob && actionId.equals(event.getJobId())) {
            HeraActionVo heraActionVo = cache.getHeraActionVo();
            if (heraActionVo != null) {
                HeraAction heraAction = heraJobActionService.findById(actionId);
                if (heraAction != null && StringUtils.isBlank(heraAction.getStatus()) && heraAction.getAuto() == 1) {
                    if (actionId < Long.parseLong(ActionUtil.getCurrActionVersion())) {
                        startNewJob(BeanConvertUtils.transform(heraAction), LogConstant.LOST_JOB_LOG, TriggerTypeEnum.SCHEDULE);
                        ScheduleLog.info("lost job, start schedule :{}", actionId);
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
        if (!ActionUtil.isCurrActionVersion(actionId)) {
            return;
        }
        JobKey jobKey = new JobKey(String.valueOf(actionId), Constants.HERA_GROUP);
        if (masterContext.getQuartzSchedulerService().getScheduler().getJobDetail(jobKey) == null) {
            JobDetail jobDetail = JobBuilder.newJob(HeraQuartzJob.class).withIdentity(jobKey).build();
            jobDetail.getJobDataMap().put(Constants.QUARTZ_ID, heraActionVo.getId());
            jobDetail.getJobDataMap().put(Constants.QUARTZ_DISPATCHER, dispatcher);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(heraActionVo.getCronExpression().trim());
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(String.valueOf(actionId), Constants.HERA_GROUP).withSchedule(scheduleBuilder).build();
            masterContext.getQuartzSchedulerService().getScheduler().scheduleJob(jobDetail, trigger);
            ScheduleLog.info("--------------------------- 添加自动调度成功:{}--------------------------", heraActionVo.getId());
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
