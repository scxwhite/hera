package com.dfire.monitor.service.impl;

import com.dfire.common.service.EmailService;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import com.dfire.monitor.domain.AlarmInfo;
import com.dfire.monitor.service.AlarmCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;


/**
 * desc:
 *
 * @author scx
 * @create 2019/04/27
 */
@Service
public class AlarmCenterImpl implements AlarmCenter {


    @Autowired
    private EmailService emailService;

    @Override
    public boolean sendToWeChat(AlarmInfo alarmInfo) {
        //TODO 使用者自己开发
        MonitorLog.info("[微信告警]userInfo:{},消息结果:{}", alarmInfo.toString(), "请管理员自己设置企业微信告警配置");
        return false;
    }

    @Override
    public boolean sendToPhone(AlarmInfo alarmInfo) {
        //TODO 使用者自己开发
        MonitorLog.info("[电话告警]userInfo:{},消息结果:{}", alarmInfo.toString(), "请管理员自己设置企业微信告警配置");
        return false;
    }

    @Override
    public boolean sendToEmail(String title, String content, String address) {
        try {
            emailService.sendEmail(title, content, address);
            return true;
        } catch (MessagingException e) {
            ErrorLog.error("发送邮件[title:" + title + ",content:" + content + "]失败", e);

        }
        return false;
    }
}
