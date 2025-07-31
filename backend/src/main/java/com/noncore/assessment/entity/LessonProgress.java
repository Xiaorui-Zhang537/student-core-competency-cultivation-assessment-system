package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程学习进度实体类
 * 对应数据库lesson_progress表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "课程学习进度实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {

    @Schema(description = "进度ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Schema(description = "章节ID", example = "1")
    private Long lessonId;

    @Schema(description = "学习进度（百分比）", example = "75.5")
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;

    @Schema(description = "是否完成", example = "false")
    @Builder.Default
    private Boolean completed = false;

    @Schema(description = "学习时长（分钟）", example = "45")
    @Builder.Default
    private Integer studyDuration = 0;

    @Schema(description = "观看次数", example = "3")
    @Builder.Default
    private Integer viewCount = 0;

    @Schema(description = "最后观看位置（秒）", example = "1800")
    @Builder.Default
    private Integer lastPosition = 0;

    @Schema(description = "学习状态", example = "in_progress", allowableValues = {"not_started", "in_progress", "completed", "paused"})
    @Builder.Default
    private String status = "not_started";

    @Schema(description = "学习笔记", example = "这个章节讲的很详细，需要多复习几遍")
    private String notes;

    @Schema(description = "学习评分", example = "4")
    @Builder.Default
    private Integer rating = 0;

    @Schema(description = "开始学习时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @Schema(description = "完成时间", example = "2024-12-28 11:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    @Schema(description = "最后学习时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastStudiedAt;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 开始学习
     */
    public void startLearning() {
        if ("not_started".equals(this.status)) {
            this.status = "in_progress";
            this.startedAt = LocalDateTime.now();
        }
        this.lastStudiedAt = LocalDateTime.now();
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
        updateTimestamp();
    }

    /**
     * 更新学习进度
     */
    public void updateProgress(BigDecimal newProgress, Integer currentPosition) {
        this.progress = newProgress;
        this.lastPosition = currentPosition;
        this.lastStudiedAt = LocalDateTime.now();
        
        // 如果进度达到100%，自动标记为完成
        if (newProgress.compareTo(new BigDecimal("100")) >= 0 && !this.completed) {
            markAsCompleted();
        } else if (!"completed".equals(this.status)) {
            this.status = "in_progress";
        }
        
        updateTimestamp();
    }

    /**
     * 标记为完成
     */
    public void markAsCompleted() {
        this.completed = true;
        this.status = "completed";
        this.progress = new BigDecimal("100");
        this.completedAt = LocalDateTime.now();
        this.lastStudiedAt = LocalDateTime.now();
        updateTimestamp();
    }

    /**
     * 暂停学习
     */
    public void pauseLearning() {
        if ("in_progress".equals(this.status)) {
            this.status = "paused";
            updateTimestamp();
        }
    }

    /**
     * 重置进度
     */
    public void resetProgress() {
        this.progress = BigDecimal.ZERO;
        this.completed = false;
        this.studyDuration = 0;
        this.lastPosition = 0;
        this.status = "not_started";
        this.completedAt = null;
        this.lastStudiedAt = null;
        updateTimestamp();
    }

    /**
     * 添加学习时长
     */
    public void addStudyTime(Integer minutes) {
        this.studyDuration = (this.studyDuration == null ? 0 : this.studyDuration) + minutes;
        this.lastStudiedAt = LocalDateTime.now();
        updateTimestamp();
    }

    /**
     * 设置学习评分
     */
    public void setLearningRating(Integer rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
            updateTimestamp();
        }
    }

    /**
     * 是否已开始学习
     */
    public boolean isStarted() {
        return !"not_started".equals(status);
    }

    /**
     * 是否正在学习
     */
    public boolean isInProgress() {
        return "in_progress".equals(status);
    }

    /**
     * 获取进度百分比
     */
    public BigDecimal getProgressPercentage() {
        return progress != null ? progress : BigDecimal.ZERO;
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // 别名方法，兼容LessonServiceImpl中的调用
    public Integer getStudyTime() {
        return getStudyDuration();
    }

    public void setStudyTime(Integer studyTime) {
        setStudyDuration(studyTime);
    }
} 