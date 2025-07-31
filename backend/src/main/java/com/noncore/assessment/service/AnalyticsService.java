package com.noncore.assessment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;

public interface AnalyticsService {

    Map<String, Object> getAnalytics(String key);

    void generateAllAnalytics() throws JsonProcessingException;
    
} 