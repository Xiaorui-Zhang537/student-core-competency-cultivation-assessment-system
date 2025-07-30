package com.noncore.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生仪表板响应DTO
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "学生仪表板响应")
public class StudentDashboardResponse {

    @Schema(description = "学生基本信息")
    private StudentProfile profile;

    @Schema(description = "学习统计数据")
    private StudyStats studyStats;

    @Schema(description = "最近课程列表")
    private List<RecentCourse> recentCourses;

    @Schema(description = "待完成作业列表")
    private List<PendingAssignment> pendingAssignments;

    @Schema(description = "最新通知列表")
    private List<RecentNotification> recentNotifications;

    @Schema(description = "学习进度数据")
    private List<ProgressData> progressData;

    @Schema(description = "能力评估概览")
    private AbilityOverview abilityOverview;

    /**
     * 学生基本信息
     */
    @Schema(description = "学生基本信息")
    public static class StudentProfile {
        @Schema(description = "学生ID", example = "1")
        private Long id;

        @Schema(description = "学生姓名", example = "张三")
        private String name;

        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "年级", example = "2023级")
        private String grade;

        @Schema(description = "专业", example = "计算机科学")
        private String major;

        @Schema(description = "学号", example = "20230001")
        private String studentNumber;

        @Schema(description = "入学时间", example = "2023-09-01 08:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime enrollmentDate;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
        public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
        public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    }

    /**
     * 学习统计数据
     */
    @Schema(description = "学习统计数据")
    public static class StudyStats {
        @Schema(description = "活跃课程数量", example = "5")
        private Integer activeCourses;

        @Schema(description = "待完成作业数量", example = "3")
        private Integer pendingAssignments;

        @Schema(description = "平均成绩", example = "85.5")
        private BigDecimal averageScore;

        @Schema(description = "总学习时长（分钟）", example = "1800")
        private Integer totalStudyTime;

        @Schema(description = "本周学习时长（分钟）", example = "300")
        private Integer weeklyStudyTime;

        @Schema(description = "整体学习进度（百分比）", example = "75.0")
        private BigDecimal overallProgress;

        @Schema(description = "已完成课程数量", example = "2")
        private Integer completedCourses;

        @Schema(description = "未读通知数量", example = "5")
        private Integer unreadNotifications;

        // Getter和Setter方法
        public Integer getActiveCourses() { return activeCourses; }
        public void setActiveCourses(Integer activeCourses) { this.activeCourses = activeCourses; }
        public Integer getPendingAssignments() { return pendingAssignments; }
        public void setPendingAssignments(Integer pendingAssignments) { this.pendingAssignments = pendingAssignments; }
        public BigDecimal getAverageScore() { return averageScore; }
        public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
        public Integer getTotalStudyTime() { return totalStudyTime; }
        public void setTotalStudyTime(Integer totalStudyTime) { this.totalStudyTime = totalStudyTime; }
        public Integer getWeeklyStudyTime() { return weeklyStudyTime; }
        public void setWeeklyStudyTime(Integer weeklyStudyTime) { this.weeklyStudyTime = weeklyStudyTime; }
        public BigDecimal getOverallProgress() { return overallProgress; }
        public void setOverallProgress(BigDecimal overallProgress) { this.overallProgress = overallProgress; }
        public Integer getCompletedCourses() { return completedCourses; }
        public void setCompletedCourses(Integer completedCourses) { this.completedCourses = completedCourses; }
        public Integer getUnreadNotifications() { return unreadNotifications; }
        public void setUnreadNotifications(Integer unreadNotifications) { this.unreadNotifications = unreadNotifications; }
    }

    /**
     * 最近课程信息
     */
    @Schema(description = "最近课程信息")
    public static class RecentCourse {
        @Schema(description = "课程ID", example = "1")
        private Long id;

        @Schema(description = "课程标题", example = "高等数学基础")
        private String title;

        @Schema(description = "教师姓名", example = "张教授")
        private String teacherName;

        @Schema(description = "学习进度（百分比）", example = "75.0")
        private BigDecimal progress;

        @Schema(description = "最后学习时间", example = "2024-12-28 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastStudiedAt;

        @Schema(description = "课程封面", example = "https://example.com/course.jpg")
        private String coverImage;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public BigDecimal getProgress() { return progress; }
        public void setProgress(BigDecimal progress) { this.progress = progress; }
        public LocalDateTime getLastStudiedAt() { return lastStudiedAt; }
        public void setLastStudiedAt(LocalDateTime lastStudiedAt) { this.lastStudiedAt = lastStudiedAt; }
        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    }

