package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.BehaviorSummaryResponse;

/**
 * 行为摘要聚合服务（阶段一，纯代码，禁止 AI）。
 */
public interface BehaviorAggregationService {

    /**
     * 获取行为摘要：若已有快照则直接返回；否则按当前时间窗聚合生成并保存快照后返回。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param range 时间窗标识（当前仅支持 7d）
     * @return 行为摘要（阶段一产物）
     */
    BehaviorSummaryResponse getOrBuildSummary(Long studentId, Long courseId, String range);

    /**
     * 强制重新聚合并保存一个新的摘要快照（阶段一，禁止 AI）。
     *
     * <p>用于定时任务或需要“阶段性快照”的场景；不复用历史快照。</p>
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param range 时间窗标识（当前仅支持 7d）
     * @return 新写入的快照（包含 id）
     */
    com.noncore.assessment.entity.BehaviorSummarySnapshot buildAndSaveSnapshot(Long studentId, Long courseId, String range);
}

