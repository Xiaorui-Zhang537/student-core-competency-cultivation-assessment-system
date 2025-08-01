package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库notifications表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "通知实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "接收者ID", example = "1")
    private Long recipientId;

    @Schema(description = "发送者ID", example = "2")
    private Long senderId;

    @Schema(description = "通知类型", example = "assignment", allowableValues = {"assignment", "grade", "course", "system", "message"})
    private String type;

    @Schema(description = "通知分类", example = "academic")
    private String category;

    @Schema(description = "通知标题", example = "新作业发布")
    private String title;

    @Schema(description = "通知内容", example = "您有一份新的作业需要完成")
    private String content;

    @Schema(description = "关联对象类型", example = "assignment")
    private String relatedType;

    @Schema(description = "关联对象ID", example = "1")
    private Long relatedId;

    @Schema(description = "关联数据", example = "{\"assignmentId\": 1, \"courseId\": 2}")
    private String data;

    @Schema(description = "是否已读", example = "false")
    @Builder.Default
    private Boolean isRead = false;

    @Schema(description = "阅读时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    @Schema(description = "通知优先级", example = "normal", allowableValues = {"low", "normal", "high", "urgent"})
    @Builder.Default
    private String priority = "normal";

    @Schema(description = "通知图标", example = "assignment")
    private String icon;

    @Schema(description = "操作URL", example = "/student/assignments/1")
    private String actionUrl;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Schema(description = "是否删除", example = "false")
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        updateTimestamp();
    }

    /**
     * 标记为未读
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
        updateTimestamp();
    }

    /**
     * 是否为系统通知
     */
    public boolean isSystemNotification() {
        return "system".equals(type);
    }

    /**
     * 是否为紧急通知
     */
    public boolean isUrgent() {
        return "urgent".equals(priority);
    }

    /**
     * 是否为高优先级通知
     */
    public boolean isHighPriority() {
        return "high".equals(priority) || "urgent".equals(priority);
    }

    /**
     * 获取通知年龄（分钟）
     */
    public long getAgeInMinutes() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * 是否为新通知（30分钟内）
     */
    public boolean isNew() {
        return getAgeInMinutes() <= 30;
    }

    /**
     * 设置关联数据
     */
    public void setRelatedData(String key, Object value) {
        // 简单的JSON构建，实际项目中建议使用Jackson
        // 这里简化处理，实际应该用JSON库
        this.data = "{\"" + key + "\": \"" + value + "\"}";
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    private LocalDateTime expiredAt;
} 