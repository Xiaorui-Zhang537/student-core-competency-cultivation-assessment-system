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
public class CourseStudentPerformanceResponse {
    private Long courseId;
    private String courseTitle;
    private Integer total;
    private Integer page;
    private Integer size;
    private List<CourseStudentPerformanceItem> items;

    // Aggregated stats for the current filtered result set (not just current page)
    private Integer averageProgress; // 0-100
    private Double averageGrade;     // 0-100
    private Integer activeStudents;  // count of activityLevel != inactive
    private Double passRate;         // percent of averageGrade >= 60
}

