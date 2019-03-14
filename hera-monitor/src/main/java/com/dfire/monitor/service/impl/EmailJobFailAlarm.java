package com.dfire.monitor.service.impl;

import com.alibaba.fastjson.JSONObject;
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
import com.dfire.monitor.config.Alarm;
import com.dfire.monitor.service.JobFailAlarm;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.mail.MessagingException;

/**
 * @author xiaosuda
 * @date 2019/2/25
 */
@Alarm
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
    	alarm(actionId,null);
    }
    @Override
    public void alarm(String actionId,String errorMsg) {
        Integer jobId = ActionUtil.getJobId(actionId);
        if (jobId == null) {
            return;
        }
        HeraJob heraJob = heraJobService.findById(jobId);
        System.out.println(JSONObject.toJSONString(heraJob));
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
            
            String title="hera调度任务失败[任务="+heraJob.getName()+"("+heraJob.getId()+"),版本号="+actionId+"]";
            String content="任务ID：" + heraJob.getId() + Constants.HTML_NEW_LINE 
    		 		+ "任务名：" + heraJob.getName()+ Constants.HTML_NEW_LINE 
    		 		+ "任务版本号：" + actionId+ Constants.HTML_NEW_LINE 
    		 		+ "任务描述：" + heraJob.getDescription()+ Constants.HTML_NEW_LINE 
    		 		+ "任务OWNER：" + heraJob.getOwner()  + Constants.HTML_NEW_LINE 
    		 		;
            if(errorMsg != null){
            	content+=Constants.HTML_NEW_LINE +Constants.HTML_NEW_LINE + "--------------------------------------------"+Constants.HTML_NEW_LINE +errorMsg;
            }
            emailService.sendEmail(title,content , address.toString());
        } catch (MessagingException e) {
            e.printStackTrace();
            ErrorLog.error("发送邮件失败");
        }
    }
}
