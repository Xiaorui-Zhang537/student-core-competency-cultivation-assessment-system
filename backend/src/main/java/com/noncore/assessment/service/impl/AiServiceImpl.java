package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiService;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.AiMemoryService;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.service.file.DocumentTextExtractor;
import com.noncore.assessment.service.llm.LlmClient;
import com.noncore.assessment.service.llm.PromptBuilder;
import com.noncore.assessment.config.AiConfigProperties;
import com.noncore.assessment.service.llm.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final LlmClient deepseekClient;
    private final com.noncore.assessment.service.llm.GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;
    private final AiConfigProperties aiConfigProperties;
    private final PromptLoader promptLoader;
    private final FileStorageService fileStorageService;
    private final AiConversationService conversationService;
    private final AiMemoryService memoryService;
    private final DocumentTextExtractor documentTextExtractor;

    // 保护：避免用户写过长记忆/附件导致上下文溢出
    private static final int MEMORY_MAX_CHARS = 3000;
    private static final int ATTACHMENT_TEXT_MAX_CHARS = 20000;

    @Override
    public String generateAnswer(AiChatRequest request, Long teacherId) {
        List<Message> messages = request.getMessages();
        if (CollectionUtils.isEmpty(messages)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "messages 不能为空");
        }

        Long courseId = request.getCourseId();
        List<Long> studentIds = request.getStudentIds();

        Course course = null;
        List<User> students = new ArrayList<>();

        if (courseId != null) {
            course = courseService.getCourseById(courseId);
            if (course == null || !Objects.equals(course.getTeacherId(), teacherId)) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "无权访问该课程");
            }

            if (!CollectionUtils.isEmpty(studentIds)) {
                // 去重并限制数量
                List<Long> unique = studentIds.stream().filter(Objects::nonNull).distinct().limit(5).toList();

                // 拉取课程学生（分页 1, size 大值），实际可优化为专用校验
                var page = enrollmentService.getCourseStudents(teacherId, courseId, 1, 1000, null, "joinDate", null, null, null);
                Set<Long> allowed = page.getItems().stream().map(User::getId).collect(Collectors.toSet());
                for (Long sid : unique) {
                    if (!allowed.contains(sid)) {
                        throw new BusinessException(ErrorCode.PERMISSION_DENIED, "学生不属于该课程");
                    }
                }
                // 过滤出所选学生信息
                students = page.getItems().stream().filter(u -> unique.contains(u.getId())).collect(Collectors.toList());
            }
        }

        // 根据请求参数决定是否注入“作文批改”系统 Prompt
        String systemPrompt = null;
        if (Boolean.TRUE.equals(request.getUseGradingPrompt())) {
            systemPrompt = promptLoader.loadSystemPrompt(request.getSystemPromptPath());
        }

        // 长期记忆：仅在“普通聊天”（非批改 Prompt）时注入
        if (!Boolean.TRUE.equals(request.getUseGradingPrompt())) {
            try {
                var mem = memoryService.getMemory(teacherId);
                if (mem != null && Boolean.TRUE.equals(mem.getEnabled())) {
                    String content = safeTrimToMax(mem.getContent(), MEMORY_MAX_CHARS);
                    if (content != null && !content.isBlank()) {
                        String memPrompt = buildMemorySystemPrompt(content);
                        systemPrompt = (systemPrompt == null || systemPrompt.isBlank())
                                ? memPrompt
                                : (systemPrompt + "\n\n" + memPrompt);
                    }
                }
            } catch (Exception ignored) {
                // 记忆不可用不应影响聊天主流程
            }
        }
        List<Message> built = promptBuilder.buildMessages(messages, systemPrompt);

        // 先确定模型（会话优先），再决定是否支持附件等能力
        String model;
        Long convId = request.getConversationId();
        if (convId != null) {
            // 强制单会话单模型：读取会话模型并忽略请求中的 model
            AiConversation conv = conversationService.getConversation(teacherId, convId);
            String m = conv != null ? conv.getModel() : null;
            model = conversationService.normalizeModel(m);
        } else {
            model = conversationService.normalizeModel(request.getModel());
        }

        boolean useGoogle = model != null && model.startsWith("google/");
        boolean useGlm = model != null && model.startsWith("glm-");

        // 将附件（如有）注入多模态消息结构：将最近一条 user 消息的 content 替换为 content[]，附加 image_url
        List<Map<String, Object>> payloadMessages = new java.util.ArrayList<>();
        for (Message m : built) {
            payloadMessages.add(java.util.Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }
        List<Long> attachments = request.getAttachmentFileIds();
        // 附件仅对 Gemini（google/*）开放；GLM 不展示上传入口，这里也直接忽略，避免意外大上下文。
        if (useGoogle && attachments != null && !attachments.isEmpty()) {
            // 找到最后一条 user 消息
            for (int i = payloadMessages.size() - 1; i >= 0; i--) {
                Object role = payloadMessages.get(i).get("role");
                if ("user".equals(String.valueOf(role))) {
                    Object content = payloadMessages.get(i).get("content");
                    java.util.List<Object> contentList = new java.util.ArrayList<>();
                    if (content != null && !String.valueOf(content).isBlank()) {
                        contentList.add(java.util.Map.of("type", "text", "text", String.valueOf(content)));
                    }
                    // 1) 文档抽取文本（pdf/doc/docx/txt）：作为 text part 注入（限制总长度）
                    int remaining = ATTACHMENT_TEXT_MAX_CHARS;
                    for (Long fid : attachments) {
                        if (remaining <= 0) break;
                        try {
                            var info = fileStorageService.getFileInfo(fid);
                            if (info == null) continue;
                            String mime = info.getMimeType();
                            String name = (info.getOriginalName() != null && !info.getOriginalName().isBlank())
                                    ? info.getOriginalName()
                                    : (info.getStoredName() != null ? info.getStoredName() : ("#" + fid));
                            if (!isSupportedDocMime(mime, name)) continue;
                            byte[] bytes = fileStorageService.downloadFile(fid, teacherId);
                            String extracted = documentTextExtractor.extractText(new java.io.ByteArrayInputStream(bytes), name, mime);
                            extracted = normalizeExtractedText(extracted);
                            if (extracted == null || extracted.isBlank()) continue;
                            String chunk = "【附件文本：" + name + "】\n" + extracted + "\n";
                            if (chunk.length() > remaining) {
                                chunk = chunk.substring(0, Math.max(0, remaining));
                            }
                            remaining -= chunk.length();
                            if (!chunk.isBlank()) {
                                contentList.add(java.util.Map.of("type", "text", "text", chunk));
                            }
                        } catch (Exception ignored) { }
                    }
                    // 2) 图片：以 data URL 内联（后续在 GeminiClient 转换为 inlineData）
                    for (Long fid : attachments) {
                        try {
                            var info = fileStorageService.getFileInfo(fid);
                            if (info == null || info.getMimeType() == null || !info.getMimeType().startsWith("image/")) {
                                continue;
                            }
                            byte[] bytes = fileStorageService.downloadFile(fid, teacherId);
                            String b64 = Base64.getEncoder().encodeToString(bytes);
                            String dataUrl = "data:" + info.getMimeType() + ";base64," + b64;
                            contentList.add(java.util.Map.of(
                                    "type", "image_url",
                                    "image_url", java.util.Map.of("url", dataUrl)
                            ));
                        } catch (Exception ignored) { }
                    }
                    payloadMessages.set(i, new java.util.HashMap<>(java.util.Map.of(
                            "role", "user",
                            "content", contentList
                    )));
                    break;
                }
            }
        }

        String baseUrl;
        String apiKey;
        if (useGoogle) {
            baseUrl = aiConfigProperties.getProviders().getGoogle().getBaseUrl();
            apiKey = aiConfigProperties.getProviders().getGoogle().getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "Google Gemini API Key 未配置，请先在后台配置");
            }
        } else if (useGlm) {
            baseUrl = aiConfigProperties.getProviders().getGlm().getBaseUrl();
            apiKey = aiConfigProperties.getProviders().getGlm().getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "GLM API Key 未配置，请先在后台配置");
            }
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "仅支持 Google Gemini 或 GLM 模型");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "LLM base-url 未配置");
        }
        if (useGoogle) {
            return geminiClient.generate(payloadMessages, model.replaceFirst("^google/", ""), Boolean.TRUE.equals(request.getJsonOnly()), baseUrl, apiKey);
        }
        if (Boolean.TRUE.equals(request.getJsonOnly())) {
            return deepseekClient.createChatCompletionJsonOnly(payloadMessages, model, false, baseUrl, apiKey);
        }
        return deepseekClient.createChatCompletionRaw(payloadMessages, model, false, baseUrl, apiKey);
    }

    @Override
    public String generateAnswerJsonOnly(AiChatRequest request, Long teacherId) {
        List<Message> messages = request.getMessages();
        if (org.springframework.util.CollectionUtils.isEmpty(messages)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "messages 不能为空");
        }

        // 默认使用“作文批改”系统 Prompt
        String systemPrompt = null;
        Boolean usePrompt = request.getUseGradingPrompt();
        if (usePrompt == null || usePrompt) {
            systemPrompt = promptLoader.loadSystemPrompt(request.getSystemPromptPath());
        }
        List<Message> built = promptBuilder.buildMessages(messages, systemPrompt);

        List<Map<String, Object>> payloadMessages = new java.util.ArrayList<>();
        for (Message m : built) {
            payloadMessages.add(java.util.Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }

        // 多模态附件处理（若有）
        List<Long> attachments = request.getAttachmentFileIds();
        if (attachments != null && !attachments.isEmpty()) {
            for (int i = payloadMessages.size() - 1; i >= 0; i--) {
                Object role = payloadMessages.get(i).get("role");
                if ("user".equals(String.valueOf(role))) {
                    Object content = payloadMessages.get(i).get("content");
                    java.util.List<Object> contentList = new java.util.ArrayList<>();
                    if (content != null && !String.valueOf(content).isBlank()) {
                        contentList.add(java.util.Map.of("type", "text", "text", String.valueOf(content)));
                    }
                    for (Long fid : attachments) {
                        try {
                            var info = fileStorageService.getFileInfo(fid);
                            if (info == null || info.getMimeType() == null || !info.getMimeType().startsWith("image/")) {
                                continue;
                            }
                            byte[] bytes = fileStorageService.downloadFile(fid, teacherId);
                            String b64 = Base64.getEncoder().encodeToString(bytes);
                            String dataUrl = "data:" + info.getMimeType() + ";base64," + b64;
                            contentList.add(java.util.Map.of(
                                    "type", "image_url",
                                    "image_url", java.util.Map.of("url", dataUrl)
                            ));
                        } catch (Exception ignored) { }
                    }
                    payloadMessages.set(i, new java.util.HashMap<>(java.util.Map.of(
                            "role", "user",
                            "content", contentList
                    )));
                    break;
                }
            }
        }

        String model = conversationService.normalizeModel(request.getModel());

        String baseUrl;
        String apiKey;
        boolean useGoogle2 = model != null && model.startsWith("google/");
        boolean useGlm = model != null && model.startsWith("glm-");
        if (useGoogle2) {
            baseUrl = aiConfigProperties.getProviders().getGoogle().getBaseUrl();
            apiKey = aiConfigProperties.getProviders().getGoogle().getApiKey();
        } else if (useGlm) {
            baseUrl = aiConfigProperties.getProviders().getGlm().getBaseUrl();
            apiKey = aiConfigProperties.getProviders().getGlm().getApiKey();
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "仅支持 Google Gemini 或 GLM 模型");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "LLM base-url 未配置");
        }
        if (useGoogle2) {
            return geminiClient.generate(payloadMessages, model.replaceFirst("^google/", ""), true, baseUrl, apiKey);
        }
        return deepseekClient.createChatCompletionJsonOnly(payloadMessages, model, false, baseUrl, apiKey);
    }

    private String buildMemorySystemPrompt(String memoryContent) {
        // 尽量用“系统级约束”表达，让模型在回答时自然参考；同时明确优先级，避免与当前问题冲突时“死守记忆”。
        return """
你正在为同一位用户提供长期帮助。下面是该用户的【长期记忆】（偏好/背景/约束）。
你必须在不与用户当前问题冲突的前提下遵循这些记忆；如果冲突，以用户当前问题为准。
不要逐字复述记忆，除非用户明确要求。

【长期记忆】
""" + memoryContent;
    }

    private String safeTrimToMax(String s, int maxChars) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return v;
        int max = Math.max(0, maxChars);
        if (v.length() <= max) return v;
        // 兼容多字节字符：按 code unit 截断足够（这里主要保护上下文长度）
        return v.substring(0, max);
    }

    private boolean isSupportedDocMime(String mimeType, String fileName) {
        String mt = mimeType == null ? "" : mimeType.toLowerCase();
        String fn = fileName == null ? "" : fileName.toLowerCase();
        if (mt.startsWith("image/")) return false;
        // mime 优先
        if (mt.contains("pdf")) return true;
        if (mt.contains("word") || mt.contains("officedocument")) return true;
        if (mt.startsWith("text/")) return true;
        // 后缀兜底
        return fn.endsWith(".pdf") || fn.endsWith(".doc") || fn.endsWith(".docx") || fn.endsWith(".txt");
    }

    private String normalizeExtractedText(String text) {
        if (text == null) return null;
        // Tika 可能带大量空行/不可见字符
        String v = text.replace("\u0000", " ").trim();
        if (v.isBlank()) return v;
        // 压缩连续空行（保持可读性）
        v = v.replaceAll("\\n{4,}", "\n\n\n");
        // 轻度截断（总长度在上游调用处统一控制，这里再做一次保险）
        if (v.length() > ATTACHMENT_TEXT_MAX_CHARS) {
            v = v.substring(0, ATTACHMENT_TEXT_MAX_CHARS);
        }
        return v;
    }
}

