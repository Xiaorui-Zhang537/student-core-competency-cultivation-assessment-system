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
public class TeacherStudentAlertsResponse {
    private List<AlertItem> alerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertItem {
        private String code;      // e.g. INACTIVE_14D, LOW_SCORE, LAGGING_PROGRESS
        private String message;   // 展示文案（i18n由前端处理亦可）
        private String severity;  // info/warn/critical
    }
}


