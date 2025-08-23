package com.noncore.assessment.controller;

import com.noncore.assessment.realtime.NotificationSseService;
import com.noncore.assessment.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@Tag(name = "通知实时流", description = "SSE 实时通知推送")
public class NotificationStreamController {

    private final NotificationSseService sseService;
    private final JwtUtil jwtUtil;

    public NotificationStreamController(NotificationSseService sseService, JwtUtil jwtUtil) {
        this.sseService = sseService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅通知流", description = "通过SSE订阅当前用户的通知事件。支持 token 查询参数或 Authorization 头。")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SseEmitter> stream(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (!StringUtils.hasText(token)) {
            String bearer = request.getHeader("Authorization");
            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                token = bearer.substring(7);
            }
        }

        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        SseEmitter emitter = sseService.register(userId);
        return ResponseEntity.ok(emitter);
    }
}


