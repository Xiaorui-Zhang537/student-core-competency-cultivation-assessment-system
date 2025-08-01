package com.noncore.assessment.service.impl;

import com.noncore.assessment.service.CacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author NonCore
 * @version 1.0
 * @date 2025/07/31
 */
@Service("redisCacheService")
public class RedisCacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (clazz.isInstance(value)) {
            return Optional.of(clazz.cast(value));
        }
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
} 