package com.dfire.common.service;

/**
 * @author xiaosuda
 * @date 2018/7/31
 */
public interface EmailService {

    /**
     * 发送邮件
     *
     * @param title   邮件标题
     * @param content 邮件内容
     * @param address 收件人，多个用,隔开
     * @return 结果
     */
    boolean sendEmail(String title, String content, String address);
}
