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
import org.springframework.web.client.HttpStatusCodeException;

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

    private final RestTemplate directRestTemplate;
    private final AiConfigProperties aiConfig;

    @Autowired
    public GeminiClient(AiConfigProperties aiConfig) {
        this.aiConfig = aiConfig;
        this.directRestTemplate = buildRestTemplate(false);
    }

    private RestTemplate buildRestTemplate(boolean useProxy) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        factory.setConnectTimeout(pc.getConnectTimeoutMs());
        factory.setReadTimeout(pc.getReadTimeoutMs());
        if (useProxy && pc != null && pc.isEnabled()) {
            Proxy proxy = new Proxy(
                "SOCKS".equalsIgnoreCase(pc.getType()) ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
                new InetSocketAddress(pc.getHost(), pc.getPort())
            );
            factory.setProxy(proxy);
        }
        return new RestTemplate(factory);
    }

    private RestTemplate buildProxyRestTemplate() {
        return buildRestTemplate(true);
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
                        } else if ("image_url".equals(type)) {
                            // OpenAI 兼容结构：{ type:'image_url', image_url:{ url:'data:image/png;base64,...' } }
                            try {
                                Object img = mm.get("image_url");
                                String url = null;
                                if (img instanceof Map<?, ?> im) {
                                    Object u = im.get("url");
                                    if (u != null) url = String.valueOf(u);
                                }
                                InlineData id = parseInlineDataFromUrl(url);
                                if (id != null && id.mimeType != null && id.dataBase64 != null) {
                                    // Gemini proto JSON 使用 camelCase：inlineData/mimeType
                                    parts.add(Map.of(
                                            "inlineData", Map.of(
                                                    "mimeType", id.mimeType,
                                                    "data", id.dataBase64
                                            )
                                    ));
                                }
                            } catch (Exception ignored) { }
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

        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        boolean proxyEnabled = pc != null && pc.isEnabled();
        boolean alwaysProxy = proxyEnabled && pc.isAlwaysUseProxy();
        boolean allowRetryWithProxy = proxyEnabled && pc.isAutoRetryWithProxy() && !alwaysProxy;

        // 默认直连；仅在 alwaysUseProxy=true 时强制走代理；否则直连失败后再切代理重试一次
        RestTemplate rt = alwaysProxy ? buildProxyRestTemplate() : directRestTemplate;
        boolean usingProxy = alwaysProxy;
        boolean switchedToProxy = alwaysProxy;

        int maxAttempts = attempts + (allowRetryWithProxy ? 1 : 0);

        RestClientException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                if (i == 1) {
                    if (usingProxy) {
                        log.info("Gemini generateContent via proxy {}:{} (type={}, model={}, jsonOnly={})",
                            pc.getHost(), pc.getPort(), pc.getType(), model, jsonOnly);
                    } else {
                        log.info("Gemini generateContent direct (model={}, jsonOnly={})", model, jsonOnly);
                    }
                }
                HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) rt.exchange(url, HttpMethod.POST, entity, Map.class);
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

                // 认证/权限/配额等错误：把 Google 的返回原因透出（便于定位“Key 限制/未启用 API/无权限”）
                if (e instanceof HttpStatusCodeException he) {
                    String raw = he.getResponseBodyAsString();
                    String brief = raw == null ? "" : raw.trim();
                    if (brief.length() > 800) {
                        brief = brief.substring(0, 800) + "...(truncated)";
                    }
                    int sc = he.getStatusCode().value();
                    log.warn("Gemini http error status={}, body={}", sc, brief);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Gemini HTTP " + sc + (brief.isBlank() ? "" : (": " + brief)));
                }

                // 直连失败时（网络/握手类错误）自动切代理重试一次
                if (!switchedToProxy && allowRetryWithProxy && looksLikeNetworkIssue(e)) {
                    switchedToProxy = true;
                    usingProxy = true;
                    rt = buildProxyRestTemplate();
                    log.warn("Gemini direct failed, retrying with proxy {}:{} (type={}) ... err={}",
                        pc.getHost(), pc.getPort(), pc.getType(), safeErr(e));
                    continue;
                }

                // 判断是否 429/503，若是则指数退避重试
                int sc = extractStatusCode(e);
                boolean retryable = (sc == 429 || sc == 503);
                if (retryable && i < maxAttempts) {
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

    private static class InlineData {
        final String mimeType;
        final String dataBase64;
        InlineData(String mimeType, String dataBase64) {
            this.mimeType = mimeType;
            this.dataBase64 = dataBase64;
        }
    }

    private InlineData parseInlineDataFromUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.isEmpty()) return null;
        // 仅支持 data URL（后端会内联，避免外网不可达与鉴权问题）
        // 形如：data:image/png;base64,AAAA...
        if (!u.startsWith("data:")) return null;
        int semi = u.indexOf(";base64,");
        if (semi <= "data:".length()) return null;
        String mime = u.substring("data:".length(), semi).trim();
        String b64 = u.substring(semi + ";base64,".length()).trim();
        if (mime.isEmpty() || b64.isEmpty()) return null;
        return new InlineData(mime, b64);
    }

    private String mapRole(String role) {
        // Gemini 采用 user/model 两类角色，system 可并入 user（由 PromptBuilder 注入在最前）
        if ("assistant".equals(role)) return "model";
        return "user";
    }

    private boolean looksLikeNetworkIssue(RestClientException e) {
        Throwable t = e;
        // unwrap common wrappers
        for (int i = 0; i < 4 && t != null; i++) {
            if (t instanceof org.springframework.web.client.ResourceAccessException rae && rae.getCause() != null) {
                t = rae.getCause();
                continue;
            }
            if (t.getCause() == null) break;
            t = t.getCause();
        }
        if (t == null) return false;
        return (t instanceof java.net.ConnectException)
            || (t instanceof java.net.UnknownHostException)
            || (t instanceof java.net.SocketTimeoutException)
            || (t instanceof javax.net.ssl.SSLHandshakeException)
            || (t instanceof java.io.EOFException);
    }

    private String safeErr(Exception e) {
        try {
            return e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
        } catch (Exception ignore) {
            return "unknown";
        }
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


