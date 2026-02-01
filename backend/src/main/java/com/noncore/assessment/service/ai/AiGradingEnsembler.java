package com.noncore.assessment.service.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * AI 批改结果稳定化（多次取样聚合）工具类。
 *
 * <p>规则（与你确认后的版本）：</p>
 * <ul>
 *   <li>默认取样 2 次，取均值。</li>
 *   <li>若两次 overall.final_score 分差 &gt; diffThreshold（默认 0.8），并且存在第 3 次结果，则：
 *       取第 3 次与前两次更接近的那一次构成“最近对”，再取均值。</li>
 *   <li>解释合并：对最终用于平均的“那一对”做 evidence/suggestions 去重合并，并生成确定性的 holistic_feedback。</li>
 * </ul>
 */
public final class AiGradingEnsembler {

    private AiGradingEnsembler() {}

    /**
     * 对已归一化的多次结果执行稳定聚合，输出最终标准结构，并附加 meta.ensemble（不影响现有前端渲染）。
     *
     * @param runs 已归一化结果（至少 1 条，推荐 2~3 条）
     * @param samplesRequested 调用方请求的取样数（用于 meta）
     * @param diffThreshold 触发第 3 次的分差阈值（0~5 标尺）
     * @return 聚合后的标准结构 Map
     */
    public static Map<String, Object> ensemble(List<Map<String, Object>> runs, int samplesRequested, double diffThreshold) {
        if (runs == null || runs.isEmpty()) {
            Map<String, Object> empty = new HashMap<>(AiGradingNormalizer.normalize(Map.of()));
            attachMeta(empty, samplesRequested, diffThreshold, List.of(), "empty", new int[]{1, 1}, 0.0);
            return empty;
        }
        if (runs.size() == 1) {
            Map<String, Object> out = AiGradingNormalizer.normalize(runs.get(0));
            attachMeta(out, samplesRequested, diffThreshold, scoreRuns(runs), "single", new int[]{1, 1}, 0.0);
            return out;
        }

        // 统一提取分数（0~5）
        List<Double> scores = scoreRuns(runs);
        double s1 = scores.get(0);
        double s2 = scores.get(1);
        double diff12 = Math.abs(s1 - s2);

        // 选择用于平均的 pair（下标为 0-based）
        int a = 0, b = 1;
        String method = "mean2";
        if (runs.size() >= 3 && diff12 > diffThreshold) {
            double s3 = scores.get(2);
            double d31 = Math.abs(s3 - s1);
            double d32 = Math.abs(s3 - s2);
            if (d31 <= d32) {
                a = 0; b = 2;
            } else {
                a = 1; b = 2;
            }
            method = "closest2of3";
        }

        Map<String, Object> left = AiGradingNormalizer.normalize(runs.get(a));
        Map<String, Object> right = AiGradingNormalizer.normalize(runs.get(b));
        Map<String, Object> merged = mergePair(left, right);
        attachMeta(merged, samplesRequested, diffThreshold, scores, method, new int[]{a + 1, b + 1}, Math.abs(scores.get(a) - scores.get(b)));
        return merged;
    }

    // -------------------- pair merge --------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergePair(Map<String, Object> a, Map<String, Object> b) {
        Map<String, Object> out = new HashMap<>();
        // 维度与子项
        mergeGroup(out, a, b, "moral_reasoning", List.of("stage_level", "foundations_balance", "argument_chain"));
        mergeGroup(out, a, b, "attitude_development", List.of("emotional_engagement", "resilience", "focus_flow"));
        mergeGroup(out, a, b, "ability_growth", List.of("blooms_level", "metacognition", "transfer"));
        mergeGroup(out, a, b, "strategy_optimization", List.of("diversity", "depth", "self_regulation"));

        // overall：重新计算，避免不一致
        Map<String, Object> overall = new HashMap<>();
        Map<String, Object> dimAvg = new HashMap<>();
        double mrAvg = avgGroupScores((Map<String, Object>) out.get("moral_reasoning"), List.of("stage_level", "foundations_balance", "argument_chain"));
        double adAvg = avgGroupScores((Map<String, Object>) out.get("attitude_development"), List.of("emotional_engagement", "resilience", "focus_flow"));
        double agAvg = avgGroupScores((Map<String, Object>) out.get("ability_growth"), List.of("blooms_level", "metacognition", "transfer"));
        double soAvg = avgGroupScores((Map<String, Object>) out.get("strategy_optimization"), List.of("diversity", "depth", "self_regulation"));
        dimAvg.put("moral_reasoning", round1(mrAvg));
        dimAvg.put("attitude", round1(adAvg));
        dimAvg.put("ability", round1(agAvg));
        dimAvg.put("strategy", round1(soAvg));
        double finalScore = round1(avg(List.of(mrAvg, adAvg, agAvg, soAvg)));
        overall.put("dimension_averages", dimAvg);
        overall.put("final_score", finalScore);

