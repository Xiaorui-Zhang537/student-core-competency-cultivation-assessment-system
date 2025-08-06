package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.service.CommunityService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/community")
@Tag(name = "社区管理", description = "社区帖子、评论、点赞等相关接口")
public class CommunityController extends BaseController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService, UserService userService) {
        super(userService);
        this.communityService = communityService;
    }

    @PostMapping("/posts")
    @Operation(summary = "发布帖子")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Post>> createPost(@RequestBody Post post) {
        post.setAuthorId(getCurrentUserId());
        Post createdPost = communityService.createPost(post, null); // Tags logic needs to be handled if required
        return ResponseEntity.ok(ApiResponse.success(createdPost));
    }

    @GetMapping("/posts")
    @Operation(summary = "获取帖子列表")
    public ResponseEntity<ApiResponse<PageResult<Post>>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String orderBy) {
        Long userId = getOptionalUserId();
        PageResult<Post> result = communityService.getPostList(page, size, category, keyword, orderBy, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "获取帖子详情")
    public ResponseEntity<ApiResponse<Post>> getPostDetail(@PathVariable Long id) {
        Long userId = getOptionalUserId();
        Post post = communityService.getPostDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PostMapping("/posts/{id}/like")
    @Operation(summary = "点赞/取消点赞帖子")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likePost(@PathVariable Long id) {
        boolean liked = communityService.likePost(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    @PostMapping("/posts/{id}/comments")
    @Operation(summary = "发表评论")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostComment>> createComment(@PathVariable Long id, @RequestBody PostComment comment) {
        comment.setPostId(id);
        comment.setAuthorId(getCurrentUserId());
        PostComment createdComment = communityService.createComment(comment);
        return ResponseEntity.ok(ApiResponse.success(createdComment));
    }

    @GetMapping("/posts/{id}/comments")
    @Operation(summary = "获取帖子评论列表")
    public ResponseEntity<ApiResponse<PageResult<PostComment>>> getCommentList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getOptionalUserId();
        PageResult<PostComment> result = communityService.getCommentList(id, page, size, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取社区统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommunityStats() {
        Map<String, Object> stats = communityService.getCommunityStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/hot-topics")
    @Operation(summary = "获取热门话题")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> topics = communityService.getHotTopics(limit);
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    @GetMapping("/active-users")
    @Operation(summary = "获取活跃用户")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActiveUsers(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> users = communityService.getActiveUsers(limit);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/my-posts")
    @Operation(summary = "获取我的帖子")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResult<Post>>> getMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Post> result = communityService.getUserPosts(getCurrentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/search/tags")
    @Operation(summary = "搜索标签")
    public ResponseEntity<ApiResponse<List<com.noncore.assessment.entity.Tag>>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<com.noncore.assessment.entity.Tag> tags = communityService.searchTags(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
} 