package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应数据库中的courses表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private Long teacherId;
    private String teacherName; // 冗余字段，用于显示
    private String category;
    private String difficulty; // beginner, intermediate, advanced
    private Integer duration; // 预计学习时长（小时）
    private Integer maxStudents;
    @Builder.Default
    private String status = "draft"; // draft, published, archived
    private LocalDate startDate;
    private LocalDate endDate;
    private String objectives; // 学习目标（JSON格式或分隔符分隔）
    private String requirements; // 前置要求
    @Builder.Default
    private Integer enrollmentCount = 0; // 已报名人数
    @Builder.Default
    private Double rating = 0.0; // 课程评分
    @Builder.Default
    private Integer reviewCount = 0; // 评价数量
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private Boolean deleted = false;
    
    /**
     * 检查课程是否已发布
     */
    public boolean isPublished() {
        return "published".equals(this.status);
    }

    /**
     * 检查课程是否已满员
     */
    public boolean isFull() {
        return maxStudents != null && enrollmentCount != null && enrollmentCount >= maxStudents;
    }

    /**
     * 检查课程是否在进行中
     */
    public boolean isInProgress() {
        if (startDate == null) return false;
        LocalDate now = LocalDate.now();
        if (endDate == null) {
            return !now.isBefore(startDate);
        }
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
} 