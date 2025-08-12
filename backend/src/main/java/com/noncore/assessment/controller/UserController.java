package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.ChangePasswordRequest;
import com.noncore.assessment.dto.request.ResetPasswordRequest;
import com.noncore.assessment.dto.request.UpdateProfileRequest;
import com.noncore.assessment.dto.response.UserProfileResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "Endpoints for user profile, password management, and email verification.")
@Validated
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取用户资料", description = "获取当前登录用户的个人资料")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        User user = getCurrentUser();
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .bio(user.getBio())
                .emailVerified(user.isEmailVerified())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .school(user.getSchool())
                .subject(user.getSubject())
                .studentNo(user.getStudentNo())
                .teacherNo(user.getTeacherNo())
                .birthday(user.getBirthday() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(user.getBirthday()) : null)
                .country(user.getCountry())
                .province(user.getProvince())
                .city(user.getCity())
                .phone(user.getPhone())
                .build();
        return ResponseEntity.ok(ApiResponse.success(profileResponse));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的个人资料")
    public ResponseEntity<ApiResponse<User>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateUserProfile(getCurrentUserId(), request);
        updatedUser.setPassword(null); // 确保密码不返回
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(getCurrentUserId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") @NotBlank(message = "邮箱不能为空") String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "使用重置令牌重置密码")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
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
    @Operation(summary = "重新发送验证邮件", description = "重新发送邮箱验证邮件")
    public ResponseEntity<ApiResponse<Void>> resendVerification() {
        userService.resendVerification(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

} 