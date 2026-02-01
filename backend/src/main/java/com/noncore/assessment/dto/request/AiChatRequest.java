package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotNull
    @NotEmpty
    private List<Message> messages;

    private Long courseId;

    private List<Long> studentIds;

    private String model;

    private String provider; // provider is derived by model

    private Long conversationId; // 可选：指定会话

    private java.util.List<Long> attachmentFileIds; // 可选：多模态附件（若模型不支持将忽略）

    // 选项：是否强制 JSON 对象输出（OpenAI response_format: json_object）
    private Boolean jsonOnly;

    // 选项：是否使用“作文批改”系统 Prompt（为 true 时使用系统 Prompt；为 false 时不注入）
    private Boolean useGradingPrompt;

    // 可选：覆盖系统 Prompt 文件路径（为空则使用配置中的默认路径）
    private String systemPromptPath;

    /**
     * 可选：AI 批改稳定化取样次数（用于批改接口）。
     * - 不用于普通聊天
     * - 为空时由后端接口使用默认值
     */
    private Integer samples;

    /**
     * 可选：触发第 3 次取样的分差阈值（基于 overall.final_score 的 0~5 标尺）。
     * - 仅在 samples>=2 时生效
     * - 为空时由后端接口使用默认值（当前推荐 0.8）
     */
    private Double diffThreshold;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @NotNull
        private String role; // user | assistant | system

        @NotNull
        @NotEmpty
        private String content;
    }
}

