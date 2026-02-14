package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AdminAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员审计日志 Mapper。
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminAuditLogMapper {

    int insert(AdminAuditLog log);
}

