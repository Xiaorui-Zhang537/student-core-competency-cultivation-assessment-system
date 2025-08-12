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
public class AbilityRadarResponse {
    private List<String> dimensions; // 固定顺序的5维
    private List<Double> studentScores; // 0-100
    private List<Double> classAvgScores; // 0-100
    private List<Double> deltas; // student - classAvg
    private Map<String, Double> weights;

    private Double studentComposite;
    private Double classComposite;
    private Double compositeDelta;

    private PreviousPeriod prevPeriod;
    private List<String> weakDimensions; // 分数<阈值或差距<-10

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreviousPeriod {
        private List<Double> studentScores;
        private List<Double> classAvgScores;
        private Double studentComposite;
        private Double classComposite;
    }
}

