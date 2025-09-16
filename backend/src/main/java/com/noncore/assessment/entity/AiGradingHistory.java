package com.noncore.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 批改历史记录
 */
@Data
public class AiGradingHistory {
    private Long id;
    private Long teacherId;
    private Long fileId;
    private String fileName;
    private String model;
    private Double finalScore;
    private String rawJson;
    private LocalDateTime createdAt;
}


