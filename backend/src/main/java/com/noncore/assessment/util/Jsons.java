package com.noncore.assessment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Jsons {
    /**
     * 独立的 JSON 工具 ObjectMapper：
     * - 必须支持 java.time.*（用于行为摘要/快照序列化）
     * - 与 Spring Boot 默认行为保持一致：不输出时间戳数组
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    private Jsons() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.isBlank()) return java.util.Collections.emptyMap();
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            // 尝试宽松解析：去除代码块围栏/前后噪声/尾逗号/BOM
            try {
                String cleaned = cleanJson(json);
                return MAPPER.readValue(cleaned, Map.class);
            } catch (Exception ignored) {
                throw new IllegalArgumentException("Invalid JSON returned by LLM", e);
            }
        }
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj 任意对象
     * @return JSON 字符串（序列化失败则抛出 IllegalArgumentException）
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", e);
        }
    }

    private static String cleanJson(String input) {
        String s = input;
        // 去 BOM
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
            s = s.substring(1);
        }
        s = s.trim();

        // 去除 ```json ... ``` 或 ``` ... ``` 代码围栏
        Pattern fence = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher fm = fence.matcher(s);
        if (fm.find()) {
            s = fm.group(1).trim();
        }

        // 若前后有非 JSON 文本，截取第一个 { 到最后一个 }
        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            s = s.substring(firstBrace, lastBrace + 1);
        }

        // 去除尾逗号: ,}\n 或 ,]\n
        s = s.replaceAll(",\\s*}", "}");
        s = s.replaceAll(",\\s*]", "]");

        // 兜底 trim
        s = s.trim();
        return s;
    }
}


