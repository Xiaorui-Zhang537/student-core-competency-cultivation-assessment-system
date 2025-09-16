package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.AiGradingHistory;
import com.noncore.assessment.mapper.AiGradingHistoryMapper;
import com.noncore.assessment.service.AiGradingHistoryService;
import com.noncore.assessment.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGradingHistoryServiceImpl implements AiGradingHistoryService {

    private final AiGradingHistoryMapper mapper;

    @Override
    public void save(AiGradingHistory rec) {
        mapper.insert(rec);
    }

    @Override
    public PageResult<AiGradingHistory> list(Long teacherId, String q, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 100);
        int offset = (p - 1) * s;
        List<AiGradingHistory> items = mapper.listByTeacher(teacherId, offset, s, q);
        long total = mapper.countByTeacher(teacherId, q);
        int totalPages = (int) Math.ceil(total * 1.0 / s);
        return new PageResult<>(items, p, s, total, totalPages);
    }

    @Override
    public AiGradingHistory get(Long teacherId, Long id) {
        return mapper.getById(id, teacherId);
    }
}


