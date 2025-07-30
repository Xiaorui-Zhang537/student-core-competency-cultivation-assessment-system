package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 帖子点赞实体类
 *
 * @author Assessment System
 */
public class PostLike {
    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 构造函数
    public PostLike() {}

    public PostLike(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PostLike{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
} 