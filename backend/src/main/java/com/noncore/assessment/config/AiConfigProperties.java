package com.noncore.assessment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfigProperties {

    private Providers providers = new Providers();
    private String defaultProvider = "openrouter";
    // 系统 Prompt 文件路径，支持 classpath: 或文件系统路径
    private String systemPromptPath = "classpath:/prompts/essay_evaluation_system_prompt.txt";

    private Deepseek deepseek = new Deepseek(); // backward-compatible default model
    private ProxyConfig proxy = new ProxyConfig();
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class Providers {
        private Provider openrouter = new Provider();
        private Provider deepseek = new Provider();
        private Provider google = new Provider();
    }

    @Data
    public static class Provider {
        private String baseUrl;
        private String apiKey;/**
        * OpenRouter 免费模型要求携带的来源站点，写入 HTTP-Referer。
        * 对其它兼容渠道无副作用。
        */
       private String referer;
       /**
        * OpenRouter 推荐的站点名称，写入 X-Title，便于通过免费通道校验。
        */
       private String title;
   }

    @Data
    public static class Deepseek {
        private String model = "z-ai/glm-4.5-air:free";
    }

    @Data
    public static class ProxyConfig {
        private boolean enabled = true;
        private boolean alwaysUseProxy = false;
        private boolean autoRetryWithProxy = true;
        private String host = "127.0.0.1";
        private int port = 7897;
        /** HTTP 或 SOCKS */
        private String type = "HTTP";
        private int connectTimeoutMs = 10000;
        private int readTimeoutMs = 60000;
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffMs = 800;
        private long jitterMs = 200;
    }
}

