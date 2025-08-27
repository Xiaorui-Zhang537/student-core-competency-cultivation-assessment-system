package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityCompareResponse {
    private List<String> dimensions;
    private Series seriesA;
    private Series seriesB;
    private List<Double> deltasAB; // A - B
    private Double compositeDeltaAB; // A - B
    private Map<String, Double> weights;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Series {
        private List<Double> studentScores;
        private List<Double> classAvgScores; // 可为空，取决于 includeClassAvg
        private Double studentComposite;
        private Double classComposite; // 可为空
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private String includeClassAvg; // none | A | B | both
        private Period periodA;
        private Period periodB;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private String startDate;
        private String endDate;
        private List<Long> assignmentIds;
    }
}


