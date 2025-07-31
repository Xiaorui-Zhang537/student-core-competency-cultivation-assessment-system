package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子实体类
 *
 * @author Assessment System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Boolean pinned = false;

    /**
     * 是否匿名发布
     */
    @Builder.Default
    private Boolean anonymous = false;

    /**
     * 是否允许评论
     */
    @Builder.Default
    private Boolean allowComments = true;

    /**
     * 浏览次数
     */
    @Builder.Default
    private Integer views = 0;

    /**
     * 点赞数
     */
    @Builder.Default
    private Integer likesCount = 0;

    /**
     * 评论数
     */
    @Builder.Default
    private Integer commentsCount = 0;

    /**
     * 状态：draft,published,deleted
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
} 