package com.dfire.core.netty.master.schedule;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.vo.HeraRerunVo;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterWorkHolder;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.ScheduleLog;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * desc:重跑任务的触发
 *
 * @author scx
 * @create 2020/01/06
 */
public class RerunJobLaunch extends ScheduledChore {

    private Master master;

    private RerunJobLaunch(Master master, long initialDelay, long period, TimeUnit unit) {
        super("RerunJobLaunch", initialDelay, period, unit);
        this.master = master;
    }

    public RerunJobLaunch(Master master) {
        this(master, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void chore() {
        if (master.getMasterContext().isStop()) {
            ScheduleLog.info("master is on stop status,stop run rerunJobLaunch");
            return;
        }
        Integer hourOfDay = ActionUtil.getHourOfDay();
        //重跑时间范围检测
        if (!(hourOfDay >= HeraGlobalEnv.getRerunStartTime() && hourOfDay < HeraGlobalEnv.getRerunEndTime())) {
            return;
        }
        List<HeraRerunVo> rerunVos = master.getMasterContext().getHeraRerunService().findByEnd(0);
        for (HeraRerunVo rerunVo : rerunVos) {
            if (!Boolean.TRUE.toString().equals(rerunVo.getExtra().get(Constants.ACTION_DONE))) {
                continue;
            }
            BlockingQueue<JobElement> rerunQueue = new LinkedBlockingQueue<>(master.getMasterContext().getRerunQueue());
            int isRunning = 0;
            for (JobElement element : rerunQueue) {
                if (Objects.equals(ActionUtil.getJobId(element.getJobId()), rerunVo.getJobId())) {
                    isRunning++;
                }
            }
            for (MasterWorkHolder workHolder : new ArrayList<>(master.getMasterContext().getWorkMap().values())) {
                for (Long aLong : workHolder.getRerunRunning()) {
                    if (Objects.equals(ActionUtil.getJobId(aLong.toString()), rerunVo.getJobId())) {
                        isRunning++;
                    }
                }
            }
            int rerunThread = Integer.parseInt(rerunVo.getExtra().getOrDefault(Constants.RERUN_THREAD, "1"));
            if (rerunThread > isRunning) {
                rerunThread -= isRunning;
                Long startAction = rerunVo.getActionNow() == null || rerunVo.getActionNow() == 0L ? ActionUtil.getActionByDateStr(rerunVo.getStartTime()) - 1 : rerunVo.getActionNow();
                Long endAction = ActionUtil.getActionByDateStr(rerunVo.getEndTime());
                List<HeraAction> heraActions;
                if (Boolean.parseBoolean(rerunVo.getExtra().getOrDefault(Constants.RERUN_FAILED, "false"))) {
                    String lastRerunId = rerunVo.getExtra().get(Constants.LAST_RERUN_ID);
                    if (lastRerunId == null) {
                        ErrorLog.error("last rerun id is null ,cancel rerun");
                        rerunVo.setIsEnd(1);
                        master.getMasterContext().getHeraRerunService().updateById(rerunVo);
                        continue;
                    }
                    heraActions = master.getMasterContext()
                            .getHeraJobHistoryService()
                            .findRerunFailedIdsByLimit(startAction, rerunVo.getJobId(), lastRerunId, rerunThread)
                            .stream()
                            .map(history -> HeraAction.builder().id(history.getActionId()).owner(history.getOperator()).hostGroupId(history.getHostGroupId()).build())
                            .collect(Collectors.toList());
                } else {
                    heraActions = master.getMasterContext().getHeraJobActionService()
                            .findByStartAndEnd(startAction, endAction, rerunVo.getJobId(), rerunThread);
                }
                if (heraActions.size() > 0) {
                    heraActions.stream().sorted(Comparator.comparingLong(HeraAction::getId)).forEach(action -> {
                        HeraJobHistory history = HeraJobHistory.builder()
                                .actionId(action.getId())
                                .illustrate("自动重跑")
                                .jobId(rerunVo.getJobId())
                                .triggerType(TriggerTypeEnum.AUTO_RERUN.getId())
                                .operator(action.getOwner())
                                .hostGroupId(action.getHostGroupId())
                                .properties(StringUtil.convertMapToString(Collections.singletonMap(Constants.RERUN_ID, String.valueOf(rerunVo.getId())))).
                                        build();
                        master.getMasterContext().getHeraJobHistoryService().insert(history);
                        master.run(BeanConvertUtils.convert(history), master.getMasterContext().getHeraJobService().findMemById(rerunVo.getJobId()));
                        master.getMasterContext().getHeraRerunService().updateById(HeraRerunVo.builder()
                                .id(rerunVo.getId())
                                .actionNow(history.getActionId())
                                .build());
                    });
                }
            }
        }

    }


}
