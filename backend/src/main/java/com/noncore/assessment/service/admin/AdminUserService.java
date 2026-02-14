package com.noncore.assessment.service.admin;

import com.noncore.assessment.dto.request.admin.AdminUserCreateRequest;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.util.PageResult;

/**
 * 管理员用户管理服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminUserService {

    PageResult<AdminUserListItemResponse> pageUsers(int page, int size, String keyword, String role, String status, boolean includeDeleted);

    User createUser(AdminUserCreateRequest request);

    void updateUserRole(Long id, String role);

    void updateUserStatus(Long id, String status);

    void sendPasswordResetEmail(Long id, String lang);
}

