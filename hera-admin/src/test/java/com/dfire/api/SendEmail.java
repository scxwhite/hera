package com.dfire.api;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by xiaosuda on 2018/7/31.
 */
public class SendEmail {

    public static void main(String[] args) throws MessagingException {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.aliyun.com");
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.port", 465);
        properties.put("mail.smtp.socketFactory.port", 465);


        Session session = Session.getInstance(properties);

        session.setDebug(true);

        Transport transport = session.getTransport("smtp");

        transport.connect("smtp.aliyun.com", "scx_white@aliyun.com", "aaascx521");

        Message message = createSimpleMessage(session);

        transport.sendMessage(message, message.getAllRecipients());
        transport.close();


    }

    private static Message createSimpleMessage(Session session) throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress("scx_white@aliyun.com"));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("xiaosuda@2dfire.com"));
        mimeMessage.setSubject("只包含文本的简单邮件");
        mimeMessage.setContent("你好啊！", "text/html;charset=UTF-8");
        return mimeMessage;
    }
}
