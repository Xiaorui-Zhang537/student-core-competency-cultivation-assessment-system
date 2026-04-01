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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
                    if (o instanceof Map<?, ?> mm) {
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
                            } catch (Exception ignored) {
                            }
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

        List<TransportPlan> plans = new ArrayList<>();
        if (alwaysProxy) {
            List<ProxyTarget> proxyTargets = buildProxyTargets(pc);
            if (proxyTargets.isEmpty()) {
                plans.add(new TransportPlan("direct", directRestTemplate));
            } else {
                for (ProxyTarget p : proxyTargets) {
                    plans.add(new TransportPlan("proxy " + p.host() + ":" + p.port() + " (" + p.type() + ")", buildRestTemplateForProxy(p)));
                }
            }
        } else {
            plans.add(new TransportPlan("direct", directRestTemplate));
            if (allowRetryWithProxy) {
                for (ProxyTarget p : buildProxyTargets(pc)) {
                    plans.add(new TransportPlan("proxy " + p.host() + ":" + p.port() + " (" + p.type() + ")", buildRestTemplateForProxy(p)));
                }
            }
        }

        RestClientException last = null;
        String lastPlan = plans.isEmpty() ? "direct" : plans.get(0).label();
        for (int i = 0; i < plans.size(); i++) {
            TransportPlan plan = plans.get(i);
            try {
                return executeGenerateWithRetries(plan, url, body, headers, attempts, baseBackoff, jitter, model, jsonOnly);
            } catch (RestClientException e) {
                last = e;
                lastPlan = plan.label();
                if (looksLikeNetworkIssue(e) && i < plans.size() - 1) {
                    log.warn("Gemini {} failed, switching to {} ... err={}",
                            plan.label(), plans.get(i + 1).label(), safeErr(e));
                    continue;
                }
                log.error("Gemini request error via {}", plan.label(), e);
                break;
            }
        }

        String msg = rootMessage(last);
        if (looksLikeNetworkIssue(last)) {
            msg = msg + "（网络/代理连接异常，请检查 AI_PROXY_HOST/AI_PROXY_PORT/AI_PROXY_TYPE，或确认本机代理已启动）";
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini请求异常(model=" + model + ", via=" + lastPlan + "): " + msg);
    }

    private String executeGenerateWithRetries(TransportPlan plan,
                                              String url,
                                              RequestBody body,
                                              HttpHeaders headers,
                                              int attempts,
                                              long baseBackoff,
                                              long jitter,
                                              String model,
                                              boolean jsonOnly) {
        for (int i = 1; i <= attempts; i++) {
            try {
                if (i == 1) {
                    log.info("Gemini generateContent {} (model={}, jsonOnly={})", plan.label(), model, jsonOnly);
                }
                HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) plan.template().exchange(url, HttpMethod.POST, entity, Map.class);
                if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                    log.error("Gemini non-2xx or empty body: status={}, body={}", resp.getStatusCode(), resp.getBody());
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Gemini错误: " + resp.getStatusCode());
                }
                Map<String, Object> bodyMap = resp.getBody();
                Object cands = bodyMap.get("candidates");
                if (cands instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> mm) {
                        Object content = mm.get("content");
                        if (content instanceof Map<?, ?> cm) {
                            Object parts = cm.get("parts");
                            if (parts instanceof List<?> pl && !pl.isEmpty()) {
                                Object p0 = pl.get(0);
                                if (p0 instanceof Map<?, ?> pm) {
                                    Object text = pm.get("text");
                                    if (text != null) return String.valueOf(text);
                                }
                            }
                        }
                    }
                }
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法解析大模型返回");
            } catch (RestClientException e) {
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

                // 判断是否 429/503，若是则指数退避重试
                int sc = extractStatusCode(e);
                boolean retryable = (sc == 429 || sc == 503);
                if (retryable && i < attempts) {
                    long sleep = (long) (baseBackoff * Math.pow(2, i - 1) + Math.random() * jitter);
                    log.warn("Gemini {} received (status={}), retrying in {} ms... (attempt {}/{})", sc, sc, sleep, i + 1, attempts);
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
                throw e;
            }
        }
        throw new RestClientException("Gemini request failed");
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

    private String normalizeBaseUrl(String v) { return (v == null) ? "" : (v.endsWith("/") ? v.substring(0, v.length()-1) : v); }

    private int extractStatusCode(RestClientException e) {
        try {
            if (e instanceof org.springframework.web.client.HttpStatusCodeException he) {
                return he.getStatusCode().value();
            }
        } catch (Throwable ignore) { }
        return -1;
    }

    private record ProxyTarget(String host, int port, String type) {}
    private record TransportPlan(String label, RestTemplate template) {}

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestBody {
        private List<Map<String, Object>> contents;
        @JsonProperty("generationConfig")
        private Map<String, Object> generationConfig;
    }
}
