package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;

public interface AbilityAnalyticsService {
    AbilityRadarResponse getRadar(AbilityRadarQuery query, Long teacherId);
    AbilityWeightsResponse getWeights(Long courseId, Long teacherId);
    AbilityWeightsResponse updateWeights(UpdateAbilityWeightsRequest request, Long teacherId);
    AbilityCompareResponse getRadarCompare(AbilityCompareQuery query, Long teacherId);
    AbilityDimensionInsightsResponse getDimensionInsights(AbilityCompareQuery query, Long teacherId);

    // Student-facing variants (no teacher ownership check; restricted to current student)
    AbilityRadarResponse getRadarForStudent(AbilityRadarQuery query, Long studentId);
    AbilityCompareResponse getRadarCompareForStudent(AbilityCompareQuery query, Long studentId);
    AbilityDimensionInsightsResponse getDimensionInsightsForStudent(AbilityCompareQuery query, Long studentId);
}

