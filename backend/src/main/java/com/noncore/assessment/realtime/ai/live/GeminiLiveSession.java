package com.noncore.assessment.realtime.ai.live;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.noncore.assessment.config.AiConfigProperties;
import com.noncore.assessment.service.llm.PromptLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gemini Live API（BidiGenerateContent / WebSocket）桥接会话。
 * <p>
 * - 浏览器 <-> 本服务：Spring WebSocket（TextMessage）\n
 * - 本服务 <-> Gemini：OkHttp WebSocket（JSON message，代理更稳定）
 *
 * 该类负责：建立到 Gemini Live 的连接、发送 setup、转发音频 chunks、
 * 解析 serverContent（转写/文本/音频）并推送给前端。
 *
 * @author System
 * @since 2026-02-02
 */
public class GeminiLiveSession {

    private static final Logger log = LoggerFactory.getLogger(GeminiLiveSession.class);

    /**
     * Live API WebSocket endpoint（Gemini API）。
     * 参考：Live API - WebSockets API reference
     */
    private static final String GEMINI_LIVE_WS_ENDPOINT =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent";

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = mapper.getFactory();

    private final AiConfigProperties aiConfig;
    private final PromptLoader promptLoader;
    private final WebSocketSession clientSession;

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private volatile WebSocket geminiWs;
    private volatile CompletableFuture<Void> setupCompleteFuture = null;

    // 发送节流：避免无限 in-flight 堆积
    private final AtomicInteger inFlightSends = new AtomicInteger(0);
    private final int maxInFlight = 64;

    // 文本增量：用于把 modelTurn text 变为 delta
    private volatile String lastAssistantText = "";

    // 转写累计：避免“分段转写只推最后一段”导致前端显示不完整
    private volatile String lastUserTranscript = "";
    private volatile String lastAssistantTranscript = "";

    // 把 server 的 fragmented text 消息拼接成完整 JSON
    private final StringBuilder serverMsgBuf = new StringBuilder();

    // 诊断：是否收到过任何一条服务端消息
    private final AtomicBoolean receivedAnyServerMessage = new AtomicBoolean(false);

    // 顺序发送队列（用于串行化 sendText，避免多线程同时 send）
    private final ConcurrentLinkedQueue<String> sendQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean draining = new AtomicBoolean(false);

    public GeminiLiveSession(AiConfigProperties aiConfig,
                             PromptLoader promptLoader,
                             WebSocketSession clientSession) {
        this.aiConfig = aiConfig;
        this.promptLoader = promptLoader;
        this.clientSession = clientSession;
    }

    /**
     * 连接到 Gemini Live 并发送 setup。
     *
     * @param model google/gemini-xxx 或 gemini-xxx
     * @param mode text|audio|both
     * @param locale BCP-47 或简写（用于 prompt/输出语言偏好）
     * @param scenario 口语训练场景（用于 prompt）
     */
    public CompletableFuture<Void> connect(String model, String mode, String locale, String scenario) {
        if (closed.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("session closed"));
        }

