package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Email;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户注册、登录、密码重置等认证相关接口")
@Validated
public class AuthController extends BaseController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        super(userService);
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名或邮箱登录")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户，注册后需完成邮箱验证")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @RequestParam @NotBlank(message = "刷新令牌不能为空") String refreshToken) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将当前令牌加入黑名单")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "验证邮箱", description = "使用验证令牌验证邮箱")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "验证令牌", required = true)
            @RequestParam @NotBlank(message = "验证令牌不能为空") String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "重新发送验证邮件", description = "通过邮箱重发验证邮件")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email,
            @RequestParam(required = false, defaultValue = "zh-CN") String lang) {
        userService.resendVerificationByEmail(email, lang);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 