package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.AiMemory;
import com.noncore.assessment.mapper.AiMemoryMapper;
import com.noncore.assessment.service.AiMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiMemoryServiceImpl implements AiMemoryService {

    private final AiMemoryMapper memoryMapper;

    @Override
    @Transactional(readOnly = true)
    public AiMemory getMemory(Long userId) {
        return memoryMapper.selectByUserId(userId);
    }

    @Override
    public AiMemory updateMemory(Long userId, Boolean enabled, String content) {
        AiMemory m = new AiMemory();
        m.setUserId(userId);
        m.setEnabled(enabled == null ? Boolean.TRUE : enabled);
        m.setContent(content);
        memoryMapper.upsertByUserId(m);
        return memoryMapper.selectByUserId(userId);
    }
}


