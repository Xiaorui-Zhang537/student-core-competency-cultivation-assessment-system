package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiGradingHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiGradingHistoryMapper {
    int insert(AiGradingHistory rec);
    List<AiGradingHistory> listByTeacher(@Param("teacherId") Long teacherId,
                                         @Param("offset") int offset,
                                         @Param("size") int size,
                                         @Param("q") String q);
    long countByTeacher(@Param("teacherId") Long teacherId,
                        @Param("q") String q);
    AiGradingHistory getById(@Param("id") Long id,
                             @Param("teacherId") Long teacherId);
}


