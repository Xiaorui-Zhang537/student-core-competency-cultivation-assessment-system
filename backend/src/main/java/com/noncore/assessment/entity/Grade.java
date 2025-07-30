package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 * 对应数据库grades表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "成绩实体")
public class Grade {

    @Schema(description = "成绩ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "作业ID", example = "1")
    private Long assignmentId;

    @Schema(description = "提交ID", example = "1")
    private Long submissionId;

    @Schema(description = "评分人ID", example = "1")
    private Long graderId;

    @Schema(description = "得分", example = "85.5")
    private BigDecimal score;

    @Schema(description = "满分", example = "100.0")
    private BigDecimal maxScore;

    @Schema(description = "百分比得分", example = "85.5")
    private BigDecimal percentage;

    @Schema(description = "等级", example = "A")
    private String gradeLevel;

    @Schema(description = "评分反馈", example = "作业完成质量很好，逻辑清晰，但有几个小错误需要注意")
    private String feedback;

    @Schema(description = "成绩状态", example = "published", allowableValues = {"draft", "published", "returned"})
    private String status;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    /**
     * 默认构造方法
     */
    public Grade() {
        this.status = "draft";
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public Grade(Long studentId, Long assignmentId, BigDecimal score, BigDecimal maxScore) {
        this();
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.score = score;
        this.maxScore = maxScore;
    }

    /**
     * 计算百分比得分
     */
    public BigDecimal calculatePercentage() {
        if (score == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(maxScore, 4, RoundingMode.HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    /**
     * 获取等级
     */
    public String calculateGradeLevel() {
        BigDecimal percentage = calculatePercentage();
        return getString(percentage);
    }

    public static String getString(BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("90")) >= 0) return "A";
        if (percentage.compareTo(new BigDecimal("80")) >= 0) return "B";
        if (percentage.compareTo(new BigDecimal("70")) >= 0) return "C";
        if (percentage.compareTo(new BigDecimal("60")) >= 0) return "D";
        return "F";
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return "published".equals(status);
    }

    /**
     * 发布成绩
     */
    public void publish() {
        this.status = "published";
        // this.gradedAt = LocalDateTime.now(); // This line was removed from the new_code, so it's removed here.
        updateTimestamp();
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 判断是否及格
     */
    public boolean isPassed() {
        return calculatePercentage().compareTo(new BigDecimal("60")) >= 0;
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

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getGraderId() {
        return graderId;
    }

    public void setGraderId(Long graderId) {
        this.graderId = graderId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", assignmentId=" + assignmentId +
                ", submissionId=" + submissionId +
                ", graderId=" + graderId +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", percentage=" + percentage +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", feedback='" + feedback + '\'' +
                ", status='" + status + '\'' +
                ", deleted=" + deleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 