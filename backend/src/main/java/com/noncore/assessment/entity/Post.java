package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子实体类
 *
 * @author Assessment System
 */
public class Post {
    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子分类：study,homework,share,qa,chat
     */
    private String category;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 是否匿名发布
     */
    private Boolean anonymous;

    /**
     * 是否允许评论
     */
    private Boolean allowComments;

    /**
     * 浏览次数
     */
    private Integer views;

    /**
     * 点赞数
     */
    private Integer likesCount;

    /**
     * 评论数
     */
    private Integer commentsCount;

    /**
     * 状态：draft,published,deleted
     */
    private String status;

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
    private Boolean deleted;

    // 额外字段，用于关联查询
    /**
     * 作者信息
     */
    private User author;

    /**
     * 标签列表
     */
    private List<Tag> tags;

    /**
     * 最新评论列表
     */
    private List<PostComment> recentComments;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 最后回复信息
     */
    private PostComment lastReply;

    // 构造函数
    public Post() {}

    public Post(String title, String content, String category, Long authorId) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.authorId = authorId;
        this.pinned = false;
        this.anonymous = false;
        this.allowComments = true;
        this.views = 0;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.status = "published";
        this.deleted = false;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<PostComment> getRecentComments() {
        return recentComments;
    }

    public void setRecentComments(List<PostComment> recentComments) {
        this.recentComments = recentComments;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public PostComment getLastReply() {
        return lastReply;
    }

    public void setLastReply(PostComment lastReply) {
        this.lastReply = lastReply;
    }

    /**
     * 增加浏览量
     */
    public void incrementViews() {
        this.views = (this.views == null ? 0 : this.views) + 1;
    }

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
     * 增加评论数
     */
    public void incrementComments() {
        this.commentsCount = (this.commentsCount == null ? 0 : this.commentsCount) + 1;
    }

    /**
     * 减少评论数
     */
    public void decrementComments() {
        this.commentsCount = Math.max(0, (this.commentsCount == null ? 0 : this.commentsCount) - 1);
    }

    /**
     * 检查是否已发布
     */
    public boolean isPublished() {
        return "published".equals(this.status);
    }

    /**
     * 检查是否允许查看
     */
    public boolean isViewable() {
        return isPublished() && !Boolean.TRUE.equals(this.deleted);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", authorId=" + authorId +
                ", pinned=" + pinned +
                ", views=" + views +
                ", likesCount=" + likesCount +
                ", commentsCount=" + commentsCount +
                ", status='" + status + '\'' +
                ", deleted=" + deleted +
                '}';
    }
} 