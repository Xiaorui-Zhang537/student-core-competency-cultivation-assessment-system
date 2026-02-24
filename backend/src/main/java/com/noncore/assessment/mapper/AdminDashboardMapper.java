package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘数据聚合 Mapper。
 *
 * <p>说明：这里只做只读聚合查询，不涉及业务权限（权限在 Controller 层以 ADMIN 角色保证）。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminDashboardMapper {

    Long countUsersAll(@Param("includeDeleted") boolean includeDeleted);

    Long countUsersByRole(@Param("role") String role, @Param("includeDeleted") boolean includeDeleted);

    Long countCoursesAll();

    Long countCoursesByStatus(@Param("status") String status);

    Long countPostsAll();

    Long countPostCommentsAll();

    Long countReportsAll();

    Long countReportsByStatus(@Param("status") String status);

    Long countActiveUsersSince(@Param("since") LocalDateTime since);

    Long countGradesByLevel(@Param("level") String level);

    List<Map<String, Object>> abilityRadarOverview(@Param("days") int days);

    Long countAiConversationsSince(@Param("since") LocalDateTime since);

    Long countAiMessagesSince(@Param("since") LocalDateTime since);

    Long countAiActiveUsersSince(@Param("since") LocalDateTime since);

    List<Map<String, Object>> aiUsageByUser(@Param("since") LocalDateTime since, @Param("limit") int limit);
}

