package com.dfire.core.netty.master.schedule;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraActionMani;
import com.dfire.common.enums.CycleEnum;
import com.dfire.common.enums.JobScheduleTypeEnum;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.core.event.Dispatcher;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.constant.MasterConstant;
import com.dfire.event.Events;
import com.dfire.event.HeraJobLostEvent;
import com.dfire.event.HeraJobSuccessEvent;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import com.dfire.logs.ScheduleLog;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * desc:任务信号丢失定时检测backup
 *
 * @author scx
 * @create 2020/01/06
 */
public class LostJobCheck extends ScheduledChore {

    private final Master master;

    private LostJobCheck(Master master, long initialDelay, long period, TimeUnit unit) {
        super("LostJobCheck", initialDelay, period, unit);
        this.master = master;
    }

    public LostJobCheck(Master master, Integer minuteOfHour) {
        this(master, minuteOfHour <= 30 ? 40 - minuteOfHour : 70 - minuteOfHour, 30, TimeUnit.MINUTES);
    }


    @Override
    protected void chore() {
        if (master.getMasterContext().isStop()) {
            ScheduleLog.info("master is on stop status,stop run LostJobCheck");
            return;
        }
        //信号丢失检测
        ScheduleLog.info("refresh host group success, start roll back");
        master.getMasterContext().refreshHostGroupCache();
        String currDate = ActionUtil.getCurrActionVersion();
        Dispatcher dispatcher = master.getMasterContext().getDispatcher();
        if (dispatcher != null) {
            Long endAction = Long.parseLong(currDate) - MasterConstant.PRE_CHECK_MIN;
            List<HeraActionMani> manifest = master.getMasterContext().getHeraJobActionService().getAllManifest(endAction);
            if (manifest != null && manifest.size() > 0) {
                Map<Long, HeraActionMani> actionMapNew = manifest.stream().collect(Collectors.toMap(HeraActionMani::getId, heraActionMani -> heraActionMani));
                for (Long actionId : actionMapNew.keySet()) {
                    if (actionId <= endAction) {
                        rollBackLostJob(actionId, actionMapNew);
                        checkLostSingle(actionId, actionMapNew);
                    }
                }
            }
        }
    }


    /**
     * 漏跑检测
     *
     * @param actionId     版本id
     * @param actionMapNew actionMap集合
     */

    private void rollBackLostJob(Long actionId, Map<Long, HeraActionMani> actionMapNew) {
        HeraActionMani lostJob = actionMapNew.get(actionId);
        boolean isCheck = lostJob != null
                && lostJob.getAuto() == 1
                && (StringUtils.isBlank(lostJob.getStatus()));
        if (isCheck && master.checkJobRun(master.getMasterContext().getHeraJobService().findById(lostJob.getJobId()))) {
            boolean isAllComplete = true;
            HeraActionMani heraAction;
            //依赖任务
            if (lostJob.getScheduleType().equals(JobScheduleTypeEnum.Dependent.getType())) {
                String[] jobDependList = lostJob.getDependencies().split(Constants.COMMA);
                for (String jobDepend : jobDependList) {
                    heraAction = actionMapNew.get(Long.parseLong(jobDepend));
                    if (heraAction == null || !StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                        isAllComplete = false;
                        break;
                    }
                }
            } else if (lostJob.getScheduleType().equals(JobScheduleTypeEnum.Independent.getType()) && CycleEnum.isSelfDep(lostJob.getCycle())) {
                //定时+自依赖任务
                String[] jobDependList = lostJob.getDependencies().split(Constants.COMMA);
                for (String dep : jobDependList) {
                    if (dep.equals(String.valueOf(lostJob.getId()))) {
                        continue;
                    }
                    heraAction = actionMapNew.get(Long.parseLong(dep));
                    if (heraAction == null || !StatusEnum.SUCCESS.toString().equals(heraAction.getStatus())) {
                        isAllComplete = false;
                        break;
                    }
                }
            }
            if (isAllComplete) {
                addRollBackJob(actionId);
            }
        }
    }

    /**
     * 信号丢失处理
     *
     * @param actionId     hera_action 表信息id /版本id
     * @param actionMapNew hera_action 内存信息 /内存保存的今天版本信息
     */
    private void checkLostSingle(Long actionId, Map<Long, HeraActionMani> actionMapNew) {
        try {
            HeraActionMani checkJob = actionMapNew.get(actionId);
            if (checkJob == null) {
                return;
            }
            if (StatusEnum.RUNNING.toString().equals(checkJob.getStatus())) {
                HeraJobHistory actionHistory = master.getMasterContext().getHeraJobHistoryService().findById(checkJob.getHistoryId());
                if (actionHistory == null) {
                    return;
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(actionHistory.getStatus()) && !actionHistory.getStatus().equals(StatusEnum.RUNNING.toString())) {
                    master.getMasterContext().getMasterSchedule().schedule(() -> {
                        HeraAction newAction = master.getMasterContext().getHeraJobActionService().findById(actionId);
                        if (StatusEnum.RUNNING.toString().equals(newAction.getStatus()) && newAction.getHistoryId().equals(checkJob.getHistoryId())) {
                            if (actionHistory.getTriggerType().equals(TriggerTypeEnum.SCHEDULE.getId())
                                    || actionHistory.getTriggerType().equals(TriggerTypeEnum.MANUAL_RECOVER.getId())
                                    || actionHistory.getTriggerType().equals(TriggerTypeEnum.SUPER_RECOVER.getId())) {
                                master.getMasterContext().getWorkMap().values().forEach(workHolder -> {
                                    workHolder.getRunning().remove(actionId);
                                    workHolder.getSuperRunning().remove(actionId);
                                    workHolder.getRerunRunning().remove(actionId);
                                    workHolder.getManningRunning().remove(actionId);
                                });
                                if (actionHistory.getStatus().equals(StatusEnum.SUCCESS.toString())) {
                                    MonitorLog.info("history_id:{},action_id:{}信号丢失，但历史执行成功，直接广播并更新状态", newAction.getHistoryId(), actionId);
                                    HeraJobSuccessEvent successEvent = new HeraJobSuccessEvent(actionId, TriggerTypeEnum.parser(actionHistory.getTriggerType())
                                            , BeanConvertUtils.convert(actionHistory));
                                    master.getMasterContext().getDispatcher().forwardEvent(successEvent);
                                    master.getMasterContext().getHeraJobActionService().updateStatus(actionId, StatusEnum.SUCCESS.toString());
                                } else {
                                    MonitorLog.warn("任务信号丢失重试actionId:{},historyId:{}", actionId, newAction.getHistoryId());
                                    master.startNewJob(actionHistory, "任务信号丢失重试");
                                }
                            }
                        }
                    }, 30, TimeUnit.SECONDS);

                }
            }
        } catch (Exception e) {
            ErrorLog.error("信号丢失检测异常", e);
        }

    }

    private void addRollBackJob(Long actionId) {
        master.getMasterContext().getDispatcher().forwardEvent(new HeraJobLostEvent(Events.UpdateJob, actionId));
        ScheduleLog.info("roll back lost actionId :" + actionId);
    }
}
