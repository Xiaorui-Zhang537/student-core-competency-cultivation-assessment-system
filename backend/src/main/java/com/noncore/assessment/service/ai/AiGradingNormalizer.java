package com.noncore.assessment.service.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 批改结果归一化工具类。
 *
 * <p>说明：前端当前已兼容多种模型输出结构（evaluation_result / evaluation / 标准结构）。
 * 为了让“多次取样稳定算法”在后端执行时不依赖前端容错，这里在后端做一次最小但稳定的归一化：
 * 输出固定为标准结构（root: moral_reasoning/attitude_development/ability_growth/strategy_optimization/overall）。</p>
 */
public final class AiGradingNormalizer {

    private AiGradingNormalizer() {}

    /**
     * 将模型返回的任意 JSON（已解析为 Map）归一化为标准结构。
     *
     * @param raw LLM 返回 JSON 解析结果
     * @return 归一化后的结构（永不返回 null）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> normalize(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return emptyStandard();

        Map<String, Object> unwrapped = unwrapCommonContainer(raw);
        if (unwrapped != raw) {
            return normalize(unwrapped);
        }

        // 1) 已是标准结构：包含 overall 或四个维度任意一个
        if (looksLikeStandard(raw)) {
            return normalizeStandardLike(raw);
        }

        // 2) evaluation_result 数组结构（新标准）
        Object er = raw.get("evaluation_result");
        if (er instanceof List<?> list) {
            return normalizeFromEvaluationArray((List<Object>) list);
        }

        // 3) evaluation 对象结构（兼容旧模型输出）
        Object eval = raw.get("evaluation");
        if (eval instanceof Map<?, ?> m) {
            return normalizeFromEvaluationObject((Map<String, Object>) m);
        }

        Map<String, Object> loose = normalizeFromLooseDimensionMap(raw);
        if (isRenderable(loose)) {
            return loose;
        }

        // 兜底：返回空标准结构，避免 NPE
        return emptyStandard();
    }

    /**
     * 从标准结构中提取 overall.final_score（0~5）。若缺失则根据四维均分计算。
     *
     * @param normalized 已归一化的结构
     * @return 0~5 的分数（double）
     */
    @SuppressWarnings("unchecked")
    public static double extractFinalScore05(Map<String, Object> normalized) {
        if (normalized == null) return 0.0;
        Object ov = normalized.get("overall");
        if (ov instanceof Map<?, ?> ovm) {
            Object fs = ((Map<String, Object>) ovm).get("final_score");
            Double d = toDouble(fs);
            if (d != null) return clamp05(d);
        }
        // fallback: 由 dimension_averages 计算
        Map<String, Object> ensured = deepCopy(normalized);
        ensureOverall(ensured);
        Object ov2 = ensured.get("overall");
        if (ov2 instanceof Map<?, ?> ovm2) {
            Double d = toDouble(((Map<String, Object>) ovm2).get("final_score"));
            return d == null ? 0.0 : clamp05(d);
        }
        return 0.0;
    }

    /**
     * 判断归一化结果是否足以被前端报告正常展示。
     * <p>当前评分 Prompt 要求 1~5 分；归一化为空壳时 final_score 会是 0，不能当作有效报告。</p>
     */
    public static boolean isRenderable(Map<String, Object> normalized) {
        if (normalized == null || normalized.isEmpty()) return false;
        double finalScore = extractFinalScore05(normalized);
        if (!(finalScore > 0.0)) return false;
        return hasAnySection(normalized, "moral_reasoning")
                || hasAnySection(normalized, "attitude_development")
                || hasAnySection(normalized, "ability_growth")
                || hasAnySection(normalized, "strategy_optimization");
    }

    // -------------------- normalization implementations --------------------

