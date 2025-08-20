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

    public AiController(AiService aiService, UserService userService,
                        AiConversationService conversationService,
                        AiMemoryService memoryService) {
        super(userService);
        this.aiService = aiService;
        this.conversationService = conversationService;
        this.memoryService = memoryService;
    }

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

        // 记录用户消息（取最后一条用户输入）
        Long userMsgId = null;
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            var last = request.getMessages().get(request.getMessages().size() - 1);
            if (last != null && last.getContent() != null) {
                var m = conversationService.appendMessage(userId, convId, last.getRole(), last.getContent(), request.getAttachmentFileIds());
                userMsgId = m.getId();
            }
        }

        // 生成答案
        String answer = aiService.generateAnswer(request, userId);

        // 记录助手消息
        var assistant = conversationService.appendMessage(userId, convId, "assistant", answer, null);

        return ResponseEntity.ok(ApiResponse.success(new AiChatResponse(answer, convId, assistant.getId())));
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

