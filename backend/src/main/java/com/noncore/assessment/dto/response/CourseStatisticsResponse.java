package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsResponse {

    private int totalCourses;
    private int publishedCourses;
    private int totalEnrollments;
    private double averageRating;
    private List<Map<String, Object>> details;
} 