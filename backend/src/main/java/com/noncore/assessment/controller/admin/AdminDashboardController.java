package com.noncore.assessment.controller.admin;

import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminDashboardService;
import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员仪表盘控制器。
 *
 * <p>说明：该模块面向 ADMIN 角色，聚合全局统计数据。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "管理员-仪表盘", description = "管理员仪表盘总览数据")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController extends BaseController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService, UserService userService) {
        super(userService);
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/overview")
    @Operation(summary = "仪表盘总览")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overview(
            @RequestParam(value = "days", required = false, defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getOverview(days)));
    }

    @GetMapping("/ability-radar-overview")
    @Operation(summary = "仪表盘五维能力雷达总览")
    public ResponseEntity<ApiResponse<Map<String, Object>>> abilityRadarOverview(
            @RequestParam(value = "days", required = false, defaultValue = "180") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getAbilityRadarOverview(days)));
    }

    @GetMapping("/ai-usage-overview")
    @Operation(summary = "仪表盘 AI 使用总览（按用户访问）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiUsageOverview(
            @RequestParam(value = "days", required = false, defaultValue = "30") int days,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminDashboardService.getAiUsageOverview(days, limit)));
    }
}

