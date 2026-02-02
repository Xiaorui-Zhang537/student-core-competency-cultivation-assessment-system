package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 口语训练回合入库响应。
 *
 * @author System
 * @since 2026-02-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiVoiceTurnResponse {

    /**
     * 语音会话ID（兼容：旧字段名 conversationId）。
     */
    private Long sessionId;
    private Long conversationId;

    /**
     * 语音回合ID。
     */
    private Long turnId;
}

