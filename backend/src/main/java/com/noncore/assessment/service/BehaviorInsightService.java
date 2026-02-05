package com.noncore.assessment.service;

import com.noncore.assessment.entity.BehaviorInsight;

/**
 * 行为洞察服务（阶段二 AI 解读结果）。
 *
 * <p>强约束：本接口仅定义“结果存取”，不在此层强制调用 AI；
 * AI 调用应由上层用例服务负责，并保证只读阶段一摘要、禁止计算分数。</p>
 */
public interface BehaviorInsightService {

    /**
     * 保存洞察结果（入库）。
     *
     * @param insight 洞察记录
     */
    void save(BehaviorInsight insight);

    /**
     * 获取指定快照的最新洞察结果。
     *
     * @param snapshotId 快照ID
     * @param schemaVersion schema版本（如 behavior_insight.v1）
     * @return 最新洞察（可能为 null）
     */
    BehaviorInsight getLatestBySnapshot(Long snapshotId, String schemaVersion);

    /**
     * 获取学生在课程（可空）下的最新洞察（用于冷却治理）。
     */
    BehaviorInsight getLatestByStudentCourse(Long studentId, Long courseId, String schemaVersion);

    /**
     * 获取学生在课程（可空）+ 时间窗(rangeKey) 下的最新洞察（通过 snapshot_id 关联快照过滤 rangeKey）。
     */
    BehaviorInsight getLatestByStudentCourseRange(Long studentId, Long courseId, String rangeKey, String schemaVersion);

    /**
     * 统计学生在指定时间点之后生成的洞察次数（用于限流）。
     *
     * @param studentId 学生ID
     * @param schemaVersion schema版本（如 behavior_insight.v1）
     * @param since 起始时间（包含）
     * @return 次数
     */
    long countByStudentSince(Long studentId, String schemaVersion, java.time.LocalDateTime since);
}

