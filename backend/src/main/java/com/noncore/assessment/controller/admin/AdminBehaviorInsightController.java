package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.BehaviorInsightResponse;
import com.noncore.assessment.entity.BehaviorInsight;
import com.noncore.assessment.service.BehaviorInsightGenerationService;
import com.noncore.assessment.service.BehaviorInsightService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.Jsons;
import com.noncore.assessment.util.PageResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final BehaviorInsightService behaviorInsightService;
    private final com.noncore.assessment.service.admin.AdminAuditLogService adminAuditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public AdminBehaviorInsightController(BehaviorInsightGenerationService insightGenerationService,
                                          BehaviorInsightService behaviorInsightService,
                                          com.noncore.assessment.service.admin.AdminAuditLogService adminAuditLogService,
                                          UserService userService) {
        super(userService);
        this.insightGenerationService = insightGenerationService;
        this.behaviorInsightService = behaviorInsightService;
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

    @GetMapping("/history")
    @Operation(summary = "管理员分页查询行为洞察历史")
    public ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> history(@RequestParam Long studentId,
                                                                                 @RequestParam(required = false) Long courseId,
                                                                                 @RequestParam(required = false) String range,
                                                                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                                                                 @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                                 HttpServletRequest httpRequest) {
        Long operatorId = getCurrentUserId();
        final int safePage = (page == null || page < 1) ? 1 : page;
        final int safeSize = (size == null || size < 1) ? 10 : Math.min(size, 100);
        final String normalizedRange = normalizeRange(range);

        List<BehaviorInsight> rows = behaviorInsightService.pageByStudentCourseRange(
                studentId, courseId, normalizedRange, "behavior_insight.v1", safePage, safeSize);
        long total = behaviorInsightService.countByStudentCourseRange(studentId, courseId, normalizedRange, "behavior_insight.v1");
        int totalPages = safeSize <= 0 ? 0 : (int) Math.ceil(total / (double) safeSize);

        List<Map<String, Object>> items = rows.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row.getId());
            item.put("studentId", row.getStudentId());
            item.put("courseId", row.getCourseId());
            item.put("snapshotId", row.getSnapshotId());
            item.put("generatedAt", row.getGeneratedAt());
            item.put("status", row.getStatus());
            item.put("model", row.getModel());
            item.put("promptVersion", row.getPromptVersion());
            return item;
        }).toList();

        try {
            Map<String, Object> detail = new HashMap<>();
            detail.put("courseId", courseId);
            detail.put("range", normalizedRange);
            detail.put("page", safePage);
            detail.put("size", safeSize);
            adminAuditLogService.record(operatorId, "admin.behavior.insights.history", "query", studentId, detail, httpRequest);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(ApiResponse.success(PageResult.of(items, safePage, safeSize, total, totalPages)));
    }

    @GetMapping("/history/{id}")
    @Operation(summary = "管理员查看行为洞察历史详情")
    public ResponseEntity<ApiResponse<BehaviorInsightResponse>> historyDetail(@PathVariable Long id,
                                                                              HttpServletRequest httpRequest) {
        Long operatorId = getCurrentUserId();
        BehaviorInsight row = behaviorInsightService.getById(id);
        if (row == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        BehaviorInsightResponse detail = convertToInsightResponse(row);
        try {
            Map<String, Object> auditDetail = new HashMap<>();
            auditDetail.put("id", id);
            auditDetail.put("courseId", row.getCourseId());
            auditDetail.put("snapshotId", row.getSnapshotId());
            adminAuditLogService.record(operatorId, "admin.behavior.insights.history.detail", "query", row.getStudentId(), auditDetail, httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 统一标准化 range 取值，避免大小写和空值导致查询不命中。
     */
    private String normalizeRange(String range) {
        if (range == null || range.isBlank()) return null;
        String r = range.trim().toLowerCase();
        return switch (r) {
            case "7d", "30d", "180d", "365d" -> r;
            default -> null;
        };
    }

    /**
     * 将数据库中保存的 insightJson 反序列化为标准响应，并补齐审计元信息。
     */
    private BehaviorInsightResponse convertToInsightResponse(BehaviorInsight row) {
        BehaviorInsightResponse parsed;
        try {
            parsed = objectMapper.convertValue(Jsons.parseObject(row.getInsightJson()), BehaviorInsightResponse.class);
        } catch (Exception ignored) {
            parsed = new BehaviorInsightResponse();
        }
        parsed.setSchemaVersion(row.getSchemaVersion());
        parsed.setSnapshotId(row.getSnapshotId());
        BehaviorInsightResponse.Meta meta = parsed.getMeta() == null ? new BehaviorInsightResponse.Meta() : parsed.getMeta();
        meta.setGeneratedAt(row.getGeneratedAt());
        meta.setModel(row.getModel());
        meta.setPromptVersion(row.getPromptVersion());
        meta.setStatus(row.getStatus());
        parsed.setMeta(meta);
        return parsed;
    }
}

