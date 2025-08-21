package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.noncore.assessment.service.AnalyticsQueryService;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
@Tag(name = "Teacher Management", description = "Endpoints for teachers to manage courses, students, and analytics.")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class TeacherController extends BaseController {

    private final AnalyticsQueryService analyticsQueryService;
    private final EnrollmentService enrollmentService;

    public TeacherController(AnalyticsQueryService analyticsQueryService, UserService userService, EnrollmentService enrollmentService) {
        super(userService);
        this.analyticsQueryService = analyticsQueryService;
        this.enrollmentService = enrollmentService;
    }

    // Removed legacy single-student progress endpoint; analytics now uses course-scoped student performance APIs

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

    @GetMapping("/analytics/course/{courseId}/students")
    @Operation(summary = "获取课程学生表现列表（分页）")
    public ResponseEntity<ApiResponse<CourseStudentPerformanceResponse>> getCourseStudentPerformance(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String search,
            @Parameter(description = "排序字段(name|grade)") @RequestParam(required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "活跃度筛选") @RequestParam(required = false) String activity,
            @Parameter(description = "成绩筛选") @RequestParam(required = false) String grade,
            @Parameter(description = "进度筛选") @RequestParam(required = false) String progress
    ) {
        Long teacherId = getCurrentUserId();
        CourseStudentPerformanceResponse resp = analyticsQueryService.getCourseStudentPerformance(teacherId, courseId, page, size, search, sortBy, activity, grade, progress);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/analytics/course/{courseId}/students/export")
    @Operation(summary = "导出课程学生表现（CSV）")
    public ResponseEntity<byte[]> exportCourseStudentPerformanceCsv(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String search,
            @Parameter(description = "排序字段(name|grade)") @RequestParam(required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "活跃度筛选") @RequestParam(required = false) String activity,
            @Parameter(description = "成绩筛选") @RequestParam(required = false) String grade,
            @Parameter(description = "进度筛选") @RequestParam(required = false) String progress
    ) {
        Long teacherId = getCurrentUserId();
        // 取全部数据（设置大尺寸）
        CourseStudentPerformanceResponse resp = analyticsQueryService.getCourseStudentPerformance(teacherId, courseId, 1, 100000, search, sortBy, activity, grade, progress);
        StringBuilder sb = new StringBuilder();
        sb.append("studentId,studentName,studentNo,progress,averageGrade,activityLevel,studyTimePerWeek,completedLessons,totalLessons,lastActiveAt\n");
        if (resp.getItems() != null) {
            for (CourseStudentPerformanceItem i : resp.getItems()) {
                sb.append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getStudentId()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.escape(i.getStudentName()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.escape(i.getStudentNo()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getProgress()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getAverageGrade()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.escape(String.valueOf(i.getActivityLevel())))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getStudyTimePerWeek()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getCompletedLessons()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.nullToEmpty(i.getTotalLessons()))
                  .append(',').append(com.noncore.assessment.util.CsvUtils.escape(i.getLastActiveAt()))
                  .append('\n');
            }
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_" + courseId + "_students.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    // CSV helpers moved to CsvUtils

    @PostMapping("/courses/{courseId}/students/{studentId}/progress/reset")
    @Operation(summary = "重置学生课程进度")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetStudentCourseProgress(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        Long teacherId = getCurrentUserId();
        enrollmentService.resetStudentCourseProgress(teacherId, courseId, studentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}