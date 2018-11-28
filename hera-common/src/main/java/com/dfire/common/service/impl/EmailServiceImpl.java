package com.dfire.common.service.impl;

import com.dfire.common.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
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
    public void sendEmail(String title, String content, List<String> address) throws MessagingException {
        int len = address.size();
        InternetAddress[] addresses = new InternetAddress[len];
        for (int i = 0; i < len; i++) {
            addresses[i] = new InternetAddress(address.get(i));
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
