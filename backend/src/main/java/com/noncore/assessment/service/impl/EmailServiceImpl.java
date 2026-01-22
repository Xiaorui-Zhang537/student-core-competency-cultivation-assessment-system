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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from-address:}")
    private String fromEmail;

    @Value("${spring.mail.from-name:Student Core Competence System}")
    private String fromName;

    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender emailSender,
                            @Autowired(required = false) SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
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
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            if (fromEmail != null && !fromEmail.isEmpty()) {
                try {
                    helper.setFrom(new jakarta.mail.internet.InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
                } catch (Exception ignored) {
                    helper.setFrom(fromEmail);
                }
            }
            helper.setText(text, true);
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

    @Override
    @Async
    public void sendTemplate(String to, String subject, String templateName, java.util.Map<String, Object> variables, String language) {
        if (templateEngine == null) {
            // 回退到直接发送，无模板
            String fallback = variables != null && variables.containsKey("actionUrl")
                    ? String.valueOf(variables.get("actionUrl"))
                    : "";
            sendEmail(to, subject, fallback);
            return;
        }
        Context context = new Context();
        if (variables != null) {
            for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        String html = templateEngine.process("email/" + templateName, context);
        sendEmail(to, subject, html);
    }
} 