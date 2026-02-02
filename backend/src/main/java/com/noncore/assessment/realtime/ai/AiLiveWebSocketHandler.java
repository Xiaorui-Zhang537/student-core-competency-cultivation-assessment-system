package com.noncore.assessment.realtime.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.config.AiConfigProperties;
import com.noncore.assessment.realtime.ai.live.GeminiLiveSession;
import com.noncore.assessment.service.llm.PromptLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Live WebSocket handler（第一阶段：建立实时通道骨架）。
 * <p>
 * 该 handler 负责：\n
 * - 握手后立即向客户端发送 ready\n
 * - 解析/校验基础消息结构，并桥接到 Gemini Live WebSocket\n
 *
 * @author System
 * @since 2026-02-02
 */
@Component
public class AiLiveWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AiConfigProperties aiConfigProperties;
    private final PromptLoader promptLoader;

    /**
     * 每个前端 WS session 对应一个 Gemini Live 会话。
     */
    private final ConcurrentHashMap<String, GeminiLiveSession> liveSessions = new ConcurrentHashMap<>();

    public AiLiveWebSocketHandler(AiConfigProperties aiConfigProperties, PromptLoader promptLoader) {
        this.aiConfigProperties = aiConfigProperties;
        this.promptLoader = promptLoader;
    }

    /**
     * 连接建立：向客户端发送 ready（包含 userId）。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_USER_ID);
        String role = (String) session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_ROLE);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "ready");
        msg.put("userId", userId);
        msg.put("role", role);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
    }

    /**
     * 处理文本消息：当前仅校验 JSON 并对未知消息返回占位错误。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<?, ?> payload;
        try {
            payload = objectMapper.readValue(message.getPayload(), Map.class);
        } catch (Exception ex) {
            sendError(session, "BAD_JSON", "消息不是有效 JSON");
            return;
        }
        Object type = payload.get("type");
        if (type == null) {
            sendError(session, "MISSING_TYPE", "缺少 type 字段");
            return;
        }

        String t = String.valueOf(type);
        if ("ping".equalsIgnoreCase(t)) {
            sendOk(session, "pong", Map.of("ts", payload.get("ts")));
            return;
        }
        if ("start".equalsIgnoreCase(t)) {
            handleStart(session, payload);
            return;
        }
        if ("audio_chunk".equalsIgnoreCase(t)) {
            handleAudioChunk(session, payload);
            return;
        }
        if ("activity_start".equalsIgnoreCase(t)) {
            handleActivityStart(session);
            return;
        }
        if ("activity_end".equalsIgnoreCase(t)) {
            handleActivityEnd(session);
            return;
        }
        if ("stop".equalsIgnoreCase(t)) {
            handleStop(session);
            return;
        }

        sendError(session, "UNKNOWN_TYPE", "不支持的 type: " + t);
    }

    /**
     * 连接关闭：留空（后续在此处释放 Gemini WS 等资源）。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 释放对应的 Gemini Live 会话
        GeminiLiveSession live = liveSessions.remove(session.getId());
        if (live != null) {
            live.close();
        }
        super.afterConnectionClosed(session, status);
    }

    /**
     * 启动一段 Live 会话：建立到 Gemini Live 的 WS，并发送 setup。
     */
    private void handleStart(WebSocketSession session, Map<?, ?> payload) {
        Object modelObj = payload.get("model");
        Object modeObj = payload.get("mode");
        Object localeObj = payload.get("locale");
        Object scenarioObj = payload.get("scenario");

        String mode = modeObj == null ? "both" : String.valueOf(modeObj);
        String locale = localeObj == null ? "" : String.valueOf(localeObj);
        String scenario = scenarioObj == null ? "" : String.valueOf(scenarioObj);

        // Live API 可用模型与普通对话模型不同。若前端未指定模型，则根据输出模式选择 Live 推荐默认值：
        // - 文本：gemini-live-2.5-flash-preview
        // - 音频（含 both）：gemini-2.5-flash-native-audio-preview-12-2025
        //
        // 参考（官方文档示例）：https://ai.google.dev/gemini-api/docs/live-guide
        String model = (modelObj == null || String.valueOf(modelObj).isBlank())
                ? (("audio".equalsIgnoreCase(mode) || "both".equalsIgnoreCase(mode))
                    ? "gemini-2.5-flash-native-audio-preview-12-2025"
                    : "gemini-live-2.5-flash-preview")
                : String.valueOf(modelObj);

        // 仅允许 google 模型（Live 走 Gemini）
        if (!model.startsWith("google/") && !model.startsWith("gemini-") && !model.startsWith("models/")) {
            sendError(session, "INVALID_MODEL", "Live 仅支持 Gemini 模型（google/* 或 gemini-*）");
            return;
        }

        // 若已有旧会话，先关闭
        GeminiLiveSession old = liveSessions.remove(session.getId());
        if (old != null) {
            old.close();
        }

        GeminiLiveSession live = new GeminiLiveSession(aiConfigProperties, promptLoader, session);
        liveSessions.put(session.getId(), live);

        live.connect(model, mode, locale, scenario)
                .whenComplete((ok, ex) -> {
                    if (ex != null) {
                        sendError(session, "CONNECT_FAILED", ex.getMessage() != null ? ex.getMessage() : "连接失败");
                        liveSessions.remove(session.getId());
                        try { live.close(); } catch (Exception ignore) {}
                    } else {
                        sendOk(session, "started", Map.of("mode", mode, "model", model));
                    }
                });
    }

    /**
     * 转发音频分片到 Gemini Live。
     */
    private void handleAudioChunk(WebSocketSession session, Map<?, ?> payload) {
        GeminiLiveSession live = liveSessions.get(session.getId());
        if (live == null) {
            sendError(session, "NOT_STARTED", "请先发送 start");
            return;
        }
        Object b64 = payload.get("pcm16Base64");
        Object rate = payload.get("sampleRate");
        if (b64 == null || !StringUtils.hasText(String.valueOf(b64))) {
            sendError(session, "MISSING_AUDIO", "缺少 pcm16Base64");
            return;
        }
        Integer sr = null;
        try {
            if (rate instanceof Number n) sr = n.intValue();
            else if (rate != null) sr = Integer.parseInt(String.valueOf(rate));
        } catch (Exception ignore) {}

        live.sendAudioChunk(String.valueOf(b64), sr);
    }

    /**
     * 手动标记活动开始（用于按回合/按住说话）。
     * 注意：仅当 setup 中禁用了 automaticActivityDetection 时可用。
     */
    private void handleActivityStart(WebSocketSession session) {
        GeminiLiveSession live = liveSessions.get(session.getId());
        if (live == null) {
            sendError(session, "NOT_STARTED", "请先发送 start");
            return;
        }
        live.sendActivityStart();
        sendOk(session, "activity_started", Map.of());
    }

    /**
     * 手动标记活动结束（用于触发模型开始生成回复）。
     * 注意：仅当 setup 中禁用了 automaticActivityDetection 时可用。
     */
    private void handleActivityEnd(WebSocketSession session) {
        GeminiLiveSession live = liveSessions.get(session.getId());
        if (live == null) {
            sendError(session, "NOT_STARTED", "请先发送 start");
            return;
        }
        live.sendActivityEnd();
        sendOk(session, "activity_ended", Map.of());
    }

    /**
     * 停止会话（关闭 Gemini Live WS）。
     */
    private void handleStop(WebSocketSession session) {
        GeminiLiveSession live = liveSessions.remove(session.getId());
        if (live != null) {
            live.close();
        }
        sendOk(session, "stopped", Map.of());
    }

    private void sendError(WebSocketSession session, String code, String message) {
        try {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("code", code);
            err.put("message", message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(err)));
        } catch (Exception ignore) {
            // ignore
        }
    }

    private void sendOk(WebSocketSession session, String type, Map<String, Object> data) {
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", type);
            if (data != null && !data.isEmpty()) {
                msg.putAll(data);
            }
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        } catch (Exception ignore) {
            // ignore
        }
    }
}

