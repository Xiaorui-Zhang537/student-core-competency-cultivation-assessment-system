package com.noncore.assessment.controller;

import com.noncore.assessment.service.ChatService;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.entity.Notification;
import com.noncore.assessment.dto.request.NotificationRequest;
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
    private final NotificationService notificationService;

    public ChatController(ChatService chatService, com.noncore.assessment.service.UserService userService, NotificationService notificationService) {
        super(userService);
        this.chatService = chatService;
        this.notificationService = notificationService;
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

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取与某人的会话消息")
    public ResponseEntity<ApiResponse<PageResult<Notification>>> getMessages(
            @RequestParam("peerId") Long peerId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        PageResult<Notification> result = notificationService.getConversation(getCurrentUserId(), peerId, page, size, courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "发送聊天消息")
    public ResponseEntity<ApiResponse<Notification>> sendMessage(@RequestBody NotificationRequest request) {
        Long senderId = getCurrentUserId();
        Notification n = notificationService.sendMessage(
                senderId,
                request.getRecipientId(),
                request.getContent(),
                request.getRelatedType(),
                request.getRelatedId(),
                request.getAttachmentFileIds()
        );
        return ResponseEntity.ok(ApiResponse.success(n));
    }

    @PutMapping("/conversations/peer/{peerId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "按对端标记会话为已读")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markReadByPeer(@PathVariable("peerId") Long peerId,
                                                                          @RequestParam(value = "courseId", required = false) Long courseId) {
        int count = notificationService.markConversationAsRead(getCurrentUserId(), peerId, courseId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("marked", count)));
    }

    @PutMapping("/conversations/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "归档/取消归档会话")
    public ResponseEntity<ApiResponse<Map<String, Object>>> archiveConversation(@PathVariable("id") Long conversationId,
                                                                               @RequestParam("archived") boolean archived) {
        // 简化：直接在 mapper 更新归档标记
        int updated = chatService.setArchived(getCurrentUserId(), conversationId, archived);
        return ResponseEntity.ok(ApiResponse.success(Map.of("updated", updated)));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取聊天未读总数")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadTotal() {
        Integer unread = chatService.getUnreadTotal(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", unread)));
    }
}


