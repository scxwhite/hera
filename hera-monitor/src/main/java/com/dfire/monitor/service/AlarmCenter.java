package com.dfire.monitor.service;

import com.dfire.monitor.domain.AlarmInfo;

/**
 * @author scx
 */
public interface AlarmCenter {

    /**
     * 发送监控信息到企业微信
     *
     * @param alarmInfo
     * @return JSONObject
     */
    void sendToWeChat(AlarmInfo alarmInfo);

    /**
     * 发送监控信息到手机
     *
     * @param alarmInfo alarmInfo
     * @return JSONObject
     */
    void sendToPhone(AlarmInfo alarmInfo);

}
