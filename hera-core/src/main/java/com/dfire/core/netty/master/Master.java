package com.dfire.core.netty.master;


import com.dfire.common.constants.Constants;
import com.dfire.common.constants.LogConstant;
import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
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
import com.dfire.core.event.listenter.HeraAddJobListener;
import com.dfire.core.event.listenter.HeraDebugListener;
import com.dfire.core.event.listenter.HeraJobFailListener;
import com.dfire.core.event.listenter.HeraJobSuccessListener;
import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.core.route.loadbalance.LoadBalance;
import com.dfire.core.route.loadbalance.LoadBalanceFactory;
import com.dfire.core.util.CronParse;
import com.dfire.event.Events;
import com.dfire.event.HeraJobLostEvent;
import com.dfire.event.HeraJobMaintenanceEvent;
import com.dfire.event.HeraJobSuccessEvent;
import com.dfire.logs.*;
import com.dfire.monitor.domain.AlarmInfo;
import io.netty.channel.Channel;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
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
        if (HeraGlobalEnv.getEnv().equalsIgnoreCase(Constants.PRE_ENV)) {
            //可以在此关闭预发环境执行任务
            //masterContext.getDispatcher().addDispatcherListener(new HeraStopScheduleJobListener());
        }
        masterContext.getThreadPool().execute(() -> {
            HeraLog.info("-----------------------------init action,time: {}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().addDispatcherListener(new HeraAddJobListener(this, masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobFailListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraDebugListener(masterContext));
            masterContext.getDispatcher().addDispatcherListener(new HeraJobSuccessListener(masterContext));
            List<HeraAction> allJobList = masterContext.getHeraJobActionService().getAfterAction(getBeforeDayAction());
            HeraLog.info("-----------------------------action size:{}, time {}-----------------------------", allJobList.size(), System.currentTimeMillis());
            heraActionMap = new ConcurrentHashMap<>(allJobList.size());
            allJobList.forEach(heraAction -> {
                masterContext.getDispatcher().
                        addJobHandler(new JobHandler(heraAction.getId().toString(), this, masterContext));
                heraActionMap.put(heraAction.getId(), heraAction);
            });
            HeraLog.info("-----------------------------add actions to handler success, time:{}-----------------------------", System.currentTimeMillis());
            masterContext.getDispatcher().forwardEvent(Events.Initialize);
            HeraLog.info("-----------------------------dispatcher actions success, time{}-----------------------------", System.currentTimeMillis());
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
        });
    }

    private long getBeforeDayAction() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -HeraGlobalEnv.getJobCacheDay());
        return Long.parseLong(ActionUtil.getActionVersionByDate(calendar.getTime()));
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
                ErrorLog.error("定时任务异常", e);
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
                ErrorLog.error("定时任务异常", e);
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
        if (isCheck && checkJobRun(masterContext.getHeraJobService().findById(lostJob.getJobId()))) {
            String dependencies = lostJob.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                List<String> jobDependList = Arrays.asList(dependencies.split(Constants.COMMA));
                boolean isAllComplete = false;
                HeraAction heraAction;
                if (jobDependList.size() > 0) {
                    for (String jobDepend : jobDependList) {
                        heraAction = actionMapNew.get(Long.parseLong(jobDepend));
                        if (heraAction == null || !(isAllComplete = StatusEnum.SUCCESS.toString().equals(heraAction.getStatus()))) {
                            break;
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
            if (StatusEnum.RUNNING.toString().equals(checkJob.getStatus())) {
                HeraJobHistory actionHistory = masterContext.getHeraJobHistoryService().findById(checkJob.getHistoryId());
                if (actionHistory == null) {
                    return;
                }
                if (actionHistory.getStatus() != null && !actionHistory.getStatus().equals(StatusEnum.RUNNING.toString())) {
                    masterContext.getMasterSchedule().schedule(() -> {
                        HeraAction newAction = masterContext.getHeraJobActionService().findById(String.valueOf(actionId));
                        if (StatusEnum.RUNNING.toString().equals(newAction.getStatus())) {
                            HeartLog.warn("任务信号丢失actionId:{},historyId:{}", actionId, newAction.getHistoryId());
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
            ErrorLog.error("信号丢失检测异常", e);
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

            private Integer nextTime = HeraGlobalEnv.getScanRate();

            @Override
            public void run() {
                try {
                    if (!masterRunJob.isTaskLimit() && scan()) {
                        nextTime = HeraGlobalEnv.getScanRate();
                    } else {
                        nextTime = (nextTime + DELAY_TIME) > MAX_DELAY_TIME ? MAX_DELAY_TIME : nextTime + DELAY_TIME;
                    }
                } catch (Exception e) {
                    ScanLog.error("scan waiting queueTask exception", e);
                } finally {
                    masterContext.masterSchedule.schedule(this, nextTime, TimeUnit.MILLISECONDS);
                }
            }
        }, HeraGlobalEnv.getScanRate(), TimeUnit.MILLISECONDS);
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
                        jobList.addAll(getParentJob(heraJob.getDependencies()));
                    }
                    actionMap = heraActionMap;
                    List<Long> shouldRemove = new ArrayList<>();
                    for (Long actionId : actionMap.keySet()) {
                        if (StringUtil.actionIdToJobId(String.valueOf(actionId), String.valueOf(jobId))) {
                            shouldRemove.add(actionId);
                        }
                    }
                    shouldRemove.forEach(actionMap::remove);
                    List<AbstractHandler> handlers = new ArrayList<>(masterContext.getDispatcher().getJobHandlers());
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
                generateScheduleJobAction(jobList, cronDate, actionMap, nowAction, idMap, jobMap);
                for (Map.Entry<Integer, HeraJob> entry : jobMap.entrySet()) {
                    generateDependJobAction(jobMap, entry.getValue(), actionMap, nowAction, idMap);
                }
                if (executeHour < ActionUtil.ACTION_CREATE_MAX_HOUR) {
                    heraActionMap = actionMap;
                }
                Dispatcher dispatcher = masterContext.getDispatcher();
                if (dispatcher != null) {
                    if (actionMap.size() > 0) {
                        for (Long id : actionMap.keySet()) {
                            dispatcher.addJobHandler(new JobHandler(id.toString(), masterContext.getMaster(), masterContext));
                            //如果是今天的版本 更新缓存
                            if (ActionUtil.isTodayActionVersion(id.toString())) {
                                dispatcher.forwardEvent(new HeraJobMaintenanceEvent(Events.UpdateActions, id.toString()));
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
     * 递归获取所有父级依赖任务
     *
     * @param dpIdStr
     * @return
     */
    private List<HeraJob> getParentJob(String dpIdStr) {
        List<HeraJob> jobList = new ArrayList<>();
        Arrays.stream(dpIdStr.split(Constants.COMMA)).forEach(id -> {
            HeraJob dpJob = masterContext.getHeraJobService().findById(Integer.parseInt(id));
            if (dpJob.getScheduleType() == 1) {
                jobList.addAll(getParentJob(dpJob.getDependencies()));
            }
            jobList.add(dpJob);
        });
        return jobList;
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
    public void generateScheduleJobAction(List<HeraJob> jobList, String cronDate, Map<Long, HeraAction> actionMap, Long nowAction, Map<Integer, List<HeraAction>> idMap, Map<Integer, HeraJob> jobMap) {
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
        String dayAction = String.valueOf(getBeforeDayAction());
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
     * @param jobMap    任务映射map
     * @param heraJob   当前生成版本的任务
     * @param actionMap 版本map
     * @param nowAction 生成版本时间的action
     * @param idMap     job的id集合  只要已经检测过的id都放入idSet中
     */
    private void generateDependJobAction(Map<Integer, HeraJob> jobMap, HeraJob heraJob, Map<Long, HeraAction> actionMap, Long nowAction, Map<Integer, List<HeraAction>> idMap) {
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
                //如果idSet不包含依赖任务dpId  则递归查找
                if (!idMap.containsKey(dpId)) {
                    generateDependJobAction(jobMap, jobMap.get(dpId), actionMap, nowAction, idMap);
                }
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
                        masterContext.getHeraJobActionService().insert(actionNew, nowAction);
                        actionMap.put(actionNew.getId(), actionNew);
                        insertList.add(actionNew);
                    }
                    idMap.put(heraJob.getId(), insertList);
                }
            }
        }

    }


    /**
     * 扫描任务等待队列，取出任务去执行
     */
    public boolean scan() throws InterruptedException {
        boolean hasTask = false;
        if (!masterContext.getScheduleQueue().isEmpty()) {
            JobElement jobElement = masterContext.getScheduleQueue().take();
            MasterWorkHolder selectWork = getRunnableWork(jobElement);
            if (selectWork == null) {
                masterContext.getScheduleQueue().put(jobElement);
                ScheduleLog.warn("can not get work to execute Schedule job in master,job is:{}", jobElement.toString());
            } else {
                masterRunJob.run(selectWork, jobElement);
                hasTask = true;
            }
        }

        if (!masterContext.getManualQueue().isEmpty()) {
            JobElement jobElement = masterContext.getManualQueue().take();
            MasterWorkHolder selectWork = getRunnableWork(jobElement);
            if (selectWork == null) {
                masterContext.getManualQueue().put(jobElement);
                ScheduleLog.warn("can not get work to execute ManualQueue job in master,job is:{}", jobElement.toString());
            } else {
                masterRunJob.run(selectWork, jobElement);
                hasTask = true;
            }
        }

        if (!masterContext.getDebugQueue().isEmpty()) {
            JobElement jobElement = masterContext.getDebugQueue().take();
            MasterWorkHolder selectWork = getRunnableWork(jobElement);
            if (selectWork == null) {
                masterContext.getDebugQueue().put(jobElement);
                ScheduleLog.warn("can not get work to execute DebugQueue job in master,job is:{}", jobElement.toString());
            } else {
                masterRunJob.run(selectWork, jobElement);
                hasTask = true;
            }

        }
        return hasTask;

    }


    /**
     * 获取hostGroupId中可以分发任务的worker
     *
     * @param jobElement job 部分信息
     * @return
     */
    private MasterWorkHolder getRunnableWork(JobElement jobElement) {
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
        String actionId = heraJobHistory.getActionId();
        heraJobHistory.setStatusEnum(StatusEnum.RUNNING);
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
            HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, heraJobHistory.getTriggerType(), heraJobHistory.getId());
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
                .costMinute(endMinute)
                .build();
        try {
            if (heraJobHistory.getTriggerType() == TriggerTypeEnum.MANUAL) {
                element.setTriggerType(TriggerTypeEnum.MANUAL);
                masterContext.getManualQueue().put(element);
            } else {
                element.setTriggerType(TriggerTypeEnum.SCHEDULE);
                masterContext.getScheduleQueue().put(element);
            }
        } catch (InterruptedException e) {
            ErrorLog.error("添加任务" + element.getJobId() + "失败", e);
        }
    }


    public boolean checkJobExists(HeraJobHistoryVo heraJobHistory, boolean checkOnly) {
        // 允许重复的话 不检测
        if (masterContext.getHeraJobService().isRepeat(heraJobHistory.getJobId())) {
            return false;
        }
        String actionId = heraJobHistory.getActionId();
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
                if (workHolder.getRunning().contains(jobId)) {
                    exists = true;
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
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
                if (workHolder.getManningRunning().contains(jobId)) {
                    exists = true;
                    TaskLog.warn("该任务正在执行，添加失败 {}", actionId);
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
                log(heraJobHistory.getIllustrate()).build();
        masterContext.getHeraJobHistoryService().insert(newHistory);
        this.run(BeanConvertUtils.convert(newHistory), heraJob);
    }

    private String getIpFromChannel(Channel channel) {
        return channel.remoteAddress().toString().split(":")[0];
    }

    public void printThreadPoolLog() {
        masterRunJob.printThreadPoolLog();
    }
}
