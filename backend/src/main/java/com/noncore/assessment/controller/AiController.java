package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.response.AiChatResponse;
import com.noncore.assessment.dto.request.CreateConversationRequest;
import com.noncore.assessment.dto.request.UpdateConversationRequest;
import com.noncore.assessment.dto.request.UpdateMemoryRequest;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.entity.AiMessage;
import com.noncore.assessment.entity.AiMemory;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.AiMemoryService;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.behavior.BehaviorEventRecorder;
import com.noncore.assessment.behavior.BehaviorEventType;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiService;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.service.file.DocumentTextExtractor;
import com.noncore.assessment.service.ai.AiGradingEnsembler;
import com.noncore.assessment.service.ai.AiGradingNormalizer;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import com.noncore.assessment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    private final AiService aiService;
    private final AiConversationService conversationService;
    private final AiMemoryService memoryService;
    private final FileStorageService fileStorageService;
    private final DocumentTextExtractor documentTextExtractor;
    private final com.noncore.assessment.config.AiConfigProperties aiConfigProperties;
    private final BehaviorEventRecorder behaviorEventRecorder;

    public AiController(AiService aiService, UserService userService,
                        AiConversationService conversationService,
                        AiMemoryService memoryService,
                        FileStorageService fileStorageService,
                        DocumentTextExtractor documentTextExtractor,
                        com.noncore.assessment.service.AiGradingHistoryService historyService,
                        com.noncore.assessment.config.AiConfigProperties aiConfigProperties,
                        BehaviorEventRecorder behaviorEventRecorder) {
        super(userService);
        this.aiService = aiService;
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.fileStorageService = fileStorageService;
        this.documentTextExtractor = documentTextExtractor;
        this.historyService = historyService;
        this.aiConfigProperties = aiConfigProperties;
        this.behaviorEventRecorder = behaviorEventRecorder;
    }

    private final com.noncore.assessment.service.AiGradingHistoryService historyService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AI聊天", description = "基于课程与学生上下文的AI聊天（非流式）")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        if (request.getStudentIds() != null && request.getStudentIds().size() > 5) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多选择 5 名学生");
        }
        Long userId = getCurrentUserId();
        String targetModel = resolveModel(request, userId);

        if (hasRole("STUDENT") && targetModel != null && targetModel.startsWith("google/")) {
            java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
            long used = conversationService.countAssistantMessagesByModelSince(userId, "google/gemini", startOfDay);
            if (used >= 20) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "今日 Gemini 使用次数已达上限（20次），请切换其它模型或明日再试");
            }
        }
        if (hasRole("STUDENT") && targetModel != null && targetModel.startsWith("glm-")) {
            java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
            long used = conversationService.countAssistantMessagesByModelSince(userId, "glm-", sevenDaysAgo);
            if (used >= 20) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "过去7天 GLM 使用次数已达上限（20次），请稍后再试");
            }
        }
        // 确定会话
        Long convId = request.getConversationId();
        if (convId == null) {
            String title = null;
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                var last = request.getMessages().get(request.getMessages().size() - 1);
                if (last != null && last.getContent() != null) {
                    String c = last.getContent().trim();
                    title = c.length() > 20 ? c.substring(0, 20) : c;
                }
            }
            var created = conversationService.createConversation(userId, title, targetModel, request.getProvider());
            convId = created.getId();
        } else {
            // 校验会话归属
            conversationService.getConversation(userId, convId);
        }

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            var last = request.getMessages().get(request.getMessages().size() - 1);
            if (last != null && last.getContent() != null) {
                // 关键：判定“首次提问/追问”不能依赖 request.conversationId（前端通常总会携带 conversationId）
                long msgCountBefore = 0;
                try {
                    msgCountBefore = conversationService.countMessages(userId, convId);
                } catch (Exception ignored) { }
                var m = conversationService.appendMessage(userId, convId, last.getRole(), last.getContent(), request.getAttachmentFileIds());
                // 行为记录：AI 提问/追问（仅记录事实，不评价，不算分）
                try {
                    if (hasRole("STUDENT") && "user".equalsIgnoreCase(last.getRole())) {
                        boolean isFirstTurn = msgCountBefore <= 0;
                        java.util.Map<String, Object> meta = new java.util.HashMap<>();
                        meta.put("messageId", m != null ? m.getId() : null);
                        meta.put("provider", request.getProvider());
                        meta.put("model", request.getModel());
                        behaviorEventRecorder.record(
                                userId,
                                request.getCourseId(),
                                isFirstTurn ? BehaviorEventType.AI_QUESTION : BehaviorEventType.AI_FOLLOW_UP,
                                "ai_conversation",
                                convId,
                                meta
                        );
                    }
                } catch (Exception ignored) {}
            }
        }

        // 生成答案
        String answer = aiService.generateAnswer(request, userId);

        // 记录助手消息
        var assistant = conversationService.appendMessage(userId, convId, "assistant", answer, null);

        return ResponseEntity.ok(ApiResponse.success(new AiChatResponse(answer, convId, assistant.getId())));
    }

    private String resolveModel(AiChatRequest request, Long userId) {
        String defaultModel = aiConfigProperties.getDeepseek().getModel();
        if (request.getConversationId() != null) {
            try {
                AiConversation conv = conversationService.getConversation(userId, request.getConversationId());
                if (conv != null) {
                    return conversationService.normalizeModel(conv.getModel());
                }
            } catch (Exception ignored) { }
        }
        String requested = request.getModel();
        if (requested == null || requested.isBlank()) return defaultModel;
        return conversationService.normalizeModel(requested);
    }

    @PostMapping("/grade/files")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改（按文件ID批量）", description = "从文件存储读取文本（支持 docx/pdf/txt），使用作文批改 Prompt，可选 JSON-only 输出")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> gradeByFiles(@RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId();
        java.util.List<Integer> ids = (java.util.List<Integer>) body.getOrDefault("fileIds", java.util.List.of());
        java.util.List<Long> fileIds = ids.stream().map(Integer::longValue).toList();
        String model = (String) body.get("model");
        // 允许 google/* 与 glm-*，其余回退默认（避免前端选了 GLM 但后端被强制改为 Gemini）
        if (model == null || model.isBlank()) {
            model = "google/gemini-2.5-pro";
        } else if (!(model.startsWith("google/") || model.startsWith("glm-"))) {
            model = "google/gemini-2.5-pro";
        }
        // 批改场景：默认强制 JSON-only
        Boolean jsonOnly = (Boolean) body.get("jsonOnly");
        if (jsonOnly == null) jsonOnly = Boolean.TRUE;
        Boolean usePrompt = (Boolean) body.get("useGradingPrompt");
        if (usePrompt == null) usePrompt = Boolean.TRUE;
        // 稳定化参数（可选）：默认保持旧行为（samples=1）
        int samplesRequested = toInt(body.get("samples"), 1);
        if (samplesRequested < 1) samplesRequested = 1;
        if (samplesRequested > 3) samplesRequested = 3;
        double diffThreshold = toDouble(body.get("diffThreshold"), 0.8);
        if (diffThreshold < 0) diffThreshold = 0;
        if (diffThreshold > 5) diffThreshold = 5;

        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
        for (Long fid : fileIds) {
            try {
                var info = fileStorageService.getFileInfo(fid);
                String fileName = info != null ? (info.getOriginalName() != null ? info.getOriginalName() : info.getStoredName()) : ("#" + fid);
                byte[] bytes = fileStorageService.downloadFile(fid, userId);
                String text = documentTextExtractor.extractText(new java.io.ByteArrayInputStream(bytes), fileName, info != null ? info.getMimeType() : null);
                // 组装请求（使用 setter 而非全参构造）
                var req = new com.noncore.assessment.dto.request.AiChatRequest();
                req.setMessages(java.util.List.of(new com.noncore.assessment.dto.request.AiChatRequest.Message("user", text)));
                req.setModel(model);
                req.setJsonOnly(jsonOnly);
                req.setUseGradingPrompt(usePrompt);
                // JSON-only：支持稳定化取样；非 JSON-only 保持一次调用（供前端 fallback 调试用）
                String respText;
                java.util.Map<String, Object> parsed;
                if (Boolean.TRUE.equals(jsonOnly) && samplesRequested > 1) {
                    var stable = generateStableJsonOnly(req, userId, samplesRequested, diffThreshold);
                    parsed = stable.get("result") instanceof java.util.Map<?,?> m ? (java.util.Map<String, Object>) m : java.util.Map.of();
                    respText = String.valueOf(stable.getOrDefault("rawJson", ""));
                } else {
                    respText = Boolean.TRUE.equals(jsonOnly)
                            ? aiService.generateAnswerJsonOnly(req, userId)
                            : aiService.generateAnswer(req, userId);
                    java.util.Map<String, Object> p = Boolean.TRUE.equals(jsonOnly)
                            ? com.noncore.assessment.util.Jsons.parseObject(respText)
                            : java.util.Map.of("text", respText);
                    // 单次也补齐 overall，保持输出一致（并同步 rawJson）
                    if (Boolean.TRUE.equals(jsonOnly)) {
                        p = AiGradingNormalizer.normalize(p);
                        respText = com.noncore.assessment.util.Jsons.toJson(p);
                    }
                    parsed = p;
                }
                // save history when jsonOnly
                if (jsonOnly) {
                    Double finalScore = null;
                    try {
                        finalScore = AiGradingNormalizer.extractFinalScore05(parsed);
                    } catch (Exception ignored) {}
                    var rec = new com.noncore.assessment.entity.AiGradingHistory();
                    rec.setTeacherId(userId);
                    rec.setFileId(fid);
                    rec.setFileName(fileName);
                    rec.setModel(model);
                    rec.setFinalScore(finalScore);
                    rec.setRawJson(respText);
                    rec.setCreatedAt(java.time.LocalDateTime.now());
                    historyService.save(rec);
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    row.put("fileId", fid);
                    row.put("fileName", fileName);
                    row.put("result", parsed);
                    row.put("historyId", rec.getId());
                    results.add(row);
                } else {
                    results.add(java.util.Map.of(
                            "fileId", fid,
                            "fileName", fileName,
                            "result", parsed
                    ));
                }
            } catch (Exception e) {
                String fileNameFallback = null;
                try {
                    var info = fileStorageService.getFileInfo(fid);
                    fileNameFallback = info != null ? (info.getOriginalName() != null ? info.getOriginalName() : info.getStoredName()) : ("#" + fid);
                } catch (Exception ignored) {}
                results.add(java.util.Map.of(
                        "fileId", fid,
                        "fileName", fileNameFallback != null ? fileNameFallback : String.valueOf(fid),
                        "error", e.getMessage()
                ));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("results", results)));
    }

    @PostMapping("/grade/essay")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改单篇作文（强制 JSON 输出）")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> gradeEssay(@Valid @RequestBody AiChatRequest request) {
        Long userId = getCurrentUserId();
        // 稳定化参数：默认保持旧行为（samples=1）
        int samplesRequested = request.getSamples() == null ? 1 : request.getSamples();
        if (samplesRequested < 1) samplesRequested = 1;
        if (samplesRequested > 3) samplesRequested = 3;
        double diffThreshold = request.getDiffThreshold() == null ? 0.8 : request.getDiffThreshold();
        if (diffThreshold < 0) diffThreshold = 0;
        if (diffThreshold > 5) diffThreshold = 5;

        // 强制 JSON-only（与接口语义一致）
        request.setJsonOnly(Boolean.TRUE);

        String json;
        java.util.Map<String, Object> parsed;
        if (samplesRequested > 1) {
            var stable = generateStableJsonOnly(request, userId, samplesRequested, diffThreshold);
            parsed = stable.get("result") instanceof java.util.Map<?,?> m ? (java.util.Map<String, Object>) m : java.util.Map.of();
            json = String.valueOf(stable.getOrDefault("rawJson", ""));
        } else {
            json = aiService.generateAnswerJsonOnly(request, userId);
            parsed = AiGradingNormalizer.normalize(com.noncore.assessment.util.Jsons.parseObject(json));
            json = com.noncore.assessment.util.Jsons.toJson(parsed);
        }
        try {
            // 写入 AI 批改历史（essay 无文件ID）
            try {
                Double finalScore = null;
                finalScore = AiGradingNormalizer.extractFinalScore05(parsed);
                var rec = new com.noncore.assessment.entity.AiGradingHistory();
                rec.setTeacherId(userId);
                rec.setFileId(null);
                rec.setFileName(null);
                rec.setModel(request.getModel());
                rec.setFinalScore(finalScore);
                rec.setRawJson(json);
                rec.setCreatedAt(java.time.LocalDateTime.now());
                historyService.save(rec);
                java.util.Map<String,Object> out = new java.util.HashMap<>(parsed);
                out.put("historyId", rec.getId());
                return ResponseEntity.ok(ApiResponse.success(out));
            } catch (Exception ignore) {
                return ResponseEntity.ok(ApiResponse.success(parsed));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                    "error", "INVALID_JSON",
                    "message", ex.getMessage(),
                    "raw", json
            )));
        }
    }

    /**
     * 对 JSON-only 批改执行“2 次均值 + 分差大则第 3 次 + 取最近对”的稳定化算法，并输出聚合后的 JSON。
     *
     * @param baseRequest 已构造好的请求（messages/model/useGradingPrompt/jsonOnly 等）
     * @param userId 当前教师ID
     * @param samplesRequested 请求的取样次数（建议 2）
     * @param diffThreshold 触发第 3 次的阈值（0~5 标尺，推荐 0.8）
     * @return { result: Map, rawJson: String }
     */
    private java.util.Map<String, Object> generateStableJsonOnly(AiChatRequest baseRequest, Long userId, int samplesRequested, double diffThreshold) {
        // 仅允许 1~3
        int req = Math.max(1, Math.min(samplesRequested, 3));

        java.util.List<java.util.Map<String, Object>> runs = new java.util.ArrayList<>();
        java.util.List<String> rawRuns = new java.util.ArrayList<>();

        // 1) 先跑两次（或一次）
        int firstBatch = Math.min(req, 2);
        for (int i = 0; i < firstBatch; i++) {
            String json = aiService.generateAnswerJsonOnly(baseRequest, userId);
            rawRuns.add(json);
            try {
                java.util.Map<String, Object> parsed = com.noncore.assessment.util.Jsons.parseObject(json);
                runs.add(AiGradingNormalizer.normalize(parsed));
            } catch (Exception ignored) {
                // 忽略无效 JSON（后续若无可用结果则整体返回 INVALID_JSON）
            }
        }

        // 2) 若两次都有效且分差过大，则补第 3 次（即使 samplesRequested=2，也允许触发一次“稳定补采样”）
        if (runs.size() >= 2 && runs.size() < 3) {
            double s1 = AiGradingNormalizer.extractFinalScore05(runs.get(0));
            double s2 = AiGradingNormalizer.extractFinalScore05(runs.get(1));
            if (Math.abs(s1 - s2) > diffThreshold) {
                try {
                    String json3 = aiService.generateAnswerJsonOnly(baseRequest, userId);
                    rawRuns.add(json3);
                    java.util.Map<String, Object> parsed3 = com.noncore.assessment.util.Jsons.parseObject(json3);
                    runs.add(AiGradingNormalizer.normalize(parsed3));
                } catch (Exception ignored) {}
            }
        }

        // 3) 聚合：至少 1 条有效结果
        if (runs.isEmpty()) {
            // 将最后一次 raw 透出，便于排障
            String raw = rawRuns.isEmpty() ? "" : rawRuns.get(rawRuns.size() - 1);
            return java.util.Map.of(
                    "result", java.util.Map.of("error", "INVALID_JSON", "message", "Invalid JSON returned by model", "raw", raw),
                    "rawJson", raw
            );
        }

        java.util.Map<String, Object> merged = AiGradingEnsembler.ensemble(runs, req, diffThreshold);
        String rawJson = com.noncore.assessment.util.Jsons.toJson(merged);
        return java.util.Map.of("result", merged, "rawJson", rawJson);
    }

    private int toInt(Object v, int def) {
        if (v == null) return def;
        try {
            if (v instanceof Number n) return n.intValue();
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception ignored) {
            return def;
        }
    }

    private double toDouble(Object v, double def) {
        if (v == null) return def;
        try {
            if (v instanceof Number n) return n.doubleValue();
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception ignored) {
            return def;
        }
    }

    @PostMapping("/grade/essay/batch")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批量批改作文（强制 JSON 输出）")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> gradeEssayBatch(@Valid @RequestBody java.util.List<AiChatRequest> requests) {
        Long userId = getCurrentUserId();
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
        for (AiChatRequest req : requests) {
            // 稳定化参数：默认保持旧行为（samples=1）
            int samplesRequested = req.getSamples() == null ? 1 : req.getSamples();
            if (samplesRequested < 1) samplesRequested = 1;
            if (samplesRequested > 3) samplesRequested = 3;
            double diffThreshold = req.getDiffThreshold() == null ? 0.8 : req.getDiffThreshold();
            if (diffThreshold < 0) diffThreshold = 0;
            if (diffThreshold > 5) diffThreshold = 5;

            req.setJsonOnly(Boolean.TRUE);

            String json;
            java.util.Map<String, Object> parsed;
            if (samplesRequested > 1) {
                var stable = generateStableJsonOnly(req, userId, samplesRequested, diffThreshold);
                parsed = stable.get("result") instanceof java.util.Map<?,?> m ? (java.util.Map<String, Object>) m : java.util.Map.of();
                json = String.valueOf(stable.getOrDefault("rawJson", ""));
            } else {
                json = aiService.generateAnswerJsonOnly(req, userId);
                parsed = AiGradingNormalizer.normalize(com.noncore.assessment.util.Jsons.parseObject(json));
                json = com.noncore.assessment.util.Jsons.toJson(parsed);
            }
            try {
                results.add(parsed);
                // history
                Double finalScore = null;
                try {
                    finalScore = AiGradingNormalizer.extractFinalScore05(parsed);
                } catch (Exception ignored) {}
                var rec = new com.noncore.assessment.entity.AiGradingHistory();
                rec.setTeacherId(userId);
                rec.setFileId(null);
                rec.setFileName(null);
                rec.setModel(req.getModel());
                rec.setFinalScore(finalScore);
                rec.setRawJson(json);
                rec.setCreatedAt(java.time.LocalDateTime.now());
                historyService.save(rec);
            } catch (IllegalArgumentException ex) {
                results.add(java.util.Map.of(
                        "error", "INVALID_JSON",
                        "message", ex.getMessage(),
                        "raw", json
                ));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/grade/history")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改历史-分页")
    public ResponseEntity<ApiResponse<com.noncore.assessment.util.PageResult<com.noncore.assessment.entity.AiGradingHistory>>> history(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        var res = historyService.list(getCurrentUserId(), q, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/grade/history/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改历史-详情")
    public ResponseEntity<ApiResponse<com.noncore.assessment.entity.AiGradingHistory>> historyDetail(@PathVariable Long id) {
        var rec = historyService.get(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(rec));
    }

    @DeleteMapping("/grade/history/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改历史-删除")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(@PathVariable Long id) {
        historyService.delete(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 兼容某些环境禁用 DELETE 的情况，提供 POST 兜底
    @PostMapping("/grade/history/{id}/delete")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改历史-删除(POST 兼容)")
    public ResponseEntity<ApiResponse<Void>> deleteHistoryPost(@PathVariable Long id) {
        historyService.delete(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 会话管理
    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "新建会话")
    public ResponseEntity<ApiResponse<AiConversation>> createConversation(@RequestBody CreateConversationRequest req) {
        AiConversation c = conversationService.createConversation(getCurrentUserId(),
                req.getTitle(), req.getModel(), req.getProvider());
        return ResponseEntity.ok(ApiResponse.success(c));
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "会话列表")
    public ResponseEntity<ApiResponse<PageResult<AiConversation>>> listConversations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        PageResult<AiConversation> res = conversationService.listConversations(getCurrentUserId(), q, pinned, archived, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新会话")
    public ResponseEntity<ApiResponse<Void>> updateConversation(@PathVariable Long id, @RequestBody UpdateConversationRequest req) {
        conversationService.updateConversation(getCurrentUserId(), id, req.getTitle(), req.getPinned(), req.getArchived());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除会话")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "会话消息列表（默认最多100条）")
    public ResponseEntity<ApiResponse<PageResult<AiMessage>>> listMessages(@PathVariable Long id,
                                                                          @RequestParam(required = false) Integer page,
                                                                          @RequestParam(required = false) Integer size) {
        PageResult<AiMessage> res = conversationService.listMessages(getCurrentUserId(), id, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    // 用户长期记忆
    @GetMapping("/memory")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户记忆")
    public ResponseEntity<ApiResponse<AiMemory>> getMemory() {
        return ResponseEntity.ok(ApiResponse.success(memoryService.getMemory(getCurrentUserId())));
    }

    @PutMapping("/memory")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新用户记忆")
    public ResponseEntity<ApiResponse<AiMemory>> updateMemory(@RequestBody UpdateMemoryRequest req) {
        AiMemory m = memoryService.updateMemory(getCurrentUserId(), req.getEnabled(), req.getContent());
        return ResponseEntity.ok(ApiResponse.success(m));
    }
}

