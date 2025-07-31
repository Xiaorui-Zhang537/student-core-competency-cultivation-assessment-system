package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.*;
import com.noncore.assessment.service.AnalyticsQueryService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@Tag(name = "Teacher Management", description = "Endpoints for teachers to manage courses, students, and analytics.")
public class TeacherController extends BaseController {

    private final AnalyticsQueryService analyticsQueryService;

    public TeacherController(AnalyticsQueryService analyticsQueryService, UserService userService) {
        super(userService);
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/analytics/student-progress")
    @Operation(summary = "获取特定学生的详细进度报告")
    public ResponseEntity<ApiResponse<StudentProgressReportResponse>> getStudentProgressReport(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @Parameter(description = "课程ID（可选）") @RequestParam(required = false) Long courseId) {
        Long teacherId = getCurrentUserId();
        StudentProgressReportResponse report = analyticsQueryService.getStudentProgressReport(teacherId, studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/analytics/course/{courseId}")
    @Operation(summary = "获取课程分析数据")
    public ResponseEntity<ApiResponse<CourseAnalyticsResponse>> getCourseAnalytics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "时间范围") @RequestParam(required = false) String timeRange) {
        Long teacherId = getCurrentUserId();
        CourseAnalyticsResponse analytics = analyticsQueryService.getCourseAnalytics(teacherId, courseId, timeRange);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
    
    @GetMapping("/analytics/assignment/{assignmentId}")
    @Operation(summary = "获取作业分析数据")
    public ResponseEntity<ApiResponse<AssignmentAnalyticsResponse>> getAssignmentAnalytics(
            @Parameter(description = "作业ID") @PathVariable Long assignmentId) {
        Long teacherId = getCurrentUserId();
        AssignmentAnalyticsResponse analytics = analyticsQueryService.getAssignmentAnalytics(teacherId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/analytics/class-performance/{courseId}")
    @Operation(summary = "获取班级整体表现数据")
    public ResponseEntity<ApiResponse<ClassPerformanceResponse>> getClassPerformance(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "时间范围") @RequestParam(required = false, defaultValue = "last_30_days") String timeRange) {
        Long teacherId = getCurrentUserId();
        ClassPerformanceResponse performance = analyticsQueryService.getClassPerformance(teacherId, courseId, timeRange);
        return ResponseEntity.ok(ApiResponse.success(performance));
    }
} 