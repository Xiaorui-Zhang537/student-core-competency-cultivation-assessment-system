package com.noncore.assessment.controller;

import com.noncore.assessment.service.ChatService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天会话", description = "统一最近会话与已读管理")
public class ChatController extends BaseController {

    private final ChatService chatService;

    public ChatController(ChatService chatService, com.noncore.assessment.service.UserService userService) {
        super(userService);
        this.chatService = chatService;
    }

    @GetMapping("/conversations/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的最近会话")
    public ResponseEntity<ApiResponse<PageResult<Map<String, Object>>>> myConversations(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "pinned", required = false) Boolean pinned,
            @RequestParam(value = "archived", required = false) Boolean archived
    ) {
        PageResult<Map<String, Object>> res = chatService.listMyConversations(getCurrentUserId(), pinned, archived, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/conversations/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记会话为已读")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markRead(
            @PathVariable("id") Long conversationId
    ) {
        Map<String, Object> result = chatService.markConversationRead(getCurrentUserId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}


