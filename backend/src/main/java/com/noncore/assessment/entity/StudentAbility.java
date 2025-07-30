package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生能力记录实体类
 * 对应数据库student_abilities表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "学生能力记录实体")
public class StudentAbility {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "能力维度ID", example = "1")
    private Long dimensionId;

    @Schema(description = "当前得分", example = "85.50")
    private BigDecimal currentScore;

    @Schema(description = "能力等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced", "expert"})
    private String level;

    @Schema(description = "最后评估时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAssessmentAt;

    @Schema(description = "评估次数", example = "5")
    private Integer assessmentCount;

    @Schema(description = "发展趋势", example = "rising", allowableValues = {"rising", "stable", "declining"})
    private String trend;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 冗余字段，用于显示
    @Schema(description = "维度名称", example = "编程能力")
    private String dimensionName;

    @Schema(description = "维度分类", example = "technical")
    private String dimensionCategory;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    /**
     * 默认构造方法
     */
    public StudentAbility() {
        this.currentScore = BigDecimal.ZERO;
        this.level = "beginner";
        this.assessmentCount = 0;
        this.trend = "stable";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public StudentAbility(Long studentId, Long dimensionId) {
        this();
        this.studentId = studentId;
        this.dimensionId = dimensionId;
    }

    /**
     * 更新能力等级
     */
    public void updateLevel() {
        if (currentScore == null) {
            this.level = "beginner";
            return;
        }

        double score = currentScore.doubleValue();
        if (score >= 90) {
            this.level = "expert";
        } else if (score >= 75) {
            this.level = "advanced";
        } else if (score >= 60) {
            this.level = "intermediate";
        } else {
            this.level = "beginner";
        }
    }

    /**
     * 更新评估统计
     */
    public void updateAssessmentStats(BigDecimal newScore) {
        if (this.currentScore != null && newScore != null) {
            int comparison = newScore.compareTo(this.currentScore);
            if (comparison > 0) {
                this.trend = "rising";
            } else if (comparison < 0) {
                this.trend = "declining";
            } else {
                this.trend = "stable";
            }
        }

        this.currentScore = newScore;
        this.assessmentCount = (this.assessmentCount == null ? 0 : this.assessmentCount) + 1;
        this.lastAssessmentAt = LocalDateTime.now();
        updateLevel();
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

    public BigDecimal getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(BigDecimal currentScore) {
        this.currentScore = currentScore;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LocalDateTime getLastAssessmentAt() {
        return lastAssessmentAt;
    }

    public void setLastAssessmentAt(LocalDateTime lastAssessmentAt) {
        this.lastAssessmentAt = lastAssessmentAt;
    }

    public Integer getAssessmentCount() {
        return assessmentCount;
    }

    public void setAssessmentCount(Integer assessmentCount) {
        this.assessmentCount = assessmentCount;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
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

    public String getDimensionCategory() {
        return dimensionCategory;
    }

    public void setDimensionCategory(String dimensionCategory) {
        this.dimensionCategory = dimensionCategory;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return "StudentAbility{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", dimensionId=" + dimensionId +
                ", currentScore=" + currentScore +
                ", level='" + level + '\'' +
                ", trend='" + trend + '\'' +
                '}';
    }
} 