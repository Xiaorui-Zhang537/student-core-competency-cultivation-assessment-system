package com.noncore.assessment.service.impl;

import com.noncore.assessment.behavior.BehaviorAbilityDimensionCode;
import com.noncore.assessment.behavior.BehaviorLevel;
import com.noncore.assessment.behavior.BehaviorSchemaVersions;
import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.response.BehaviorInsightResponse;
import com.noncore.assessment.dto.response.BehaviorSummaryResponse;
import com.noncore.assessment.entity.BehaviorInsight;
import com.noncore.assessment.entity.BehaviorSummarySnapshot;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiQuotaService;
import com.noncore.assessment.service.AiService;
import com.noncore.assessment.service.BehaviorAggregationService;
import com.noncore.assessment.service.BehaviorInsightGenerationService;
import com.noncore.assessment.service.BehaviorInsightService;
import com.noncore.assessment.service.BehaviorSummarySnapshotService;
import com.noncore.assessment.util.Jsons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 行为洞察生成（阶段二）：调用 AI 做解释与总结（JSON-only），并强制证据引用。
 */
@Service
@RequiredArgsConstructor
public class BehaviorInsightGenerationServiceImpl implements BehaviorInsightGenerationService {

    private static final String PROMPT_PATH = "classpath:/prompts/behavior_insight_system_prompt.txt";
    private static final String PROMPT_VERSION = "behavior_insight_prompt.v4";
    private static final String DEFAULT_MODEL = "google/gemini-2.5-pro";

    private final BehaviorAggregationService aggregationService;
    private final BehaviorSummarySnapshotService snapshotService;
    private final BehaviorInsightService insightService;
    private final AiService aiService;
    private final AiQuotaService aiQuotaService;

    @Override
    public BehaviorInsightResponse generate(Long operatorId,
                                           Long studentId,
                                           Long courseId,
                                           String range,
                                           String model,
                                           boolean force,
                                           boolean studentSelfTrigger) {
        if (operatorId == null || studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "operatorId/studentId 必填");
        }

