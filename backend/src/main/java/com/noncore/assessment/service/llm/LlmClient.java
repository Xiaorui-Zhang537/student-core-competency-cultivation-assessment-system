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

import java.util.List;
import java.util.Map;
import com.noncore.assessment.config.AiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Slf4j
@Component
public class LlmClient {

    private final RestTemplate restTemplate;
    private final AiConfigProperties aiConfig;

    @Autowired
    public LlmClient(AiConfigProperties aiConfig) {
        this.aiConfig = aiConfig;
        this.restTemplate = buildRestTemplate(false);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RestTemplate buildRestTemplate(boolean useProxy) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
        factory.setConnectTimeout(pc.getConnectTimeoutMs());
        factory.setReadTimeout(pc.getReadTimeoutMs());
        if (useProxy || pc.isAlwaysUseProxy()) {
            Proxy proxy = new Proxy(
                    "SOCKS".equalsIgnoreCase(pc.getType()) ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
                    new InetSocketAddress(pc.getHost(), pc.getPort()));
            factory.setProxy(proxy);
        }
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
        String url = normalizeBaseUrl(baseUrl) + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        applyOpenRouterHeaders(headers, baseUrl);

        // 不启用 reasoning，确保广泛兼容
        RequestBody body = buildBody(payloadMessages, model, false, false);
        return doRequest(url, headers, body);
    }

    /**
     * JSON-only 变体：强制上游以 JSON 对象返回（OpenAI 兼容 response_format）。
     */
    public String createChatCompletionJsonOnly(List<Map<String, Object>> payloadMessages, String model, boolean stream, String baseUrl, String apiKey) {
        String url = normalizeBaseUrl(baseUrl) + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        applyOpenRouterHeaders(headers, baseUrl);

        RequestBody body = buildBody(payloadMessages, model, false, true);
        return doRequest(url, headers, body);
    }

    private RequestBody buildBody(List<Map<String, Object>> payloadMessages, String model, boolean includeReasoning, boolean jsonOnly) {
        RequestBody body = new RequestBody();
        body.setModel(model);
        body.setMessages(payloadMessages);
        body.setStream(false);
        body.setTemperature(0.2);
        body.setMaxTokens(1024);
        // 不附带 reasoning，避免通道不兼容
        if (jsonOnly) {
            body.setResponseFormat(java.util.Map.of("type", "json_object"));
        }
        return body;
    }

    private String doRequest(String url, HttpHeaders headers, RequestBody body) {
        try {
            HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.error("LLM non-2xx or empty body: status={}, body={}", resp.getStatusCode(), resp.getBody());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "LLM错误: " + resp.getStatusCode());
            }

            Map<String, Object> respBody = resp.getBody();
            Object choices = respBody != null ? respBody.get("choices") : null;
            if (choices instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?,?> m) {
                    Object message = m.get("message");
                    if (message instanceof Map<?,?> mm) {
                        Object content = mm.get("content");
                        if (content != null) return String.valueOf(content);
                    }
                }
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法解析大模型返回");
        } catch (RestClientException e) {
            log.error("LLM request error", e);
            if (e instanceof HttpStatusCodeException statusEx) {
                String message = buildFriendlyError(statusEx, url);
                // 对于 4xx 客户端错误（如 401/402），无需走代理重试，直接提示
                if (statusEx.getStatusCode().is4xxClientError()) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, message);
                }
            }
            AiConfigProperties.ProxyConfig pc = aiConfig.getProxy();
            if (pc.isEnabled() && pc.isAutoRetryWithProxy() && !pc.isAlwaysUseProxy()) {
                try {
                    log.warn("Retrying LLM request via proxy {}:{}", pc.getHost(), pc.getPort());
                    RestTemplate proxyRt = buildRestTemplate(true);
                    HttpEntity<RequestBody> entity = new HttpEntity<>(body, headers);
                    @SuppressWarnings("unchecked")
                    ResponseEntity<Map<String, Object>> resp = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) proxyRt.exchange(url, HttpMethod.POST, entity, Map.class);
                    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                        log.error("LLM (proxy) non-2xx or empty body: status={}, body={}", resp.getStatusCode(), resp.getBody());
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "LLM错误(代理): " + resp.getStatusCode());
                    }
                    Map<String, Object> respBody = resp.getBody();
                    Object choices = respBody != null ? respBody.get("choices") : null;
                    if (choices instanceof List<?> list && !list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof Map<?,?> m) {
                            Object message = m.get("message");
                            if (message instanceof Map<?,?> mm) {
                                Object content = mm.get("content");
                                if (content != null) return String.valueOf(content);
                            }
                        }
                    }
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法解析大模型返回(代理)");
                } catch (RestClientException e2) {
                    log.error("LLM proxy request error", e2);
                }
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "LLM请求异常: " + e.getMessage());
        }
    }

    private String buildFriendlyError(HttpStatusCodeException ex, String url) {
        String provider = url != null && url.contains("openrouter.ai") ? "OpenRouter" : "LLM";
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
            return provider + " 请求失败：余额不足或密钥无效（" 
                + (upstreamMessage != null ? upstreamMessage : reason) + ")";
        }
    
        // 未通过认证（401）
        if (status == HttpStatus.UNAUTHORIZED) {
            return provider + " 请求失败：未通过认证，请检查 API Key（" 
                + (upstreamMessage != null ? upstreamMessage : reason) + ")";
        }
    
        // 禁止访问（403）
        if (status == HttpStatus.FORBIDDEN) {
            return provider + " 请求失败：访问被拒绝（" 
                + (upstreamMessage != null ? upstreamMessage : reason) + ")";
        }
    
        // 其他错误
        return provider + " 请求失败：" + (upstreamMessage != null ? upstreamMessage : reason);
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


    private String normalizeBaseUrl(String v) {
        if (v == null) return "";
        return v.endsWith("/") ? v.substring(0, v.length()-1) : v;
    }

    private boolean isOpenRouter(String baseUrl) {
        return baseUrl != null && baseUrl.contains("openrouter.ai");
    }

    private void applyOpenRouterHeaders(HttpHeaders headers, String baseUrl) {
        // OpenRouter: 显式声明不采集数据，避免命中“Paid model training”隐私策略导致 404
        if (!isOpenRouter(baseUrl)) return;

        headers.set("X-OpenAI-Data-Collection-Opt-Out", "true");

        AiConfigProperties.Provider orProvider = aiConfig.getProviders().getOpenrouter();
        if (orProvider != null) {
            if (orProvider.getReferer() != null && !orProvider.getReferer().isBlank()) {
                headers.set("HTTP-Referer", orProvider.getReferer());
            }
            if (orProvider.getTitle() != null && !orProvider.getTitle().isBlank()) {
                headers.set("X-Title", orProvider.getTitle());
            }
        }
    }

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