package com.noncore.assessment.service.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.noncore.assessment.config.AiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Slf4j
@Component
public class LlmClient {

    private final RestTemplate directRestTemplate;
    private final AiConfigProperties aiConfig;

    @Autowired
    public LlmClient(AiConfigProperties aiConfig) {
        this.aiConfig = aiConfig;
        this.directRestTemplate = buildRestTemplate();
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        factory.setConnectTimeout(pc.getConnectTimeoutMs());
        factory.setReadTimeout(pc.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    private RestTemplate buildRestTemplateForProxy(ProxyTarget target) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        factory.setConnectTimeout(pc.getConnectTimeoutMs());
        factory.setReadTimeout(pc.getReadTimeoutMs());
        Proxy proxy = new Proxy(
                "SOCKS".equalsIgnoreCase(target.type()) ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
                new InetSocketAddress(target.host(), target.port())
        );
        factory.setProxy(proxy);
        return new RestTemplate(factory);
    }

    public String createChatCompletion(List<Message> messages, String model, boolean stream, String baseUrl, String apiKey) {
        // 向后兼容：仅字符串 content 的简单消息
        List<Map<String, Object>> payloadMessages = messages.stream().map(m -> Map.<String, Object>of(
                "role", m.getRole(),
                "content", m.getContent()
        )).toList();
        return createChatCompletionRaw(payloadMessages, model, stream, baseUrl, apiKey);
    }

    /**
     * 直接发送 OpenAI 兼容的消息结构。支持 content 为字符串或数组（image_url 等多模态）。
     */
    public String createChatCompletionRaw(List<Map<String, Object>> payloadMessages, String model, boolean stream, String baseUrl, String apiKey) {
        String url = buildChatUrl(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        // 不启用 reasoning，确保广泛兼容
        RequestBody body = buildBody(payloadMessages, model, false, false);
        return doRequest(url, headers, body);
    }

    /**
     * JSON-only 变体：强制上游以 JSON 对象返回（OpenAI 兼容 response_format）。
     */
    public String createChatCompletionJsonOnly(List<Map<String, Object>> payloadMessages, String model, boolean stream, String baseUrl, String apiKey) {
        return createChatCompletionJsonOnly(payloadMessages, model, stream, baseUrl, apiKey, true);
    }

    /**
     * JSON-only 变体。部分 OpenAI-compatible 模型（如某些火山方舟模型）不支持 response_format，
     * 此时使用 prompt-only JSON 约束，并保留一次自动降级重试。
     */
    public String createChatCompletionJsonOnly(List<Map<String, Object>> payloadMessages,
                                               String model,
                                               boolean stream,
                                               String baseUrl,
                                               String apiKey,
                                               boolean preferResponseFormat) {
        String url = buildChatUrl(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        List<Map<String, Object>> messages = preferResponseFormat ? payloadMessages : withJsonOnlyInstruction(payloadMessages);
        RequestBody body = buildBody(messages, model, false, preferResponseFormat);
        try {
            return doRequest(url, headers, body);
        } catch (BusinessException ex) {
            if (preferResponseFormat && isUnsupportedResponseFormatError(ex.getMessage())) {
                log.warn("LLM model {} does not support response_format, retrying with prompt-only JSON mode", model);
                RequestBody fallbackBody = buildBody(withJsonOnlyInstruction(payloadMessages), model, false, false);
                return doRequest(url, headers, fallbackBody);
            }
            throw ex;
        }
    }

    private RequestBody buildBody(List<Map<String, Object>> payloadMessages, String model, boolean includeReasoning, boolean jsonOnly) {
        RequestBody body = new RequestBody();
        body.setModel(model);
        body.setMessages(payloadMessages);
        body.setStream(false);
        body.setTemperature(0.2);
        body.setMaxTokens(8192);
        // 不附带 reasoning，避免通道不兼容
        if (jsonOnly) {
            body.setResponseFormat(java.util.Map.of("type", "json_object"));
        }
        return body;
    }

    private List<Map<String, Object>> withJsonOnlyInstruction(List<Map<String, Object>> payloadMessages) {
        String instruction = "You must respond with exactly one valid JSON object. Do not include markdown fences, explanations, or any text outside the JSON object.";
        List<Map<String, Object>> out = new ArrayList<>();
        if (payloadMessages != null && !payloadMessages.isEmpty()
                && "system".equals(String.valueOf(payloadMessages.get(0).get("role")))) {
            Map<String, Object> first = new LinkedHashMap<>(payloadMessages.get(0));
            Object content = first.get("content");
            first.put("content", instruction + "\n\n" + String.valueOf(content == null ? "" : content));
            out.add(first);
            out.addAll(payloadMessages.subList(1, payloadMessages.size()));
            return out;
        }
        out.add(Map.of("role", "system", "content", instruction));
        if (payloadMessages != null) {
            out.addAll(payloadMessages);
        }
        return out;
    }

    private boolean isUnsupportedResponseFormatError(String message) {
        if (!StringUtils.hasText(message)) return false;
        String lower = message.toLowerCase();
        return lower.contains("response_format")
                && (lower.contains("not support")
                || lower.contains("not supported")
                || lower.contains("not valid")
                || lower.contains("invalid")
                || lower.contains("unsupported")
                || lower.contains("不支持"));
    }

    private String doRequest(String url, HttpHeaders headers, RequestBody body) {
        List<TransportPlan> plans = buildTransportPlans();
        RestClientException last = null;
        String lastPlan = plans.isEmpty() ? "direct" : plans.get(0).label();

        for (int i = 0; i < plans.size(); i++) {
            TransportPlan plan = plans.get(i);
            try {
                return doRequestViaTransport(plan, url, headers, body);
            } catch (BusinessException be) {
                // 上游已返回可读业务错误（401/402/403/5xx等），不切链路重试
                throw be;
            } catch (RestClientException e) {
                last = e;
                lastPlan = plan.label();
                if (shouldRetryWithNextTransport(e) && i < plans.size() - 1) {
                    log.warn("LLM {} failed, switching to {} ... err={}",
                            plan.label(), plans.get(i + 1).label(), safeErr(e));
                    continue;
                }
                log.error("LLM request error via {}", plan.label(), e);
                break;
            }
        }

        String msg = rootMessage(last);
        if (looksLikeNetworkIssue(last)) {
            msg = msg + "（网络/代理连接异常，请检查 AI_PROXY_HOST/AI_PROXY_PORT/AI_PROXY_TYPE，或确认本机代理已启动）";
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "LLM请求异常(via=" + lastPlan + "): " + msg);
    }

    private String doRequestViaTransport(TransportPlan plan, String url, HttpHeaders headers, RequestBody body) {
        try {
            log.info("LLM request via {} -> {}", plan.label(), url);
            HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) plan.template().exchange(url, HttpMethod.POST, entity, Map.class);
            return parseSuccessfulResponse(resp);
        } catch (RestClientException e) {
            if (e instanceof HttpStatusCodeException statusEx) {
                String message = buildFriendlyError(statusEx, url, body == null ? null : body.getModel());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, message);
            }
            throw e;
        }
    }

    private String parseSuccessfulResponse(ResponseEntity<Map<String, Object>> resp) {
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            log.error("LLM non-2xx or empty body: status={}, body={}", resp.getStatusCode(), resp.getBody());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "LLM错误: " + resp.getStatusCode());
        }

