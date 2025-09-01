package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseProgressResponse {
    private Long courseId;
    private Double progress;       // 总体进度（0-100）
    private Long totalStudyMinutes; // 总学习时长（分钟）
    private Long weeklyStudyMinutes; // 本周学习时长（分钟）
    private String lastStudiedLessonTitle; // 最近学习章节
}


