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

    /**
     * 获取管理员全局五维能力雷达总览。
     *
     * @param days 统计窗口（天）
     * @return 维度平均分与样本量
     */
    Map<String, Object> getAbilityRadarOverview(int days);

    /**
     * 获取管理员 AI 使用总览（按用户访问/消息统计）。
     *
     * @param days 统计窗口（天）
     * @param limit 排行榜返回上限
     * @return 汇总与用户排行
     */
    Map<String, Object> getAiUsageOverview(int days, int limit);
}

