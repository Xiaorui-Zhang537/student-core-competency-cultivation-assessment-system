package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.mapper.AdminUserMapper;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.CsvUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员-数据导出（轻量同步版）。
 *
 * <p>说明：首期以同步 CSV 导出为主，避免前端拉取全量数据；后续可扩展为异步任务+文件存储。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/exports")
@Tag(name = "管理员-数据导出", description = "管理员数据导出（CSV）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExportsController extends BaseController {

    private final AdminUserMapper adminUserMapper;
    private final AbilityReportMapper abilityReportMapper;
    private final AdminAuditLogService adminAuditLogService;

    public AdminExportsController(AdminUserMapper adminUserMapper,
                                  AbilityReportMapper abilityReportMapper,
                                  AdminAuditLogService adminAuditLogService,
                                  UserService userService) {
        super(userService);
        this.adminUserMapper = adminUserMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/users.csv")
    @Operation(summary = "导出用户列表（CSV）")
    public ResponseEntity<byte[]> exportUsersCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            HttpServletRequest httpRequest
    ) {
        // 导出上限：避免一次性拉取过大
        int size = 50000;
        List<AdminUserListItemResponse> items = adminUserMapper.pageUsers(keyword, role, status, includeDeleted, 0, size);

        StringBuilder sb = new StringBuilder();
        sb.append("id,username,email,role,status,nickname,studentNo,teacherNo,emailVerified,deleted,createdAt\n");
        if (items != null) {
            for (AdminUserListItemResponse u : items) {
                sb.append(CsvUtils.nullToEmpty(u.getId())).append(',')
                        .append(CsvUtils.escape(u.getUsername())).append(',')
                        .append(CsvUtils.escape(u.getEmail())).append(',')
                        .append(CsvUtils.escape(u.getRole())).append(',')
                        .append(CsvUtils.escape(u.getStatus())).append(',')
                        .append(CsvUtils.escape(u.getNickname())).append(',')
                        .append(CsvUtils.escape(u.getStudentNo())).append(',')
                        .append(CsvUtils.escape(u.getTeacherNo())).append(',')
                        .append(CsvUtils.nullToEmpty(u.isEmailVerified())).append(',')
                        .append(CsvUtils.nullToEmpty(u.isDeleted())).append(',')
                        .append(CsvUtils.escape(u.getCreatedAt() == null ? "" : String.valueOf(u.getCreatedAt())))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.users.csv",
                "export",
                null,
                Map.of("keyword", keyword, "role", role, "status", status, "includeDeleted", includeDeleted, "rows", items == null ? 0 : items.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/ability-reports.csv")
    @Operation(summary = "导出能力报告列表（CSV）")
    public ResponseEntity<byte[]> exportAbilityReportsCsv(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long submissionId,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        List<AbilityReport> items = abilityReportMapper.selectAdminReports(
                studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end, 0, size
        );

        StringBuilder sb = new StringBuilder();
        sb.append("id,studentId,studentName,studentNo,reportType,title,overallScore,courseId,assignmentId,submissionId,isPublished,createdAt\n");
        if (items != null) {
            for (AbilityReport r : items) {
                sb.append(CsvUtils.nullToEmpty(r.getId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getStudentId())).append(',')
                        .append(CsvUtils.escape(r.getStudentName())).append(',')
                        .append(CsvUtils.escape(r.getStudentNumber())).append(',')
                        .append(CsvUtils.escape(r.getReportType())).append(',')
                        .append(CsvUtils.escape(r.getTitle())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getOverallScore())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getCourseId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getAssignmentId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getSubmissionId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getIsPublished())).append(',')
                        .append(CsvUtils.escape(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString()))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.abilityReports.csv",
                "export",
                null,
                Map.of(
                        "studentId", studentId,
                        "reportType", reportType,
                        "isPublished", isPublished,
                        "courseId", courseId,
                        "assignmentId", assignmentId,
                        "submissionId", submissionId,
                        "start", start == null ? null : start.toString(),
                        "end", end == null ? null : end.toString(),
                        "rows", items == null ? 0 : items.size()
                ),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ability_reports.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * 简单健康检查/占位，避免前端导出中心初期空白。
     */
    @GetMapping("/capabilities")
    @Operation(summary = "导出能力列表")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "formats", java.util.List.of("csv"),
                "maxRows", 50000,
                "exports", java.util.List.of(
                        java.util.Map.of("key", "users", "path", "/api/admin/exports/users.csv"),
                        java.util.Map.of("key", "abilityReports", "path", "/api/admin/exports/ability-reports.csv")
                )
        )));
    }
}

