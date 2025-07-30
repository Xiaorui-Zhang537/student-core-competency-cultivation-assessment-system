package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Submission;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AssignmentService;
import com.noncore.assessment.service.SubmissionService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import com.noncore.assessment.mapper.UserMapper;

/**
 * 作业控制器
 * 提供作业相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/assignments")
@Tag(name = "作业管理", description = "作业发布、查询、提交等相关接口")
public class AssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final UserMapper userMapper;

    public AssignmentController(AssignmentService assignmentService, SubmissionService submissionService, UserMapper userMapper) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.userMapper = userMapper;
    }

    /**
     * 分页查询作业列表
     */
    @GetMapping
    @Operation(summary = "分页查询作业", description = "支持课程筛选、状态筛选、关键词搜索等")
    public ApiResponse<PageResult<Assignment>> getAssignments(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "作业状态") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        try {
            PageResult<Assignment> result = assignmentService.getAssignments(page, size, courseId, teacherId, status, keyword);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            logger.error("查询作业列表失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 根据ID获取作业详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取作业详情", description = "根据作业ID获取详细信息")
    public ApiResponse<Assignment> getAssignmentById(@PathVariable Long id) {
        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            return ApiResponse.success("获取成功", assignment);
        } catch (Exception e) {
            logger.error("获取作业详情失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 创建新作业
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建作业", description = "教师创建新作业")
    public ApiResponse<Assignment> createAssignment(@Valid @RequestBody Assignment assignment) {
        try {
            // 设置当前用户为作业教师
            Long currentUserId = getCurrentUserId();
            assignment.setTeacherId(currentUserId);
            
            Assignment createdAssignment = assignmentService.createAssignment(assignment);
            return ApiResponse.success("作业创建成功", createdAssignment);
        } catch (Exception e) {
            logger.error("创建作业失败: {}", e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新作业信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新作业", description = "更新作业信息")
    public ApiResponse<Assignment> updateAssignment(@PathVariable Long id, @Valid @RequestBody Assignment assignment) {
        try {
            Assignment updatedAssignment = assignmentService.updateAssignment(id, assignment);
            return ApiResponse.success("作业更新成功", updatedAssignment);
        } catch (Exception e) {
            logger.error("更新作业失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除作业", description = "删除指定作业")
    public ApiResponse<String> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ApiResponse.success("作业删除成功", "deleted");
        } catch (Exception e) {
            logger.error("删除作业失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 发布作业
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布作业", description = "将作业状态设置为已发布")
    public ApiResponse<String> publishAssignment(@PathVariable Long id) {
        try {
            assignmentService.publishAssignment(id);
            return ApiResponse.success("作业发布成功", "published");
        } catch (Exception e) {
            logger.error("发布作业失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 下架作业
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "下架作业", description = "将作业状态设置为草稿")
    public ApiResponse<String> unpublishAssignment(@PathVariable Long id) {
        try {
            assignmentService.unpublishAssignment(id);
            return ApiResponse.success("作业下架成功", "unpublished");
        } catch (Exception e) {
            logger.error("下架作业失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 关闭作业
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "关闭作业", description = "关闭作业，不再接收提交")
    public ApiResponse<String> closeAssignment(@PathVariable Long id) {
        try {
            assignmentService.closeAssignment(id);
            return ApiResponse.success("作业关闭成功", "closed");
        } catch (Exception e) {
            logger.error("关闭作业失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取课程的作业列表
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程作业", description = "获取指定课程的所有作业")
    public ApiResponse<List<Assignment>> getAssignmentsByCourse(@PathVariable Long courseId) {
        try {
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
            return ApiResponse.success("查询成功", assignments);
        } catch (Exception e) {
            logger.error("获取课程作业失败: courseId={}, error={}", courseId, e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取我的作业列表（教师）
     */
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取我的作业", description = "教师查看自己创建的作业")
    public ApiResponse<List<Assignment>> getMyAssignments() {
        try {
            Long teacherId = getCurrentUserId();
            List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
            return ApiResponse.success("查询成功", assignments);
        } catch (Exception e) {
            logger.error("获取我的作业失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取学生的作业列表
     */
    @GetMapping("/for-student")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "获取学生作业", description = "学生查看可做的作业")
    public ApiResponse<List<Assignment>> getAssignmentsForStudent(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            List<Assignment> assignments = assignmentService.getAssignmentsForStudent(studentId, courseId);
            return ApiResponse.success("查询成功", assignments);
        } catch (Exception e) {
            logger.error("获取学生作业失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取即将到期的作业
     */
    @GetMapping("/due-soon")
    @Operation(summary = "即将到期作业", description = "获取即将到期的作业列表")
    public ApiResponse<List<Assignment>> getDueAssignments(
            @Parameter(description = "天数", example = "7") @RequestParam(defaultValue = "7") Integer days) {
        try {
            List<Assignment> assignments = assignmentService.getDueAssignments(days);
            return ApiResponse.success("查询成功", assignments);
        } catch (Exception e) {
            logger.error("获取即将到期作业失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 检查作业是否可以提交
     */
    @GetMapping("/{id}/can-submit")
    @Operation(summary = "检查提交状态", description = "检查作业是否可以提交")
    public ApiResponse<Boolean> checkCanSubmit(@PathVariable Long id) {
        try {
            boolean canSubmit = assignmentService.canSubmit(id);
            return ApiResponse.success("查询成功", canSubmit);
        } catch (Exception e) {
            logger.error("检查作业提交状态失败: assignmentId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取作业统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取作业统计", description = "获取作业相关的统计信息")
    public ApiResponse<Map<String, Object>> getAssignmentStatistics(
            @Parameter(description = "教师ID，不传则统计所有") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "课程ID，不传则统计所有") @RequestParam(required = false) Long courseId) {
        try {
            // 如果没有传teacherId且当前用户是教师，则统计当前教师的作业
            if (teacherId == null && getCurrentUserRole().equals("TEACHER")) {
                teacherId = getCurrentUserId();
            }
            Map<String, Object> statistics = assignmentService.getAssignmentStatistics(teacherId, courseId);
            return ApiResponse.success("统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取作业统计失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 批量更新作业状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个作业的状态")
    public ApiResponse<String> batchUpdateStatus(@RequestBody BatchUpdateRequest request) {
        try {
            assignmentService.batchUpdateStatus(request.getAssignmentIds(), request.getStatus());
            return ApiResponse.success("批量更新成功", "updated");
        } catch (Exception e) {
            logger.error("批量更新作业状态失败: {}", e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // ==================== 作业提交相关接口 ====================

    /**
     * 获取学生作业提交详情
     */
    @GetMapping("/{id}/submission")
    @Operation(summary = "获取学生作业提交详情", description = "获取当前学生对指定作业的提交详情")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER')")
    public ApiResponse<Submission> getAssignmentSubmission(@PathVariable Long id) {
        try {
            // 获取当前用户ID
            Long currentUserId = getCurrentUserId();
            
            // 学生只能查看自己的提交，教师可以查看所有提交
            Submission submission;
            if (getCurrentUserRole().equals("STUDENT")) {
                submission = submissionService.getStudentSubmission(id, currentUserId);
            } else {
                // 教师需要通过其他参数指定学生ID，这里简化处理
                throw new BusinessException(ErrorCode.STUDENT_ID_REQUIRED, "教师查看提交需要指定学生ID");
            }
            
            if (submission == null) {
                return ApiResponse.success("暂无提交记录", null);
            }
            
            return ApiResponse.success("获取提交详情成功", submission);
        } catch (Exception e) {
            logger.error("获取提交详情失败", e);
            return ApiResponse.error("获取提交详情失败: " + e.getMessage());
        }
    }

    /**
     * 提交作业
     */
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交作业", description = "学生提交作业内容和文件")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Submission> submitAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file) {
        try {
            Long studentId = getCurrentUserId();
            Submission submission = submissionService.submitAssignment(id, studentId, content, file);
            return ApiResponse.success("作业提交成功", submission);
        } catch (Exception e) {
            logger.error("提交作业失败", e);
            return ApiResponse.error("提交作业失败: " + e.getMessage());
        }
    }

    /**
     * 保存作业草稿
     */
    @PostMapping("/{id}/draft")
    @Operation(summary = "保存作业草稿", description = "学生保存作业草稿")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Submission> saveDraft(
            @PathVariable Long id,
            @RequestParam String content) {
        try {
            Long studentId = getCurrentUserId();
            Submission submission = submissionService.saveDraft(id, studentId, content);
            return ApiResponse.success("草稿保存成功", submission);
        } catch (Exception e) {
            logger.error("保存草稿失败", e);
            return ApiResponse.error("保存草稿失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生成绩列表
     */
    @GetMapping("/grades")
    @Operation(summary = "获取学生成绩列表", description = "获取当前学生的所有作业成绩")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<PageResult<Map<String, Object>>> getStudentGrades(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long studentId = getCurrentUserId();
            PageResult<Map<String, Object>> grades = submissionService.getStudentGrades(studentId, page, size);
            return ApiResponse.success("获取成绩列表成功", grades);
        } catch (Exception e) {
            logger.error("获取成绩列表失败", e);
            return ApiResponse.error("获取成绩列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业成绩详情
     */
    @GetMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "获取作业成绩详情", description = "获取指定提交的成绩详情")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER')")  
    public ApiResponse<Map<String, Object>> getSubmissionGrade(@PathVariable Long submissionId) {
        try {
            Map<String, Object> grade = submissionService.getSubmissionGrade(submissionId);
            return ApiResponse.success("获取成绩详情成功", grade);
        } catch (Exception e) {
            logger.error("获取成绩详情失败", e);
            return ApiResponse.error("获取成绩详情失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新请求DTO
     */
    public static class BatchUpdateRequest {
        private List<Long> assignmentIds;
        private String status;

        public List<Long> getAssignmentIds() {
            return assignmentIds;
        }

        public void setAssignmentIds(List<Long> assignmentIds) {
            this.assignmentIds = assignmentIds;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户未登录");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户未登录");
        }
        return Long.valueOf(authentication.getName());
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }
} 