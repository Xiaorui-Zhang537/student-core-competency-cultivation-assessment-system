package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "课程章节（大章）")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Schema(description = "章节ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "章节标题")
    private String title;

    @Schema(description = "章节简介")
    private String description;

    @Schema(description = "排序索引")
    private Integer orderIndex;

    @Schema(description = "是否删除")
    @Builder.Default
    private Boolean deleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}


