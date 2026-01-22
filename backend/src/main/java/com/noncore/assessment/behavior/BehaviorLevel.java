package com.noncore.assessment.behavior;

import java.util.Arrays;
import java.util.Optional;

/**
 * 阶段性能力判断档位（非分数、非百分比）。
 *
 * <p>用于阶段二 AI 输出：只允许输出固定档位，禁止输出任何数值成绩或权重计算结果。</p>
 */
public enum BehaviorLevel {
    EMERGING,
    DEVELOPING,
    PROFICIENT,
    ADVANCED;

    /**
     * 解析档位（支持大小写不敏感）。
     *
     * @param level 档位字符串
     * @return 若匹配则返回枚举，否则 empty
     */
    public static Optional<BehaviorLevel> fromString(String level) {
        if (level == null || level.isBlank()) {
            return Optional.empty();
        }
        String normalized = level.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(v -> v.name().equals(normalized))
                .findFirst();
    }
}

