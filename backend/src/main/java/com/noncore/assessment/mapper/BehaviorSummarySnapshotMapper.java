package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.BehaviorSummarySnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface BehaviorSummarySnapshotMapper {
    int insert(BehaviorSummarySnapshot snapshot);

    BehaviorSummarySnapshot getLatest(@Param("studentId") Long studentId,
                                      @Param("courseId") Long courseId,
                                      @Param("rangeKey") String rangeKey,
                                      @Param("schemaVersion") String schemaVersion,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);

    BehaviorSummarySnapshot getById(@Param("id") Long id);
}

