package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程章节实体类
 * 对应数据库lessons表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "课程章节实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Schema(description = "章节ID", example = "1")
    private Long id;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节标题", example = "第一章 导论")
    private String title;

    @Schema(description = "章节描述", example = "本章节介绍课程的基本概念和学习目标")
    private String description;

    @Schema(description = "章节内容", example = "详细的章节内容文本")
    private String content;

    @Schema(description = "视频URL", example = "https://example.com/videos/lesson1.mp4")
    private String videoUrl;

    @Schema(description = "允许拖动进度条", example = "true")
    @Builder.Default
    private Boolean allowScrubbing = true;

    @Schema(description = "允许倍速播放", example = "true")
    @Builder.Default
    private Boolean allowSpeedChange = true;

    @Schema(description = "章节时长（分钟）", example = "45")
    private Integer duration;

    @Schema(description = "排序索引", example = "1")
    private Integer orderIndex;

    @Schema(description = "是否免费", example = "true")
    @Builder.Default
    private Boolean isFree = false;

    @Schema(description = "所属章节ID", example = "10")
    private Long chapterId;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "章节权重（用于课程加权进度）", example = "1.00")
    private Double weight;

    @Schema(description = "状态", example = "draft")
    private String status;

    @Schema(description = "是否删除", example = "false")
    @Builder.Default
    private Boolean deleted = false;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * 是否可以免费观看
     */
    public boolean isAccessible() {
        return isFree != null && isFree;
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 