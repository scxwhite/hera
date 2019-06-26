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
    boolean sendToWeChat(AlarmInfo alarmInfo);

    /**
     * 发送监控信息到手机
     *
     * @param alarmInfo alarmInfo
     * @return JSONObject
     */
    boolean sendToPhone(AlarmInfo alarmInfo);


    /**
     * 发送信息到邮件
     *
     * @param title   邮件标题
     * @param content 邮件内容（html格式）
     * @param address 邮件地址，多个使用;分割
     */
    boolean sendToEmail(String title, String content, String address);

}