        String rk = (range == null || range.isBlank()) ? "7d" : range.trim().toLowerCase();
        if (!"7d".equals(rk) && !"30d".equals(rk) && !"180d".equals(rk) && !"365d".equals(rk)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "当前仅支持 range=7d/30d/180d/365d");
        }

        if (studentSelfTrigger && force) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生自助触发不允许 force 重跑");
        }

        // 阶段一：确保摘要已生成（纯代码聚合）
        BehaviorSummaryResponse summary = aggregationService.getOrBuildSummary(studentId, courseId, rk);
        BehaviorSummarySnapshot snapshot = snapshotService.getLatest(studentId, courseId, rk, BehaviorSchemaVersions.SUMMARY_V1, null, null);
        if (snapshot == null || snapshot.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法获取摘要快照");
        }

        // 治理：限流/复用策略
        // - 学生自助触发：7天滚动窗口最多 2 次（仅统计 status=success 的记录）
        // - 教师/管理员：默认复用 7 天内 success；force=true 可重跑
        try {
            // 兼容：优先读取 v2；若未生成过 v2，则回退读取 v1（避免升级后“历史洞察全丢失”）
            BehaviorInsight existing = studentSelfTrigger
                    ? firstNonNull(
                        insightService.getLatestByStudentCourse(studentId, courseId, BehaviorSchemaVersions.INSIGHT_V2),
                        insightService.getLatestByStudentCourse(studentId, courseId, BehaviorSchemaVersions.INSIGHT_V1)
                    )
                    : firstNonNull(
                        insightService.getLatestBySnapshot(snapshot.getId(), BehaviorSchemaVersions.INSIGHT_V2),
                        insightService.getLatestBySnapshot(snapshot.getId(), BehaviorSchemaVersions.INSIGHT_V1)
                    );

            LocalDateTime now = LocalDateTime.now();

            if (studentSelfTrigger) {
                Map<String, Object> quota = buildStudentQuota(studentId, now);
                long usedInWindow = readLong(quota.get("usedInWindow"));
                long limitPerWindow = readLong(quota.get("limitPerWindow"));
                if (usedInWindow >= limitPerWindow) {
                    // 达到滚动窗口上限：返回最近一次结果 + 直到窗口释放
                    BehaviorInsightResponse cached = (existing != null && existing.getInsightJson() != null)
                            ? parseAndValidate(existing.getInsightJson(), snapshot.getId(), null)
                            : BehaviorInsightResponse.builder()
                                .schemaVersion(BehaviorSchemaVersions.INSIGHT_V2)
                                .snapshotId(snapshot.getId())
                                .meta(BehaviorInsightResponse.Meta.builder()
                                        .generatedAt(now)
                                        .model((model == null || model.isBlank()) ? DEFAULT_MODEL : model.trim())
                                        .promptVersion(PROMPT_VERSION)
                                        .status("partial")
                                        .build())
                                .build();
                    cached.setSnapshotId(snapshot.getId());
                    cached.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
                    if (cached.getMeta() == null) cached.setMeta(new BehaviorInsightResponse.Meta());
                    cached.getMeta().setStatus(cached.getMeta().getStatus() == null ? "partial" : cached.getMeta().getStatus());
                    java.util.Map<String, Object> extra = cached.getExtra() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(cached.getExtra());
                    applyStudentQuota(extra, quota);
                    cached.setExtra(extra);
                    return cached;
                }
            }

            if (!studentSelfTrigger && existing != null && existing.getGeneratedAt() != null) {
                boolean withinCooldown = existing.getGeneratedAt().isAfter(now.minusDays(7));
                if (!force && "success".equalsIgnoreCase(existing.getStatus()) && withinCooldown) {
                    BehaviorInsightResponse cached = parseAndValidate(existing.getInsightJson(), snapshot.getId(), null);
                    cached.setSnapshotId(snapshot.getId());
                    cached.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
                    if (cached.getMeta() == null) cached.setMeta(new BehaviorInsightResponse.Meta());
                    cached.getMeta().setGeneratedAt(existing.getGeneratedAt());
                    cached.getMeta().setModel(existing.getModel());
                    cached.getMeta().setPromptVersion(existing.getPromptVersion());
                    cached.getMeta().setStatus(existing.getStatus());
                    return cached;
                }
            }
        } catch (Exception ignored) {}

        // 治理：若阶段一没有任何可引用证据，则不调用 AI，直接降级输出“证据不足”
        if (summary.getEvidenceItems() == null || summary.getEvidenceItems().isEmpty()) {
            BehaviorInsightResponse degraded = BehaviorInsightResponse.builder()
                    .schemaVersion(BehaviorSchemaVersions.INSIGHT_V2)
                    .snapshotId(snapshot.getId())
                    .explainScore(BehaviorInsightResponse.ExplainScore.builder()
                            .text("证据不足：当前时间窗内未生成可引用的行为证据条目，无法做进一步解释。")
                            .evidenceIds(java.util.Collections.emptyList())
                            .build())
                    .stageJudgements(java.util.List.of(
                            BehaviorInsightResponse.StageJudgementItem.builder()
                                    .dimensionCode(com.noncore.assessment.behavior.BehaviorAbilityDimensionCode.MORAL_COGNITION)
                                    .level(com.noncore.assessment.behavior.BehaviorLevel.EMERGING)
                                    .rationale("证据不足")
                                    .evidenceIds(java.util.Collections.emptyList())
                                    .build(),
                            BehaviorInsightResponse.StageJudgementItem.builder()
                                    .dimensionCode(com.noncore.assessment.behavior.BehaviorAbilityDimensionCode.LEARNING_ATTITUDE)
                                    .level(com.noncore.assessment.behavior.BehaviorLevel.EMERGING)
                                    .rationale("证据不足")
                                    .evidenceIds(java.util.Collections.emptyList())
                                    .build(),
                            BehaviorInsightResponse.StageJudgementItem.builder()
                                    .dimensionCode(com.noncore.assessment.behavior.BehaviorAbilityDimensionCode.LEARNING_ABILITY)
                                    .level(com.noncore.assessment.behavior.BehaviorLevel.EMERGING)
                                    .rationale("证据不足")
                                    .evidenceIds(java.util.Collections.emptyList())
                                    .build(),
                            BehaviorInsightResponse.StageJudgementItem.builder()
                                    .dimensionCode(com.noncore.assessment.behavior.BehaviorAbilityDimensionCode.LEARNING_METHOD)
                                    .level(com.noncore.assessment.behavior.BehaviorLevel.EMERGING)
                                    .rationale("证据不足")
                                    .evidenceIds(java.util.Collections.emptyList())
                                    .build()
                    ))
                    .formativeSuggestions(java.util.Collections.emptyList())
                    .meta(BehaviorInsightResponse.Meta.builder()
                            .generatedAt(LocalDateTime.now())
                            .model((model == null || model.isBlank()) ? DEFAULT_MODEL : model.trim())
                            .promptVersion(PROMPT_VERSION)
                            .status("partial")
                            .build())
                    // 学生测试策略：不因 “证据不足”进入冷却（NO_EVIDENCE 不计入滚动窗口额度）
                    .extra(studentSelfTrigger ? java.util.Map.of(
                            "quota", buildStudentQuota(studentId, LocalDateTime.now())
                    ) : null)
                    .build();

            BehaviorInsight rec = new BehaviorInsight();
            rec.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
            rec.setSnapshotId(snapshot.getId());
            rec.setStudentId(studentId);
            rec.setCourseId(courseId);
            rec.setModel(degraded.getMeta() != null ? degraded.getMeta().getModel() : DEFAULT_MODEL);
            rec.setPromptVersion(PROMPT_VERSION);
            rec.setStatus("partial");
            rec.setInsightJson(Jsons.toJson(degraded));
            rec.setErrorMessage("NO_EVIDENCE");
            rec.setGeneratedAt(LocalDateTime.now());
            rec.setCreatedAt(LocalDateTime.now());
            insightService.save(rec);
            appendReportContext(degraded, snapshot.getId());
            return degraded;
        }

        // 阶段二：调用 AI（JSON-only），只读摘要 JSON
        String targetModel = (model == null || model.isBlank()) ? DEFAULT_MODEL : model.trim();
        AiChatRequest req = new AiChatRequest();
        req.setCourseId(courseId);
        req.setStudentIds(java.util.List.of(studentId));
        req.setModel(targetModel);
        req.setJsonOnly(Boolean.TRUE);
        req.setUseGradingPrompt(Boolean.TRUE);
        req.setSystemPromptPath(PROMPT_PATH);

        String payload = Jsons.toJson(summary);
        req.setMessages(java.util.List.of(new AiChatRequest.Message("user", payload)));

        String raw;
        String status = "success";
        String err = null;
        BehaviorInsightResponse parsed = null;

        try {
            raw = aiService.generateAnswerJsonOnly(req, operatorId);
            parsed = parseAndValidate(raw, snapshot.getId(), summary);
        } catch (Exception e) {
            raw = null;
            status = "failed";
            err = e.getMessage();
        }

        BehaviorInsight rec = new BehaviorInsight();
        rec.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
        rec.setSnapshotId(snapshot.getId());
        rec.setStudentId(studentId);
        rec.setCourseId(courseId);
        rec.setModel(targetModel);
        rec.setPromptVersion(PROMPT_VERSION);
        rec.setStatus(status);
        rec.setInsightJson(raw == null ? "{}" : raw);
        rec.setErrorMessage(err);
        rec.setGeneratedAt(LocalDateTime.now());
        rec.setCreatedAt(LocalDateTime.now());
        insightService.save(rec);

        if (parsed == null) {
            // 失败返回一个最小结构，便于前端显示“暂不可用”
            BehaviorInsightResponse failedResp = BehaviorInsightResponse.builder()
                    .schemaVersion(BehaviorSchemaVersions.INSIGHT_V2)
                    .snapshotId(snapshot.getId())
                    .explainScore(BehaviorInsightResponse.ExplainScore.builder()
                            .text(err == null || err.isBlank() ? "洞察生成失败：AI 服务暂不可用" : ("洞察生成失败：" + err))
                            .evidenceIds(java.util.Collections.emptyList())
                            .build())
                    .meta(BehaviorInsightResponse.Meta.builder()
                            .generatedAt(LocalDateTime.now())
                            .model(targetModel)
                            .promptVersion(PROMPT_VERSION)
                            .status("failed")
                            .build())
                    .extra(studentSelfTrigger ? java.util.Map.of(
                            "quota", buildStudentQuota(studentId, LocalDateTime.now())
                    ) : null)
                    .build();
            appendReportContext(failedResp, snapshot.getId());
            return failedResp;
        }
        // 填充 meta（以系统为准）
        if (parsed.getMeta() == null) {
            parsed.setMeta(new BehaviorInsightResponse.Meta());
        }
        parsed.getMeta().setGeneratedAt(LocalDateTime.now());
        parsed.getMeta().setModel(targetModel);
        parsed.getMeta().setPromptVersion(PROMPT_VERSION);
        parsed.getMeta().setStatus("success");
        parsed.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
        parsed.setSnapshotId(snapshot.getId());
        appendReportContext(parsed, snapshot.getId());
        if (studentSelfTrigger) {
            java.util.Map<String, Object> extra = parsed.getExtra() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(parsed.getExtra());
            applyStudentQuota(extra, buildStudentQuota(studentId, LocalDateTime.now()));
            parsed.setExtra(extra);
        }
        return parsed;
    }

    @Override
    public BehaviorInsightResponse getLatest(Long operatorId, Long studentId, Long courseId, String range) {
        if (operatorId == null || studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "operatorId/studentId 必填");
        }
        String rk = (range == null || range.isBlank()) ? "7d" : range.trim().toLowerCase();
        if (!"7d".equals(rk) && !"30d".equals(rk) && !"180d".equals(rk) && !"365d".equals(rk)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "当前仅支持 range=7d/30d/180d/365d");
        }
        BehaviorSummarySnapshot snapshot = snapshotService.getLatest(studentId, courseId, rk, BehaviorSchemaVersions.SUMMARY_V1, null, null);

        // 兼容：优先 v2，回退 v1
        // 注意：摘要快照可能因“有新事件”而刷新（snapshotId 变化），此时洞察未必已重新生成。
        // 为避免前端每次都要点“生成”，这里增加回退：按 student+course+rangeKey 返回最近一次洞察。
        BehaviorInsight latest = null;
        if (snapshot != null && snapshot.getId() != null) {
            latest = firstNonNull(
                    insightService.getLatestBySnapshot(snapshot.getId(), BehaviorSchemaVersions.INSIGHT_V2),
                    insightService.getLatestBySnapshot(snapshot.getId(), BehaviorSchemaVersions.INSIGHT_V1)
            );
        }
        boolean fallbackUsed = false;
        boolean anyRangeFallbackUsed = false;
        boolean anyCourseFallbackUsed = false;
        if (latest == null || latest.getInsightJson() == null) {
            latest = firstNonNull(
                    insightService.getLatestByStudentCourseRange(studentId, courseId, rk, BehaviorSchemaVersions.INSIGHT_V2),
                    insightService.getLatestByStudentCourseRange(studentId, courseId, rk, BehaviorSchemaVersions.INSIGHT_V1)
            );
            fallbackUsed = latest != null && latest.getInsightJson() != null;
        }
        // 教师/管理员端常见场景：当前选择窗无数据，但历史曾生成过报告。
        // 兜底为“最近一次报告（不限制 range）”，保证报告区始终有最近结果可读。
        if (latest == null || latest.getInsightJson() == null) {
            latest = firstNonNull(
                    insightService.getLatestByStudentCourse(studentId, courseId, BehaviorSchemaVersions.INSIGHT_V2),
                    insightService.getLatestByStudentCourse(studentId, courseId, BehaviorSchemaVersions.INSIGHT_V1)
            );
            anyRangeFallbackUsed = latest != null && latest.getInsightJson() != null;
        }
        // 兼容：若传入了 courseId 仍未命中，回退到“任意课程”的最近结果。
        // 典型场景：教师在“全部课程”上下文生成（course_id 为空），学生端按课程查看时需要可见。
        if ((latest == null || latest.getInsightJson() == null) && courseId != null) {
            latest = firstNonNull(
                    insightService.getLatestByStudentAnyCourseRange(studentId, rk, BehaviorSchemaVersions.INSIGHT_V2),
                    insightService.getLatestByStudentAnyCourseRange(studentId, rk, BehaviorSchemaVersions.INSIGHT_V1)
            );
            anyCourseFallbackUsed = latest != null && latest.getInsightJson() != null;
        }
        if ((latest == null || latest.getInsightJson() == null) && courseId != null) {
            latest = firstNonNull(
                    insightService.getLatestByStudentAnyCourse(studentId, BehaviorSchemaVersions.INSIGHT_V2),
                    insightService.getLatestByStudentAnyCourse(studentId, BehaviorSchemaVersions.INSIGHT_V1)
            );
            anyCourseFallbackUsed = latest != null && latest.getInsightJson() != null;
        }
        if (latest == null || latest.getInsightJson() == null) return null;
        try {
            Long usedSnapshotId = latest.getSnapshotId() != null
                    ? latest.getSnapshotId()
                    : (snapshot == null ? null : snapshot.getId());
            BehaviorInsightResponse resp = parseAndValidate(latest.getInsightJson(), usedSnapshotId, null);
            resp.setSnapshotId(usedSnapshotId);
            resp.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
            if (resp.getMeta() == null) resp.setMeta(new BehaviorInsightResponse.Meta());
            resp.getMeta().setModel(latest.getModel());
            resp.getMeta().setPromptVersion(latest.getPromptVersion());
            resp.getMeta().setStatus(latest.getStatus());
            resp.getMeta().setGeneratedAt(latest.getGeneratedAt());
            appendReportContext(resp, usedSnapshotId);
            // 学生查看自己的洞察：注入滚动窗口额度/冷却信息（达到上限才冷却）
            if (operatorId != null && operatorId.equals(studentId)) {
                java.util.Map<String, Object> extra = resp.getExtra() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(resp.getExtra());
                applyStudentQuota(extra, buildStudentQuota(studentId, LocalDateTime.now()));
                if (fallbackUsed && snapshot != null && snapshot.getId() != null && !snapshot.getId().equals(usedSnapshotId)) {
                    extra.put("latestSummarySnapshotId", snapshot.getId());
                    extra.put("notice", "当前展示为最近一次已生成的洞察；最新摘要快照尚未生成洞察。");
                }
                if (anyCourseFallbackUsed) {
                    extra.put("notice", "当前课程暂无可用洞察，已自动展示最近一次历史洞察。");
                }
                if (anyRangeFallbackUsed) {
                    extra.put("notice", "当前行为时间窗暂无可用洞察，已自动展示最近一次历史洞察。");
                }
                resp.setExtra(extra);
            } else if (anyRangeFallbackUsed || anyCourseFallbackUsed) {
                java.util.Map<String, Object> extra = resp.getExtra() == null ? new java.util.HashMap<>() : new java.util.HashMap<>(resp.getExtra());
                if (anyCourseFallbackUsed) {
                    extra.put("notice", "当前课程暂无可用洞察，已自动展示最近一次历史洞察。");
                } else {
                    extra.put("notice", "当前行为时间窗暂无可用洞察，已自动展示最近一次历史洞察。");
                }
                resp.setExtra(extra);
            }
            return resp;
        } catch (Exception e) {
            return null;
        }
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... items) {
        if (items == null) return null;
        for (T it : items) {
            if (it != null) return it;
        }
        return null;
    }

    private Map<String, Object> buildStudentQuota(Long studentId, LocalDateTime now) {
        int windowDays = AiQuotaService.INSIGHT_WINDOW_DAYS;
        long baseLimit = AiQuotaService.BASE_INSIGHT_WINDOW_LIMIT;
        long bonusLimit = 0;
        try {
            bonusLimit = Math.max(0, aiQuotaService.getInsightBonusWindow(studentId));
        } catch (Exception ignored) {}
        long limitPerWindow = baseLimit + bonusLimit;
        LocalDateTime current = now == null ? LocalDateTime.now() : now;
        LocalDateTime windowStartedAt = current.minusDays(windowDays);
        long usedInWindow =
                insightService.countByStudentSince(studentId, BehaviorSchemaVersions.INSIGHT_V2, windowStartedAt)
              + insightService.countByStudentSince(studentId, BehaviorSchemaVersions.INSIGHT_V1, windowStartedAt);
        LocalDateTime earliestCountedAt = usedInWindow <= 0
                ? null
                : insightService.getEarliestCountedByStudentSince(studentId, windowStartedAt);
        LocalDateTime nextAvailableAt = earliestCountedAt == null
                ? null
                : earliestCountedAt.plusDays(windowDays);

        Map<String, Object> quota = new java.util.LinkedHashMap<>();
        quota.put("windowDays", windowDays);
        quota.put("baseLimitPerWindow", baseLimit);
        quota.put("bonusLimitPerWindow", bonusLimit);
        quota.put("limitPerWindow", limitPerWindow);
        quota.put("usedInWindow", usedInWindow);
        quota.put("windowStartedAt", windowStartedAt);
        quota.put("nextAvailableAt", nextAvailableAt);
        return quota;
    }

    private void applyStudentQuota(Map<String, Object> extra, Map<String, Object> quota) {
        if (extra == null || quota == null) {
            return;
        }
        extra.put("quota", quota);
        long usedInWindow = readLong(quota.get("usedInWindow"));
        long limitPerWindow = readLong(quota.get("limitPerWindow"));
        if (usedInWindow >= limitPerWindow) {
            Object nextAvailableAt = quota.get("nextAvailableAt");
            if (nextAvailableAt != null) {
                extra.put("cooldown", java.util.Map.of(
                        "active", true,
                        "nextAvailableAt", nextAvailableAt
                ));
            }
        }
    }

    private long readLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /**
     * 解析并校验 AI 输出：
     * - 必须是 JSON 对象
     * - evidenceIds 必须来自阶段一 evidenceItems（当 summary 非空时）
     * - 不得出现明显的“新分数/权重”字段（弱校验）
     */
    @SuppressWarnings("unchecked")
    private BehaviorInsightResponse parseAndValidate(String rawJson, Long snapshotId, BehaviorSummaryResponse summary) {
        Map<String, Object> root = Jsons.parseObject(rawJson);

        // 治理：禁止出现常见评分字段（避免 AI “越权算分”）
        if (containsForbiddenScoringFields(root)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 输出疑似包含分数/权重字段，已拒绝");
        }

        Set<String> allowedEvidenceIds = new HashSet<>();
        if (summary != null && summary.getEvidenceItems() != null) {
            for (var it : summary.getEvidenceItems()) {
                if (it != null && it.getEvidenceId() != null) {
                    allowedEvidenceIds.add(it.getEvidenceId());
                }
            }
        }

        BehaviorInsightResponse out = new BehaviorInsightResponse();
        out.setSchemaVersion(BehaviorSchemaVersions.INSIGHT_V2);
        out.setSnapshotId(snapshotId);

        Object explain = root.get("explainScore");
        if (explain instanceof Map<?, ?> em) {
            BehaviorInsightResponse.ExplainScore ex = new BehaviorInsightResponse.ExplainScore();
            ex.setText(em.get("text") == null ? null : String.valueOf(em.get("text")));
            ex.setEvidenceIds(extractEvidenceIds(em.get("evidenceIds")));
            out.setExplainScore(ex);
        }

        Object sjs = root.get("stageJudgements");
        if (sjs instanceof List<?> list) {
            List<BehaviorInsightResponse.StageJudgementItem> items = new ArrayList<>();
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> m)) continue;
                BehaviorInsightResponse.StageJudgementItem it = new BehaviorInsightResponse.StageJudgementItem();
                String dim = m.get("dimensionCode") == null ? null : String.valueOf(m.get("dimensionCode"));
                String lvl = m.get("level") == null ? null : String.valueOf(m.get("level"));
                it.setDimensionCode(BehaviorAbilityDimensionCode.fromCode(dim).orElse(null));
                it.setLevel(BehaviorLevel.fromString(lvl).orElse(null));
                it.setRationale(m.get("rationale") == null ? null : String.valueOf(m.get("rationale")));
                it.setEvidenceIds(extractEvidenceIds(m.get("evidenceIds")));
                items.add(it);
            }
            out.setStageJudgements(items);
        }

        Object sugg = root.get("formativeSuggestions");
        if (sugg instanceof List<?> list) {
            List<BehaviorInsightResponse.FormativeSuggestion> items = new ArrayList<>();
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> m)) continue;
                BehaviorInsightResponse.FormativeSuggestion it = new BehaviorInsightResponse.FormativeSuggestion();
                it.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
                it.setDescription(m.get("description") == null ? null : String.valueOf(m.get("description")));
                Object na = m.get("nextActions");
                if (na instanceof List<?> nl) {
                    List<String> ns = new ArrayList<>();
                    for (Object x : nl) if (x != null) ns.add(String.valueOf(x));
                    it.setNextActions(ns);
                }
                it.setEvidenceIds(extractEvidenceIds(m.get("evidenceIds")));
                items.add(it);
            }
            out.setFormativeSuggestions(items);
        }

        // v2: 结构化风险预警
        Object ra = root.get("riskAlerts");
        if (ra instanceof List<?> list) {
            List<BehaviorInsightResponse.RiskAlert> items = new ArrayList<>();
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> m)) continue;
                BehaviorInsightResponse.RiskAlert it = new BehaviorInsightResponse.RiskAlert();
                it.setSeverity(m.get("severity") == null ? null : String.valueOf(m.get("severity")));
                it.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
                it.setMessage(m.get("message") == null ? null : String.valueOf(m.get("message")));
                String dim = m.get("dimensionCode") == null ? null : String.valueOf(m.get("dimensionCode"));
                it.setDimensionCode(BehaviorAbilityDimensionCode.fromCode(dim).orElse(null));
                it.setEvidenceIds(extractEvidenceIds(m.get("evidenceIds")));
                items.add(it);
            }
            out.setRiskAlerts(items);
        }

        // v2: 结构化行动建议
        Object ar = root.get("actionRecommendations");
        if (ar instanceof List<?> list) {
            List<BehaviorInsightResponse.ActionRecommendation> items = new ArrayList<>();
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> m)) continue;
                BehaviorInsightResponse.ActionRecommendation it = new BehaviorInsightResponse.ActionRecommendation();
                it.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
                it.setDescription(m.get("description") == null ? null : String.valueOf(m.get("description")));
                Object na = m.get("nextActions");
                if (na instanceof List<?> nl) {
                    List<String> ns = new ArrayList<>();
                    for (Object x : nl) if (x != null) ns.add(String.valueOf(x));
                    it.setNextActions(ns);
                }
                String dim = m.get("dimensionCode") == null ? null : String.valueOf(m.get("dimensionCode"));
                it.setDimensionCode(BehaviorAbilityDimensionCode.fromCode(dim).orElse(null));
                it.setEvidenceIds(extractEvidenceIds(m.get("evidenceIds")));
                items.add(it);
            }
            out.setActionRecommendations(items);
        }

        // 若 provided summary，强制 evidenceIds 都在白名单（否则拒绝）
        if (summary != null) {
            validateEvidenceIds(out, allowedEvidenceIds);
            // 治理：强制覆盖 4 个维度（缺失则补齐为“证据不足”）
            ensureAllDimensions(out);
        }
        return out;
    }

    private List<String> extractEvidenceIds(Object v) {
        if (v instanceof List<?> list) {
            List<String> ids = new ArrayList<>();
            for (Object o : list) {
                if (o == null) continue;
                ids.add(String.valueOf(o));
            }
            return ids;
        }
        return java.util.Collections.emptyList();
    }

    private void validateEvidenceIds(BehaviorInsightResponse out, Set<String> allowed) {
        if (allowed == null) return;
        java.util.function.Consumer<List<String>> check = (ids) -> {
            if (ids == null) return;
            for (String id : ids) {
                if (id == null) continue;
                if (!allowed.contains(id)) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 输出引用了不存在的 evidenceId: " + id);
                }
            }
        };
        if (out.getExplainScore() != null) check.accept(out.getExplainScore().getEvidenceIds());
        if (out.getStageJudgements() != null) {
            for (var it : out.getStageJudgements()) {
                if (it != null) check.accept(it.getEvidenceIds());
            }
        }
        if (out.getFormativeSuggestions() != null) {
            for (var it : out.getFormativeSuggestions()) {
                if (it != null) check.accept(it.getEvidenceIds());
            }
        }
        if (out.getRiskAlerts() != null) {
            for (var it : out.getRiskAlerts()) {
                if (it != null) check.accept(it.getEvidenceIds());
            }
        }
        if (out.getActionRecommendations() != null) {
            for (var it : out.getActionRecommendations()) {
                if (it != null) check.accept(it.getEvidenceIds());
            }
        }
    }

    private void ensureAllDimensions(BehaviorInsightResponse out) {
        java.util.Set<BehaviorAbilityDimensionCode> present = new java.util.HashSet<>();
        if (out.getStageJudgements() != null) {
            for (var it : out.getStageJudgements()) {
                if (it != null && it.getDimensionCode() != null) present.add(it.getDimensionCode());
            }
        }
        java.util.List<BehaviorInsightResponse.StageJudgementItem> list =
                out.getStageJudgements() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(out.getStageJudgements());

        for (BehaviorAbilityDimensionCode dim : BehaviorAbilityDimensionCode.values()) {
            if (!present.contains(dim)) {
                list.add(BehaviorInsightResponse.StageJudgementItem.builder()
                        .dimensionCode(dim)
                        .level(BehaviorLevel.EMERGING)
                        .rationale("证据不足")
                        .evidenceIds(java.util.Collections.emptyList())
                        .build());
            }
        }
        out.setStageJudgements(list);
    }

    @SuppressWarnings({"unchecked"})
    private boolean containsForbiddenScoringFields(Object node) {
        if (node == null) return false;
        java.util.Set<String> forbiddenKeys = java.util.Set.of(
                "score", "max_score", "final_score", "overall_score", "percentage", "percent", "weight",
                "gpa", "rank", "ranking", "weighted", "average", "avg_score", "overallScore", "finalScore"
        );
        if (node instanceof java.util.Map<?, ?> m) {
            for (java.util.Map.Entry<?, ?> e : m.entrySet()) {
                String key = e.getKey() == null ? null : String.valueOf(e.getKey());
                // explainScore 是允许的结构名（不代表数值），但其内部仍不可出现 score/max_score 等字段
                if (key != null && forbiddenKeys.contains(key)) {
                    return true;
                }
                if (containsForbiddenScoringFields(e.getValue())) {
                    return true;
                }
            }
        } else if (node instanceof java.util.List<?> list) {
            for (Object it : list) {
                if (containsForbiddenScoringFields(it)) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void appendReportContext(BehaviorInsightResponse resp, Long snapshotId) {
        if (resp == null || snapshotId == null) return;
        try {
            BehaviorSummarySnapshot snapshot = snapshotService.getById(snapshotId);
            if (snapshot == null || snapshot.getSummaryJson() == null || snapshot.getSummaryJson().isBlank()) return;
            Map<String, Object> root = Jsons.parseObject(snapshot.getSummaryJson());
            if (root == null || root.isEmpty()) return;

            Map<String, Object> reportContext = new HashMap<>();
            reportContext.put("snapshotId", snapshot.getId());
            reportContext.put("range", snapshot.getRangeKey());
            reportContext.put("from", snapshot.getPeriodFrom());
            reportContext.put("to", snapshot.getPeriodTo());
            reportContext.put("inputEventCount", snapshot.getInputEventCount());

            Map<String, Object> stats = new HashMap<>();
            stats.put("aiQuestionCount", getNestedInt(root, "activityStats", "ai", "questionCount"));
            stats.put("aiFollowUpCount", getNestedInt(root, "activityStats", "ai", "followUpCount"));
            stats.put("assignmentSubmitCount", getNestedInt(root, "activityStats", "assignment", "submitCount"));
            stats.put("assignmentResubmitCount", getNestedInt(root, "activityStats", "assignment", "resubmitCount"));
            stats.put("communityAskCount", getNestedInt(root, "activityStats", "community", "askCount"));
            stats.put("communityAnswerCount", getNestedInt(root, "activityStats", "community", "answerCount"));
            stats.put("feedbackViewCount", getNestedInt(root, "activityStats", "feedback", "viewCount"));
            reportContext.put("activityStats", stats);
            Object signals = root.get("signals");
            if (signals instanceof Map<?, ?> signalsMapRaw) {
                Map<String, Object> signalsMap = (Map<String, Object>) signalsMapRaw;
                Map<String, Object> goalSummary = new HashMap<>();
                goalSummary.put("goalSetCount", signalsMap.get("goalSetCount"));
                goalSummary.put("goalUpdateCount", signalsMap.get("goalUpdateCount"));
                goalSummary.put("goalAchievedCount", signalsMap.get("goalAchievedCount"));
                goalSummary.put("goalCompletionRate", signalsMap.get("goalCompletionRate"));
                goalSummary.put("overdueRecoveryCount", signalsMap.get("overdueRecoveryCount"));
                goalSummary.put("activeGoalCount", signalsMap.get("activeGoalCount"));
                goalSummary.put("overdueGoalCount", signalsMap.get("overdueGoalCount"));
                goalSummary.put("hasOverdueGoals", signalsMap.get("hasOverdueGoals"));
                goalSummary.put("goalLinkedScoreDelta", signalsMap.get("goalLinkedScoreDelta"));
                goalSummary.put("goalFocusSummary", signalsMap.get("goalFocusSummary"));
                goalSummary.put("goalNextTargetDate", signalsMap.get("goalNextTargetDate"));
                goalSummary.put("goalNearestDueInDays", signalsMap.get("goalNearestDueInDays"));
                goalSummary.put("goalLatestActionAt", signalsMap.get("goalLatestActionAt"));
                reportContext.put("goalSummary", goalSummary);
            }

            Map<String, Object> extra = resp.getExtra() == null ? new HashMap<>() : new HashMap<>(resp.getExtra());
            extra.put("reportContext", reportContext);
            resp.setExtra(extra);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private Integer getNestedInt(Map<String, Object> root, String k1, String k2, String k3) {
        try {
            Object o1 = root.get(k1);
            if (!(o1 instanceof Map<?, ?> m1)) return 0;
            Object o2 = ((Map<String, Object>) m1).get(k2);
            if (!(o2 instanceof Map<?, ?> m2)) return 0;
            Object o3 = ((Map<String, Object>) m2).get(k3);
            if (o3 == null) return 0;
            return (int) Math.round(Double.parseDouble(String.valueOf(o3)));
        } catch (Exception ignored) {
            return 0;
        }
    }

}
