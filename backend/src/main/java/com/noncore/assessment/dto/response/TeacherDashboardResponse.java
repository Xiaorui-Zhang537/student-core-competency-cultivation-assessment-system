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
public class TeacherDashboardResponse {

    private Stats stats;
    private List<ActiveCourseDto> activeCourses;
    private List<PendingGradingDto> pendingGradings;
    private List<StudentOverviewDto> studentOverviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long activeCourses;
        private long totalStudents;
        private long pendingGradingsCount;
        private long monthlyAssignments;
        private double averageAssignmentScore;
        private double averageCourseRating;
        private double studentCompletionRate;
        private long weeklyActiveStudents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveCourseDto {
        private Long id;
        private String title;
        private String updatedAt;
        private long enrolledStudents;
        private long activeStudents;
        private double avgProgress;
        private long pendingGrades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingGradingDto {
        private Long submissionId;
        private String assignmentTitle;
        private String studentName;
        private String courseTitle;
        private String submittedAt;
        private String dueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentOverviewDto {
        private Long id;
        private String displayName;
        private String avatar;
        private double avgGrade;
        private double avgProgress;
        private String lastActive;
    }
} 