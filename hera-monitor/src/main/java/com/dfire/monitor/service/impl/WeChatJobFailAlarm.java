package com.dfire.monitor.service.impl;

import com.dfire.common.config.Alarm;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.enums.AlarmLevel;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.event.HeraJobFailedEvent;
import com.dfire.monitor.domain.AlarmInfo;
import com.dfire.monitor.service.AlarmCenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

/**
 * desc:
 * 企业微信告警
 *
 * @author scx
 * @create 2019/04/26
 */
@Alarm
public class WeChatJobFailAlarm extends AbstractJobFailAlarm {


    @Autowired
    private AlarmCenter alarmCenter;

    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private HeraJobService heraJobService;

    @Override
    public void alarm(HeraJobFailedEvent failedEvent, Set<HeraSso> monitorUser) {
        HeraJob heraJob = failedEvent.getHeraJob();
        //低于微信等级直接跳过
        if (AlarmLevel.lessThan(heraJob.getOffset(), AlarmLevel.WE_CHAT) || heraJob.getAuto() != 1) {
            return;
        }
        //给监控任务的所有人告警
        String msg = buildJobErrorMsg(heraJob, failedEvent.getRunCount(), monitorUser);
        String split = "|";
        //获得小组组上的工号
        StringBuilder noticeIds = new StringBuilder(
                Optional.ofNullable(heraUserService.findByName(heraJob.getOwner()))
                        .map(HeraUser::getUid)
                        .orElse(""));

        //获得监控该任务的人员的工号
        Optional.ofNullable(monitorUser)
                .ifPresent(users ->
                        users.forEach(user ->
                                noticeIds.append(split).append(user.getJobNumber())));
        //加上关注任务的所有人
        noticeIds.append(split).append(HeraGlobalEnv.getMonitorUsers());
        //给关注所有任务的人发送
        alarmCenter.sendToWeChat(AlarmInfo.builder()
                .message(msg)
                .userId(noticeIds.toString())
                .build());
    }

    @Override
    public void alarm(JobElement element, Set<HeraSso> monitorUser) {
        int id = ActionUtil.getJobId(element.getJobId());
        HeraJob heraJob = heraJobService.findById(id);
        String split = "|";
        StringBuilder noticeIds = new StringBuilder(
                Optional.ofNullable(heraUserService.findByName(heraJob.getOwner()))
                        .map(HeraUser::getUid)
                        .orElse(""));
        Optional.ofNullable(monitorUser)
                .ifPresent(users ->
                        users.forEach(user ->
                                noticeIds.append(split).append(user.getJobNumber())));
        alarmCenter.sendToWeChat(AlarmInfo.builder()
                .message(buildTimeoutMsg(element, monitorUser))
                .userId(noticeIds.toString()).build());
    }
}
