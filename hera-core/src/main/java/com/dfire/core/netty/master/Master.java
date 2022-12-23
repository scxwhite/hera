package com.dfire.core.netty.master;


import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.constants.TimeFormatConstant;
import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.exception.HostGroupNotExistsException;
import com.dfire.common.kv.Tuple;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.HeraDateTool;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.event.handler.AbstractHandler;
import com.dfire.core.event.handler.JobHandler;
import com.dfire.core.event.listenter.*;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.core.route.loadbalance.LoadBalance;
import com.dfire.core.route.loadbalance.LoadBalanceFactory;
import com.dfire.core.util.CronParse;
import com.dfire.event.Events;
import com.dfire.event.HeraJobMaintenanceEvent;
import com.dfire.event.HeraJobSuccessEvent;
import com.dfire.logs.*;
import com.dfire.monitor.domain.AlarmInfo;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:24 2018/1/12
 * @desc hera核心任务调度器
 */
@Component
@Order(1)
public class Master {

    @Getter
    private MasterContext masterContext;
    @Getter
    private ConcurrentHashMap<Long, HeraAction> heraActionMap;

    private volatile boolean isGenerateActioning = false;

    private LoadBalance loadBalance;

    private MasterRunJob masterRunJob;


    private Channel lastWork;

    public void init(MasterContext masterContext) {
        this.masterContext = masterContext;
        masterRunJob = new MasterRunJob(masterContext, this);
        loadBalance = LoadBalanceFactory.getLoadBalance();
        heraActionMap = new ConcurrentHashMap<>();
        if (HeraGlobalEnv.getEnv().equalsIgnoreCase(Constants.PRE_ENV)) {
            //可以在此关闭预发环境执行任务
            //masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }
        //关于为什么异步 因为版本过多时会导致启动时长较长，导致master断线，重新选举出新的work，然后周而复始，进入死循环
        masterContext.getThreadPool().execute(() -> {
            HeraLog.info("-----------------------------init action,time: {}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobFinishListener(masterContext));
            List<HeraAction> allJobList = masterContext.getHeraJobActionService().getAfterAction(getBeforeDayAction());
            HeraLog.info("-----------------------------action size:{}, time {}-----------------------------", allJobList.size(), System.currentTimeMillis());
            allJobList.forEach(heraAction -> {
                masterContext.getDispatcher().
                        addJobHandler(new JobHandler(heraAction.getId(), this, masterContext));
                heraActionMap.put(heraAction.getId(), heraAction);
            });
            HeraLog.info("-----------------------------add actions to handler success, time:{}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().forwardEvent(Events.Initialize);
            HeraLog.info("-----------------------------dispatcher actions success, time{}-----------------------------", System.currentTimeMillis());
            masterContext.refreshHostGroupCache();
            HeraLog.info("refresh hostGroup cache");
        });
    }

    public boolean isTaskLimit() {
        return masterRunJob.isTaskLimit();
    }

    public Integer getRunningTaskNum() {
        return masterRunJob.getRunningTaskNum();
    }

    private long getBeforeDayAction() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -HeraGlobalEnv.getJobCacheDay());
        return Long.parseLong(ActionUtil.getActionVersionByDate(calendar.getTime()));
    }


    public boolean checkJobRun(HeraJob heraJob) {
        if (heraJob == null) {
            ScheduleLog.warn("任务被删除，取消执行");
            return false;
        }
        if (heraJob.getAuto() == 0) {
            ScheduleLog.warn("任务被关闭，取消执行:" + heraJob.getId());
            return false;
        }
        return true;
    }

    private Set<String> areaList(String areas) {
        if (StringUtils.isBlank(areas)) {
            return new HashSet<>(0);
        }
        Map<Integer, String> areaMap = masterContext.getHeraAreaService().findAll().stream()
                .collect(Collectors.toMap(HeraArea::getId, HeraArea::getName));
        return Arrays.stream(areas.split(Constants.COMMA))
                .distinct()
                .map(Integer::parseInt)
                .map(areaMap::get)
                .collect(Collectors.toSet());
    }


    public boolean generateSingleAction(Integer jobId) {
        ScheduleLog.info("单个任务版本生成：{}", jobId);
        return generateAction(true, jobId, false);
    }

    public boolean generateBatchAction(boolean mustExe) {
        ScheduleLog.info("全量任务版本生成");
        long begin = System.currentTimeMillis();
        boolean flag = generateAction(false, null, mustExe);
        ScheduleLog.info("生成版本时间:" + (System.currentTimeMillis() - begin) + " ms");
        return flag;
    }

    private boolean generateAction(boolean isSingle, Integer jobId, boolean mustExe) {
        try {
            if (isGenerateActioning) {
                return true;
            }
            DateTime dateTime = new DateTime();
            Date now = dateTime.toDate();
            int executeHour = dateTime.getHourOfDay();
            //只在凌晨23、0点生成版本
            //  boolean execute = mustExe || executeHour == 23 || executeHour == 0;
            boolean execute = mustExe || executeHour == 0 || (executeHour > ActionUtil.ACTION_CREATE_MIN_HOUR && executeHour <= ActionUtil.ACTION_CREATE_MAX_HOUR);
            if (execute || isSingle) {
                String currString = ActionUtil.getCurrHourVersion();
                if (executeHour == ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    Tuple<String, Date> nextDayString = ActionUtil.getNextDayString();
                    //例如：今天 2018.07.17 23:50  currString = 201807180000000000 now = 2018.07.18 23:50
                    currString = nextDayString.getSource();
                    now = nextDayString.getTarget();
                }
                Long nowAction = Long.parseLong(currString);
                ConcurrentHashMap<Long, HeraAction> actionMap = new ConcurrentHashMap<>(heraActionMap.size());
                List<HeraJob> jobList = new ArrayList<>();
                //批量生成
                if (!isSingle) {
                    isGenerateActioning = true;
                    jobList = masterContext.getHeraJobService().getAll();
                } else { //单个任务生成版本
                    HeraJob heraJob = masterContext.getHeraJobService().findById(jobId);
                    jobList.add(heraJob);
                    if (heraJob.getScheduleType() == 1) {
                        jobList.addAll(getParentJob(heraJob.getDependencies(), new HashSet<>()));
                    }
                    actionMap = heraActionMap;
                    List<Long> shouldRemove = new ArrayList<>();
                    for (Long actionId : actionMap.keySet()) {
                        if (StringUtil.actionIdToJobId(String.valueOf(actionId), String.valueOf(jobId))) {
                            shouldRemove.add(actionId);
                        }
                    }
                    shouldRemove.forEach(actionMap::remove);
                    List<AbstractHandler> handlers = new ArrayList<>(masterContext.getDispatcher().getJobHandlers().values());
                    if (handlers.size() > 0) {
                        for (AbstractHandler handler : handlers) {
                            JobHandler jobHandler = (JobHandler) handler;
                            if (StringUtil.actionIdToJobId(jobHandler.getActionId(), String.valueOf(jobId))) {
                                masterContext.getQuartzSchedulerService().deleteJob(jobHandler.getActionId());
                                masterContext.getDispatcher().removeJobHandler(jobHandler);
                            }
                        }
                    }
                }
                String cronDate = ActionUtil.getActionVersionPrefix(now);
                Map<Integer, List<HeraAction>> idMap = new HashMap<>(jobList.size());
                Map<Integer, HeraJob> jobMap = new HashMap<>(jobList.size());
                // 依赖层级多的情况下，生成依赖任务实例，深度遍历的递归非常耗时，改为广度遍历
                // 先做一次 拓扑排序 生成依赖任务顺序生成即可
                jobList = this.topologicalSort(jobList);
                generateScheduleJobAction(jobList, cronDate, actionMap, nowAction, idMap, jobMap);
                for (Map.Entry<Integer, HeraJob> entry : jobMap.entrySet()) {
                    generateDependJobAction(entry.getValue(), actionMap, nowAction, idMap);
                }
                if (executeHour < ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    heraActionMap = actionMap;
                }
                Dispatcher dispatcher = masterContext.getDispatcher();
                if (dispatcher != null) {
                    if (actionMap.size() > 0) {
                        for (Long id : actionMap.keySet()) {
                            JobHandler jobHandler = new JobHandler(id, masterContext.getMaster(), masterContext);
                            jobHandler = dispatcher.addJobHandler(jobHandler);
                            //如果是今天的版本 更新缓存
                            if (ActionUtil.isTodayActionVersion(id.toString())) {
                                jobHandler.handleEvent(new HeraJobMaintenanceEvent(Events.UpdateActions, id));
                            }
                        }
                    }
                }
                ScheduleLog.info("[单个任务:{}，任务id:{}]generate action success", isSingle, jobId);
                return true;
            }
        } catch (Exception e) {
            ErrorLog.error("生成版本异常", e);
            notifyAdmin("版本生成异常警告", e.getMessage());
        } finally {
            isGenerateActioning = false;
        }
        return false;
    }

    /**
     * 拓扑排序
     *
     * @param jobDetails
     * @return
     */
    public List<HeraJob> topologicalSort(List<HeraJob> jobDetails) {
        Map<Integer, HeraJob> jobInfo = Maps.newHashMap();

        //任务的下游依赖
        Map<Integer, Set<Integer>> downStreamJobInfo = Maps.newHashMap();
        for (HeraJob heraJob : jobDetails) {
            jobInfo.put(heraJob.getId(), heraJob);
            if (heraJob.getDependencies() != null) {
                HashSet<String> depList = Sets.newHashSet(Splitter.on(",").split(heraJob.getDependencies()));
                for (String dep : depList) {
                    if (!Strings.isNullOrEmpty(dep)) {
                        Set<Integer> downStreamJobList = downStreamJobInfo.get(Integer.valueOf(dep));
                        if (downStreamJobList == null) {
                            downStreamJobList = Sets.newHashSet();
                        }
                        downStreamJobList.add(heraJob.getId());
                        downStreamJobInfo.put(Integer.valueOf(dep), downStreamJobList);
                    }
                }
            }
            if (!downStreamJobInfo.containsKey(heraJob.getId())) {
                downStreamJobInfo.put(heraJob.getId(), Sets.newHashSet());
            }
        }

        List<HeraJob> newJobDetails = Lists.newArrayList();
        // 入度不为0 即有上游任务的节点
        Map<Integer, Integer> notZeroIndegreeMap = Maps.newHashMap();
        //入度为0 即没有上游任务的节点
        Queue<Integer> zeroIndegreeQueue = new LinkedList<>();
        for (HeraJob heraJob : jobDetails) {
            int jobId = heraJob.getId();
            String dep = heraJob.getDependencies();
            if (heraJob.getScheduleType().equals(JobScheduleTypeEnum.Independent.getType())) {
                zeroIndegreeQueue.add(jobId);
                newJobDetails.add(heraJob);
            } else {
                Set<String> depList = Sets.newHashSet(Splitter.on(",").split(dep));
                int size = depList.size();
                //过滤掉依赖了不会生成实例的任务
                for (String id : depList) {
                    if (!jobInfo.containsKey(Integer.valueOf(id))) {
                        size--;
                    }
                }
                if (size == 0) {
                    zeroIndegreeQueue.add(jobId);
                    newJobDetails.add(heraJob);
                } else {
                    notZeroIndegreeMap.put(jobId, size);
                }
            }
        }

        while (!zeroIndegreeQueue.isEmpty()) {
            int node = zeroIndegreeQueue.poll();
            Set<Integer> downStreamJobList = downStreamJobInfo.get(node);
            for (int downStreamJob : downStreamJobList) {
                if (notZeroIndegreeMap.containsKey(downStreamJob)) {
                    int degree = notZeroIndegreeMap.get(downStreamJob);
                    if (--degree == 0) {
                        newJobDetails.add(jobInfo.get(downStreamJob));
                        zeroIndegreeQueue.add(downStreamJob);
                        notZeroIndegreeMap.remove(downStreamJob);
                    } else {
                        notZeroIndegreeMap.put(downStreamJob, degree);
                    }
                }
            }
        }
        return newJobDetails;
    }


    /**
     * 递归获取所有父级依赖任务
     *
     * @param dpIdStr
     * @return
     */
    public Set<HeraJob> getParentJob(String dpIdStr, Set<Integer> jobCheck) {
        Set<HeraJob> jobSet = new HashSet<>();
        Arrays.stream(dpIdStr.split(Constants.COMMA)).forEach(id -> {
            HeraJob dpJob = masterContext.getHeraJobService().findMemById(Integer.parseInt(id));
            if (dpJob != null && !jobCheck.contains(dpJob.getId())) {
                jobCheck.add(dpJob.getId());
                if (dpJob.getScheduleType() == 1) {
                    jobSet.addAll(getParentJob(dpJob.getDependencies(), jobCheck));
                }
                jobSet.add(dpJob);
            }
        });
        return jobSet;
    }

    /**
     * 自动任务的版本生成
     *
     * @param jobList   任务集合
     * @param cronDate  日期
     * @param actionMap actionMap集合
     * @param nowAction 生成版本时间的action
     * @param idMap     已经遍历过的idMap
     * @param jobMap    依赖任务map映射
     */
    public void generateScheduleJobAction(List<HeraJob> jobList, String
            cronDate, Map<Long, HeraAction> actionMap, Long
                                                  nowAction, Map<Integer, List<HeraAction>> idMap, Map<Integer, HeraJob> jobMap) {
        List<HeraAction> insertActionList = new ArrayList<>();
        for (HeraJob heraJob : jobList) {
            if (heraJob.getScheduleType() != null) {
                if (heraJob.getScheduleType() == 1) {
                    jobMap.put(heraJob.getId(), heraJob);
                } else if (heraJob.getScheduleType() == 0) {
                    String cron = heraJob.getCronExpression();
                    List<String> list = new ArrayList<>();
                    if (StringUtils.isNotBlank(cron)) {
                        boolean isCronExp = CronParse.Parser(cron, cronDate, list);
                        if (!isCronExp) {
                            HeraLog.warn("cron parse error,jobId={},cron = {}", heraJob.getId(), cron);
                            continue;
                        }
                        List<HeraAction> heraAction = createHeraAction(list, heraJob);
                        idMap.put(heraJob.getId(), heraAction);
                        insertActionList.addAll(heraAction);
                    }
                } else {
                    ErrorLog.error("任务{}未知的调度类型{}", heraJob.getId(), heraJob.getScheduleType());
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
    private void batchInsertList(List<HeraAction> insertActionList, Map<Long, HeraAction> actionMap, Long
            nowAction) {
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
            heraAction.setBatchId(getBatchIdFromActionIdPeriod(actionId, heraJob.getCronPeriod(), heraJob.getCronInterval())); //批次号
            heraActionList.add(heraAction);
        }
        return heraActionList;
    }


    public void clearInvalidAction() {
        ScheduleLog.warn("开始进行版本清理");
        Dispatcher dispatcher = masterContext.getDispatcher();
        Long currDate = ActionUtil.getLongCurrActionVersion();
        Long nextDay = ActionUtil.getLongNextDayActionVersion();
        Long preCheckTime = currDate - MasterConstant.PRE_CHECK_MIN;

        Map<Long, HeraAction> actionMapNew = heraActionMap;
        //移除未生成的调度
        List<AbstractHandler> handlers = Lists.newArrayList(dispatcher.getJobHandlers().values());
        List<JobHandler> shouldRemove = new ArrayList<>();
        Long dayAction = getBeforeDayAction();
        if (handlers.size() > 0) {
            handlers.forEach(handler -> {
                JobHandler jobHandler = (JobHandler) handler;
                Long actionId = jobHandler.getActionId();
                // Long  = Long.parseLong(actionId);
                if (actionId < preCheckTime) {
                    masterContext.getQuartzSchedulerService().deleteJob(actionId);
                } else if (actionId >= currDate && actionId < nextDay) {
                    if (!actionMapNew.containsKey(actionId)) {
                        masterContext.getQuartzSchedulerService().deleteJob(actionId);
                        masterContext.getHeraJobActionService().delete(actionId);
                        shouldRemove.add(jobHandler);
                    }
                }
                //移除非缓存时间内的版本的订阅者
                if (actionId.compareTo(dayAction) < 0) {
                    shouldRemove.add(jobHandler);
                }
            });
        }
        //移除 过期 失效的handler
        shouldRemove.forEach(dispatcher::removeJobHandler);
        ScheduleLog.warn("版本清理完成,清理handler个数为:" + shouldRemove.size());
    }


    /**
     * 递归生成任务依赖action
     *
     * @param heraJob   当前生成版本的任务
     * @param actionMap 版本map
     * @param nowAction 生成版本时间的action
     * @param idMap     job的id集合  只要已经检测过的id都放入idSet中
     */
    public void generateDependJobAction(HeraJob heraJob, Map<Long, HeraAction> actionMap, Long nowAction, Map<Integer, List<HeraAction>> idMap) {
        if (heraJob == null || idMap.containsKey(heraJob.getId())) {
            return;
        }
        String jobDependencies = heraJob.getDependencies();
        if (StringUtils.isNotBlank(jobDependencies)) {
            Map<String, List<HeraAction>> dependenciesMap = new HashMap<>(1024);
            String[] dependencies = jobDependencies.split(Constants.COMMA);
            String actionMinDeps = "";
            boolean noAction = false;
            for (String dependentId : dependencies) {
                Integer dpId = Integer.parseInt(dependentId);
                List<HeraAction> dpActions = idMap.get(dpId);
                dependenciesMap.put(dependentId, dpActions);
                if (dpActions == null || dpActions.size() == 0) {
                    HeraLog.info("{}今天找不到版本，无法为任务{}生成版本", dependentId, heraJob.getId());
                    noAction = true;
                    break;
                }
                if (StringUtils.isBlank(actionMinDeps)) {
                    actionMinDeps = dependentId;
                }
                //找到所依赖的任务中版本最少的作为基准版本。
                if (dependenciesMap.get(actionMinDeps).size() > dependenciesMap.get(dependentId).size()) {
                    actionMinDeps = dependentId;
                } else if (dependenciesMap.get(dependentId).size() > 0 && dependenciesMap.get(actionMinDeps).size() == dependenciesMap.get(dependentId).size() &&
                        dependenciesMap.get(actionMinDeps).get(0).getId() < dependenciesMap.get(dependentId).get(0).getId()) {
                    //如果两个版本的个数一样  那么应该找一个时间较大的
                    actionMinDeps = dependentId;
                }
            }
            if (noAction) {
                idMap.put(heraJob.getId(), null);
            } else {
                List<HeraAction> actionMinList = dependenciesMap.get(actionMinDeps);
                if (actionMinList != null && actionMinList.size() > 0) {
                    List<HeraAction> insertList = new ArrayList<>();
                    for (HeraAction action : actionMinList) {
                        StringBuilder actionDependencies = new StringBuilder(action.getId().toString());
                        Long longActionId = Long.parseLong(actionDependencies.toString());
                        for (String dependency : dependencies) {
                            if (!dependency.equals(actionMinDeps)) {
                                List<HeraAction> otherAction = dependenciesMap.get(dependency);
                                if (otherAction == null || otherAction.size() == 0) {
                                    continue;
                                }
                                //找到一个离基准版本时间最近的action，添加为该任务的依赖
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
                        actionNew.setHostGroupId(heraJob.getHostGroupId());
                        actionNew.setBatchId(getBatchIdFromActionIdPeriod(actionId, heraJob.getCronPeriod(), heraJob.getCronInterval()));//批次号
                        masterContext.getHeraJobActionService().insert(actionNew, nowAction);
                        actionMap.put(actionNew.getId(), actionNew);
                        insertList.add(actionNew);
                    }
                    idMap.put(heraJob.getId(), insertList);
                }
            }
        }

    }


    public boolean scanQueue(BlockingQueue<JobElement> queue) throws InterruptedException {
        if (!queue.isEmpty()) {
            JobElement jobElement = queue.take();
            MasterWorkHolder selectWork;
            try {
                selectWork = getRunnableWork(jobElement);
                if (selectWork == null) {
                    queue.put(jobElement);
                    ScheduleLog.warn("can not get work to execute job in master,job is:{}", jobElement.toString());
                } else {
                    masterRunJob.run(selectWork, jobElement);
                    return true;
                }
            } catch (HostGroupNotExistsException e) {
                updateStatus(e.getMessage(), jobElement.getJobId(), jobElement.getTriggerType());
                ErrorLog.error("can not get work to execute job in master", e);
            }
        }
        return false;
    }

    private void updateStatus(String msg, Long id, TriggerTypeEnum typeEnum) {
        switch (typeEnum) {
            case SCHEDULE:
            case MANUAL_RECOVER:
            case MANUAL:
                HeraAction heraAction = masterContext.getHeraJobActionService().findById(id);
                masterContext.getHeraJobActionService().updateStatus(id, StatusEnum.FAILED.name());
                masterContext.getHeraJobHistoryService().updateHeraJobHistoryLogAndStatus(HeraJobHistory.builder()
                        .id(heraAction.getHistoryId())
                        .log(msg)
                        .status(StatusEnum.FAILED.name())
                        .illustrate("自动取消")
                        .endTime(new Date())
                        .build());
                break;
            case DEBUG:
                masterContext.getHeraDebugHistoryService().updateStatus(id, msg, StatusEnum.FAILED.name());
                break;
            default:
                break;
        }
    }


    /**
     * 获取hostGroupId中可以分发任务的worker
     *
     * @param jobElement job 部分信息
     * @return
     */
    private MasterWorkHolder getRunnableWork(JobElement jobElement) throws HostGroupNotExistsException {
        //TODO 如果是emr集群 是否可以在这里判断内存信息？
        MasterWorkHolder selectWork = loadBalance.select(jobElement, masterContext);
        if (selectWork == null) {
            return null;
        }
        Channel channel = selectWork.getChannel().getChannel();
        HeartBeatInfo beatInfo = selectWork.getHeartBeatInfo();
        // 如果最近两次选择的work一致  需要等待机器最新状态发来之后(睡眠)再进行任务分发
        if (HeraGlobalEnv.getWarmUpCheck() > 0 && lastWork != null && channel == lastWork && (beatInfo.getCpuLoadPerCore() > 0.6F || beatInfo.getMemRate() > 0.7F)) {
            ScheduleLog.info("达到预热条件，睡眠" + HeraGlobalEnv.getWarmUpCheck() + "秒");
            try {
                TimeUnit.SECONDS.sleep(HeraGlobalEnv.getWarmUpCheck());
            } catch (InterruptedException e) {
                ErrorLog.error("InterruptedException", e);
            }
            lastWork = null;
            return null;
        }
        lastWork = channel;
        return selectWork;
    }

    public void debug(HeraDebugHistoryVo debugHistory) {
        //如果是emr集群,开发中心任务直接在固定集群跑
        boolean fixedEmr = HeraGlobalEnv.isEmrJob();
        JobElement element = JobElement.builder()
                .jobId(debugHistory.getId())
                .hostGroupId(debugHistory.getHostGroupId())
                .historyId(debugHistory.getId())
                .triggerType(TriggerTypeEnum.DEBUG)
                .fixedEmr(fixedEmr)
                .costMinute(0)
                .build();
        debugHistory.setStatus(StatusEnum.RUNNING);
        debugHistory.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        debugHistory.getLog().append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 进入任务队列");
        masterContext.getHeraDebugHistoryService().update(BeanConvertUtils.convert(debugHistory));
        try {
            masterContext.getDebugQueue().put(element);
        } catch (InterruptedException e) {
            ErrorLog.error("添加开发中心执行任务失败:" + element.getJobId(), e);
        }
    }


    private String getInheritVal(Integer groupId, String key, String defaultKey) {
        HeraGroup group = masterContext.getHeraGroupService().findConfigById(groupId);
        String defaultVal = null;
        while (group != null && groupId != null && groupId != 0) {
            Map<String, String> map = StringUtil.convertStringToMap(group.getConfigs());
            // 多重继承相同变量，以第一个的为准
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().equals(key)) {
                    return entry.getValue();
                }
                if (StringUtils.isBlank(defaultVal) && entry.getKey().equals(defaultKey)) {
                    defaultVal = entry.getValue();
                }
            }
            groupId = group.getParent();
            group = masterContext.getHeraGroupService().findConfigById(groupId);
        }
        return defaultVal;
    }

    /**
     * 手动执行任务或者手动恢复任务的时候，先进行任务是否在执行的判断，
     * 没有在运行进入队列等待，已经在运行的任务不入队列，避免重复执行
     *
     * @param heraJobHistory heraJobHistory表信息
     */
    public void run(HeraJobHistoryVo heraJobHistory, HeraJob heraJob) {
        Long actionId = heraJobHistory.getActionId();
        //重复job检测
        if (checkJobExists(heraJobHistory, false)) {
            return;
        }
        HeraAction heraAction = masterContext.getHeraJobActionService().findById(actionId);
        Set<String> areaList = areaList(heraJob.getAreaId());
        //非执行区域任务直接设置为成功
        if (!areaList.contains(HeraGlobalEnv.getArea()) && !areaList.contains(Constants.ALL_AREA)) {
            ScheduleLog.info("非{}区域任务，直接设置为成功:{}", HeraGlobalEnv.getArea(), heraJob.getId());
            heraAction.setLastResult(heraAction.getStatus());
            heraAction.setStatus(StatusEnum.SUCCESS.toString());
            heraAction.setHistoryId(heraJobHistory.getId());
            heraAction.setReadyDependency("{}");
            String host = "localhost";
            heraAction.setHost(host);
            Date endTime = new Date();
            heraAction.setStatisticStartTime(endTime);
            heraAction.setStatisticEndTime(endTime);
            masterContext.getHeraJobActionService().update(heraAction);
            heraJobHistory.getLog().append("非" + HeraGlobalEnv.getArea() + "区域任务，直接设置为成功");
            heraJobHistory.setStatusEnum(StatusEnum.SUCCESS);
            heraJobHistory.setEndTime(endTime);
            heraJobHistory.setStartTime(endTime);
            heraJobHistory.setExecuteHost(host);
            masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, heraJobHistory.getTriggerType(), heraJobHistory);
            masterContext.getDispatcher().forwardEvent(successEvent);
            return;
        }

        //先在数据库中set一些执行任务所需的必须值 然后再加入任务队列
        heraAction.setLastResult(heraAction.getStatus());
        heraAction.setStatus(StatusEnum.RUNNING.toString());
        heraAction.setHistoryId(heraJobHistory.getId());
        heraAction.setStatisticStartTime(new Date());
        heraAction.setStatisticEndTime(null);
        masterContext.getHeraJobActionService().update(heraAction);
        heraJobHistory.getLog().append(ActionUtil.getTodayString() + " 进入任务队列");
        masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));


        boolean isFixed;
        int priorityLevel = 3;
        Map<String, String> configs = StringUtil.convertStringToMap(heraAction.getConfigs());
        String priorityLevelValue = configs.get("run.priority.level");
        if (priorityLevelValue != null) {
            priorityLevel = Integer.parseInt(priorityLevelValue);
        }
        String areaFixed = HeraGlobalEnv.getArea() + Constants.POINT + Constants.HERA_EMR_FIXED;
        if (configs.containsKey(Constants.HERA_EMR_FIXED) || configs.containsKey(areaFixed)) {
            isFixed = Boolean.parseBoolean(configs.get(areaFixed)) || Boolean.parseBoolean(configs.get(Constants.HERA_EMR_FIXED));
        } else {
            isFixed = Boolean.parseBoolean(getInheritVal(heraAction.getGroupId(), areaFixed, Constants.HERA_EMR_FIXED));
        }
        Integer endMinute = masterContext.getHeraJobService().findMustEndMinute(heraAction.getJobId());
        JobElement element = JobElement.builder()
                .jobId(heraJobHistory.getActionId())
                .hostGroupId(heraJobHistory.getHostGroupId())
                .priorityLevel(priorityLevel)
                .historyId(heraJobHistory.getId())
                .fixedEmr(isFixed)
                .owner(heraJob.getOwner())
                .costMinute(endMinute)
                .build();
        try {
            element.setTriggerType(heraJobHistory.getTriggerType());
            HeraAction cacheAction = heraActionMap.get(element.getJobId());
            if (cacheAction != null) {
                cacheAction.setStatus(StatusEnum.RUNNING.toString());
            }
            switch (heraJobHistory.getTriggerType()) {
                case MANUAL:
                    masterContext.getManualQueue().put(element);
                    break;
                case AUTO_RERUN:
                    masterContext.getRerunQueue().put(element);
                    break;
                case MANUAL_RECOVER:
                case SCHEDULE:
                    masterContext.getScheduleQueue().put(element);
                    break;
                case SUPER_RECOVER:
                    masterContext.getSuperRecovery().put(element);
                    break;
                default:
                    ErrorLog.error("不支持的调度类型:{},id:{}", heraJobHistory.getTriggerType().toName(), heraJobHistory.getActionId());
                    break;
            }
        } catch (InterruptedException e) {
            ErrorLog.error("添加任务" + element.getJobId() + "失败", e);
        }
    }


    public boolean checkJobExists(HeraJobHistoryVo heraJobHistory, boolean checkOnly) {
        // 允许重复的话 不检测,重跑任务也不检测
        if (masterContext.getHeraJobService().isRepeat(heraJobHistory.getJobId()) || heraJobHistory.getTriggerType() == TriggerTypeEnum.AUTO_RERUN) {
            return false;
        }
        Long actionId = heraJobHistory.getActionId();
        Integer jobId = heraJobHistory.getJobId();

        boolean exists = false;
        if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL_RECOVER || heraJobHistory.getTriggerType() == TriggerTypeEnum.SCHEDULE) {
            // check调度器等待队列是否有此任务在排队
            for (JobElement jobElement : masterContext.getScheduleQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    exists = true;
                    TaskLog.warn("调度队列已存在该任务，添加失败 {}", actionId);
                }
            }
            // check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (!exists) {
                    for (Long aLong : workHolder.getRunning()) {
                        if (Objects.equals(ActionUtil.getJobId(aLong.toString()), jobId)) {
                            exists = true;
                            TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                            break;
                        }
                    }
                }

            }
        } else if (heraJobHistory.getTriggerType() == TriggerTypeEnum.SUPER_RECOVER) {
            // check调度器等待队列是否有此任务在排队
            for (JobElement jobElement : masterContext.getSuperRecovery()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    exists = true;
                    TaskLog.warn("调度队列已存在该任务，添加失败 {}", actionId);
                }
            }
            // check所有的worker中是否有此任务的id在执行，如果有，不进入队列等待
            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (!exists) {
                    for (Long aLong : workHolder.getSuperRunning()) {
                        if (Objects.equals(ActionUtil.getJobId(aLong.toString()), jobId)) {
                            exists = true;
                            TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                            break;
                        }
                    }
                }

            }
        } else if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {

            for (JobElement jobElement : masterContext.getManualQueue()) {
                if (ActionUtil.jobEquals(jobElement.getJobId(), actionId)) {
                    exists = true;
                    TaskLog.warn("手动任务队列已存在该任务，添加失败 {}", actionId);
                }
            }

            for (MasterWorkHolder workHolder : masterContext.getWorkMap().values()) {
                if (!exists) {
                    for (Long aLong : workHolder.getManningRunning()) {
                        if (Objects.equals(ActionUtil.getJobId(aLong.toString()), jobId)) {
                            exists = true;
                            TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
                            break;
                        }
                    }
                }

            }
        }
        if (exists && !checkOnly) {
            heraJobHistory.getLog().append(LogConstant.CHECK_QUEUE_LOG);
            heraJobHistory.setStartTime(new Date());
            heraJobHistory.setEndTime(new Date());
            heraJobHistory.setIllustrate("任务已在调度队列");
            //由于设置为失败会被告警   所以暂时设置为wait状态
            heraJobHistory.setStatusEnum(StatusEnum.WAIT);
            masterContext.getHeraJobHistoryService().update(BeanConvertUtils.convert(heraJobHistory));
        }
        return exists;

    }


    private void notifyAdmin(String title, String content) {
        HeraUser admin = masterContext.getHeraUserService().findByName(HeraGlobalEnv.getAdmin());
        if (admin != null) {
            masterContext.getAlarmCenter().sendToEmail(title, content, admin.getEmail());
            masterContext.getAlarmCenter().sendToPhone(AlarmInfo.builder().message(title + "\n" + content).phone(admin.getPhone()).build());
        } else {
            ErrorLog.error("内部异常{}:{}，找不到{}管理员的联系方式", title, content, HeraGlobalEnv.getAdmin());
        }

    }

    /**
     * work断开的处理
     *
     * @param channel channel
     */
    public void workerDisconnectProcess(Channel channel) {
        String ip = getIpFromChannel(channel);
        ErrorLog.error("work:{}断线", ip);
        notifyAdmin("警告:work断线了", ip);
        MasterWorkHolder workHolder = masterContext.getWorkMap().get(channel);
        masterContext.getWorkMap().remove(channel);
        if (workHolder != null) {
            Set<Long> scheduleTask = workHolder.getRunning();

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

                        for (Long action : scheduleTask) {
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
                                        , BeanConvertUtils.convert(heraJobHistory));
                                heraAction.setStatus(heraJobHistory.getStatus());
                                masterContext.getHeraJobActionService().updateStatus(heraAction.getId(), heraAction.getStatus());
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
                                    TimeUnit.SECONDS.sleep(HeraGlobalEnv.getHeartBeat() * 2);

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
                        for (Long action : scheduleTask) {
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

    public void startNewJob(HeraJobHistory heraJobHistory, String illustrate) {
        HeraJob heraJob = masterContext.getHeraJobService().findById(heraJobHistory.getJobId());
        if (heraJob == null || heraJob.getAuto() == 0 || !checkJobRun(heraJob)) {
            ScheduleLog.warn("任务已关闭或者删除，取消重跑." + heraJobHistory.getJobId());
            return;
        }
        heraJobHistory.setStatus(StatusEnum.FAILED.toString());
        masterContext.getHeraJobHistoryService().update(heraJobHistory);
        HeraJobHistory newHistory = HeraJobHistory.builder().
                actionId(heraJobHistory.getActionId()).
                illustrate(illustrate).
                jobId(heraJobHistory.getJobId()).
                triggerType(heraJobHistory.getTriggerType()).
                operator(heraJobHistory.getOperator()).
                hostGroupId(heraJobHistory.getHostGroupId()).
                batchId(heraJobHistory.getBatchId()).
                bizLabel(heraJobHistory.getBizLabel()).
                log(heraJobHistory.getIllustrate()).build();
        masterContext.getHeraJobHistoryService().insert(newHistory);
        run(BeanConvertUtils.convert(newHistory), heraJob);
    }

    private String getIpFromChannel(Channel channel) {
        return channel.remoteAddress().toString().split(":")[0];
    }

    public void printThreadPoolLog() {
        masterRunJob.printThreadPoolLog();
    }

    /**
     * 输出批次号
     *
     * @param actionId
     * @param cronPeriod   周期
     * @param cronInterval 间隔
     * @return 批次号；示例actionId=20190102112233,cronPeriod=day,cronInterval=-1,则批次号=2019-01-01
     */
    public String getBatchIdFromActionIdPeriod(Long actionId, String cronPeriod, int cronInterval) {
        cronPeriod = cronPeriod.toLowerCase();
        if (cronPeriod.equals("other")) {
            return actionId.toString();
        } else {
            String dmStr = actionId.toString().substring(0, 14);
            Date currDate = HeraDateTool.StringToDate(dmStr, "yyyyMMddHHmmss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(currDate);
            String outDateStr;
            SimpleDateFormat outDateFormat = new SimpleDateFormat(TimeFormatConstant.YYYY_MM_DD_HH_MM_SS);
            if (cronPeriod.equals("year")) {
                cal.add(Calendar.YEAR, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 4);
            } else if (cronPeriod.equals("month")) {
                cal.add(Calendar.MONTH, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 7);
            } else if (cronPeriod.equals("day")) {
                cal.add(Calendar.DATE, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 10);
            } else if (cronPeriod.equals("hour")) {
                cal.add(Calendar.HOUR, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 13);
            } else if (cronPeriod.equals("minute")) {
                cal.add(Calendar.MINUTE, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 16);
            } else if (cronPeriod.equals("second")) {
                cal.add(Calendar.SECOND, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 19);
            } else {//未知，使用秒方案
                cal.add(Calendar.SECOND, cronInterval);
                outDateStr = outDateFormat.format(cal.getTime());
                return outDateStr.substring(0, 19);
            }
        }
    }


}
