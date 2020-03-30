package com.dfire.monitor.service.impl;

import com.dfire.common.config.Alarm;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.HeraSso;
import com.dfire.common.enums.AlarmLevel;
import com.dfire.common.service.*;
import com.dfire.common.vo.JobElement;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.event.HeraJobFailedEvent;
import com.dfire.logs.ErrorLog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import java.util.Optional;
import java.util.Set;

/**
 * @author xiaosuda
 * @date 2019/2/25
 */
@Alarm("emailJobFailAlarm")
public class EmailJobFailAlarm extends AbstractJobFailAlarm {


    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HeraJobHistoryService heraJobHistoryService;

    @Autowired
    private HeraJobService heraJobService;

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
        try {
            emailService.sendEmail(title, content, address.toString());
        } catch (MessagingException e) {
            ErrorLog.error("发送邮件失败", e);

        }
    }

    @Override
    public void alarm(JobElement element, Set<HeraSso> monitorUser) {
        HeraJobHistory jobHistory = heraJobHistoryService.findById(element.getHistoryId());
        HeraJob heraJob = heraJobService.findById(jobHistory.getJobId());
        String address = getMonitorAddress(monitorUser);
        String title = "【重要】hera调度任务超时[任务=" + heraJob.getName() + "(" + heraJob.getId() + ")]";
        String content = buildTimeoutMsg(element, monitorUser)
                + Constants.HTML_NEW_LINE + "任务日志：" + jobHistory.getLog();
        try {
            emailService.sendEmail(title, content, address);
        } catch (MessagingException e) {
            ErrorLog.error("发送邮件失败", e);
        }
    }

    private String getMonitorAddress(Set<HeraSso> monitorUsers) {
        StringBuilder address = new StringBuilder();
        Optional.ofNullable(monitorUsers).ifPresent(users -> users.forEach(user -> address.append(user.getEmail()).append(Constants.SEMICOLON)));
        return address.toString();
    }
}
