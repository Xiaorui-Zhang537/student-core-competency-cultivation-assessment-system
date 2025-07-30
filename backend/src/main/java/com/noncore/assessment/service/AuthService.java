package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;
import com.noncore.assessment.entity.User;

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
     * 获取当前用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getCurrentUser(String username);

    /**
     * 获取用户资料
     *
     * @param username 用户名
     * @return 用户资料
     */
    User getUserProfile(String username);

    /**
     * 更新用户资料
     *
     * @param user 用户信息
     */
    void updateUserProfile(User user);

    /**
     * 修改密码
     *
     * @param username 用户名
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     */
    void changePassword(String username, String currentPassword, String newPassword);

    /**
     * 发送忘记密码邮件
     *
     * @param email 邮箱地址
     */
    void forgotPassword(String email);

    /**
     * 重置密码
     *
     * @param token 重置令牌
     * @param newPassword 新密码
     */
    void resetPassword(String token, String newPassword);

    /**
     * 验证邮箱
     *
     * @param token 验证令牌
     */
    void verifyEmail(String token);

    /**
     * 重新发送验证邮件
     *
     * @param username 用户名
     */
    void resendVerification(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);
} 