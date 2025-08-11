package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentPerformanceItem {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String avatar;
    private Integer progress; // 0-100
    private Integer completedLessons;
    private Integer totalLessons;
    private Double averageGrade;
    private String activityLevel; // high/medium/low/inactive
    private Integer studyTimePerWeek; // hours
    private String lastActiveAt;
}

