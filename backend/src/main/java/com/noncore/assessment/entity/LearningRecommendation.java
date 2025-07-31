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
 * 学习建议实体类
 * 对应数据库learning_recommendations表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "学习建议实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private String recommendationType = "course";

    @Schema(description = "资源链接", example = "https://example.com/java-tutorial")
    private String resourceUrl;

    @Schema(description = "难度等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced"})
    @Builder.Default
    private String difficultyLevel = "intermediate";

    @Schema(description = "预估学习时间", example = "2-3小时")
    private String estimatedTime;

    @Schema(description = "优先级分数", example = "0.85")
    @Builder.Default
    private BigDecimal priorityScore = BigDecimal.ONE;

    @Schema(description = "是否已读", example = "false")
    @Builder.Default
    private Boolean isRead = false;

    @Schema(description = "是否已采纳", example = "false")
    @Builder.Default
    private Boolean isAccepted = false;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

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
} 