package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;

/**
 * 认证服务接口
 * 定义用户认证相关的业务方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证响应（包含用户信息和令牌）
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 认证响应（包含用户信息和令牌）
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * 刷新访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌信息
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 访问令牌
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);
} 