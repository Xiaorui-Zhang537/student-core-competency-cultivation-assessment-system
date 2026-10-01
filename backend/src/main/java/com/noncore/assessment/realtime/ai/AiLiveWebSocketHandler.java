package com.noncore.assessment.realtime.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.config.AiConfigProperties;
import com.noncore.assessment.realtime.ai.live.LiveVoiceSession;
import com.noncore.assessment.realtime.ai.live.GeminiLiveSession;
import com.noncore.assessment.realtime.ai.live.VolcRealtimeVoiceSession;
import com.noncore.assessment.service.AiMemoryService;
import com.noncore.assessment.service.AiModelRegistryService;
import com.noncore.assessment.service.AiQuotaService;
import com.noncore.assessment.service.AiVoicePracticeService;
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
    private final AiModelRegistryService modelRegistry;
    private final AiMemoryService memoryService;
    private final AiVoicePracticeService voicePracticeService;
    private final AiQuotaService quotaService;

    /**
     * 每个前端 WS session 对应一个实时语音会话。
     */
    private final ConcurrentHashMap<String, LiveVoiceSession> liveSessions = new ConcurrentHashMap<>();

    public AiLiveWebSocketHandler(AiConfigProperties aiConfigProperties,
                                  PromptLoader promptLoader,
                                  AiModelRegistryService modelRegistry,
                                  AiMemoryService memoryService,
                                  AiVoicePracticeService voicePracticeService,
                                  AiQuotaService quotaService) {
        this.aiConfigProperties = aiConfigProperties;
        this.promptLoader = promptLoader;
        this.modelRegistry = modelRegistry;
        this.memoryService = memoryService;
        this.voicePracticeService = voicePracticeService;
        this.quotaService = quotaService;
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
        LiveVoiceSession live = liveSessions.remove(session.getId());
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

        String model = modelRegistry.normalizeVoiceModel(modelObj == null ? null : String.valueOf(modelObj), mode);
        if (!isVoiceModelVisibleForSession(session, model)) {
            sendError(session, "MODEL_NOT_VISIBLE", "当前端侧未开放该语音模型");
            return;
        }
        String quotaError = validateVoiceQuota(session);
        if (quotaError != null) {
            sendError(session, "VOICE_QUOTA_EXCEEDED", quotaError);
            return;
        }
        scenario = appendMemoryContext(session, scenario);

        // 若已有旧会话，先关闭
        LiveVoiceSession old = liveSessions.remove(session.getId());
        if (old != null) {
            old.close();
        }

        LiveVoiceSession live = modelRegistry.isVolcModel(model)
                ? new VolcRealtimeVoiceSession(aiConfigProperties, promptLoader, session)
                : new GeminiLiveSession(aiConfigProperties, promptLoader, session);
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
        LiveVoiceSession live = liveSessions.get(session.getId());
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
        LiveVoiceSession live = liveSessions.get(session.getId());
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
        LiveVoiceSession live = liveSessions.get(session.getId());
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
        LiveVoiceSession live = liveSessions.remove(session.getId());
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

    private String appendMemoryContext(WebSocketSession session, String scenario) {
        Long userId = null;
        try {
            userId = (Long) session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_USER_ID);
        } catch (Exception ignored) {
        }
        String base = scenario == null ? "" : scenario.trim();
        if (userId == null) return base;
        try {
            var mem = memoryService.getMemory(userId);
            if (mem == null || !Boolean.TRUE.equals(mem.getEnabled())) return base;
            String content = mem.getContent() == null ? "" : mem.getContent().trim();
            if (content.isBlank()) return base;
            if (content.length() > 3000) content = content.substring(0, 3000);
            String memInstruction = "用户长期记忆（偏好/背景/约束）：\n"
                    + content
                    + "\n你应在不与当前口语任务冲突时参考这些记忆；若冲突，以当前任务为准；不要逐字复述记忆。";
            return base.isBlank() ? memInstruction : (base + "\n\n" + memInstruction);
        } catch (Exception ignored) {
            return base;
        }
    }

    private boolean isVoiceModelVisibleForSession(WebSocketSession session, String model) {
        try {
            String role = String.valueOf(session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_ROLE));
            if ("ADMIN".equalsIgnoreCase(role)) return true;
            String audience = "TEACHER".equalsIgnoreCase(role)
                    ? AiModelRegistryService.AUDIENCE_TEACHER
                    : AiModelRegistryService.AUDIENCE_STUDENT;
            return modelRegistry.isVisibleFor(model, AiModelRegistryService.SURFACE_VOICE, audience);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String validateVoiceQuota(WebSocketSession session) {
        try {
            String role = String.valueOf(session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_ROLE));
            if (!"STUDENT".equalsIgnoreCase(role)) return null;
            Long userId = (Long) session.getAttributes().get(AiLiveHandshakeInterceptor.ATTR_USER_ID);
            if (userId == null) return "未登录或登录状态已失效";
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            long used = voicePracticeService.countTurnsByUserSince(userId, monday.atStartOfDay());
            int bonus = 0;
            try { bonus = Math.max(0, quotaService.getVoiceChatBonusWeekly(userId)); } catch (Exception ignored) {}
            int limit = AiQuotaService.BASE_VOICE_WEEKLY_LIMIT + bonus;
            return used >= limit ? "本周口语训练调用次数已达上限（" + limit + "次），请下周再试" : null;
        } catch (Exception ignored) {
            return "无法校验口语训练配额";
        }
    }
}