        // 生成确定性 holistic_feedback（避免拼接两次 LLM 文本导致继续抖动）
        overall.put("holistic_feedback", buildHolisticFeedback(out, dimAvg, finalScore));
        out.put("overall", overall);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void mergeGroup(Map<String, Object> out, Map<String, Object> a, Map<String, Object> b, String groupKey, List<String> subKeys) {
        Map<String, Object> ga = asMap(a.get(groupKey));
        Map<String, Object> gb = asMap(b.get(groupKey));
        Map<String, Object> gOut = new HashMap<>();
        for (String sk : subKeys) {
            Map<String, Object> sa = asMap(ga.get(sk));
            Map<String, Object> sb = asMap(gb.get(sk));
            gOut.put(sk, mergeSection(sa, sb));
        }
        out.put(groupKey, gOut);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeSection(Map<String, Object> a, Map<String, Object> b) {
        double s1 = clamp05(toDouble(a.get("score")));
        double s2 = clamp05(toDouble(b.get("score")));
        double score = round1((s1 + s2) / 2.0);

        List<Map<String, Object>> ev = mergeEvidence(asEvidence(a.get("evidence")), asEvidence(b.get("evidence")), 3);
        List<String> sug = mergeSuggestions(asStrings(a.get("suggestions")), asStrings(b.get("suggestions")), 5);

        Map<String, Object> out = new HashMap<>();
        out.put("score", score);
        out.put("evidence", ev);
        out.put("suggestions", sug);
        return out;
    }

    // -------------------- evidence/suggestions merge --------------------

    private static List<Map<String, Object>> mergeEvidence(List<Map<String, Object>> a, List<Map<String, Object>> b, int limit) {
        Map<String, Map<String, Object>> byQuote = new HashMap<>();
        mergeEvidenceInto(byQuote, a);
        mergeEvidenceInto(byQuote, b);

        List<Map<String, Object>> out = new ArrayList<>(byQuote.values());
        out.sort(Comparator.comparingInt(o -> -String.valueOf(o.getOrDefault("quote", "")).length()));
        if (limit > 0 && out.size() > limit) return out.subList(0, limit);
        return out;
    }

    private static void mergeEvidenceInto(Map<String, Map<String, Object>> byQuote, List<Map<String, Object>> list) {
        if (list == null) return;
        for (Map<String, Object> e : list) {
            if (e == null) continue;
            String quote = String.valueOf(e.getOrDefault("quote", "")).trim();
            String key = normalizeTextKey(quote);
            if (key.isEmpty()) key = normalizeTextKey(String.valueOf(e));

            Map<String, Object> existing = byQuote.get(key);
            if (existing == null) {
                byQuote.put(key, new HashMap<>(e));
                continue;
            }
            // 合并缺失字段：优先保留更“完整”的内容
            fillIfBlank(existing, "quote", quote);
            fillIfBlank(existing, "reasoning", String.valueOf(e.getOrDefault("reasoning", "")));
            fillIfBlank(existing, "conclusion", String.valueOf(e.getOrDefault("conclusion", "")));
            fillIfBlank(existing, "explanation", String.valueOf(e.getOrDefault("explanation", "")));
        }
    }

    private static List<String> mergeSuggestions(List<String> a, List<String> b, int limit) {
        Set<String> seen = new LinkedHashSet<>();
        addSuggestions(seen, a);
        addSuggestions(seen, b);
        List<String> out = new ArrayList<>(seen);
        if (limit > 0 && out.size() > limit) return out.subList(0, limit);
        return out;
    }

    private static void addSuggestions(Set<String> seen, List<String> list) {
        if (list == null) return;
        for (String s : list) {
            String v = String.valueOf(s == null ? "" : s).trim();
            if (v.isEmpty()) continue;
            // 以“弱规范化”去重，避免大小写/空格差异导致重复
            String key = normalizeTextKey(v);
            if (!key.isEmpty()) seen.add(v);
        }
    }

    // -------------------- meta.ensemble --------------------

    @SuppressWarnings("unchecked")
    private static void attachMeta(Map<String, Object> out, int samplesRequested, double diffThreshold, List<Double> scores, String method, int[] chosenPair, double pairDiff) {
        if (out == null) return;
        Map<String, Object> meta;
        Object mv = out.get("meta");
        if (mv instanceof Map<?, ?> m) {
            meta = (Map<String, Object>) m;
        } else {
            meta = new HashMap<>();
            out.put("meta", meta);
        }

        Map<String, Object> ensemble = new HashMap<>();
        ensemble.put("samplesRequested", samplesRequested);
        ensemble.put("diffThreshold", round2(diffThreshold));
        ensemble.put("triggeredThird", scores != null && scores.size() >= 3);
        ensemble.put("method", method);
        ensemble.put("chosenPair", List.of(chosenPair[0], chosenPair[1]));
        ensemble.put("pairDiff", round2(pairDiff));

        List<Map<String, Object>> runs = new ArrayList<>();
        if (scores != null) {
            for (int i = 0; i < scores.size(); i++) {
                runs.add(Map.of("index", i + 1, "finalScore", round2(scores.get(i))));
            }
        }
        ensemble.put("runs", runs);

        double confidence = clamp01(1.0 - (pairDiff / 5.0));
        ensemble.put("confidence", round2(confidence));
        meta.put("ensemble", ensemble);
    }

    private static List<Double> scoreRuns(List<Map<String, Object>> runs) {
        List<Double> out = new ArrayList<>();
        for (Map<String, Object> r : runs) {
            double s = AiGradingNormalizer.extractFinalScore05(r);
            out.add(s);
        }
        return out;
    }

    // -------------------- holistic feedback --------------------

    @SuppressWarnings("unchecked")
    private static String buildHolisticFeedback(Map<String, Object> out, Map<String, Object> dimAvg, double finalScore) {
        String avgLine = "Averages — Moral: " + dimAvg.getOrDefault("moral_reasoning", 0)
                + ", Attitude: " + dimAvg.getOrDefault("attitude", 0)
                + ", Ability: " + dimAvg.getOrDefault("ability", 0)
                + ", Strategy: " + dimAvg.getOrDefault("strategy", 0)
                + ". Final: " + round1(finalScore) + "/5.";

        List<String> all = new ArrayList<>();
        collectSuggestions(all, asMap(out.get("moral_reasoning")));
        collectSuggestions(all, asMap(out.get("attitude_development")));
        collectSuggestions(all, asMap(out.get("ability_growth")));
        collectSuggestions(all, asMap(out.get("strategy_optimization")));
        if (all.isEmpty()) return avgLine;

        StringBuilder sb = new StringBuilder();
        sb.append(avgLine).append('\n');
        sb.append("Key suggestions:").append('\n');
        int count = 0;
        for (String s : all) {
            if (count >= 6) break;
            sb.append("- ").append(s).append('\n');
            count++;
        }
        return sb.toString().trim();
    }

    private static void collectSuggestions(List<String> out, Map<String, Object> group) {
        if (group == null || group.isEmpty()) return;
        for (Object secObj : group.values()) {
            if (!(secObj instanceof Map<?, ?> sm)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> sec = (Map<String, Object>) sm;
            List<String> arr = asStrings(sec.get("suggestions"));
            for (String s : arr) {
                String v = String.valueOf(s == null ? "" : s).trim();
                if (!v.isEmpty()) out.add(v);
                if (out.size() >= 12) return;
            }
        }
    }

    // -------------------- misc helpers --------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object v) {
        if (v instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asEvidence(Object v) {
        if (v instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) out.add(new HashMap<>((Map<String, Object>) m));
            }
            return out;
        }
        return new ArrayList<>();
    }

    private static List<String> asStrings(Object v) {
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) out.add(s);
            }
            return out;
        }
        return new ArrayList<>();
    }

    private static double avgGroupScores(Map<String, Object> group, List<String> subKeys) {
        if (group == null) return 0.0;
        List<Double> nums = new ArrayList<>();
        for (String k : subKeys) {
            Object sec = group.get(k);
            if (sec instanceof Map<?, ?> sm) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) sm;
                nums.add(clamp05(toDouble(m.get("score"))));
            }
        }
        return avg(nums);
    }

    private static double avg(List<Double> nums) {
        if (nums == null || nums.isEmpty()) return 0.0;
        double sum = 0.0;
        int c = 0;
        for (Double d : nums) {
            if (d == null || !Double.isFinite(d)) continue;
            sum += d;
            c++;
        }
        return c == 0 ? 0.0 : (sum / c);
    }

    private static double toDouble(Object v) {
        if (v == null) return 0.0;
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception ignored) { return 0.0; }
    }

    private static double clamp05(double v) {
        if (v < 0) return 0.0;
        if (v > 5) return 5.0;
        return v;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0.0;
        if (v > 1) return 1.0;
        return v;
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    private static String normalizeTextKey(String s) {
        String v = String.valueOf(s == null ? "" : s).trim().toLowerCase(Locale.ROOT);
        v = v.replaceAll("\\s+", " ");
        return v;
    }

    private static void fillIfBlank(Map<String, Object> target, String key, String value) {
        if (target == null) return;
        Object existing = target.get(key);
        String ev = existing == null ? "" : String.valueOf(existing).trim();
        String nv = value == null ? "" : value.trim();
        if (ev.isEmpty() && !nv.isEmpty()) {
            target.put(key, nv);
        }
    }
}

