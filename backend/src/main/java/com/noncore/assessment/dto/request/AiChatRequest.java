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

