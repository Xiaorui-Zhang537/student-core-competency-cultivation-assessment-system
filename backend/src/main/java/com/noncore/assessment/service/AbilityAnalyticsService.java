package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;

public interface AbilityAnalyticsService {
    AbilityRadarResponse getRadar(AbilityRadarQuery query, Long teacherId);
    AbilityWeightsResponse getWeights(Long courseId, Long teacherId);
    AbilityWeightsResponse updateWeights(UpdateAbilityWeightsRequest request, Long teacherId);
}

