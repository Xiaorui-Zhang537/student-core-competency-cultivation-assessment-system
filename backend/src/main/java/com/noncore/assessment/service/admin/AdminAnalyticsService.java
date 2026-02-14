package com.noncore.assessment.service.admin;

import java.util.Map;

/**
 * 管理员-分析看板服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminAnalyticsService {

    Map<String, Object> getOverviewSeries(int days);
}

