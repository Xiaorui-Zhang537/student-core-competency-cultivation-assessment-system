package com.noncore.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noncore.assessment.behavior.BehaviorAbilityDimensionCode;
import com.noncore.assessment.behavior.BehaviorLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 行为证据评价：阶段二 AI 解读输出（只解释与总结，不计算分数）。
 *
 * <p>强约束：不得输出任何新分数、百分比、加权结果。所有结论必须引用 evidenceIds。</p>
 */
@Schema(description = "行为证据-阶段二洞察（AI 解读）")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorInsightResponse {

    @Schema(description = "洞察 schema 版本", example = "behavior_insight.v1")
    private String schemaVersion;

    @Schema(description = "对应的摘要快照ID（阶段一产物）", example = "101")
    private Long snapshotId;

    @Schema(description = "解释作业成绩的过程性说明（不产生新分数）")
    private ExplainScore explainScore;

    @Schema(description = "阶段性能力判断（非分数），每项必须引用 evidenceIds")
    private List<StageJudgementItem> stageJudgements;

    @Schema(description = "形成性建议（可执行动作），每条建议建议引用 evidenceIds")
    private List<FormativeSuggestion> formativeSuggestions;

    @Schema(description = "结构化风险预警（用于前端直接展示），每条必须引用 evidenceIds")
    private List<RiskAlert> riskAlerts;

    @Schema(description = "结构化行动建议（用于前端直接展示），每条建议必须引用 evidenceIds")
    private List<ActionRecommendation> actionRecommendations;

    @Schema(description = "生成元信息（用于审计）")
    private Meta meta;

    @Schema(description = "可选：用于调试/治理的额外字段（不建议前端展示）")
    private Map<String, Object> extra;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExplainScore {
        @Schema(description = "解释文本（解释作业成绩背后的学习过程）")
        private String text;

        @Schema(description = "支持该解释的 evidenceIds（必须存在于摘要中）")
        private List<String> evidenceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageJudgementItem {
        @Schema(description = "能力维度编码（不含学习成绩维度）", example = "LEARNING_ABILITY")
        private BehaviorAbilityDimensionCode dimensionCode;

        @Schema(description = "阶段档位（非分数）", example = "DEVELOPING")
        private BehaviorLevel level;

        @Schema(description = "判断依据（必须引用 evidenceIds）")
        private String rationale;

        @Schema(description = "证据引用列表（必须存在于摘要中）")
        private List<String> evidenceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormativeSuggestion {
        @Schema(description = "建议标题", example = "针对反馈做一次对照修改")
        private String title;

        @Schema(description = "建议描述（尽量可执行）")
        private String description;

        @Schema(description = "下一步行动（可选，清单形式）")
        private List<String> nextActions;

        @Schema(description = "证据引用列表（可选但推荐）")
        private List<String> evidenceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskAlert {
        @Schema(description = "严重程度", example = "warn", allowableValues = {"info", "warn", "critical"})
        private String severity;

        @Schema(description = "标题（短句）", example = "近7天学习节奏不稳定")
        private String title;

        @Schema(description = "说明（可读文本）")
        private String message;

        @Schema(description = "关联维度（可选）", example = "LEARNING_ATTITUDE")
        private BehaviorAbilityDimensionCode dimensionCode;

        @Schema(description = "证据引用列表（必须存在于摘要中）")
        private List<String> evidenceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRecommendation {
        @Schema(description = "标题（短句）", example = "把反馈迭代固化成自查清单")
        private String title;

        @Schema(description = "建议描述（尽量可执行）")
        private String description;

        @Schema(description = "下一步行动（可选，清单形式）")
        private List<String> nextActions;

        @Schema(description = "关联维度（可选）", example = "LEARNING_METHOD")
        private BehaviorAbilityDimensionCode dimensionCode;

        @Schema(description = "证据引用列表（必须存在于摘要中）")
        private List<String> evidenceIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @Schema(description = "生成时间", example = "2026-01-08 12:05:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generatedAt;

        @Schema(description = "模型标识（用于审计）", example = "google/gemini-2.5-pro")
        private String model;

        @Schema(description = "Prompt 版本（用于审计）", example = "behavior_insight_prompt.v1")
        private String promptVersion;

        @Schema(description = "生成状态", example = "success", allowableValues = {"success", "failed", "partial"})
        private String status;
    }
}

