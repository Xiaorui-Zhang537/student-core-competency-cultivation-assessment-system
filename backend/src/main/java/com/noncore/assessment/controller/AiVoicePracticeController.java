package com.noncore.assessment.controller;

import com.noncore.assessment.behavior.BehaviorEventRecorder;
import com.noncore.assessment.behavior.BehaviorEventType;
import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.AiVoiceTurn;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiVoicePracticeService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 口语训练（独立记录）。
 */
@RestController
@RequestMapping("/ai/voice")
public class AiVoicePracticeController extends BaseController {

    private final AiVoicePracticeService voicePracticeService;
    private final BehaviorEventRecorder behaviorEventRecorder;

    public AiVoicePracticeController(UserService userService,
                                     AiVoicePracticeService voicePracticeService,
                                     BehaviorEventRecorder behaviorEventRecorder) {
        super(userService);
        this.voicePracticeService = voicePracticeService;
        this.behaviorEventRecorder = behaviorEventRecorder;
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

    /**
     * 上报口语训练音频复听/回放时长（按秒增量）。
     *
     * <p>说明：该接口仅用于记录“发生了复听复盘”这一事实，不代表评价、不算分。</p>
     */
    @PostMapping("/replay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上报口语复听时长", description = "按秒上报口语训练音频复听/回放的增量时长（用户录音/AI 回复音频）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reportReplay(@Valid @RequestBody ReportReplayRequest req) {
        Long userId = getCurrentUserId();
        // 仅学生行为进入行为证据（教师端查看/演示不计入）
        if (!hasRole("STUDENT")) {
            return ResponseEntity.ok(ApiResponse.success(Map.of("recorded", false)));
        }
        Integer delta = req.getDeltaSeconds();
        if (delta == null || delta <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "deltaSeconds 必须为正整数");
        }
        if (delta > 1800) { // 单次最大 30 分钟，避免异常上报
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "deltaSeconds 超出上限（<=1800）");
        }
        String role = req.getAudioRole() == null ? "" : req.getAudioRole().trim().toLowerCase();
        if (!"user".equals(role) && !"assistant".equals(role)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "audioRole 仅支持 user/assistant");
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("sessionId", req.getSessionId());
        meta.put("turnId", req.getTurnId());
        meta.put("audioRole", role);
        meta.put("deltaSeconds", delta);
        meta.put("fileId", req.getFileId());
        meta.put("messageId", req.getMessageId());

        // relatedId 优先使用 sessionId，便于按会话追溯；允许为空（极端情况下前端尚未拿到 sessionId）
        Long relatedId = req.getSessionId();
        behaviorEventRecorder.record(
                userId,
                null, // 按约定：语音证据不绑定 courseId（全局学习行为）
                BehaviorEventType.VOICE_PRACTICE_AUDIO_REPLAY,
                "ai_voice_session",
                relatedId,
                meta
        );
        return ResponseEntity.ok(ApiResponse.success(Map.of("recorded", true)));
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

    @Data
    public static class ReportReplayRequest {
        /** 语音会话ID（可空） */
        private Long sessionId;
        /** 语音回合ID（可空） */
        private Long turnId;
        /** 音频归属：user（学生录音）/assistant（AI 回复音频） */
        @NotBlank(message = "audioRole 不能为空")
        private String audioRole;
        /** 本次增量时长（秒） */
        @Min(value = 1, message = "deltaSeconds 必须 >= 1")
        @Max(value = 1800, message = "deltaSeconds 必须 <= 1800")
        private Integer deltaSeconds;
        /** 对应文件ID（可空，用于审计定位） */
        private Long fileId;
        /** 前端消息ID（可空，用于审计定位） */
        private String messageId;
    }
}

