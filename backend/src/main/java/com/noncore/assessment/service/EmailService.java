package com.noncore.assessment.service;

public interface EmailService {

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param text 内容
     */
    void sendEmail(String to, String subject, String text);

    /**
     * 发送简单的HTML格式邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     */
    void sendSimpleMessage(String to, String subject, String htmlContent);
} 