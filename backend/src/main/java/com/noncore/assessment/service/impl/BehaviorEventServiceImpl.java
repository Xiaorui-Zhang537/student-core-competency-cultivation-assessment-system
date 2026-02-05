package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.BehaviorEvent;
import com.noncore.assessment.mapper.BehaviorEventMapper;
import com.noncore.assessment.service.BehaviorEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为事件服务实现（事实记录，append-only）。
 *
 * <p>强约束：不得引入 AI 调用。</p>
 */
@Service
@RequiredArgsConstructor
public class BehaviorEventServiceImpl implements BehaviorEventService {

    private final BehaviorEventMapper mapper;

    /**
     * 保存行为事件（只新增，不更新、不删除）。
     *
     * @param event 行为事件
     */
    @Override
    public void save(BehaviorEvent event) {
        if (event == null) return;
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(LocalDateTime.now());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(event);
    }

    /**
     * 按学生/课程/时间窗查询事件。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param from 起始时间（可空）
     * @param to 结束时间（可空）
     * @param limit 限制条数（可空）
     * @return 事件列表
     */
    @Override
    public List<BehaviorEvent> listByStudentCourseRange(Long studentId, Long courseId, LocalDateTime from, LocalDateTime to, Integer limit) {
        return mapper.listByStudentCourseRange(studentId, courseId, from, to, limit);
    }

    @Override
    public List<BehaviorEvent> listByStudentCourseRangePaged(Long studentId,
                                                             Long courseId,
                                                             LocalDateTime from,
                                                             LocalDateTime to,
                                                             Integer offset,
                                                             Integer limit) {
        return mapper.listByStudentCourseRangePaged(studentId, courseId, from, to, offset, limit);
    }

    /**
     * 判断是否存在晚于指定时间点的事件（用于判断摘要快照是否过期）。
     */
    @Override
    public boolean hasEventsAfter(Long studentId, Long courseId, LocalDateTime since) {
        if (studentId == null || since == null) return false;
        try {
            return mapper.existsAfter(studentId, courseId, since) != null;
        } catch (Exception ignored) {
            // 容错：这里不抛错，避免影响主流程；若 DB 异常将由后续聚合/查询兜底
            return false;
        }
    }
}

