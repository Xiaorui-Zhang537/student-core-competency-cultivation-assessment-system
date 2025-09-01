package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnalysisResponse {

    private Kpi kpi;
    private Radar radar;
    private Trends trends;
    private List<RecentGradeItem> recentGrades;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kpi {
        private Double avgScore;         // 平均成绩（0-100）
        private Double completionRate;   // 作业完成率（0-100）
        private Long studyHours;         // 学习时长（小时）
        private Long activeDays;         // 活跃天数（最近区间）
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Radar {
        private Double invest;    // 投入
        private Double quality;   // 质量
        private Double mastery;   // 掌握
        private Double stability; // 稳定
        private Double growth;    // 成长
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trends {
        private List<Point> score;      // 成绩走势
        private List<Point> completion; // 完成率趋势
        private List<Point> hours;      // 学习时长趋势
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private String x; // 日期或时间点
        private Double y; // 数值
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentGradeItem {
        private String assignmentTitle;
        private String courseTitle;
        private BigDecimal score;
        private String gradedAt;
    }
}


