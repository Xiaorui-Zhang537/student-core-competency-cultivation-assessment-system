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
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiService;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.service.file.DocumentTextExtractor;
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

    public AiController(AiService aiService, UserService userService,
                        AiConversationService conversationService,
                        AiMemoryService memoryService,
                        FileStorageService fileStorageService,
                        DocumentTextExtractor documentTextExtractor,
                        com.noncore.assessment.service.AiGradingHistoryService historyService) {
        super(userService);
        this.aiService = aiService;
        this.conversationService = conversationService;
        this.memoryService = memoryService;
        this.fileStorageService = fileStorageService;
        this.documentTextExtractor = documentTextExtractor;
        this.historyService = historyService;
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
            var created = conversationService.createConversation(userId, title, request.getModel(), request.getProvider());
            convId = created.getId();
        } else {
            // 校验会话归属
            conversationService.getConversation(userId, convId);
        }

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            var last = request.getMessages().get(request.getMessages().size() - 1);
            if (last != null && last.getContent() != null) {
                var m = conversationService.appendMessage(userId, convId, last.getRole(), last.getContent(), request.getAttachmentFileIds());
            }
        }

        // 生成答案
        String answer = aiService.generateAnswer(request, userId);

        // 记录助手消息
        var assistant = conversationService.appendMessage(userId, convId, "assistant", answer, null);

        return ResponseEntity.ok(ApiResponse.success(new AiChatResponse(answer, convId, assistant.getId())));
    }

    @PostMapping("/grade/files")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批改（按文件ID批量）", description = "从文件存储读取文本（支持 docx/pdf/txt），使用作文批改 Prompt，可选 JSON-only 输出")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> gradeByFiles(@RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId();
        java.util.List<Integer> ids = (java.util.List<Integer>) body.getOrDefault("fileIds", java.util.List.of());
        java.util.List<Long> fileIds = ids.stream().map(Integer::longValue).toList();
        String model = (String) body.get("model");
        if (model == null || !model.startsWith("google/")) {
            model = "google/gemini-2.5-pro";
        }
        // 批改场景：默认强制 JSON-only
        Boolean jsonOnly = (Boolean) body.get("jsonOnly");
        if (jsonOnly == null) jsonOnly = Boolean.TRUE;
        Boolean usePrompt = (Boolean) body.get("useGradingPrompt");
        if (usePrompt == null) usePrompt = Boolean.TRUE;

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
                String resp = jsonOnly ? aiService.generateAnswerJsonOnly(req, userId) : aiService.generateAnswer(req, userId);
                java.util.Map<String, Object> parsed = jsonOnly
                        ? com.noncore.assessment.util.Jsons.parseObject(resp)
                        : java.util.Map.of("text", resp);
                results.add(java.util.Map.of(
                        "fileId", fid,
                        "fileName", fileName,
                        "result", parsed
                ));
                // save history when jsonOnly
                if (jsonOnly) {
                    Double finalScore = null;
                    try {
                        Object ov = ((java.util.Map<?,?>)parsed).get("overall");
                        if (ov instanceof java.util.Map<?,?> ovm) {
                            Object fs = ovm.get("final_score");
                            if (fs != null) finalScore = Double.valueOf(String.valueOf(fs));
                        }
                    } catch (Exception ignored) {}
                    var rec = new com.noncore.assessment.entity.AiGradingHistory();
                    rec.setTeacherId(userId);
                    rec.setFileId(fid);
                    rec.setFileName(fileName);
                    rec.setModel(model);
                    rec.setFinalScore(finalScore);
                    rec.setRawJson(resp);
                    rec.setCreatedAt(java.time.LocalDateTime.now());
                    historyService.save(rec);
                }
            } catch (Exception e) {
                results.add(java.util.Map.of(
                        "fileId", fid,
                        "fileName", String.valueOf(fid),
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
        String json = aiService.generateAnswerJsonOnly(request, userId);
        try {
            java.util.Map<String, Object> parsed = com.noncore.assessment.util.Jsons.parseObject(json);
            return ResponseEntity.ok(ApiResponse.success(parsed));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                    "error", "INVALID_JSON",
                    "message", ex.getMessage(),
                    "raw", json
            )));
        }
    }

    @PostMapping("/grade/essay/batch")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "AI 批量批改作文（强制 JSON 输出）")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> gradeEssayBatch(@Valid @RequestBody java.util.List<AiChatRequest> requests) {
        Long userId = getCurrentUserId();
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
        for (AiChatRequest req : requests) {
            String json = aiService.generateAnswerJsonOnly(req, userId);
            try {
                var parsed = com.noncore.assessment.util.Jsons.parseObject(json);
                results.add(parsed);
                // history
                Double finalScore = null;
                try {
                    Object ov = ((java.util.Map<?,?>)parsed).get("overall");
                    if (ov instanceof java.util.Map<?,?> ovm) {
                        Object fs = ovm.get("final_score");
                        if (fs != null) finalScore = Double.valueOf(String.valueOf(fs));
                    }
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

