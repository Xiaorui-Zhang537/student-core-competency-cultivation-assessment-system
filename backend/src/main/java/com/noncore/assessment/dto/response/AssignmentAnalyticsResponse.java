package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Assignment;
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
public class AssignmentAnalyticsResponse {

    private Assignment assignment;
    private SubmissionStats submissionStats;
    private List<Map<String, Object>> gradeDistribution;
    private BigDecimal averageScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmissionStats {
        private int totalSubmissions;
        private int pendingGrading;
        private int graded;
        private BigDecimal submissionRate;
    }
} 