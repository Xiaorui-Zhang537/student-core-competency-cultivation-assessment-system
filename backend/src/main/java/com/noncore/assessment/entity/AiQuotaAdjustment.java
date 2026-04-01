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
public class AiQuotaAdjustment {
    private Long id;
    private Long userId;
    private Integer aiChatBonusWeekly;
    private Integer insightBonusWindow;
    private Integer voiceChatBonusWeekly;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

