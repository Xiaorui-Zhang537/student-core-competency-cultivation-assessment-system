package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private Stats stats;
    private List<RecentCourseDto> recentCourses;
    private List<PendingAssignmentDto> pendingAssignments;
    private List<RecentNotificationDto> recentNotifications;
    private Double abilityOverallScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long activeCourses;
        private long pendingAssignments;
        private double averageScore;
        private long totalStudyTime; // in minutes
        private long weeklyStudyTime; // in minutes
        private double overallProgress; // as a percentage
        private long unreadNotifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCourseDto {
        private Long id;
        private String title;
        private String teacherName;
        private double progress;
        private String coverImage;
        private String lastStudied;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingAssignmentDto {
        private Long id;
        private String title;
        private String courseTitle;
        private String dueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentNotificationDto {
        private Long id;
        private String title;
        private String content;
        private String type;
        private boolean isRead;
        private String createdAt;
    }
} 