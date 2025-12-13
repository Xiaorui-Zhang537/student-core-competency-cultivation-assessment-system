package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiService;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.service.FileStorageService;
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
        List<Message> built = promptBuilder.buildMessages(messages, systemPrompt);

        // 将附件（如有）注入多模态消息结构：将最近一条 user 消息的 content 替换为 content[]，附加 image_url
        List<Map<String, Object>> payloadMessages = new java.util.ArrayList<>();
        for (Message m : built) {
            payloadMessages.add(java.util.Map.of(
                    "role", m.getRole(),
                    "content", m.getContent()
            ));
        }
        List<Long> attachments = request.getAttachmentFileIds();
        if (attachments != null && !attachments.isEmpty()) {
            // 找到最后一条 user 消息
            for (int i = payloadMessages.size() - 1; i >= 0; i--) {
                Object role = payloadMessages.get(i).get("role");
                if ("user".equals(String.valueOf(role))) {
                    Object content = payloadMessages.get(i).get("content");
                    java.util.List<Object> contentList = new java.util.ArrayList<>();
                    if (content != null && !String.valueOf(content).isBlank()) {
                        contentList.add(java.util.Map.of("type", "text", "text", String.valueOf(content)));
                    }
                    // 将图片以 data URL 内联，避免外网不可达与鉴权问题
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

        String baseUrl;
        String apiKey;
        boolean useGoogle = model != null && model.startsWith("google/");
        boolean useGlm = model != null && model.startsWith("glm-");
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
}

