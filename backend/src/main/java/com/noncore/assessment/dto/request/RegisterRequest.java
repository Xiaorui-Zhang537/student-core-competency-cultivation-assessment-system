package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户注册请求DTO
 * 用于接收用户注册表单数据
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Setter
@Getter
public class RegisterRequest {

    // Getter和Setter方法
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "^(student|teacher)$", message = "用户角色只能是student或teacher")
    private String role;

    @Size(max = 20, message = "姓氏长度不能超过20个字符")
    private String firstName;

    @Size(max = 20, message = "名字长度不能超过20个字符")
    private String lastName;

    @Size(max = 50, message = "显示名称长度不能超过50个字符")
    private String displayName;

    @Size(max = 20, message = "年级信息长度不能超过20个字符")
    private String grade;

    @Size(max = 50, message = "专业/学科信息长度不能超过50个字符")
    private String subject;

    @Size(max = 100, message = "学校信息长度不能超过100个字符")
    private String school;

    @Pattern(regexp = "^1[3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String bio;

    // 默认构造方法
    public RegisterRequest() {}

    // 带参构造方法
    public RegisterRequest(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * 检查密码一致性
     *
     * @return true如果密码一致，false如果不一致
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
} 