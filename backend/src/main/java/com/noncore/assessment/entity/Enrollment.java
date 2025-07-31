package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 选课记录实体类
 * 对应数据库中的enrollments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    private Long id;
    private Long studentId;
    private Long courseId;
    @Builder.Default
    private String status = "enrolled"; // enrolled, completed, dropped, failed
    @Builder.Default
    private Double progress = 0.0; // 课程进度百分比 (0.0-100.0)
    private Double grade; // 课程成绩
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessAt;
    private String notes; // 备注信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 冗余字段，用于联表查询时显示
    private String studentName;
    private String courseName;
    private String teacherName;
    
    /**
     * 检查是否已完成课程
     */
    public boolean isCompleted() {
        return "completed".equals(this.status);
    }

    /**
     * 检查是否已退课
     */
    public boolean isDropped() {
        return "dropped".equals(this.status);
    }

    /**
     * 检查是否正在学习
     */
    public boolean isActive() {
        return "enrolled".equals(this.status);
    }

    /**
     * 更新学习进度
     */
    public void updateProgress(Double newProgress) {
        if (newProgress != null && newProgress >= 0 && newProgress <= 100) {
            this.progress = newProgress;
            this.lastAccessAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            
            // 如果进度达到100%，自动设置为完成状态
            if (newProgress >= 100.0 && !"completed".equals(this.status)) {
                this.status = "completed";
                this.completedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * 设置课程成绩
     */
    public void setGradeAndComplete(Double grade) {
        this.grade = grade;
        if (grade != null) {
            this.status = grade >= 60 ? "completed" : "failed";
            this.completedAt = LocalDateTime.now();
            this.progress = 100.0;
        }
        this.updatedAt = LocalDateTime.now();
    }
} 