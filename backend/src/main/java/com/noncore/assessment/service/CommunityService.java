package com.noncore.assessment.service;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.entity.Tag;
import com.noncore.assessment.util.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 社区服务接口
 *
 * @author Assessment System
 */
public interface CommunityService {

    /**
     * 发布帖子
     *
     * @param post 帖子信息
     * @param tags 标签列表
     * @return 发布的帖子
     */
    Post createPost(Post post, List<String> tags);

    /**
     * 获取帖子详情
     *
     * @param id 帖子ID
     * @param userId 当前用户ID（用于判断是否点赞）
     * @return 帖子详情
     */
    Post getPostDetail(Long id, Long userId);

    /**
     * 更新帖子
     *
     * @param post 帖子信息
     * @param tags 标签列表
     * @return 更新的帖子
     */
    Post updatePost(Post post, List<String> tags, Long userId);

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @param userId 当前用户ID
     */
    void deletePost(Long id, Long userId);

    /**
     * 分页查询帖子列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param category 分类
     * @param keyword 关键词
     * @param orderBy 排序方式
     * @param userId 当前用户ID
     * @return 帖子分页结果
     */
    PageResult<Post> getPostList(int page, int size, String category, String keyword, String orderBy, Long userId);

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否点赞成功
     */
    boolean likePost(Long postId, Long userId);

    /**
     * 发表评论
     *
     * @param comment 评论信息
     * @return 发表的评论
     */
    PostComment createComment(PostComment comment);

    /**
     * 获取帖子评论列表
     *
     * @param postId 帖子ID
     * @param page 页码
     * @param size 每页大小
     * @param userId 当前用户ID
     * @return 评论分页结果
     */
    PageResult<PostComment> getCommentList(Long postId, int page, int size, Long userId, Long parentId, String orderBy);

    /**
     * 删除评论（作者或有权限用户）
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否点赞成功
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 获取社区统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getCommunityStats();

    /**
     * 获取热门话题
     *
     * @param limit 限制数量
     * @return 热门话题列表
     */
    List<Map<String, Object>> getHotTopics(int limit);

    /**
     * 获取活跃用户
     *
     * @param limit 限制数量
     * @return 活跃用户列表
     */
    List<Map<String, Object>> getActiveUsers(int limit);

    /**
     * 获取分类统计
     *
     * @return 分类统计列表
     */
    List<Map<String, Object>> getCategoryStats();

    /**
     * 根据标签搜索帖子
     *
     * @param tagName 标签名称
     * @param page 页码
     * @param size 每页大小
     * @param userId 当前用户ID
     * @return 帖子分页结果
     */
    PageResult<Post> searchPostsByTag(String tagName, int page, int size, Long userId);

    /**
     * 获取用户的帖子列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 帖子分页结果
     */
    PageResult<Post> getUserPosts(Long userId, int page, int size);

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> searchTags(String keyword, int limit);
} 