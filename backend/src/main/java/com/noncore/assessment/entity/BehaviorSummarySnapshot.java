package com.noncore.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行为摘要快照（阶段一产物，纯代码聚合）。
 *
 * <p>该快照用于：前端展示阶段一摘要；以及作为阶段二 AI 解读的唯一输入。</p>
 */
@Data
public class BehaviorSummarySnapshot {
    private Long id;

    /** schema 版本（如 behavior_summary.v1） */
    private String schemaVersion;

    private Long studentId;
    private Long courseId;

    /** 时间窗标识（如 7d / custom） */
    private String rangeKey;

    /** 时间窗起始（包含） */
    private LocalDateTime periodFrom;

    /** 时间窗结束（不包含） */
    private LocalDateTime periodTo;

    /** 输入事件数 */
    private Integer inputEventCount;

    /** 事件类型包含列表 JSON（字符串形式存储，DB 字段为 JSON） */
    private String eventTypesIncluded;

    /** 摘要 JSON（字符串形式存储） */
    private String summaryJson;

    /** 生成时间 */
    private LocalDateTime generatedAt;

    /** 创建时间（入库时间） */
    private LocalDateTime createdAt;
}

