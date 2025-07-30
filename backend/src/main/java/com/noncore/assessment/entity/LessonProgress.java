package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
    private BigDecimal progress;

    @Schema(description = "是否完成", example = "false")
    private Boolean completed;

    @Schema(description = "学习时长（分钟）", example = "45")
    private Integer studyDuration;

    @Schema(description = "观看次数", example = "3")
    private Integer viewCount;

    @Schema(description = "最后观看位置（秒）", example = "1800")
    private Integer lastPosition;

    @Schema(description = "学习状态", example = "in_progress", allowableValues = {"not_started", "in_progress", "completed", "paused"})
    private String status;

    @Schema(description = "学习笔记", example = "这个章节讲的很详细，需要多复习几遍")
    private String notes;

    @Schema(description = "学习评分", example = "4")
    private Integer rating;

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
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 默认构造方法
     */
    public LessonProgress() {
        this.progress = BigDecimal.ZERO;
        this.completed = false;
        this.studyDuration = 0;
        this.viewCount = 0;
        this.lastPosition = 0;
        this.status = "not_started";
        this.rating = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public LessonProgress(Long studentId, Long courseId, Long lessonId) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
    }

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

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Integer studyDuration) {
        this.studyDuration = studyDuration;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(Integer lastPosition) {
        this.lastPosition = lastPosition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastStudiedAt() {
        return lastStudiedAt;
    }

    public void setLastStudiedAt(LocalDateTime lastStudiedAt) {
        this.lastStudiedAt = lastStudiedAt;
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

    // 别名方法，兼容LessonServiceImpl中的调用
    public Integer getStudyTime() {
        return getStudyDuration();
    }

    public void setStudyTime(Integer studyTime) {
        setStudyDuration(studyTime);
    }

    @Override
    public String toString() {
        return "LessonProgress{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", lessonId=" + lessonId +
                ", progress=" + progress +
                ", completed=" + completed +
                ", status='" + status + '\'' +
                ", studyDuration=" + studyDuration +
                ", lastStudiedAt=" + lastStudiedAt +
                '}';
    }
} 