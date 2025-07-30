package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "章节时长（分钟）", example = "45")
    private Integer duration;

    @Schema(description = "排序索引", example = "1")
    private Integer orderIndex;

    @Schema(description = "是否免费", example = "true")
    private Boolean isFree;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态", example = "draft")
    private String status;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 默认构造方法
     */
    public Lesson() {
        this.isFree = false;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public Lesson(Long courseId, String title, String description, Integer orderIndex) {
        this();
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

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

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", duration=" + duration +
                ", orderIndex=" + orderIndex +
                ", isFree=" + isFree +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", deleted=" + deleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 