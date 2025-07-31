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
 * 能力维度实体类
 * 对应数据库ability_dimensions表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力维度实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityDimension {

    @Schema(description = "维度ID", example = "1")
    private Long id;

    @Schema(description = "维度名称", example = "编程能力")
    private String name;

    @Schema(description = "维度描述", example = "代码编写、调试和优化能力")
    private String description;

    @Schema(description = "分类", example = "technical", allowableValues = {"technical", "soft", "academic", "creative"})
    private String category;

    @Schema(description = "权重系数", example = "1.00")
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.00");

    @Schema(description = "最高分数", example = "100")
    @Builder.Default
    private Integer maxScore = 100;

    @Schema(description = "图标标识", example = "code")
    private String icon;

    @Schema(description = "显示颜色", example = "#10B981")
    @Builder.Default
    private String color = "#3B82F6";

    @Schema(description = "排序顺序", example = "1")
    @Builder.Default
    private Integer sortOrder = 0;

    @Schema(description = "是否激活", example = "true")
    @Builder.Default
    private Boolean isActive = true;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 