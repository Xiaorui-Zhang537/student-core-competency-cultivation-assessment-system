package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String coverImageUrl;
    private String teacherName;
    /** 0-100 之间的进度百分比 */
    private Double progress;
    private LocalDateTime enrolledAt;
}


