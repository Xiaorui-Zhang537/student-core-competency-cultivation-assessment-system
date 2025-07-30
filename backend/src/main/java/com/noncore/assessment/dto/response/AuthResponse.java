package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.User;
import java.time.LocalDateTime;

/**
 * 认证响应DTO
 * 用于返回用户认证结果，包含用户信息和JWT令牌
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class AuthResponse {

    private User user;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private LocalDateTime issuedAt;

    // 默认构造方法
    public AuthResponse() {
        this.issuedAt = LocalDateTime.now();
    }

    // 带参构造方法
    public AuthResponse(User user, String accessToken, String refreshToken, Long expiresIn) {
        this();
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    /**
     * 创建完整的认证响应
     *
     * @param user 用户信息
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     * @return 认证响应对象
     */
    public static AuthResponse of(User user, String accessToken, String refreshToken, Long expiresIn) {
        return new AuthResponse(user, accessToken, refreshToken, expiresIn);
    }

    /**
     * 创建令牌刷新响应
     *
     * @param accessToken 新的访问令牌
     * @param expiresIn 过期时间（秒）
     * @return 认证响应对象
     */
    public static AuthResponse tokenRefresh(String accessToken, Long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(expiresIn);
        return response;
    }

    /**
     * 创建只包含用户信息的响应
     *
     * @param user 用户信息
     * @return 认证响应对象
     */
    public static AuthResponse userOnly(User user) {
        AuthResponse response = new AuthResponse();
        response.setUser(user);
        return response;
    }

    // Getter和Setter方法
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "user=" + (user != null ? user.getUsername() : null) +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", issuedAt=" + issuedAt +
                '}';
    }
} 