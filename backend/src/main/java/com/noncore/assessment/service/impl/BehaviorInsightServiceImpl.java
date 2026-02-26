package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.BehaviorInsight;
import com.noncore.assessment.mapper.BehaviorInsightMapper;
import com.noncore.assessment.service.BehaviorInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 行为洞察服务实现（阶段二结果存取）。
 */
@Service
@RequiredArgsConstructor
public class BehaviorInsightServiceImpl implements BehaviorInsightService {

    private final BehaviorInsightMapper mapper;

    /**
     * 保存洞察结果（入库）。
     *
     * @param insight 洞察记录
     */
    @Override
    public void save(BehaviorInsight insight) {
        if (insight == null) return;
        if (insight.getGeneratedAt() == null) {
            insight.setGeneratedAt(LocalDateTime.now());
        }
        if (insight.getCreatedAt() == null) {
            insight.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(insight);
    }

    /**
     * 获取指定快照的最新洞察结果。
     */
    @Override
    public BehaviorInsight getLatestBySnapshot(Long snapshotId, String schemaVersion) {
        return mapper.getLatestBySnapshot(snapshotId, schemaVersion);
    }

    @Override
    public BehaviorInsight getLatestByStudentCourse(Long studentId, Long courseId, String schemaVersion) {
        return mapper.getLatestByStudentCourse(studentId, courseId, schemaVersion);
    }

    @Override
    public BehaviorInsight getLatestByStudentCourseRange(Long studentId, Long courseId, String rangeKey, String schemaVersion) {
        return mapper.getLatestByStudentCourseRange(studentId, courseId, rangeKey, schemaVersion);
    }

    @Override
    public long countByStudentSince(Long studentId, String schemaVersion, LocalDateTime since) {
        if (studentId == null) return 0L;
        return mapper.countByStudentSince(studentId, schemaVersion, since);
    }

    @Override
    public java.util.List<BehaviorInsight> pageByStudentCourseRange(Long studentId,
                                                                     Long courseId,
                                                                     String rangeKey,
                                                                     String schemaVersion,
                                                                     int page,
                                                                     int size) {
        if (studentId == null) return java.util.List.of();
        final int safePage = Math.max(1, page);
        final int safeSize = Math.max(1, Math.min(size, 100));
        final int offset = (safePage - 1) * safeSize;
        return mapper.pageByStudentCourseRange(studentId, courseId, rangeKey, schemaVersion, offset, safeSize);
    }

    @Override
    public long countByStudentCourseRange(Long studentId, Long courseId, String rangeKey, String schemaVersion) {
        if (studentId == null) return 0L;
        return mapper.countByStudentCourseRange(studentId, courseId, rangeKey, schemaVersion);
    }

    @Override
    public BehaviorInsight getById(Long id) {
        if (id == null) return null;
        return mapper.getById(id);
    }
}

