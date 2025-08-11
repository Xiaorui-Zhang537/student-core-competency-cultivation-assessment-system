package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.CommunityService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 *
 * @author Assessment System
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final TagMapper tagMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostTagMapper postTagMapper;


    public CommunityServiceImpl(PostMapper postMapper, PostCommentMapper postCommentMapper,
                                PostLikeMapper postLikeMapper, TagMapper tagMapper,
                                CommentLikeMapper commentLikeMapper, PostTagMapper postTagMapper) {
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.postLikeMapper = postLikeMapper;
        this.tagMapper = tagMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.postTagMapper = postTagMapper;
    }

    @Override
    @Transactional
    public Post createPost(Post post, List<String> tags) {
        // 插入帖子
        postMapper.insert(post);

        // 处理标签
        if (tags != null && !tags.isEmpty()) {
            handlePostTags(post.getId(), tags);
        }

        return post;
    }

    @Override
    public Post getPostDetail(Long id, Long userId) {
        // 增加浏览量
        postMapper.incrementViews(id);

        // 获取帖子详情
        Post post = postMapper.selectDetailById(id);
        if (post == null) {
            return null;
        }

        // 获取标签
        List<Tag> postTags = tagMapper.selectByPostId(id);
        post.setTags(postTags);

        // 获取最新评论
        List<PostComment> recentComments = postCommentMapper.selectRecentByPostId(id, 3);
        post.setRecentComments(recentComments);

        // 获取最后回复
        PostComment lastReply = postCommentMapper.selectLastByPostId(id);
        post.setLastReply(lastReply);

        // 检查用户是否已点赞
        if (userId != null) {
            PostLike like = postLikeMapper.selectByPostIdAndUserId(id, userId);
            post.setLiked(like != null);
        }

        return post;
    }

    @Override
    @Transactional
    public Post updatePost(Post post, List<String> tags, Long userId) {
        // 权限校验：仅作者可编辑
        Post exist = postMapper.selectById(post.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "帖子不存在");
        }
        if (!Objects.equals(exist.getAuthorId(), userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_OPERATION, "用户无权编辑此帖子");
        }

        // 更新帖子
        postMapper.updateById(post);

        // 重新处理标签
        if (tags != null) {
            handlePostTags(post.getId(), tags);
        }

        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            // Post not found, can be considered as a successful deletion.
            return;
        }
        if (!post.getAuthorId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_OPERATION, "用户无权删除此帖子");
        }
        postMapper.softDeleteById(id);
    }

    @Override
    public PageResult<Post> getPostList(int page, int size, String category, String keyword, String orderBy, Long userId) {
        PageHelper.startPage(page, size);

        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("keyword", keyword);
        params.put("orderBy", orderBy);
        params.put("status", "published");

        List<Post> posts = postMapper.selectDetailList(params);

        // 设置标签和点赞状态
        for (Post post : posts) {
            // 获取标签
            List<Tag> postTags = tagMapper.selectByPostId(post.getId());
            post.setTags(postTags);

            // 检查用户是否已点赞
            if (userId != null) {
                PostLike like = postLikeMapper.selectByPostIdAndUserId(post.getId(), userId);
                post.setLiked(like != null);
            }

            // 获取最后回复
            PostComment lastReply = postCommentMapper.selectLastByPostId(post.getId());
            post.setLastReply(lastReply);
        }

        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        return PageResult.of(posts, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    @Transactional
    public boolean likePost(Long postId, Long userId) {
        PostLike existingLike = postLikeMapper.selectByPostIdAndUserId(postId, userId);

        if (existingLike != null) {
            // 取消点赞
            postLikeMapper.delete(postId, userId);
            postMapper.decrementLikes(postId);
            return false;
        } else {
            // 点赞
            PostLike like = new PostLike(postId, userId);
            postLikeMapper.insert(like);
            postMapper.incrementLikes(postId);
            return true;
        }
    }

    @Override
    @Transactional
    public PostComment createComment(PostComment comment) {
        // 插入评论
        postCommentMapper.insert(comment);

        // 增加帖子评论数
        postMapper.incrementComments(comment.getPostId());

        return comment;
    }

    @Override
    public PageResult<PostComment> getCommentList(Long postId, int page, int size, Long userId, Long parentId, String orderBy) {
        PageHelper.startPage(page, size);

        Map<String, Object> params = new HashMap<>();
        params.put("postId", postId);
        params.put("status", "published");
        if (parentId != null) {
            params.put("parentId", parentId);
        }
        // orderBy: time(默认) / hot
        params.put("orderBy", orderBy);

        List<PostComment> comments = parentId == null
                ? postCommentMapper.selectDetailList(params)
                : postCommentMapper.selectByPostId(postId, params);

        // 标记当前用户是否点赞过该评论，避免前端状态与后端不一致导致首次点击出现相反增减
        if (userId != null && comments != null && !comments.isEmpty()) {
            for (PostComment c : comments) {
                Integer likedCount = commentLikeMapper.selectByCommentIdAndUserId(c.getId(), userId);
                boolean liked = likedCount != null && likedCount > 0;
                c.setLiked(liked);
            }
        }

        PageInfo<PostComment> pageInfo = new PageInfo<>(comments);
        return PageResult.of(comments, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            return;
        }
        if (!Objects.equals(comment.getAuthorId(), userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_OPERATION, "用户无权删除该评论");
        }
        postCommentMapper.softDeleteById(commentId);
        postMapper.decrementComments(comment.getPostId());
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        Integer existCount = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        if (existCount != null && existCount > 0) {
            // 取消点赞
            commentLikeMapper.delete(commentId, userId);
            postCommentMapper.decrementLikes(commentId);
            return false;
        } else {
            // 点赞
            commentLikeMapper.insert(commentId, userId);
            postCommentMapper.incrementLikes(commentId);
            return true;
        }
    }

    @Override
    public Map<String, Object> getCommunityStats() {
        return postMapper.selectCommunityStats();
    }

    @Override
    public List<Map<String, Object>> getHotTopics(int limit) {
        List<Tag> hotTags = tagMapper.selectHotTags(limit);
        return hotTags.stream().map(tag -> {
            Map<String, Object> topic = new HashMap<>();
            topic.put("id", tag.getId());
            topic.put("name", tag.getName());
            topic.put("postCount", tag.getPostsCount());
            return topic;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getActiveUsers(int limit) {
        return postMapper.selectActiveUsers(limit);
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return postMapper.selectCategoryStats();
    }

    @Override
    public PageResult<Post> searchPostsByTag(String tagName, int page, int size, Long userId) {
        Tag tag = tagMapper.selectByName(tagName);
        if (tag == null) {
            return PageResult.of(Collections.emptyList(), page, size, 0L, 0);
        }

        PageHelper.startPage(page, size);
        Map<String, Object> params = new HashMap<>();
        List<Post> posts = postMapper.selectByTagId(tag.getId(), params);

        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        return PageResult.of(posts, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public PageResult<Post> getUserPosts(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        Map<String, Object> params = new HashMap<>();
        params.put("status", "published");

        List<Post> posts = postMapper.selectByUserId(userId, params);

        PageInfo<Post> pageInfo = new PageInfo<>(posts);
        return PageResult.of(posts, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public List<Tag> searchTags(String keyword, int limit) {
        return tagMapper.searchTags(keyword, limit);
    }

    /**
     * 处理帖子标签
     */
    private void handlePostTags(Long postId, List<String> tagNames) {
        // 1. 删除旧的标签关联
        postTagMapper.deleteByPostId(postId);

        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        // 2. 找出已存在的标签和需要创建的新标签
        List<Tag> existingTags = tagMapper.selectByNames(tagNames);
        Map<String, Long> existingTagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, Tag::getId));

        List<Tag> newTagsToCreate = new ArrayList<>();
        for (String tagName : tagNames) {
            if (!existingTagMap.containsKey(tagName)) {
                newTagsToCreate.add(new Tag(tagName));
            }
        }

        // 3. 批量创建新标签
        if (!newTagsToCreate.isEmpty()) {
            tagMapper.batchInsert(newTagsToCreate);
            // 将新创建的标签加入到map中
            for (Tag newTag : newTagsToCreate) {
                existingTagMap.put(newTag.getName(), newTag.getId());
            }
        }

        // 4. 批量创建帖子和标签的关联
        List<PostTag> postTagsToInsert = tagNames.stream()
                .map(tagName -> new PostTag(postId, existingTagMap.get(tagName)))
                .collect(Collectors.toList());

        if (!postTagsToInsert.isEmpty()) {
            postTagMapper.batchInsert(postTagsToInsert);
        }
    }
} 