package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudentActivityResponse {
    /** 最近一次访问时间（取 enrollments.last_access_at 最大值） */
    private Date lastAccessTime;
    /** 近一周学习时长（分钟） */
    private Long weeklyStudyMinutes;
    /** 最近学习的章节列表（最多 N 条） */
    private List<RecentLessonDto> recentLessons;
    /** 统一事件流（lesson|submission|quiz|discussion|visit） */
    private List<ActivityEventDto> recentEvents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentLessonDto {
        private Long lessonId;
        private String lessonTitle;
        private Long courseId;
        private String courseTitle;
        private Date studiedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEventDto {
        private String eventType; // lesson|submission|quiz|discussion|visit
        private String title;
        private Long courseId;
        private String courseTitle;
        private Date occurredAt;
        private Integer durationSeconds; // optional
        private String link; // optional deeplink
    }
}


