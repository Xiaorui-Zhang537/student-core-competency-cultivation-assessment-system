package com.noncore.assessment.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 管理员端用户列表项。
 *
 * <p>注意：不返回 password 等敏感字段。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Data
@Builder
public class AdminUserListItemResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String status;
    private String avatar;
    private String nickname;
    private String mbti;
    private String firstName;
    private String lastName;
    private String studentNo;
    private String teacherNo;
    private boolean emailVerified;
    private boolean deleted;
    private Date createdAt;
    private Date updatedAt;
}

