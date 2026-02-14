package com.noncore.assessment.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员创建用户请求。
 *
 * <p>说明：用于创建学生/教师/管理员账号。角色字段与 DB enum 对齐：student/teacher/admin。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    /**
     * 角色：student / teacher / admin
     */
    @NotBlank(message = "角色不能为空")
    private String role;

    /**
     * 状态：active / disabled
     */
    private String status;

    private String nickname;
    private String firstName;
    private String lastName;
    private String studentNo;
    private String teacherNo;
    private String school;
    private String subject;
    private String grade;
}

