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
    
    @Schema(description = "名")
    private String firstName;

    @Schema(description = "姓")
    private String lastName;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "专业/科目")
    private String subject;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "工号")
    private String teacherNo;

    @Schema(description = "生日，yyyy-MM-dd")
    private String birthday;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省份/州")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "手机号")
    private String phone;
} 