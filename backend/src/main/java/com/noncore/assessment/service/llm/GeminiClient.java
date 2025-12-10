package com.noncore.assessment.service.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import com.noncore.assessment.config.AiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Google Gemini 直连客户端（v1beta, responses:generate）
 * 说明：Gemini 与 OpenAI Chat Completions 不同，这里做最小可用适配（文本-only）。
 */
@Slf4j
@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final AiConfigProperties aiConfig;

    @Autowired
    public GeminiClient(AiConfigProperties aiConfig) {
        this.aiConfig = aiConfig;
        this.restTemplate = buildRestTemplate();
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        factory.setConnectTimeout(pc.getConnectTimeoutMs());
        factory.setReadTimeout(pc.getReadTimeoutMs());
        Proxy proxy = new Proxy(
            "SOCKS".equalsIgnoreCase(pc.getType()) ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
            new InetSocketAddress("127.0.0.1", 7890));
        factory.setProxy(proxy);
        return new RestTemplate(factory);
    }

        /**
     * 兼容旧调用签名，参数不再生效（Gemini 永远使用固定代理）。
     */
        private RestTemplate buildRestTemplate(boolean ignoredAlwaysUseProxy) {
            return buildRestTemplate();
        }

    public String generate(List<Map<String, Object>> partsMessages, String model, boolean jsonOnly, String baseUrl, String apiKey) {
        // 将 OpenAI 风格的 messages（数组，包含 {type:'text', text:'...'}） 转为 Gemini 的 contents 结构
        // 约定：partsMessages 的每一项为 { role, content }；content 为字符串或数组（text parts）
        var contents = new java.util.ArrayList<Map<String, Object>>();
        for (Map<String, Object> m : partsMessages) {
            String role = String.valueOf(m.get("role"));
            Object content = m.get("content");
            var parts = new java.util.ArrayList<Map<String, Object>>();
            if (content instanceof String s) {
                parts.add(Map.of("text", s));
            } else if (content instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?,?> mm) {
                        String type = String.valueOf(mm.get("type"));
                        if ("text".equals(type)) {
                            parts.add(Map.of("text", String.valueOf(mm.get("text"))));
                        }
                    }
                }
            }
            contents.add(Map.of(
                    "role", mapRole(role),
                    "parts", parts
            ));
        }

        String url = normalizeBaseUrl(baseUrl) + "/v1beta/models/" + model + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        RequestBody body = new RequestBody();
        body.setContents(contents);
        if (jsonOnly) {
            // Gemini JSON 模式：使用 responseMimeType 指定 JSON
            body.setGenerationConfig(Map.of("responseMimeType", "application/json"));
        }

        AiConfigProperties.RetryConfig rc = aiConfig.getRetry();
        int attempts = Math.max(1, rc.getMaxAttempts());
        long baseBackoff = Math.max(0, rc.getBackoffMs());
        long jitter = Math.max(0, rc.getJitterMs());

        boolean preferProxy = shouldUseProxy(baseUrl);
        RestTemplate primaryRt = preferProxy ? buildRestTemplate(true) : restTemplate;

        RestClientException last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) primaryRt.exchange(url, HttpMethod.POST, entity, Map.class);
                if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                    log.error("Gemini non-2xx or empty body: status={}, body={}", resp.getStatusCode(), resp.getBody());
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini错误: " + resp.getStatusCode());
                }
                Map<String, Object> bodyMap = resp.getBody();
                Object cands = bodyMap.get("candidates");
                if (cands instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?,?> mm) {
                        Object content = mm.get("content");
                        if (content instanceof Map<?,?> cm) {
                            Object parts = cm.get("parts");
                            if (parts instanceof List<?> pl && !pl.isEmpty()) {
                                Object p0 = pl.get(0);
                                if (p0 instanceof Map<?,?> pm) {
                                    Object text = pm.get("text");
                                    if (text != null) return String.valueOf(text);
                                }
                            }
                        }
                    }
                }
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法解析大模型返回");
            } catch (RestClientException e) {
                last = e;
                // 判断是否 429/503，若是则指数退避重试
                int sc = extractStatusCode(e);
                boolean retryable = (sc == 429 || sc == 503);
                if (retryable && i < attempts) {
                    long sleep = (long) (baseBackoff * Math.pow(2, i - 1) + Math.random() * jitter);
                    log.warn("Gemini {} received (status={}), retrying in {} ms... (attempt {}/{})", sc, sc, sleep, i + 1, attempts);
                    try { Thread.sleep(sleep); } catch (InterruptedException ignored) { }
                    continue;
                }

                log.error("Gemini request error", e);

                break;
            }
        }
        String msg = last != null ? last.getMessage() : "unknown";
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini请求异常(model=" + model + "): " + msg);
    }

    private String mapRole(String role) {
        // Gemini 采用 user/model 两类角色，system 可并入 user（由 PromptBuilder 注入在最前）
        if ("assistant".equals(role)) return "model";
        return "user";
    }

    private boolean shouldUseProxy(String baseUrl) {
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        if (!pc.isEnabled()) {
            return false;
        }
        if (pc.isAlwaysUseProxy()) {
            return true;
        }
        return baseUrl != null && baseUrl.contains("googleapis.com");
    }

    private String normalizeBaseUrl(String v) { return (v == null) ? "" : (v.endsWith("/") ? v.substring(0, v.length()-1) : v); }

    private int extractStatusCode(RestClientException e) {
        try {
            if (e instanceof org.springframework.web.client.HttpStatusCodeException he) {
                return he.getStatusCode().value();
            }
        } catch (Throwable ignore) { }
        return -1;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestBody {
        private List<Map<String, Object>> contents;
        @JsonProperty("generationConfig")
        private Map<String, Object> generationConfig;
    }
}


