package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Submission;
import com.noncore.assessment.service.SubmissionService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.noncore.assessment.dto.request.SubmissionRequest;
import com.noncore.assessment.behavior.BehaviorEventRecorder;
import com.noncore.assessment.behavior.BehaviorEventType;

import java.util.Map;
import com.noncore.assessment.util.PageResult;

@RestController
@RequestMapping("")
@Tag(name = "作业提交管理", description = "学生提交作业、草稿、查询成绩等相关接口")
public class SubmissionController extends BaseController {

    private final SubmissionService submissionService;
    private final BehaviorEventRecorder behaviorEventRecorder;

    public SubmissionController(SubmissionService submissionService,
                                BehaviorEventRecorder behaviorEventRecorder,
                                UserService userService) {
        super(userService);
        this.submissionService = submissionService;
        this.behaviorEventRecorder = behaviorEventRecorder;
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(summary = "获取提交详情", description = "根据提交ID获取提交详情（教师/管理员可用）")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<Submission>> getSubmissionById(@PathVariable Long submissionId) {
        Submission submission = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(ApiResponse.success(submission));
    }

    @GetMapping("/assignments/{assignmentId}/submission")
    @Operation(summary = "获取学生作业提交详情", description = "获取当前学生对指定作业的提交详情")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Submission>> getAssignmentSubmission(@PathVariable Long assignmentId) {
        Submission submission = submissionService.getStudentSubmission(assignmentId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(submission));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "分页获取作业提交列表", description = "教师/管理员分页获取指定作业的提交列表")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<Submission>>> getAssignmentSubmissions(
            @PathVariable Long assignmentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Submission> result = submissionService.getAssignmentSubmissions(assignmentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    @Operation(summary = "提交作业", description = "学生提交作业内容和文件")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Submission>> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file) {
        Submission submission = submissionService.submitAssignment(assignmentId, getCurrentUserId(), content, file);
        // 行为记录：作业提交/修改（仅记录事实，不评价，不算分）
        try {
            boolean isResubmit = submission != null && submission.getSubmissionCount() != null && submission.getSubmissionCount() > 1;
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("submissionId", submission != null ? submission.getId() : null);
            meta.put("submissionCount", submission != null ? submission.getSubmissionCount() : null);
            behaviorEventRecorder.record(
                    getCurrentUserId(),
                    null,
                    isResubmit ? BehaviorEventType.ASSIGNMENT_RESUBMIT : BehaviorEventType.ASSIGNMENT_SUBMIT,
                    "assignment",
                    assignmentId,
                    meta
            );
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(submission));
    }

    @PostMapping(value = "/assignments/{assignmentId}/submit", consumes = "application/json")
    @Operation(summary = "提交作业(JSON)", description = "学生以JSON提交作业，fileIds为已上传文件ID列表")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Submission>> submitAssignmentJson(
            @PathVariable Long assignmentId,
            @RequestBody SubmissionRequest request) {
        Submission submission = submissionService.submitAssignment(assignmentId, getCurrentUserId(), request);
        // 行为记录：作业提交/修改（仅记录事实，不评价，不算分）
        try {
            boolean isResubmit = submission != null && submission.getSubmissionCount() != null && submission.getSubmissionCount() > 1;
            java.util.Map<String, Object> meta = new java.util.HashMap<>();
            meta.put("submissionId", submission != null ? submission.getId() : null);
            meta.put("submissionCount", submission != null ? submission.getSubmissionCount() : null);
            behaviorEventRecorder.record(
                    getCurrentUserId(),
                    null,
                    isResubmit ? BehaviorEventType.ASSIGNMENT_RESUBMIT : BehaviorEventType.ASSIGNMENT_SUBMIT,
                    "assignment",
                    assignmentId,
                    meta
            );
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(submission));
    }

    @PostMapping("/assignments/{assignmentId}/draft")
    @Operation(summary = "保存作业草稿", description = "学生保存作业草稿")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Submission>> saveDraft(
            @PathVariable Long assignmentId,
            @RequestParam String content) {
        Submission submission = submissionService.saveDraft(assignmentId, getCurrentUserId(), content);
        return ResponseEntity.ok(ApiResponse.success(submission));
    }

    @GetMapping("/submissions/my/grades")
    @Operation(summary = "获取我的成绩列表", description = "获取当前学生的所有作业成绩")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> getMyGrades(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Map<String, Object>> grades = submissionService.getStudentGrades(getCurrentUserId(), page, size);
        // 行为记录：查看反馈（成绩列表）
        try {
            behaviorEventRecorder.record(
                    getCurrentUserId(),
                    null,
                    BehaviorEventType.FEEDBACK_VIEW,
                    "grade_list",
                    null,
                    java.util.Map.of("page", page, "size", size)
            );
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "获取作业成绩详情", description = "获取指定提交的成绩详情")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubmissionGrade(@PathVariable Long submissionId) {
        Map<String, Object> grade = submissionService.getSubmissionGrade(submissionId);
        // 行为记录：查看反馈（成绩详情）
        try {
            if (hasRole("STUDENT")) {
                java.util.Map<String, Object> meta = new java.util.HashMap<>();
                meta.put("kind", "submission_grade");
                if (grade != null) {
                    Object aid = grade.get("assignmentId");
                    if (aid != null) meta.put("assignmentId", aid);
                }
                behaviorEventRecorder.record(
                        getCurrentUserId(),
                        null,
                        BehaviorEventType.FEEDBACK_VIEW,
                        "submission",
                        submissionId,
                        meta
                );
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @GetMapping("/submissions/{submissionId}/export")
    @Operation(summary = "导出提交", description = "导出指定提交为ZIP压缩包")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<byte[]> exportSubmission(@PathVariable Long submissionId) {
        byte[] data = submissionService.exportSubmissionZip(submissionId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submission_" + submissionId + ".zip\"");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }
} 