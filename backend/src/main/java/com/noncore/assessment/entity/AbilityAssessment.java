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
 * 能力评估实体类
 * 对应数据库ability_assessments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力评估实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private String status = "draft";

    @Schema(description = "评估时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessedAt;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Schema(description = "是否删除", example = "false")
    @Builder.Default
    private Boolean deleted = false;

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
} 