    private static Map<String, Object> normalizeFromEvaluationArray(List<Object> arr) {
        Map<String, Object> out = emptyStandard();
        @SuppressWarnings("unchecked")
        Map<String, Object> mr = (Map<String, Object>) out.get("moral_reasoning");
        @SuppressWarnings("unchecked")
        Map<String, Object> ad = (Map<String, Object>) out.get("attitude_development");
        @SuppressWarnings("unchecked")
        Map<String, Object> ag = (Map<String, Object>) out.get("ability_growth");
        @SuppressWarnings("unchecked")
        Map<String, Object> so = (Map<String, Object>) out.get("strategy_optimization");

        for (Object g : arr) {
            if (!(g instanceof Map<?, ?> gm)) continue;
            Object dimObj = gm.get("dimension");
            String dim = dimObj == null ? "" : String.valueOf(dimObj);
            String dimKey = mapDimension(dim);
            if (dimKey.isEmpty() && gm.get("id") != null) {
                dimKey = mapDimension(String.valueOf(gm.get("id")));
            }
            if (dimKey.isEmpty()) continue;
            Object subs = firstValue(gm, "sub_criteria", "subCriteria", "criteria", "items", "children", "details");
            if (!(subs instanceof List<?> subList)) continue;
            for (Object it : subList) {
                if (!(it instanceof Map<?, ?> im)) continue;
                Object idObj = im.get("id");
                String id = idObj == null ? String.valueOf(firstValue(im, "criterion", "name", "title", "label")) : String.valueOf(idObj);
                String secKey = mapSubCriterion(dimKey, id);
                if (secKey.isEmpty()) continue;
                Map<String, Object> sec = toSection(im);
                if ("moral_reasoning".equals(dimKey)) mr.put(secKey, sec);
                if ("attitude_development".equals(dimKey)) ad.put(secKey, sec);
                if ("ability_growth".equals(dimKey)) ag.put(secKey, sec);
                if ("strategy_optimization".equals(dimKey)) so.put(secKey, sec);
            }
        }
        ensureOverall(out);
        return out;
    }

