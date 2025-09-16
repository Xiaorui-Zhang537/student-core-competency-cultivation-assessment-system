package com.noncore.assessment.service.llm;

import com.noncore.assessment.config.AiConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 负责按需读取系统 Prompt。每次调用时读取，保证热更新能力（例如替换文件后无需重启生效）。
 */
@Component
@RequiredArgsConstructor
public class PromptLoader {

    private final ResourceLoader resourceLoader;
    private final AiConfigProperties aiConfigProperties;

    /**
     * 读取系统 Prompt 文本。
     * 支持路径前缀：classpath: 或文件系统路径。
     * 若读取失败抛出运行时异常，由上层统一处理。
     */
    public String loadSystemPrompt() {
        String location = aiConfigProperties.getSystemPromptPath();
        if (location == null || location.isBlank()) {
            // 默认兜底，不返回空
            location = "classpath:/prompts/essay_evaluation_system_prompt.txt";
        }
        return readResource(location);
    }

    /**
     * 按覆盖路径读取系统 Prompt（若覆盖为空则使用默认路径）。
     */
    public String loadSystemPrompt(String overrideLocation) {
        if (overrideLocation == null || overrideLocation.isBlank()) {
            return loadSystemPrompt();
        }
        return readResource(overrideLocation);
    }

    private String readResource(String location) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("System prompt resource not found: " + location);
        }
        try (InputStream is = resource.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString().trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read system prompt: " + location, e);
        }
    }
}


