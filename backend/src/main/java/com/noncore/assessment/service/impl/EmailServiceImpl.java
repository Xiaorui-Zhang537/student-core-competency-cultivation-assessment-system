package com.noncore.assessment.service.impl;

import com.noncore.assessment.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender emailSender;

    @Value("${spring.mail.from-address}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String text) {
        if (emailSender == null) {
            logger.warn("Mail sender is not configured. Email to {} with subject '{}' was not sent.", to, subject);
            return;
        }
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true for HTML
            emailSender.send(message);
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "发送邮件失败", e);
        }
    }

    @Override
    @Async
    public void sendSimpleMessage(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent);
    }
} 