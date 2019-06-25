package com.dfire.monitor.service.impl;

import com.dfire.logs.MonitorLog;
import com.dfire.monitor.domain.AlarmInfo;
import com.dfire.monitor.service.AlarmCenter;
import org.springframework.stereotype.Service;


/**
 * desc:
 *
 * @author scx
 * @create 2019/04/27
 */
@Service
public class AlarmCenterImpl implements AlarmCenter {

    @Override
    public void sendToWeChat(AlarmInfo alarmInfo) {
        //TODO 使用者自己开发
        MonitorLog.info("[微信告警]userInfo:{},消息结果:{}", alarmInfo.toString(), "请管理员自己设置企业微信告警配置");

    }

    @Override
    public void sendToPhone(AlarmInfo alarmInfo) {
        //TODO 使用者自己开发
        MonitorLog.info("[电话告警]userInfo:{},消息结果:{}", alarmInfo.toString(), "请管理员自己设置企业微信告警配置");

    }
}