package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiQuotaAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiQuotaAdjustmentMapper {
    AiQuotaAdjustment selectByUserId(@Param("userId") Long userId);
    int upsertByUserId(AiQuotaAdjustment quota);
}

