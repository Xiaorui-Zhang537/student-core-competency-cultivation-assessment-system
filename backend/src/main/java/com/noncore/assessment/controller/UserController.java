package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.ChangePasswordRequest;
import com.noncore.assessment.dto.request.ResetPasswordRequest;
import com.noncore.assessment.dto.request.UpdateProfileRequest;
import com.noncore.assessment.dto.response.UserProfileResponse;
import com.noncore.assessment.dto.request.UpdateAvatarRequest;
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

    // 邮箱验证与重发移动至 AuthController，以下新增邮箱更换相关接口

    @PostMapping("/me/email/change-initiate")
    @Operation(summary = "发起更换邮箱", description = "向新邮箱发送确认链接，确认后才生效")
    public ResponseEntity<ApiResponse<Void>> changeEmailInitiate(
            @Parameter(description = "新邮箱", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") @NotBlank(message = "新邮箱不能为空") String newEmail,
            @RequestParam(required = false, defaultValue = "zh-CN") String lang) {
        userService.initiateChangeEmail(getCurrentUserId(), newEmail, lang);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/email/change/confirm")
    @Operation(summary = "确认更换邮箱", description = "使用令牌确认更换邮箱")
    public ResponseEntity<ApiResponse<Void>> changeEmailConfirm(
            @Parameter(description = "确认令牌", required = true)
            @RequestParam @NotBlank(message = "确认令牌不能为空") String token) {
        userService.confirmChangeEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/avatar")
    @Operation(summary = "更新头像", description = "使用已上传的文件ID设置为当前用户头像，并清理旧头像文件")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        userService.updateAvatar(getCurrentUserId(), request.getFileId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}