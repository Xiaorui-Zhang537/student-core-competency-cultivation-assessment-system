package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    int insert(Report report);
    Report selectById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    List<Report> page(@Param("status") String status,
                      @Param("offset") int offset,
                      @Param("size") int size);
    long count(@Param("status") String status);
}


