package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.AssignmentAnalyticsResponse;
import com.noncore.assessment.dto.response.ClassPerformanceResponse;
import com.noncore.assessment.dto.response.CourseAnalyticsResponse;
import com.noncore.assessment.dto.response.CourseStudentPerformanceResponse;

public interface AnalyticsQueryService {

    // Removed legacy single-student progress report method

    CourseAnalyticsResponse getCourseAnalytics(Long teacherId, Long courseId, String timeRange);

    AssignmentAnalyticsResponse getAssignmentAnalytics(Long teacherId, Long assignmentId);

    ClassPerformanceResponse getClassPerformance(Long teacherId, Long courseId, String timeRange);

    // Removed unused analytics placeholders to simplify API surface

    CourseStudentPerformanceResponse getCourseStudentPerformance(Long teacherId, Long courseId, Integer page, Integer size,
                                                                String search, String sortBy, String activityFilter, String gradeFilter, String progressFilter);
} 