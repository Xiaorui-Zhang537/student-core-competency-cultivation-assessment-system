package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学习建议实体类
 * 对应数据库learning_recommendations表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "学习建议实体")
public class LearningRecommendation {

    @Schema(description = "建议ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "能力维度ID", example = "1")
    private Long dimensionId;

    @Schema(description = "建议标题", example = "Java基础语法练习")
    private String title;

    @Schema(description = "建议内容", example = "建议您通过编写小程序来加强Java基础语法的掌握")
    private String description;

    @Schema(description = "建议类型", example = "course", allowableValues = {"course", "resource", "practice", "project"})
    private String recommendationType;

    @Schema(description = "资源链接", example = "https://example.com/java-tutorial")
    private String resourceUrl;

    @Schema(description = "难度等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced"})
    private String difficultyLevel;

    @Schema(description = "预估学习时间", example = "2-3小时")
    private String estimatedTime;

    @Schema(description = "优先级分数", example = "0.85")
    private BigDecimal priorityScore;

    @Schema(description = "是否已读", example = "false")
    private Boolean isRead;

    @Schema(description = "是否已采纳", example = "false")
    private Boolean isAccepted;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "过期时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

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
    public LearningRecommendation() {
        this.recommendationType = "course";
        this.difficultyLevel = "intermediate";
        this.priorityScore = BigDecimal.ONE;
        this.isRead = false;
        this.isAccepted = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public LearningRecommendation(Long studentId, Long dimensionId, String title, String description) {
        this();
        this.studentId = studentId;
        this.dimensionId = dimensionId;
        this.title = title;
        this.description = description;
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 标记为已采纳
     */
    public void markAsAccepted() {
        this.isAccepted = true;
        if (!this.isRead) {
            markAsRead();
        }
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 获取优先级文本
     */
    public String getPriorityText() {
        if (priorityScore == null) return "中等";
        double score = priorityScore.doubleValue();
        if (score >= 0.8) return "高";
        if (score >= 0.6) return "中等";
        return "低";
    }

    /**
     * 获取类型文本
     */
    public String getTypeText() {
        return switch (recommendationType) {
            case "course" -> "课程";
            case "resource" -> "资源";
            case "practice" -> "练习";
            case "project" -> "项目";
            default -> "其他";
        };
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

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public BigDecimal getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(BigDecimal priorityScore) {
        this.priorityScore = priorityScore;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
        return "LearningRecommendation{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", title='" + title + '\'' +
                ", recommendationType='" + recommendationType + '\'' +
                ", isRead=" + isRead +
                ", isAccepted=" + isAccepted +
                '}';
    }
} 