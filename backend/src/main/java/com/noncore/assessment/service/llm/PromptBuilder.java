package com.noncore.assessment.service.llm;

import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final PromptLoader promptLoader;

    /**
     * 按需构建消息：可选择性注入系统 Prompt（若 systemPrompt 为 null/空，则不注入）。
     */
    public List<Message> buildMessages(List<Message> userMessages, String systemPrompt) {
        List<Message> built = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            built.add(new Message("system", systemPrompt));
        }
        int keep = Math.min(6, userMessages.size());
        built.addAll(userMessages.subList(userMessages.size() - keep, userMessages.size()));
        return built;
    }

    /**
     * 使用配置中的系统 Prompt 文件读取后注入。
     */
    public List<Message> buildWithDefaultSystemPrompt(List<Message> userMessages) {
        return buildMessages(userMessages, promptLoader.loadSystemPrompt());
    }
}

