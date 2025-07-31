package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeStatsResponse {
    private BigDecimal averageScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private int submissionCount;
    private List<Map<String, Object>> gradeDistribution;
} 