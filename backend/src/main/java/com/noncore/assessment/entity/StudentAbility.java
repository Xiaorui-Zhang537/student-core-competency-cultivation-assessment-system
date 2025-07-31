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
 * 学生能力记录实体类
 * 对应数据库student_abilities表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "学生能力记录实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAbility {

    public StudentAbility(Long studentId, Long dimensionId) {
        this.studentId = studentId;
        this.dimensionId = dimensionId;
        this.currentScore = BigDecimal.ZERO;
        this.level = "beginner";
        this.assessmentCount = 0;
        this.trend = "stable";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "学生ID", example = "1")
    private Long studentId;

    @Schema(description = "能力维度ID", example = "1")
    private Long dimensionId;

    @Schema(description = "当前得分", example = "85.50")
    @Builder.Default
    private BigDecimal currentScore = BigDecimal.ZERO;

    @Schema(description = "能力等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced", "expert"})
    @Builder.Default
    private String level = "beginner";

    @Schema(description = "最后评估时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAssessmentAt;

    @Schema(description = "评估次数", example = "5")
    @Builder.Default
    private Integer assessmentCount = 0;

    @Schema(description = "发展趋势", example = "rising", allowableValues = {"rising", "stable", "declining"})
    @Builder.Default
    private String trend = "stable";

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 冗余字段，用于显示
    @Schema(description = "维度名称", example = "编程能力")
    private String dimensionName;

    @Schema(description = "维度分类", example = "technical")
    private String dimensionCategory;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;
    
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
} 