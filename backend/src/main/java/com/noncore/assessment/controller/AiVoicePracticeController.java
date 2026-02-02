package com.noncore.assessment.controller;

import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.AiVoiceTurn;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiVoicePracticeService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 口语训练（独立记录）。
 */
@RestController
@RequestMapping("/ai/voice")
public class AiVoicePracticeController extends BaseController {

    private final AiVoicePracticeService voicePracticeService;

    public AiVoicePracticeController(UserService userService, AiVoicePracticeService voicePracticeService) {
        super(userService);
        this.voicePracticeService = voicePracticeService;
    }

    @PostMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建语音会话", description = "创建一个口语训练语音会话（不写入 AI 聊天会话）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession(@Valid @RequestBody CreateVoiceSessionRequest req) {
        Long userId = getCurrentUserId();
        String title = req.getTitle();
        if (title == null || title.isBlank()) {
            title = (req.getScenario() != null && !req.getScenario().isBlank())
                    ? ("语音练习-" + req.getScenario().trim())
                    : "语音练习";
        }
        AiVoiceSession s = voicePracticeService.createSession(
                userId,
                title,
                req.getModel(),
                req.getMode(),
                req.getLocale(),
                req.getScenario()
        );
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "sessionId", s.getId(),
                "id", s.getId()
        )));
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "语音会话列表", description = "分页获取当前用户的语音会话列表")
    public ResponseEntity<ApiResponse<List<AiVoiceSession>>> listSessions(@RequestParam(value = "q", required = false) String q,
                                                                         @RequestParam(value = "page", required = false) Integer page,
                                                                         @RequestParam(value = "size", required = false) Integer size) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(voicePracticeService.listSessions(userId, q, page, size)));
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "语音会话详情", description = "获取语音会话详情")
    public ResponseEntity<ApiResponse<AiVoiceSession>> getSession(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(voicePracticeService.getSession(userId, sessionId)));
    }

    @GetMapping("/sessions/{sessionId}/turns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "语音回合列表", description = "获取语音会话下的回合列表（按时间正序）")
    public ResponseEntity<ApiResponse<List<AiVoiceTurn>>> listTurns(@PathVariable Long sessionId,
                                                                    @RequestParam(value = "page", required = false) Integer page,
                                                                    @RequestParam(value = "size", required = false) Integer size) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(voicePracticeService.listTurns(userId, sessionId, page, size)));
    }

    @PatchMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新语音会话", description = "更新语音会话（标题/置顶）")
    public ResponseEntity<ApiResponse<AiVoiceSession>> updateSession(@PathVariable Long sessionId,
                                                                     @Valid @RequestBody UpdateVoiceSessionRequest req) {
        Long userId = getCurrentUserId();
        AiVoiceSession s = voicePracticeService.updateSession(userId, sessionId, req.getTitle(), req.getPinned());
        return ResponseEntity.ok(ApiResponse.success(s));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除语音会话", description = "删除语音会话（软删除），回合记录将不再展示")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteSession(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        voicePracticeService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("deleted", true)));
    }

    @Data
    public static class CreateVoiceSessionRequest {
        private String title;
        private String model;
        private String mode;
        private String locale;
        private String scenario;
    }

    @Data
    public static class UpdateVoiceSessionRequest {
        private String title;
        private Boolean pinned;
    }
}

