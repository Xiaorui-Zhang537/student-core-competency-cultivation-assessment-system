package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.mapper.AdminDashboardMapper;
import com.noncore.assessment.service.admin.AdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员仪表盘服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public AdminDashboardServiceImpl(AdminDashboardMapper adminDashboardMapper) {
        this.adminDashboardMapper = adminDashboardMapper;
    }

    @Override
    public Map<String, Object> getOverview(int daysActiveWindow) {
        int days = daysActiveWindow <= 0 ? 7 : daysActiveWindow;

        Map<String, Object> data = new HashMap<>();
        data.put("daysActiveWindow", days);

        // 用户
        long usersAll = safe(adminDashboardMapper.countUsersAll(false));
        long students = safe(adminDashboardMapper.countUsersByRole("student", false));
        long teachers = safe(adminDashboardMapper.countUsersByRole("teacher", false));
        long admins = safe(adminDashboardMapper.countUsersByRole("admin", false));

        data.put("users", Map.of(
                "total", usersAll,
                "students", students,
                "teachers", teachers,
                "admins", admins
        ));

        // 课程
        long coursesAll = safe(adminDashboardMapper.countCoursesAll());
        long draft = safe(adminDashboardMapper.countCoursesByStatus("draft"));
        long published = safe(adminDashboardMapper.countCoursesByStatus("published"));
        long archived = safe(adminDashboardMapper.countCoursesByStatus("archived"));
        data.put("courses", Map.of(
                "total", coursesAll,
                "draft", draft,
                "published", published,
                "archived", archived
        ));

        // 社区
        long posts = safe(adminDashboardMapper.countPostsAll());
        long comments = safe(adminDashboardMapper.countPostCommentsAll());
        data.put("community", Map.of(
                "posts", posts,
                "comments", comments
        ));

        // 举报
        long reportsAll = safe(adminDashboardMapper.countReportsAll());
        long reportsPending = safe(adminDashboardMapper.countReportsByStatus("pending"));
        data.put("reports", Map.of(
                "total", reportsAll,
                "pending", reportsPending
        ));

        // 活跃用户（按行为事件）
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        long activeUsers = safe(adminDashboardMapper.countActiveUsersSince(since));
        data.put("activity", Map.of(
                "activeUsers", activeUsers,
                "since", since.toString()
        ));

        return data;
    }

    private long safe(Long v) {
        return v == null ? 0L : v;
    }
}

