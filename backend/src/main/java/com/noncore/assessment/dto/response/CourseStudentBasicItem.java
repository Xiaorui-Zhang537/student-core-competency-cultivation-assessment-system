package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentBasicItem {
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String avatar;
    private Double progress;      // 0-100
    private Integer completedLessons;
    private Integer totalLessons;
    private Double averageGrade;  // 0-100
    private String activityLevel; // 可选：HIGH/MEDIUM/LOW
    private Long lastActiveAt;    // epoch millis
    private Long joinedAt;        // epoch millis
}


