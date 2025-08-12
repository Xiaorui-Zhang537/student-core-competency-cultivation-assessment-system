package com.noncore.assessment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 更新课程五维能力权重请求
 */
@Data
public class UpdateAbilityWeightsRequest {
    @NotNull
    private Long courseId;

    /**
     * 维度编码到权重的映射：
     * MORAL_COGNITION / LEARNING_ATTITUDE / LEARNING_ABILITY / LEARNING_METHOD / ACADEMIC_GRADE
     */
    @NotNull
    private Map<String, Double> weights;
}

