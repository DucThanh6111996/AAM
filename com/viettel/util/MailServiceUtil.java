package com.viettel.util;

import com.viettel.mailservice.MailService;
import com.viettel.passprotector.PassProtector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.ResourceBundle;

public class MailServiceUtil implements MailService {

    protected static final Logger logger = LoggerFactory.getLogger(MailServiceUtil.class);
    private Session session;
    private ResourceBundle mailProps;

    public MailServiceUtil() {
        try {
            mailProps = ResourceBundle.getBundle("config");
            final String username = mailProps.getString("smtp_user");
            final String password = PassProtector.decrypt(mailProps.getString("smtp_password"), "ipchange");

            Properties props = new Properties();
            props.put("mail.smtp.host", mailProps.getString("smtp_server"));
            props.put("mail.smtp.socketFactory.port", mailProps.getString("smtp_port"));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", mailProps.getString("smtp_port"));

            session = Session.getInstance(props,
                    new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }

    }

    public void sendMail(String toList[], String ccList[], String bccList[], String subject, String content, byte[] attatchmentDatas[], String attatchmentNames[]) throws Exception {

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailProps.getString("smtp_user")));

            if (toList != null) {
                for (String s : toList) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(s.trim()));
                }
            }
            if (ccList != null) {
                for (String s : ccList) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(s.trim()));
                }
            }
            if (bccList != null) {
                for (String s : bccList) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(s.trim()));
                }
            }
            message.setSubject(subject, "utf-8");

            //Create the message part 
            MimeBodyPart messageBodyPart = new MimeBodyPart();

            //Fill the message
            messageBodyPart.setContent(content, "text/html;charset=utf-8");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            //Set text message part
            multipart.addBodyPart(messageBodyPart);

            //Part two is attachment
            if (attatchmentDatas != null && attatchmentNames != null && attatchmentDatas.length == attatchmentNames.length) {
                for (int i = 0; i < attatchmentDatas.length; i++) {
                    byte[] attatchmentData = attatchmentDatas[i];
                    messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setDataHandler(new DataHandler(attatchmentData, "application/octet-stream"));
                    messageBodyPart.setFileName(attatchmentNames[i]);
                    multipart.addBodyPart(messageBodyPart);
                }
            }

            //Send the complete message parts
            message.setContent(multipart);

            Transport.send(message);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw t;
        }
    }

}
