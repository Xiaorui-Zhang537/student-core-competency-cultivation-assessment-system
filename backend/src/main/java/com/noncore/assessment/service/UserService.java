package com.noncore.assessment.service;

import com.noncore.assessment.entity.User;
import com.noncore.assessment.dto.request.UpdateProfileRequest;

/**
 * 用户服务接口
 * 定义用户资料管理、密码管理等相关业务方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-07-30
 */
public interface UserService {

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
     * @param userId  当前用户ID
     * @param request 包含要更新字段的请求对象
     * @return 更新后的用户对象
     */
    User updateUserProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

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
     * @param userId 用户ID
     */
    void resendVerification(Long userId);

    /**
     * 通过邮箱重发验证邮件（防止枚举攻击，若邮箱不存在也返回成功）
     * @param email 邮箱
     * @param lang 语言
     */
    void resendVerificationByEmail(String email, String lang);

    /**
     * 发起更换邮箱流程，向新邮箱发送确认链接
     * @param userId 用户ID
     * @param newEmail 新邮箱
     * @param lang 语言
     */
    void initiateChangeEmail(Long userId, String newEmail, String lang);

    /**
     * 确认更换邮箱
     * @param token 确认令牌
     */
    void confirmChangeEmail(String token);
} 