package com.dfire.common.service.impl;

import com.dfire.common.constants.Constants;
import com.dfire.common.service.EmailService;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author xiaosuda
 * @date 2018/7/31
 */
@Service("emailServiceImpl")
public class EmailServiceImpl implements EmailService {

    @Value("${mail.port}")
    private String mailPort;
    @Value("${mail.protocol}")
    private String mailProtocol;
    @Value("${mail.host}")
    private String mailHost;
    @Value("${mail.user}")
    private String mailUser;
    @Value("${mail.password}")
    private String mailPassword;


    @Override
    public void sendEmail(String title, String content, String address) throws MessagingException {
        if (StringUtils.isEmpty(address)) {
            return;
        }
        String[] userEmails = address.split(Constants.SEMICOLON);
        int len = userEmails.length;
        InternetAddress[] addresses = new InternetAddress[len];
        for (int i = 0; i < len; i++) {
            addresses[i] = new InternetAddress(userEmails[i]);
        }
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", mailHost);
        properties.setProperty("mail.transport.protocol", mailProtocol);
        properties.setProperty("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.port", mailPort);
        properties.put("mail.smtp.socketFactory.port", mailPort);

        Session session = Session.getInstance(properties);

        session.setDebug(false);

        Transport transport = session.getTransport(mailProtocol);

        transport.connect(mailHost, mailUser, mailPassword);

        Message message = createSimpleMessage(session, title, content, addresses);
        transport.sendMessage(message, message.getAllRecipients());
        MonitorLog.info("发送邮件成功,内容:{}, 联系人:{}", content, address);
        transport.close();
    }

    private Message createSimpleMessage(Session session, String title, String content, InternetAddress[] addresses) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(mailUser));
        mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
        mimeMessage.setSubject(title);
        mimeMessage.setContent(content, "text/html;charset=UTF-8");
        return mimeMessage;
    }
}
