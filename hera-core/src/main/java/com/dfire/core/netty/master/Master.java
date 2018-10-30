package com.dfire.core.netty.master;


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
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.WorkerRouter;
import com.dfire.core.route.factory.DefaultWorkerRouterFactory;
import com.dfire.core.util.CronParse;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcResponse;
import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;

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
@Slf4j
public class Master {

    private MasterContext masterContext;
    private Map<Long, HeraAction> heraActionMap;
    private ThreadPoolExecutor executeJobPool;
    private final Integer DELAY_TIME = 1;
    private final Integer MAX_DELAY_TIME = 10;

    public Master(final MasterContext masterContext) {

        this.masterContext = masterContext;
        heraActionMap = new HashMap<>();
        executeJobPool = new ThreadPoolExecutor(HeraGlobalEnvironment.getMaxParallelNum(), HeraGlobalEnvironment.getMaxParallelNum(), 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("EXECUTE_JOB"), new ThreadPoolExecutor.AbortPolicy());
        executeJobPool.allowCoreThreadTimeOut(true);
        String exeEnvironment = "pre";
        if (HeraGlobalEnvironment.getEnv().equalsIgnoreCase(exeEnvironment)) {
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
        masterContext.setMaster(this);
        masterContext.refreshHostGroupCache();
        log.info("refresh hostGroup cache");


        TimerTask generateActionTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                try {
                    generateBatchAction();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    masterContext.masterTimer.newTimeout(this, 1, TimeUnit.HOURS);
                }
            }
        };
        //延迟加载  避免定时调度未启动
        masterContext.masterTimer.newTimeout(generateActionTask, 20, TimeUnit.SECONDS);

        /**
         * 扫描任务等待队列，可获得worker的任务将执行
         * 对于没有科运行机器的时候，自动调度任务将offer到exception队列，manual,debug任务重新offer到原队列
         *
         */
        TimerTask scanWaitingQueueTask = new TimerTask() {
            Integer nextTime = HeraGlobalEnvironment.getScanRate();

            @Override
            public void run(Timeout timeout) {
                try {
                    if (scan()) {
                        nextTime = HeraGlobalEnvironment.getScanRate();
                    } else {
                        nextTime = (nextTime + DELAY_TIME) > MAX_DELAY_TIME ? MAX_DELAY_TIME : nextTime + DELAY_TIME;
                    }
                    log.info("scan waiting queueTask run");
                } catch (Exception e) {
                    log.error("scan waiting queueTask exception");
                } finally {
                    masterContext.masterTimer.newTimeout(this, nextTime, TimeUnit.SECONDS);
                }
            }
        };
        masterContext.masterTimer.newTimeout(scanWaitingQueueTask, 3, TimeUnit.SECONDS);


