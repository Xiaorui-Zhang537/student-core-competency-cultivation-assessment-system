package com.noncore.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员审计日志实体。
 *
 * <p>用于记录管理员高风险动作：创建管理员、修改用户角色/状态、内容治理、导出等。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Data
public class AdminAuditLog {
    private Long id;
    private Long actorId;
    private String action;
    private String targetType;
    private Long targetId;
    private String detailJson;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}

