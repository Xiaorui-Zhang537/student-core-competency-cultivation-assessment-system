package com.noncore.assessment.service;

import com.noncore.assessment.entity.BehaviorEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为事件服务（事实记录）。
 *
 * <p>强约束：该服务不得引入 AI 调用；只负责事件写入与查询。</p>
 */
public interface BehaviorEventService {
    void save(BehaviorEvent event);

    List<BehaviorEvent> listByStudentCourseRange(Long studentId, Long courseId, LocalDateTime from, LocalDateTime to, Integer limit);

    /**
     * 按学生/课程/时间窗分页查询事件（按时间倒序）。
     */
    List<BehaviorEvent> listByStudentCourseRangePaged(Long studentId,
                                                      Long courseId,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      Integer offset,
                                                      Integer limit);

    /**
     * 判断是否存在晚于指定时间点的事件（用于判断摘要快照是否过期）。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param since 时间点（不包含该时间）
     * @return true 表示存在新事件
     */
    boolean hasEventsAfter(Long studentId, Long courseId, LocalDateTime since);
}

