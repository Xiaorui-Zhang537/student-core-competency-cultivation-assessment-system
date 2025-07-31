package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 认证响应DTO
 * 用于返回用户认证结果，包含用户信息和JWT令牌
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UserDto user;
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    /**
     * 创建完整的认证响应
     *
     * @param user 用户信息
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @param expiresIn 过期时间（秒）
     * @return 认证响应对象
     */
    public static AuthResponse of(UserDto user, String accessToken, String refreshToken, Long expiresIn) {
        return AuthResponse.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 创建令牌刷新响应
     *
     * @param accessToken 新的访问令牌
     * @param expiresIn 过期时间（秒）
     * @return 认证响应对象
     */
    public static AuthResponse tokenRefresh(String accessToken, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 创建只包含用户信息的响应
     *
     * @param user 用户信息
     * @return 认证响应对象
     */
    public static AuthResponse userOnly(UserDto user) {
        return AuthResponse.builder()
                .user(user)
                .build();
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