package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 认证控制器
 * 提供用户认证相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户注册、登录、密码重置等认证相关接口")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名或邮箱登录")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success("注册成功", authResponse));
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam @NotBlank(message = "刷新令牌不能为空") String refreshToken) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", authResponse));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将当前令牌加入黑名单")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取当前登录用户的个人资料")
    public ResponseEntity<ApiResponse<User>> getProfile() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户未登录");
        }
        User profile = authService.getUserProfile(username);
        return ResponseEntity.ok(ApiResponse.success("获取成功", profile));
    }

    /**
     * 更新用户个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的个人资料")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@Valid @RequestBody User user) {
        String username = getCurrentUsername();
        user.setUsername(username);
        authService.updateUserProfile(user);
        return ResponseEntity.ok(ApiResponse.success("更新成功"));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String username = getCurrentUsername();
        authService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("密码重置邮件已发送"));
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "使用重置令牌重置密码")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
    }

    /**
     * 验证邮箱
     */
    @PostMapping("/verify-email")
    @Operation(summary = "验证邮箱", description = "使用验证令牌验证邮箱")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "验证令牌", required = true)
            @RequestParam @NotBlank(message = "验证令牌不能为空") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("邮箱验证成功"));
    }

    /**
     * 重新发送验证邮件
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "重新发送验证邮件", description = "重新发送邮箱验证邮件")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email) {
        authService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success("验证邮件已重新发送"));
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = authService.isUsernameExists(username);
            return ApiResponse.success("检查完成", exists);
        } catch (Exception e) {
            logger.error("检查用户名失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被注册")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = authService.isEmailExists(email);
            return ApiResponse.success("检查完成", exists);
        } catch (Exception e) {
            logger.error("检查邮箱失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 修改密码请求DTO
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    /**
     * 忘记密码请求DTO
     */
    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 重置密码请求DTO
     */
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        private String confirmPassword;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    /**
     * 邮箱验证请求DTO
     */
    public static class EmailVerifyRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
} 