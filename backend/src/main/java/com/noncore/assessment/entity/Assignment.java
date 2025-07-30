package com.noncore.assessment.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 作业实体类
 * 对应数据库中的assignments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class Assignment {

    private Long id;
    private Long courseId;
    private Long teacherId;
    private String title;
    private String description;
    private String requirements; // 作业要求
    private BigDecimal maxScore; // 满分
    private LocalDateTime dueDate; // 截止时间
    private Boolean allowLate; // 是否允许迟交
    private Integer maxAttempts; // 最大提交次数
    private Integer maxSubmissions; // 最大提交次数的别名，与maxAttempts保持一致
    private String fileTypes; // 允许的文件类型 (json格式)
    private String status; // draft, published, closed
    private Integer submissionCount; // 已提交数量
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    // 冗余字段，用于显示
    private String courseName;
    private String teacherName;

    // 默认构造方法
    public Assignment() {
        this.deleted = false;
        this.allowLate = false;
        this.maxAttempts = 1;
        this.submissionCount = 0;
        this.status = "draft";
    }

    // 带参构造方法
    public Assignment(Long courseId, Long teacherId, String title) {
        this();
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.title = title;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getAllowLate() {
        return allowLate;
    }

    public void setAllowLate(Boolean allowLate) {
        this.allowLate = allowLate;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.maxSubmissions = maxAttempts; // 保持同步
    }

    public Integer getMaxSubmissions() {
        return maxSubmissions != null ? maxSubmissions : maxAttempts;
    }

    public void setMaxSubmissions(Integer maxSubmissions) {
        this.maxSubmissions = maxSubmissions;
        this.maxAttempts = maxSubmissions; // 保持同步
    }

    public String getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(String fileTypes) {
        this.fileTypes = fileTypes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

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

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                ", submissionCount=" + submissionCount +
                '}';
    }
} 