    /**
     * 待完成作业信息
     */
    @Schema(description = "待完成作业信息")
    public static class PendingAssignment {
        @Schema(description = "作业ID", example = "1")
        private Long id;

        @Schema(description = "作业标题", example = "第一次作业")
        private String title;

        @Schema(description = "课程名称", example = "高等数学基础")
        private String courseName;

        @Schema(description = "截止时间", example = "2024-12-30 23:59:59")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dueDate;

        @Schema(description = "剩余天数", example = "2")
        private Integer daysLeft;

        @Schema(description = "紧急程度", example = "high", allowableValues = {"low", "normal", "high", "urgent"})
        private String urgency;

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        public Integer getDaysLeft() { return daysLeft; }
        public void setDaysLeft(Integer daysLeft) { this.daysLeft = daysLeft; }
        public String getUrgency() { return urgency; }
        public void setUrgency(String urgency) { this.urgency = urgency; }
    }

    /**
     * 最新通知信息
     */
    @Schema(description = "最新通知信息")
    public static class RecentNotification {
        @Schema(description = "通知ID", example = "1")
        private Long id;

        @Schema(description = "通知标题", example = "新作业发布")
        private String title;

        @Schema(description = "通知内容", example = "您有一份新的作业需要完成")
        private String content;

        @Schema(description = "通知类型", example = "assignment")
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
     * 学习进度数据（用于图表）
     */
    @Schema(description = "学习进度数据")
    public static class ProgressData {
        @Schema(description = "日期", example = "2024-12-28")
        private String date;

        @Schema(description = "学习时长（分钟）", example = "120")
        private Integer studyTime;

        @Schema(description = "完成的章节数", example = "3")
        private Integer completedLessons;

        // Getter和Setter方法
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Integer getStudyTime() { return studyTime; }
        public void setStudyTime(Integer studyTime) { this.studyTime = studyTime; }
        public Integer getCompletedLessons() { return completedLessons; }
        public void setCompletedLessons(Integer completedLessons) { this.completedLessons = completedLessons; }
    }

    /**
     * 能力评估概览
     */
    @Schema(description = "能力评估概览")
    public static class AbilityOverview {
        @Schema(description = "总体评分", example = "85.0")
        private BigDecimal overallScore;

        @Schema(description = "排名", example = "15")
        private Integer ranking;

        @Schema(description = "能力维度评分")
        private List<DimensionScore> dimensionScores;

        @Schema(description = "能力维度评分")
        public static class DimensionScore {
            @Schema(description = "维度名称", example = "逻辑思维")
            private String name;

            @Schema(description = "评分", example = "88.0")
            private BigDecimal score;

            // Getter和Setter方法
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public BigDecimal getScore() { return score; }
            public void setScore(BigDecimal score) { this.score = score; }
        }

        // Getter和Setter方法
        public BigDecimal getOverallScore() { return overallScore; }
        public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }
        public Integer getRanking() { return ranking; }
        public void setRanking(Integer ranking) { this.ranking = ranking; }
        public List<DimensionScore> getDimensionScores() { return dimensionScores; }
        public void setDimensionScores(List<DimensionScore> dimensionScores) { this.dimensionScores = dimensionScores; }
    }

    // 主类的Getter和Setter方法
    public StudentProfile getProfile() { return profile; }
    public void setProfile(StudentProfile profile) { this.profile = profile; }
    public StudyStats getStudyStats() { return studyStats; }
    public void setStudyStats(StudyStats studyStats) { this.studyStats = studyStats; }
    public List<RecentCourse> getRecentCourses() { return recentCourses; }
    public void setRecentCourses(List<RecentCourse> recentCourses) { this.recentCourses = recentCourses; }
    public List<PendingAssignment> getPendingAssignments() { return pendingAssignments; }
    public void setPendingAssignments(List<PendingAssignment> pendingAssignments) { this.pendingAssignments = pendingAssignments; }
    public List<RecentNotification> getRecentNotifications() { return recentNotifications; }
    public void setRecentNotifications(List<RecentNotification> recentNotifications) { this.recentNotifications = recentNotifications; }
    public List<ProgressData> getProgressData() { return progressData; }
    public void setProgressData(List<ProgressData> progressData) { this.progressData = progressData; }
    public AbilityOverview getAbilityOverview() { return abilityOverview; }
    public void setAbilityOverview(AbilityOverview abilityOverview) { this.abilityOverview = abilityOverview; }
} 