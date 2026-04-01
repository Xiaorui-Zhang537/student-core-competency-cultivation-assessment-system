package com.noncore.assessment.service;

import com.noncore.assessment.entity.AiQuotaAdjustment;

public interface AiQuotaService {
    int BASE_GEMINI_WEEKLY_LIMIT = 10;
    int BASE_GLM_WEEKLY_LIMIT = 20;
    int BASE_INSIGHT_WINDOW_LIMIT = 2;
    int BASE_VOICE_WEEKLY_LIMIT = 15;
    int INSIGHT_WINDOW_DAYS = 7;

    AiQuotaAdjustment getQuota(Long userId);
    AiQuotaAdjustment updateQuota(Long userId,
                                  Integer aiChatBonusWeekly,
                                  Integer insightBonusWindow,
                                  Integer voiceChatBonusWeekly);
    int getAiChatBonusWeekly(Long userId);
    int getInsightBonusWindow(Long userId);
    int getVoiceChatBonusWeekly(Long userId);
}

