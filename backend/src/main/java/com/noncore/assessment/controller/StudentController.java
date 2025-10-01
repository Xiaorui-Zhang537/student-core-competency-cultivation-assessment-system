package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.dto.response.StudentCourseResponse;
import com.noncore.assessment.dto.response.StudentAnalysisResponse;
import com.noncore.assessment.dto.response.StudentCourseProgressResponse;
import com.noncore.assessment.dto.response.StudentLessonDetailResponse;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.*;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@Tag(name = "学生模块", description = "学生相关接口")
public class StudentController extends BaseController {

    private final StudentService studentService;
    private final DashboardService dashboardService;
    // enrollmentService 已在字段列表顶部声明，无需重复
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final StudentAnalysisService studentAnalysisService;
    private final LessonService lessonService;
    private final SubmissionService submissionService;

    public StudentController(StudentService studentService, DashboardService dashboardService, EnrollmentService enrollmentService, AssignmentService assignmentService, StudentAnalysisService studentAnalysisService, LessonService lessonService, SubmissionService submissionService, UserService userService) {
        super(userService);
        this.studentService = studentService;
        this.dashboardService = dashboardService;
        this.enrollmentService = enrollmentService;
        this.assignmentService = assignmentService;
        this.studentAnalysisService = studentAnalysisService;
        this.lessonService = lessonService;
        this.submissionService = submissionService;
    }

    @GetMapping("/my-submissions")
    @Operation(summary = "分页获取我的提交")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<com.noncore.assessment.util.PageResult<com.noncore.assessment.entity.Submission>>> getMySubmissions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long courseId
    ) {
        var result = submissionService.getStudentSubmissions(getCurrentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "获取课节详情与我的进度")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentLessonDetailResponse>> getLessonDetail(@PathVariable Long lessonId) {
        var lesson = lessonService.getLessonById(lessonId);
        var progress = lessonService.getStudentProgress(getCurrentUserId(), lessonId);
        return ResponseEntity.ok(ApiResponse.success(StudentLessonDetailResponse.builder().lesson(lesson).progress(progress).build()));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    @Operation(summary = "标记课节完成")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> completeLesson(@PathVariable Long lessonId) {
        lessonService.markLessonCompleted(getCurrentUserId(), lessonId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/lessons/{lessonId}/incomplete")
    @Operation(summary = "取消课节完成（将进度置0）")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> incompleteLesson(@PathVariable Long lessonId) {
        lessonService.updateStudentProgress(getCurrentUserId(), lessonId, java.math.BigDecimal.ZERO, 0, null);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取学生仪表盘数据")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        Long studentId = getCurrentUserId();
        StudentDashboardResponse dashboardData = dashboardService.getStudentDashboardData(studentId);
        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    @GetMapping("/courses/{id}/progress")
    @Operation(summary = "获取课程学习进度")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentCourseProgressResponse>> getCourseProgress(@PathVariable("id") Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            // 进度（0-100）、总时长、本周时长、最近章节名
            Double progress = java.util.Optional.ofNullable(lessonService.getOverallProgress(studentId, courseId)).orElse(0.0);
            Long total = java.util.Optional.ofNullable(lessonService.getTotalStudyMinutes(studentId, courseId)).orElse(0L);
            Long weekly = java.util.Optional.ofNullable(lessonService.getWeeklyStudyMinutes(studentId)).orElse(0L);
            String lastTitle = lessonService.getLastStudiedLessonTitle(studentId);
            StudentCourseProgressResponse resp = StudentCourseProgressResponse.builder()
                    .courseId(courseId)
                    .progress(progress)
                    .totalStudyMinutes(total)
                    .weeklyStudyMinutes(weekly)
                    .lastStudiedLessonTitle(lastTitle)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(StudentCourseProgressResponse.builder()
                    .courseId(courseId).progress(0.0).totalStudyMinutes(0L).weeklyStudyMinutes(0L).lastStudiedLessonTitle("").build()));
        }
    }

    @GetMapping("/my/courses")
    @Operation(summary = "获取我参加的课程")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Course>>> getMyEnrolledCourses() {
        List<Course> courses = enrollmentService.getEnrolledCourses(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 前端兼容路径：/students/my-courses
    @GetMapping("/my-courses")
    @Operation(summary = "获取我参加的课程（兼容路径）")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Course>>> getMyEnrolledCoursesCompat() {
        return getMyEnrolledCourses();
    }

    @GetMapping("/my-courses/paged")
    @Operation(summary = "分页获取我参加的课程（包含进度与教师名）")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<com.noncore.assessment.util.PageResult<StudentCourseResponse>>> getMyCoursesPaged(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(value = "q", required = false) String keyword
    ) {
        Long sid = getCurrentUserId();
        // 先同步一次课程冗余进度，确保前端卡片显示
        try { enrollmentService.syncStudentCourseProgress(sid); } catch (Exception ignored) {}
        var result = enrollmentService.getStudentCoursesPaged(sid, page, size, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/assignments")
    @Operation(summary = "查询我的作业（分页+过滤）")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<com.noncore.assessment.util.PageResult<Assignment>>> getMyAssignments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "includeHistory", required = false, defaultValue = "false") Boolean includeHistory,
            @RequestParam(value = "onlyPending", required = false, defaultValue = "false") Boolean onlyPending
    ) {
        var result = assignmentService.getAssignmentsForStudent(getCurrentUserId(), page, size, courseId, status, keyword, includeHistory, onlyPending);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/courses/{id}/participants")
    @Operation(summary = "获取课程参与者（教师与学生）")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<com.noncore.assessment.dto.response.CourseParticipantsResponse>> getCourseParticipants(
            @PathVariable("id") Long courseId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        var result = studentService.getCourseParticipants(getCurrentUserId(), courseId, keyword);
        return ResponseEntity.ok(com.noncore.assessment.util.ApiResponse.success(result));
    }

    @GetMapping("/analysis")
    @Operation(summary = "获取学生成绩分析聚合数据")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentAnalysisResponse>> getStudentAnalysis(
            @RequestParam(value = "range", required = false) String range
    ) {
        try {
            StudentAnalysisResponse resp = studentAnalysisService.getStudentAnalysis(getCurrentUserId(), range);
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (Exception e) {
            // 容错返回空结构，避免前端崩溃
            StudentAnalysisResponse empty = StudentAnalysisResponse.builder()
                    .kpi(StudentAnalysisResponse.Kpi.builder()
                            .avgScore(0.0)
                            .completionRate(0.0)
                            .studyHours(0L)
                            .activeDays(0L)
                            .build())
                    .radar(StudentAnalysisResponse.Radar.builder()
                            .invest(0.0).quality(0.0).mastery(0.0).stability(0.0).growth(0.0)
                            .build())
                    .trends(StudentAnalysisResponse.Trends.builder()
                            .score(java.util.Collections.emptyList())
                            .completion(java.util.Collections.emptyList())
                            .hours(java.util.Collections.emptyList())
                            .build())
                    .recentGrades(java.util.Collections.emptyList())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(empty));
        }
    }

    @GetMapping("/my/assignments/pending")
    @Operation(summary = "获取我待完成的作业")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getMyPendingAssignments() {
        List<Assignment> assignments = studentService.getPendingAssignments(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
} 