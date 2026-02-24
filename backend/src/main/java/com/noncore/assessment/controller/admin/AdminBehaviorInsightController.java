package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.BehaviorInsightResponse;
import com.noncore.assessment.service.BehaviorInsightGenerationService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-行为洞察（阶段二，AI 解读）。
 *
 * <p>说明：行为洞察的业务逻辑已支持 isAuthenticated；此处提供管理员专用入口，便于前端按 /admin API 统一管理。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/behavior/insights")
@Tag(name = "管理员-行为洞察", description = "管理员行为洞察查询/生成（全局）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBehaviorInsightController extends BaseController {

    private final BehaviorInsightGenerationService insightGenerationService;
    private final com.noncore.assessment.service.admin.AdminAuditLogService adminAuditLogService;

    public AdminBehaviorInsightController(BehaviorInsightGenerationService insightGenerationService,
                                          com.noncore.assessment.service.admin.AdminAuditLogService adminAuditLogService,
                                          UserService userService) {
        super(userService);
        this.insightGenerationService = insightGenerationService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @PostMapping("/generate")
    @Operation(summary = "管理员生成行为洞察", description = "按时间窗触发 AI 对阶段一摘要进行解释与总结（不计算分数）")
    public ResponseEntity<ApiResponse<BehaviorInsightResponse>> generate(@RequestParam Long studentId,
                                                                         @RequestParam(required = false) Long courseId,
                                                                         @RequestParam(required = false, defaultValue = "7d") String range,
                                                                         @RequestParam(required = false) String model,
                                                                         @RequestParam(required = false, defaultValue = "false") Boolean force,
                                                                         HttpServletRequest httpRequest) {
        Long operatorId = getCurrentUserId();
        boolean f = Boolean.TRUE.equals(force);
        BehaviorInsightResponse resp = insightGenerationService.generate(operatorId, studentId, courseId, range, model, f, false);
        try {
            adminAuditLogService.record(operatorId, "admin.behavior.insights.generate", "ai", studentId,
                    java.util.Map.of("courseId", courseId, "range", range, "model", model, "force", f), httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping
    @Operation(summary = "管理员获取最新行为洞察")
    public ResponseEntity<ApiResponse<BehaviorInsightResponse>> getLatest(@Parameter(description = "学生ID") @RequestParam Long studentId,
                                                                          @RequestParam(required = false) Long courseId,
                                                                          @RequestParam(required = false, defaultValue = "7d") String range,
                                                                          HttpServletRequest httpRequest) {
        Long operatorId = getCurrentUserId();
        BehaviorInsightResponse resp = insightGenerationService.getLatest(operatorId, studentId, courseId, range);
        try {
            adminAuditLogService.record(operatorId, "admin.behavior.insights.latest", "query", studentId,
                    java.util.Map.of("courseId", courseId, "range", range), httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}

