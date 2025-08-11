package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.AssignmentAnalyticsResponse;
import com.noncore.assessment.dto.response.ClassPerformanceResponse;
import com.noncore.assessment.dto.response.CourseAnalyticsResponse;
import com.noncore.assessment.dto.response.CourseStudentPerformanceResponse;
import com.noncore.assessment.dto.response.StudentProgressReportResponse;
import java.util.Map;

public interface AnalyticsQueryService {

    StudentProgressReportResponse getStudentProgressReport(Long teacherId, Long studentId, Long courseId);

    CourseAnalyticsResponse getCourseAnalytics(Long teacherId, Long courseId, String timeRange);

    AssignmentAnalyticsResponse getAssignmentAnalytics(Long teacherId, Long assignmentId);

    ClassPerformanceResponse getClassPerformance(Long teacherId, Long courseId, String timeRange);

    Map<String, Object> getTeachingAnalytics(Long teacherId, String timeRange);
    
    Map<String, Object> generateTeachingReport(Long teacherId, Long courseId, String reportType, String timeRange);

    CourseStudentPerformanceResponse getCourseStudentPerformance(Long teacherId, Long courseId, Integer page, Integer size,
                                                                String search, String sortBy, String activityFilter, String gradeFilter, String progressFilter);
} 