package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "聊天消息附件")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageAttachment {
    private Long id;
    private Long notificationId; // 对应 notifications.id（type='message')
    private Long fileId;         // 对应 file_records.id
    private Long uploaderId;     // 上传者
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}


