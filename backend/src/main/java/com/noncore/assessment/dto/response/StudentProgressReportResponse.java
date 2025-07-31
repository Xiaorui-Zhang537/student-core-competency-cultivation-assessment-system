package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressReportResponse {

    private User student;
    private BigDecimal overallProgress;
    private Integer completedLessons;
    private Integer totalLessons;
    private Integer studyTimeMinutes;
    private BigDecimal averageGrade;
    private List<Grade> grades;
} 