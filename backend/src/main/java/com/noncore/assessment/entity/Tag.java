package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签实体类
 *
 * @author Assessment System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    public Tag(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.postsCount = 0;
    }
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
    @Builder.Default
    private String color = "#3B82F6";

    /**
     * 使用此标签的帖子数
     */
    @Builder.Default
    private Integer postsCount = 0;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
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
} 