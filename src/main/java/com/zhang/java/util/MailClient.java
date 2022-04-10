package com.zhang.java.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @Date 2022/4/5 16:36
 * @Author zsy
 * @Description
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sendFrom;

    /**
     * 发邮件
     *
     * @param sendTo  收件人邮箱
     * @param subject 主题
     * @param content 内容
     */
    public void sendMail(String sendTo, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(sendFrom);
            helper.setTo(sendTo);
            helper.setSubject(subject);
            //设置为true表示支持html格式内容
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败：" + e.getMessage());
        }
    }
}
