package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.GradeStatsResponse;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.GradeService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grades")
@Tag(name = "成绩管理", description = "成绩录入、查询、统计等相关接口")
public class GradeController extends BaseController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService, UserService userService) {
        super(userService);
        this.gradeService = gradeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建成绩", description = "教师为学生作业创建成绩记录")
    public ResponseEntity<ApiResponse<Grade>> createGrade(@Valid @RequestBody Grade grade) {
        Grade createdGrade = gradeService.createGrade(grade);
        return ResponseEntity.ok(ApiResponse.success(createdGrade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取成绩详情", description = "根据成绩ID获取详细信息")
    public ResponseEntity<ApiResponse<Grade>> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新成绩", description = "更新成绩信息")
    public ResponseEntity<ApiResponse<Grade>> updateGrade(@PathVariable Long id, @Valid @RequestBody Grade grade) {
        if (gradeService.canModifyGrade(id, getCurrentUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        Grade updatedGrade = gradeService.updateGrade(id, grade);
        return ResponseEntity.ok(ApiResponse.success(updatedGrade));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除成绩", description = "删除指定成绩记录")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id) {
        if (gradeService.canModifyGrade(id, getCurrentUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取学生成绩", description = "获取指定学生的所有成绩")
    public ResponseEntity<ApiResponse<List<Grade>>> getGradesByStudent(@PathVariable Long studentId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        List<Grade> grades = gradeService.getGradesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/student/{studentId}/page")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "分页获取学生成绩", description = "分页获取指定学生的成绩列表")
    public ResponseEntity<ApiResponse<PageResult<Grade>>> getStudentGradesWithPagination(
            @PathVariable Long studentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        PageResult<Grade> result = gradeService.getStudentGradesWithPagination(studentId, page, size, courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取作业成绩", description = "获取指定作业的所有成绩")
    public ResponseEntity<ApiResponse<List<Grade>>> getGradesByAssignment(@PathVariable Long assignmentId) {
        List<Grade> grades = gradeService.getGradesByAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/assignment/{assignmentId}/page")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "分页获取作业成绩", description = "分页获取指定作业的成绩列表")
    public ResponseEntity<ApiResponse<PageResult<Grade>>> getAssignmentGradesWithPagination(
            @PathVariable Long assignmentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Grade> result = gradeService.getAssignmentGradesWithPagination(assignmentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/student/{studentId}/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取学生作业成绩", description = "获取指定学生在指定作业的成绩")
    public ResponseEntity<ApiResponse<Grade>> getStudentAssignmentGrade(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        Grade grade = gradeService.getStudentAssignmentGrade(studentId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量评分", description = "批量为学生作业评分")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchGrading(@RequestBody List<Grade> grades) {
        Map<String, Object> result = gradeService.batchGrading(grades);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/student/{studentId}/average")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "计算学生平均分", description = "计算指定学生的平均分")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateStudentAverageScore(@PathVariable Long studentId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        BigDecimal average = gradeService.calculateStudentAverageScore(studentId);
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    @GetMapping("/student/{studentId}/course/{courseId}/average")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "计算课程平均分", description = "计算学生在指定课程的平均分")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateStudentCourseAverageScore(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        BigDecimal average = gradeService.calculateStudentCourseAverageScore(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    @GetMapping("/assignment/{assignmentId}/average")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "计算作业平均分", description = "计算指定作业的平均分")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateAssignmentAverageScore(@PathVariable Long assignmentId) {
        BigDecimal average = gradeService.calculateAssignmentAverageScore(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(average));
    }

    @GetMapping("/assignment/{assignmentId}/distribution")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取成绩分布", description = "获取指定作业的成绩分布统计")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGradeDistribution(@PathVariable Long assignmentId) {
        List<Map<String, Object>> distribution = gradeService.getGradeDistribution(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(distribution));
    }

    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取课程成绩统计", description = "获取指定课程的成绩统计信息")
    public ResponseEntity<ApiResponse<GradeStatsResponse>> getCourseGradeStatistics(@PathVariable Long courseId) {
        GradeStatsResponse stats = gradeService.getCourseGradeStatistics(courseId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/student/{studentId}/statistics")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取学生成绩统计", description = "获取指定学生的成绩统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentGradeStatistics(@PathVariable Long studentId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        Map<String, Object> statistics = gradeService.getStudentGradeStatistics(studentId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取待评分作业", description = "获取教师待评分的作业列表")
    public ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> getPendingGrades(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> result = gradeService.getPendingGrades(getCurrentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布成绩", description = "发布指定成绩")
    public ResponseEntity<ApiResponse<Void>> publishGrade(@PathVariable Long id) {
        if (gradeService.canModifyGrade(id, getCurrentUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        gradeService.publishGrade(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/batch-publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量发布成绩", description = "批量发布多个成绩")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchPublishGrades(@RequestBody List<Long> gradeIds) {
        Map<String, Object> result = gradeService.batchPublishGrades(gradeIds);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/ranking")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取成绩排名", description = "获取成绩排名列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGradeRanking(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "作业ID") @RequestParam(required = false) Long assignmentId) {
        List<Map<String, Object>> ranking = gradeService.getGradeRanking(courseId, assignmentId);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }

    @GetMapping("/student/{studentId}/ranking")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取学生排名", description = "获取指定学生的排名信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentRanking(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        Map<String, Object> ranking = gradeService.getStudentRanking(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "导出成绩", description = "导出成绩数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportGrades(@RequestBody Map<String, Object> exportRequest) {
        Long courseId = exportRequest.get("courseId") != null ?
                Long.valueOf(exportRequest.get("courseId").toString()) : null;
        Long assignmentId = exportRequest.get("assignmentId") != null ?
                Long.valueOf(exportRequest.get("assignmentId").toString()) : null;
        String format = (String) exportRequest.getOrDefault("format", "excel");
        Map<String, Object> result = gradeService.exportGrades(courseId, assignmentId, format);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/student/{studentId}/trend")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "获取成绩趋势", description = "获取学生成绩趋势数据")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGradeTrend(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "统计天数", example = "30") @RequestParam(defaultValue = "30") Integer days) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        List<Map<String, Object>> trend = gradeService.getGradeTrend(studentId, courseId, days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "添加成绩评语", description = "为指定成绩添加评语")
    public ResponseEntity<ApiResponse<Void>> addGradeFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> feedbackData) {
        if (gradeService.canModifyGrade(id, getCurrentUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        String feedback = feedbackData.get("feedback");
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        gradeService.addGradeFeedback(id, feedback);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/regrade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "重新评分", description = "重新为指定成绩评分")
    public ResponseEntity<ApiResponse<Void>> regrade(
            @PathVariable Long id,
            @RequestBody Map<String, Object> regradeData) {
        if (gradeService.canModifyGrade(id, getCurrentUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        BigDecimal newScore = regradeData.get("newScore") != null ?
                new BigDecimal(regradeData.get("newScore").toString()) : null;
        String reason = (String) regradeData.get("reason");
        if (newScore == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        gradeService.regrade(id, newScore, reason);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取成绩历史", description = "获取指定成绩的历史记录")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGradeHistory(@PathVariable Long id) {
        List<Map<String, Object>> history = gradeService.getGradeHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT', 'ADMIN')")
    @Operation(summary = "计算GPA", description = "计算学生的GPA")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateGPA(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        if (hasRole("STUDENT") && !getCurrentUserId().equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        BigDecimal gpa = gradeService.calculateGPA(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(gpa));
    }
} 