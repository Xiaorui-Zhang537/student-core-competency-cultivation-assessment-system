package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
    private BigDecimal weight;

    @Schema(description = "最高分数", example = "100")
    private Integer maxScore;

    @Schema(description = "图标标识", example = "code")
    private String icon;

    @Schema(description = "显示颜色", example = "#10B981")
    private String color;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 默认构造方法
     */
    public AbilityDimension() {
        this.weight = new BigDecimal("1.00");
        this.maxScore = 100;
        this.color = "#3B82F6";
        this.sortOrder = 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public AbilityDimension(String name, String description, String category) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "AbilityDimension{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", weight=" + weight +
                ", isActive=" + isActive +
                '}';
    }
} 