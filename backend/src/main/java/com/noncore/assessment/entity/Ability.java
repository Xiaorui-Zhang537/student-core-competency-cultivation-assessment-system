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
 * 能力实体类
 * 对应数据库abilities表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "能力实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ability {

    @Schema(description = "能力ID", example = "1")
    private Long id;

    @Schema(description = "能力名称", example = "Java编程能力")
    private String name;

    @Schema(description = "能力描述", example = "Java语言编程和应用开发能力")
    private String description;

    @Schema(description = "能力分类", example = "technical", allowableValues = {"technical", "soft", "academic", "creative"})
    private String category;

    @Schema(description = "能力等级", example = "intermediate", allowableValues = {"beginner", "intermediate", "advanced", "expert"})
    private String level;

    @Schema(description = "权重", example = "0.8")
    private BigDecimal weight;

    @Schema(description = "父能力ID", example = "1")
    private Long parentId;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

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

    @Schema(description = "是否删除", example = "false")
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 