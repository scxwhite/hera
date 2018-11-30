package com.dfire.common.service;

import javax.mail.MessagingException;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/7/31
 */
public interface EmailService {

    void sendEmail(String title, String content, List<String> address) throws MessagingException;
}
