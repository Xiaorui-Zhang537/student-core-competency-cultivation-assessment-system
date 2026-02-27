package com.noncore.assessment.dto.response.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员端-课程课堂笔记列表项（只读）。
 *
 * <p>数据来源：lesson_progress.notes + lessons/chapters/users 联表。</p>
 *
 * @author System
 * @since 2026-02-27
 */
@Data
@Builder
public class AdminLessonNoteListItemResponse {
    private Long lessonId;
    private String lessonTitle;
    private String chapterTitle;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String notes;
    private LocalDateTime updatedAt;
}

