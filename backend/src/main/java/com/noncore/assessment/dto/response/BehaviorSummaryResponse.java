package com.noncore.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行为证据评价：阶段一聚合结果（纯代码生成，不调用 AI）。
 *
 * <p>该响应只包含“事实统计 + 证据引用”，不包含任何分数/加权结果。</p>
 */
@Schema(description = "行为证据-阶段一摘要")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorSummaryResponse {

    @Schema(description = "摘要 schema 版本", example = "behavior_summary.v1")
    private String schemaVersion;

    @Schema(description = "摘要元信息")
    private Meta meta;

    @Schema(description = "事实统计（不评价）")
    private ActivityStats activityStats;

    @Schema(description = "证据条目列表（每条都可回溯到原始事件 eventId）")
    private List<EvidenceItem> evidenceItems;

    @Schema(description = "只记录不评价的事件汇总（例如资源访问）")
    private NonEvaluative nonEvaluative;

    @Schema(description = "可复现的规则信号（布尔/枚举），用于约束阶段二解读，不是分数", example = "{\"highIterationOnFeedback\": true}")
    private Map<String, Object> signals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @Schema(description = "学生ID", example = "1")
        private Long studentId;

        @Schema(description = "课程ID", example = "1")
        private Long courseId;

        @Schema(description = "时间窗标识（如 7d / custom）", example = "7d")
        private String range;

        @Schema(description = "时间窗起始（包含）", example = "2026-01-01 00:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime from;

        @Schema(description = "时间窗结束（不包含）", example = "2026-01-08 00:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime to;

        @Schema(description = "生成时间", example = "2026-01-08 12:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generatedAt;

        @Schema(description = "输入事件总数", example = "42")
        private Integer inputEventCount;

        @Schema(description = "包含的事件类型 code 列表", example = "[\"assignment_submit\",\"feedback_view\"]")
        private List<String> eventTypesIncluded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityStats {
        @Schema(description = "AI 提问统计（事实）")
        private AiStats ai;

        @Schema(description = "作业行为统计（事实）")
        private AssignmentStats assignment;

        @Schema(description = "社区互动统计（事实）")
        private CommunityStats community;

        @Schema(description = "反馈查看统计（事实）")
        private FeedbackStats feedback;

        @Schema(description = "资源访问统计（只记录不评价）")
        private ResourceStats resource;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiStats {
        @Schema(description = "提问次数", example = "5")
        private Integer questionCount;

        @Schema(description = "追问次数", example = "2")
        private Integer followUpCount;

        @Schema(description = "追问占比（0-1，事实指标，不是分数）", example = "0.4")
        private Double followUpRate;

        @Schema(description = "典型主题标签（规则/关键词提取，不调用 AI）", example = "[\"递归\",\"复杂度\"]")
        private List<String> topicTags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentStats {
        @Schema(description = "提交次数", example = "3")
        private Integer submitCount;

        @Schema(description = "修改/重交次数", example = "1")
        private Integer resubmitCount;

        @Schema(description = "查看反馈后发生修改的次数", example = "1")
        private Integer resubmitAfterFeedbackCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityStats {
        @Schema(description = "发问次数", example = "1")
        private Integer askCount;

        @Schema(description = "回答次数", example = "2")
        private Integer answerCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackStats {
        @Schema(description = "查看反馈次数", example = "4")
        private Integer viewCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceStats {
        @Schema(description = "资源访问次数", example = "10")
        private Integer viewCount;

        @Schema(description = "资源类别统计（如 video/pdf/link）", example = "{\"video\": 3, \"pdf\": 7}")
        private Map<String, Integer> byCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceItem {
        @Schema(description = "证据ID（用于阶段二引用）", example = "ev_20260108_0001")
        private String evidenceId;

        @Schema(description = "证据类型（可选，规则生成）", example = "feedback_iteration")
        private String evidenceType;

        @Schema(description = "证据标题（规则生成）", example = "查看反馈后进行修改")
        private String title;

        @Schema(description = "证据说明（规则生成）", example = "本周在查看反馈后对作业进行了 1 次修改。")
        private String description;

        @Schema(description = "引用的原始 eventId 列表（可审计）", example = "[1001, 1002]")
        private List<Long> eventRefs;

        @Schema(description = "发生时间（证据代表的关键时刻）", example = "2026-01-06 14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime occurredAt;

        @Schema(description = "可选跳转链接（deeplink）", example = "/assignments/1/submissions/2")
        private String link;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NonEvaluative {
        @Schema(description = "只记录不评价的事件条目（可按需截断）")
        private List<NonEvaluativeItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NonEvaluativeItem {
        @Schema(description = "事件类型 code", example = "resource_view")
        private String eventType;

        @Schema(description = "统计值（如访问次数）", example = "10")
        private Integer count;

        @Schema(description = "补充信息（可选）")
        private Map<String, Object> meta;
    }
}

