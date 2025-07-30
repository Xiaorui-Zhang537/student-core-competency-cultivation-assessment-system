package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 能力发展目标实体类
 * 对应数据库ability_goals表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力发展目标实体")
public class AbilityGoal {

    @Schema(description = "目标ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "能力维度ID", example = "1")
    private Long dimensionId;

    @Schema(description = "目标标题", example = "提升Java编程能力")
    private String title;

    @Schema(description = "目标描述", example = "在下个月内将Java编程能力提升到80分以上")
    private String description;

    @Schema(description = "目标分数", example = "80.00")
    private BigDecimal targetScore;

    @Schema(description = "当前分数", example = "65.00")
    private BigDecimal currentScore;

    @Schema(description = "目标达成日期", example = "2024-01-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @Schema(description = "优先级", example = "medium", allowableValues = {"low", "medium", "high"})
    private String priority;

    @Schema(description = "目标状态", example = "active", allowableValues = {"active", "achieved", "paused", "cancelled"})
    private String status;

    @Schema(description = "达成时间", example = "2024-01-25 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime achievedAt;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 冗余字段，用于显示
    @Schema(description = "维度名称", example = "编程能力")
    private String dimensionName;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    /**
     * 默认构造方法
     */
    public AbilityGoal() {
        this.currentScore = BigDecimal.ZERO;
        this.priority = "medium";
        this.status = "active";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public AbilityGoal(Long studentId, Long dimensionId, String title, BigDecimal targetScore, LocalDate targetDate) {
        this();
        this.studentId = studentId;
        this.dimensionId = dimensionId;
        this.title = title;
        this.targetScore = targetScore;
        this.targetDate = targetDate;
    }

    /**
     * 计算进度百分比
     */
    public BigDecimal getProgress() {
        if (currentScore == null || targetScore == null || targetScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal progress = currentScore.divide(targetScore, 4, java.math.RoundingMode.HALF_UP)
                                         .multiply(BigDecimal.valueOf(100));
        return progress.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(100) : progress;
    }

    /**
     * 检查是否已达成
     */
    public boolean isAchieved() {
        return "achieved".equals(this.status) || 
               (currentScore != null && targetScore != null && currentScore.compareTo(targetScore) >= 0);
    }

    /**
     * 标记为已达成
     */
    public void markAsAchieved() {
        this.status = "achieved";
        this.achievedAt = LocalDateTime.now();
        updateTimestamp();
    }

    /**
     * 更新当前分数
     */
    public void updateCurrentScore(BigDecimal newScore) {
        this.currentScore = newScore;
        if (isAchieved() && !"achieved".equals(this.status)) {
            markAsAchieved();
        }
        updateTimestamp();
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

    public Long getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(Long dimensionId) {
        this.dimensionId = dimensionId;
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

    public BigDecimal getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(BigDecimal targetScore) {
        this.targetScore = targetScore;
    }

    public BigDecimal getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(BigDecimal currentScore) {
        this.currentScore = currentScore;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
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

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return "AbilityGoal{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", title='" + title + '\'' +
                ", targetScore=" + targetScore +
                ", currentScore=" + currentScore +
                ", status='" + status + '\'' +
                '}';
    }
} 