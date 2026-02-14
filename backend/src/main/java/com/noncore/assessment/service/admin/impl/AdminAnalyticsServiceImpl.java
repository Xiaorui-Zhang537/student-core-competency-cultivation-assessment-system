package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.mapper.AdminAnalyticsMapper;
import com.noncore.assessment.service.admin.AdminAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-分析看板服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional(readOnly = true)
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final AdminAnalyticsMapper adminAnalyticsMapper;

    public AdminAnalyticsServiceImpl(AdminAnalyticsMapper adminAnalyticsMapper) {
        this.adminAnalyticsMapper = adminAnalyticsMapper;
    }

    @Override
    public Map<String, Object> getOverviewSeries(int days) {
        int d = days <= 0 ? 30 : Math.min(days, 365);
        Map<String, Object> data = new HashMap<>();
        data.put("days", d);
        data.put("activeUsersDaily", adminAnalyticsMapper.activeUsersDaily(d));
        data.put("newUsersDaily", adminAnalyticsMapper.newUsersDaily(d));
        data.put("enrollmentsDaily", adminAnalyticsMapper.enrollmentsDaily(d));
        return data;
    }
}

