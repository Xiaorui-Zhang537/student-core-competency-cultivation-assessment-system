package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.service.CommunityService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.JwtUtil;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 社区管理控制器
 * 处理社区帖子、评论、点赞等相关接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/community")
@Tag(name = "社区管理", description = "社区帖子、评论、点赞等相关接口")
public class CommunityController {

    private final CommunityService communityService;
    private final JwtUtil jwtUtil;

    public CommunityController(CommunityService communityService, JwtUtil jwtUtil) {
        this.communityService = communityService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    @PostMapping("/posts")
    @Operation(summary = "发布帖子")
    public ApiResponse<Post> createPost(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ApiResponse.error(401, "请先登录");
            }

            Post post = new Post();
            post.setTitle((String) request.get("title"));
            post.setContent((String) request.get("content"));
            post.setCategory((String) request.get("category"));
            post.setAuthorId(userId);
            post.setAnonymous((Boolean) request.getOrDefault("anonymous", false));
            post.setAllowComments((Boolean) request.getOrDefault("allowComments", true));

            String tagsInput = (String) request.get("tagsInput");
            List<String> tags = tagsInput != null ? Arrays.asList(tagsInput.split("\\s+")) : null;

            Post createdPost = communityService.createPost(post, tags);
            return ApiResponse.success(createdPost);
        } catch (Exception e) {
            return ApiResponse.error(500, "发布帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts")
    @Operation(summary = "获取帖子列表")
    public ApiResponse<PageResult<Post>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String orderBy,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            PageResult<Post> result = communityService.getPostList(page, size, category, keyword, orderBy, userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取帖子列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "获取帖子详情")
    public ApiResponse<Post> getPostDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            Post post = communityService.getPostDetail(id, userId);
            if (post == null) {
                return ApiResponse.error(404, "帖子不存在");
            }
            return ApiResponse.success(post);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取帖子详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/like")
    @Operation(summary = "点赞/取消点赞帖子")
    public ApiResponse<Map<String, Object>> likePost(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ApiResponse.error(401, "请先登录");
            }

            boolean liked = communityService.likePost(id, userId);
            return ApiResponse.success(Map.of("liked", liked));
        } catch (Exception e) {
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/comments")
    @Operation(summary = "发表评论")
    public ApiResponse<PostComment> createComment(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            if (userId == null) {
                return ApiResponse.error(401, "请先登录");
            }

            PostComment comment = new PostComment();
            comment.setPostId(id);
            comment.setAuthorId(userId);
            comment.setContent((String) request.get("content"));

            if (request.containsKey("parentId")) {
                comment.setParentId(((Number) request.get("parentId")).longValue());
            }

            PostComment createdComment = communityService.createComment(comment);
            return ApiResponse.success(createdComment);
        } catch (Exception e) {
            return ApiResponse.error(500, "发表评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/comments")
    @Operation(summary = "获取帖子评论列表")
    public ApiResponse<PageResult<PostComment>> getCommentList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            PageResult<PostComment> result = communityService.getCommentList(id, page, size, userId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取评论列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取社区统计信息")
    public ApiResponse<Map<String, Object>> getCommunityStats() {
        try {
            Map<String, Object> stats = communityService.getCommunityStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot-topics")
    @Operation(summary = "获取热门话题")
    public ApiResponse<List<Map<String, Object>>> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> topics = communityService.getHotTopics(limit);
            return ApiResponse.success(topics);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取热门话题失败: " + e.getMessage());
        }
    }

    @GetMapping("/active-users")
    @Operation(summary = "获取活跃用户")
    public ApiResponse<List<Map<String, Object>>> getActiveUsers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> users = communityService.getActiveUsers(limit);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取活跃用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/my-posts")
    @Operation(summary = "获取我的帖子")
    public ApiResponse<PageResult<Post>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ApiResponse.error(401, "请先登录");
            }

            PageResult<Post> result = communityService.getUserPosts(userId, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取我的帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/search/tags")
    @Operation(summary = "搜索标签")
    public ApiResponse<List<com.noncore.assessment.entity.Tag>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<com.noncore.assessment.entity.Tag> tags = communityService.searchTags(keyword, limit);
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error("搜索标签失败: " + e.getMessage());
        }
    }
} 