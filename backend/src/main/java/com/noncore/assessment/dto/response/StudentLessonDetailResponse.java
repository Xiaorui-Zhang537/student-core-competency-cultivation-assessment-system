package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLessonDetailResponse {
    private Lesson lesson;
    private LessonProgress progress;
}


