package com.noncore.assessment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfigProperties {

    private Providers providers = new Providers();
    private String defaultProvider = "volc";
    private String defaultModel = "volc/doubao-seed-2.0-lite";
    private String gradingModel = "volc/doubao-seed-2.0-pro";
    // 系统 Prompt 文件路径，支持 classpath: 或文件系统路径
    private String systemPromptPath = "classpath:/prompts/essay_evaluation_system_prompt.txt";

    private Deepseek deepseek = new Deepseek(); // backward-compatible default model
    private ModelMappings models = new ModelMappings();
    private VoiceConfig voice = new VoiceConfig();
    private ProxyConfig proxy = new ProxyConfig();
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class Providers {
        private Provider glm = new Provider();
        private Provider deepseek = new Provider();
        private Provider google = new Provider();
        private Provider volc = new Provider();
    }

    @Data
    public static class Provider {
        private String baseUrl;
        private String apiKey;
    }

    @Data
    public static class Deepseek {
        private String model = "volc/doubao-seed-2.0-lite";
    }

    @Data
    public static class ModelMappings {
        private String doubaoSeed20Lite = "doubao-seed-2-0-lite-260215";
        private String doubaoSeed20Mini = "doubao-seed-2-0-mini-260215";
        private String doubaoSeed20Pro = "doubao-seed-2-0-pro-260215";
    }

    @Data
    public static class VoiceConfig {
        private String defaultModel = "volc/voice-realtime";
        private String appId;
        private String appKey;
        private String apiKey;
        private String resourceId = "volc.speech.dialog";
        private String wsBaseUrl = "wss://ai-gateway.vei.volces.com/v1/realtime";
        private String voiceType = "zh_female_tianmeiyueyue_moon_bigtts";
        private int maxTokens = 1024;
        private double temperature = 0.15;
        private double topP = 0.3;
    }

    @Data
    public static class ProxyConfig {
        private boolean enabled = true;
        /**
         * 是否强制所有 AI 出站请求都走代理。
         * <p>
         * 说明：若为 false，则默认直连；当启用 {@link #autoRetryWithProxy} 时，直连失败会自动切代理重试一次。
         */
        private boolean alwaysUseProxy = false;
        /**
         * 直连失败时是否自动重试代理（用于国内/受限网络环境）。
         * 对应配置：ai.proxy.auto-retry-with-proxy
         */
        private boolean autoRetryWithProxy = true;
        private String host = "127.0.0.1";
        private int port = 7890;
        /** HTTP 或 SOCKS */
        private String type = "HTTP";
        private int connectTimeoutMs = 10000;
        private int readTimeoutMs = 300000;
         // 显式提供布尔 getter，避免缺少 Lombok 注解处理时出现编译错误
         public boolean isEnabled() { return enabled; }
         public boolean isAlwaysUseProxy() { return alwaysUseProxy; }
         public boolean isAutoRetryWithProxy() { return autoRetryWithProxy; }
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffMs = 800;
        private long jitterMs = 200;
    }
}
