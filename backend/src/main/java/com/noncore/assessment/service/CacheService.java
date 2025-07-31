package com.noncore.assessment.service;

import java.util.Optional;

public interface CacheService {
    <T> Optional<T> get(String key, Class<T> clazz);
    void set(String key, Object value, long timeoutInSeconds);
    void delete(String key);
} 