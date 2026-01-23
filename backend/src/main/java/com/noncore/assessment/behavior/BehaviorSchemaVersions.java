package com.noncore.assessment.behavior;

/**
 * 行为证据评价：数据契约版本号管理。
 *
 * <p>用于阶段一（纯代码聚合）与阶段二（AI 解读）的输入/输出 JSON 兼容性控制。
 * 任何字段变更都应升级版本号，并在 /docs 中同步说明。</p>
 */
public final class BehaviorSchemaVersions {

    /** 阶段一：行为摘要快照 schema 版本 */
    public static final String SUMMARY_V1 = "behavior_summary.v1";

    /** 阶段二：行为洞察（AI 解读）schema 版本 */
    public static final String INSIGHT_V1 = "behavior_insight.v1";
    /** 阶段二：行为洞察（AI 解读）schema 版本（新增结构化预警/建议输出） */
    public static final String INSIGHT_V2 = "behavior_insight.v2";

    private BehaviorSchemaVersions() {
        // 工具类不允许实例化
    }
}