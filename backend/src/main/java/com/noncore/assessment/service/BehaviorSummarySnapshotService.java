package com.noncore.assessment.service;

import com.noncore.assessment.entity.BehaviorSummarySnapshot;

import java.time.LocalDateTime;

/**
 * 行为摘要快照服务（阶段一产物）。
 *
 * <p>强约束：该服务不得引入 AI 调用；只负责快照存取与查询。</p>
 */
public interface BehaviorSummarySnapshotService {

    /**
     * 保存摘要快照（入库）。
     *
     * @param snapshot 摘要快照
     */
    void save(BehaviorSummarySnapshot snapshot);

    /**
     * 获取指定时间窗的最新快照。
     *
     * @param studentId 学生ID
     * @param courseId  课程ID（可空）
     * @param rangeKey  时间窗标识（如 7d）
     * @param schemaVersion schema版本（如 behavior_summary.v1）
     * @param from 时间窗起始（包含）
     * @param to 时间窗结束（不包含）
     * @return 最新快照（可能为 null）
     */
    BehaviorSummarySnapshot getLatest(Long studentId, Long courseId, String rangeKey, String schemaVersion, LocalDateTime from, LocalDateTime to);

    /**
     * 根据ID获取快照。
     *
     * @param id 快照ID
     * @return 快照（可能为 null）
     */
    BehaviorSummarySnapshot getById(Long id);
}

