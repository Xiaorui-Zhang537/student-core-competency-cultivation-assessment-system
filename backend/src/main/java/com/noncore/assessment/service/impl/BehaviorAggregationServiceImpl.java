package com.noncore.assessment.service.impl;

import com.noncore.assessment.behavior.BehaviorEventType;
import com.noncore.assessment.behavior.BehaviorSchemaVersions;
import com.noncore.assessment.dto.response.BehaviorSummaryResponse;
import com.noncore.assessment.entity.BehaviorEvent;
import com.noncore.assessment.entity.BehaviorSummarySnapshot;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.BehaviorAggregationService;
import com.noncore.assessment.service.BehaviorEventService;
import com.noncore.assessment.service.BehaviorSummarySnapshotService;
import com.noncore.assessment.util.Jsons;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 阶段一聚合器：将行为事件聚合为结构化摘要 JSON（禁止 AI）。
 */
@Service
@RequiredArgsConstructor
public class BehaviorAggregationServiceImpl implements BehaviorAggregationService {

    private static final int DEFAULT_EVENT_LIMIT = 5000;
    private static final DateTimeFormatter EVIDENCE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BehaviorEventService behaviorEventService;
    private final BehaviorSummarySnapshotService snapshotService;

    /**
     * 获取或生成行为摘要（阶段一）。
     *
     * <p>强约束：此方法不允许调用任何 AI 服务。</p>
     */
    @Override
    public BehaviorSummaryResponse getOrBuildSummary(Long studentId, Long courseId, String range) {
        String rk = normalizeRange(range);

        // 先读取最近一次快照（不强制 period 对齐，v0 先保证可用）
        BehaviorSummarySnapshot latest = snapshotService.getLatest(studentId, courseId, rk, BehaviorSchemaVersions.SUMMARY_V1, null, null);
        if (latest != null && latest.getSummaryJson() != null && !latest.getSummaryJson().isBlank()) {
            // 若快照生成后出现了新事件，则认为快照已过期，重新聚合（避免“计数不更新”）
            try {
                if (latest.getGeneratedAt() != null
                        && behaviorEventService.hasEventsAfter(studentId, courseId, latest.getGeneratedAt())) {
                    latest = null;
                }
            } catch (Exception ignored) { }
        }
        if (latest != null && latest.getSummaryJson() != null && !latest.getSummaryJson().isBlank()) {
            // 直接返回快照内 JSON（避免重复计算），但仍返回结构化对象，便于后续演进
            try {
                // 这里不做严格反序列化成 DTO（避免未来字段变更导致失败），先用 Map 回填关键字段
                Map<String, Object> parsed = Jsons.parseObject(latest.getSummaryJson());
                return mapToSummaryResponse(parsed);
            } catch (Exception ignored) {
                // 若解析失败则回退到重新聚合
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7);
        LocalDateTime to = now;

        List<BehaviorEvent> events = behaviorEventService.listByStudentCourseRange(studentId, courseId, from, to, DEFAULT_EVENT_LIMIT);
        List<BehaviorEvent> asc = new ArrayList<>(events == null ? List.of() : events);
        asc.sort(Comparator.comparing(BehaviorEvent::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BehaviorEvent::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        BehaviorSummaryResponse summary = aggregate(studentId, courseId, rk, from, to, asc, now);

        BehaviorSummarySnapshot snapshot = new BehaviorSummarySnapshot();
        snapshot.setSchemaVersion(BehaviorSchemaVersions.SUMMARY_V1);
        snapshot.setStudentId(studentId);
        snapshot.setCourseId(courseId);
        snapshot.setRangeKey(rk);
        snapshot.setPeriodFrom(from);
        snapshot.setPeriodTo(to);
        snapshot.setInputEventCount(summary.getMeta() != null ? summary.getMeta().getInputEventCount() : 0);
        snapshot.setEventTypesIncluded(Jsons.toJson(summary.getMeta() != null ? summary.getMeta().getEventTypesIncluded() : List.of()));
        snapshot.setSummaryJson(Jsons.toJson(summary));
        snapshot.setGeneratedAt(now);
        snapshot.setCreatedAt(now);
        snapshotService.save(snapshot);

        // 重新返回（此时直接返回聚合对象）
        return summary;
    }

    /**
     * 强制重新聚合并保存一个新的快照（阶段一，禁止 AI）。
     */
    @Override
    public BehaviorSummarySnapshot buildAndSaveSnapshot(Long studentId, Long courseId, String range) {
        String rk = normalizeRange(range);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7);
        LocalDateTime to = now;

        List<BehaviorEvent> events = behaviorEventService.listByStudentCourseRange(studentId, courseId, from, to, DEFAULT_EVENT_LIMIT);
        List<BehaviorEvent> asc = new ArrayList<>(events == null ? List.of() : events);
        asc.sort(Comparator.comparing(BehaviorEvent::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BehaviorEvent::getId, Comparator.nullsLast(Comparator.naturalOrder())));

        BehaviorSummaryResponse summary = aggregate(studentId, courseId, rk, from, to, asc, now);

        BehaviorSummarySnapshot snapshot = new BehaviorSummarySnapshot();
        snapshot.setSchemaVersion(BehaviorSchemaVersions.SUMMARY_V1);
        snapshot.setStudentId(studentId);
        snapshot.setCourseId(courseId);
        snapshot.setRangeKey(rk);
        snapshot.setPeriodFrom(from);
        snapshot.setPeriodTo(to);
        snapshot.setInputEventCount(summary.getMeta() != null ? summary.getMeta().getInputEventCount() : 0);
        snapshot.setEventTypesIncluded(Jsons.toJson(summary.getMeta() != null ? summary.getMeta().getEventTypesIncluded() : List.of()));
        snapshot.setSummaryJson(Jsons.toJson(summary));
        snapshot.setGeneratedAt(now);
        snapshot.setCreatedAt(now);
        snapshotService.save(snapshot);
        return snapshot;
    }

    private String normalizeRange(String range) {
        if (range == null || range.isBlank()) return "7d";
        String r = range.trim().toLowerCase();
        if (!"7d".equals(r)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "当前仅支持 range=7d");
        }
        return r;
    }

    private BehaviorSummaryResponse aggregate(Long studentId,
                                              Long courseId,
                                              String rangeKey,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              List<BehaviorEvent> eventsAsc,
                                              LocalDateTime generatedAt) {
        Map<String, Integer> countsByType = new HashMap<>();
        Map<String, Integer> resourceByCategory = new HashMap<>();
        Set<String> typesIncluded = new HashSet<>();

        int resubmitAfterFeedbackCount = 0;
        BehaviorEvent firstFeedbackForResubmit = null;
        BehaviorEvent firstResubmitAfterFeedback = null;

        // 先分离 feedback_view 与 resubmit，做简单“反馈驱动迭代”检测（24h 内）
        List<BehaviorEvent> feedbackViews = new ArrayList<>();
        List<BehaviorEvent> resubmits = new ArrayList<>();

        for (BehaviorEvent e : eventsAsc) {
            if (e == null) continue;
            String t = e.getEventType();
            if (t == null) continue;
            typesIncluded.add(t);
            countsByType.put(t, countsByType.getOrDefault(t, 0) + 1);

            if (BehaviorEventType.FEEDBACK_VIEW.getCode().equalsIgnoreCase(t)) {
                feedbackViews.add(e);
            }
            if (BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode().equalsIgnoreCase(t)) {
                resubmits.add(e);
            }

            if (BehaviorEventType.RESOURCE_VIEW.getCode().equalsIgnoreCase(t)) {
                Map<String, Object> meta = safeParseMeta(e.getMetadata());
                String cat = meta.get("resourceType") != null ? String.valueOf(meta.get("resourceType"))
                        : (meta.get("category") != null ? String.valueOf(meta.get("category")) : null);
                if (cat != null && !cat.isBlank()) {
                    resourceByCategory.put(cat, resourceByCategory.getOrDefault(cat, 0) + 1);
                }
            }
        }

        // 双指针：对每个 resubmit 找到最近的 feedback_view（24h 内）
        int j = 0;
        for (BehaviorEvent r : resubmits) {
            if (r.getOccurredAt() == null) continue;
            LocalDateTime windowStart = r.getOccurredAt().minusHours(24);
            while (j < feedbackViews.size()) {
                BehaviorEvent f = feedbackViews.get(j);
                if (f.getOccurredAt() == null) {
                    j++;
                    continue;
                }
                if (f.getOccurredAt().isBefore(windowStart)) {
                    j++;
                    continue;
                }
                break;
            }
            // 在 windowStart..resubmit 之间取最后一个 feedback
            BehaviorEvent candidate = null;
            int k = j;
            while (k < feedbackViews.size()) {
                BehaviorEvent f = feedbackViews.get(k);
                if (f.getOccurredAt() == null) {
                    k++;
                    continue;
                }
                if (f.getOccurredAt().isAfter(r.getOccurredAt())) {
                    break;
                }
                candidate = f;
                k++;
            }
            if (candidate != null) {
                resubmitAfterFeedbackCount++;
                if (firstFeedbackForResubmit == null) {
                    firstFeedbackForResubmit = candidate;
                    firstResubmitAfterFeedback = r;
                }
            }
        }

        // 组装 activityStats
        int aiQ = countsByType.getOrDefault(BehaviorEventType.AI_QUESTION.getCode(), 0);
        int aiF = countsByType.getOrDefault(BehaviorEventType.AI_FOLLOW_UP.getCode(), 0);
        double followRate = (aiQ + aiF) > 0 ? (aiF * 1.0 / (aiQ + aiF)) : 0.0;

        BehaviorSummaryResponse.ActivityStats stats = BehaviorSummaryResponse.ActivityStats.builder()
                .ai(BehaviorSummaryResponse.AiStats.builder()
                        .questionCount(aiQ)
                        .followUpCount(aiF)
                        .followUpRate(followRate)
                        .topicTags(List.of())
                        .build())
                .assignment(BehaviorSummaryResponse.AssignmentStats.builder()
                        .submitCount(countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), 0))
                        .resubmitCount(countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode(), 0))
                        .resubmitAfterFeedbackCount(resubmitAfterFeedbackCount)
                        .build())
                .community(BehaviorSummaryResponse.CommunityStats.builder()
                        .askCount(countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ASK.getCode(), 0))
                        .answerCount(countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ANSWER.getCode(), 0))
                        .build())
                .feedback(BehaviorSummaryResponse.FeedbackStats.builder()
                        .viewCount(countsByType.getOrDefault(BehaviorEventType.FEEDBACK_VIEW.getCode(), 0))
                        .build())
                .resource(BehaviorSummaryResponse.ResourceStats.builder()
                        .viewCount(countsByType.getOrDefault(BehaviorEventType.RESOURCE_VIEW.getCode(), 0))
                        .byCategory(resourceByCategory.isEmpty() ? null : resourceByCategory)
                        .build())
                .build();

        // 证据条目（v0：只做最小一条“反馈后修改”证据）
        List<BehaviorSummaryResponse.EvidenceItem> evidences = new ArrayList<>();
        int evidenceSeq = 1;
        if (firstFeedbackForResubmit != null && firstResubmitAfterFeedback != null) {
            String eid = buildEvidenceId(generatedAt, evidenceSeq++);
            evidences.add(BehaviorSummaryResponse.EvidenceItem.builder()
                    .evidenceId(eid)
                    .evidenceType("feedback_iteration")
                    .title("查看反馈后进行修改")
                    .description("本阶段内在查看反馈后对作业进行了修改。")
                    .eventRefs(List.of(firstFeedbackForResubmit.getId(), firstResubmitAfterFeedback.getId()))
                    .occurredAt(firstResubmitAfterFeedback.getOccurredAt())
                    .link(extractLink(firstResubmitAfterFeedback))
                    .build());
        }

        // v1：补齐常见行为证据条目，避免“有统计但无 evidenceItems”导致阶段二永远显示“证据不足”
        // 说明：这里仍然只记录事实与引用事件，不做评价、不算分
        int aiTotal = aiQ + aiF;
        if (aiTotal > 0) {
            String eid = buildEvidenceId(generatedAt, evidenceSeq++);
            evidences.add(BehaviorSummaryResponse.EvidenceItem.builder()
                    .evidenceId(eid)
                    .evidenceType("ai_activity")
                    .title("AI 交流记录")
                    .description("本阶段内产生 AI 提问 " + aiQ + " 次，追问 " + aiF + " 次。")
                    .eventRefs(lastEventIdsByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.AI_QUESTION.getCode(),
                            BehaviorEventType.AI_FOLLOW_UP.getCode()
                    ), 5))
                    .occurredAt(latestOccurredAtByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.AI_QUESTION.getCode(),
                            BehaviorEventType.AI_FOLLOW_UP.getCode()
                    )))
                    .build());
        }

        int commAsk = countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ASK.getCode(), 0);
        int commAns = countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ANSWER.getCode(), 0);
        if ((commAsk + commAns) > 0) {
            String eid = buildEvidenceId(generatedAt, evidenceSeq++);
            evidences.add(BehaviorSummaryResponse.EvidenceItem.builder()
                    .evidenceId(eid)
                    .evidenceType("community_activity")
                    .title("社区互动记录")
                    .description("本阶段内社区发问 " + commAsk + " 次，回答/回复 " + commAns + " 次。")
                    .eventRefs(lastEventIdsByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.COMMUNITY_ASK.getCode(),
                            BehaviorEventType.COMMUNITY_ANSWER.getCode()
                    ), 5))
                    .occurredAt(latestOccurredAtByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.COMMUNITY_ASK.getCode(),
                            BehaviorEventType.COMMUNITY_ANSWER.getCode()
                    )))
                    .build());
        }

        int submit = countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), 0);
        int resub = countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode(), 0);
        if ((submit + resub) > 0) {
            String eid = buildEvidenceId(generatedAt, evidenceSeq++);
            evidences.add(BehaviorSummaryResponse.EvidenceItem.builder()
                    .evidenceId(eid)
                    .evidenceType("assignment_activity")
                    .title("作业提交记录")
                    .description("本阶段内作业提交 " + submit + " 次，修改/重交 " + resub + " 次。")
                    .eventRefs(lastEventIdsByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(),
                            BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode()
                    ), 5))
                    .occurredAt(latestOccurredAtByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(),
                            BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode()
                    )))
                    .build());
        }

        int fb = countsByType.getOrDefault(BehaviorEventType.FEEDBACK_VIEW.getCode(), 0);
        if (fb > 0) {
            String eid = buildEvidenceId(generatedAt, evidenceSeq++);
            evidences.add(BehaviorSummaryResponse.EvidenceItem.builder()
                    .evidenceId(eid)
                    .evidenceType("feedback_view")
                    .title("反馈查看记录")
                    .description("本阶段内查看反馈 " + fb + " 次（如成绩评语/能力报告等）。")
                    .eventRefs(lastEventIdsByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.FEEDBACK_VIEW.getCode()
                    ), 5))
                    .occurredAt(latestOccurredAtByTypes(eventsAsc, java.util.Set.of(
                            BehaviorEventType.FEEDBACK_VIEW.getCode()
                    )))
                    .build());
        }

        // nonEvaluative（资源访问）
        List<BehaviorSummaryResponse.NonEvaluativeItem> nonEvalItems = new ArrayList<>();
        int rv = countsByType.getOrDefault(BehaviorEventType.RESOURCE_VIEW.getCode(), 0);
        if (rv > 0) {
            Map<String, Object> meta = resourceByCategory.isEmpty() ? null : Map.of("byCategory", resourceByCategory);
            nonEvalItems.add(BehaviorSummaryResponse.NonEvaluativeItem.builder()
                    .eventType(BehaviorEventType.RESOURCE_VIEW.getCode())
                    .count(rv)
                    .meta(meta)
                    .build());
        }

        Map<String, Object> signals = new HashMap<>();
        signals.put("highIterationOnFeedback", resubmitAfterFeedbackCount >= 1);
        signals.put("lowCommunityEngagement",
                (countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ASK.getCode(), 0)
                        + countsByType.getOrDefault(BehaviorEventType.COMMUNITY_ANSWER.getCode(), 0)) == 0);

        // =========================
        // 时间维度 + 抗刷/抗噪信号（纯事实，不评价）
        // =========================
        try {
            signals.put("activeDays", countActiveDays(eventsAsc));

            Map<String, Object> timeSignals = new HashMap<>();
            timeSignals.put("submitLast24hShare", safeRatio(
                    countByTypesSince(eventsAsc, Set.of(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode()),
                            to == null ? null : to.minusHours(24)),
                    countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), 0)
                            + countsByType.getOrDefault(BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode(), 0)
            ));
            timeSignals.put("submitMaxDailyShare", maxDailyShare(eventsAsc,
                    Set.of(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode())));
            timeSignals.put("aiMaxDailyShare", maxDailyShare(eventsAsc,
                    Set.of(BehaviorEventType.AI_QUESTION.getCode(), BehaviorEventType.AI_FOLLOW_UP.getCode())));

            // burst：滑窗内事件数异常（提示可能“短时间刷记录”，不直接作为能力证据）
            Map<String, Object> burstAll = detectBurst(eventsAsc, null, 5, 8); // 5分钟>=8条（全类型）
            Map<String, Object> burstAi = detectBurst(eventsAsc,
                    Set.of(BehaviorEventType.AI_QUESTION.getCode(), BehaviorEventType.AI_FOLLOW_UP.getCode()),
                    5, 6); // 5分钟>=6条（AI相关）
            Map<String, Object> burstSubmit = detectBurst(eventsAsc,
                    Set.of(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode()),
                    10, 3); // 10分钟>=3条（提交相关）

            Map<String, Object> antiSpam = new HashMap<>();
            Map<String, Integer> caps = defaultDailyCaps();
            Map<String, Integer> effective = applyDailyCaps(eventsAsc, caps);
            antiSpam.put("dailyCaps", caps);
            antiSpam.put("effectiveCountsByType", effective);
            antiSpam.put("burstAll", burstAll);
            antiSpam.put("burstAi", burstAi);
            antiSpam.put("burstSubmit", burstSubmit);

            timeSignals.put("burstAny", Boolean.TRUE.equals(burstAll.get("burst")));
            timeSignals.put("burstAi", Boolean.TRUE.equals(burstAi.get("burst")));
            timeSignals.put("burstSubmit", Boolean.TRUE.equals(burstSubmit.get("burst")));

            signals.put("timeSignals", timeSignals);
            signals.put("antiSpam", antiSpam);
        } catch (Exception ignored) { }

        return BehaviorSummaryResponse.builder()
                .schemaVersion(BehaviorSchemaVersions.SUMMARY_V1)
                .meta(BehaviorSummaryResponse.Meta.builder()
                        .studentId(studentId)
                        .courseId(courseId)
                        .range(rangeKey)
                        .from(from)
                        .to(to)
                        .generatedAt(generatedAt)
                        .inputEventCount(eventsAsc.size())
                        .eventTypesIncluded(typesIncluded.stream().sorted().toList())
                        .build())
                .activityStats(stats)
                .evidenceItems(evidences)
                .nonEvaluative(BehaviorSummaryResponse.NonEvaluative.builder()
                        .items(nonEvalItems.isEmpty() ? null : nonEvalItems)
                        .build())
                .signals(signals)
                .build();
    }

    private String buildEvidenceId(LocalDateTime now, int seq) {
        String day = now == null ? LocalDateTime.now().format(EVIDENCE_DATE) : now.format(EVIDENCE_DATE);
        return "ev_" + day + "_" + String.format("%04d", seq);
    }

    private List<Long> lastEventIdsByTypes(List<BehaviorEvent> eventsAsc, java.util.Set<String> types, int max) {
        if (eventsAsc == null || eventsAsc.isEmpty() || types == null || types.isEmpty() || max <= 0) return java.util.Collections.emptyList();
        List<Long> ids = new ArrayList<>();
        for (int i = eventsAsc.size() - 1; i >= 0 && ids.size() < max; i--) {
            BehaviorEvent e = eventsAsc.get(i);
            if (e == null || e.getEventType() == null) continue;
            for (String t : types) {
                if (t != null && t.equalsIgnoreCase(e.getEventType())) {
                    if (e.getId() != null) ids.add(e.getId());
                    break;
                }
            }
        }
        return ids;
    }

    private LocalDateTime latestOccurredAtByTypes(List<BehaviorEvent> eventsAsc, java.util.Set<String> types) {
        if (eventsAsc == null || eventsAsc.isEmpty() || types == null || types.isEmpty()) return null;
        for (int i = eventsAsc.size() - 1; i >= 0; i--) {
            BehaviorEvent e = eventsAsc.get(i);
            if (e == null || e.getEventType() == null) continue;
            for (String t : types) {
                if (t != null && t.equalsIgnoreCase(e.getEventType())) {
                    return e.getOccurredAt();
                }
            }
        }
        return null;
    }

    private int countActiveDays(List<BehaviorEvent> eventsAsc) {
        if (eventsAsc == null || eventsAsc.isEmpty()) return 0;
        java.util.Set<LocalDate> days = new java.util.HashSet<>();
        for (BehaviorEvent e : eventsAsc) {
            if (e == null || e.getOccurredAt() == null) continue;
            days.add(e.getOccurredAt().toLocalDate());
        }
        return days.size();
    }

    private int countByTypesSince(List<BehaviorEvent> eventsAsc, java.util.Set<String> types, LocalDateTime since) {
        if (eventsAsc == null || eventsAsc.isEmpty() || types == null || types.isEmpty() || since == null) return 0;
        int c = 0;
        for (BehaviorEvent e : eventsAsc) {
            if (e == null || e.getEventType() == null || e.getOccurredAt() == null) continue;
            if (e.getOccurredAt().isBefore(since)) continue;
            for (String t : types) {
                if (t != null && t.equalsIgnoreCase(e.getEventType())) {
                    c++;
                    break;
                }
            }
        }
        return c;
    }

    private Double safeRatio(int num, int den) {
        if (den <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, num * 1.0 / den));
    }

    private Double maxDailyShare(List<BehaviorEvent> eventsAsc, java.util.Set<String> types) {
        if (eventsAsc == null || eventsAsc.isEmpty() || types == null || types.isEmpty()) return 0.0;
        Map<LocalDate, Integer> daily = new HashMap<>();
        int total = 0;
        for (BehaviorEvent e : eventsAsc) {
            if (e == null || e.getEventType() == null || e.getOccurredAt() == null) continue;
            boolean match = false;
            for (String t : types) {
                if (t != null && t.equalsIgnoreCase(e.getEventType())) { match = true; break; }
            }
            if (!match) continue;
            total++;
            LocalDate d = e.getOccurredAt().toLocalDate();
            daily.put(d, daily.getOrDefault(d, 0) + 1);
        }
        if (total <= 0) return 0.0;
        int max = 0;
        for (Integer v : daily.values()) if (v != null && v > max) max = v;
        return safeRatio(max, total);
    }

    private Map<String, Integer> defaultDailyCaps() {
        // 仅用于抗刷/抗噪：不作为“成绩/能力”判断依据
        Map<String, Integer> caps = new HashMap<>();
        caps.put(BehaviorEventType.AI_QUESTION.getCode(), 10);
        caps.put(BehaviorEventType.AI_FOLLOW_UP.getCode(), 20);
        caps.put(BehaviorEventType.COMMUNITY_ASK.getCode(), 5);
        caps.put(BehaviorEventType.COMMUNITY_ANSWER.getCode(), 10);
        caps.put(BehaviorEventType.FEEDBACK_VIEW.getCode(), 30);
        caps.put(BehaviorEventType.ASSIGNMENT_SUBMIT.getCode(), 5);
        caps.put(BehaviorEventType.ASSIGNMENT_RESUBMIT.getCode(), 5);
        // resource_view 为 nonEvaluative，不进入此处（避免把“资源访问”作为刷的目标）
        return caps;
    }

    private Map<String, Integer> applyDailyCaps(List<BehaviorEvent> eventsAsc, Map<String, Integer> caps) {
        Map<String, Integer> effective = new HashMap<>();
        if (eventsAsc == null || eventsAsc.isEmpty() || caps == null || caps.isEmpty()) return effective;
        Map<String, Map<LocalDate, Integer>> seen = new HashMap<>();
        for (BehaviorEvent e : eventsAsc) {
            if (e == null || e.getEventType() == null || e.getOccurredAt() == null) continue;
            String type = e.getEventType();
            Integer cap = findCap(caps, type);
            if (cap == null) continue; // 不在 cap 范围内则不计入 effective
            LocalDate day = e.getOccurredAt().toLocalDate();
            seen.putIfAbsent(type.toLowerCase(), new HashMap<>());
            Map<LocalDate, Integer> daily = seen.get(type.toLowerCase());
            int cur = daily.getOrDefault(day, 0);
            if (cur >= cap) continue;
            daily.put(day, cur + 1);
            effective.put(type, effective.getOrDefault(type, 0) + 1);
        }
        return effective;
    }

    private Integer findCap(Map<String, Integer> caps, String type) {
        if (type == null) return null;
        for (Map.Entry<String, Integer> e : caps.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(type)) return e.getValue();
        }
        return null;
    }

    /**
     * burst 检测：在 rolling windowMinutes 内若事件数 >= threshold，则标记 burst=true
     * 只用于抗刷/抗噪提示，不作为能力证据。
     */
    private Map<String, Object> detectBurst(List<BehaviorEvent> eventsAsc, java.util.Set<String> types, int windowMinutes, int threshold) {
        Map<String, Object> out = new HashMap<>();
        out.put("burst", false);
        out.put("maxInWindow", 0);
        out.put("windowMinutes", windowMinutes);
        out.put("threshold", threshold);
        if (eventsAsc == null || eventsAsc.isEmpty() || windowMinutes <= 0 || threshold <= 0) return out;
        java.util.ArrayDeque<LocalDateTime> q = new java.util.ArrayDeque<>();
        int max = 0;
        for (BehaviorEvent e : eventsAsc) {
            if (e == null || e.getOccurredAt() == null || e.getEventType() == null) continue;
            if (types != null && !types.isEmpty()) {
                boolean match = false;
                for (String t : types) {
                    if (t != null && t.equalsIgnoreCase(e.getEventType())) { match = true; break; }
                }
                if (!match) continue;
            }
            LocalDateTime t = e.getOccurredAt();
            q.addLast(t);
            LocalDateTime cutoff = t.minusMinutes(windowMinutes);
            while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) q.removeFirst();
            if (q.size() > max) max = q.size();
        }
        out.put("maxInWindow", max);
        if (max >= threshold) out.put("burst", true);
        return out;
    }

    private Map<String, Object> safeParseMeta(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) return java.util.Collections.emptyMap();
        try {
            return Jsons.parseObject(metadataJson);
        } catch (Exception ignored) {
            return java.util.Collections.emptyMap();
        }
    }

    private String extractLink(BehaviorEvent e) {
        Map<String, Object> meta = safeParseMeta(e != null ? e.getMetadata() : null);
        Object link = meta.get("link");
        return link == null ? null : String.valueOf(link);
    }

    @SuppressWarnings("unchecked")
    private BehaviorSummaryResponse mapToSummaryResponse(Map<String, Object> m) {
        // v0：从 JSON 里取回关键字段，保证前端可直接使用（不依赖再次聚合）
        BehaviorSummaryResponse resp = new BehaviorSummaryResponse();
        resp.setSchemaVersion(String.valueOf(m.getOrDefault("schemaVersion", BehaviorSchemaVersions.SUMMARY_V1)));
        resp.setSignals((Map<String, Object>) m.get("signals"));

        // activityStats
        Object stats = m.get("activityStats");
        if (stats instanceof Map<?, ?> sm) {
            BehaviorSummaryResponse.ActivityStats out = new BehaviorSummaryResponse.ActivityStats();
            out.setAi(parseAiStats((Map<String, Object>) sm.get("ai")));
            out.setAssignment(parseAssignmentStats((Map<String, Object>) sm.get("assignment")));
            out.setCommunity(parseCommunityStats((Map<String, Object>) sm.get("community")));
            out.setFeedback(parseFeedbackStats((Map<String, Object>) sm.get("feedback")));
            out.setResource(parseResourceStats((Map<String, Object>) sm.get("resource")));
            resp.setActivityStats(out);
        }

        // evidenceItems
        Object evs = m.get("evidenceItems");
        if (evs instanceof List<?> list) {
            List<BehaviorSummaryResponse.EvidenceItem> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> em) {
                    out.add(parseEvidenceItem((Map<String, Object>) em));
                }
            }
            resp.setEvidenceItems(out);
        }

        // nonEvaluative
        Object ne = m.get("nonEvaluative");
        if (ne instanceof Map<?, ?> nem) {
            BehaviorSummaryResponse.NonEvaluative out = new BehaviorSummaryResponse.NonEvaluative();
            Object items = nem.get("items");
            if (items instanceof List<?> il) {
                List<BehaviorSummaryResponse.NonEvaluativeItem> outItems = new ArrayList<>();
                for (Object o : il) {
                    if (o instanceof Map<?, ?> im) {
                        outItems.add(parseNonEvaluativeItem((Map<String, Object>) im));
                    }
                }
                out.setItems(outItems);
            }
            resp.setNonEvaluative(out);
        }

        Object meta = m.get("meta");
        if (meta instanceof Map<?, ?> mm) {
            BehaviorSummaryResponse.Meta out = new BehaviorSummaryResponse.Meta();
            Object sid = mm.get("studentId");
            Object cid = mm.get("courseId");
            out.setStudentId(sid == null ? null : Long.valueOf(String.valueOf(sid)));
            out.setCourseId(cid == null ? null : Long.valueOf(String.valueOf(cid)));
            out.setRange(mm.get("range") == null ? null : String.valueOf(mm.get("range")));
            Object cnt = mm.get("inputEventCount");
            out.setInputEventCount(cnt == null ? null : Integer.valueOf(String.valueOf(cnt)));
            Object et = mm.get("eventTypesIncluded");
            if (et instanceof List<?> list) {
                List<String> ets = new ArrayList<>();
                for (Object o : list) {
                    if (o != null) ets.add(String.valueOf(o));
                }
                out.setEventTypesIncluded(ets);
            }
            resp.setMeta(out);
        }
        return resp;
    }

    private BehaviorSummaryResponse.AiStats parseAiStats(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.AiStats out = new BehaviorSummaryResponse.AiStats();
        out.setQuestionCount(toInt(m.get("questionCount")));
        out.setFollowUpCount(toInt(m.get("followUpCount")));
        out.setFollowUpRate(toDouble(m.get("followUpRate")));
        Object tags = m.get("topicTags");
        if (tags instanceof List<?> tl) {
            List<String> ts = new ArrayList<>();
            for (Object o : tl) if (o != null) ts.add(String.valueOf(o));
            out.setTopicTags(ts);
        }
        return out;
    }

    private BehaviorSummaryResponse.AssignmentStats parseAssignmentStats(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.AssignmentStats out = new BehaviorSummaryResponse.AssignmentStats();
        out.setSubmitCount(toInt(m.get("submitCount")));
        out.setResubmitCount(toInt(m.get("resubmitCount")));
        out.setResubmitAfterFeedbackCount(toInt(m.get("resubmitAfterFeedbackCount")));
        return out;
    }

    private BehaviorSummaryResponse.CommunityStats parseCommunityStats(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.CommunityStats out = new BehaviorSummaryResponse.CommunityStats();
        out.setAskCount(toInt(m.get("askCount")));
        out.setAnswerCount(toInt(m.get("answerCount")));
        return out;
    }

    private BehaviorSummaryResponse.FeedbackStats parseFeedbackStats(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.FeedbackStats out = new BehaviorSummaryResponse.FeedbackStats();
        out.setViewCount(toInt(m.get("viewCount")));
        return out;
    }

    @SuppressWarnings("unchecked")
    private BehaviorSummaryResponse.ResourceStats parseResourceStats(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.ResourceStats out = new BehaviorSummaryResponse.ResourceStats();
        out.setViewCount(toInt(m.get("viewCount")));
        Object bc = m.get("byCategory");
        if (bc instanceof Map<?, ?> bcm) {
            Map<String, Integer> map = new HashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) bc).entrySet()) {
                if (e.getKey() != null) {
                    map.put(String.valueOf(e.getKey()), toInt(e.getValue()));
                }
            }
            out.setByCategory(map);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private BehaviorSummaryResponse.EvidenceItem parseEvidenceItem(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.EvidenceItem out = new BehaviorSummaryResponse.EvidenceItem();
        out.setEvidenceId(m.get("evidenceId") == null ? null : String.valueOf(m.get("evidenceId")));
        out.setEvidenceType(m.get("evidenceType") == null ? null : String.valueOf(m.get("evidenceType")));
        out.setTitle(m.get("title") == null ? null : String.valueOf(m.get("title")));
        out.setDescription(m.get("description") == null ? null : String.valueOf(m.get("description")));
        Object refs = m.get("eventRefs");
        if (refs instanceof List<?> rl) {
            List<Long> ids = new ArrayList<>();
            for (Object o : rl) {
                if (o == null) continue;
                try { ids.add(Long.valueOf(String.valueOf(o))); } catch (Exception ignored) {}
            }
            out.setEventRefs(ids);
        }
        out.setLink(m.get("link") == null ? null : String.valueOf(m.get("link")));
        // occurredAt：不强制解析，保持 null（前端仍可用）
        return out;
    }

    @SuppressWarnings("unchecked")
    private BehaviorSummaryResponse.NonEvaluativeItem parseNonEvaluativeItem(Map<String, Object> m) {
        if (m == null) return null;
        BehaviorSummaryResponse.NonEvaluativeItem out = new BehaviorSummaryResponse.NonEvaluativeItem();
        out.setEventType(m.get("eventType") == null ? null : String.valueOf(m.get("eventType")));
        out.setCount(toInt(m.get("count")));
        Object meta = m.get("meta");
        if (meta instanceof Map<?, ?> mm) {
            out.setMeta((Map<String, Object>) mm);
        }
        return out;
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        try { return Integer.valueOf(String.valueOf(v)); } catch (Exception ignored) { return null; }
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        try { return Double.valueOf(String.valueOf(v)); } catch (Exception ignored) { return null; }
    }
}

