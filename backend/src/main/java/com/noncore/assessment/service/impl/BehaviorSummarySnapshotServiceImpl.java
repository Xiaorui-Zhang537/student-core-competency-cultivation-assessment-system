package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.BehaviorSummarySnapshot;
import com.noncore.assessment.mapper.BehaviorSummarySnapshotMapper;
import com.noncore.assessment.service.BehaviorSummarySnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 行为摘要快照服务实现（阶段一产物，禁止 AI）。
 */
@Service
@RequiredArgsConstructor
public class BehaviorSummarySnapshotServiceImpl implements BehaviorSummarySnapshotService {

    private final BehaviorSummarySnapshotMapper mapper;

    /**
     * 保存摘要快照（入库）。
     *
     * @param snapshot 摘要快照
     */
    @Override
    public void save(BehaviorSummarySnapshot snapshot) {
        if (snapshot == null) return;
        if (snapshot.getGeneratedAt() == null) {
            snapshot.setGeneratedAt(LocalDateTime.now());
        }
        if (snapshot.getCreatedAt() == null) {
            snapshot.setCreatedAt(LocalDateTime.now());
        }
        mapper.insert(snapshot);
    }

    /**
     * 获取指定时间窗的最新快照。
     */
    @Override
    public BehaviorSummarySnapshot getLatest(Long studentId, Long courseId, String rangeKey, String schemaVersion, LocalDateTime from, LocalDateTime to) {
        return mapper.getLatest(studentId, courseId, rangeKey, schemaVersion, from, to);
    }

    /**
     * 根据ID获取快照。
     */
    @Override
    public BehaviorSummarySnapshot getById(Long id) {
        return mapper.getById(id);
    }
}

