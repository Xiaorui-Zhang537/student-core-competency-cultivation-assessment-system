package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.admin.AdminUserCreateRequest;
import com.noncore.assessment.dto.request.admin.AdminUserRoleUpdateRequest;
import com.noncore.assessment.dto.request.admin.AdminUserStatusUpdateRequest;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.service.admin.AdminUserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员-用户管理控制器。
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/users")
@Tag(name = "管理员-用户管理", description = "管理员用户管理（学生/教师/管理员）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController extends BaseController {

    private final AdminUserService adminUserService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminUsersController(AdminUserService adminUserService,
                                AdminAuditLogService adminAuditLogService,
                                UserService userService) {
        super(userService);
        this.adminUserService = adminUserService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping
    @Operation(summary = "分页查询用户")
    public ResponseEntity<ApiResponse<PageResult<AdminUserListItemResponse>>> pageUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.pageUsers(page, size, keyword, role, status, includeDeleted)));
    }

    @PostMapping
    @Operation(summary = "创建用户（可创建管理员）")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody AdminUserCreateRequest request,
                                                        HttpServletRequest httpRequest) {
        User created = adminUserService.createUser(request);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.user.create",
                "user",
                created != null ? created.getId() : null,
                Map.of("role", request.getRole(), "status", request.getStatus(), "username", request.getUsername(), "email", request.getEmail()),
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "更新用户角色")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id,
                                                        @Valid @RequestBody AdminUserRoleUpdateRequest request,
                                                        HttpServletRequest httpRequest) {
        adminUserService.updateUserRole(id, request.getRole());
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.user.updateRole",
                "user",
                id,
                Map.of("role", request.getRole()),
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态（active/disabled）")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody AdminUserStatusUpdateRequest request,
                                                          HttpServletRequest httpRequest) {
        adminUserService.updateUserStatus(id, request.getStatus());
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.user.updateStatus",
                "user",
                id,
                Map.of("status", request.getStatus()),
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/password/reset-email")
    @Operation(summary = "发送密码重置邮件")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByEmail(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "zh-CN") String lang,
            HttpServletRequest httpRequest
    ) {
        adminUserService.sendPasswordResetEmail(id, lang);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.user.passwordResetEmail",
                "user",
                id,
                Map.of("lang", lang),
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success());
    }
}

