package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.AiQuotaAdjustment;
import com.noncore.assessment.mapper.AiQuotaAdjustmentMapper;
import com.noncore.assessment.service.AiQuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiQuotaServiceImpl implements AiQuotaService {

    private final AiQuotaAdjustmentMapper quotaMapper;

    private int clampNonNegative(Integer value) {
        if (value == null) return 0;
        return Math.max(0, Math.min(10_000, value));
    }

    @Override
    @Transactional(readOnly = true)
    public AiQuotaAdjustment getQuota(Long userId) {
        AiQuotaAdjustment q = quotaMapper.selectByUserId(userId);
        if (q != null) return q;
        return AiQuotaAdjustment.builder()
                .userId(userId)
                .aiChatBonusWeekly(0)
                .insightBonusWindow(0)
                .voiceChatBonusWeekly(0)
                .build();
    }

    @Override
    public AiQuotaAdjustment updateQuota(Long userId,
                                         Integer aiChatBonusWeekly,
                                         Integer insightBonusWindow,
                                         Integer voiceChatBonusWeekly) {
        AiQuotaAdjustment current = getQuota(userId);
        int nextAi = clampNonNegative(aiChatBonusWeekly != null ? aiChatBonusWeekly : current.getAiChatBonusWeekly());
        int nextInsight = clampNonNegative(insightBonusWindow != null ? insightBonusWindow : current.getInsightBonusWindow());
        int nextVoice = clampNonNegative(voiceChatBonusWeekly != null ? voiceChatBonusWeekly : current.getVoiceChatBonusWeekly());
        AiQuotaAdjustment in = AiQuotaAdjustment.builder()
                .userId(userId)
                .aiChatBonusWeekly(nextAi)
                .insightBonusWindow(nextInsight)
                .voiceChatBonusWeekly(nextVoice)
                .build();
        quotaMapper.upsertByUserId(in);
        return getQuota(userId);
    }

    @Override
    public int getAiChatBonusWeekly(Long userId) {
        AiQuotaAdjustment q = getQuota(userId);
        return clampNonNegative(q == null ? 0 : q.getAiChatBonusWeekly());
    }

    @Override
    public int getInsightBonusWindow(Long userId) {
        AiQuotaAdjustment q = getQuota(userId);
        return clampNonNegative(q == null ? 0 : q.getInsightBonusWindow());
    }

    @Override
    public int getVoiceChatBonusWeekly(Long userId) {
        AiQuotaAdjustment q = getQuota(userId);
        return clampNonNegative(q == null ? 0 : q.getVoiceChatBonusWeekly());
    }
}
