package com.noncore.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行为洞察记录（阶段二 AI 解读结果）。
 *
 * <p>强约束：该结果不得包含任何新分数/百分比/加权结果，只允许解释与总结。</p>
 */
@Data
public class BehaviorInsight {
    private Long id;

    /** schema 版本（如 behavior_insight.v1） */
    private String schemaVersion;

    /** 对应的摘要快照ID */
    private Long snapshotId;

    private Long studentId;
    private Long courseId;

    /** 模型标识（审计用） */
    private String model;

    /** prompt 版本（审计用） */
    private String promptVersion;

    /** 生成状态：success/failed/partial */
    private String status;

    /** AI 输出 JSON（字符串形式存储） */
    private String insightJson;

    /** 失败原因（可选） */
    private String errorMessage;

    /** 生成时间 */
    private LocalDateTime generatedAt;

    /** 创建时间（入库时间） */
    private LocalDateTime createdAt;
}

