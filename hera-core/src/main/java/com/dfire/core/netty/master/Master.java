package com.dfire.core.netty.master;


import com.dfire.common.constants.LogConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.model.HeraGroupBean;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraHostGroupVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.DateUtil;
import com.dfire.common.util.HeraDateTool;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.HeraException;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.*;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.message.Protocol;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.message.Protocol.Response;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import com.dfire.core.util.CronParse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc hera核心任务调度器
 */
@Slf4j
public class Master {

    private MasterContext masterContext;
    private Map<Long, HeraAction> heraActionMap;
    private ExecutorService executeJobPool;

    public Master(final MasterContext masterContext) {

        this.masterContext = masterContext;
        ThreadFactory executeJobThreadFactory = new ThreadFactoryBuilder().setNameFormat("exe-job-pool-%d").build();
        executeJobPool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<>(1024), executeJobThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        HeraGroupBean globalGroup = masterContext.getHeraGroupService().getGlobalGroup();
        String exeEnvironment = "pre";
        if (HeraGlobalEnvironment.env.equalsIgnoreCase(exeEnvironment)) {
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }

        masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));
        Map<String, HeraJobBean> allJobBeans = globalGroup.getAllSubJobBeans();

        for (String id : allJobBeans.keySet()) {
            masterContext.getDispatcher().addJobHandler(new JobHandler(id, this, masterContext));
        }

        masterContext.getDispatcher().forwardEvent(Events.Initialize);
        masterContext.setMaster(this);
        masterContext.refreshHostGroupCache();
        log.info("refresh hostGroup cache");

        /**
         * 漏泡检测，清理schedule线程，1小时调度一次,超过15分钟，job开始检测漏泡
         *
         */
        TimerTask clearScheduleTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                log.info("refresh host group success,start clear schedule");
                masterContext.refreshHostGroupCache();
                try {
                    String currDate = DateUtil.getTodayStringForAction();
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, +1);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd0000000000");
                    String nextDay = simpleDateFormat.format(calendar.getTime());

                    Dispatcher dispatcher = masterContext.getDispatcher();
                    if (dispatcher != null) {
                        Map<Long, HeraAction> actionMapNew = heraActionMap;
                        if (actionMapNew != null && actionMapNew.size() > 0) {
                            List<Long> actionIdList = new ArrayList<>();
                            for (Long actionId : actionMapNew.keySet()) {
                                Long tmp = Long.parseLong(currDate) - 15000000;
                                if (actionId < tmp) {
                                    int retryCount = 0;
                                    rollBackLostJob(actionId, actionMapNew, retryCount, actionIdList);

                                }
                            }
                            log.info("roll back action count:" + actionMapNew.size());

                            List<AbstractHandler> handlers = dispatcher.getJobHandlers();
                            if (handlers != null && handlers.size() > 0) {
                                handlers.forEach(handler -> {
                                    JobHandler jobHandler = (JobHandler) handler;
                                    String jobId = jobHandler.getActionId();
                                    if (Long.parseLong(jobId) < (Long.parseLong(currDate) - 15000000)) {
                                        masterContext.getQuartzSchedulerService().deleteJob(jobId);
                                    } else if (Long.parseLong(jobId) >= Long.parseLong(currDate) && Long.parseLong(jobId) < Long.parseLong(nextDay)) {
                                        if (actionMapNew.containsKey(Long.parseLong(jobId))) {
                                            masterContext.getQuartzSchedulerService().deleteJob(jobId);
                                            masterContext.getHeraJobActionService().delete(jobId);
                                        }
                                    }
                                });
                            }
                        }
                        log.info("clear job scheduler ok");
                    }

                } catch (Exception e) {
                    log.error("roll back lost job failed or clear job schedule failed !", e);
                }
                masterContext.masterTimer.newTimeout(this, 30, TimeUnit.MINUTES);
            }
        };
        masterContext.masterTimer.newTimeout(clearScheduleTask, 30, TimeUnit.MINUTES);

        TimerTask generateActionTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();

                int executeHour = DateUtil.getCurrentHour(calendar);
                int executeMinute = DateUtil.getCurrentMinute(calendar);
                //凌晨生成版本，早上七点以后开始再次生成版本
                boolean execute = (executeHour == 0 && executeMinute == 0)
                        || (executeHour == 0 && executeMinute == 35)
                        || (executeHour > 7 && executeMinute == 20)
                        || (executeHour > 7 && executeMinute < 22);
                if (execute) {
                    String currString = DateUtil.getTodayStringForAction();
                    if (executeHour == 23) {
                        currString = DateUtil.getNextDayString().getSource();
                        now = DateUtil.getNextDayString().getTarget();
                    }
                    log.info("generate depend action date: " + currString);
                    List<HeraJob> jobList = masterContext.getHeraJobService().getAll();
                    Map<Long, HeraAction> actionMap = new HashMap<>();
                    SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
                    generateScheduleJobAction(jobList, now, dfDate, actionMap);
                    generateDependJobAction(jobList, now, dfDate, actionMap, 0);
                    if (executeHour < 23) {
                        heraActionMap = actionMap;
                    }
                    log.info("generate depend action success" + actionMap.size());
                    Dispatcher dispatcher = masterContext.getDispatcher();
                    if (dispatcher != null) {
                        if (actionMap.size() > 0) {
                            for (Long id : actionMap.keySet()) {
                                dispatcher.addJobHandler(new JobHandler(id.toString(), masterContext.getMaster(), masterContext));
                                if (id > Long.parseLong(currString)) {
                                    masterContext.getDispatcher().forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateJob, id.toString()));
                                }
                            }
                        }
                    }
                    log.info("generate all action success");
                }
                masterContext.masterTimer.newTimeout(this, 30, TimeUnit.MINUTES);
            }
        };
        masterContext.masterTimer.newTimeout(generateActionTask, 30, TimeUnit.MINUTES);

        /**
         * 扫描任务等待队列，可获得worker的任务将执行
         * 对于没有科运行机器的时候，自动调度任务将offer到exception队列，manual,debug任务重新offer到原队列
         *
         */
        TimerTask scanWaitingQueueTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                try {
                    scan();
                    log.info("scan waiting queueTask run");
                } catch (Exception e) {
                    log.error("scan waiting queueTask exception");
                }
                masterContext.masterTimer.newTimeout(this, 2, TimeUnit.SECONDS);
            }
        };
        masterContext.masterTimer.newTimeout(scanWaitingQueueTask, 3, TimeUnit.SECONDS);


        /**
         * 扫描exception队列去执行
         *
         */
        TimerTask scanExceptionQueueTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                try {
                    scanExceptionQueue();
                    log.info("scan exception queueTask run");
                } catch (Exception e) {
                    log.error("scan exception queueTask exception");
                }
                masterContext.masterTimer.newTimeout(this, 2, TimeUnit.SECONDS);
            }
        };
        masterContext.masterTimer.newTimeout(scanExceptionQueueTask, 3, TimeUnit.SECONDS);

        TimerTask checkHeartBeatTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                Date now = new Date();
                for (MasterWorkHolder worker : masterContext.getWorkMap().values()) {
                    Date timestamp = worker.getHeartBeatInfo().timestamp;
                    try {
                        if (timestamp == null || (now.getTime() - timestamp.getTime()) > 1000 * 60) {
                            worker.getChannel().close();
                        }
                    } catch (Exception e) {
                        log.error("worker error, master close channel");
                    }

                }
                masterContext.masterTimer.newTimeout(this, 4, TimeUnit.SECONDS);
            }
        };
        masterContext.masterTimer.newTimeout(checkHeartBeatTask, 4, TimeUnit.SECONDS);
    }


    private void scanExceptionQueue() {
        if (!masterContext.getExceptionQueue().isEmpty()) {
            final JobElement element = masterContext.getExceptionQueue().poll();
            runScheduleJobAction(element);
        }

    }

    /**
     * hera自动调度任务版本生成，版本id 18位当前时间 + actionId,
     *
     * @param jobList
     * @param now
     * @param format
     * @param actionMap
     */
    public void generateScheduleJobAction(List<HeraJob> jobList, Date now, SimpleDateFormat format, Map<Long, HeraAction> actionMap) {
        for (HeraJob heraJob : jobList) {
            if (heraJob.getScheduleType() != null && heraJob.getScheduleType().equals("0")) {
                String cron = heraJob.getCronExpression();
                String cronDate = format.format(now);
                List<String> list = new ArrayList<>();
                if (StringUtils.isNotBlank(cron)) {
                    boolean isCronExp = false;
                    isCronExp = CronParse.Parser(cron, cronDate, list);
                    if (!isCronExp) {
                        log.error("cron parse error,cron = " + cron);
                    }
                    list.stream().forEach(str -> {
                        String actionDate = HeraDateTool.StringToDateStr(str, "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmm");
                        String actionCron = HeraDateTool.StringToDateStr(str, "yyyy-MM-dd HH:mm:ss", "0 m H d M") + " ?";
                        HeraAction heraAction = new HeraAction();
                        BeanUtils.copyProperties(heraJob, heraAction);
                        Long actionId = Long.parseLong(actionDate) * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
                        heraAction.setId(actionId.toString());
                        heraAction.setCronExpression(actionCron);
                        heraAction.setGmtCreate(new Date());
                        heraAction.setJobId(String.valueOf(heraJob.getId()));

                        masterContext.getHeraJobActionService().insert(heraAction);
                        log.info("generate actions success :" + actionDate);
                        actionMap.put(Long.parseLong(heraAction.getId()), heraAction);

                    });
                }
            }
        }
    }

    /**
     * hera 依赖任务版本生成
     *
     * @param jobList
     * @param actionMap
     * @param retryCount
     */
    public void generateDependJobAction(List<HeraJob> jobList, Date now, SimpleDateFormat simpleDateFormat, Map<Long, HeraAction> actionMap, int retryCount) {
        retryCount++;
        String currDate = simpleDateFormat.format(now);
        String scheduleType = "1";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd0000000000");
        String actionDate = dateFormat.format(now);
        for (HeraJob heraJob : jobList) {
            //依赖任务生成版本
            if (heraJob.getScheduleType() != null && heraJob.getScheduleType().equals(scheduleType)) {

                String jobDependencies = heraJob.getDependencies();
                if (StringUtils.isNotBlank(jobDependencies)) {

                    Map<String, List<HeraAction>> dependenciesMap = new HashMap<>(1024);
                    String[] dependencies = jobDependencies.split(",");
                    for (String dependentId : dependencies) {
                        List<HeraAction> dependActionList = new ArrayList<>();

                        for (Map.Entry<Long, HeraAction> entry : actionMap.entrySet()) {
                            if (entry.getValue().getJobId().equals(dependentId)) {
                                dependActionList.add(entry.getValue());
                            }
                        }
                        dependenciesMap.put(dependentId, dependActionList);
                        if (retryCount > 20) {
                            if (!heraJob.getConfigs().contains("sameday")) {
                                if (dependenciesMap.get(dependentId).size() == 0) {
                                    List<HeraAction> lostJobAction = masterContext.getHeraJobActionService().findByJobId(dependentId);
                                    actionMap.put(Long.parseLong(lostJobAction.get(0).getId()), lostJobAction.get(0));
                                    dependActionList.add(lostJobAction.get(0));
                                    dependenciesMap.put(dependentId, dependActionList);
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    Long actionId = Long.parseLong(actionDate);
                    List<String> dependentList = Arrays.asList(dependencies)
                            .stream()
                            .map(dependentId -> {
                                Long tmp = actionId + Long.parseLong(dependentId);
                                return String.valueOf(tmp);
                            }).collect(Collectors.toList());

                    String dependenciesId = dependentList.stream().collect(Collectors.joining(","));
                    HeraAction actionNew = new HeraAction();
                    BeanUtils.copyProperties(heraJob, actionNew);
                    actionNew.setId(String.valueOf(actionId + Long.parseLong(String.valueOf(heraJob.getId()))));
                    actionNew.setGmtCreate(new Date());
                    actionNew.setDependencies(dependenciesId);
                    actionNew.setJobDependencies(heraJob.getDependencies());
                    actionNew.setJobId(String.valueOf(heraJob.getId()));
                    if (!actionMap.containsKey(actionNew.getId())) {
                        masterContext.getHeraJobActionService().insert(actionNew);
                        actionMap.put(Long.parseLong(actionNew.getId()), actionNew);
                    }
                }
            }
        }
    }


    private void rollBackLostJob(Long actionId, Map<Long, HeraAction> actionMapNew, int retryCount, List<Long> actionIdList) {
        retryCount++;
        HeraAction lostJob = actionMapNew.get(actionId);
        if (lostJob != null) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(","));
                boolean isAllComplete = true;
                if (jobDependList != null && jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        if (actionMapNew.get(jobDepend) != null) {
                            HeraAction action = actionMapNew.get(jobDepend);
                            if (action != null) {
                                String status = action.getStatus();
                                if (status == null || status.equals("wait")) {
                                    isAllComplete = false;
                                    if (retryCount < 30 && actionIdList.contains(Long.parseLong(jobDepend))) {
                                        rollBackLostJob(Long.parseLong(jobDepend), actionMapNew, retryCount, actionIdList);

                                    }
                                } else if (status.equals("failed")) {
                                    isAllComplete = false;
                                }
                            }

                        }
                    }
                }
                if (isAllComplete) {
                    if (!actionIdList.contains(actionId)) {
                        masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId.toString()));
                        actionIdList.add(actionId);
                        log.info("roll back lost actionId :" + actionId);
                    }
                }
            }
        }
    }

    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public void scan() {

        if (!masterContext.getScheduleQueue().isEmpty()) {
            final JobElement element = masterContext.getScheduleQueue().poll();
            runScheduleJobAction(element);
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            final JobElement element = masterContext.getManualQueue().poll();
            MasterWorkHolder selectWork = getRunnableWork(element.getHostGroupId());
            if (selectWork == null) {
                masterContext.getManualQueue().offer(element);
            } else {
                runManualJob(selectWork, element.getJobId());
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            final JobElement element = masterContext.getDebugQueue().poll();
            MasterWorkHolder selectWork = getRunnableWork(element.getHostGroupId());
            if (selectWork == null) {
                masterContext.getDebugQueue().offer(element);
            } else {
                runDebugJob(selectWork, element.getJobId());
            }
        }

    }

    /**
     * 手动执行任务调度器执行逻辑，向master的channel写manual任务执行请求
     *
     * @param selectWork
     * @param historyId
     */
    private void runManualJob(MasterWorkHolder selectWork, String historyId) {
        final MasterWorkHolder workHolder = selectWork;
        log.info("start run manual job, historyId = {}", historyId);
        this.executeJobPool.execute(new Runnable() {
            @Override
            public void run() {
                HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(historyId);
                HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
                historyVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
                JobStatus jobStatus = masterContext.getHeraJobActionService().findJobStatus(history.getActionId());

                jobStatus.setStatus(StatusEnum.RUNNING);
                jobStatus.setHistoryId(historyId);
                historyVo.setStatusEnum(jobStatus.getStatus());
                masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(historyVo));

                Exception exception = null;
                Response response = null;
                try {
                    Future<Response> future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                            ExecuteKind.ManualKind, history.getId());
                    response = future.get();
                } catch (Exception e) {
                    exception = e;
                    log.error("manual job run error" + historyId, e);
                    jobStatus.setStatus(StatusEnum.FAILED);
                    history.setStatus(jobStatus.getStatus().toString());
                    masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(history);

                }
                boolean success = response.getStatus() != null && response.getStatus() == Protocol.Status.OK;
                log.info("historyId 执行结果" + historyId + "---->" + response.getStatus());

                if (!success) {
                    HeraException heraException = null;
                    if (exception != null) {
                        heraException = new HeraException(exception);
                        log.error("manual actionId = {} error, {}", history.getActionId(), heraException.getMessage());
                    }
                    log.info("actionId = {} manual execute failed", history.getActionId());
                    jobStatus.setStatus(StatusEnum.FAILED);
                    HeraJobHistory jobHistory = masterContext.getHeraJobHistoryService().findById(history.getId());
                    HeraJobHistoryVo jobHistoryVo = BeanConvertUtils.convert(jobHistory);
                    HeraJobFailedEvent failedEvent = new HeraJobFailedEvent(history.getActionId(), jobHistoryVo.getTriggerType(), jobHistoryVo);
                    if (jobHistory != null && jobHistory.getIllustrate() != null
                            && jobHistory.getIllustrate().contains(LogConstant.CANCEL_JOB_LOG)) {
                        masterContext.getDispatcher().forwardEvent(failedEvent);
                    }
                } else {
                    jobStatus.setStatus(StatusEnum.SUCCESS);
                    HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(history.getActionId(), historyVo.getTriggerType(), history.getId());
                    masterContext.getDispatcher().forwardEvent(successEvent);
                }
            }
        });
    }

    /**
     * 自动调度任务执行入口，当出队列的任务获取不到执行worker的情况下，任务先进入exceptionQueue进行等待
     *
     * @param element
     */
    private void runScheduleJobAction(JobElement element) {
        MasterWorkHolder workHolder = getRunnableWork(element.getHostGroupId());
        if (workHolder == null) {
            masterContext.getExceptionQueue().offer(element);
        } else {
            runScheduleJob(workHolder, element.getJobId());
        }

    }

    /**
     * 调度任务执行前，先获取任务的执行重试时间间隔和重试次数
     *
     * @param workHolder
     * @param actionId
     */
    private void runScheduleJob(MasterWorkHolder workHolder, String actionId) {
        final MasterWorkHolder work = workHolder;
        this.executeJobPool.execute(new Runnable() {
            @Override
            public void run() {
                int runCount = 0;
                int retryCount = 0;
                int retryWaitTime = 1;
                HeraActionVo heraActionVo = masterContext.getHeraJobActionService().findHeraActionVo(actionId).getSource();
                Map<String, String> properties = heraActionVo.getConfigs();
                if (properties != null && properties.size() > 0) {
                    retryCount = Integer.parseInt(properties.get("roll.back.times") == null ? "0" : properties.get("roll.back.times"));
                    retryWaitTime = Integer.parseInt(properties.get("roll.back.wait.time") == null ? "0" : properties.get("roll.back.wait.time"));
                }
                runScheduleJobContext(work, actionId, runCount, retryCount, retryWaitTime);
            }
        });
    }

    /**
     * 自动调度任务开始执行入口，向master端的channel写请求任务执行请求
     *
     * @param work
     * @param actionId
     * @param runCount
     * @param retryCount
     * @param retryWaitTime
     */
    private void runScheduleJobContext(MasterWorkHolder work, String actionId, int runCount, int retryCount, int retryWaitTime) {
        runCount++;
        boolean isCancelJob = false;
        if (runCount > 1) {
            try {
                Thread.sleep(retryWaitTime * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        HeraJobHistoryVo heraJobHistoryVo;
        TriggerTypeEnum triggerType = null;
        if (runCount == 1) {
            HeraJobHistory heraJobHistory = masterContext.getHeraJobHistoryService().
                    findById(masterContext.getHeraJobActionService().findById(actionId).getHistoryId());
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            triggerType = heraJobHistoryVo.getTriggerType();
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");

        } else {
            HeraActionVo heraJobVo = masterContext.getHeraJobActionService().findHeraActionVo(actionId).getSource();
            heraJobHistoryVo = HeraJobHistoryVo.builder()
                    .illustrate("失败任务重试，开始执行")
                    .triggerType(TriggerTypeEnum.SCHEDULE)
                    .jobId(actionId)
                    .operator(heraJobVo.getOwner())
                    .status(StatusEnum.RUNNING)
                    .hostGroupId(heraJobVo.getHostGroupId())
                    .build();
            masterContext.getHeraJobHistoryService().insert(BeanConvertUtils.convert(heraJobHistoryVo));
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 第" + (runCount - 1) + "次重试运行");
        }
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(heraJobHistoryVo));
        heraJobHistoryVo.setStatusEnum(StatusEnum.RUNNING);
        JobStatus jobStatus = masterContext.getHeraJobActionService().findJobStatus(actionId);
        jobStatus.setHistoryId(actionId);
        jobStatus.setStatus(StatusEnum.RUNNING);
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));

        Exception exception = null;
        Response response = null;
        try {
            Future<Response> future = new MasterExecuteJob().executeJob(masterContext, work,
                    ExecuteKind.ScheduleKind, heraJobHistoryVo.getId());
            response = future.get();
        } catch (Exception e) {
            log.error("schedule job run error :" + actionId, e);
            jobStatus.setStatus(StatusEnum.FAILED);
            heraJobHistoryVo.setStatusEnum(jobStatus.getStatus());
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));

        }
        boolean success = response.getStatus() == Protocol.Status.OK;
        log.info("job_id 执行结果" + actionId + "---->" + response.getStatus());

        if (success && (heraJobHistoryVo.getTriggerType() == TriggerTypeEnum.SCHEDULE
                || heraJobHistoryVo.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER)) {
            jobStatus.setReadyDependency(new HashMap<>());
        }
        if (!success) {
            jobStatus.setStatus(StatusEnum.FAILED);
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(heraJobHistoryVo.getId());
            HeraJobHistoryVo jobHistory = BeanConvertUtils.convert(history);
            HeraJobFailedEvent event = new HeraJobFailedEvent(actionId, triggerType, jobHistory);
            event.setRollBackTime(retryWaitTime);
            event.setRunCount(runCount);
            if (jobHistory != null && jobHistory.getIllustrate() != null
                    && jobHistory.getIllustrate().contains("手动取消该任务")) {
                isCancelJob = true;
            } else {
                masterContext.getDispatcher().forwardEvent(event);
            }
        } else {
            jobStatus.setStatus(StatusEnum.SUCCESS);
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, triggerType, heraJobHistoryVo.getId());
            heraJobHistoryVo.setStatus(StatusEnum.SUCCESS);
            masterContext.getDispatcher().forwardEvent(successEvent);
        }
        if (runCount < (retryCount + 1) && !success && !isCancelJob) {
            runScheduleJobContext(work, actionId, runCount, retryCount, retryWaitTime);
        }
    }

    /**
     * 开发中心脚本执行逻辑
     *
     * @param selectWork
     * @param jobId
     */
    private void runDebugJob(MasterWorkHolder selectWork, String jobId) {
        final MasterWorkHolder workHolder = selectWork;
        this.executeJobPool.execute(new Runnable() {
            @Override
            public void run() {
                HeraDebugHistoryVo history = masterContext.getHeraDebugHistoryService().findById(jobId);
                history.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
                masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(history));
                Exception exception = null;
                Response response = null;
                try {
                    Future<Response> future = new MasterExecuteJob().executeJob(masterContext, workHolder, ExecuteKind.DebugKind, jobId);
                    response = future.get();
                } catch (Exception e) {
                    exception = e;
                    log.error(String.format("debugId:%s run failed", jobId), e);
                }
                boolean success = response.getStatus() == Protocol.Status.OK ? true : false;
                if (!success) {
                    exception = new HeraException(String.format("fileId:%s run failed ", history.getFileId()), exception);
                    log.info("debug job error");
                    history = masterContext.getHeraDebugHistoryService().findById(jobId);
                    HeraDebugFailEvent failEvent = HeraDebugFailEvent.builder()
                            .debugHistory(BeanConvertUtils.convert(history))
                            .throwable(exception)
                            .fileId(history.getFileId())
                            .build();
                    masterContext.getDispatcher().forwardEvent(failEvent);
                } else {
                    log.info("debug success");
                    HeraDebugSuccessEvent successEvent = HeraDebugSuccessEvent.builder()
                            .fileId(history.getFileId())
                            .history(BeanConvertUtils.convert(history))
                            .build();
                    masterContext.getDispatcher().forwardEvent(successEvent);
                }
            }
        });
    }

    /**
     * 获取hostGroupId中可以分发任务的worker
     *
     * @param hostGroupId
     * @return
     */
    private MasterWorkHolder getRunnableWork(int hostGroupId) {
        if (hostGroupId == 0) {
            hostGroupId = HeraGlobalEnvironment.defaultWorkerGroup;
        }
        MasterWorkHolder workHolder = null;
        if (masterContext.getHostGroupCache() != null) {
            HeraHostGroupVo hostGroupCache = masterContext.getHostGroupCache().get(hostGroupId);
            List<String> hosts = hostGroupCache.getHosts();
            if (hostGroupCache != null && hosts != null && hosts.size() > 0) {
                int size = hosts.size();
                for (int i = 0; i < size && workHolder == null; i++) {
                    String host = hostGroupCache.selectHost();
                    if (host == null) {
                        break;
                    }
                    for (MasterWorkHolder worker : masterContext.getWorkMap().values()) {
                        if (worker != null && worker.heartBeatInfo != null && worker.heartBeatInfo.host.trim().equals(host.trim())) {
                            HeartBeatInfo heartBeatInfo = worker.heartBeatInfo;
                            if (heartBeatInfo.getMemRate() != null && heartBeatInfo.getCpuLoadPerCore() != null
                                    && heartBeatInfo.getMemRate() < HeraGlobalEnvironment.getMaxMemRate() && heartBeatInfo.getCpuLoadPerCore() < HeraGlobalEnvironment.getMaxCpuLoadPerCore()) {

                                Float assignTaskNum = (heartBeatInfo.getMemTotal() - HeraGlobalEnvironment.getMaxCpuLoadPerCore()) / HeraGlobalEnvironment.getMaxCpuLoadPerCore();
                                int sum = heartBeatInfo.debugRunning.size() + heartBeatInfo.manualRunning.size() + heartBeatInfo.running.size();
                                if (assignTaskNum.intValue() > sum) {
                                    workHolder = worker;
                                    break;
                                }
                            }
                        } else {
                            if (worker == null) {
                                log.error("worker is null");
                            } else if (worker != null && worker.getHeartBeatInfo() == null && worker.getChannel() != null) {
                                log.error("worker " + worker.getChannel() + "heart is null");
                            }
                        }
                    }
                }
            }
        }
        return workHolder;
    }

    public void debug(HeraDebugHistoryVo debugHistory) {
        JobElement element = JobElement.builder()
                .jobId(debugHistory.getId())
                .hostGroupId(debugHistory.getHostGroupId())
                .build();
        debugHistory.setStatusEnum(StatusEnum.RUNNING);
        debugHistory.setStartTime(new Date());
        debugHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 进入任务队列");
        masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        masterContext.getDebugQueue().offer(element);

    }

    /**
     * 手动执行任务或者手动恢复任务的时候，先进行任务是否在执行的判断，
     * 没有在运行进入队列等待，已经在运行的任务不入队列，避免重复执行
     *
     * @param heraJobHistory
     * @return
     */
    public HeraJobHistoryVo run(HeraJobHistoryVo heraJobHistory) {
        String actionId = heraJobHistory.getActionId();
        int priorityLevel = 3;
        HeraActionVo heraJobVo = masterContext.getHeraJobActionService().findHeraActionVo(actionId).getSource();
        String priorityLevelValue = heraJobVo.getConfigs().get("run.priority.level");
        if (priorityLevelValue != null) {
            priorityLevel = Integer.parseInt(priorityLevelValue);
        }
        JobElement element = JobElement.builder()
                .jobId(heraJobHistory.getActionId())
                .hostGroupId(heraJobHistory.getHostGroupId())
                .priorityLevel(priorityLevel)
                .build();
        heraJobHistory.setStatusEnum(StatusEnum.RUNNING);
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER) {
            for (JobElement jobElement : new ArrayList<>(masterContext.getScheduleQueue())) {
                if (jobElement.getJobId().equals(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    break;
                }
            }
            for (Channel key : masterContext.getWorkMap().keySet()) {
                MasterWorkHolder workHolder = masterContext.getWorkMap().get(key);
                if (workHolder.getRunning().containsKey(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    break;
                }
            }
        }

        if (heraJobHistory.getStatusEnum() == StatusEnum.RUNNING) {
            heraJobHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "进入任务队列");
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(heraJobHistory));
            if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {
                element.setJobId(heraJobHistory.getId());
                masterContext.getManualQueue().offer(element);
            } else {
                JobStatus jobStatus = masterContext.getHeraJobActionService().findJobStatus(actionId);
                jobStatus.setStatus(StatusEnum.RUNNING);
                jobStatus.setHistoryId(heraJobHistory.getId());
                masterContext.getHeraJobActionService().updateStatus(jobStatus);
                masterContext.getScheduleQueue().offer(element);
            }
        }
        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
        return heraJobHistory;
    }
}
