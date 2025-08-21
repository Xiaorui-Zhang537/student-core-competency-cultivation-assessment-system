package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    private Long id;
    private Long conversationId;
    private Long userId;
    private String role; // user | assistant | system
    private String content;
    private String attachments; // JSON 字符串（文件ID数组）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}


