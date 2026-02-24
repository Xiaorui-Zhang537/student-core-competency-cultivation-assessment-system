package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.AiVoiceTurn;
import com.noncore.assessment.service.AiVoicePracticeService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理员-口语训练审计接口。
 *
 * <p>说明：现有 /ai/voice/* 仅返回当前用户数据；管理员通过本接口按 studentId 查看会话与回合。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/ai/voice")
@Tag(name = "管理员-口语训练审计", description = "管理员查看学生口语训练会话/回合（审计）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiVoiceAuditController extends BaseController {

    private final AiVoicePracticeService voicePracticeService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminAiVoiceAuditController(AiVoicePracticeService voicePracticeService,
                                       AdminAuditLogService adminAuditLogService,
                                       UserService userService) {
        super(userService);
        this.voicePracticeService = voicePracticeService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/sessions")
    @Operation(summary = "管理员按学生查询语音会话列表", description = "分页获取指定学生的语音会话列表")
    public ResponseEntity<ApiResponse<List<AiVoiceSession>>> listSessions(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest httpRequest
    ) {
        List<AiVoiceSession> res = voicePracticeService.listSessions(studentId, q, page, size);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.voice.sessions.list", "query", studentId,
                    Map.of("q", q, "page", page, "size", size, "rows", res == null ? 0 : res.size()),
                    httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "管理员获取语音会话详情")
    public ResponseEntity<ApiResponse<AiVoiceSession>> getSession(
            @PathVariable Long sessionId,
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            HttpServletRequest httpRequest
    ) {
        AiVoiceSession s = voicePracticeService.getSession(studentId, sessionId);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.voice.sessions.get", "query", studentId,
                    Map.of("sessionId", sessionId), httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(s));
    }

    @GetMapping("/sessions/{sessionId}/turns")
    @Operation(summary = "管理员获取语音回合列表", description = "获取语音会话下的回合列表（按时间正序）")
    public ResponseEntity<ApiResponse<List<AiVoiceTurn>>> listTurns(
            @PathVariable Long sessionId,
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest httpRequest
    ) {
        List<AiVoiceTurn> res = voicePracticeService.listTurns(studentId, sessionId, page, size);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.voice.sessions.turns", "query", studentId,
                    Map.of("sessionId", sessionId, "page", page, "size", size, "rows", res == null ? 0 : res.size()),
                    httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}

