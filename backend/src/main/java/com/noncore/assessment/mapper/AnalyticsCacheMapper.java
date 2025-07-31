package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AnalyticsCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalyticsCacheMapper {

    AnalyticsCache findByKey(@Param("cacheKey") String cacheKey);

    void upsert(AnalyticsCache cache);

} 