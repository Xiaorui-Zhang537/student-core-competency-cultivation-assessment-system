package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "角色", example = "student")
    private String role;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "MBTI 人格类型(16种)")
    private String mbti;

    @Schema(description = "名字")
    private String firstName;

    @Schema(description = "姓氏")
    private String lastName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "工号")
    private String teacherNo;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "专业/科目")
    private String subject;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "电话号码")
    private String phone;

    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省份/州")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;

    @Schema(description = "是否已删除", example = "false")
    private boolean deleted;

    @Schema(description = "邮箱是否已验证", example = "false")
    private boolean emailVerified;

    public void initialize() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.deleted = false;
        this.emailVerified = false;
        this.status = "active";
    }

    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
}