package com.noncore.assessment.behavior;

import java.util.Arrays;
import java.util.Optional;

/**
 * 阶段性能力判断维度编码（非分数）。
 *
 * <p>该维度用于“行为证据评价”的阶段二输出（AI 解读），表达阶段性判断的维度归属。
 * 注意：不包含 ACADEMIC_GRADE（学习成绩），因为学习成绩仍然由作业成绩体系独立产生。</p>
 */
public enum BehaviorAbilityDimensionCode {
    MORAL_COGNITION,
    LEARNING_ATTITUDE,
    LEARNING_ABILITY,
    LEARNING_METHOD;

    /**
     * 尝试从字符串解析维度编码。
     *
     * @param code 维度编码字符串
     * @return 若匹配则返回枚举，否则 empty
     */
    public static Optional<BehaviorAbilityDimensionCode> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(v -> v.name().equals(normalized))
                .findFirst();
    }
}