        //定时检测work心跳是否超时
        TimerTask checkHeartBeatTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
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
                masterContext.masterTimer.newTimeout(this, 1, TimeUnit.MINUTES);
            }
        };
        masterContext.masterTimer.newTimeout(checkHeartBeatTask, 20, TimeUnit.SECONDS);
    }

    /**
     * 漏泡检测，清理schedule线程，1小时调度一次,超过15分钟，job开始检测漏泡
     */
    private void lostJobCheck() {
        log.info("refresh host group success,start clear schedule");
        masterContext.refreshHostGroupCache();
        String currDate = DateUtil.getNowStringForAction();

        Dispatcher dispatcher = masterContext.getDispatcher();
        if (dispatcher != null) {
            Map<Long, HeraAction> actionMapNew = heraActionMap;
            Long tmp = Long.parseLong(currDate) - 15000000;
            if (actionMapNew != null && actionMapNew.size() > 0) {
                List<Long> actionIdList = new ArrayList<>();
                for (Long actionId : actionMapNew.keySet()) {
                    if (actionId < tmp) {
                        rollBackLostJob(actionId, actionMapNew, actionIdList);
                    }
                }
                log.info("roll back action count:" + actionIdList.size());
            }
            log.info("clear job scheduler ok");
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    private void removeJob() {
        log.warn("开始进行版本清理");
        Dispatcher dispatcher = masterContext.getDispatcher();
        Long currDate = Long.parseLong(DateUtil.getNowStringForAction());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd0000000000");
        Long nextDay = Long.parseLong(simpleDateFormat.format(calendar.getTime()));
        Long tmp = currDate - 15000000;
        Map<Long, HeraAction> actionMapNew = heraActionMap;
        //移除未生成的调度
        List<AbstractHandler> handlers = dispatcher.getJobHandlers();
        List<JobHandler> shouldRemove = new ArrayList<>();
        if (handlers != null && handlers.size() > 0) {
            handlers.forEach(handler -> {
                JobHandler jobHandler = (JobHandler) handler;
                String actionId = jobHandler.getActionId();
                Long aid = Long.parseLong(actionId);
                if (Long.parseLong(actionId) < tmp) {
                    masterContext.getQuartzSchedulerService().deleteJob(actionId);
                } else if (aid >= currDate && aid < nextDay) {
                    if (!actionMapNew.containsKey(aid)) {
                        masterContext.getQuartzSchedulerService().deleteJob(actionId);
                        masterContext.getHeraJobActionService().delete(actionId);
                    }
                }
                if (!DateUtil.isToday(actionId)) {
                    shouldRemove.add(jobHandler);
                }
            });
        }
        //移除 过期 失效的handler
        shouldRemove.forEach(dispatcher::removeJobHandler);
        log.warn("版本清理完成");
    }

    public void generateBatchAction() {
        log.info("全量任务版本生成");
        generateAction(false, null);
    }

    public boolean generateSingleAction(Integer jobId) {
        log.info("单个任务版本生成：{}", jobId);
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
            //进行漏跑检测 + 清理
            lostJobCheck();
            log.info("generate all action success");
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
                        log.error("cron parse error,cron = " + cron);
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
            log.warn("重试ID:{}, 未找到版本个数:{} , 重试次数:{}", retryId, noCompleteCount, retryCount);
        }
    }


    private void rollBackLostJob(Long actionId, Map<Long, HeraAction> actionMapNew, List<Long> actionIdList) {
        HeraAction lostJob = actionMapNew.get(actionId);
        if (lostJob != null) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(","));
                boolean isAllComplete = true;
                if (jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        Long jobDep = Long.parseLong(jobDepend);
                        if (actionMapNew.get(jobDep) != null) {
                            HeraAction action = actionMapNew.get(jobDep);
                            if (action != null) {
                                String status = action.getStatus();
                                if (status == null || status.equals("wait")) {
                                    isAllComplete = false;
                                } else if (status.equals(StatusEnum.FAILED.toString())) {
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
            } else { //独立任务情况
                if (!actionIdList.contains(actionId)) {
                    masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId.toString()));
                    actionIdList.add(actionId);
                    log.info("roll back lost actionId :" + actionId);
                }
            }
        }
    }

    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public boolean scan() {
        boolean hasTask = false;
        if (!masterContext.getScheduleQueue().isEmpty()) {
            log.warn("schedule队列任务：{}", masterContext.getScheduleQueue());
            printThreadPoolLog();
            JobElement jobElement = masterContext.getScheduleQueue().peek();

            MasterWorkHolder workHolder = getRunnableWork(jobElement);
            if (workHolder == null) {
                log.warn("can not get work to execute job in master");
            } else {
                jobElement = masterContext.getScheduleQueue().poll();
                runScheduleJob(workHolder, jobElement.getJobId());
                hasTask = true;
            }
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            log.warn("manual队列任务：{}", masterContext.getManualQueue());
            printThreadPoolLog();
            JobElement element = masterContext.getScheduleQueue().peek();
            MasterWorkHolder selectWork = getRunnableWork(element);
            if (selectWork == null) {
                log.warn("can not get work to execute job in master");
            } else {
                element = masterContext.getManualQueue().poll();
                runManualJob(selectWork, element.getJobId());
                hasTask = true;
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            log.warn("debug队列任务：{}", masterContext.getDebugQueue());
            printThreadPoolLog();
            JobElement element = masterContext.getScheduleQueue().peek();
            MasterWorkHolder selectWork = getRunnableWork(element);
            if (selectWork == null) {
                log.warn("can not get work to execute job in master");
            } else {
                element = masterContext.getDebugQueue().poll();
                runDebugJob(selectWork, element.getJobId());
                hasTask = true;
            }
        }
        return hasTask;

    }

    private void printThreadPoolLog() {
        StringBuilder sb = new StringBuilder("当前线程池信息");
        sb.append("[ActiveCount: ").append(executeJobPool.getActiveCount()).append(",");
        sb.append("CompletedTaskCount：").append(executeJobPool.getCompletedTaskCount()).append(",");
        sb.append("PoolSize:").append(executeJobPool.getPoolSize()).append(",");
        sb.append("LargestPoolSize:").append(executeJobPool.getLargestPoolSize()).append(",");
        sb.append("TaskCount:").append(executeJobPool.getTaskCount()).append("]");
        log.warn(sb.toString());
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
                future.cancel(true);
                response = null;
                log.error("manual job run error" + historyId, e);
                jobStatus.setStatus(StatusEnum.FAILED);
                history.setStatus(jobStatus.getStatus().toString());
                masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(history);

            }
            boolean success = response != null && response.getStatusEnum() != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            log.info("historyId 执行结果" + historyId + "---->" + response.getStatusEnum());

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

        log.debug("重试次数：{},重试时间：{},actionId:{}", retryCount, retryWaitTime, actionId);
        runCount++;
        boolean isCancelJob = false;
        if (runCount > 1) {
            log.debug("任务重试，睡眠：{}秒", retryWaitTime);
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
            response = null;
            log.error("schedule job run error :" + actionId, e);
            future.cancel(true);
            jobStatus.setStatus(StatusEnum.FAILED);
            heraJobHistoryVo.setStatusEnum(jobStatus.getStatus());
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        }
        boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
        log.debug("job_id 执行结果" + actionId + "---->" + response.getStatusEnum());

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
        masterContext.getHeraJobActionService().updateStatus(jobStatus);
        if (runCount < (retryCount + 1) && !success && !isCancelJob) {
            log.debug("--------------------------失败任务，准备重试--------------------------");
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
                future.cancel(true);
                response = null;
                log.error(String.format("debugId:%s run failed", jobId), e);
            }
            boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
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
        WorkerRouter workerRouter = new DefaultWorkerRouterFactory().newWorkerRouter("first");
        MasterWorkHolder workHolder = workerRouter.selectWorker(hostGroupId, masterContext);
        return workHolder;
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
    public HeraJobHistoryVo run(HeraJobHistoryVo heraJobHistory) {
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
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER) {

            /**
             *  check调度器等待队列是否有此任务在排队
             *
             */
            for (JobElement jobElement : new ArrayList<>(masterContext.getScheduleQueue())) {
                if (jobElement.getJobId().equals(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    break;
                }
            }
            for (JobElement jobElement : new ArrayList<>(masterContext.getManualQueue())) {
                if (jobElement.getJobId().equals(actionId)) {
                    heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG);
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    break;
                }
            }

            /**
             *  check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
             *
             */
            for (Channel key : masterContext.getWorkMap().keySet()) {
                MasterWorkHolder workHolder = masterContext.getWorkMap().get(key);
                if (workHolder.getRunning().containsKey(actionId)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(LogConstant.CHECK_QUEUE_LOG).append("执行worker ip " + workHolder.getChannel().localAddress());
                    heraJobHistory.getLog().append(sb.toString());
                    heraJobHistory.setStartTime(new Date());
                    heraJobHistory.setEndTime(new Date());
                    heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                    break;
                }
            }

            for (Channel key : masterContext.getWorkMap().keySet()) {
                MasterWorkHolder workHolder = masterContext.getWorkMap().get(key);
                if (workHolder.getManningRunning().containsKey(actionId)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(LogConstant.CHECK_MANUAL_QUEUE_LOG).append("执行worker ip " + workHolder.getChannel().localAddress());
                    heraJobHistory.getLog().append(sb.toString());
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

    /**
     * work断开的处理
     *
     * @param channel
     */
    public void workerDisconnectProcess(Channel channel) {

        String ip = channel.remoteAddress().toString().split(":")[0];
        log.error("work:{}断线", ip);
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        masterContext.getWorkMap().remove(channel);

        if (workHolder != null) {
            List<String> scheduleTask = workHolder.getHeartBeatInfo().getRunning();
            //十分钟后开始检查 work是否重连成功
            masterContext.masterTimer.newTimeout((x) -> {
                Channel newChannel = null;
                //遍历新的心跳信息 匹配断线ip是否重新连接
                Set<Channel> channels = masterContext.getWorkMap().keySet();
                for (Channel cha : channels) {
                    if (cha.remoteAddress().toString().split(":")[0].equals(ip)) {
                        newChannel = cha;
                        break;
                    }
                }

                if (newChannel != null) {
                    log.warn("work重连成功:{}", newChannel.remoteAddress());
                    // 判断任务状态 无论是否成功，全部重新广播一遍
                    HeraAction heraAction;
                    HeraJobHistory heraJobHistory;
                    for (String action : scheduleTask) {
                        heraAction = masterContext.getHeraJobActionService().findById(action);
                        //检测action表是否已经更新 如果更新 证明work的成功信号发送给了master已经广播
                        if (StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                            continue;
                        }
                        heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                        //如果work已经运行成功但是成功信号没有发送给master master做一次广播
                        if (StatusEnum.SUCCESS.toString().equals(heraJobHistory.getStatus())) {
                            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(action, TriggerTypeEnum.parser(heraJobHistory.getTriggerType())
                                    , heraJobHistory.getId());
                            heraAction.setStatus(heraJobHistory.getStatus());
                            masterContext.getHeraJobActionService().updateStatus(heraAction);
                            //成功时间广播
                            masterContext.getDispatcher().forwardEvent(successEvent);
                        } else if (StatusEnum.FAILED.toString().equals(heraJobHistory.getStatus())) {
                            //丢失重试次数信息   master直接重试
                            this.run(BeanConvertUtils.convert(heraJobHistory));
                        } else if (StatusEnum.RUNNING.toString().equals(heraJobHistory.getStatus())){

                        }

                    }

                } else {


                }

            }, 10, TimeUnit.MINUTES);

        }
        StringBuilder content = new StringBuilder();
        content.append("不幸的消息，work宕机了:").append(channel.remoteAddress()).append("<br>");
        content.append("自动调度队列任务：").append(workHolder.getHeartBeatInfo().getRunning()).append("<br>");
        content.append("手动队列任务：").append(workHolder.getHeartBeatInfo().getManualRunning()).append("<br>");
        content.append("开发中心队列任务：").append(workHolder.getHeartBeatInfo().getDebugRunning()).append("<br>");
        log.error(content.toString());


    }

}
