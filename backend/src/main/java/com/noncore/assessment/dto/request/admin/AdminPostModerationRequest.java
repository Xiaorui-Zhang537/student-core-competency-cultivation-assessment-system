package com.noncore.assessment.dto.request.admin;

import lombok.Data;

/**
 * 管理员帖子治理请求（部分字段可选）。
 *
 * <p>status 推荐值：published / draft / deleted</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminPostModerationRequest {
    private String status;
    private Boolean pinned;
    private Boolean allowComments;
    private Boolean deleted;

    public String getStatus() {
        return status;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public Boolean getDeleted() {
        return deleted;
    }
}
