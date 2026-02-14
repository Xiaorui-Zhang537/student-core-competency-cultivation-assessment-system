package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 管理员-分析看板聚合 Mapper。
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminAnalyticsMapper {

    List<Map<String, Object>> activeUsersDaily(@Param("days") int days);

    List<Map<String, Object>> newUsersDaily(@Param("days") int days);

    List<Map<String, Object>> enrollmentsDaily(@Param("days") int days);
}

