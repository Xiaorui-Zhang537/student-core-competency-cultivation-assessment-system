package com.noncore.assessment.behavior;

import com.noncore.assessment.entity.Enrollment;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.BehaviorAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 行为摘要快照定时生成（阶段一，禁止 AI）。
 *
 * <p>说明：该定时任务只生成阶段一快照，不触发阶段二 AI；AI 解读默认由教师手动触发，避免不可控成本。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BehaviorSnapshotScheduler {

    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final BehaviorAggregationService aggregationService;

    /**
     * 每周一凌晨 03:30 生成一次“7d”摘要快照。
     */
    @Scheduled(cron = "0 30 3 ? * MON")
    public void generateWeeklySnapshots() {
        try {
            List<com.noncore.assessment.entity.User> students = userMapper.selectUsersByRole("student");
            if (students == null || students.isEmpty()) return;

            for (var u : students) {
                if (u == null || u.getId() == null) continue;
                try {
                    List<Enrollment> enrollments = enrollmentMapper.selectEnrollmentsByStudentId(u.getId());
                    if (enrollments == null || enrollments.isEmpty()) continue;
                    for (Enrollment e : enrollments) {
                        if (e == null) continue;
                        Long courseId = e.getCourseId();
                        // 强制生成新快照（阶段性）
                        try {
                            aggregationService.buildAndSaveSnapshot(u.getId(), courseId, "7d");
                        } catch (Exception ex) {
                            log.warn("Behavior snapshot failed: studentId={}, courseId={}, err={}", u.getId(), courseId, ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Behavior snapshot student loop failed: studentId={}, err={}", u.getId(), ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.error("Behavior snapshot scheduler failed", ex);
        }
    }
}

