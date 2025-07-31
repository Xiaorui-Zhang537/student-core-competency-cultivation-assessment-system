package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalyticsResponse {
    private Course course;
    private int totalStudents;
    private int activeStudents;
    private int totalAssignments;
    private BigDecimal completionRate;
    private BigDecimal averageScore;
    private List<Map<String, Object>> timeSeriesData;
} 