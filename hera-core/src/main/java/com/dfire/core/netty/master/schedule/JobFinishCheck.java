package com.dfire.core.netty.master.schedule;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.util.ActionUtil;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.ScheduledChore;
import com.dfire.core.netty.master.Master;
import com.dfire.core.netty.master.MasterContext;
import com.dfire.logs.MonitorLog;
import com.dfire.monitor.domain.AlarmInfo;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 任务定时是否完成检测
 * @author scx
 */
public class JobFinishCheck extends ScheduledChore {

    private MasterContext masterContext;
    private Master master;
    private Set<JobChecker> checkSet;
    private int lastCheckTime = 0;

    private JobFinishCheck(Master master, long initialDelay, long period, TimeUnit unit) {
        super("JobFinishCheck", initialDelay, period, unit);
        this.master = master;
        this.masterContext = master.getMasterContext();
        checkSet = Sets.newHashSet();
    }

    public JobFinishCheck(Master master) {
        //保证每隔5分钟一次
        this(master, 5 - new DateTime().getMinuteOfHour() % 5, 5, TimeUnit.MINUTES);
        //this(master, 1, 1, TimeUnit.MINUTES);
    }


    private void check() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        //如果是整点 初始化
        if (hour == 0 && minute == 0) {
            lastCheckTime = 0;
            //昨天的就清理掉吧
            checkSet.clear();
        }


        //先检测之前未成功的
        List<JobChecker> finish = new ArrayList<>();
        checkSet.forEach(check -> {
            HeraAction heraAction = master.getMasterContext().getHeraJobActionService().findTodaySuccessByJobId(check.getJobId());
            if (heraAction != null) {
                finish.add(check);
            } else {
                //还需要判断任务是否关闭、结束时间修改等操作
                HeraJob heraJob = master.getMasterContext().getHeraJobService().findById(check.getJobId());
                if (heraJob.getAuto() == 0) {
                    finish.add(check);
                    MonitorLog.info("任务[id:{}]关闭,取消检测", check.getJobId());
                } else if (heraJob.getEstimatedEndHour() > check.getEndTime()) {
                    finish.add(check);
                    MonitorLog.info("任务[id:{}]结束定时修改为{},取消检测此次检测:{}", check.getJobId(), heraJob.getEstimatedEndHour(), check.getEndTime());
                } else {
                    MonitorLog.info("任务[id:{},time:{}]未按时完成", check.getJobId(), ActionUtil.intTOHour(check.getEndTime()));
                    notice(check);
                }
            }
        });
        //删除已经执行完成的
        if (!finish.isEmpty()) {
            checkSet.removeAll(finish);
        }

        int nowTime = ActionUtil.hourToInt(hour + Constants.COLON + minute);
        List<HeraJob> heraJobs = masterContext.getHeraJobService()
                .findEstimatedEndHours(lastCheckTime, nowTime);
        heraJobs.forEach(job -> {
            HeraAction heraAction = master.getMasterContext().getHeraJobActionService().findTodaySuccessByJobId(job.getId());
            if (heraAction != null) {
                MonitorLog.info("任务[id:{},time:{}]按时完成", job.getId(), ActionUtil.intTOHour(job.getEstimatedEndHour()));
            } else {
                MonitorLog.info("任务[id:{},time:{}]未按时完成", job.getId(), ActionUtil.intTOHour(job.getEstimatedEndHour()));
                JobChecker jobChecker = new JobChecker(job.getEstimatedEndHour(), job.getId(), job.getName());
                notice(jobChecker);
                checkSet.add(jobChecker);
            }
        });
        lastCheckTime = nowTime;
    }

    private void notice(JobChecker job) {
        Set<HeraSso> monitorUser = Sets.newConcurrentHashSet();
        Optional.ofNullable(masterContext.getHeraJobMonitorService().findByJobId(job.getJobId()))
                .map(HeraJobMonitor::getUserIds)
                .ifPresent(ids -> Arrays.stream(ids
                        .split(Constants.COMMA))
                        .filter(StringUtils::isNotBlank)
                        .forEach(id -> {
                            Optional.ofNullable(masterContext.getHeraSsoService().findSsoById(Integer.parseInt(id)))
                                    .ifPresent(monitorUser::add);
                        }));
        String split = "|";
        //获得监控该任务的人员的工号
        StringBuilder noticeIds = new StringBuilder();
        Optional.of(monitorUser)
                .ifPresent(users ->
                        users.forEach(user ->
                                noticeIds.append(split).append(user.getJobNumber())));
        //加上关注任务的所有人
        noticeIds.append(split)
                .append(HeraGlobalEnv.getMonitorUsers());
        masterContext.getAlarmCenter().sendToWeChat(AlarmInfo.builder()
                .message("【任务未完成告警】" +
                        "hera任务" + job.getName() + " [" + job.getJobId() + "]\n" +
                        "预计完成时间:" + ActionUtil.intTOHour(job.getEndTime()) + "\n" +
                        "检测时间:" + new DateTime().toString("HH:mm") + "\n" +
                        "环境:" + HeraGlobalEnv.getEnv() + "\n" +
                        "区域:" + HeraGlobalEnv.getArea() + "\n")
                .userId(noticeIds.toString())
                .build());


        String phoneMsg = "赫拉任务" + job.getJobId() + "未按时完成请尽快排查";

        Optional.of(monitorUser)
                .ifPresent(users ->
                        users.forEach(user -> {
                            masterContext.getAlarmCenter().sendToPhone(AlarmInfo.builder()
                                    .message(phoneMsg)
                                    .phone(user.getPhone())
                                    .build());
                        }));

    }

    @Override
    protected void chore() {
        check();
    }

    class JobChecker {
        @Getter
        private int endTime;

        @Getter
        private String name;

        @Getter
        private Integer jobId;


        JobChecker(int endTime, Integer jobId, String name) {
            this.endTime = endTime;
            this.jobId = jobId;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JobChecker)) {
                return false;
            }
            return ((JobChecker) obj).getJobId().equals(this.getJobId());
        }

        @Override
        public int hashCode() {
            return jobId.hashCode();
        }
    }
}
