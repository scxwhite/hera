package com.dfire.core.netty.master;


import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.kv.Tuple;
import com.dfire.common.util.*;
import com.dfire.core.HeraException;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.event.*;
import com.dfire.core.event.base.ApplicationEvent;
import com.dfire.core.event.base.Events;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.core.netty.master.response.MasterExecuteJob;
import com.dfire.core.queue.JobElement;
import com.dfire.core.route.factory.StrategyWorkerEnum;
import com.dfire.core.route.factory.StrategyWorkerFactory;
import com.dfire.core.route.strategy.IStrategyWorker;
import com.dfire.core.util.CronParse;
import com.dfire.logs.*;
import com.dfire.protocol.JobExecuteKind;
import com.dfire.protocol.ResponseStatus;
import com.dfire.protocol.RpcResponse;
import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
    @Getter
    private Map<Long, HeraAction> heraActionMap = new HashMap<>();
    private ThreadPoolExecutor executeJobPool;

    private volatile boolean isGenerateActioning = false;
    private IStrategyWorker chooseWorkerStrategy;


    public void init(MasterContext masterContext) {
        this.masterContext = masterContext;
        chooseWorkerStrategy = StrategyWorkerFactory.getStrategyWorker(StrategyWorkerEnum.FIRST);
        executeJobPool = new ThreadPoolExecutor(HeraGlobalEnvironment.getMaxParallelNum(), HeraGlobalEnvironment.getMaxParallelNum(), 10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE), new NamedThreadFactory("master-execute-job-thread"), new ThreadPoolExecutor.AbortPolicy());
        executeJobPool.allowCoreThreadTimeOut(true);
        if (HeraGlobalEnvironment.getEnv().equalsIgnoreCase(Constants.PRE_ENV)) {
            masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }
        executeJobPool.execute(() -> {
            HeraLog.info("-----------------------------init action,time: {}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));
            List<HeraAction> allJobList = masterContext.getHeraJobActionService().getTodayAction();
            HeraLog.info("-----------------------------action size:{}, time {}-----------------------------", allJobList.size(), System.currentTimeMillis());
            heraActionMap = new HashMap<>(allJobList.size());
            allJobList.forEach(heraAction -> {
                masterContext.getDispatcher().
                        addJobHandler(new JobHandler(heraAction.getId().toString(), this, masterContext));
                heraActionMap.put(heraAction.getId(), heraAction);
            });
            HeraLog.info("-----------------------------add actions to handler success, time:{}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().forwardEvent(Events.Initialize);
            HeraLog.info("-----------------------------dispatcher actions success, time{}-----------------------------", System.currentTimeMillis());
        });
        masterContext.refreshHostGroupCache();
        HeraLog.info("refresh hostGroup cache");
        // 1.生成版本
        batchActionCheck();
        // 2.扫描任务
        waitingQueueCheck();
        // 3.心跳检查
        heartCheck();
        // 4.漏跑检测
        lostJobCheck();

    }

    /**
     * 版本定时生成
     */
    private void batchActionCheck() {
        //启动的时候生成版本
        masterContext.masterSchedule.schedule(() -> {
            try {
                generateBatchAction();
                clearInvalidAction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, TimeUnit.SECONDS);

        //只在整点生成版本
        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            try {
                generateBatchAction();
                if (DateTime.now().getHourOfDay() == MasterConstant.MORNING_TIME) {
                    clearInvalidAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 60 - new DateTime().getMinuteOfHour(), 60, TimeUnit.MINUTES);
    }


    /**
     * 漏泡检测，清理schedule线程，30分钟调度一次
     * 信号丢失检测
     * job开始检测15分钟之前的漏跑任务
     */
    private void lostJobCheck() {
        int minute = new DateTime().getMinuteOfHour();
        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            ScheduleLog.info("refresh host group success, start roll back");
            masterContext.refreshHostGroupCache();
            String currDate = ActionUtil.getCurrActionVersion();
            Dispatcher dispatcher = masterContext.getDispatcher();
            if (dispatcher != null) {
                Map<Long, HeraAction> actionMapNew = heraActionMap;
                if (actionMapNew != null && actionMapNew.size() > 0) {
                    List<Long> actionIdList = new ArrayList<>();
                    Long tmp = Long.parseLong(currDate) - MasterConstant.PRE_CHECK_MIN;
                    for (Long actionId : actionMapNew.keySet()) {
                        if (actionId < tmp) {
                            rollBackLostJob(actionId, actionMapNew, actionIdList);
                            checkLostSingle(actionId, actionMapNew);
                        }
                    }
                    ScheduleLog.info("roll back action count:" + actionIdList.size());
                }
                ScheduleLog.info("clear job scheduler ok");
            }
        }, minute <= 30 ? 40 - minute : 70 - minute, 30, TimeUnit.MINUTES);

    }

    /**
     * 漏跑检测
     *
     * @param actionId     版本id
     * @param actionMapNew actionMap集合
     * @param actionIdList 重跑的actionId
     */

    private void rollBackLostJob(Long actionId, Map<Long, HeraAction> actionMapNew, List<Long> actionIdList) {
        HeraAction lostJob = actionMapNew.get(actionId);
        boolean isCheck = lostJob != null
                && lostJob.getAuto() == 1
                && lostJob.getStatus() == null;
        if (isCheck) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(","));
                boolean isAllComplete = false;
                HeraAction heraAction;
                if (jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        heraAction = actionMapNew.get(Long.parseLong(jobDepend));
                        if (heraAction != null) {
                            if (!(isAllComplete = Constants.STATUS_SUCCESS.equals(heraAction.getStatus()))) {
                                break;
                            }
                        }
                    }
                }
                if (isAllComplete) {
                    addRollBackJob(actionIdList, actionId);
                }
            } else { //独立任务情况
                addRollBackJob(actionIdList, actionId);
            }
        }
    }

    /**
     * 信号丢失处理
     *
     * @param actionId     hera_action 表信息id /版本id
     * @param actionMapNew hera_action 内存信息 /内存保存的今天版本信息
     */
    private void checkLostSingle(Long actionId, Map<Long, HeraAction> actionMapNew) {
        try {
            HeraAction checkJob = actionMapNew.get(actionId);
            if (checkJob == null) {
                return;
            }
            if (Constants.STATUS_RUNNING.equals(checkJob.getStatus())) {
                HeraJobHistory actionHistory = masterContext.getHeraJobHistoryService().findById(checkJob.getHistoryId());
                if (actionHistory == null) {
                    return;
                }
                if (actionHistory.getStatus() != null && !actionHistory.getStatus().equals(Constants.STATUS_RUNNING)) {
                    masterContext.getMasterSchedule().schedule(() -> {
                        HeraAction newAction = masterContext.getHeraJobActionService().findById(String.valueOf(actionId));
                        if (Constants.STATUS_RUNNING.equals(newAction.getStatus())) {
                            ErrorLog.error("任务信号丢失actionId:{},historyId:{}", actionId, newAction.getHistoryId());
                            Integer jobId = ActionUtil.getJobId(String.valueOf(actionId));
                            boolean scheduleType = actionHistory.getTriggerType().equals(TriggerTypeEnum.SCHEDULE.getId())
                                    || actionHistory.getTriggerType().equals(TriggerTypeEnum.MANUAL_RECOVER.getId());
                            //TODO 可以选择重跑 or 广播 + 设置状态 这里偷懒 直接重跑
                            masterContext.getWorkMap().values().forEach(workHolder -> {
                                if (scheduleType) {
                                    workHolder.getRunning().remove(jobId);
                                } else {
                                    workHolder.getManningRunning().remove(jobId);
                                }
                            });
                            startNewJob(actionHistory, "任务信号丢失重试");
                        }

                    }, 1, TimeUnit.MINUTES);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addRollBackJob(List<Long> actionIdList, Long actionId) {
        String actionStr = String.valueOf(actionId);
        if (!actionIdList.contains(actionId) &&
                !checkJobExists(HeraJobHistoryVo
                        .builder()
                        .actionId(actionStr)
                        .triggerType(TriggerTypeEnum.SCHEDULE)
                        .jobId((ActionUtil.getJobId(actionStr)))
                        .build(), true)) {
            masterContext.getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionStr));
            actionIdList.add(actionId);
            ScheduleLog.info("roll back lost actionId :" + actionId);
        }
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
                } catch (Exception e) {
                    ScanLog.error("scan waiting queueTask exception", e);
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

        masterContext.masterSchedule.scheduleAtFixedRate(() -> {
            Date now = new Date();
            Map<Channel, MasterWorkHolder> workMap = masterContext.getWorkMap();
            List<Channel> removeChannel = new ArrayList<>(workMap.size());
            for (Channel channel : workMap.keySet()) {
                MasterWorkHolder workHolder = workMap.get(channel);
                if (workHolder.getHeartBeatInfo() == null) {
                    continue;
                }
                Long workTime = workHolder.getHeartBeatInfo().getTimestamp();
                if (workTime == null || now.getTime() - workTime > 1000 * 60L) {
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

    public boolean generateBatchAction() {
        ScheduleLog.info("全量任务版本生成");
        long begin = System.currentTimeMillis();
        boolean flag = generateAction(false, null);
        ScheduleLog.info("生成版本时间:" + (System.currentTimeMillis() - begin) + " ms");
        return flag;
    }

    private boolean generateAction(boolean isSingle, Integer jobId) {
        try {
            if (isGenerateActioning) {
                return true;
            }
            DateTime dateTime = new DateTime();
            Date now = dateTime.toDate();
            int executeHour = dateTime.getHourOfDay();
            //凌晨生成版本，早上七点以后开始再次生成版本
            boolean execute = executeHour == 0 || (executeHour > ActionUtil.ACTION_CREATE_MIN_HOUR && executeHour <= ActionUtil.ACTION_CREATE_MAX_HOUR);
            if (execute || isSingle) {
                String currString = ActionUtil.getCurrActionVersion();
                Long nowAction = Long.parseLong(currString);
                if (executeHour == ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    Tuple<String, Date> nextDayString = ActionUtil.getNextDayString();
                    //例如：今天 2018.07.17 23:50  currString = 201807180000000000 now = 2018.07.18 23:50
                    currString = nextDayString.getSource();
                    now = nextDayString.getTarget();
                }
                Map<Long, HeraAction> actionMap = new HashMap<>(heraActionMap.size());
                List<HeraJob> jobList = new ArrayList<>();
                //批量生成
                if (!isSingle) {
                    isGenerateActioning = true;
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
                String cronDate = ActionUtil.getActionVersionByTime(now);
                generateScheduleJobAction(jobList, cronDate, actionMap, nowAction);
                generateDependJobAction(jobList, actionMap, 0, nowAction, new HashSet<>());

                if (executeHour < ActionUtil.ACTION_CREATE_MAX_HOUR) {
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
                ScheduleLog.info("[单个任务:{}，任务id:{}]generate action success", isSingle, jobId);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isGenerateActioning = false;
        }
        return false;
    }

    /**
     * hera自动调度任务版本生成，版本id 18位当前时间 + actionId
     *
     * @param jobList   jobList
     * @param actionMap cronDate
     */
    public void generateScheduleJobAction(List<HeraJob> jobList, String cronDate, Map<Long, HeraAction> actionMap, Long nowAction) {
        List<HeraAction> insertActionList = new ArrayList<>();
        for (HeraJob heraJob : jobList) {
            if (heraJob.getScheduleType() != null && heraJob.getScheduleType() == 0) {
                String cron = heraJob.getCronExpression();
                List<String> list = new ArrayList<>();
                if (StringUtils.isNotBlank(cron)) {
                    boolean isCronExp = CronParse.Parser(cron, cronDate, list);
                    if (!isCronExp) {
                        ErrorLog.error("cron parse error,jobId={},cron = {}", heraJob.getId(), cron);
                        continue;
                    }
                    insertActionList.addAll(createHeraAction(list, heraJob));
                }
            }
        }
        batchInsertList(insertActionList, actionMap, nowAction);

    }


    /**
     * 批量插入
     *
     * @param insertActionList 要插入/更新的hera_action 集合
     */
    private void batchInsertList(List<HeraAction> insertActionList, Map<Long, HeraAction> actionMap, Long nowAction) {
        // 每次批量的条数
        int maxSize = insertActionList.size();
        int batchNum = 500;
        int step = batchNum > maxSize ? maxSize : batchNum;
        if (maxSize != 0) {
            for (int i = 0; i < maxSize; i = i + batchNum) {
                List<HeraAction> insertList;
                if ((step + batchNum) > maxSize) {
                    insertList = insertActionList.subList(i, step);
                    step = maxSize;
                } else {
                    insertList = insertActionList.subList(i, step);
                    step = step + batchNum;
                }
                masterContext.getHeraJobActionService().batchInsert(insertList, nowAction);
                for (HeraAction action : insertList) {
                    actionMap.put(action.getId(), action);
                }

            }
        }
    }


    /**
     * 生成action
     *
     * @param list    表格cronTab 表达式，对应多了时间点的版本集合
     * @param heraJob hera_job 表对象
     * @return 更新后的action 信息，保存到内存
     */
    private List<HeraAction> createHeraAction(List<String> list, HeraJob heraJob) {
        List<HeraAction> heraActionList = new ArrayList<>();
        for (String str : list) {
            String actionDate = HeraDateTool.StringToDateStr(str, ActionUtil.DEFAULT_FORMAT, ActionUtil.ACTION_MIN);
            String actionCron = HeraDateTool.StringToDateStr(str, ActionUtil.DEFAULT_FORMAT, ActionUtil.ACTION_CRON) + " ?";
            HeraAction heraAction = new HeraAction();
            BeanUtils.copyProperties(heraJob, heraAction);
            Long actionId = Long.parseLong(actionDate) * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
            heraAction.setId(actionId);
            heraAction.setCronExpression(actionCron);
            heraAction.setGmtCreate(new Date());
            heraAction.setJobId(heraJob.getId());
            heraAction.setHistoryId(heraJob.getHistoryId());
            heraAction.setAuto(heraJob.getAuto());
            heraAction.setGmtModified(new Date());
            heraAction.setJobDependencies(null);
            heraAction.setDependencies(null);
            heraAction.setReadyDependency(null);
            heraAction.setHostGroupId(heraJob.getHostGroupId());
            heraActionList.add(heraAction);
        }
        return heraActionList;
    }


    private void clearInvalidAction() {
        ScheduleLog.warn("开始进行版本清理");
        Dispatcher dispatcher = masterContext.getDispatcher();
        Long currDate = ActionUtil.getLongCurrActionVersion();
        Long nextDay = ActionUtil.getLongNextDayActionVersion();
        Long preCheckTime = currDate - MasterConstant.PRE_CHECK_MIN;

        Map<Long, HeraAction> actionMapNew = heraActionMap;
        //移除未生成的调度
        List<AbstractHandler> handlers = dispatcher.getJobHandlers();
        List<JobHandler> shouldRemove = new ArrayList<>();
        if (handlers != null && handlers.size() > 0) {
            handlers.forEach(handler -> {
                JobHandler jobHandler = (JobHandler) handler;
                String actionId = jobHandler.getActionId();
                Long aid = Long.parseLong(actionId);
                if (Long.parseLong(actionId) < preCheckTime) {
                    masterContext.getQuartzSchedulerService().deleteJob(actionId);
                } else if (aid >= currDate && aid < nextDay) {
                    if (!actionMapNew.containsKey(aid)) {
                        masterContext.getQuartzSchedulerService().deleteJob(actionId);
                        masterContext.getHeraJobActionService().delete(actionId);
                    }
                }
                //移除非今天版本的订阅者
                if (!ActionUtil.isInitActionVersion(actionId)) {
                    shouldRemove.add(jobHandler);
                }
            });
        }
        //移除 过期 失效的handler
        shouldRemove.forEach(dispatcher::removeJobHandler);
        ScheduleLog.warn("版本清理完成");
    }

    /**
     * hera 依赖任务版本生成
     *
     * @param jobList    hera_job 表集合
     * @param actionMap  内存本本状态
     * @param retryCount 循环依赖
     */
    public void generateDependJobAction(List<HeraJob> jobList, Map<Long, HeraAction> actionMap, int retryCount, Long nowAction, Set<Integer> ids) {
        retryCount++;
        int noCompleteCount = 0;
        //最大递归次数
        int maxRetryCount = 80;
        List<Integer> notGenerate = new ArrayList<>();
        List<HeraAction> insertActionList = new ArrayList<>();
        for (HeraJob heraJob : jobList) {
            //依赖任务生成版本
            if (!ids.contains(heraJob.getId()) && heraJob.getScheduleType() != null && heraJob.getScheduleType() == 1) {
                String jobDependencies = heraJob.getDependencies();
                if (StringUtils.isNotBlank(jobDependencies)) {

                    Map<String, List<HeraAction>> dependenciesMap = new HashMap<>(1024);
                    String[] dependencies = jobDependencies.split(Constants.COMMA);
                    for (String dependentId : dependencies) {
                        Integer dpId = Integer.parseInt(dependentId);
                        List<HeraAction> dependActionList = new ArrayList<>();

                        for (Map.Entry<Long, HeraAction> entry : actionMap.entrySet()) {
                            if (entry.getValue().getJobId().equals(dpId)) {
                                dependActionList.add(entry.getValue());
                            }
                        }
                        dependenciesMap.put(dependentId, dependActionList);
                        if (retryCount > 20) {
                            if (!heraJob.getConfigs().contains(RunningJobKeyConstant.DEPENDENCY_CYCLE_VALUE)) {
                                if (dependenciesMap.get(dependentId).size() == 0) {
                                    HeraAction lostJobAction = masterContext.getHeraJobActionService().findLatestByJobId(dependentId);
                                    if (lostJobAction != null) {
                                        actionMap.put(lostJobAction.getId(), lostJobAction);
                                        dependActionList.add(lostJobAction);
                                        dependenciesMap.put(dependentId, dependActionList);
                                    }
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
                                dependenciesMap.get(actionMostDeps).get(0).getId() > dependenciesMap.get(dependency).get(0).getId()) {
                            actionMostDeps = dependency;
                        }
                    }
                    //新加任务 可能无版本
                    if (!isComplete) {
                        noCompleteCount++;
                        notGenerate.add(heraJob.getId());
                    } else {
                        List<HeraAction> actionMostList = dependenciesMap.get(actionMostDeps);

                        if (actionMostList != null && actionMostList.size() > 0) {
                            for (HeraAction action : actionMostList) {
                                StringBuilder actionDependencies = new StringBuilder(action.getId().toString());
                                Long longActionId = Long.parseLong(actionDependencies.toString());
                                for (String dependency : dependencies) {
                                    if (!dependency.equals(actionMostDeps)) {
                                        List<HeraAction> otherAction = dependenciesMap.get(dependency);
                                        if (otherAction == null || otherAction.size() == 0) {
                                            continue;
                                        }
                                        String otherActionId = otherAction.get(0).getId().toString();
                                        for (HeraAction o : otherAction) {
                                            if (Math.abs(o.getId() - longActionId) < Math.abs(Long.parseLong(otherActionId) - longActionId)) {
                                                otherActionId = o.getId().toString();
                                            }
                                        }
                                        actionDependencies.append(",");
                                        actionDependencies.append(Long.parseLong(otherActionId) / 1000000 * 1000000 + Long.parseLong(dependency));
                                    }
                                }
                                HeraAction actionNew = new HeraAction();
                                BeanUtils.copyProperties(heraJob, actionNew);
                                Long actionId = longActionId / 1000000 * 1000000 + Long.parseLong(String.valueOf(heraJob.getId()));
                                actionNew.setId(actionId);
                                actionNew.setGmtCreate(new Date());
                                actionNew.setDependencies(actionDependencies.toString());
                                actionNew.setJobDependencies(heraJob.getDependencies());
                                actionNew.setJobId(heraJob.getId());
                                actionNew.setAuto(heraJob.getAuto());
                                actionNew.setGmtModified(new Date());
                                actionNew.setHostGroupId(heraJob.getHostGroupId());
                                insertActionList.add(actionNew);
                                ids.add(heraJob.getId());
                            }

                        }
                    }

                }
            }
        }
        batchInsertList(insertActionList, actionMap, nowAction);

        if (noCompleteCount > 0 && retryCount < maxRetryCount) {
            generateDependJobAction(jobList, actionMap, retryCount, nowAction, ids);
        } else if (retryCount == maxRetryCount && noCompleteCount > 0) {
            ScheduleLog.warn("未找到版本的ID:{}, 未找到版本个数:{} , 重试次数:{}", JSONObject.toJSONString(notGenerate), notGenerate.size(), retryCount);
        }
    }


    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public boolean scan() {
        boolean hasTask = false;
        if (!masterContext.getScheduleQueue().isEmpty()) {
            JobElement jobElement = masterContext.getScheduleQueue().poll();
            if (jobElement != null) {
                MasterWorkHolder workHolder = getRunnableWork(jobElement);
                if (workHolder == null) {
                    masterContext.getScheduleQueue().offer(jobElement);
                    ScheduleLog.warn("can not get work to execute Schedule job in master,job is:{}", jobElement.toString());
                } else {
                    runScheduleJob(workHolder, jobElement.getJobId());
                    hasTask = true;
                }
            }
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            JobElement jobElement = masterContext.getManualQueue().poll();
            if (jobElement != null) {
                MasterWorkHolder selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    masterContext.getManualQueue().offer(jobElement);
                    ScheduleLog.warn("can not get work to execute ManualQueue job in master,job is:{}", jobElement.toString());
                } else {
                    runManualJob(selectWork, jobElement.getJobId());
                    hasTask = true;
                }
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            JobElement jobElement = masterContext.getDebugQueue().poll();
            if (jobElement != null) {
                MasterWorkHolder selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    masterContext.getDebugQueue().offer(jobElement);
                    ScheduleLog.warn("can not get work to execute DebugQueue job in master,job is:{}", jobElement.toString());
                } else {
                    runDebugJob(selectWork, jobElement.getJobId());
                    hasTask = true;
                }
            }

        }
        return hasTask;

    }

    public void printThreadPoolLog() {
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
     * @param selectWork selectWork 所选机器
     * @param actionId   actionId
     */
    private void runManualJob(MasterWorkHolder selectWork, String actionId) {
        final MasterWorkHolder workHolder = selectWork;
        SocketLog.info("start run manual job, actionId = {}", actionId);

        this.executeJobPool.execute(() -> {
            HeraAction heraAction = masterContext.getHeraJobActionService().findById(actionId);
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
            HeraJobHistoryVo historyVo = BeanConvertUtils.convert(history);
            historyVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            heraAction.setStatus(Constants.STATUS_RUNNING);
            historyVo.setStatusEnum(StatusEnum.RUNNING);
            HeraAction cacheAction = heraActionMap.get(Long.parseLong(actionId));
            if (cacheAction != null) {
                cacheAction.setStatus(Constants.STATUS_RUNNING);
                cacheAction.setHistoryId(heraAction.getHistoryId());
            }
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(historyVo));

            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                        JobExecuteKind.ExecuteKind.ManualKind, actionId);
                response = future.get();
            } catch (Exception e) {
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                ErrorLog.error("manual job run error {}", e);
            }
            boolean success = response != null && response.getStatusEnum() != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            if (response != null) {
                ScheduleLog.info("actionId 执行结果" + actionId + "---->" + response.getStatusEnum());
            }
            ApplicationEvent event;
            if (!success) {
                if (exception != null) {
                    HeraException heraException = new HeraException(exception);
                    ErrorLog.error("manual actionId = {} error, {}", history.getActionId(), heraException.getMessage());
                }
                ScheduleLog.info("actionId = {} manual execute failed", history.getActionId());
                heraAction.setStatus(Constants.STATUS_FAILED);
                HeraJobHistory jobHistory = masterContext.getHeraJobHistoryService().findById(history.getId());
                if (LogConstant.CANCEL_JOB_LOG.equals(jobHistory.getIllustrate())) {
                    event = null;
                } else {
                    HeraJobHistoryVo jobHistoryVo = BeanConvertUtils.convert(jobHistory);
                    event = new HeraJobFailedEvent(history.getActionId(), jobHistoryVo.getTriggerType(), jobHistoryVo);
                }
            } else {
                heraAction.setStatus(Constants.STATUS_SUCCESS);
                event = new HeraJobSuccessEvent(history.getActionId(), historyVo.getTriggerType(), history.getId());
            }
            cacheAction = heraActionMap.get(Long.parseLong(actionId));
            if (cacheAction != null) {
                cacheAction.setStatus(heraAction.getStatus());
            }
            heraAction.setStatisticEndTime(new Date());
            masterContext.getHeraJobActionService().update(heraAction);
            if (event != null) {
                masterContext.getDispatcher().forwardEvent(event);
            }
        });
    }

    /**
     * 调度任务执行前，先获取任务的执行重试时间间隔和重试次数
     *
     * @param workHolder 所选机器
     * @param actionId   actionId
     */
    private void runScheduleJob(MasterWorkHolder workHolder, String actionId) {
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
            runScheduleJobContext(workHolder, actionId, runCount, retryCount, retryWaitTime);
        });
    }

    /**
     * 自动调度任务开始执行入口，向master端的channel写请求任务执行请求
     *
     * @param workHolder    workHolder
     * @param actionId      actionId
     * @param runCount      runCount
     * @param retryCount    retryCount
     * @param retryWaitTime retryWaitTime
     */
    private void runScheduleJobContext(MasterWorkHolder workHolder, String actionId, int runCount, int retryCount, int retryWaitTime) {

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
        HeraAction heraAction;
        if (runCount == 1) {

            heraAction = masterContext.getHeraJobActionService().findById(actionId);
            heraJobHistory = masterContext.getHeraJobHistoryService().
                    findById(heraAction.getHistoryId());
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            triggerType = heraJobHistoryVo.getTriggerType();
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");

        } else {
            heraAction = masterContext.getHeraJobActionService().findById(actionId);
            heraJobHistory = HeraJobHistory.builder()
                    .illustrate(LogConstant.FAIL_JOB_RETRY)
                    .triggerType(TriggerTypeEnum.SCHEDULE.getId())
                    .jobId(heraAction.getJobId())
                    .actionId(String.valueOf(heraAction.getId()))
                    .operator(heraAction.getOwner())
                    .build();
            masterContext.getHeraJobHistoryService().insert(heraJobHistory);
            heraAction.setHistoryId(heraJobHistory.getId());
            heraAction.setStatus(Constants.STATUS_RUNNING);
            masterContext.getHeraJobActionService().update(heraAction);
            heraJobHistoryVo = BeanConvertUtils.convert(heraJobHistory);
            heraJobHistoryVo.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 第" + (runCount - 1) + "次重试运行\n");
            triggerType = heraJobHistoryVo.getTriggerType();
        }
        HeraAction cacheAction = heraActionMap.get(Long.parseLong(actionId));
        if (cacheAction != null) {
            cacheAction.setStatus(Constants.STATUS_RUNNING);
            cacheAction.setHistoryId(heraJobHistory.getId());
        }
        heraJobHistoryVo.setStatusEnum(StatusEnum.RUNNING);
        masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        RpcResponse.Response response = null;
        Future<RpcResponse.Response> future = null;
        try {
            future = new MasterExecuteJob().executeJob(masterContext, workHolder,
                    ScheduleKind, actionId);
            response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
        } catch (Exception e) {
            ErrorLog.error("schedule job run error :" + actionId, e);
            if (future != null) {
                future.cancel(true);
            }
            heraAction.setStatus(Constants.STATUS_FAILED);
            heraJobHistoryVo.setStatusEnum(StatusEnum.FAILED);
            masterContext.getHeraJobHistoryService().updateHeraJobHistoryStatus(BeanConvertUtils.convert(heraJobHistoryVo));
        }
        boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
        ScheduleLog.info("job_id 执行结果" + actionId + "---->" + (response == null ? "空指针" : response.getStatusEnum().toString()));
        if (!success) {
            heraAction.setStatus(Constants.STATUS_FAILED);
            HeraJobHistory history = masterContext.getHeraJobHistoryService().findById(heraJobHistoryVo.getId());
            HeraJobHistoryVo jobHistory = BeanConvertUtils.convert(history);
            HeraJobFailedEvent event = new HeraJobFailedEvent(actionId, triggerType, jobHistory);
            event.setRollBackTime(retryWaitTime);
            event.setRunCount(runCount);
            if (Constants.CANCEL_JOB_MESSAGE.equals(jobHistory.getIllustrate())) {
                isCancelJob = true;
                ScheduleLog.info("任务取消，取消重试:{}", jobHistory.getActionId());
            } else {
                masterContext.getDispatcher().forwardEvent(event);
            }
        } else {
            //如果是依赖任务 置空依赖
            if (JobScheduleTypeEnum.Dependent.getType().equals(heraAction.getScheduleType())) {
                heraAction.setReadyDependency("{}");
            }
            heraAction.setStatus(Constants.STATUS_SUCCESS);
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, triggerType, heraJobHistory.getId());
            masterContext.getDispatcher().forwardEvent(successEvent);
        }
        cacheAction = heraActionMap.get(Long.parseLong(actionId));
        if (cacheAction != null) {
            cacheAction.setStatus(heraAction.getStatus());
        }
        heraAction.setStatisticEndTime(new Date());
        masterContext.getHeraJobActionService().update(heraAction);
        if (runCount < (retryCount + 1) && !success && !isCancelJob) {
            DebugLog.info("--------------------------失败任务，准备重试--------------------------");
            runScheduleJobContext(workHolder, actionId, runCount, retryCount, retryWaitTime);
        }
    }

    /**
     * 开发中心脚本执行逻辑
     *
     * @param selectWork 所选机器
     * @param debugId    debugId
     */
    private void runDebugJob(MasterWorkHolder selectWork, String debugId) {
        final MasterWorkHolder workHolder = selectWork;
        this.executeJobPool.execute(() -> {
            HeraDebugHistoryVo history = masterContext.getHeraDebugHistoryService().findById(Integer.parseInt(debugId));
            history.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 开始运行");
            masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(history));
            Exception exception = null;
            RpcResponse.Response response = null;
            Future<RpcResponse.Response> future = null;
            try {
                future = new MasterExecuteJob().executeJob(masterContext, workHolder, JobExecuteKind.ExecuteKind.DebugKind, debugId);
                response = future.get(HeraGlobalEnvironment.getTaskTimeout(), TimeUnit.HOURS);
            } catch (Exception e) {
                exception = e;
                if (future != null) {
                    future.cancel(true);
                }
                DebugLog.error(String.format("debugId:%s run failed", debugId), e);
            }
            boolean success = response != null && response.getStatusEnum() == ResponseStatus.Status.OK;
            if (!success) {
                exception = new HeraException(String.format("fileId:%s run failed ", history.getFileId()), exception);
                TaskLog.info("8.Master: debug job error");
                history = masterContext.getHeraDebugHistoryService().findById(Integer.parseInt(debugId));
                HeraDebugFailEvent failEvent = HeraDebugFailEvent.builder()
                        .debugHistory(BeanConvertUtils.convert(history))
                        .throwable(exception)
                        .fileId(history.getFileId())
                        .build();
                masterContext.getDispatcher().forwardEvent(failEvent);
            } else {
                TaskLog.info("7.Master: debug success");
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
     * @param jobElement job 部分信息
     * @return
     */
    private MasterWorkHolder getRunnableWork(JobElement jobElement) {
        return chooseWorkerStrategy.chooseWorker(jobElement, masterContext);
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
     * @param heraJobHistory heraJobHistory表信息
     */
    public void run(HeraJobHistoryVo heraJobHistory) {
        String actionId = heraJobHistory.getActionId();
        int priorityLevel = 3;
        HeraAction heraAction = masterContext.getHeraJobActionService().findById(actionId);
        Map<String, String> configs = StringUtil.convertStringToMap(heraAction.getConfigs());
        String priorityLevelValue = configs.get("run.priority.level");
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
        if (checkJobExists(heraJobHistory, false)) {
            return;
        }
        //先在数据库中set一些执行任务所需的必须值 然后再加入任务队列
        heraAction.setLastResult(heraAction.getStatus());
        heraAction.setStatus(Constants.STATUS_RUNNING);
        heraAction.setHistoryId(heraJobHistory.getId());
        heraAction.setHost(heraJobHistory.getExecuteHost());
        heraAction.setStatisticStartTime(new Date());
        heraAction.setStatisticEndTime(null);
        masterContext.getHeraJobActionService().update(heraAction);
        heraJobHistory.getLog().append(ActionUtil.getTodayString() + "进入任务队列");
        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {
            masterContext.getManualQueue().offer(element);
        } else {
            masterContext.getScheduleQueue().offer(element);
        }
    }


    private boolean checkJobExists(HeraJobHistoryVo heraJobHistory, boolean checkOnly) {
        // TODO 任务检测，不要使用for循环，后面改成hash查找
        String actionId = heraJobHistory.getActionId();
        Integer jobId = heraJobHistory.getJobId();
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER || heraJobHistory.getTriggerType() == TriggerTypeEnum.SCHEDULE) {
            // check调度器等待队列是否有此任务在排队
            for (JobElement jobElement : masterContext.getScheduleQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("调度队列已存在该任务，添加失败 {}", actionId);
                    return true;
                }
            }
            // check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getRunning().contains(jobId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().getLocalAddress());
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                    return true;

                }
            }

        } else if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {

            for (JobElement jobElement : masterContext.getManualQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG);
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("手动任务队列已存在该任务，添加失败 {}", actionId);
                    return true;
                }
            }

            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (workHolder.getManningRunning().contains(jobId)) {
                    if (!checkOnly) {
                        heraJobHistory.getLog().append(LogConstant.CHECK_MANUAL_QUEUE_LOG + "执行worker ip " + workHolder.getChannel().getLocalAddress());
                        heraJobHistory.setStartTime(new Date());
                        heraJobHistory.setEndTime(new Date());
                        heraJobHistory.setStatusEnum(StatusEnum.FAILED);
                        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
                    }
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
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
        ErrorLog.error("work:{}断线", ip);
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
                                startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
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
                                startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
                            }
                        }
                    } else {
                        for (String action : scheduleTask) {
                            heraAction = masterContext.getHeraJobActionService().findById(action);
                            heraJobHistory = masterContext.getHeraJobHistoryService().findById(heraAction.getHistoryId());
                            heraJobHistory.setIllustrate("work断线超出十分钟，重新执行该任务");
                            SocketLog.warn("work断线并且未重连，重新调度任务{}", action);
                            startNewJob(heraJobHistory, LogConstant.RETRY_JOB);
                        }
                    }
                } catch (InterruptedException e) {
                    ErrorLog.error("work断线任务检测异常{}", e);
                }
            }, 10, TimeUnit.MINUTES);

            String content = "不幸的消息，work宕机了:" + channel.remoteAddress() + "<br>" +
                    "自动调度队列任务：" + workHolder.getHeartBeatInfo().getRunning() + "<br>" +
                    "手动队列任务：" + workHolder.getHeartBeatInfo().getManualRunning() + "<br>" +
                    "开发中心队列任务：" + workHolder.getHeartBeatInfo().getDebugRunning() + "<br>";
            ErrorLog.error(content);
        }
    }

    private void startNewJob(HeraJobHistory heraJobHistory, String illustrate) {
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        masterContext.getHeraJobHistoryService().update(heraJobHistory);
        HeraJobHistory newHistory = HeraJobHistory.builder().
                actionId(heraJobHistory.getActionId()).
                illustrate(illustrate).
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
