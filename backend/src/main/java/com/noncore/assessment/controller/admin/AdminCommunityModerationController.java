package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.admin.AdminCommentModerationRequest;
import com.noncore.assessment.dto.request.admin.AdminPostModerationRequest;
import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.service.admin.AdminCommunityModerationService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-社区内容治理控制器。
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/community")
@Tag(name = "管理员-社区治理", description = "管理员对帖子/评论进行下架/置顶/删除等治理操作")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommunityModerationController extends BaseController {

    private final AdminCommunityModerationService adminCommunityModerationService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminCommunityModerationController(AdminCommunityModerationService adminCommunityModerationService,
                                              AdminAuditLogService adminAuditLogService,
                                              UserService userService) {
        super(userService);
        this.adminCommunityModerationService = adminCommunityModerationService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/posts")
    @Operation(summary = "分页查询帖子（治理视角）")
    public ResponseEntity<ApiResponse<PageResult<Post>>> pagePosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCommunityModerationService.pagePosts(page, size, keyword, category, status, pinned, includeDeleted)
        ));
    }

    @PutMapping("/posts/{id}")
    @Operation(summary = "治理帖子（更新状态/置顶/禁评/删除）")
    public ResponseEntity<ApiResponse<Void>> moderatePost(@PathVariable("id") Long id,
                                                          @Valid @RequestBody AdminPostModerationRequest request,
                                                          HttpServletRequest httpRequest) {
        adminCommunityModerationService.moderatePost(id, request.getStatus(), request.getPinned(), request.getAllowComments(), request.getDeleted());
        Map<String, Object> detail = new HashMap<>();
        detail.put("status", request.getStatus());
        detail.put("pinned", request.getPinned());
        detail.put("allowComments", request.getAllowComments());
        detail.put("deleted", request.getDeleted());
        adminAuditLogService.record(getCurrentUserId(), "admin.community.post.moderate", "post", id, detail, httpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/comments")
    @Operation(summary = "分页查询评论（治理视角）")
    public ResponseEntity<ApiResponse<PageResult<PostComment>>> pageComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminCommunityModerationService.pageComments(page, size, postId, keyword, status, includeDeleted)
        ));
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "治理评论（更新状态/删除）")
    public ResponseEntity<ApiResponse<Void>> moderateComment(@PathVariable("id") Long id,
                                                             @Valid @RequestBody AdminCommentModerationRequest request,
                                                             HttpServletRequest httpRequest) {
        adminCommunityModerationService.moderateComment(id, request.getStatus(), request.getDeleted());
        Map<String, Object> detail = new HashMap<>();
        detail.put("status", request.getStatus());
        detail.put("deleted", request.getDeleted());
        adminAuditLogService.record(getCurrentUserId(), "admin.community.comment.moderate", "post_comment", id, detail, httpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

