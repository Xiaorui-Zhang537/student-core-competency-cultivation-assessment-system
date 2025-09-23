package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeListItem {
    private Long id;
    private Long submissionId;
    private Long studentId;
    private Long assignmentId;
    private Long courseId;

    private String assignmentTitle;
    private String courseTitle;
    private String teacherName;

    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percentage;
    private String gradeLevel;
    private String status; // draft|published|returned

    private LocalDateTime gradedAt; // publishedAt or updatedAt
    private Boolean isPublished;
}


