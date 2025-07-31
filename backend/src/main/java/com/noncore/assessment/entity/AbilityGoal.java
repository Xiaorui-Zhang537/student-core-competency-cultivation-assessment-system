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
 * 能力发展目标实体类
 * 对应数据库ability_goals表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力发展目标实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private BigDecimal currentScore = BigDecimal.ZERO;

    @Schema(description = "目标达成日期", example = "2024-01-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @Schema(description = "优先级", example = "medium", allowableValues = {"low", "medium", "high"})
    @Builder.Default
    private String priority = "medium";

    @Schema(description = "目标状态", example = "active", allowableValues = {"active", "achieved", "paused", "cancelled"})
    @Builder.Default
    private String status = "active";

    @Schema(description = "达成时间", example = "2024-01-25 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime achievedAt;

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

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

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
} 