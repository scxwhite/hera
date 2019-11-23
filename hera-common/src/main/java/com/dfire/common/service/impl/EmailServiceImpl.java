package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.service.EmailService;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/7/31
 */
@Service("emailServiceImpl")
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String title, String content, String address) throws MessagingException {
        if (StringUtils.isEmpty(address)) {
            return;
        }
        List<String> userEmails = Arrays.stream(address.split(Constants.SEMICOLON)).distinct().collect(Collectors.toList());
        int len = userEmails.size();
        InternetAddress[] addresses = new InternetAddress[len];
        for (int i = 0; i < len; i++) {
            addresses[i] = new InternetAddress(userEmails.get(i));
        }
        Properties properties = new Properties();
        properties.put("mail.smtp.host", HeraGlobalEnv.getMailHost());
        properties.put("mail.transport.protocol", HeraGlobalEnv.getMailProtocol());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.port", HeraGlobalEnv.getMailPort());
        properties.put("mail.smtp.socketFactory.port", HeraGlobalEnv.getMailPort());

        Session session = Session.getInstance(properties);

        session.setDebug(false);

        Transport transport = session.getTransport();

        transport.connect(HeraGlobalEnv.getMailHost(), HeraGlobalEnv.getMailUser(), HeraGlobalEnv.getMailPassword());

        Message message = createSimpleMessage(session, title, content, addresses);
        transport.sendMessage(message, message.getAllRecipients());
        MonitorLog.info("发送邮件成功,Title:{}, 联系人:{}", title, address);
        transport.close();
    }

    private Message createSimpleMessage(Session session, String title, String content, InternetAddress[] addresses) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(HeraGlobalEnv.getMailUser()));
        mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
        mimeMessage.setSubject(title);
        mimeMessage.setContent(content, "text/html;charset=UTF-8");
        return mimeMessage;
    }
}
