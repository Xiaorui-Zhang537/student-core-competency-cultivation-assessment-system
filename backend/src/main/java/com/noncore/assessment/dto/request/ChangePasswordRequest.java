package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改密码请求DTO
 * 用于接收修改密码表单数据
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Setter
@Getter
public class ChangePasswordRequest {

    // Getter和Setter方法
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度必须在6-50个字符之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // 默认构造方法
    public ChangePasswordRequest() {}

    // 带参构造方法
    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    /**
     * 检查新密码一致性
     */
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
} 