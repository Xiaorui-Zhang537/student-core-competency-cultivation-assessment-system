package com.noncore.assessment.service.impl;

import com.noncore.assessment.service.CacheService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的简单缓存实现（完全无 Redis 依赖）。
 * 仅在当前进程内有效，重启即失效。
 */
@Service("inMemoryCacheService")
@Primary
public class InMemoryCacheServiceImpl implements CacheService {

    private static final class ValueHolder {
        final Object value;
        final long expireAtMillis; // <=0 表示不过期
        ValueHolder(Object value, long expireAtMillis) {
            this.value = value;
            this.expireAtMillis = expireAtMillis;
        }
        boolean expired() {
            return expireAtMillis > 0 && System.currentTimeMillis() > expireAtMillis;
        }
    }

    private final Map<String, ValueHolder> store = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        ValueHolder holder = store.get(key);
        if (holder == null) return Optional.empty();
        if (holder.expired()) { store.remove(key); return Optional.empty(); }
        if (clazz.isInstance(holder.value)) {
            return Optional.of(clazz.cast(holder.value));
        }
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, long timeoutInSeconds) {
        long expireAt = timeoutInSeconds > 0 ? System.currentTimeMillis() + timeoutInSeconds * 1000L : 0L;
        store.put(key, new ValueHolder(value, expireAt));
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }
}