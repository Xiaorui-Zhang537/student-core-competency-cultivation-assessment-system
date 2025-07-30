package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 标签实体类
 *
 * @author Assessment System
 */
public class Tag {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 使用此标签的帖子数
     */
    private Integer postsCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 构造函数
    public Tag() {}

    public Tag(String name) {
        this.name = name;
        this.color = "#3B82F6";
        this.postsCount = 0;
    }

    public Tag(String name, String description, String color) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.postsCount = 0;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(Integer postsCount) {
        this.postsCount = postsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 增加帖子数量
     */
    public void incrementPostsCount() {
        this.postsCount = (this.postsCount == null ? 0 : this.postsCount) + 1;
    }

    /**
     * 减少帖子数量
     */
    public void decrementPostsCount() {
        this.postsCount = Math.max(0, (this.postsCount == null ? 0 : this.postsCount) - 1);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", postsCount=" + postsCount +
                '}';
    }
} 