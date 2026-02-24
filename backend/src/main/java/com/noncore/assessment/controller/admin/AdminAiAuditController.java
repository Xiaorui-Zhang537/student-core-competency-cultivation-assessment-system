package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.entity.AiMessage;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
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

import java.util.Map;

/**
 * 管理员-AI 问答审计接口。
 *
 * <p>说明：现有 /ai/conversations 与 /ai/conversations/{id}/messages 仅允许访问当前登录用户数据；管理员通过本接口按 studentId 审计。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/ai")
@Tag(name = "管理员-AI审计", description = "管理员查看学生 AI 问答与消息记录（审计）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiAuditController extends BaseController {

    private final AiConversationService conversationService;
    private final AdminAuditLogService adminAuditLogService;

    public AdminAiAuditController(AiConversationService conversationService,
                                  AdminAuditLogService adminAuditLogService,
                                  UserService userService) {
        super(userService);
        this.conversationService = conversationService;
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping("/conversations")
    @Operation(summary = "管理员按学生查询 AI 会话列表（分页）")
    public ResponseEntity<ApiResponse<PageResult<AiConversation>>> listConversations(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest httpRequest
    ) {
        PageResult<AiConversation> res = conversationService.listConversations(studentId, q, pinned, archived, page, size);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.conversations.list", "query", studentId,
                    Map.of("q", q, "pinned", pinned, "archived", archived, "page", page, "size", size, "rows", res == null ? 0 : (res.getItems() == null ? 0 : res.getItems().size())),
                    httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "管理员获取 AI 会话详情")
    public ResponseEntity<ApiResponse<AiConversation>> getConversation(
            @PathVariable Long id,
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            HttpServletRequest httpRequest
    ) {
        AiConversation c = conversationService.getConversation(studentId, id);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.conversations.get", "query", studentId,
                    Map.of("conversationId", id), httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(c));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "管理员按学生查询会话消息列表（分页）")
    public ResponseEntity<ApiResponse<PageResult<AiMessage>>> listMessages(
            @PathVariable Long id,
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest httpRequest
    ) {
        PageResult<AiMessage> res = conversationService.listMessages(studentId, id, page, size);
        try {
            adminAuditLogService.record(getCurrentUserId(), "admin.ai.conversations.messages", "query", studentId,
                    Map.of("conversationId", id, "page", page, "size", size, "rows", res == null ? 0 : (res.getItems() == null ? 0 : res.getItems().size())),
                    httpRequest);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}

