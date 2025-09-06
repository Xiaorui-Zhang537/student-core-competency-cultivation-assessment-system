package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "帮助文章反馈/投票")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpArticleFeedback {
    private Long id;
    private Long articleId;
    private Long userId;
    private Boolean helpful;
    private String content;
    private LocalDateTime createdAt;
}


