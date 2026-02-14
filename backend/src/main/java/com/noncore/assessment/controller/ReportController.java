package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Report;
import com.noncore.assessment.service.ReportService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@Tag(name = "举报管理", description = "提交与处理举报")
public class ReportController extends BaseController {
    private final ReportService reportService;
    private final AdminAuditLogService adminAuditLogService;

    public ReportController(ReportService reportService,
                            AdminAuditLogService adminAuditLogService,
                            UserService userService) {
        super(userService);
        this.reportService = reportService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @PostMapping
    @Operation(summary = "提交举报")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<Report>> create(@RequestBody Report report) {
        report.setReporterId(getCurrentUserId());
        Report r = reportService.createReport(report);
        return ResponseEntity.ok(ApiResponse.success(r));
    }

    @GetMapping("/{id}")
    @Operation(summary = "举报详情")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<ApiResponse<Report>> get(@PathVariable Long id) {
        Report r = reportService.getReport(id);
        // 简化：允许教师查看；如需限制为本人再加校验
        return ResponseEntity.ok(ApiResponse.success(r));
    }

    @GetMapping
    @Operation(summary = "举报分页列表（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResult<Report>>> page(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success(reportService.pageReports(status, page, size)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新举报状态（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id,
                                                          @RequestParam String status,
                                                          HttpServletRequest httpRequest) {
        reportService.updateStatus(id, status);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.report.updateStatus",
                "report",
                id,
                Map.of("status", status),
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success());
    }
}


