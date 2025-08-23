package com.noncore.assessment.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notification SSE broadcaster
 * Manage per-user SseEmitters and provide simple broadcast APIs.
 */
@Component
public class NotificationSseService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSseService.class);

    // userId -> list of emitters (support multi-tab)
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * Register an emitter for a user and send initial connect event
     */
    public SseEmitter register(Long userId) {
        // default timeout: 30 minutes
        SseEmitter emitter = new SseEmitter(30L * 60L * 1000L);

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            logger.warn("Failed to send initial connected event, removing emitter: {}", e.getMessage());
            removeEmitter(userId, emitter);
        }
        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    /**
     * Send an event to a specific user
     */
    public void sendToUser(Long userId, String eventName, Object payload) {
        List<SseEmitter> list = userEmitters.get(userId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * Send an event to multiple users
     */
    public void sendToUsers(Iterable<Long> userIds, String eventName, Object payload) {
        for (Long userId : userIds) {
            sendToUser(userId, eventName, payload);
        }
    }
}


