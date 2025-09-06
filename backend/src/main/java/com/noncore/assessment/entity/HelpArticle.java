package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帮助中心文章实体
 */
@Schema(description = "帮助中心文章")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpArticle {
    private Long id;
    private Long categoryId;
    private String title;
    private String slug;
    private String contentMd;
    private String contentHtml;
    private String tags;
    private Integer views;
    private Integer upVotes;
    private Integer downVotes;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


