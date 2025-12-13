package com.noncore.assessment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfigProperties {

    private Providers providers = new Providers();
    private String defaultProvider = "google";
    // 系统 Prompt 文件路径，支持 classpath: 或文件系统路径
    private String systemPromptPath = "classpath:/prompts/essay_evaluation_system_prompt.txt";

    private Deepseek deepseek = new Deepseek(); // backward-compatible default model
    private ProxyConfig proxy = new ProxyConfig();
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class Providers {
        private Provider glm = new Provider();
        private Provider deepseek = new Provider();
        private Provider google = new Provider();
    }

    @Data
    public static class Provider {
        private String baseUrl;
        private String apiKey;
    }

    @Data
    public static class Deepseek {
        private String model = "google/gemini-2.5-pro";
    }

    @Data
    public static class ProxyConfig {
        private boolean enabled = true;
        private boolean alwaysUseProxy = true;
        private String host = "127.0.0.1";
        private int port = 7890;
        /** HTTP 或 SOCKS */
        private String type = "HTTP";
        private int connectTimeoutMs = 10000;
        private int readTimeoutMs = 60000;
         // 显式提供布尔 getter，避免缺少 Lombok 注解处理时出现编译错误
         public boolean isEnabled() { return enabled; }
         public boolean isAlwaysUseProxy() { return alwaysUseProxy; }
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffMs = 800;
        private long jitterMs = 200;
    }
}

