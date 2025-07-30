package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库users表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @JsonIgnore
    @Schema(description = "密码", hidden = true)
    private String password;

    @Schema(description = "用户角色", example = "student", allowableValues = {"student", "teacher", "admin"})
    private String role;

    @Schema(description = "头像URL", example = "https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan")
    private String avatar;

    @Schema(description = "姓氏", example = "张")
    private String firstName;

    @Schema(description = "名字", example = "三")
    private String lastName;

    @Schema(description = "显示名称", example = "张三")
    private String displayName;

    @Schema(description = "个人简介", example = "热爱学习的学生")
    private String bio;

    @Schema(description = "年级（学生）", example = "2023级")
    private String grade;

    @Schema(description = "专业/科目", example = "计算机科学")
    private String subject;

    @Schema(description = "学校/学院", example = "计算机学院")
    private String school;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Schema(description = "是否删除", hidden = true)
    private Boolean deleted;

    @Schema(description = "邮箱是否已验证", example = "true")
    private Boolean emailVerified;

    /**
     * 默认构造方法
     */
    public User() {}

    /**
     * 构造方法
     */
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取完整姓名
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + lastName;
        }
        return displayName != null ? displayName : username;
    }

    /**
     * 是否是学生
     */
    public boolean isStudent() {
        return "student".equals(role);
    }

    /**
     * 是否是教师
     */
    public boolean isTeacher() {
        return "teacher".equals(role);
    }

    /**
     * 是否是管理员
     */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", displayName='" + displayName + '\'' +
                ", school='" + school + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 