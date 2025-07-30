package com.noncore.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师仪表板响应DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "教师仪表板响应")
public class TeacherDashboardResponse {

    @Schema(description = "教师基本信息")
    private TeacherProfile profile;

    @Schema(description = "教学统计数据")
    private TeachingStats teachingStats;

    @Schema(description = "活跃课程列表")
    private List<ActiveCourse> activeCourses;

    @Schema(description = "待评分作业列表")
    private List<PendingGrading> pendingGradings;

    @Schema(description = "学生概览")
    private List<StudentOverview> studentOverviews;

    @Schema(description = "最新通知列表")
    private List<RecentNotification> recentNotifications;

    @Schema(description = "教学数据图表")
    private List<TeachingData> teachingData;

    /**
     * 教师基本信息
     */
    @Schema(description = "教师基本信息")
    public static class TeacherProfile {
        @Schema(description = "教师ID", example = "1")
        private Long id;

        @Schema(description = "教师姓名", example = "张教授")
        private String name;

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "职位", example = "副教授")
        private String title;

        @Schema(description = "学院", example = "计算机学院")
        private String department;

        @Schema(description = "专业领域", example = "机器学习、数据挖掘")
        private String specialization;

        @Schema(description = "工作年限", example = "8")
        private Integer yearsOfExperience;

        @Schema(description = "联系邮箱", example = "zhang@university.edu")
        private String email;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getSpecialization() { return specialization; }
        public void setSpecialization(String specialization) { this.specialization = specialization; }
        public Integer getYearsOfExperience() { return yearsOfExperience; }
        public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * 教学统计数据
     */
    @Schema(description = "教学统计数据")
    public static class TeachingStats {
        @Schema(description = "活跃课程数量", example = "3")
        private Integer activeCourses;

        @Schema(description = "总学生数量", example = "120")
        private Integer totalStudents;

        @Schema(description = "待评分作业数量", example = "15")
        private Integer pendingGradings;

        @Schema(description = "本月发布作业数量", example = "8")
        private Integer monthlyAssignments;

        @Schema(description = "平均作业分数", example = "82.5")
        private BigDecimal averageAssignmentScore;

        @Schema(description = "课程平均评分", example = "4.6")
        private BigDecimal averageCourseRating;

        @Schema(description = "学生完成率", example = "85.2")
        private BigDecimal studentCompletionRate;

        @Schema(description = "本周活跃学生数", example = "98")
        private Integer weeklyActiveStudents;

        // Getter和Setter方法
        public Integer getActiveCourses() { return activeCourses; }
        public void setActiveCourses(Integer activeCourses) { this.activeCourses = activeCourses; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public Integer getPendingGradings() { return pendingGradings; }
        public void setPendingGradings(Integer pendingGradings) { this.pendingGradings = pendingGradings; }
        public Integer getMonthlyAssignments() { return monthlyAssignments; }
        public void setMonthlyAssignments(Integer monthlyAssignments) { this.monthlyAssignments = monthlyAssignments; }
        public BigDecimal getAverageAssignmentScore() { return averageAssignmentScore; }
        public void setAverageAssignmentScore(BigDecimal averageAssignmentScore) { this.averageAssignmentScore = averageAssignmentScore; }
        public BigDecimal getAverageCourseRating() { return averageCourseRating; }
        public void setAverageCourseRating(BigDecimal averageCourseRating) { this.averageCourseRating = averageCourseRating; }
        public BigDecimal getStudentCompletionRate() { return studentCompletionRate; }
        public void setStudentCompletionRate(BigDecimal studentCompletionRate) { this.studentCompletionRate = studentCompletionRate; }
        public Integer getWeeklyActiveStudents() { return weeklyActiveStudents; }
        public void setWeeklyActiveStudents(Integer weeklyActiveStudents) { this.weeklyActiveStudents = weeklyActiveStudents; }
    }

    /**
     * 活跃课程信息
     */
    @Schema(description = "活跃课程信息")
    public static class ActiveCourse {
        @Schema(description = "课程ID", example = "1")
        private Long id;

        @Schema(description = "课程标题", example = "高等数学基础")
        private String title;

        @Schema(description = "注册学生数", example = "45")
        private Integer enrolledStudents;

        @Schema(description = "课程进度", example = "65.0")
        private BigDecimal progress;

        @Schema(description = "待评分作业数", example = "8")
        private Integer pendingGrades;

        @Schema(description = "活跃学生数", example = "42")
        private Integer activeStudents;

