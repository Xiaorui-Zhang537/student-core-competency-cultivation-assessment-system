package com.noncore.assessment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

/**
 * AI 配置启动诊断（安全：不输出任何 API Key 明文）。
 * <p>
 * 用途：快速确认“后端进程是否读到了环境变量/配置文件”，避免在 macOS/IDE 启动时环境变量不继承导致的误判。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiStartupDiagnostics {

    private final AiConfigProperties aiConfigProperties;
    private final Environment environment;

    @PostConstruct
    public void logAiConfigSnapshot() {
        try {
            String[] profiles = environment.getActiveProfiles();

            AiConfigProperties.Provider google = aiConfigProperties.getProviders() != null
                    ? aiConfigProperties.getProviders().getGoogle()
                    : null;
            String googleBaseUrl = google != null ? google.getBaseUrl() : null;
            String googleKey = google != null ? google.getApiKey() : null;
            boolean googleKeyPresent = StringUtils.hasText(googleKey);

            AiConfigProperties.ProxyConfig proxy = aiConfigProperties.getProxy();
            boolean proxyEnabled = proxy != null && proxy.isEnabled();
            boolean alwaysProxy = proxyEnabled && proxy.isAlwaysUseProxy();
            boolean retryWithProxy = proxyEnabled && proxy.isAutoRetryWithProxy();

            log.info("[AI_DIAG] profiles={}, google.baseUrl={}, google.apiKeyPresent={}, proxy.enabled={}, proxy.type={}, proxy.host={}, proxy.port={}, proxy.alwaysUseProxy={}, proxy.autoRetryWithProxy={}",
                    Arrays.toString(profiles),
                    googleBaseUrl,
                    googleKeyPresent,
                    proxyEnabled,
                    proxy != null ? proxy.getType() : null,
                    proxy != null ? proxy.getHost() : null,
                    proxy != null ? proxy.getPort() : null,
                    alwaysProxy,
                    retryWithProxy
            );
        } catch (Exception e) {
            log.warn("[AI_DIAG] failed to print ai config snapshot: {}", e.getMessage());
        }
    }
}

