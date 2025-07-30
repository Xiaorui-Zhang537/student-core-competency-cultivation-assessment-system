package com.noncore.assessment.entity;

import java.time.LocalDateTime;

/**
 * 作业提交实体类
 * 对应数据库中的submissions表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class Submission {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content; // 提交内容
    private String fileName; // 提交文件名
    private String filePath; // 文件存储路径
    private Integer submissionCount; // 第几次提交
    private String status; // submitted, graded, returned
    private LocalDateTime submittedAt;
    private Boolean isLate; // 是否迟交
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 冗余字段，用于显示
    private String assignmentTitle;
    private String studentName;
    private String courseName;

    // 默认构造方法
    public Submission() {
        this.status = "submitted";
        this.isLate = false;
        this.submissionCount = 1;
    }

    // 带参构造方法
    public Submission(Long assignmentId, Long studentId, String content) {
        this();
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
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

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
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

    /**
     * 检查是否已批改
     */
    public boolean isGraded() {
        return "graded".equals(this.status);
    }

    /**
     * 检查是否已提交
     */
    public boolean isSubmitted() {
        return "submitted".equals(this.status) || "graded".equals(this.status);
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", status='" + status + '\'' +
                ", submissionCount=" + submissionCount +
                ", submittedAt=" + submittedAt +
                ", isLate=" + isLate +
                '}';
    }
} 