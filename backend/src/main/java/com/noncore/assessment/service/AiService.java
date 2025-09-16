package com.noncore.assessment.service;

import com.noncore.assessment.dto.request.AiChatRequest;

public interface AiService {
    /**
     * 基于课程与学生上下文，调用大模型生成回答。
     * @param request 对话与上下文
     * @param teacherId 当前教师ID
     * @return 模型回答
     */
    String generateAnswer(AiChatRequest request, Long teacherId);

    /**
     * 与 generateAnswer 类似，但强制模型以 JSON 对象输出（用于作文批改）。
     */
    String generateAnswerJsonOnly(AiChatRequest request, Long teacherId);
}

