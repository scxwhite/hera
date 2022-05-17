package com.dfire.core.netty.master.schedule;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraRerunVo;
import com.dfire.common.util.ActionUtil;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.logs.MonitorLog;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * desc:重跑任务的初始化，比如生成版本，总共要执行的次数等
 *
 * @author scx
 * @create 2020/01/06
 */
public class RerunJobInit extends ScheduledChore {

    private Master master;

    private RerunJobInit(Master master, long initialDelay, long period, TimeUnit unit) {
        super("RerunJobInit",initialDelay, period, unit);
        this.master = master;
    }

    public RerunJobInit(Master master) {
        this(master, 60, 300, TimeUnit.SECONDS);
    }

    @Override
    protected void chore() {
        List<HeraRerunVo> rerunVos = master.getMasterContext().getHeraRerunService().findByEnd(0);
        for (HeraRerunVo rerunVo : rerunVos) {
            if (Boolean.TRUE.toString().equals(rerunVo.getExtra().get(Constants.ACTION_DONE))) {
                continue;
            }
            HeraJob heraJob = master.getMasterContext().getHeraJobService().findMemById(rerunVo.getJobId());
            Long nowAction = Long.parseLong(ActionUtil.getCurrActionVersion());
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.setTimeInMillis(ActionUtil.getMillisFromStrDate(rerunVo.getStartTime()));
            endTime.setTimeInMillis(ActionUtil.getMillisFromStrDate(rerunVo.getEndTime()));
            List<HeraJob> heraJobs = new ArrayList<>();
            heraJobs.add(heraJob);
            if (heraJob.getScheduleType() == 1) {
                heraJobs.addAll(master.getParentJob(heraJob.getDependencies(), new HashSet<>()));
            }
            String cronDate;
            int allAction = 0;
            Long startAction = ActionUtil.getActionByDateStr(rerunVo.getStartTime());
            Long endAction = ActionUtil.getActionByDateStr(rerunVo.getEndTime());
            long formatNum = 10000000000L;
            master.getMasterContext().getHeraJobActionService().deleteAction(startAction / formatNum * formatNum, endAction / formatNum * formatNum, rerunVo.getJobId());
            while (startTime.compareTo(endTime) <= 0) {
                Map<Long, HeraAction> actionMap = new HashMap<>(8);
                Map<Integer, List<HeraAction>> idMap = new HashMap<>(8);
                Map<Integer, HeraJob> jobMap = new HashMap<>(8);
                cronDate = ActionUtil.getActionVersionPrefix(startTime.getTime());
                heraJobs = master.topologicalSort(heraJobs);
                master.generateScheduleJobAction(heraJobs, cronDate, actionMap, nowAction, idMap, jobMap);
                jobMap.values().forEach(job -> master.generateDependJobAction(job, actionMap, nowAction, idMap));
                startTime.add(Calendar.DAY_OF_YEAR, 1);
                for (Long aLong : actionMap.keySet()) {
                    if (Objects.equals(ActionUtil.getJobId(String.valueOf(aLong)), rerunVo.getJobId()) && aLong >= startAction && aLong <= endAction) {
                        allAction++;
                    }
                }
            }
            rerunVo.getExtra().put(Constants.ACTION_DONE, Boolean.TRUE.toString());
            rerunVo.getExtra().put(Constants.ACTION_PROCESS_NUM, "0");
            rerunVo.getExtra().put(Constants.ACTION_ALL_NUM, String.valueOf(allAction));
            if (allAction == 0) {
                rerunVo.setIsEnd(1);
            }
            master.getMasterContext().getHeraRerunService().updateById(rerunVo);
            MonitorLog.info("添加重跑任务{}成功,start:{},end:{}", rerunVo.getJobId(), rerunVo.getStartTime(), rerunVo.getEndTime());
        }
    }
}
