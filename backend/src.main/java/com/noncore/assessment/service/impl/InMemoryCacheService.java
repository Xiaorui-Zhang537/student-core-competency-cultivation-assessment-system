package com.noncore.assessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryCacheService implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

    private static class CacheEntry {
        final String value;
        final LocalDateTime expiryTime;

        CacheEntry(String value, long timeoutInSeconds) {
            this.value = value;
            this.expiryTime = LocalDateTime.now().plusSeconds(timeoutInSeconds);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public InMemoryCacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        CacheEntry entry = cache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                cache.remove(key);
                return Optional.empty();
            }
            try {
                T value = objectMapper.readValue(entry.value, clazz);
                return Optional.of(value);
            } catch (IOException e) {
                logger.error("Failed to deserialize cache value for key: {}", key, e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, long timeoutInSeconds) {
        try {
            String stringValue = objectMapper.writeValueAsString(value);
            cache.put(key, new CacheEntry(stringValue, timeoutInSeconds));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize cache value for key: {}", key, e);
        }
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }
} 