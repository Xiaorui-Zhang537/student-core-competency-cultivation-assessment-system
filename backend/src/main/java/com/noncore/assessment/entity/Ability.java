package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
    private Boolean isActive;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    /**
     * 默认构造方法
     */
    public Ability() {
        this.isActive = true;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public Ability(String name, String description, String category, String level) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Ability{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", level='" + level + '\'' +
                ", weight=" + weight +
                ", isActive=" + isActive +
                '}';
    }
} 