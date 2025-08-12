package com.noncore.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AbilityWeightsResponse {
    private Long courseId;
    private Map<String, Double> weights;
    private LocalDateTime updatedAt;
}

