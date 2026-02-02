package com.noncore.assessment.realtime.ai;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * AI 实时语音（Live）WebSocket 配置。
 * <p>
 * 注册 WS 路径：/ai/live/ws
 *
 * @author System
 * @since 2026-02-02
 */
@Configuration
@EnableWebSocket
public class AiLiveWebSocketConfig implements WebSocketConfigurer {

    private final AiLiveWebSocketHandler handler;
    private final AiLiveHandshakeInterceptor handshakeInterceptor;

    public AiLiveWebSocketConfig(AiLiveWebSocketHandler handler,
                                 AiLiveHandshakeInterceptor handshakeInterceptor) {
        this.handler = handler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    /**
     * 注册 WebSocket handler 与握手拦截器，并设置允许的 origin patterns。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ai/live/ws")
                .addInterceptors(handshakeInterceptor)
                // 与现有 CORS 配置一致：开发与生产域名均需可用；WS 这里用 patterns 覆盖更稳妥
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://0.0.0.0:5173",
                        "http://*.local:5173",
                        "http://*.lan:5173",
                        "https://www.stucoreai.space",
                        "http://www.stucoreai.space",
                        "https://stucoreai.space",
                        "http://stucoreai.space"
                );
    }
}

