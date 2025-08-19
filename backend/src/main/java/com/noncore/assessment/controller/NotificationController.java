package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Notification;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.noncore.assessment.dto.request.BatchNotificationRequest;
import com.noncore.assessment.dto.request.NotificationRequest;


import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 * 处理通知的发送、接收、管理等操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "通知管理", description = "通知发送、查询、标记已读等操作")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        super(userService);
        this.notificationService = notificationService;
    }

    /**
     * 获取我的通知列表（分页）
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的通知列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResult<Notification>>> getMyNotifications(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        PageResult<Notification> notifications = notificationService.getUserNotifications(getCurrentUserId(), type, page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "获取通知详情", description = "根据通知ID获取通知详细信息")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Notification>> getNotificationDetail(@PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationDetail(notificationId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/batch/read")
    @Operation(summary = "批量标记通知为已读", description = "批量将多个通知标记为已读状态")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchMarkAsRead(@RequestBody List<Long> notificationIds) {
        Map<String, Object> result = notificationService.batchMarkAsRead(notificationIds, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/all/read")
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有通知标记为已读状态")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead() {
        int count = notificationService.markAllAsRead(getCurrentUserId());
        Map<String, Object> result = Map.of("markedCount", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的通知")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知", description = "批量删除多个通知")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteNotifications(@RequestBody List<Long> notificationIds) {
        Map<String, Object> result = notificationService.batchDeleteNotifications(notificationIds, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadCount(
            @RequestParam(value = "type", required = false) String type) {
        Integer count = notificationService.getUnreadCount(getCurrentUserId(), type);
        Map<String, Object> result = Map.of("unreadCount", count);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取通知统计信息", description = "获取当前用户的通知统计信息")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotificationStats() {
        Map<String, Object> stats = notificationService.getNotificationStats(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取与某人的会话（分页）
     */
    @GetMapping("/conversation")
    @Operation(summary = "获取会话", description = "获取当前用户与指定对端的会话消息")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResult<Notification>>> getConversation(
            @RequestParam("peerId") Long peerId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        PageResult<Notification> result = notificationService.getConversation(getCurrentUserId(), peerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 将与某人的会话全部标记为已读
     */
    @PostMapping("/conversation/read")
    @Operation(summary = "标记会话已读")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readConversation(@RequestParam("peerId") Long peerId) {
        int count = notificationService.markConversationAsRead(getCurrentUserId(), peerId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("marked", count)));
    }
    /**
     * 发送通知（教师专用）
     */
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "教师向学生发送通知")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Notification>> sendNotification(@RequestBody NotificationRequest request) {
        Notification notification = notificationService.sendNotification(
            request.getRecipientId(), getCurrentUserId(), request.getTitle(), request.getContent(),
            request.getType(), request.getCategory(), request.getPriority(),
            request.getRelatedType(), request.getRelatedId()
        );
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    /**
     * 批量发送通知（教师专用）
     */
    @PostMapping("/batch/send")
    @Operation(summary = "批量发送通知", description = "教师批量向学生发送通知")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchSendNotification(@RequestBody BatchNotificationRequest request) {
        Map<String, Object> result = notificationService.batchSendNotification(
            request.getRecipientIds(), getCurrentUserId(), request.getTitle(), request.getContent(),
            request.getType(), request.getCategory(), request.getPriority(),
            request.getRelatedType(), request.getRelatedId()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 发送作业相关通知（教师专用）
     */
    @PostMapping("/assignment/{assignmentId}")
    @Operation(summary = "发送作业相关通知", description = "向作业相关的学生发送通知")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendAssignmentNotification(
            @PathVariable Long assignmentId,
            @RequestParam("type") String type,
            @RequestParam(value = "customMessage", required = false) String customMessage) {
        Map<String, Object> result = notificationService.sendAssignmentNotification(assignmentId, type, customMessage);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 发送课程相关通知（教师专用）
     */
    @PostMapping("/course/{courseId}")
    @Operation(summary = "发送课程相关通知", description = "向课程相关的学生发送通知")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCourseNotification(
            @PathVariable Long courseId,
            @RequestParam("type") String type,
            @RequestParam(value = "customMessage", required = false) String customMessage) {
        Map<String, Object> result = notificationService.sendCourseNotification(courseId, type, customMessage);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
} 