package com.noncore.assessment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "用户公开资料响应模型")
public class UserProfileResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "性别")
    private String gender;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "邮箱是否已验证")
    private boolean emailVerified;
    
    // 可以根据需要添加更多安全的字段，如名字等
    @Schema(description = "显示名称")
    private String displayName;
} 