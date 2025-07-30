package com.noncore.assessment.entity;

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
    private String status; // draft, published, archived
    private LocalDate startDate;
    private LocalDate endDate;
    private String objectives; // 学习目标（JSON格式或分隔符分隔）
    private String requirements; // 前置要求
    private Integer enrollmentCount; // 已报名人数
    private Double rating; // 课程评分
    private Integer reviewCount; // 评价数量
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    // 默认构造方法
    public Course() {
        this.deleted = false;
        this.enrollmentCount = 0;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.status = "draft";
    }

    // 带参构造方法
    public Course(String title, String description, Long teacherId) {
        this();
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
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

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", teacherId=" + teacherId +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", status='" + status + '\'' +
                ", enrollmentCount=" + enrollmentCount +
                ", rating=" + rating +
                ", reviewCount=" + reviewCount +
                ", deleted=" + deleted +
                '}';
    }
} 