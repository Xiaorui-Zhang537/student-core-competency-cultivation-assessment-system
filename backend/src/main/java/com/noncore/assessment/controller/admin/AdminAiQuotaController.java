package com.noncore.assessment.controller.admin;

import com.noncore.assessment.behavior.BehaviorSchemaVersions;
import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.entity.AiQuotaAdjustment;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.AiQuotaService;
import com.noncore.assessment.service.AiVoicePracticeService;
import com.noncore.assessment.service.BehaviorInsightService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理员-学生 AI 额度管理：
 * - AI 问答周额度加额
 * - 洞见生成窗口额度加额
 * - 语音聊天周额度加额
 */
@RestController
@RequestMapping("/admin/ai/quotas")
@Tag(name = "管理员-AI额度管理", description = "管理员调整学生 AI/洞见/语音额度并查看用量")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiQuotaController extends BaseController {

    private final AiQuotaService quotaService;
    private final AiConversationService conversationService;
    private final BehaviorInsightService insightService;
    private final AiVoicePracticeService voicePracticeService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminAiQuotaController(AiQuotaService quotaService,
                                  AiConversationService conversationService,
                                  BehaviorInsightService insightService,
                                  AiVoicePracticeService voicePracticeService,
                                  AdminAuditLogService adminAuditLogService,
                                  UserService userService) {
        super(userService);
        this.quotaService = quotaService;
        this.conversationService = conversationService;
        this.insightService = insightService;
        this.voicePracticeService = voicePracticeService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping
    @Operation(summary = "获取学生 AI 额度与用量")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuota(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            HttpServletRequest httpRequest
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "studentId 非法");
        }
        Map<String, Object> data = buildQuotaPayload(studentId);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.quotas.get", "query", studentId,
                    Map.of("studentId", studentId), httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping
    @Operation(summary = "更新学生 AI 额度加额")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateQuota(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @RequestBody UpdateQuotaRequest req,
            HttpServletRequest httpRequest
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "studentId 非法");
        }
        UpdateQuotaRequest body = req == null ? new UpdateQuotaRequest() : req;
        AiQuotaAdjustment updated = quotaService.updateQuota(
                studentId,
                body.getAiChatBonusWeekly(),
                body.getInsightBonusWindow(),
                body.getVoiceChatBonusWeekly()
        );
        Map<String, Object> data = buildQuotaPayload(studentId);
        try {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("studentId", studentId);
            detail.put("aiChatBonusWeekly", updated.getAiChatBonusWeekly());
            detail.put("insightBonusWindow", updated.getInsightBonusWindow());
            detail.put("voiceChatBonusWeekly", updated.getVoiceChatBonusWeekly());
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.quotas.update", "edit", studentId, detail, httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    private Map<String, Object> buildQuotaPayload(Long studentId) {
        AiQuotaAdjustment quota = quotaService.getQuota(studentId);
        int aiChatBonus = Math.max(0, quota.getAiChatBonusWeekly() == null ? 0 : quota.getAiChatBonusWeekly());
        int insightBonus = Math.max(0, quota.getInsightBonusWindow() == null ? 0 : quota.getInsightBonusWindow());
        int voiceBonus = Math.max(0, quota.getVoiceChatBonusWeekly() == null ? 0 : quota.getVoiceChatBonusWeekly());

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDateTime startOfWeek = monday.atStartOfDay();
        LocalDateTime insightWindowStart = LocalDateTime.now().minusDays(AiQuotaService.INSIGHT_WINDOW_DAYS);

        long geminiUsed = conversationService.countAssistantMessagesByModelSince(studentId, "google/gemini", startOfWeek);
        long glmUsed = conversationService.countAssistantMessagesByModelSince(studentId, "glm-", startOfWeek);
        long insightUsed =
                insightService.countByStudentSince(studentId, BehaviorSchemaVersions.INSIGHT_V2, insightWindowStart)
                        + insightService.countByStudentSince(studentId, BehaviorSchemaVersions.INSIGHT_V1, insightWindowStart);
        long voiceUsed = voicePracticeService.countTurnsByUserSince(studentId, startOfWeek);

        int geminiLimit = AiQuotaService.BASE_GEMINI_WEEKLY_LIMIT + aiChatBonus;
        int glmLimit = AiQuotaService.BASE_GLM_WEEKLY_LIMIT + aiChatBonus;
        int insightLimit = AiQuotaService.BASE_INSIGHT_WINDOW_LIMIT + insightBonus;
        int voiceLimit = AiQuotaService.BASE_VOICE_WEEKLY_LIMIT + voiceBonus;

        Map<String, Object> base = new LinkedHashMap<>();
        base.put("geminiWeekly", AiQuotaService.BASE_GEMINI_WEEKLY_LIMIT);
        base.put("glmWeekly", AiQuotaService.BASE_GLM_WEEKLY_LIMIT);
        base.put("insightWindow", AiQuotaService.BASE_INSIGHT_WINDOW_LIMIT);
        base.put("voiceChatWeekly", AiQuotaService.BASE_VOICE_WEEKLY_LIMIT);
        base.put("insightWindowDays", AiQuotaService.INSIGHT_WINDOW_DAYS);

        Map<String, Object> bonus = new LinkedHashMap<>();
        bonus.put("aiChatWeekly", aiChatBonus);
        bonus.put("insightWindow", insightBonus);
        bonus.put("voiceChatWeekly", voiceBonus);

        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("geminiWeekly", geminiLimit);
        limits.put("glmWeekly", glmLimit);
        limits.put("insightWindow", insightLimit);
        limits.put("voiceChatWeekly", voiceLimit);
        limits.put("insightWindowDays", AiQuotaService.INSIGHT_WINDOW_DAYS);

        Map<String, Object> usage = new LinkedHashMap<>();
        usage.put("geminiWeeklyUsed", geminiUsed);
        usage.put("glmWeeklyUsed", glmUsed);
        usage.put("insightWindowUsed", insightUsed);
        usage.put("voiceChatWeeklyUsed", voiceUsed);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", studentId);
        data.put("base", base);
        data.put("bonus", bonus);
        data.put("limits", limits);
        data.put("usage", usage);
        return data;
    }

    @Data
    public static class UpdateQuotaRequest {
        private Integer aiChatBonusWeekly;
        private Integer insightBonusWindow;
        private Integer voiceChatBonusWeekly;
    }
}

