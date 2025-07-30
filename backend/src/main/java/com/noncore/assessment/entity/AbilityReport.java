package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
public class AbilityReport {

    @Schema(description = "报告ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "报告类型", example = "monthly", allowableValues = {"monthly", "semester", "annual", "custom"})
    private String reportType;

    @Schema(description = "报告标题", example = "2024年1月能力发展报告")
    private String title;

    @Schema(description = "综合得分", example = "78.50")
    private BigDecimal overallScore;

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

    @Schema(description = "生成方式", example = "system", allowableValues = {"system", "teacher"})
    private String generatedBy;

    @Schema(description = "是否发布", example = "true")
    private Boolean isPublished;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "发布时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    // 冗余字段，用于显示
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "学生学号", example = "2021001")
    private String studentNumber;

    /**
     * 默认构造方法
     */
    public AbilityReport() {
        this.reportType = "monthly";
        this.overallScore = BigDecimal.ZERO;
        this.generatedBy = "system";
        this.isPublished = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public AbilityReport(Long studentId, String reportType, LocalDate periodStart, LocalDate periodEnd) {
        this();
        this.studentId = studentId;
        this.reportType = reportType;
        this.reportPeriodStart = periodStart;
        this.reportPeriodEnd = periodEnd;
        this.title = generateTitle(reportType, periodStart);
    }

    /**
     * 生成报告标题
     */
    private String generateTitle(String type, LocalDate start) {
        String typeText = switch (type) {
            case "monthly" -> "月度";
            case "semester" -> "学期";
            case "annual" -> "年度";
            default -> "自定义";
        };
        return String.format("%d年%d月%s能力发展报告", start.getYear(), start.getMonthValue(), typeText);
    }

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

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public LocalDate getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDate reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDate getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDate reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public String getDimensionScores() {
        return dimensionScores;
    }

    public void setDimensionScores(String dimensionScores) {
        this.dimensionScores = dimensionScores;
    }

    public String getTrendsAnalysis() {
        return trendsAnalysis;
    }

    public void setTrendsAnalysis(String trendsAnalysis) {
        this.trendsAnalysis = trendsAnalysis;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(String improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    @Override
    public String toString() {
        return "AbilityReport{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", reportType='" + reportType + '\'' +
                ", title='" + title + '\'' +
                ", overallScore=" + overallScore +
                ", isPublished=" + isPublished +
                '}';
    }
} 