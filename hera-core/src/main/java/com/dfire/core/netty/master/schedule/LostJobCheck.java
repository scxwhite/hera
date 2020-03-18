package com.dfire.core.netty.master.schedule;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.ActionUtil;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.event.Events;
import com.dfire.event.HeraJobLostEvent;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeartLog;
import com.dfire.logs.ScheduleLog;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * desc:任务信号丢失定时检测backup
 *
 * @author scx
 * @create 2020/01/06
 */
public class LostJobCheck extends ScheduledChore {

    private Master master;

    private LostJobCheck(Master master, long initialDelay, long period, TimeUnit unit) {
        super("LostJobCheck", initialDelay, period, unit);
        this.master = master;
    }

    public LostJobCheck(Master master, Integer minuteOfHour) {
        this(master, minuteOfHour <= 30 ? 40 - minuteOfHour : 70 - minuteOfHour, 30, TimeUnit.MINUTES);
    }

    @Override
    protected void chore() {
        //信号丢失检测
        ScheduleLog.info("refresh host group success, start roll back");
        master.getMasterContext().refreshHostGroupCache();
        String currDate = ActionUtil.getCurrActionVersion();
        Dispatcher dispatcher = master.getMasterContext().getDispatcher();
        if (dispatcher != null) {
            Map<Long, HeraAction> actionMapNew = new HashMap<>(master.getHeraActionMap());
            if (actionMapNew.size() > 0) {
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
        if (isCheck && master.checkJobRun(master.getMasterContext().getHeraJobService().findById(lostJob.getJobId()))) {
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
                HeraJobHistory actionHistory = master.getMasterContext().getHeraJobHistoryService().findById(checkJob.getHistoryId());
                if (actionHistory == null) {
                    return;
                }
                if (actionHistory.getStatus() != null && !actionHistory.getStatus().equals(StatusEnum.RUNNING.toString())) {
                    master.getMasterContext().getMasterSchedule().schedule(() -> {
                        HeraAction newAction = master.getMasterContext().getHeraJobActionService().findById(actionId);
                        if (StatusEnum.RUNNING.toString().equals(newAction.getStatus())) {
                            HeartLog.warn("任务信号丢失actionId:{},historyId:{}", actionId, newAction.getHistoryId());
                            boolean scheduleType = actionHistory.getTriggerType().equals(TriggerTypeEnum.SCHEDULE.getId())
                                    || actionHistory.getTriggerType().equals(TriggerTypeEnum.MANUAL_RECOVER.getId());
                            //TODO 可以选择重跑 or 广播 + 设置状态 这里偷懒 直接重跑
                            master.getMasterContext().getWorkMap().values().forEach(workHolder -> {
                                if (scheduleType) {
                                    workHolder.getRunning().remove(actionId);
                                } else {
                                    workHolder.getManningRunning().remove(actionId);
                                }
                            });
                            master.startNewJob(actionHistory, "任务信号丢失重试");
                        }
                    }, 1, TimeUnit.MINUTES);

                }
            }
        } catch (Exception e) {
            ErrorLog.error("信号丢失检测异常", e);
        }

    }

    private void addRollBackJob(List<Long> actionIdList, Long actionId) {
        if (!actionIdList.contains(actionId) &&
                !master.checkJobExists(HeraJobHistoryVo
                        .builder()
                        .actionId(actionId)
                        .triggerType(TriggerTypeEnum.SCHEDULE)
                        .jobId((ActionUtil.getJobId(actionId)))
                        .build(), true)) {
            master.getMasterContext().getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId));
            actionIdList.add(actionId);
            ScheduleLog.info("roll back lost actionId :" + actionId);
        }
    }
}