        Map<String, Object> respBody = resp.getBody();
        // GLM 有时会包裹一层 data，或在顶层返回 output_text/text
        Map<?, ?> root = unwrapData(respBody);

        String direct = extractFromMap(root);
        if (direct != null && !direct.isBlank()) {
            return direct;
        }

        // 兼容 data 数组/未解包的顶层字段
        String fallbackBody = extractFromMap(respBody);
        if (fallbackBody != null && !fallbackBody.isBlank()) {
            return fallbackBody;
        }

        Object choices = root != null ? root.get("choices") : null;
        if (choices instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> m) {
                String fromChoice = extractFromMap(m);
                if (fromChoice != null && !fromChoice.isBlank()) {
                    return fromChoice;
                }
            }
        }
        // 尝试返回原始 JSON 字符串，避免前端收到空结果
        try {
            return OBJECT_MAPPER.writeValueAsString(respBody);
        } catch (Exception ignored) {
        }
        String asString = String.valueOf(respBody);
        if (asString != null && !asString.isBlank()) {
            return asString;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法解析大模型返回");
    }

    private List<TransportPlan> buildTransportPlans() {
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        boolean proxyEnabled = pc != null && pc.isEnabled();
        boolean alwaysProxy = proxyEnabled && pc.isAlwaysUseProxy();
        boolean allowRetryWithProxy = proxyEnabled && pc.isAutoRetryWithProxy() && !alwaysProxy;

        List<TransportPlan> plans = new ArrayList<>();
        if (alwaysProxy) {
            List<ProxyTarget> targets = buildProxyTargets(pc);
            if (targets.isEmpty()) {
                plans.add(new TransportPlan("direct", directRestTemplate));
            } else {
                for (ProxyTarget target : targets) {
                    plans.add(new TransportPlan(
                            "proxy " + target.host() + ":" + target.port() + " (" + target.type() + ")",
                            buildRestTemplateForProxy(target)));
                }
            }
            return plans;
        }

        plans.add(new TransportPlan("direct", directRestTemplate));
        if (allowRetryWithProxy) {
            for (ProxyTarget target : buildProxyTargets(pc)) {
                plans.add(new TransportPlan(
                        "proxy " + target.host() + ":" + target.port() + " (" + target.type() + ")",
                        buildRestTemplateForProxy(target)));
            }
        }
        return plans;
    }

    private List<ProxyTarget> buildProxyTargets(AiConfigProperties.ProxyConfig pc) {
        if (pc == null || !pc.isEnabled()) return List.of();
        LinkedHashMap<String, ProxyTarget> dedup = new LinkedHashMap<>();
        String host = pc.getHost();
        int port = pc.getPort();
        String type = normalizeProxyType(pc.getType());

        addProxyTarget(dedup, host, port, type);
        addProxyTarget(dedup, host, port, flipProxyType(type));
        if (isLikelyLocalHost(host)) {
            for (String h : List.of("127.0.0.1", "localhost")) {
                for (int p : new int[]{7890, 7897}) {
                    addProxyTarget(dedup, h, p, type);
                    addProxyTarget(dedup, h, p, flipProxyType(type));
                }
            }
        }
        return new ArrayList<>(dedup.values());
    }

    private void addProxyTarget(LinkedHashMap<String, ProxyTarget> dedup, String host, int port, String type) {
        if (!StringUtils.hasText(host) || port <= 0) return;
        String normalizedType = normalizeProxyType(type);
        String key = host.trim().toLowerCase() + ":" + port + ":" + normalizedType;
        dedup.putIfAbsent(key, new ProxyTarget(host.trim(), port, normalizedType));
    }

    private String normalizeProxyType(String type) {
        return "SOCKS".equalsIgnoreCase(type) ? "SOCKS" : "HTTP";
    }

    private String flipProxyType(String type) {
        return "SOCKS".equalsIgnoreCase(type) ? "HTTP" : "SOCKS";
    }

    private boolean isLikelyLocalHost(String host) {
        if (!StringUtils.hasText(host)) return false;
        String h = host.trim().toLowerCase();
        return "127.0.0.1".equals(h) || "localhost".equals(h) || "::1".equals(h);
    }

    private boolean looksLikeNetworkIssue(Throwable e) {
        if (e == null) return false;
        Throwable t = e;
        for (int i = 0; i < 10 && t != null; i++) {
            if ((t instanceof java.net.ConnectException)
                    || (t instanceof java.net.UnknownHostException)
                    || (t instanceof java.net.SocketTimeoutException)
                    || (t instanceof java.net.SocketException)
                    || (t instanceof java.io.EOFException)
                    || (t instanceof java.io.IOException)
                    || (t instanceof javax.net.ssl.SSLException)) {
                return true;
            }
            String m = String.valueOf(t.getMessage()).toLowerCase();
            if (m.contains("handshake")
                    || m.contains("remote host terminated")
                    || m.contains("connection reset")
                    || m.contains("broken pipe")
                    || m.contains("unexpected end of stream")
                    || m.contains("connect timed out")
                    || m.contains("connection refused")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private boolean shouldRetryWithNextTransport(Throwable e) {
        if (!looksLikeNetworkIssue(e)) return false;
        Throwable t = e;
        for (int i = 0; i < 10 && t != null; i++) {
            String m = String.valueOf(t.getMessage()).toLowerCase();
            if (t instanceof java.net.SocketTimeoutException && m.contains("read timed out")) {
                return false;
            }
            t = t.getCause();
        }
        return true;
    }

    private String safeErr(Throwable e) {
        try {
            return e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
        } catch (Exception ignore) {
            return "unknown";
        }
    }

    private String rootMessage(Throwable e) {
        if (e == null) return "unknown";
        Throwable t = e;
        for (int i = 0; i < 10 && t.getCause() != null; i++) {
            t = t.getCause();
        }
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

    private String buildFriendlyError(HttpStatusCodeException ex, String url, String model) {
        String provider = url != null && url.contains("open.bigmodel.cn")
                ? "GLM"
                : (url != null && url.contains("volces.com") ? "火山方舟" : "LLM");
        String upstreamMessage = extractErrorMessage(ex.getResponseBodyAsString());
    
        HttpStatusCode status = ex.getStatusCode();
        String reason;
    
        // 兼容 Spring Boot 3：HttpStatusCode 不再有 getReasonPhrase()，必须判断类型
        if (status instanceof HttpStatus httpStatus) {
            reason = status.value() + " " + httpStatus.getReasonPhrase();
        } else {
            reason = String.valueOf(status.value());
        }
    
        // 余额不足（402）
        if (status == HttpStatus.PAYMENT_REQUIRED) {
            return appendVolcModelHint(provider,
                    provider + " 请求失败：余额不足或密钥无效（"
                            + (upstreamMessage != null ? upstreamMessage : reason) + ")",
                    upstreamMessage,
                    model);
        }
    
        // 未通过认证（401）
        if (status == HttpStatus.UNAUTHORIZED) {
            return appendVolcModelHint(provider,
                    provider + " 请求失败：未通过认证，请检查 API Key（"
                            + (upstreamMessage != null ? upstreamMessage : reason) + ")",
                    upstreamMessage,
                    model);
        }
    
        // 禁止访问（403）
        if (status == HttpStatus.FORBIDDEN) {
            return appendVolcModelHint(provider,
                    provider + " 请求失败：访问被拒绝（"
                            + (upstreamMessage != null ? upstreamMessage : reason) + ")",
                    upstreamMessage,
                    model);
        }
    
        // 其他错误
        String message = provider + " 请求失败：" + (upstreamMessage != null ? upstreamMessage : reason);
        return appendVolcModelHint(provider, message, upstreamMessage, model);
    }

    private String appendVolcModelHint(String provider, String message, String upstreamMessage, String model) {
        if ("火山方舟".equals(provider) && looksLikeVolcModelMappingError(upstreamMessage)) {
            message += "。当前上游 model/endpoint 为 `" + (model == null ? "" : model) + "`；"
                    + "请先在方舟控制台开通该模型服务，或在环境变量 "
                    + "VOLC_MODEL_DOUBAO_SEED_2_0_LITE / VOLC_MODEL_DOUBAO_SEED_2_0_MINI / VOLC_MODEL_DOUBAO_SEED_2_0_PRO "
                    + "中填你已经创建并可调用的模型 ID 或接入点 ID。";
        }
        return message;
    }

    private boolean looksLikeVolcModelMappingError(String message) {
        if (!StringUtils.hasText(message)) return false;
        String lower = message.toLowerCase();
        return (lower.contains("model") || lower.contains("endpoint"))
                && (lower.contains("does not exist")
                || lower.contains("do not have access")
                || lower.contains("not found")
                || lower.contains("no access")
                || lower.contains("not activated")
                || lower.contains("activate the model")
                || lower.contains("activated the model"));
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> parsed = OBJECT_MAPPER.readValue(body, Map.class);
            Object err = parsed.get("error");
            if (err instanceof Map<?,?> m) {
                Object msg = m.get("message");
                if (msg != null) {
                    return String.valueOf(msg);
                }
            }
        } catch (Exception ignored) {
        }
        return body.strip();
    }

    private String extractContentText(Object content) {
        if (content == null) return null;
        if (content instanceof String s) return s;
        if (content instanceof List<?> list) {
            for (Object it : list) {
                if (it instanceof Map<?,?> m) {
                    Object text = m.get("text");
                    if (text != null && !String.valueOf(text).isBlank()) {
                        return String.valueOf(text);
                    }
                    Object innerContent = m.get("content");
                    if (innerContent != null && !String.valueOf(innerContent).isBlank()) {
                        return String.valueOf(innerContent);
                    }
                }
            }
        }
        return null;
    }

    private Map<?,?> unwrapData(Map<String, Object> body) {
        if (body == null) return Collections.emptyMap();
        Object data = body.get("data");
        if (data instanceof Map<?,?> dm) {
            return dm;
        } else if (data instanceof List<?> dl && !dl.isEmpty()) {
            Object first = dl.get(0);
            if (first instanceof Map<?,?> fm) {
                return fm;
            }
        }
        return body;
    }

    private String extractFromMap(Map<?,?> map) {
        if (map == null) return null;
        // 先尝试标准 content/message 字段
        Object content = map.get("content");
        String resolved = extractContentText(content);
        if (resolved != null && !resolved.isBlank()) return resolved;

        Object message = map.get("message");
        if (message instanceof Map<?,?> mm) {
            resolved = extractContentText(mm.get("content"));
            if (resolved != null && !resolved.isBlank()) return resolved;
            Object reasoning = mm.get("reasoning_content");
            if (reasoning == null) {
                reasoning = mm.get("reasoningcontent");
            }
            resolved = extractContentText(reasoning);
            if (resolved != null && !resolved.isBlank()) return resolved;
        } else if (message != null && !(message instanceof Map)) {
            String msg = String.valueOf(message);
            if (!msg.isBlank()) return msg;
        }

        Object messages = map.get("messages");
        if (messages instanceof List<?> ml && !ml.isEmpty()) {
            for (Object obj : ml) {
                if (obj instanceof Map<?,?> mm) {
                    String msg = extractFromMap(mm);
                    if (msg != null && !msg.isBlank()) return msg;
                }
            }
        }

        // OpenAI/GLM choices 列表
        Object choices = map.get("choices");
        if (choices instanceof List<?> cl && !cl.isEmpty()) {
            for (Object obj : cl) {
                if (obj instanceof Map<?,?> cm) {
                    String msg = extractFromMap(cm);
                    if (msg != null && !msg.isBlank()) return msg;
                    Object delta = cm.get("delta");
                    if (delta instanceof Map<?,?> dm) {
                        msg = extractFromMap(dm);
                        if (msg != null && !msg.isBlank()) return msg;
                    }
                }
            }
        }

        // 常见文本字段：output_text/text/result
        for (String key : new String[] {"output_text", "text", "result", "output"}) {
            Object v = map.get(key);
            if (v instanceof Map<?,?> mv) {
                String nested = extractFromMap(mv);
                if (nested != null && !nested.isBlank()) return nested;
            } else if (v != null) {
                String s = String.valueOf(v);
                if (!s.isBlank()) return s;
            }
        }

        return null;
    }


    private String normalizeBaseUrl(String v) {
        if (v == null) return "";
        return v.endsWith("/") ? v.substring(0, v.length()-1) : v;
    }

    private String buildChatUrl(String baseUrl) {
        String normalized = normalizeBaseUrl(baseUrl);
        if (normalized.matches(".*/v\\d+")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private record ProxyTarget(String host, int port, String type) {}
    private record TransportPlan(String label, RestTemplate template) {}

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestBody {
        private String model;
        private List<Map<String, Object>> messages;
        private Boolean stream;
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        private Double temperature;
        private Map<String, Object> reasoning;
        @JsonProperty("response_format")
        private Map<String, Object> responseFormat;
    }
}
