package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能力评估实体类
 * 对应数据库ability_assessments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力评估实体")
public class AbilityAssessment {

    @Schema(description = "评估ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "能力维度ID", example = "1")
    private Long dimensionId; // 能力维度ID

    @Schema(description = "能力ID", example = "1")
    private Long abilityId;

    // 关联字段
    @Schema(description = "能力维度名称", example = "编程能力")
    private String dimensionName; // 能力维度名称

    @Schema(description = "评估者ID", example = "1")
    private Long assessorId;

    @Schema(description = "评估类型", example = "assignment", allowableValues = {"assignment", "project", "exam", "peer", "self"})
    private String assessmentType;

    @Schema(description = "关联对象ID", example = "1")
    private Long relatedId;

    @Schema(description = "得分", example = "85.5")
    private BigDecimal score;

    @Schema(description = "满分", example = "100.0")
    private BigDecimal maxScore;

    @Schema(description = "能力等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced", "expert"})
    private String abilityLevel;

    @Schema(description = "置信度", example = "0.85")
    private BigDecimal confidence;

    @Schema(description = "评估依据", example = "基于作业表现和代码质量")
    private String evidence;

    @Schema(description = "改进建议", example = "需要加强异常处理和代码注释")
    private String improvement;

    @Schema(description = "评估状态", example = "completed", allowableValues = {"draft", "completed", "reviewed"})
    private String status;

    @Schema(description = "评估时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessedAt;

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
    public AbilityAssessment() {
        this.status = "draft";
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public AbilityAssessment(Long studentId, Long abilityId, Long assessorId, String assessmentType) {
        this();
        this.studentId = studentId;
        this.abilityId = abilityId;
        this.assessorId = assessorId;
        this.assessmentType = assessmentType;
    }

    /**
     * 计算百分比得分
     */
    public BigDecimal getPercentage() {
        if (score == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(maxScore, 4, java.math.RoundingMode.HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    /**
     * 完成评估
     */
    public void complete() {
        this.status = "completed";
        this.assessedAt = LocalDateTime.now();
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

    public Long getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(Long abilityId) {
        this.abilityId = abilityId;
    }

    public Long getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(Long dimensionId) {
        this.dimensionId = dimensionId;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public Long getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(Long assessorId) {
        this.assessorId = assessorId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
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

    public String getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(String abilityLevel) {
        this.abilityLevel = abilityLevel;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getImprovement() {
        return improvement;
    }

    public void setImprovement(String improvement) {
        this.improvement = improvement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
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
        return "AbilityAssessment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", abilityId=" + abilityId +
                ", assessmentType='" + assessmentType + '\'' +
                ", score=" + score +
                ", abilityLevel='" + abilityLevel + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} 