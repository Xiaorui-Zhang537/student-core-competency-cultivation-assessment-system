package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Notification;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/notifications")
@Tag(name = "通知管理", description = "通知发送、查询、标记已读等操作")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取我的通知列表（分页）
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的通知列表")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<PageResult<Notification>> getMyNotifications(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        try {
            Long userId = getCurrentUserId();
            PageResult<Notification> notifications = notificationService.getUserNotifications(userId, type, page, size);
            return ApiResponse.success("获取通知列表成功", notifications);
        } catch (Exception e) {
            logger.error("获取通知列表失败", e);
            return ApiResponse.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "获取通知详情", description = "根据通知ID获取通知详细信息")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Notification> getNotificationDetail(@PathVariable Long notificationId) {
        try {
            Long userId = getCurrentUserId();
            Notification notification = notificationService.getNotificationDetail(notificationId, userId);
            return ApiResponse.success("获取通知详情成功", notification);
        } catch (Exception e) {
            logger.error("获取通知详情失败", e);
            return ApiResponse.error("获取通知详情失败: " + e.getMessage());
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
        try {
            Long userId = getCurrentUserId();
            boolean success = notificationService.markAsRead(notificationId, userId);
            
            if (success) {
                return ApiResponse.success("标记为已读成功");
            } else {
                return ApiResponse.error("标记为已读失败");
            }
        } catch (Exception e) {
            logger.error("标记为已读失败", e);
            return ApiResponse.error("标记为已读失败: " + e.getMessage());
        }
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/batch/read")
    @Operation(summary = "批量标记通知为已读", description = "批量将多个通知标记为已读状态")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> batchMarkAsRead(@RequestBody List<Long> notificationIds) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = notificationService.batchMarkAsRead(notificationIds, userId);
            return ApiResponse.success("批量标记为已读完成", result);
        } catch (Exception e) {
            logger.error("批量标记为已读失败", e);
            return ApiResponse.error("批量标记为已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/all/read")
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有通知标记为已读状态")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> markAllAsRead() {
        try {
            Long userId = getCurrentUserId();
            int count = notificationService.markAllAsRead(userId);
            
            Map<String, Object> result = Map.of("markedCount", count);
            return ApiResponse.success("标记所有通知为已读成功", result);
        } catch (Exception e) {
            logger.error("标记所有通知为已读失败", e);
            return ApiResponse.error("标记所有通知为已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的通知")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Void> deleteNotification(@PathVariable Long notificationId) {
        try {
            Long userId = getCurrentUserId();
            boolean success = notificationService.deleteNotification(notificationId, userId);
            
            if (success) {
                return ApiResponse.success("删除通知成功");
            } else {
                return ApiResponse.error("删除通知失败");
            }
        } catch (Exception e) {
            logger.error("删除通知失败", e);
            return ApiResponse.error("删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知", description = "批量删除多个通知")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> batchDeleteNotifications(@RequestBody List<Long> notificationIds) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = notificationService.batchDeleteNotifications(notificationIds, userId);
            return ApiResponse.success("批量删除通知完成", result);
        } catch (Exception e) {
            logger.error("批量删除通知失败", e);
            return ApiResponse.error("批量删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> getUnreadCount(
            @RequestParam(value = "type", required = false) String type) {
        try {
            Long userId = getCurrentUserId();
            Integer count = notificationService.getUnreadCount(userId, type);
            
            Map<String, Object> result = Map.of("unreadCount", count);
            return ApiResponse.success("获取未读通知数量成功", result);
        } catch (Exception e) {
            logger.error("获取未读通知数量失败", e);
            return ApiResponse.error("获取未读通知数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取通知统计信息", description = "获取当前用户的通知统计信息")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> getNotificationStats() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> stats = notificationService.getNotificationStats(userId);
            return ApiResponse.success("获取通知统计成功", stats);
        } catch (Exception e) {
            logger.error("获取通知统计失败", e);
            return ApiResponse.error("获取通知统计失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知（教师专用）
     */
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "教师向学生发送通知")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Notification> sendNotification(@RequestBody NotificationRequest request) {
        try {
            Long senderId = getCurrentUserId();
            
            Notification notification = notificationService.sendNotification(
                request.getRecipientId(), senderId, request.getTitle(), request.getContent(),
                request.getType(), request.getCategory(), request.getPriority(),
                request.getRelatedType(), request.getRelatedId()
            );
            
            return ApiResponse.success("发送通知成功", notification);
        } catch (Exception e) {
            logger.error("发送通知失败", e);
            return ApiResponse.error("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 批量发送通知（教师专用）
     */
    @PostMapping("/batch/send")
    @Operation(summary = "批量发送通知", description = "教师批量向学生发送通知")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> batchSendNotification(@RequestBody BatchNotificationRequest request) {
        try {
            Long senderId = getCurrentUserId();
            
            Map<String, Object> result = notificationService.batchSendNotification(
                request.getRecipientIds(), senderId, request.getTitle(), request.getContent(),
                request.getType(), request.getCategory(), request.getPriority(),
                request.getRelatedType(), request.getRelatedId()
            );
            
            return ApiResponse.success("批量发送通知完成", result);
        } catch (Exception e) {
            logger.error("批量发送通知失败", e);
            return ApiResponse.error("批量发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送作业相关通知（教师专用）
     */
    @PostMapping("/assignment/{assignmentId}")
    @Operation(summary = "发送作业相关通知", description = "向作业相关的学生发送通知")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> sendAssignmentNotification(
            @PathVariable Long assignmentId,
            @RequestParam("type") String type,
            @RequestParam(value = "customMessage", required = false) String customMessage) {
        try {
            Map<String, Object> result = notificationService.sendAssignmentNotification(assignmentId, type, customMessage);
            return ApiResponse.success("发送作业通知成功", result);
        } catch (Exception e) {
            logger.error("发送作业通知失败", e);
            return ApiResponse.error("发送作业通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送课程相关通知（教师专用）
     */
    @PostMapping("/course/{courseId}")
    @Operation(summary = "发送课程相关通知", description = "向课程相关的学生发送通知")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> sendCourseNotification(
            @PathVariable Long courseId,
            @RequestParam("type") String type,
            @RequestParam(value = "customMessage", required = false) String customMessage) {
        try {
            Map<String, Object> result = notificationService.sendCourseNotification(courseId, type, customMessage);
            return ApiResponse.success("发送课程通知成功", result);
        } catch (Exception e) {
            logger.error("发送课程通知失败", e);
            return ApiResponse.error("发送课程通知失败: " + e.getMessage());
        }
    }

    // 私有辅助方法
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(authentication.getName());
    }

    // 内部类：请求对象

    public static class NotificationRequest {
        private Long recipientId;
        private String title;
        private String content;
        private String type;
        private String category;
        private String priority;
        private String relatedType;
        private Long relatedId;

        // Getters and Setters
        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getRelatedType() { return relatedType; }
        public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
        public Long getRelatedId() { return relatedId; }
        public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    }

    public static class BatchNotificationRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private String type;
        private String category;
        private String priority;
        private String relatedType;
        private Long relatedId;

        // Getters and Setters
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getRelatedType() { return relatedType; }
        public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
        public Long getRelatedId() { return relatedId; }
        public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    }
} 