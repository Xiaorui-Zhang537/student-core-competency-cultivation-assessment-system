package com.noncore.assessment.realtime.ai;

import com.noncore.assessment.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * AI Live WebSocket 握手拦截器。
 * <p>
 * 目标：在 WS 握手阶段校验 JWT，并把 userId/role 写入 WebSocket session attributes，
 * 供后续消息处理使用。该逻辑与 {@link com.noncore.assessment.controller.NotificationStreamController}
 * 对 SSE 的 token 获取方式保持一致（支持 query token 或 Authorization Bearer）。
 *
 * @author System
 * @since 2026-02-02
 */
@Component
public class AiLiveHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_ROLE = "role";
    public static final String ATTR_TOKEN = "token";

    private final JwtUtil jwtUtil;

    public AiLiveHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 握手前：解析 token，校验 access token，并把 userId/role 放入 attributes。
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletReq)) {
            return false;
        }
        HttpServletRequest http = servletReq.getServletRequest();

        String token = extractToken(http);
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
                return false;
            }
            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            if (userId == null || !StringUtils.hasText(role)) {
                return false;
            }
            attributes.put(ATTR_TOKEN, token);
            attributes.put(ATTR_USER_ID, userId);
            attributes.put(ATTR_ROLE, role);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * 握手后：无需额外处理。
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    /**
     * 提取 token：优先 query token，其次 Authorization Bearer，其次 X-Auth-Token。
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        String custom = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(custom)) {
            return custom;
        }
        return null;
    }
}

