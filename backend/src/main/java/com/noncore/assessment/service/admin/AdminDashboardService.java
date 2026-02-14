package com.noncore.assessment.service.admin;

import java.util.Map;

/**
 * 管理员仪表盘服务。
 *
 * <p>返回数据面向前端展示，尽量保持字段稳定，便于后续扩展。</p>
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminDashboardService {

    /**
     * 获取管理员仪表盘总览数据。
     *
     * @param daysActiveWindow 统计活跃用户的窗口天数（例如 7）
     */
    Map<String, Object> getOverview(int daysActiveWindow);
}