    /**
     * 兼容旧 evaluation 对象结构（如分组标题为 “1) ...”）。
     * 这里做“能解析就解析”的宽松映射，解析不到的子项置空默认值。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeFromEvaluationObject(Map<String, Object> evaluation) {
        Map<String, Object> out = emptyStandard();
        Map<String, Object> moral = pickGroup(evaluation, "1", "moral", "道德", "推理");
        Map<String, Object> attitude = pickGroup(evaluation, "2", "attitude", "态度");
        Map<String, Object> ability = pickGroup(evaluation, "3", "ability", "能力");
        Map<String, Object> strategy = pickGroup(evaluation, "4", "strategy", "策略");

        putSec(out, "moral_reasoning", "stage_level", pickSub(moral, "1a", "stage", "level", "阶段", "水平"));
        putSec(out, "moral_reasoning", "foundations_balance", pickSub(moral, "1b", "foundation", "基础", "广度"));
        putSec(out, "moral_reasoning", "argument_chain", pickSub(moral, "1c", "argument", "counter", "论证", "反驳"));

        putSec(out, "attitude_development", "emotional_engagement", pickSub(attitude, "2a", "emotional", "engagement", "情感", "投入"));
        putSec(out, "attitude_development", "resilience", pickSub(attitude, "2b", "resilience", "persistence", "坚持", "韧性"));
        putSec(out, "attitude_development", "focus_flow", pickSub(attitude, "2c", "focus", "flow", "专注", "流畅"));

        putSec(out, "ability_growth", "blooms_level", pickSub(ability, "3a", "bloom", "taxonomy", "布鲁姆", "层级"));
        putSec(out, "ability_growth", "metacognition", pickSub(ability, "3b", "metacognition", "元认知", "反思"));
        putSec(out, "ability_growth", "transfer", pickSub(ability, "3c", "transfer", "迁移", "应用"));

        putSec(out, "strategy_optimization", "diversity", pickSub(strategy, "4a", "diversity", "多样"));
        putSec(out, "strategy_optimization", "depth", pickSub(strategy, "4b", "depth", "深度"));
        putSec(out, "strategy_optimization", "self_regulation", pickSub(strategy, "4c", "regulation", "self", "自我调节"));

        fillDirectGroupFallback(out, "moral_reasoning", moral, List.of("stage_level", "foundations_balance", "argument_chain"));
        fillDirectGroupFallback(out, "attitude_development", attitude, List.of("emotional_engagement", "resilience", "focus_flow"));
        fillDirectGroupFallback(out, "ability_growth", ability, List.of("blooms_level", "metacognition", "transfer"));
        fillDirectGroupFallback(out, "strategy_optimization", strategy, List.of("diversity", "depth", "self_regulation"));

        ensureOverall(out);
        return out;
    }

    // -------------------- helpers --------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> unwrapCommonContainer(Map<String, Object> raw) {
        for (String key : List.of("result", "data", "report", "assessment", "grading", "output")) {
            Object v = raw.get(key);
            if (v instanceof Map<?, ?> m) {
                return (Map<String, Object>) m;
            }
        }
        return raw;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeStandardLike(Map<String, Object> raw) {
        Map<String, Object> out = emptyStandard();
        copyStandardGroup(out, raw, "moral_reasoning", List.of("moral_reasoning", "moral", "道德推理", "道德"));
        copyStandardGroup(out, raw, "attitude_development", List.of("attitude_development", "attitude", "learning_attitude", "学习态度", "态度"));
        copyStandardGroup(out, raw, "ability_growth", List.of("ability_growth", "ability", "learning_ability", "能力成长", "能力"));
        copyStandardGroup(out, raw, "strategy_optimization", List.of("strategy_optimization", "strategy", "learning_strategy", "策略优化", "策略"));
        Object overall = raw.get("overall");
        if (overall instanceof Map<?, ?> m) {
            out.put("overall", deepCopy((Map<String, Object>) m));
        }
        ensureOverall(out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void copyStandardGroup(Map<String, Object> out, Map<String, Object> raw, String targetKey, List<String> aliases) {
        Object groupObj = null;
        for (String alias : aliases) {
            if (raw.containsKey(alias)) {
                groupObj = raw.get(alias);
                break;
            }
        }
        if (!(groupObj instanceof Map<?, ?> gm)) return;
        Map<String, Object> group = (Map<String, Object>) gm;
        Object outGroupObj = out.get(targetKey);
        if (!(outGroupObj instanceof Map<?, ?> ogm)) return;
        Map<String, Object> outGroup = (Map<String, Object>) ogm;
        List<String> subKeys = subKeysFor(targetKey);
        boolean direct = isSectionLike(group);
        if (direct) {
            Map<String, Object> sec = toSection(group);
            for (String subKey : subKeys) outGroup.put(subKey, new HashMap<>(sec));
            return;
        }
        for (Map.Entry<String, Object> e : group.entrySet()) {
            String subKey = subKeys.contains(e.getKey()) ? e.getKey() : mapSubCriterion(targetKey, e.getKey());
            if (subKey.isEmpty() || !(e.getValue() instanceof Map<?, ?> sm)) continue;
            outGroup.put(subKey, toSection(sm));
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeFromLooseDimensionMap(Map<String, Object> raw) {
        Map<String, Object> out = emptyStandard();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            String dimKey = mapDimension(e.getKey());
            if (dimKey.isEmpty() || !(e.getValue() instanceof Map<?, ?> gm)) continue;
            Map<String, Object> group = (Map<String, Object>) gm;
            Object outGroupObj = out.get(dimKey);
            if (!(outGroupObj instanceof Map<?, ?> ogm)) continue;
            Map<String, Object> outGroup = (Map<String, Object>) ogm;
            if (isSectionLike(group)) {
                Map<String, Object> sec = toSection(group);
                for (String subKey : subKeysFor(dimKey)) outGroup.put(subKey, new HashMap<>(sec));
                continue;
            }
            for (Map.Entry<String, Object> child : group.entrySet()) {
                String subKey = mapSubCriterion(dimKey, child.getKey());
                if (subKey.isEmpty() || !(child.getValue() instanceof Map<?, ?> sm)) continue;
                outGroup.put(subKey, toSection(sm));
            }
        }
        ensureOverall(out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void putSec(Map<String, Object> out, String dimKey, String secKey, Map<String, Object> raw) {
        Map<String, Object> sec = raw == null ? defaultSection() : toSection(raw);
        Object grp = out.get(dimKey);
        if (grp instanceof Map<?, ?> g) {
            ((Map<String, Object>) g).put(secKey, sec);
        }
    }

    private static Map<String, Object> pickGroup(Map<String, Object> evaluation, String... hints) {
        if (evaluation == null) return null;
        for (Map.Entry<String, Object> e : evaluation.entrySet()) {
            if (e.getKey() == null) continue;
            if (containsAny(e.getKey(), hints) || startsWithLoose(e.getKey(), hints)) {
                if (e.getValue() instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mm = (Map<String, Object>) m;
                    return mm;
                }
            }
        }
        return null;
    }

    private static Map<String, Object> pickSub(Map<String, Object> group, String idPrefix, String... keywords) {
        if (group == null) return null;
        String idp = normalizeKey(idPrefix);
        for (Map.Entry<String, Object> e : group.entrySet()) {
            String nk = normalizeKey(e.getKey());
            if (!nk.isEmpty() && (nk.startsWith(idp) || containsAny(e.getKey(), keywords))) {
                if (e.getValue() instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mm = (Map<String, Object>) m;
                    return mm;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void fillDirectGroupFallback(Map<String, Object> out, String dimKey, Map<String, Object> group, List<String> subKeys) {
        if (group == null || !isSectionLike(group)) return;
        Object outGroupObj = out.get(dimKey);
        if (!(outGroupObj instanceof Map<?, ?> ogm)) return;
        Map<String, Object> outGroup = (Map<String, Object>) ogm;
        Map<String, Object> sec = toSection(group);
        for (String subKey : subKeys) {
            Object existing = outGroup.get(subKey);
            if (!(existing instanceof Map<?, ?> em) || !hasPositiveScore((Map<?, ?>) em)) {
                outGroup.put(subKey, new HashMap<>(sec));
            }
        }
    }

    private static String normalizeKey(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean containsAny(String value, String... hints) {
        if (value == null || hints == null) return false;
        String lower = value.toLowerCase(Locale.ROOT);
        String norm = normalizeKey(value);
        for (String hint : hints) {
            if (hint == null || hint.isBlank()) continue;
            String h = hint.toLowerCase(Locale.ROOT);
            String hn = normalizeKey(hint);
            if (lower.contains(h) || (!hn.isEmpty() && norm.contains(hn))) return true;
        }
        return false;
    }

    private static boolean startsWithLoose(String value, String... hints) {
        if (value == null || hints == null) return false;
        String norm = normalizeKey(value);
        for (String hint : hints) {
            String hn = normalizeKey(hint);
            if (!hn.isEmpty() && norm.startsWith(hn)) return true;
        }
        return false;
    }

    private static boolean isSectionLike(Map<?, ?> map) {
        if (map == null) return false;
        return firstValue(map, "score", "score_value", "scoreValue", "rating", "level", "points") != null
                || firstValue(map, "evidence", "reasoning", "analysis", "feedback", "comment") != null
                || firstValue(map, "suggestions", "suggestion", "recommendations", "improvements") != null;
    }

    private static boolean hasPositiveScore(Map<?, ?> map) {
        Double d = toDouble(firstValue(map, "score", "score_value", "scoreValue", "rating", "level", "points"));
        return d != null && d > 0;
    }

    private static List<String> subKeysFor(String dimKey) {
        return switch (dimKey) {
            case "moral_reasoning" -> List.of("stage_level", "foundations_balance", "argument_chain");
            case "attitude_development" -> List.of("emotional_engagement", "resilience", "focus_flow");
            case "ability_growth" -> List.of("blooms_level", "metacognition", "transfer");
            case "strategy_optimization" -> List.of("diversity", "depth", "self_regulation");
            default -> List.of();
        };
    }

    private static boolean looksLikeStandard(Map<String, Object> obj) {
        if (obj.containsKey("overall")) return true;
        return obj.containsKey("moral_reasoning")
                || obj.containsKey("attitude_development")
                || obj.containsKey("ability_growth")
                || obj.containsKey("strategy_optimization");
    }

    private static boolean hasAnySection(Map<String, Object> obj, String groupKey) {
        Object group = obj.get(groupKey);
        if (!(group instanceof Map<?, ?> gm) || gm.isEmpty()) return false;
        for (Object value : gm.values()) {
            if (value instanceof Map<?, ?> section && !section.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Object> emptyStandard() {
        Map<String, Object> out = new HashMap<>();
        out.put("moral_reasoning", new HashMap<String, Object>());
        out.put("attitude_development", new HashMap<String, Object>());
        out.put("ability_growth", new HashMap<String, Object>());
        out.put("strategy_optimization", new HashMap<String, Object>());
        out.put("overall", new HashMap<String, Object>());
        ensureOverall(out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void ensureOverall(Map<String, Object> out) {
        if (out == null) return;
        Map<String, Object> overall;
        Object ov = out.get("overall");
        if (ov instanceof Map<?, ?> m) {
            overall = (Map<String, Object>) m;
        } else {
            overall = new HashMap<>();
            out.put("overall", overall);
        }

        // dimension_averages
        Map<String, Object> dimAvg;
        Object da = overall.get("dimension_averages");
        if (da instanceof Map<?, ?> dam) {
            dimAvg = (Map<String, Object>) dam;
        } else {
            dimAvg = new HashMap<>();
            overall.put("dimension_averages", dimAvg);
        }

        double mrAvg = avg3(out, "moral_reasoning", List.of("stage_level", "foundations_balance", "argument_chain"));
        double adAvg = avg3(out, "attitude_development", List.of("emotional_engagement", "resilience", "focus_flow"));
        double agAvg = avg3(out, "ability_growth", List.of("blooms_level", "metacognition", "transfer"));
        double soAvg = avg3(out, "strategy_optimization", List.of("diversity", "depth", "self_regulation"));
        dimAvg.put("moral_reasoning", round1(mrAvg));
        dimAvg.put("attitude", round1(adAvg));
        dimAvg.put("ability", round1(agAvg));
        dimAvg.put("strategy", round1(soAvg));
        double finalScore = round1(avg(List.of(mrAvg, adAvg, agAvg, soAvg)));
        overall.put("final_score", finalScore);
        Object feedback = overall.get("holistic_feedback");
        if (feedback == null || String.valueOf(feedback).trim().isEmpty()) {
            overall.put("holistic_feedback", buildHolisticFeedback(out, dimAvg, finalScore));
        }
    }

    @SuppressWarnings("unchecked")
    private static String buildHolisticFeedback(Map<String, Object> out, Map<String, Object> dimAvg, double finalScore) {
        StringBuilder sb = new StringBuilder();
        sb.append("总体评分：").append(round1(finalScore)).append("/5。");
        sb.append("维度均分：道德推理 ").append(dimAvg.getOrDefault("moral_reasoning", 0));
        sb.append("，学习态度 ").append(dimAvg.getOrDefault("attitude", 0));
        sb.append("，能力成长 ").append(dimAvg.getOrDefault("ability", 0));
        sb.append("，策略优化 ").append(dimAvg.getOrDefault("strategy", 0)).append("。");

        List<String> suggestions = new ArrayList<>();
        collectSuggestions(suggestions, out.get("moral_reasoning"));
        collectSuggestions(suggestions, out.get("attitude_development"));
        collectSuggestions(suggestions, out.get("ability_growth"));
        collectSuggestions(suggestions, out.get("strategy_optimization"));
        if (!suggestions.isEmpty()) {
            sb.append("\n关键建议:");
            int count = 0;
            for (String s : suggestions) {
                if (count >= 6) break;
                sb.append("\n- ").append(s);
                count++;
            }
            return sb.toString();
        }

        List<String> observations = new ArrayList<>();
        collectEvidenceSummaries(observations, out.get("moral_reasoning"));
        collectEvidenceSummaries(observations, out.get("attitude_development"));
        collectEvidenceSummaries(observations, out.get("ability_growth"));
        collectEvidenceSummaries(observations, out.get("strategy_optimization"));
        if (!observations.isEmpty()) {
            sb.append("\n观察要点:");
            int count = 0;
            for (String s : observations) {
                if (count >= 4) break;
                sb.append("\n- ").append(s);
                count++;
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void collectSuggestions(List<String> out, Object groupObj) {
        if (!(groupObj instanceof Map<?, ?> group)) return;
        for (Object sectionObj : group.values()) {
            if (!(sectionObj instanceof Map<?, ?> section)) continue;
            Object raw = section.get("suggestions");
            if (raw instanceof List<?> list) {
                for (Object item : list) {
                    String s = String.valueOf(item == null ? "" : item).trim();
                    if (!s.isEmpty()) out.add(s);
                    if (out.size() >= 12) return;
                }
            } else if (raw != null) {
                String s = String.valueOf(raw).trim();
                if (!s.isEmpty()) out.add(s);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void collectEvidenceSummaries(List<String> out, Object groupObj) {
        if (!(groupObj instanceof Map<?, ?> group)) return;
        for (Object sectionObj : group.values()) {
            if (!(sectionObj instanceof Map<?, ?> section)) continue;
            Object raw = section.get("evidence");
            if (!(raw instanceof List<?> list)) continue;
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> ev)) continue;
                Object reasoningObj = ev.get("reasoning");
                Object conclusionObj = ev.get("conclusion");
                String reasoning = String.valueOf(reasoningObj == null ? "" : reasoningObj).trim();
                String conclusion = String.valueOf(conclusionObj == null ? "" : conclusionObj).trim();
                String text = !reasoning.isEmpty() ? reasoning : conclusion;
                if (!text.isEmpty()) out.add(text);
                if (out.size() >= 8) return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static double avg3(Map<String, Object> out, String groupKey, List<String> subKeys) {
        Object grp = out.get(groupKey);
        if (!(grp instanceof Map<?, ?> gm)) return 0.0;
        Map<String, Object> group = (Map<String, Object>) gm;
        List<Double> nums = new ArrayList<>();
        for (String k : subKeys) {
            Object sec = group.get(k);
            if (sec instanceof Map<?, ?> sm) {
                Double d = toDouble(((Map<String, Object>) sm).get("score"));
                if (d != null) nums.add(d);
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
        if (c == 0) return 0.0;
        return sum / c;
    }

    private static Map<String, Object> toSection(Map<?, ?> raw) {
        Map<String, Object> sec = new HashMap<>();
        Double score = toDouble(firstValue(raw, "score", "score_value", "scoreValue", "rating", "level", "points"));
        sec.put("score", round1(clamp05(score == null ? 0.0 : score)));
        sec.put("evidence", toEvidence(firstValue(raw, "evidence", "reasoning", "analysis", "feedback", "comment", "comments")));
        sec.put("suggestions", toSuggestions(firstValue(raw, "suggestions", "suggestion", "recommendations", "recommendation", "improvements", "improvement")));
        return sec;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> toEvidence(Object rawEv) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (rawEv instanceof Map<?, ?> evm) {
            // 兼容 {quotes:[...], reasoning, conclusion, explanations?}
            Object quotes = evm.get("quotes");
            if (quotes instanceof List<?> ql) {
                Object exps = evm.get("explanations");
                List<?> xl = exps instanceof List<?> ? (List<?>) exps : null;
                for (int i = 0; i < ql.size(); i++) {
                    String q = String.valueOf(ql.get(i));
                    Map<String, Object> e = new HashMap<>();
                    e.put("quote", q);
                    Object rr = evm.get("reasoning");
                    Object cc = evm.get("conclusion");
                    e.put("reasoning", rr == null ? "" : String.valueOf(rr));
                    e.put("conclusion", cc == null ? "" : String.valueOf(cc));
                    if (xl != null && i < xl.size() && xl.get(i) != null) {
                        e.put("explanation", String.valueOf(xl.get(i)));
                    } else if (evm.get("explanation") != null) {
                        e.put("explanation", String.valueOf(evm.get("explanation")));
                    }
                    out.add(e);
                }
            }
            return out;
        }
        if (rawEv instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mm = (Map<String, Object>) m;
                    out.add(new HashMap<>(mm));
                } else if (o != null) {
                    out.add(Map.of("quote", "", "reasoning", String.valueOf(o), "conclusion", ""));
                }
            }
            return out;
        }
        if (rawEv != null) {
            // 兜底：字符串证据
            out.add(Map.of("quote", "", "reasoning", String.valueOf(rawEv), "conclusion", ""));
        }
        return out;
    }

    private static List<String> toSuggestions(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) out.add(s);
            }
            return out;
        }
        if (raw != null) {
            String s = String.valueOf(raw).trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private static String mapDimension(String dim) {
        String s = String.valueOf(dim == null ? "" : dim).toLowerCase(Locale.ROOT);
        String n = normalizeKey(s);
        if (n.startsWith("1") || s.contains("moral") || s.contains("道德") || s.contains("推理")) return "moral_reasoning";
        if (n.startsWith("2") || s.contains("attitude") || s.contains("态度")) return "attitude_development";
        if (n.startsWith("3") || s.contains("ability") || s.contains("能力")) return "ability_growth";
        if (n.startsWith("4") || s.contains("strategy") || s.contains("策略")) return "strategy_optimization";
        return "";
    }

    private static String mapSubCriterion(String dimKey, String id) {
        String raw = String.valueOf(id == null ? "" : id);
        String i = normalizeKey(raw).toUpperCase(Locale.ROOT);
        return switch (dimKey) {
            case "moral_reasoning" -> switch (i) {
                default -> i.startsWith("1A") || containsAny(raw, "stage", "level", "阶段", "水平") ? "stage_level"
                        : i.startsWith("1B") || containsAny(raw, "foundation", "基础", "广度") ? "foundations_balance"
                        : i.startsWith("1C") || containsAny(raw, "argument", "counter", "论证", "反驳") ? "argument_chain"
                        : "";
            };
            case "attitude_development" -> switch (i) {
                default -> i.startsWith("2A") || containsAny(raw, "emotional", "engagement", "情感", "投入") ? "emotional_engagement"
                        : i.startsWith("2B") || containsAny(raw, "resilience", "persistence", "坚持", "韧性") ? "resilience"
                        : i.startsWith("2C") || containsAny(raw, "focus", "flow", "专注", "流畅") ? "focus_flow"
                        : "";
            };
            case "ability_growth" -> switch (i) {
                default -> i.startsWith("3A") || containsAny(raw, "bloom", "taxonomy", "布鲁姆", "层级") ? "blooms_level"
                        : i.startsWith("3B") || containsAny(raw, "metacognition", "元认知", "反思") ? "metacognition"
                        : i.startsWith("3C") || containsAny(raw, "transfer", "迁移", "应用") ? "transfer"
                        : "";
            };
            case "strategy_optimization" -> switch (i) {
                default -> i.startsWith("4A") || containsAny(raw, "diversity", "多样") ? "diversity"
                        : i.startsWith("4B") || containsAny(raw, "depth", "深度") ? "depth"
                        : i.startsWith("4C") || containsAny(raw, "regulation", "self", "自我调节") ? "self_regulation"
                        : "";
            };
            default -> "";
        };
    }

    private static Object firstValue(Map<?, ?> map, String... keys) {
        if (map == null || keys == null) return null;
        for (String key : keys) {
            if (map.containsKey(key)) {
                Object v = map.get(key);
                if (v != null) return v;
            }
        }
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String k = String.valueOf(e.getKey());
            for (String key : keys) {
                if (k.equalsIgnoreCase(key)) {
                    Object v = e.getValue();
                    if (v != null) return v;
                }
            }
        }
        return null;
    }

    private static Map<String, Object> defaultSection() {
        return new HashMap<>(Map.of(
                "score", 0.0,
                "evidence", new ArrayList<>(),
                "suggestions", new ArrayList<>()
        ));
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        try {
            if (v instanceof Number n) return n.doubleValue();
            String s = String.valueOf(v).trim();
            if (s.isEmpty()) return null;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                Matcher m = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(s);
                return m.find() ? Double.parseDouble(m.group()) : null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double clamp05(double v) {
        if (v < 0) return 0.0;
        if (v > 5) return 5.0;
        return v;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static Map<String, Object> deepCopy(Map<String, Object> src) {
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, Object> e : src.entrySet()) {
            out.put(e.getKey(), deepCopyValue(e.getValue()));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Object deepCopyValue(Object v) {
        if (v instanceof Map<?, ?> m) {
            Map<String, Object> mm = (Map<String, Object>) m;
            return deepCopy(mm);
        }
        if (v instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            for (Object o : list) out.add(deepCopyValue(o));
            return out;
        }
        return v;
    }
}
