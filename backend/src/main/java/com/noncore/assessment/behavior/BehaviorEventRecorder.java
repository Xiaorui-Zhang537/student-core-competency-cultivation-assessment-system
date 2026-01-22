package com.noncore.assessment.behavior;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.BehaviorEvent;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.service.BehaviorEventService;
import com.noncore.assessment.util.Jsons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 行为事件记录器（事实记录，append-only）。
 *
 * <p>强约束：该组件不得引入任何 AI 调用；只负责将“发生了什么”落库。</p>
 */
@Service
@RequiredArgsConstructor
public class BehaviorEventRecorder {

    private final BehaviorEventService behaviorEventService;
    private final AssignmentMapper assignmentMapper;

    /**
     * 记录一个行为事件。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空；部分事件可通过关联对象推断）
     * @param type 事件类型
     * @param relatedType 关联对象类型（如 assignment/submission/post/...）
     * @param relatedId 关联对象ID
     * @param metadata 元数据（可空）
     */
    public void record(Long studentId,
                       Long courseId,
                       BehaviorEventType type,
                       String relatedType,
                       Long relatedId,
                       Map<String, Object> metadata) {
        if (studentId == null || type == null) return;

        Long resolvedCourseId = courseId;
        Map<String, Object> meta = metadata == null ? new HashMap<>() : new HashMap<>(metadata);

        // 尝试从作业关联推断 courseId（让课程筛选可用）
        if (resolvedCourseId == null && relatedId != null) {
            if ("assignment".equalsIgnoreCase(relatedType)) {
                Assignment a = assignmentMapper.selectAssignmentById(relatedId);
                if (a != null && a.getCourseId() != null) {
                    resolvedCourseId = a.getCourseId();
                }
            }
            // submission 场景：若 metadata 带 assignmentId，则推断
            if ("submission".equalsIgnoreCase(relatedType)) {
                Object aid = meta.get("assignmentId");
                if (aid != null) {
                    try {
                        Long assignmentId = Long.valueOf(String.valueOf(aid));
                        Assignment a = assignmentMapper.selectAssignmentById(assignmentId);
                        if (a != null && a.getCourseId() != null) {
                            resolvedCourseId = a.getCourseId();
                        }
                    } catch (Exception ignored) { }
                }
            }
        }

        // 兜底：写入 courseId 到 metadata 便于审计
        if (resolvedCourseId != null && meta.get("courseId") == null) {
            meta.put("courseId", resolvedCourseId);
        }

        BehaviorEvent ev = new BehaviorEvent();
        ev.setStudentId(studentId);
        ev.setCourseId(resolvedCourseId);
        ev.setEventType(type.getCode());
        ev.setRelatedType(relatedType);
        ev.setRelatedId(relatedId);
        ev.setMetadata(meta.isEmpty() ? null : Jsons.toJson(meta));
        ev.setOccurredAt(LocalDateTime.now());
        ev.setCreatedAt(LocalDateTime.now());
        behaviorEventService.save(ev);
    }
}

