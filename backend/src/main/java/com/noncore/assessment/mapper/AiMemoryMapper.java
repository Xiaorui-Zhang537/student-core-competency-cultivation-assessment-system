package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiMemoryMapper {
    AiMemory selectByUserId(@Param("userId") Long userId);
    int upsertByUserId(AiMemory memory);
}


