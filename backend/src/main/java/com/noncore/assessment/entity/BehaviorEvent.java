package com.noncore.assessment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行为事件（事实记录）。
 *
 * <p>强约束：行为事件只记录“发生了什么”，不包含任何评价与分数计算。</p>
 */
@Data
public class BehaviorEvent {
    private Long id;

    /** 学生ID（行为主体） */
    private Long studentId;

    /** 课程ID（可选，若事件发生在课程上下文内） */
    private Long courseId;

    /** 事件类型 code（见 BehaviorEventType.getCode()） */
    private String eventType;

    /** 关联对象类型（assignment/submission/post/comment/ai_conversation/...） */
    private String relatedType;

    /** 关联对象ID */
    private Long relatedId;

    /** 元数据 JSON（字符串形式存储，DB 字段为 JSON） */
    private String metadata;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 记录创建时间 */
    private LocalDateTime createdAt;
}

