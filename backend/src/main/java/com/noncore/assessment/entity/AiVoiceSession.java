package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 口语训练会话（独立于 AI 聊天会话）。
 *
 * @author System
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVoiceSession {
    private Long id;
    private Long userId;
    private String title;
    private String model;
    private String mode;   // text | audio | both
    private String locale; // en-US / zh-CN
    private String scenario;
    private Boolean pinned;
    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

