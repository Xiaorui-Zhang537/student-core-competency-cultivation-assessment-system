package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAbilityReportService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员-能力报告中心。
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/ability-reports")
@Tag(name = "管理员-能力报告", description = "管理员跨学生检索/查看能力报告")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAbilityReportsController extends BaseController {

    private final AdminAbilityReportService adminAbilityReportService;

    public AdminAbilityReportsController(AdminAbilityReportService adminAbilityReportService, UserService userService) {
        super(userService);
        this.adminAbilityReportService = adminAbilityReportService;
    }

    @GetMapping
    @Operation(summary = "分页检索能力报告")
    public ResponseEntity<ApiResponse<PageResult<AbilityReport>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long submissionId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                adminAbilityReportService.pageReports(page, size, studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "能力报告详情")
    public ResponseEntity<ApiResponse<AbilityReport>> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminAbilityReportService.getReport(id)));
    }
}