        String apiKey = aiConfig.getProviders().getGoogle().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            return CompletableFuture.failedFuture(new IllegalStateException("Google Gemini API Key 未配置"));
        }

        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        boolean allowRetryWithProxy = pc != null && pc.isEnabled() && pc.isAutoRetryWithProxy() && !pc.isAlwaysUseProxy();

        // setupComplete 等待时间：国内网络/代理握手往往 > 12s，这里适当放宽，避免误判超时
        int t1 = 25;
        int t2 = 45;
        try {
            if (pc != null) {
                int ct = Math.max(1, pc.getConnectTimeoutMs() / 1000);
                // 确保至少覆盖 connectTimeout + 一点缓冲
                t1 = Math.max(t1, ct + 10);
                t2 = Math.max(t2, ct + 25);
            }
        } catch (Exception ignore) { }
        final int setupTimeout1 = t1;
        final int setupTimeout2 = t2;

        // 尝试 1：按配置直连/强制代理（alwaysUseProxy=true 则内部会直接走代理）
        CompletableFuture<Void> first = connectOnce(apiKey, model, mode, locale, scenario, /*forceProxy*/ false, /*setupTimeoutSec*/ setupTimeout1);

        if (!allowRetryWithProxy) {
            return first;
        }

        // 尝试 2：若直连失败（含 setupComplete 超时），自动切代理重试一次
        return first.handle((ok, ex) -> {
            if (ex == null) return CompletableFuture.<Void>completedFuture(null);

            Throwable root = unwrapCompletionException(ex);
            boolean shouldRetry = root instanceof java.util.concurrent.TimeoutException
                    || root instanceof java.net.ConnectException
                    || root instanceof java.net.UnknownHostException
                    || root instanceof java.net.SocketTimeoutException
                    || (root != null && String.valueOf(root.getMessage()).toLowerCase().contains("handshake"))
                    || (root != null && String.valueOf(root.getMessage()).toLowerCase().contains("connect"));

            if (!shouldRetry) {
                return CompletableFuture.<Void>failedFuture(root);
            }

            notifyClientWarn("RETRY_WITH_PROXY", "直连 Gemini Live 失败，正在尝试使用代理重试...");
            return connectOnce(apiKey, model, mode, locale, scenario, /*forceProxy*/ true, /*setupTimeoutSec*/ setupTimeout2);
        }).thenCompose(f -> f);
    }

    private CompletableFuture<Void> connectOnce(String apiKey,
                                               String model,
                                               String mode,
                                               String locale,
                                               String scenario,
                                               boolean forceProxy,
                                               int setupTimeoutSec) {
        if (closed.get()) {
            return CompletableFuture.failedFuture(new IllegalStateException("session closed"));
        }

        URI uri;
        try {
            uri = new URI(GEMINI_LIVE_WS_ENDPOINT);
        } catch (URISyntaxException e) {
            return CompletableFuture.failedFuture(e);
        }

        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        boolean usingProxy = pc != null && pc.isEnabled() && (pc.isAlwaysUseProxy() || forceProxy);
        if (usingProxy) {
            log.info("Gemini Live connecting with proxy {}:{} (type={}, model={}, mode={})",
                    pc.getHost(), pc.getPort(), pc.getType(), model, mode);
        } else {
            log.info("Gemini Live connecting directly (model={}, mode={})", model, mode);
        }

        // 本次连接对应的 setupComplete future（注意：每次尝试都要新的 future）
        this.setupCompleteFuture = new CompletableFuture<>();

        OkHttpClient client = buildOkHttpClientWithProxyIfNeeded(forceProxy);
        // 兼容鉴权：Live WebSocket 在部分环境下更倾向使用 ?key= 方式（同时保留 x-goog-api-key 头）
        String wsUrl = appendApiKeyToWsUrl(uri.toString(), apiKey);
        Request req = new Request.Builder()
                .url(wsUrl)
                .addHeader("x-goog-api-key", apiKey)
                .build();
        this.geminiWs = client.newWebSocket(req, new GeminiOkListener(model, mode, locale, scenario));
        connected.set(true);

        // 等待 setupComplete（避免前端等待 session_ready 超时）
        return this.setupCompleteFuture
                .orTimeout(setupTimeoutSec, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    Throwable root = unwrapCompletionException(ex);
                    if (root instanceof java.util.concurrent.TimeoutException) {
                        // 给前端更可读的提示：多半是代理类型/端口不对，或 wss CONNECT 被拦截
                        throw new java.util.concurrent.CompletionException(
                                new java.util.concurrent.TimeoutException(
                                        "等待 Gemini Live setupComplete 超时（可能代理端口/类型不对，建议优先改用 Clash 的 SOCKS 端口，并将 AI_PROXY_TYPE=SOCKS）"
                                )
                        );
                    }
                    throw new java.util.concurrent.CompletionException(root != null ? root : ex);
                })
                .whenComplete((ok, ex) -> {
            if (ex != null) {
                // 失败：仅关闭本次尝试的 Gemini WS，不能把整个 session 标记为 closed；
                // 否则会导致“自动切代理重试”直接失败（session closed）。
                closeGeminiSocketSilently();
                Throwable root = unwrapCompletionException(ex);
                String why = root != null ? (root.getClass().getSimpleName() + ": " + String.valueOf(root.getMessage())) : "unknown";
                log.warn("Gemini Live connect attempt failed (usingProxy={}, model={}, mode={}): {}", usingProxy, model, mode, why);
            }
        });
    }

    private void closeGeminiSocketSilently() {
        try {
            WebSocket ws = this.geminiWs;
            this.geminiWs = null;
            connected.set(false);
            if (ws != null) {
                try { ws.close(1000, "attempt_failed"); } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {
        }
    }

    private Throwable unwrapCompletionException(Throwable ex) {
        if (ex == null) return null;
        if (ex instanceof java.util.concurrent.CompletionException || ex instanceof java.util.concurrent.ExecutionException) {
            return ex.getCause() != null ? ex.getCause() : ex;
        }
        return ex;
    }

    /**
     * 发送一段实时音频（PCM16 base64）。
     */
    public void sendAudioChunk(String pcm16Base64, Integer sampleRate) {
        if (!connected.get() || closed.get()) return;
        if (!StringUtils.hasText(pcm16Base64)) return;

        // Live API Blob: { mimeType, data }
        int rate = (sampleRate == null || sampleRate <= 0) ? 16000 : sampleRate;
        Map<String, Object> blob = new HashMap<>();
        blob.put("mimeType", "audio/pcm;rate=" + rate);
        blob.put("data", pcm16Base64);

        Map<String, Object> realtimeInput = new HashMap<>();
        realtimeInput.put("audio", blob);

        Map<String, Object> msg = new HashMap<>();
        msg.put("realtimeInput", realtimeInput);

        try {
            enqueueSend(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 手动标记活动开始（禁用自动 VAD 时需要）。
     */
    public void sendActivityStart() {
        if (!connected.get() || closed.get()) return;
        // 新回合开始：清空上一回合的累计状态，避免“上一段转写在下一段重复出现”
        lastUserTranscript = "";
        lastAssistantTranscript = "";
        lastAssistantText = "";
        Map<String, Object> realtimeInput = new HashMap<>();
        realtimeInput.put("activityStart", Map.of());
        Map<String, Object> msg = new HashMap<>();
        msg.put("realtimeInput", realtimeInput);
        try {
            enqueueSend(mapper.writeValueAsString(msg));
        } catch (Exception ignore) {
        }
    }

    /**
     * 手动标记活动结束（禁用自动 VAD 时需要，用于触发模型开始生成）。
     */
    public void sendActivityEnd() {
        if (!connected.get() || closed.get()) return;
        Map<String, Object> realtimeInput = new HashMap<>();
        realtimeInput.put("activityEnd", Map.of());
        Map<String, Object> msg = new HashMap<>();
        msg.put("realtimeInput", realtimeInput);
        try {
            enqueueSend(mapper.writeValueAsString(msg));
        } catch (Exception ignore) {
        }
    }

    /**
     * 主动结束会话。
     */
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        try {
            if (geminiWs != null) {
                geminiWs.close(1000, "client_closed");
            }
        } catch (Exception ignore) {
        }
    }

    private void enqueueSend(String json) {
        if (closed.get()) return;
        sendQueue.add(json);
        drainSendQueue();
    }

    /**
     * 串行 drain 发送队列，限制 in-flight 数量。
     */
    private void drainSendQueue() {
        if (!draining.compareAndSet(false, true)) return;
        try {
            while (!closed.get()) {
                if (inFlightSends.get() >= maxInFlight) {
                    // 过载：丢弃后续音频（保守策略，避免内存爆）
                    sendQueue.clear();
                    notifyClientWarn("THROTTLED", "发送过快，已触发节流，请放慢语速或稍后重试");
                    break;
                }
                String next = sendQueue.poll();
                if (next == null) break;
                WebSocket ws = this.geminiWs;
                if (ws == null) break;
                inFlightSends.incrementAndGet();
                try {
                    boolean ok = ws.send(next);
                    if (!ok) {
                        // 发送失败时不要“沉默等待 setupComplete”，直接失败并给出提示
                        CompletableFuture<Void> f = setupCompleteFuture;
                        if (f != null && !f.isDone()) {
                            f.completeExceptionally(new IllegalStateException("WebSocket 发送失败（可能代理对 wss/CONNECT 不兼容或连接已关闭）"));
                        }
                        notifyClientError("GEMINI_WS_SEND_FAILED", "WebSocket 发送失败（可能代理不兼容），请切换 SOCKS 或检查 Clash 规则/全局模式");
                        try { ws.close(1000, "send_failed"); } catch (Exception ignore) {}
                        break;
                    }
                } catch (Exception ignore) {
                } finally {
                    inFlightSends.decrementAndGet();
                }
            }
        } finally {
            draining.set(false);
            // 若 drain 过程中有新数据入队，重新触发一次
            if (!sendQueue.isEmpty() && !closed.get()) {
                drainSendQueue();
            }
        }
    }

    private OkHttpClient buildOkHttpClientWithProxyIfNeeded(boolean forceProxy) {
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        boolean useProxy = pc != null && pc.isEnabled() && (pc.isAlwaysUseProxy() || forceProxy);
        long ct = Math.max(1000, pc != null ? pc.getConnectTimeoutMs() : 10000);

        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(ct))
                .readTimeout(Duration.ZERO) // WebSocket 长连接不超时
                .writeTimeout(Duration.ofSeconds(30))
                .pingInterval(Duration.ofSeconds(20));

        if (useProxy && pc != null && StringUtils.hasText(pc.getHost()) && pc.getPort() > 0) {
            Proxy.Type type = "SOCKS".equalsIgnoreCase(pc.getType()) ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
            b.proxy(new Proxy(type, new InetSocketAddress(pc.getHost(), pc.getPort())));
        } else {
            // 强制直连：避免系统默认代理影响
            b.proxy(Proxy.NO_PROXY);
        }
        return b.build();
    }

    /**
     * 为 WebSocket URL 追加 API key（避免某些环境下 header 鉴权不生效导致 setupComplete 永远不返回）。
     */
    private String appendApiKeyToWsUrl(String url, String apiKey) {
        try {
            if (!StringUtils.hasText(apiKey)) return url;
            String enc = URLEncoder.encode(apiKey, java.nio.charset.StandardCharsets.UTF_8);
            if (url.contains("?")) {
                return url + "&key=" + enc;
            }
            return url + "?key=" + enc;
        } catch (Exception ignore) {
            return url;
        }
    }

    private String buildSetupMessage(String model, String mode, String locale, String scenario) {
        String modelResource = normalizeModelResource(model);

        // systemInstruction：优先口语训练 prompt（后续会在 prompt-speaking todo 补齐文件）
        String systemText = "";
        try {
            systemText = promptLoader.loadSystemPrompt("classpath:/prompts/speaking_practice_system_prompt.v1.txt");
        } catch (Exception e) {
            try {
                systemText = promptLoader.loadSystemPrompt();
            } catch (Exception ignored) {
                systemText = "";
            }
        }
        // 动态补充场景/语言偏好
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(systemText)) sb.append(systemText.trim()).append("\n\n");
        if (StringUtils.hasText(locale)) sb.append("语言偏好: ").append(locale).append("\n");
        if (StringUtils.hasText(scenario)) sb.append("训练场景: ").append(scenario).append("\n");

        // Live API 的 systemInstruction 类型为 Content：建议显式带 role
        Map<String, Object> systemInstruction = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", sb.toString().trim()))
        );

        boolean wantAudio = "audio".equalsIgnoreCase(mode) || "both".equalsIgnoreCase(mode);
        boolean wantText = "text".equalsIgnoreCase(mode) || !StringUtils.hasText(mode);

        // Live API 限制：responseModalities 一次只能选择 TEXT 或 AUDIO（不能同时 TEXT+AUDIO）。
        // “音频+文字”通过 AUDIO 输出 + outputAudioTranscription 实现（音频 + 文字转写）。
        List<String> responseModalities = new ArrayList<>();
        if (wantAudio) {
            responseModalities.add("AUDIO");
        } else {
            responseModalities.add("TEXT");
        }

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseModalities", responseModalities);
        generationConfig.put("candidateCount", 1);

        if (wantAudio) {
            // 参考 Speech generation 文档：speechConfig.voiceConfig.prebuiltVoiceConfig.voiceName
            generationConfig.put("speechConfig", Map.of(
                    "voiceConfig", Map.of(
                            "prebuiltVoiceConfig", Map.of("voiceName", "Kore")
                    )
            ));
        }

        // 启用转写（输入/输出）
        Map<String, Object> setup = new HashMap<>();
        setup.put("model", modelResource);
        setup.put("generationConfig", generationConfig);
        setup.put("systemInstruction", systemInstruction);
        setup.put("inputAudioTranscription", Map.of());
        if (wantAudio) {
            // 让“音频输出”同时产出文字（转写）
            setup.put("outputAudioTranscription", Map.of());
        }

        // 关键：禁用自动 activity detection（VAD），由前端通过 activity_start/activity_end 手动控制回合。
        // 这样可以做到“我点停止 -> 立刻触发模型回复”的口语训练交互。
        setup.put("realtimeInputConfig", Map.of(
                "automaticActivityDetection", Map.of("disabled", true)
        ));

        return toJsonOrThrow(Map.of("setup", setup));
    }

    private String normalizeModelResource(String model) {
        String m = String.valueOf(model == null ? "" : model).trim();
        if (m.startsWith("google/")) {
            m = m.substring("google/".length());
        }
        if (m.startsWith("models/")) {
            return m;
        }
        if (!m.isEmpty()) {
            return "models/" + m;
        }
        // 兜底：用后端默认模型（deepseek.model 当前配置为 google/gemini-2.5-pro）
        String def = aiConfig.getDeepseek() != null ? aiConfig.getDeepseek().getModel() : "google/gemini-2.5-pro";
        if (def.startsWith("google/")) def = def.substring("google/".length());
        if (!def.startsWith("models/")) def = "models/" + def;
        return def;
    }

    private String toJsonOrThrow(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("json serialize failed", e);
        }
    }

    private void notifyClientWarn(String code, String message) {
        try {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "warn");
            err.put("code", code);
            err.put("message", message);
            safeSendToClient(err);
        } catch (Exception ignore) {
        }
    }

    private void notifyClientError(String code, String message) {
        try {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("code", code);
            err.put("message", message);
            safeSendToClient(err);
        } catch (Exception ignore) {
        }
    }

    private void safeSendToClient(Map<String, Object> payload) {
        if (!clientSession.isOpen()) return;
        try {
            String json = mapper.writeValueAsString(payload);
            synchronized (clientSession) {
                clientSession.sendMessage(new TextMessage(json));
            }
        } catch (Exception ignore) {
        }
    }

    private void handleGeminiServerMessage(Map<?, ?> msg) {
        if (msg == null) return;

        // 只打印一次：确认我们到底有没有收到服务端消息
        if (receivedAnyServerMessage.compareAndSet(false, true)) {
            try {
                log.info("Gemini Live first server message keys={}", msg.keySet());
            } catch (Exception ignore) {
            }
        }

        // Gemini Live 可能返回 error 消息（例如：模型不支持 Live / setup 配置错误 / key 权限问题）。
        // 若不处理，会导致前端只能看到 setupComplete 超时（TimeoutException），定位困难。
        Object errObj = msg.get("error");
        if (errObj instanceof Map<?, ?> err) {
            Object code = err.get("code");
            Object message = err.get("message");
            String m = message != null ? String.valueOf(message) : "Gemini Live error";
            String c = code != null ? String.valueOf(code) : "GEMINI_ERROR";
            CompletableFuture<Void> f = this.setupCompleteFuture;
            if (f != null && !f.isDone()) {
                f.completeExceptionally(new IllegalStateException(c + ": " + m));
            }
            notifyClientError("GEMINI_ERROR", c + ": " + m);
            return;
        }

        if (msg.containsKey("setupComplete")) {
            safeSendToClient(Map.of("type", "session_ready"));
            CompletableFuture<Void> f = this.setupCompleteFuture;
            if (f != null && !f.isDone()) f.complete(null);
            return;
        }

        Object serverContent = msg.get("serverContent");
        if (!(serverContent instanceof Map<?, ?> sc)) {
            return;
        }

        // 1) 输入转写
        Object inTr = sc.get("inputTranscription");
        if (inTr instanceof Map<?, ?> tr) {
            Object text = tr.get("text");
            if (text != null) {
                String next = normalizeTranscriptUpdate(lastUserTranscript, String.valueOf(text));
                lastUserTranscript = next;
                safeSendToClient(Map.of(
                        "type", "transcript",
                        "isFinal", true,
                        "text", next
                ));
            }
        }

        // 2) 输出转写（模型音频对应的文字）
        Object outTr = sc.get("outputTranscription");
        if (outTr instanceof Map<?, ?> tr) {
            Object text = tr.get("text");
            if (text != null) {
                String next = normalizeTranscriptUpdate(lastAssistantTranscript, String.valueOf(text));
                lastAssistantTranscript = next;
                safeSendToClient(Map.of(
                        "type", "assistant_transcript",
                        "isFinal", true,
                        "text", next
                ));
            }
        }

        // 3) 模型输出（文本/音频）
        Object modelTurn = sc.get("modelTurn");
        if (modelTurn instanceof Map<?, ?> mt) {
            Object partsObj = mt.get("parts");
            if (partsObj instanceof List<?> parts) {
                for (Object p : parts) {
                    if (p instanceof Map<?, ?> pm) {
                        Object text = pm.get("text");
                        if (text != null) {
                            String full = String.valueOf(text);
                            String delta = computeDelta(full);
                            if (StringUtils.hasText(delta)) {
                                safeSendToClient(Map.of("type", "assistant_text", "delta", delta, "isFinal", false));
                            }
                        }
                        Object inlineData = pm.get("inlineData");
                        if (inlineData instanceof Map<?, ?> id) {
                            Object data = id.get("data");
                            Object mimeType = id.get("mimeType");
                            if (data != null) {
                                safeSendToClient(Map.of(
                                        "type", "assistant_audio",
                                        "pcm16Base64", String.valueOf(data),
                                        "mimeType", mimeType != null ? String.valueOf(mimeType) : null
                                ));
                            }
                        }
                    }
                }
            }
        }

        // 4) turn complete / interrupted
        if (Boolean.TRUE.equals(sc.get("interrupted"))) {
            lastAssistantText = "";
            safeSendToClient(Map.of("type", "interrupted"));
        }
        if (Boolean.TRUE.equals(sc.get("generationComplete"))) {
            safeSendToClient(Map.of("type", "generation_complete"));
        }
        if (Boolean.TRUE.equals(sc.get("turnComplete"))) {
            safeSendToClient(Map.of("type", "turn_complete"));
        }
    }

    private String computeDelta(String full) {
        if (full == null) return "";
        String prev = lastAssistantText == null ? "" : lastAssistantText;
        if (full.startsWith(prev)) {
            String delta = full.substring(prev.length());
            lastAssistantText = full;
            return delta;
        }
        // 非前缀：重置（避免错位）
        lastAssistantText = full;
        return full;
    }

    /**
     * 将转写结果尽量归一化为“完整文本”。
     * <p>
     * 不同环境下，Live API 的 transcription 可能是：
     * - 全量（从头到当前）
     * - 分段（只给最新片段）
     * 这里用“前缀/包含”判断 + 兜底拼接，避免前端只显示最后一段。
     */
    private String normalizeTranscriptUpdate(String prev, String incoming) {
        String p = prev == null ? "" : prev.trim();
        String n = incoming == null ? "" : incoming.trim();
        if (n.isBlank()) return p;
        if (p.isBlank()) return n;
        // 包含关系：避免重复拼接
        if (p.contains(n)) return p;
        if (n.contains(p)) return n;
        if (n.startsWith(p)) return n;      // n 是全量
        if (p.startsWith(n)) return p;      // 回退/重复
        // 分段：拼接（加空格分隔，避免粘连）
        return p + " " + n;
    }

    private class GeminiOkListener extends WebSocketListener {
        private final String model;
        private final String mode;
        private final String locale;
        private final String scenario;

        private GeminiOkListener(String model, String mode, String locale, String scenario) {
            this.model = model;
            this.mode = mode;
            this.locale = locale;
            this.scenario = scenario;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            try {
                // setup 必须为第一条消息
                String setupJson = buildSetupMessage(model, mode, locale, scenario);
                enqueueSend(setupJson);
                log.info("Gemini Live WS opened (http={}): setup sent (model={}, mode={})",
                        response != null ? response.code() : null, model, mode);
            } catch (Exception e) {
                CompletableFuture<Void> f = setupCompleteFuture;
                if (f != null && !f.isDone()) {
                    f.completeExceptionally(e);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            // 兼容服务端把多个 JSON 对象拼在一个 WS message/或跨帧分片的情况：
            // - 将文本追加到缓冲
            // - 尝试从缓冲中解析出 1..N 个 JSON 对象并逐个处理
            serverMsgBuf.append(text);
            processServerBuffer();
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            // 兼容：若服务端以二进制帧发送（某些代理/实现可能导致文本帧被转为二进制）
            try {
                receivedAnyServerMessage.compareAndSet(false, true);
                String text = bytes != null ? bytes.utf8() : null;
                if (text != null && !text.isBlank()) {
                    serverMsgBuf.append(text);
                    processServerBuffer();
                    return;
                }
                log.warn("Gemini live binary message received (len={})", bytes != null ? bytes.size() : 0);
            } catch (Exception e) {
                log.warn("Failed to decode binary WS message: {}", e.getMessage());
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            connected.set(false);
            closed.set(true);
            log.warn("Gemini Live WS closed: code={}, reason={}", code, reason);
            CompletableFuture<Void> f = setupCompleteFuture;
            if (f != null && !f.isDone()) {
                f.completeExceptionally(new IllegalStateException("gemini ws closed before setupComplete: " + code + " " + reason));
            }
            safeSendToClient(Map.of("type", "closed", "code", code, "reason", reason));
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            connected.set(false);
            String msg = t != null ? String.valueOf(t.getMessage()) : "unknown";
            if (response != null) {
                msg = msg + " (http=" + response.code() + ")";
            }
            log.warn("Gemini Live WS error: {}", msg);
            notifyClientError("GEMINI_WS_ERROR", msg);
            CompletableFuture<Void> f = setupCompleteFuture;
            if (f != null && !f.isDone()) {
                f.completeExceptionally(t != null ? t : new IllegalStateException("gemini ws error"));
            }
        }
    }

    /**
     * 从 serverMsgBuf 中尽可能解析出多个顶层 JSON 对象并逐个处理。
     * 若遇到“EOF/不完整 JSON”，保留缓冲等待下一段；若遇到非 EOF 的解析错误，清空缓冲避免卡死。
     */
    private void processServerBuffer() {
        if (serverMsgBuf.length() == 0) return;
        if (serverMsgBuf.length() > 1024 * 1024) {
            serverMsgBuf.setLength(0);
            log.warn("Gemini live message buffer too large, dropped.");
            return;
        }

        String raw = serverMsgBuf.toString();
        long lastConsumed = 0;
        try (JsonParser p = jsonFactory.createParser(raw)) {
            while (true) {
                JsonToken t = p.nextToken();
                if (t == null) break;
                if (t == JsonToken.START_OBJECT) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> msg = mapper.readValue(p, Map.class);
                    // 读取一个对象后，当前位置已在对象结束之后
                    lastConsumed = p.currentLocation().getCharOffset();
                    handleGeminiServerMessage(msg);
                } else {
                    p.skipChildren();
                }
            }
        } catch (com.fasterxml.jackson.core.io.JsonEOFException eof) {
            // 不完整 JSON：等待更多数据
            return;
        } catch (Exception e) {
            // 非 EOF：清空避免一直解析失败
            log.warn("Failed to parse Gemini live message buffer, dropped. err={}", e.getMessage());
            serverMsgBuf.setLength(0);
            return;
        }

        if (lastConsumed > 0 && lastConsumed <= raw.length()) {
            serverMsgBuf.delete(0, (int) lastConsumed);
        } else if (lastConsumed >= raw.length()) {
            serverMsgBuf.setLength(0);
        }
    }
}

