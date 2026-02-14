package com.noncore.assessment.service.admin.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.entity.AdminAuditLog;
import com.noncore.assessment.mapper.AdminAuditLogMapper;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 管理员审计日志服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditLogServiceImpl.class);

    private final AdminAuditLogMapper adminAuditLogMapper;
    private final ObjectMapper objectMapper;

    public AdminAuditLogServiceImpl(AdminAuditLogMapper adminAuditLogMapper, ObjectMapper objectMapper) {
        this.adminAuditLogMapper = adminAuditLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(Long actorId,
                       String action,
                       String targetType,
                       Long targetId,
                       Map<String, Object> detail,
                       HttpServletRequest request) {
        try {
            AdminAuditLog log = new AdminAuditLog();
            log.setActorId(actorId);
            log.setAction(action);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setIp(extractIp(request));
            log.setUserAgent(extractUserAgent(request));
            if (detail != null) {
                log.setDetailJson(objectMapper.writeValueAsString(detail));
            } else {
                log.setDetailJson(null);
            }
            adminAuditLogMapper.insert(log);
        } catch (Exception e) {
            // 审计日志不应阻断主流程
            logger.warn("写入管理员审计日志失败(action={}, targetType={}, targetId={}): {}", action, targetType, targetId, e.getMessage());
        }
    }

    private String extractUserAgent(HttpServletRequest request) {
        try {
            return request == null ? null : request.getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return null;
        try {
            // 常见代理头
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                // 取第一个
                String[] parts = xff.split(",");
                return parts.length > 0 ? parts[0].trim() : xff.trim();
            }
            String xrip = request.getHeader("X-Real-IP");
            if (xrip != null && !xrip.isBlank()) return xrip.trim();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}

