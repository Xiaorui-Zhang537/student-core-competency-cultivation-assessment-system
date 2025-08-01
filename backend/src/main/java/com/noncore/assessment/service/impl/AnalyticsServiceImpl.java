package com.noncore.assessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.entity.AnalyticsCache;
import com.noncore.assessment.mapper.AnalyticsCacheMapper;
import com.noncore.assessment.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final AnalyticsCacheMapper analyticsCacheMapper;
    private final ObjectMapper objectMapper;

    public AnalyticsServiceImpl(AnalyticsCacheMapper analyticsCacheMapper, ObjectMapper objectMapper) {
        this.analyticsCacheMapper = analyticsCacheMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> getAnalytics(String key) {
        AnalyticsCache cache = analyticsCacheMapper.findByKey(key);
        if (cache != null) {
            try {
                return objectMapper.readValue(cache.getCacheValue(), new TypeReference<>() {
                });
            } catch (IOException e) {
                logger.error("Error parsing analytics cache for key: {}", key, e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "解析缓存数据失败", e);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void generateAllAnalytics() throws JsonProcessingException {
        logger.info("Starting generation of all analytics data...");

        generateTeacherDashboardTeachingData();
        
        logger.info("Finished generation of all analytics data.");
    }

    private void generateTeacherDashboardTeachingData() throws JsonProcessingException {
        // This is a placeholder for a more complex aggregation.
        // For now, we will generate some sample time-series data.
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> timeSeries = List.of(
            Map.of("date", "2024-01", "students", 150, "courses", 10),
            Map.of("date", "2024-02", "students", 160, "courses", 12),
            Map.of("date", "2024-03", "students", 175, "courses", 15),
            Map.of("date", "2024-04", "students", 180, "courses", 16)
        );
        data.put("timeSeriesData", timeSeries);

        AnalyticsCache cache = new AnalyticsCache();
        cache.setCacheKey("teacher_dashboard_teaching_data");
        cache.setCacheValue(objectMapper.writeValueAsString(data));
        cache.setGeneratedAt(LocalDateTime.now());
        analyticsCacheMapper.upsert(cache);
        logger.info("Generated teacher dashboard teaching data.");
    }

} 