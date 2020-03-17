package com.dfire.monitor.service.impl;

import com.dfire.common.config.Alarm;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.enums.AlarmLevel;
import com.dfire.common.vo.JobElement;
import com.dfire.event.HeraJobFailedEvent;
import com.dfire.monitor.domain.AlarmInfo;
import com.dfire.monitor.service.AlarmCenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

/**
 * 电话告警 使用者自己实现
 *
 * @author xiaosuda
 * @date 2019/3/6
 */
@Alarm
public class PhoneJobFailAlarm extends AbstractJobFailAlarm {

    @Autowired
    private AlarmCenter alarmCenter;

    @Override
    public void alarm(HeraJobFailedEvent failedEvent, Set<HeraSso> monitorUser) {
        HeraJob heraJob = failedEvent.getHeraJob();
        //低于电话等级直接跳过
        if (AlarmLevel.lessThan(heraJob.getOffset(), AlarmLevel.PHONE) || heraJob.getAuto() != 1) {
            return;
        }
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setMessage(buildJobErrorMsg(heraJob, failedEvent.getRunCount(), monitorUser));
        Optional.ofNullable(monitorUser).ifPresent(users ->
                users.forEach(user -> {
                    alarmInfo.setPhone(user.getPhone());
                    alarmCenter.sendToPhone(alarmInfo);
                }));
    }

    @Override
    public void alarm(JobElement element, Set<HeraSso> monitorUser) {

    }
}
