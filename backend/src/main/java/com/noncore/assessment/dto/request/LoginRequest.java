package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录请求DTO
 * 用于接收用户登录表单数据
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Setter
@Getter
public class LoginRequest {

    // Getter和Setter方法
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private boolean rememberMe = false;

    // 默认构造方法
    public LoginRequest() {}

    // 带参构造方法
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
} 