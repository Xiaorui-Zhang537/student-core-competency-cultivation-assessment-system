package com.noncore.assessment.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 管理员全局通知（群发）请求体。
 *
 * <p>设计目标：复用 notifications 表，按用户批量写入通知记录，供通知中心统一展示。</p>
 */
@Data
public class AdminBroadcastNotificationRequest {
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 通知类型（可选，默认 system） */
    private String type;
    /** 分类（可选，默认 system） */
    private String category;
    /** 优先级（可选，默认 normal） */
    private String priority;

    /**
     * 目标类型：
     * - all：全体用户
     * - role：按角色（student/teacher/admin）
     * - specific：指定用户ID列表
     */
    private String targetType;

    /** 当 targetType=specific 时使用 */
    private List<Long> targetIds;
    /** 当 targetType=role 时使用 */
    private String role;
}

