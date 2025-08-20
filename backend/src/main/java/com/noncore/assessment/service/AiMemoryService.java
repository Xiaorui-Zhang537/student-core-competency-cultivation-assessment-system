package com.noncore.assessment.service;

import com.noncore.assessment.entity.AiMemory;

public interface AiMemoryService {
    AiMemory getMemory(Long userId);
    AiMemory updateMemory(Long userId, Boolean enabled, String content);
}


