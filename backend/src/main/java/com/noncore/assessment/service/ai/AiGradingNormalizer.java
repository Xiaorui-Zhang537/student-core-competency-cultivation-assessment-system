package com.noncore.assessment.service.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        // 1) 已是标准结构：包含 overall 或四个维度任意一个
        if (looksLikeStandard(raw)) {
            Map<String, Object> out = deepCopy(raw);
            ensureOverall(out);
            return out;
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
            if (dimKey.isEmpty()) continue;
            Object subs = gm.get("sub_criteria");
            if (!(subs instanceof List<?> subList)) continue;
            for (Object it : subList) {
                if (!(it instanceof Map<?, ?> im)) continue;
                Object idObj = im.get("id");
                String id = idObj == null ? "" : String.valueOf(idObj);
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
        Map<String, Object> moral = pickGroup(evaluation, "moral");
        Map<String, Object> attitude = pickGroup(evaluation, "attitude");
        Map<String, Object> ability = pickGroup(evaluation, "ability");
        Map<String, Object> strategy = pickGroup(evaluation, "strategy");

        putSec(out, "moral_reasoning", "stage_level", pickSub(moral, "1a", "stage"));
        putSec(out, "moral_reasoning", "foundations_balance", pickSub(moral, "1b", "foundation"));
        putSec(out, "moral_reasoning", "argument_chain", pickSub(moral, "1c", "argument"));

        putSec(out, "attitude_development", "emotional_engagement", pickSub(attitude, "2a", "emotional"));
        putSec(out, "attitude_development", "resilience", pickSub(attitude, "2b", "resilience"));
        putSec(out, "attitude_development", "focus_flow", pickSub(attitude, "2c", "focus"));

        putSec(out, "ability_growth", "blooms_level", pickSub(ability, "3a", "bloom"));
        putSec(out, "ability_growth", "metacognition", pickSub(ability, "3b", "metacognition"));
        putSec(out, "ability_growth", "transfer", pickSub(ability, "3c", "transfer"));

        putSec(out, "strategy_optimization", "diversity", pickSub(strategy, "4a", "diversity"));
        putSec(out, "strategy_optimization", "depth", pickSub(strategy, "4b", "depth"));
        putSec(out, "strategy_optimization", "self_regulation", pickSub(strategy, "4c", "regulation"));

        ensureOverall(out);
        return out;
    }

    // -------------------- helpers --------------------

    @SuppressWarnings("unchecked")
    private static void putSec(Map<String, Object> out, String dimKey, String secKey, Map<String, Object> raw) {
        Map<String, Object> sec = raw == null ? defaultSection() : toSection(raw);
        Object grp = out.get(dimKey);
        if (grp instanceof Map<?, ?> g) {
            ((Map<String, Object>) g).put(secKey, sec);
        }
    }

    private static Map<String, Object> pickGroup(Map<String, Object> evaluation, String hint) {
        if (evaluation == null) return null;
        for (Map.Entry<String, Object> e : evaluation.entrySet()) {
            if (e.getKey() == null) continue;
            String k = e.getKey().toLowerCase(Locale.ROOT);
            if (k.contains(hint)) {
                if (e.getValue() instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mm = (Map<String, Object>) m;
                    return mm;
                }
            }
        }
        return null;
    }

    private static Map<String, Object> pickSub(Map<String, Object> group, String idPrefix, String keyword) {
        if (group == null) return null;
        String idp = normalizeKey(idPrefix);
        String kw = normalizeKey(keyword);
        for (Map.Entry<String, Object> e : group.entrySet()) {
            String nk = normalizeKey(e.getKey());
            if (!nk.isEmpty() && (nk.startsWith(idp) || nk.contains(kw))) {
                if (e.getValue() instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mm = (Map<String, Object>) m;
                    return mm;
                }
            }
        }
        return null;
    }

    private static String normalizeKey(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean looksLikeStandard(Map<String, Object> obj) {
        if (obj.containsKey("overall")) return true;
        return obj.containsKey("moral_reasoning")
                || obj.containsKey("attitude_development")
                || obj.containsKey("ability_growth")
                || obj.containsKey("strategy_optimization");
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
        overall.put("final_score", round1(avg(List.of(mrAvg, adAvg, agAvg, soAvg))));
        if (!overall.containsKey("holistic_feedback")) {
            overall.put("holistic_feedback", "");
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
        sec.put("score", round1(clamp05(toDouble(raw.get("score")) == null ? 0.0 : toDouble(raw.get("score")))));
        sec.put("evidence", toEvidence(raw.get("evidence")));
        sec.put("suggestions", toSuggestions(raw.get("suggestions")));
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
        if (s.contains("moral")) return "moral_reasoning";
        if (s.contains("attitude")) return "attitude_development";
        if (s.contains("ability")) return "ability_growth";
        if (s.contains("strategy")) return "strategy_optimization";
        return "";
    }

    private static String mapSubCriterion(String dimKey, String id) {
        String i = String.valueOf(id == null ? "" : id).toUpperCase(Locale.ROOT);
        return switch (dimKey) {
            case "moral_reasoning" -> switch (i) {
                case "1A" -> "stage_level";
                case "1B" -> "foundations_balance";
                case "1C" -> "argument_chain";
                default -> "";
            };
            case "attitude_development" -> switch (i) {
                case "2A" -> "emotional_engagement";
                case "2B" -> "resilience";
                case "2C" -> "focus_flow";
                default -> "";
            };
            case "ability_growth" -> switch (i) {
                case "3A" -> "blooms_level";
                case "3B" -> "metacognition";
                case "3C" -> "transfer";
                default -> "";
            };
            case "strategy_optimization" -> switch (i) {
                case "4A" -> "diversity";
                case "4B" -> "depth";
                case "4C" -> "self_regulation";
                default -> "";
            };
            default -> "";
        };
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
            return Double.parseDouble(String.valueOf(v));
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

