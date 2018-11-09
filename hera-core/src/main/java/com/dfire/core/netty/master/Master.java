package com.dfire.core.netty.master;


import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.util.*;
import com.dfire.common.vo.JobStatus;
import com.dfire.core.HeraException;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.*;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.factory.StrategyWorkerEnum;
import com.dfire.core.route.factory.StrategyWorkerFactory;
import com.dfire.core.route.strategy.IStrategyWorker;
import com.dfire.core.util.CronParse;
import com.dfire.logs.DebugLog;
import com.dfire.logs.HeraLog;
import com.dfire.logs.ScheduleLog;
import com.dfire.logs.SocketLog;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcResponse;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.dfire.protocol.JobExecuteKind.ExecuteKind.ScheduleKind;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc hera核心任务调度器
 */
@Component
@Order(1)
public class Master {

    private MasterContext masterContext;
    private Map<Long, HeraAction> heraActionMap = new HashMap<>();
    private ThreadPoolExecutor executeJobPool;

    public Map<Long, HeraAction> getHeraActionMap() {
        return heraActionMap;
    }

    public void init(MasterContext masterContext) {
        this.masterContext = masterContext;
        heraActionMap = new HashMap<>();
        executeJobPool = new ThreadPoolExecutor(HeraGlobalEnvironment.getMaxParallelNum(), HeraGlobalEnvironment.getMaxParallelNum(), 10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("master-execute-job-thread"), new ThreadPoolExecutor.AbortPolicy());
        executeJobPool.allowCoreThreadTimeOut(true);
        if (HeraGlobalEnvironment.getEnv().equalsIgnoreCase(Constants.PRE_ENV)) {
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }

        masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
        masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));

        List<HeraAction> allJobList = masterContext.getHeraJobActionService().getTodayAction();
        allJobList.forEach(heraAction -> masterContext.getDispatcher().
                addJobHandler(new JobHandler(String.valueOf(heraAction.getId()), this, masterContext)));
        masterContext.getDispatcher().forwardEvent(Events.Initialize);
        masterContext.refreshHostGroupCache();
        HeraLog.info("refresh hostGroup cache");


        // 1.生成版本
        batchActionCheck();
        // 2.扫描任务
        waitingQueueCheck();
        // 3.心跳检查
        heartCheck();

    }

    /**
     * 版本定时生成
     */
    private void batchActionCheck() {
        masterContext.masterSchedule.scheduleWithFixedDelay(() -> {
            try {
                ScheduleLog.info("全量任务版本生成");
                generateAction(false, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 60, TimeUnit.MINUTES);
    }


    /**
     * 扫描任务等待队列，可获得worker的任务将执行
     * 对于没有可运行机器的时，manual,debug任务重新offer到原队列
     */
    private void waitingQueueCheck() {


        masterContext.masterSchedule.schedule(new Runnable() {
            // scan频率递增的步长
            private final Integer DELAY_TIME = 100;
            // 最大scan频率
            private final Integer MAX_DELAY_TIME = 10 * 1000;

            private Integer nextTime = HeraGlobalEnvironment.getScanRate();

            @Override
            public void run() {
                try {
                    if (scan()) {
                        nextTime = HeraGlobalEnvironment.getScanRate();
                    } else {
                        nextTime = (nextTime + DELAY_TIME) > MAX_DELAY_TIME ? MAX_DELAY_TIME : nextTime + DELAY_TIME;
                    }
                    ScheduleLog.info("scan waiting queueTask run");
                } catch (Exception e) {
                    ScheduleLog.error("scan waiting queueTask exception",e);
                } finally {
                    masterContext.masterSchedule.schedule(this, nextTime, TimeUnit.MILLISECONDS);
                }
            }
        }, HeraGlobalEnvironment.getScanRate(), TimeUnit.MILLISECONDS);
    }

    /**
     * 定时检测work心跳是否超时
     */
    private void heartCheck() {

        masterContext.masterSchedule.scheduleWithFixedDelay(() -> {
            Date now = new Date();
            Map<Channel, MasterWorkHolder> workMap = masterContext.getWorkMap();
            List<Channel> removeChannel = new ArrayList<>(workMap.size());
            for (Channel channel : workMap.keySet()) {
                MasterWorkHolder workHolder = workMap.get(channel);
                if (workHolder.getHeartBeatInfo() == null) {
                    continue;
                }
                Date workTime = workHolder.getHeartBeatInfo().getTimestamp();
                if (workTime == null || now.getTime() - workTime.getTime() > 1000 * 60L) {
                    workHolder.getChannel().close();
                    removeChannel.add(channel);
                }
            }
            removeChannel.forEach(workMap::remove);
        }, 0, 1, TimeUnit.MINUTES);
    }



    public boolean generateSingleAction(Integer jobId) {
        ScheduleLog.info("单个任务版本生成：{}", jobId);
        return generateAction(true, jobId);
    }

    private synchronized boolean generateAction(boolean isSingle, Integer jobId) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        int executeHour = DateUtil.getCurrentHour(calendar);
        //凌晨生成版本，早上七点以后开始再次生成版本
        boolean execute = executeHour == 0
                || (executeHour > 7 && executeHour <= 23);
        if (execute || isSingle) {
            String currString = DateUtil.getNowStringForAction();
            if (executeHour == 23) {
                Tuple<String, Date> nextDayString = DateUtil.getNextDayString();
                //例如：今天 2018.07.17 23:50  currString = 2018.07.18 now = 2018.07.18 23:50
                currString = nextDayString.getSource();
                now = nextDayString.getTarget();
            }
            Map<Long, HeraAction> actionMap = new HashMap<>(heraActionMap.size());
            List<HeraJob> jobList = new ArrayList<>();
            SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
            //批量生成
            if (!isSingle) {
                jobList = masterContext.getHeraJobService().getAll();
            } else { //单个任务生成版本
                HeraJob heraJob = masterContext.getHeraJobService().findById(jobId);
                jobList.add(heraJob);
                actionMap = heraActionMap;
                List<Long> shouldRemove = new ArrayList<>();
                for (Long actionId : actionMap.keySet()) {
                    if (StringUtil.actionIdToJobId(String.valueOf(actionId), String.valueOf(jobId))) {
                        shouldRemove.add(actionId);
                    }
                }
                shouldRemove.forEach(actionMap::remove);
            }
            generateScheduleJobAction(jobList, now, dfDate, actionMap);
            generateDependJobAction(jobList, actionMap, 0);

            if (executeHour < 23) {
                heraActionMap = actionMap;
            }

            Dispatcher dispatcher = masterContext.getDispatcher();
            if (dispatcher != null) {
                if (actionMap.size() > 0) {
                    for (Long id : actionMap.keySet()) {
                        dispatcher.addJobHandler(new JobHandler(id.toString(), masterContext.getMaster(), masterContext));
                        if (id >= Long.parseLong(currString)) {
                            dispatcher.forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateActions, id.toString()));
                        }
                    }
                }
            }
            ScheduleLog.info("generate all action success");
            return true;
        }
        return false;
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
            if (heraJob.getScheduleType() != null && heraJob.getScheduleType() == 0) {
                String cron = heraJob.getCronExpression();
                String cronDate = format.format(now);
                List<String> list = new ArrayList<>();
                if (StringUtils.isNotBlank(cron)) {
                    boolean isCronExp = CronParse.Parser(cron, cronDate, list);
                    if (!isCronExp) {
                        ScheduleLog.error("cron parse error,cron = " + cron);
                        continue;
                    }
                    list.forEach(str -> {
                        String actionDate = HeraDateTool.StringToDateStr(str, "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmm");
                        String actionCron = HeraDateTool.StringToDateStr(str, "yyyy-MM-dd HH:mm:ss", "0 m H d M") + " ?";
                        HeraAction heraAction = new HeraAction();
                        BeanUtils.copyProperties(heraJob, heraAction);
                        Long actionId = Long.parseLong(actionDate) * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
                        heraAction.setId(actionId.toString());
                        heraAction.setCronExpression(actionCron);
                        heraAction.setGmtCreate(new Date());
                        heraAction.setJobId(String.valueOf(heraJob.getId()));
                        heraAction.setHistoryId(heraJob.getHistoryId());
                        heraAction.setAuto(heraJob.getAuto());
                        heraAction.setGmtModified(new Date());
                        heraAction.setJobDependencies(null);
                        heraAction.setDependencies(null);
                        heraAction.setReadyDependency(null);
                        heraAction.setHostGroupId(heraJob.getHostGroupId());
                        masterContext.getHeraJobActionService().insert(heraAction);
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
    public void generateDependJobAction(List<HeraJob> jobList, Map<Long, HeraAction> actionMap, int retryCount) {
        retryCount++;
        int noCompleteCount = 0, retryId = -1;
        for (HeraJob heraJob : jobList) {
            //依赖任务生成版本
            if (heraJob.getScheduleType() != null && heraJob.getScheduleType() == 1) {

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
                                    HeraAction lostJobAction = masterContext.getHeraJobActionService().findLatestByJobId(dependentId);
                                    actionMap.put(Long.parseLong(lostJobAction.getId()), lostJobAction);
                                    dependActionList.add(lostJobAction);
                                    dependenciesMap.put(dependentId, dependActionList);
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    boolean isComplete = true;

                    String actionMostDeps = "";

                    for (String dependency : dependencies) {
                        if (dependenciesMap.get(dependency) == null || dependenciesMap.get(dependency).size() == 0) {
                            isComplete = false;
                            break;
                        }

                        if (StringUtils.isBlank(actionMostDeps)) {
                            actionMostDeps = dependency;
                        }

                        if (dependenciesMap.get(actionMostDeps).size() < dependenciesMap.get(dependency).size()) {
                            actionMostDeps = dependency;
                        } else if (dependenciesMap.get(dependency).size() > 0 && dependenciesMap.get(actionMostDeps).size() == dependenciesMap.get(dependency).size() &&
                                Long.parseLong(dependenciesMap.get(actionMostDeps).get(0).getId()) > Long.parseLong(dependenciesMap.get(dependency).get(0).getId())) {
                            actionMostDeps = dependency;
                        }
                    }
                    //新加任务 可能无版本
                    if (!isComplete) {
                        noCompleteCount++;
                        retryId = heraJob.getId();
                        continue;
                    } else {
                        List<HeraAction> actionMostList = dependenciesMap.get(actionMostDeps);
                        if (actionMostList != null && actionMostList.size() > 0) {
                            for (HeraAction action : actionMostList) {
                                StringBuilder actionDependencies = new StringBuilder(action.getId());
                                Long longActionId = Long.parseLong(actionDependencies.toString());
                                for (String dependency : dependencies) {
                                    if (!dependency.equals(actionMostDeps)) {
                                        List<HeraAction> otherAction = dependenciesMap.get(dependency);
                                        if (otherAction == null || otherAction.size() == 0) {
                                            continue;
                                        }
                                        String otherActionId = otherAction.get(0).getId();
                                        for (HeraAction o : otherAction) {
                                            if (Math.abs(Long.parseLong(o.getId()) - longActionId) < Math.abs(Long.parseLong(otherActionId) - longActionId)) {
                                                otherActionId = o.getId();
                                            }
                                        }
                                        actionDependencies.append(",");
                                        actionDependencies.append(Long.parseLong(otherActionId) / 1000000 * 1000000 + Long.parseLong(dependency));
                                    }
                                }
                                HeraAction actionNew = new HeraAction();
                                BeanUtils.copyProperties(heraJob, actionNew);
                                Long actionId = longActionId / 1000000 * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
                                actionNew.setId(String.valueOf(actionId));
                                actionNew.setGmtCreate(new Date());
                                actionNew.setDependencies(actionDependencies.toString());
                                actionNew.setJobDependencies(heraJob.getDependencies());
                                actionNew.setJobId(String.valueOf(heraJob.getId()));
                                actionNew.setAuto(heraJob.getAuto());
                                actionNew.setGmtModified(new Date());
                                actionNew.setHostGroupId(heraJob.getHostGroupId());
                                if (!actionMap.containsKey(actionId)) {
                                    masterContext.getHeraJobActionService().insert(actionNew);
                                    actionMap.put(Long.parseLong(actionNew.getId()), actionNew);
                                }
                            }
                        }


                    }

                }
            }
        }
        if (noCompleteCount > 0 && retryCount < 40) {
            generateDependJobAction(jobList, actionMap, retryCount);
        } else if (retryCount == 40 && noCompleteCount > 0) {
            ScheduleLog.warn("重试ID:{}, 未找到版本个数:{} , 重试次数:{}", retryId, noCompleteCount, retryCount);
        }
    }


    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public boolean scan() {
        boolean hasTask = false;
        if (!masterContext.getScheduleQueue().isEmpty()) {
            ScheduleLog.warn("schedule队列任务：{}", masterContext.getScheduleQueue());
            JobElement jobElement = masterContext.getScheduleQueue().peek();
            MasterWorkHolder workHolder = getRunnableWork(jobElement);
            if (workHolder == null) {
                ScheduleLog.warn("can not get work to execute job in master");
            } else {
                jobElement = masterContext.getScheduleQueue().poll();
                runScheduleJob(workHolder, jobElement.getJobId());
                hasTask = true;
            }
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            ScheduleLog.warn("manual队列任务：{}", masterContext.getManualQueue());
            JobElement jobElement = masterContext.getManualQueue().peek();
            MasterWorkHolder selectWork = getRunnableWork(jobElement);
            if (selectWork == null) {
                ScheduleLog.warn("can not get work to execute job in master");
            } else {
                jobElement = masterContext.getManualQueue().poll();
                runManualJob(selectWork, jobElement.getJobId());
                hasTask = true;
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            ScheduleLog.warn("debug队列任务：{}", masterContext.getDebugQueue());
            JobElement jobElement = masterContext.getDebugQueue().peek();
            MasterWorkHolder selectWork = getRunnableWork(jobElement);
            if (selectWork == null) {
                ScheduleLog.warn("can not get work to execute job in master");
            } else {
                jobElement = masterContext.getDebugQueue().poll();
                runDebugJob(selectWork, jobElement.getJobId());
                hasTask = true;
            }
        }
        return hasTask;

    }

    private void printThreadPoolLog() {
        String sb = "当前线程池信息" + "[ActiveCount: " + executeJobPool.getActiveCount() + "," +
                "CompletedTaskCount：" + executeJobPool.getCompletedTaskCount() + "," +
                "PoolSize:" + executeJobPool.getPoolSize() + "," +
                "LargestPoolSize:" + executeJobPool.getLargestPoolSize() + "," +
                "TaskCount:" + executeJobPool.getTaskCount() + "]";
        ScheduleLog.warn(sb);
    }

    /**
     * 手动执行任务调度器执行逻辑，向master的channel写manual任务执行请求
     *
     * @param selectWork
     * @param historyId
     */
    private void runManualJob(MasterWorkHolder selectWork, String historyId) {
        final MasterWorkHolder workHolder = selectWork;
        SocketLog.info("start run manual job, historyId = {}", historyId);

        this.executeJobPool.execute(() -> {
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(historyId);
            HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
            historyVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            JobStatus jobStatus = masterContext.getHeraJobActionService().findJobStatus(history.getActionId());

            jobStatus.setStatus(StatusEnum.RUNNING);
            jobStatus.setHistoryId(historyId);
            historyVo.setStatusEnum(jobStatus.getStatus());
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(historyVo));

            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                        JobExecuteKind.ExecuteKind.ManualKind, history.getId());
                response = future.get();
            } catch (Exception e) {
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                ScheduleLog.error("manual job run error" + historyId, e);
                jobStatus.setStatus(StatusEnum.FAILED);
                history.setStatus(jobStatus.getStatus().toString());
                masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(history);
            }
            boolean success = response != null && response.getStatusEnum() != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            ScheduleLog.info("historyId 执行结果" + historyId + "---->" + response.getStatusEnum());

            if (!success) {
                HeraException heraException = null;
                if (exception != null) {
                    heraException = new HeraException(exception);
                    ScheduleLog.error("manual actionId = {} error, {}", history.getActionId(), heraException.getMessage());
                }
                ScheduleLog.info("actionId = {} manual execute failed", history.getActionId());
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
        });
    }

    /**
     * 调度任务执行前，先获取任务的执行重试时间间隔和重试次数
     *
     * @param workHolder
     * @param actionId
     */
    private void runScheduleJob(MasterWorkHolder workHolder, String actionId) {
        final MasterWorkHolder work = workHolder;
        this.executeJobPool.execute(() -> {
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

        DebugLog.info("重试次数：{},重试时间：{},actionId:{}", retryCount, retryWaitTime, actionId);
        runCount++;
        boolean isCancelJob = false;
        if (runCount > 1) {
            DebugLog.info("任务重试，睡眠：{}秒", retryWaitTime);
            try {
                Thread.sleep(retryWaitTime * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        HeraJobHistoryVo heraJobHistoryVo;
        HeraJobHistory heraJobHistory;
        TriggerTypeEnum triggerType;
        if (runCount == 1) {
            heraJobHistory = masterContext.getHeraJobHistoryService().
                    findById(masterContext.getHeraJobActionService().findById(actionId).getHistoryId());
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            triggerType = heraJobHistoryVo.getTriggerType();
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");

        } else {
            HeraActionVo heraJobVo = masterContext.getHeraJobActionService().findHeraActionVo(actionId).getSource();
            heraJobHistory = HeraJobHistory.builder()
                    .illustrate(LogConstant.FAIL_JOB_RETRY)
                    .triggerType(TriggerTypeEnum.SCHEDULE.getId())
                    .jobId(heraJobVo.getJobId())
                    .actionId(heraJobVo.getId())
                    .operator(heraJobVo.getOwner())
                    .build();
            masterContext.getHeraJobHistoryService().insert(heraJobHistory);
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 第" + (runCount - 1) + "次重试运行\n");
            triggerType = heraJobHistoryVo.getTriggerType();
        }
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryLog(BeanConvertUtils.convert(heraJobHistoryVo));
        JobStatus jobStatus = masterContext.getHeraJobActionService().findJobStatus(actionId);
        jobStatus.setHistoryId(heraJobHistory.getId());
        jobStatus.setStatus(StatusEnum.RUNNING);
        jobStatus.setStartTime(new Date());
        masterContext.getHeraJobActionService().updateStatus(jobStatus);
        heraJobHistoryVo.setStatusEnum(StatusEnum.RUNNING);
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        RpcResponse.Response response = null;
        Future<RpcResponse.Response> future = null;
        try {
            future = new MasterExecuteJob().executeJob(masterContext, work,
                    ScheduleKind, heraJobHistory.getId());
            response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
        } catch (Exception e) {
            ScheduleLog.error("schedule job run error :" + actionId, e);
            if (future != null) {
                future.cancel(true);
            }
            jobStatus.setStatus(StatusEnum.FAILED);
            heraJobHistoryVo.setStatusEnum(jobStatus.getStatus());
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        }
        boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
        ScheduleLog.info("job_id 执行结果" + actionId + "---->" + (response == null ? "空指针" : response.getStatusEnum().toString()));
        if (success && (heraJobHistoryVo.getTriggerType() == TriggerTypeEnum.SCHEDULE
                || heraJobHistoryVo.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER)) {
            jobStatus.setReadyDependency(new HashMap<>(0));
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
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, triggerType, heraJobHistory.getId());
            heraJobHistory.setStatus(StatusEnum.SUCCESS.toString());
            masterContext.getDispatcher().forwardEvent(successEvent);
        }
        jobStatus.setEndTime(new Date());
        masterContext.getHeraJobActionService().updateStatus(jobStatus);
        if (runCount < (retryCount + 1) && !success && !isCancelJob) {
            DebugLog.info("--------------------------失败任务，准备重试--------------------------");
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
        this.executeJobPool.execute(() -> {
            HeraDebugHistoryVo history = masterContext.getHeraDebugHistoryService().findById(jobId);
            history.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(history));
            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder, JobExecuteKind.ExecuteKind.DebugKind, jobId);
                response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
            } catch (Exception e) {
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                DebugLog.error(String.format("debugId:%s run failed", jobId), e);
            }
            boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            if (!success) {
                exception = new HeraException(String.format("fileId:%s run failed ", history.getFileId()), exception);
                DebugLog.info("debug job error");
                history = masterContext.getHeraDebugHistoryService().findById(jobId);
                HeraDebugFailEvent failEvent = HeraDebugFailEvent.builder()
                        .debugHistory(BeanConvertUtils.convert(history))
                        .throwable(exception)
                        .fileId(history.getFileId())
                        .build();
                masterContext.getDispatcher().forwardEvent(failEvent);
            } else {
                DebugLog.info("debug success");
                HeraDebugSuccessEvent successEvent = HeraDebugSuccessEvent.builder()
                        .fileId(history.getFileId())
                        .history(BeanConvertUtils.convert(history))
                        .build();
                masterContext.getDispatcher().forwardEvent(successEvent);
            }
        });
    }

    /**
     * 获取hostGroupId中可以分发任务的worker
     *
     * @param jobElement
     * @return
     */
    private MasterWorkHolder getRunnableWork(JobElement jobElement) {

        int hostGroupId = jobElement == null ? HeraGlobalEnvironment.defaultWorkerGroup : jobElement.getHostGroupId();
        IStrategyWorker chooseWorkerStrategy = StrategyWorkerFactory.getStrategyWorker(StrategyWorkerEnum.FIRST);
        return chooseWorkerStrategy.chooseWorker(hostGroupId, masterContext);
    }

    public void debug(HeraDebugHistoryVo debugHistory) {
        JobElement element = JobElement.builder()
                .jobId(debugHistory.getId())
                .hostGroupId(debugHistory.getHostGroupId())
                .build();
        debugHistory.setStatus(StatusEnum.RUNNING);
        debugHistory.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
    public void run(HeraJobHistoryVo heraJobHistory) {
        String actionId = heraJobHistory.getActionId();
        int priorityLevel = 3;
        HeraActionVo heraJobVo = BeanConvertUtils.convert(masterContext.getHeraJobActionService().findById(actionId)).getSource();
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
        //重复job检测
        if (checkJobExists(heraJobHistory)) {
            return;
        }
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
        heraJobHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "进入任务队列");
        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
    }


    private boolean checkJobExists(HeraJobHistoryVo heraJobHistory) {
        String actionId = heraJobHistory.getActionId();
        String historyId = heraJobHistory.getId();
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER) {
            /**
             *  check调度器等待队列是否有此任务在排队
             */
            for (JobElement jobElement : new ArrayList<>(masterContext.getScheduleQueue())) {
                if (jobElement.getJobId().equals(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setIllustrate(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    return true;
                }
            }
            /**
             *  check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
             */
            for (Channel key : masterContext.getWorkMap().keySet()) {
                MasterWorkHolder workHolder = masterContext.getWorkMap().get(key);
                if (workHolder.getRunning().containsKey(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().localAddress());
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setIllustrate(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    return true;

                }
            }


        } else if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {

            for (JobElement jobElement : masterContext.getManualQueue()) {
                if (jobElement.getJobId().equals(historyId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setIllustrate(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    return true;
                }
            }

            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getManningRunning().containsKey(historyId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().localAddress());
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    heraJobHistory.setIllustrate(LogConstant.CHECK_QUEUE_LOG);
                    masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * work断开的处理
     *
     * @param channel channel
     */
    public void workerDisconnectProcess(Channel channel) {
        String ip = getIpFromChannel(channel);
        SocketLog.error("work:{}断线", ip);
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        masterContext.getWorkMap().remove(channel);
        if (workHolder != null) {
            List<String> scheduleTask = workHolder.getHeartBeatInfo().getRunning();

            if (scheduleTask == null || scheduleTask.size() == 0) {
                return;
            }
            //十分钟后开始检查 work是否重连成功
            masterContext.masterSchedule.schedule(() -> {
                try {
                    Channel newChannel = null;
                    HeraAction heraAction;
                    HeraJobHistory heraJobHistory;
                    //遍历新的心跳信息 匹配断线ip是否重新连接
                    Set<Channel> channels = masterContext.getWorkMap().keySet();
                    for (Channel cha : channels) {
                        if (getIpFromChannel(cha).equals(ip)) {
                            newChannel = cha;
                            break;
                        }
                    }

                    if (newChannel != null) {
                        SocketLog.warn("work重连成功:{}", newChannel.remoteAddress());
                        // 判断任务状态 无论是否成功，全部重新广播一遍

                        for (String action : scheduleTask) {
                            heraAction = masterContext.getHeraJobActionService().findById(action);
                            //检测action表是否已经更新 如果更新 证明work的成功信号发送给了master已经广播
                            if (StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                                SocketLog.warn("任务{}已经执行完成并发信号给master，无需重试", action);
                                continue;
                            }
                            heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                            //如果work已经运行成功但是成功信号没有发送给master master做一次广播
                            if (StatusEnum.SUCCESS.toString().equals(heraJobHistory.getStatus())) {
                                HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(action, TriggerTypeEnum.parser(heraJobHistory.getTriggerType())
                                        , heraJobHistory.getId());
                                heraAction.setStatus(heraJobHistory.getStatus());
                                masterContext.getHeraJobActionService().updateStatus(heraAction);
                                SocketLog.warn("任务{}已经执行完成但是信号未发送给master,手动广播成功事件", action);
                                //成功时间广播
                                masterContext.getDispatcher().forwardEvent(successEvent);
                            } else if (StatusEnum.FAILED.toString().equals(heraJobHistory.getStatus())) {

                                SocketLog.warn("任务{}执行失败，但是丢失重试次数，重新调度", action);
                                //丢失重试次数信息   master直接重试
                                heraJobHistory.setIllustrate("work断线，丢失任务重试次数，重新执行该任务");
                                startNewJob(heraJobHistory);
                            } else if (StatusEnum.RUNNING.toString().equals(heraJobHistory.getStatus())) {
                                //如果仍然在运行中，那么检测新的心跳信息 判断work是断线重连 or 重启
                                HeartBeatInfo newBeatInfo = masterContext.getWorkMap().get(newChannel).getHeartBeatInfo();
                                if (newBeatInfo == null) {
                                    TimeUnit.SECONDS.sleep(HeraGlobalEnvironment.getHeartBeat() * 2);

                                    newBeatInfo = masterContext.getWorkMap().get(newChannel).getHeartBeatInfo();
                                }
                                if (newBeatInfo != null) {
                                    List<String> newRunning = newBeatInfo.getRunning();
                                    //如果work新的心跳信息 包含该任务的信息 work继续执行即可
                                    if (newRunning.contains(action)) {
                                        SocketLog.warn("任务{}还在运行中，并且work重连后心跳信息存在，等待work执行完成", action);
                                        continue;
                                    }
                                }
                                heraJobHistory.setIllustrate("work心跳该任务信息为空，重新执行该任务");
                                SocketLog.warn("任务{}还在运行中，但是work已经无该任务的相关信息，重新调度该任务", action);
                                //不包含该任务信息，重新调度
                                startNewJob(heraJobHistory);
                            }
                        }
                    } else {
                        for (String action : scheduleTask) {
                            heraAction = masterContext.getHeraJobActionService().findById(action);
                            heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                            heraJobHistory.setIllustrate("work断线超出十分钟，重新执行该任务");
                            SocketLog.warn("work断线并且未重连，重新调度任务{}", action);
                            startNewJob(heraJobHistory);
                        }
                    }
                } catch (InterruptedException e) {
                    SocketLog.error("work断线任务检测异常{}", e);
                }
            }, 10, TimeUnit.MINUTES);

            String content = "不幸的消息，work宕机了:" + channel.remoteAddress() + "<br>" +
                    "自动调度队列任务：" + workHolder.getHeartBeatInfo().getRunning() + "<br>" +
                    "手动队列任务：" + workHolder.getHeartBeatInfo().getManualRunning() + "<br>" +
                    "开发中心队列任务：" + workHolder.getHeartBeatInfo().getDebugRunning() + "<br>";
            SocketLog.error(content);
        }

    }

    private void startNewJob(HeraJobHistory heraJobHistory) {
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        masterContext.getHeraJobHistoryService().update(heraJobHistory);
        HeraJobHistory newHistory = HeraJobHistory.builder().
                actionId(heraJobHistory.getActionId()).
                illustrate(LogConstant.RETRY_JOB).
                jobId(heraJobHistory.getJobId()).
                triggerType(heraJobHistory.getTriggerType()).
                operator(heraJobHistory.getOperator()).
                hostGroupId(heraJobHistory.getHostGroupId()).
                log(heraJobHistory.getIllustrate()).build();
        masterContext.getHeraJobHistoryService().insert(newHistory);
        this.run(BeanConvertUtils.convert(newHistory));
    }

    private String getIpFromChannel(Channel channel) {
        return channel.remoteAddress().toString().split(":")[0];
    }

}
