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

    /**
     * 使用模板发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param templateName 模板文件名（位于 resources/templates/email 下）
     * @param variables 模板变量
     * @param language 语言（如 zh-CN / en-US），用于选择模板或本地化
     */
    void sendTemplate(String to, String subject, String templateName, java.util.Map<String, Object> variables, String language);
} 