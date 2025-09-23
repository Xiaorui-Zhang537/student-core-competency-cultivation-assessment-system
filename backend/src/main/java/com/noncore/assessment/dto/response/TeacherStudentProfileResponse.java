package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudentProfileResponse {
    private Long id;
    /** 首选展示名（优先 nickname，其次 lastName+firstName，否则 username） */
    private String name;

    // 基本档案（来自 users 表）
    private String username;
    private String avatar;
    private String email;
    private Boolean emailVerified;
    private String studentNo;
    private String firstName;
    private String lastName;
    private String nickname;
    private String gender;
    private String mbti;
    private String bio;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String phone;
    private String school;      // 也作为 className 的来源
    private String subject;
    private String grade;
    private Date createdAt;
    private Date updatedAt;

    // 汇总指标
    private String className;   // 兼容字段（沿用原有）
    private Integer enrolledCourseCount;
    private Double averageScore;
    private Double completionRate;   // 总体学习完成率（0-100）
    private Date lastAccessTime;     // 最近访问时间（取 enrollments.last_access_at 最大值）

    // 预留扩展（可空）
    private Integer rank;            // 可为 null
    private Double percentile;       // 可为 null
}


