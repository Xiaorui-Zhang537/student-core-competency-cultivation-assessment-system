package com.noncore.assessment.service.admin;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 管理员审计日志服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminAuditLogService {

    void record(Long actorId,
                String action,
                String targetType,
                Long targetId,
                Map<String, Object> detail,
                HttpServletRequest request);
}

