package com.noncore.assessment.service;

import com.noncore.assessment.entity.AiGradingHistory;
import com.noncore.assessment.util.PageResult;

public interface AiGradingHistoryService {
    void save(AiGradingHistory rec);
    PageResult<AiGradingHistory> list(Long teacherId, String q, int page, int size);
    AiGradingHistory get(Long teacherId, Long id);
}


