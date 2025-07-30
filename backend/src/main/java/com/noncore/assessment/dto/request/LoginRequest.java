package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 * 用于接收用户登录表单数据
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class LoginRequest {

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

    // Getter和Setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", rememberMe=" + rememberMe +
                '}';
    }
} 