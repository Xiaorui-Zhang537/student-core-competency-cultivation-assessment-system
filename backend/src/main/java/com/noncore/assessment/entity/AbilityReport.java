package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 能力报告实体类
 * 对应数据库ability_reports表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力报告实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityReport {

    public AbilityReport(Long studentId, String reportType, LocalDate reportPeriodStart, LocalDate reportPeriodEnd) {
        this.studentId = studentId;
        this.reportType = reportType;
        this.reportPeriodStart = reportPeriodStart;
        this.reportPeriodEnd = reportPeriodEnd;
        this.createdAt = LocalDateTime.now();
        this.isPublished = false;
        this.generatedBy = "system";
        this.overallScore = BigDecimal.ZERO;
        this.title = String.format("%d年%d月能力发展报告", reportPeriodEnd.getYear(), reportPeriodEnd.getMonthValue());
    }

    @Schema(description = "报告ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "报告类型", example = "monthly", allowableValues = {"monthly", "semester", "annual", "custom"})
    @Builder.Default
    private String reportType = "monthly";

    @Schema(description = "报告标题", example = "2024年1月能力发展报告")
    private String title;

    @Schema(description = "综合得分", example = "78.50")
    @Builder.Default
    private BigDecimal overallScore = BigDecimal.ZERO;

    @Schema(description = "报告周期开始", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportPeriodStart;

    @Schema(description = "报告周期结束", example = "2024-01-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportPeriodEnd;

    @Schema(description = "各维度得分（JSON格式）", example = "{\"programming\": 85, \"communication\": 72}")
    private String dimensionScores;

    @Schema(description = "趋势分析", example = "本月编程能力有显著提升，沟通能力保持稳定")
    private String trendsAnalysis;

    @Schema(description = "成就列表（JSON格式）", example = "[{\"title\": \"编程高手\", \"description\": \"完成10个编程项目\"}]")
    private String achievements;

    @Schema(description = "待改进领域（JSON格式）", example = "[{\"dimension\": \"沟通协作\", \"suggestion\": \"多参与团队讨论\"}]")
    private String improvementAreas;

    @Schema(description = "综合建议", example = "继续保持编程学习的积极性，同时加强团队协作能力的培养")
    private String recommendations;

    // 关联上下文
    private Long courseId;
    private Long assignmentId;
    private Long submissionId;
    @Schema(description = "关联AI批改历史ID", example = "123")
    private Long aiHistoryId;

    @Schema(description = "生成方式", example = "system", allowableValues = {"system", "teacher"})
    @Builder.Default
    private String generatedBy = "system";

    @Schema(description = "是否发布", example = "true")
    @Builder.Default
    private Boolean isPublished = false;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "发布时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    // 冗余字段，用于显示
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNumber;

    @Schema(description = "作业标题", example = "Python 条件语句练习")
    private String assignmentTitle;
    
    /**
     * 发布报告
     */
    public void publish() {
        this.isPublished = true;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * 取消发布
     */
    public void unpublish() {
        this.isPublished = false;
        this.publishedAt = null;
    }

    /**
     * 获取报告期间的天数
     */
    public long getPeriodDays() {
        if (reportPeriodStart == null || reportPeriodEnd == null) {
            return 0;
        }
        return reportPeriodEnd.toEpochDay() - reportPeriodStart.toEpochDay() + 1;
    }

    /**
     * 获取等级描述
     */
    public String getGradeDescription() {
        if (overallScore == null) return "暂无评估";
        double score = overallScore.doubleValue();
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "及格";
        return "需改进";
    }
} 