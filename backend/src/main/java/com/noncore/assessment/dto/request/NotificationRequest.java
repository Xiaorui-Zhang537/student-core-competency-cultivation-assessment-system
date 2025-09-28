package com.noncore.assessment.dto.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private Long recipientId;
    private String title;
    private String content;
    private String type;
    private String category;
    private String priority;
    private String relatedType;
    private Long relatedId;
    // 可选：聊天消息附件文件ID列表（对应 file_records.id）
    private java.util.List<Long> attachmentFileIds;
} 