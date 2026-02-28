package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子评论实体类
 *
 * @author Assessment System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 评论者ID
     */
    private Long authorId;

    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    @Builder.Default
    private Integer likesCount = 0;

    /**
     * 状态：published,deleted
     */
    @Builder.Default
    private String status = "published";

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @Builder.Default
    private Boolean deleted = false;

    // 额外字段，用于关联查询
    /**
     * 评论者信息
     */
    private User author;

    /**
     * 父评论信息
     */
    private PostComment parentComment;

    /**
     * 子评论列表
     */
    private List<PostComment> replies;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 所属帖子标题（管理员治理展示/导出）
     */
    private String postTitle;

    /**
     * 所属帖子作者显示名（管理员治理展示）
     */
    private String postAuthorName;
    
    /**
     * 增加点赞数
     */
    public void incrementLikes() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    /**
     * 减少点赞数
     */
    public void decrementLikes() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }

    /**
     * 检查是否已发布
     */
    public boolean isPublished() {
        return "published".equals(this.status);
    }

    /**
     * 检查是否是回复评论
     */
    public boolean isReply() {
        return this.parentId != null;
    }

    /**
     * 检查是否允许查看
     */
    public boolean isViewable() {
        return isPublished() && !Boolean.TRUE.equals(this.deleted);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public User getAuthor() {
        return author;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }
} 
