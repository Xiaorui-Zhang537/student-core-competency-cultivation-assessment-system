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

    private String provider; // openrouter | deepseek (official)

    private Long conversationId; // 可选：指定会话

    private java.util.List<Long> attachmentFileIds; // 可选：多模态附件（若模型不支持将忽略）

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

