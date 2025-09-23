package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 作业实体类
 * 对应数据库中的assignments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    private Long id;
    private Long courseId;
    private Long teacherId;
    private Long lessonId;
    /**
     * 作业类型：normal（普通）、course_bound（课程绑定-无截止）
     */
    @Builder.Default
    private String assignmentType = "normal";
    private String title;
    private String description;
    private String requirements; // 作业要求
    private BigDecimal maxScore; // 满分
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime dueDate; // 截止时间
    @Builder.Default
    private Boolean allowLate = false; // 是否允许迟交
    @Builder.Default
    private Integer maxAttempts = 1; // 最大提交次数
    private Integer maxSubmissions; // 最大提交次数的别名，与maxAttempts保持一致
    private String fileTypes; // 允许的文件类型 (json格式)
    @Builder.Default
    private String status = "draft"; // draft, published, closed
    @Builder.Default
    private Integer submissionCount = 0; // 已提交数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
    @Builder.Default
    private Boolean deleted = false;

    // 冗余字段，用于显示
    private String courseName;
    private String teacherName;

    /**
     * 检查作业是否已发布
     */
    public boolean isPublished() {
        return "published".equals(this.status);
    }

    /**
     * 检查作业是否已过期
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * 检查是否可以提交
     */
    public boolean canSubmit() {
        return isPublished() && (allowLate || !isOverdue());
    }
} 