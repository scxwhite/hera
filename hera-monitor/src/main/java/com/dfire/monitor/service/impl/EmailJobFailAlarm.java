package com.dfire.monitor.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.EmailService;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.ScheduleLog;
import com.dfire.monitor.service.JobFailAlarm;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

/**
 * @author xiaosuda
 * @date 2019/2/25
 */
@Service
public class EmailJobFailAlarm implements JobFailAlarm {


    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraJobMonitorService heraJobMonitorService;

    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private EmailService emailService;

    @Override
    public void alarm(String actionId) {
        Integer jobId = ActionUtil.getJobId(actionId);
        if (jobId == null) {
            return;
        }
        HeraJob heraJob = heraJobService.findById(jobId);
        //非开启任务不处理  最好能把这些抽取出去 提供接口实现
        if (heraJob.getAuto() != 1 && !Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
            return;
        }
        StringBuilder address = new StringBuilder();
        try {
            HeraJobMonitor monitor = heraJobMonitorService.findByJobId(heraJob.getId());
            if (monitor == null && Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
                ScheduleLog.info("任务无监控人，发送给owner：{}", heraJob.getId());
                HeraUser user = heraUserService.findByName(heraJob.getOwner());
                address.append(user.getEmail().trim());
            } else if (monitor != null) {
                String ids = monitor.getUserIds();
                String[] id = ids.split(Constants.COMMA);
                for (String anId : id) {
                    if (StringUtils.isBlank(anId)) {
                        continue;
                    }
                    HeraUser user = heraUserService.findById(Integer.parseInt(anId));
                    if (user != null && user.getEmail() != null) {
                        address.append(user.getEmail()).append(Constants.SEMICOLON);
                    }
                }
            }
            emailService.sendEmail("hera任务失败了(" + HeraGlobalEnvironment.getEnv() + ")", "任务Id :" + actionId, address.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
            ErrorLog.error("发送邮件失败");
        }
    }
}
