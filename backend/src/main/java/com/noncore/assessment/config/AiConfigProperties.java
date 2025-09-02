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

    private Deepseek deepseek = new Deepseek(); // backward-compatible default model

    @Data
    public static class Providers {
        private Provider openrouter = new Provider();
        private Provider deepseek = new Provider();
    }

    @Data
    public static class Provider {
        private String baseUrl;
        private String apiKey;
    }

    @Data
    public static class Deepseek {
        private String model = "deepseek/deepseek-chat-v3.1";
    }
}

