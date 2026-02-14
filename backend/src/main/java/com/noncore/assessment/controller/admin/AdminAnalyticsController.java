package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAnalyticsService;
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
 * 管理员-分析看板控制器。
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/analytics")
@Tag(name = "管理员-分析看板", description = "管理员全局分析序列数据")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController extends BaseController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService, UserService userService) {
        super(userService);
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/series/overview")
    @Operation(summary = "获取分析序列（活跃/新增/选课）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overviewSeries(
            @RequestParam(required = false, defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminAnalyticsService.getOverviewSeries(days)));
    }
}

