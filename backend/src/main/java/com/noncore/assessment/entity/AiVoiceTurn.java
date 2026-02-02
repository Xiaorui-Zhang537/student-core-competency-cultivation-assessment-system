package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 口语训练回合（用户发声 -> AI 回复）。
 *
 * @author System
 * @since 2026-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVoiceTurn {
    private Long id;
    private Long sessionId;
    private Long userId;
    private String userTranscript;
    private String assistantText;
    private Long userAudioFileId;
    private Long assistantAudioFileId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

