package com.noncore.assessment.entity;

import java.time.LocalDateTime;

/**
 * 选课记录实体类
 * 对应数据库中的enrollments表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class Enrollment {

    private Long id;
    private Long studentId;
    private Long courseId;
    private String status; // enrolled, completed, dropped, failed
    private Double progress; // 课程进度百分比 (0.0-100.0)
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

    // 默认构造方法
    public Enrollment() {
        this.progress = 0.0;
        this.status = "enrolled";
    }

    // 带参构造方法
    public Enrollment(Long studentId, Long courseId) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 完整构造方法
    public Enrollment(Long studentId, Long courseId, String status, LocalDateTime enrolledAt) {
        this(studentId, courseId);
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                ", grade=" + grade +
                ", enrolledAt=" + enrolledAt +
                '}';
    }
} 