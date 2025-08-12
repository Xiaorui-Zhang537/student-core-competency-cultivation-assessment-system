package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.*;
import com.noncore.assessment.service.llm.DeepseekClient;
import com.noncore.assessment.service.llm.PromptBuilder;
import com.noncore.assessment.config.AiConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;
    private final DeepseekClient deepseekClient;
    private final PromptBuilder promptBuilder;
    private final AiConfigProperties aiConfigProperties;

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

        // 画像与提示词构建
        List<Message> built = promptBuilder.buildMessages(messages, course, students);

        // 默认通过 OpenRouter 调用（兼容未来扩展）
        String provider = "openrouter";

        String model = (request.getModel() == null || request.getModel().isBlank())
                ? aiConfigProperties.getDeepseek().getModel()
                : request.getModel();

        String baseUrl;
        String apiKey;
        baseUrl = aiConfigProperties.getProviders().getOpenrouter().getBaseUrl();
        apiKey = aiConfigProperties.getProviders().getOpenrouter().getApiKey();

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "LLM base-url 未配置");
        }
        return deepseekClient.createChatCompletion(built, model, false, baseUrl, apiKey);
    }
}

