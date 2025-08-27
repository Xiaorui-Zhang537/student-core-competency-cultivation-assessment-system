package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbilityDimensionInsightsResponse {
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String code; // 维度代码
        private String name; // 维度名称
        private Double scoreA;
        private Double scoreB;
        private Double delta; // A - B
        private Double weight; // 当前权重
        private String analysis; // 简要解析（占位）
        private String suggestion; // 建议（占位）
    }
}


