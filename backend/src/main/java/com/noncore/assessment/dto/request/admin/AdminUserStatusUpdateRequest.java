package com.noncore.assessment.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员更新用户状态请求。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminUserStatusUpdateRequest {

    /**
     * 状态：active / disabled
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}

