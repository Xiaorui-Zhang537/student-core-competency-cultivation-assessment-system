package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.dto.request.PostCreateRequest;
import com.noncore.assessment.service.CommunityService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.behavior.BehaviorEventRecorder;
import com.noncore.assessment.behavior.BehaviorEventType;
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
    private final BehaviorEventRecorder behaviorEventRecorder;

    public CommunityController(CommunityService communityService, BehaviorEventRecorder behaviorEventRecorder, UserService userService) {
        super(userService);
        this.communityService = communityService;
        this.behaviorEventRecorder = behaviorEventRecorder;
    }

    @PostMapping("/posts")
    @Operation(summary = "发布帖子")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Post>> createPost(@RequestBody PostCreateRequest req) {
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCategory(req.getCategory());
        post.setAllowComments(Boolean.TRUE.equals(req.getAllowComments()));
        post.setAnonymous(Boolean.TRUE.equals(req.getAnonymous()));
        post.setPinned(Boolean.TRUE.equals(req.getPinned()));
        post.setAuthorId(getCurrentUserId());
        Post createdPost = communityService.createPost(post, req.getTags());
        // 行为记录：社区发问（仅记录事实，不评价，不算分）
        try {
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("category", req.getCategory());
            meta.put("anonymous", Boolean.TRUE.equals(req.getAnonymous()));
            behaviorEventRecorder.record(
                    getCurrentUserId(),
                    null,
                    BehaviorEventType.COMMUNITY_ASK,
                    "post",
                    createdPost != null ? createdPost.getId() : null,
                    meta
            );
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(createdPost));
    }

    @PutMapping("/posts/{id}")
    @Operation(summary = "编辑帖子（仅作者）")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Post>> updatePost(@PathVariable Long id, @RequestBody PostCreateRequest req) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setCategory(req.getCategory());
        post.setAllowComments(Boolean.TRUE.equals(req.getAllowComments()));
        post.setAnonymous(Boolean.TRUE.equals(req.getAnonymous()));
        post.setPinned(Boolean.TRUE.equals(req.getPinned()));
        // 当前用户校验在 service 内完成
        Post updated = communityService.updatePost(post, req.getTags(), getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/posts")
    @Operation(summary = "获取帖子列表")
    public ResponseEntity<ApiResponse<PageResult<Post>>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String orderBy) {
        Long userId = getOptionalUserId();
        PageResult<Post> result;
        if (tag != null && !tag.trim().isEmpty()) {
            result = communityService.searchPostsByTag(tag.trim(), page, size, userId);
        } else {
            result = communityService.getPostList(page, size, category, keyword, orderBy, userId);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "获取帖子详情")
    public ResponseEntity<ApiResponse<Post>> getPostDetail(@PathVariable Long id) {
        Long userId = getOptionalUserId();
        Post post = communityService.getPostDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "删除帖子")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        communityService.deletePost(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
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
        // 行为记录：社区回答（评论/回复）
        try {
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("postId", id);
            meta.put("parentId", comment.getParentId());
            behaviorEventRecorder.record(
                    getCurrentUserId(),
                    null,
                    BehaviorEventType.COMMUNITY_ANSWER,
                    "post_comment",
                    createdComment != null ? createdComment.getId() : null,
                    meta
            );
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(createdComment));
    }

    @GetMapping("/posts/{id}/comments")
    @Operation(summary = "获取帖子评论列表")
    public ResponseEntity<ApiResponse<PageResult<PostComment>>> getCommentList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "time") String orderBy) {
        Long userId = getOptionalUserId();
        PageResult<PostComment> result = communityService.getCommentList(id, page, size, userId, parentId, orderBy);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "点赞/取消点赞评论")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeComment(@PathVariable Long commentId) {
        boolean liked = communityService.likeComment(commentId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        communityService.deleteComment(commentId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
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