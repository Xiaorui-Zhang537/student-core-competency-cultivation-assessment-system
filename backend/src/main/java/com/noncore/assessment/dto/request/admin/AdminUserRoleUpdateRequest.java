package com.noncore.assessment.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员更新用户角色请求。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminUserRoleUpdateRequest {

    /**
     * 角色：student / teacher / admin
     */
    @NotBlank(message = "角色不能为空")
    private String role;
}

