package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.dto.response.AssignmentSubmissionStatsResponse;
import com.noncore.assessment.service.AssignmentService;
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
import com.noncore.assessment.dto.request.BatchAssignmentStatusRequest;

import java.util.List;
import java.util.Map;

/**
 * 作业控制器
 * 提供作业相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/assignments")
@Tag(name = "作业管理", description = "作业发布、查询、提交等相关接口")
public class AssignmentController extends BaseController {

    private final AssignmentService assignmentService;
    
    public AssignmentController(AssignmentService assignmentService, UserService userService) {
        super(userService);
        this.assignmentService = assignmentService;
    }

    /**
     * 分页查询作业列表
     */
    @GetMapping
    @Operation(summary = "分页查询作业", description = "支持课程筛选、状态筛选、关键词搜索等")
    public ResponseEntity<ApiResponse<PageResult<Assignment>>> getAssignments(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "作业状态") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        PageResult<Assignment> result = assignmentService.getAssignments(page, size, courseId, teacherId, status, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 根据ID获取作业详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取作业详情", description = "根据作业ID获取详细信息")
    public ResponseEntity<ApiResponse<Assignment>> getAssignmentById(@PathVariable Long id) {
        Assignment assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    /**
     * 创建新作业
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建作业", description = "教师创建新作业")
    public ResponseEntity<ApiResponse<Assignment>> createAssignment(@Valid @RequestBody Assignment assignment) {
        assignment.setTeacherId(getCurrentUserId());
        Assignment createdAssignment = assignmentService.createAssignment(assignment);
        return ResponseEntity.ok(ApiResponse.success(createdAssignment));
    }

    /**
     * 更新作业信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新作业", description = "更新作业信息")
    public ResponseEntity<ApiResponse<Assignment>> updateAssignment(@PathVariable Long id, @Valid @RequestBody Assignment assignment) {
        Assignment updatedAssignment = assignmentService.updateAssignment(id, assignment);
        return ResponseEntity.ok(ApiResponse.success(updatedAssignment));
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除作业", description = "删除指定作业")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 发布作业
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布作业", description = "将作业状态设置为已发布")
    public ResponseEntity<ApiResponse<Void>> publishAssignment(@PathVariable Long id) {
        assignmentService.publishAssignment(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 下架作业
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "下架作业", description = "将作业状态设置为草稿")
    public ResponseEntity<ApiResponse<Void>> unpublishAssignment(@PathVariable Long id) {
        assignmentService.unpublishAssignment(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 关闭作业
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "关闭作业", description = "关闭作业，不再接收提交")
    public ResponseEntity<ApiResponse<Void>> closeAssignment(@PathVariable Long id) {
        assignmentService.closeAssignment(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取课程的作业列表
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程作业", description = "获取指定课程的所有作业")
    public ResponseEntity<ApiResponse<List<Assignment>>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    /**
     * 获取我的作业列表（教师）
     */
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取我的作业", description = "教师查看自己创建的作业")
    public ResponseEntity<ApiResponse<List<Assignment>>> getMyAssignments() {
        Long teacherId = getCurrentUserId();
        List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    /**
     * 获取学生的作业列表
     */
    @GetMapping("/for-student")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "获取学生作业", description = "学生查看可做的作业")
    public ResponseEntity<ApiResponse<List<Assignment>>> getAssignmentsForStudent(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsForStudent(getCurrentUserId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    /**
     * 获取即将到期的作业
     */
    @GetMapping("/due-soon")
    @Operation(summary = "即将到期作业", description = "获取即将到期的作业列表")
    public ResponseEntity<ApiResponse<List<Assignment>>> getDueAssignments(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        List<Assignment> assignments = assignmentService.getDueAssignments(days);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    /**
     * 检查作业是否可以提交
     */
    @GetMapping("/{id}/can-submit")
    @Operation(summary = "检查提交状态", description = "检查作业是否可以提交")
    public ResponseEntity<ApiResponse<Boolean>> checkCanSubmit(@PathVariable Long id) {
        boolean canSubmit = assignmentService.canSubmit(id);
        return ResponseEntity.ok(ApiResponse.success(canSubmit));
    }

    /**
     * 获取作业统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取作业统计", description = "获取作业相关的统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssignmentStatistics(
            @Parameter(description = "教师ID，不传则统计所有") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "课程ID，不传则统计所有") @RequestParam(required = false) Long courseId) {
        if (teacherId == null && hasRole("TEACHER")) {
            teacherId = getCurrentUserId();
        }
        Map<String, Object> statistics = assignmentService.getAssignmentStatistics(teacherId, courseId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取指定作业的提交统计（课程总人数、已提交、未提交）
     */
    @GetMapping("/{id}/submission-stats")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取作业提交统计", description = "返回课程活跃选课人数与该作业的提交统计")
    public ResponseEntity<ApiResponse<AssignmentSubmissionStatsResponse>> getAssignmentSubmissionStats(@PathVariable Long id) {
        AssignmentSubmissionStatsResponse stats = assignmentService.getSubmissionStats(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 提醒未提交学生
     */
    @PostMapping("/{id}/remind-unsubmitted")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "提醒未提交学生", description = "向未提交该作业的在读学生发送提醒通知")
    public ResponseEntity<ApiResponse<Map<String, Object>>> remindUnsubmitted(@PathVariable Long id,
                                                                              @RequestBody(required = false) Map<String, String> body) {
        String customMessage = body != null ? body.getOrDefault("message", null) : null;
        Map<String, Object> res = assignmentService.remindUnsubmitted(id, getCurrentUserId(), customMessage);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /**
     * 批量更新作业状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个作业的状态")
    public ResponseEntity<ApiResponse<Void>> batchUpdateStatus(@RequestBody BatchAssignmentStatusRequest request) {
        assignmentService.batchUpdateStatus(request.getAssignmentIds(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }
} 