package com.noncore.assessment.dto.request.admin;

import lombok.Data;

/**
 * 管理员评论治理请求（部分字段可选）。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminCommentModerationRequest {
    private String status;
    private Boolean deleted;
}