        @Schema(description = "最后更新时间", example = "2024-12-28 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUpdated;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getEnrolledStudents() { return enrolledStudents; }
        public void setEnrolledStudents(Integer enrolledStudents) { this.enrolledStudents = enrolledStudents; }
        public BigDecimal getProgress() { return progress; }
        public void setProgress(BigDecimal progress) { this.progress = progress; }
        public Integer getPendingGrades() { return pendingGrades; }
        public void setPendingGrades(Integer pendingGrades) { this.pendingGrades = pendingGrades; }
        public Integer getActiveStudents() { return activeStudents; }
        public void setActiveStudents(Integer activeStudents) { this.activeStudents = activeStudents; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    /**
     * 待评分作业信息
     */
    @Schema(description = "待评分作业信息")
    public static class PendingGrading {
        @Schema(description = "提交ID", example = "1")
        private Long submissionId;

        @Schema(description = "作业标题", example = "第一次作业")
        private String assignmentTitle;

        @Schema(description = "学生姓名", example = "张三")
        private String studentName;

        @Schema(description = "课程名称", example = "高等数学基础")
        private String courseName;

        @Schema(description = "提交时间", example = "2024-12-27 14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submittedAt;

        @Schema(description = "待评时长（小时）", example = "18")
        private Integer hoursWaiting;

        @Schema(description = "紧急程度", example = "normal", allowableValues = {"low", "normal", "high", "urgent"})
        private String priority;

        // Getter和Setter方法
        public Long getSubmissionId() { return submissionId; }
        public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
        public Integer getHoursWaiting() { return hoursWaiting; }
        public void setHoursWaiting(Integer hoursWaiting) { this.hoursWaiting = hoursWaiting; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    /**
     * 学生概览信息
     */
    @Schema(description = "学生概览信息")
    public static class StudentOverview {
        @Schema(description = "学生ID", example = "1")
        private Long id;

        @Schema(description = "学生姓名", example = "张三")
        private String name;

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "平均成绩", example = "85.5")
        private BigDecimal averageGrade;

        @Schema(description = "学习进度", example = "78.0")
        private BigDecimal progress;

        @Schema(description = "活跃状态", example = "active", allowableValues = {"active", "inactive", "at_risk"})
        private String status;

        @Schema(description = "最后活跃时间", example = "2024-12-28 09:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastActiveAt;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public BigDecimal getAverageGrade() { return averageGrade; }
        public void setAverageGrade(BigDecimal averageGrade) { this.averageGrade = averageGrade; }
        public BigDecimal getProgress() { return progress; }
        public void setProgress(BigDecimal progress) { this.progress = progress; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getLastActiveAt() { return lastActiveAt; }
        public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    }

    /**
     * 最新通知信息
     */
    @Schema(description = "最新通知信息")
    public static class RecentNotification {
        @Schema(description = "通知ID", example = "1")
        private Long id;

        @Schema(description = "通知标题", example = "新学生注册")
        private String title;

        @Schema(description = "通知内容", example = "有新学生注册了您的课程")
        private String content;

        @Schema(description = "通知类型", example = "enrollment")
        private String type;

        @Schema(description = "是否已读", example = "false")
        private Boolean isRead;

        @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    /**
     * 教学数据（用于图表）
     */
    @Schema(description = "教学数据")
    public static class TeachingData {
        @Schema(description = "日期", example = "2024-12-28")
        private String date;

        @Schema(description = "学生活跃数", example = "85")
        private Integer activeStudents;

        @Schema(description = "作业提交数", example = "32")
        private Integer submissionsCount;

        @Schema(description = "平均分数", example = "82.5")
        private BigDecimal averageScore;

        // Getter和Setter方法
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Integer getActiveStudents() { return activeStudents; }
        public void setActiveStudents(Integer activeStudents) { this.activeStudents = activeStudents; }
        public Integer getSubmissionsCount() { return submissionsCount; }
        public void setSubmissionsCount(Integer submissionsCount) { this.submissionsCount = submissionsCount; }
        public BigDecimal getAverageScore() { return averageScore; }
        public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
    }

    // 主类的Getter和Setter方法
    public TeacherProfile getProfile() { return profile; }
    public void setProfile(TeacherProfile profile) { this.profile = profile; }
    public TeachingStats getTeachingStats() { return teachingStats; }
    public void setTeachingStats(TeachingStats teachingStats) { this.teachingStats = teachingStats; }
    public List<ActiveCourse> getActiveCourses() { return activeCourses; }
    public void setActiveCourses(List<ActiveCourse> activeCourses) { this.activeCourses = activeCourses; }
    public List<PendingGrading> getPendingGradings() { return pendingGradings; }
    public void setPendingGradings(List<PendingGrading> pendingGradings) { this.pendingGradings = pendingGradings; }
    public List<StudentOverview> getStudentOverviews() { return studentOverviews; }
    public void setStudentOverviews(List<StudentOverview> studentOverviews) { this.studentOverviews = studentOverviews; }
    public List<RecentNotification> getRecentNotifications() { return recentNotifications; }
    public void setRecentNotifications(List<RecentNotification> recentNotifications) { this.recentNotifications = recentNotifications; }
    public List<TeachingData> getTeachingData() { return teachingData; }
    public void setTeachingData(List<TeachingData> teachingData) { this.teachingData = teachingData; }
} 