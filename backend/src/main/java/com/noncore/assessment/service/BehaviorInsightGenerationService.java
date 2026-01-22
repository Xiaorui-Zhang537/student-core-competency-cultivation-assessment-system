package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.BehaviorInsightResponse;

/**
 * 行为洞察生成用例服务（阶段二：调用 AI 做解释与总结）。
 */
public interface BehaviorInsightGenerationService {

    /**
     * 生成并保存阶段二洞察（JSON-only）。
     *
     * @param operatorId 触发者ID（教师/管理员；用于审计与 AI 调用上下文）
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param range 时间窗（当前仅支持 7d）
     * @param model 模型标识（可空，默认 google/gemini-2.5-pro）
     * @return 洞察结构化结果
     */
    /**
     * 生成并保存阶段二洞察（JSON-only）。
     *
     * @param operatorId 触发者ID（学生/教师/管理员）
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param range 时间窗（当前仅支持 7d）
     * @param model 模型标识（可空，默认 google/gemini-2.5-pro）
     * @param force 是否强制重跑（仅教师/管理员建议使用；学生必须为 false）
     * @param studentSelfTrigger 是否为学生自助触发（将启用 7 天冷却：7 天内仅允许触发一次，直接复用最近结果）
     * @return 洞察结构化结果
     */
    BehaviorInsightResponse generate(Long operatorId,
                                    Long studentId,
                                    Long courseId,
                                    String range,
                                    String model,
                                    boolean force,
                                    boolean studentSelfTrigger);

    /**
     * 获取最新洞察（若不存在返回 null）。
     *
     * @param operatorId 当前用户ID（用于权限校验：学生仅可看自己）
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param range 时间窗（当前仅支持 7d）
     * @return 洞察结果（可能为 null）
     */
    BehaviorInsightResponse getLatest(Long operatorId, Long studentId, Long courseId, String range);
}

