package com.dfire.monitor.service.impl;

import com.dfire.common.config.Alarm;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.enums.AlarmLevel;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.service.JobFailAlarm;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.event.HeraJobFailedEvent;
import com.dfire.monitor.service.AlarmCenter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

/**
 * @author xiaosuda
 * @date 2019/2/25
 */
@Alarm("emailJobFailAlarm")
public class EmailJobFailAlarm implements JobFailAlarm {


    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private AlarmCenter alarmCenter;

    @Override
    public void alarm(HeraJobFailedEvent failedEvent, Set<HeraSso> monitorUser) {
        HeraJob heraJob = failedEvent.getHeraJob();
        if (AlarmLevel.lessThan(heraJob.getOffset(), AlarmLevel.EMAIL) || heraJob.getAuto() != 1) {
            return;
        }
        StringBuilder address = new StringBuilder();
        Optional.ofNullable(heraUserService.findByName(heraJob.getOwner()))
                .ifPresent(user -> address.append(user.getEmail()).append(Constants.SEMICOLON));
        StringBuilder owner = new StringBuilder();
        Optional.ofNullable(monitorUser)
                .ifPresent(users ->
                        users.forEach(user -> {
                                    owner.append(user.getName()).append(Constants.SEMICOLON);
                                    address.append(user.getEmail()).append(Constants.SEMICOLON);
                                }
                        ));
        String title = "【重要】hera调度任务失败[任务=" + heraJob.getName() + "(" + heraJob.getId() + ")]";
        String content = "任务ID：" + heraJob.getId() + Constants.HTML_NEW_LINE
                + "环境：" + HeraGlobalEnv.getArea() + "(" + HeraGlobalEnv.getEnv() + ")" + Constants.HTML_NEW_LINE
                + "任务名：" + heraJob.getName() + Constants.HTML_NEW_LINE
                + "任务版本号：" + failedEvent.getActionId() + Constants.HTML_NEW_LINE
                + "任务描述：" + heraJob.getDescription() + Constants.HTML_NEW_LINE
                + "任务负责人：" + owner.toString() + Constants.HTML_NEW_LINE;
        String errorMsg = failedEvent.getHeraJobHistory().getLog().getMailContent();
        address.append(HeraGlobalEnv.monitorEmails);
        if (errorMsg != null) {
            content += Constants.HTML_NEW_LINE + Constants.HTML_NEW_LINE + "--------------------------------------------" + Constants.HTML_NEW_LINE + errorMsg;
        }
        alarmCenter.sendToEmail(title, content, address.toString());
    }

    @Override
    public void alarm(JobElement element) {

    }
}
