package com.noncore.assessment.service.llm;

import com.noncore.assessment.dto.request.AiChatRequest.Message;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    public List<Message> buildMessages(List<Message> userMessages, Course course, List<User> students) {
        List<Message> built = new ArrayList<>();

        // System 指令
        built.add(new Message("system", "你是一名专业的教学数据分析助理。请基于提供的课程与学生数据，输出：1) 核心结论（要点式）；2) 主要发现（含指标与对比）；3) 可执行建议（针对课程与学生）。使用简洁中文，避免冗长。"));

        // 课程块
        if (course != null) {
            String courseBlock = String.format("课程信息：title=%s, category=%s, difficulty=%s, duration=%s小时, period=%s~%s",
                    nullToEmpty(course.getTitle()), nullToEmpty(course.getCategory()), nullToEmpty(course.getDifficulty()),
                    course.getDuration() == null ? "-" : String.valueOf(course.getDuration()),
                    String.valueOf(course.getStartDate()), String.valueOf(course.getEndDate()));
            built.add(new Message("system", courseBlock));
        }

        // 学生块（仅包含必要字段，最多 5 名）
        if (students != null && !students.isEmpty()) {
            String studentBlock = students.stream().limit(5)
                    .map(u -> String.format("{id:%s,name:%s}", String.valueOf(u.getId()), nullToEmpty(u.getNickname())))
                    .collect(Collectors.joining("; "));
            built.add(new Message("system", "学生样本（最多5）:" + studentBlock));
        }

        // 合并用户上下文（简单保留最近 6 条）
        int keep = Math.min(6, userMessages.size());
        built.addAll(userMessages.subList(userMessages.size() - keep, userMessages.size()));

        return built;
    }

    private String nullToEmpty(String v) { return v == null ? "" : v; }
}

