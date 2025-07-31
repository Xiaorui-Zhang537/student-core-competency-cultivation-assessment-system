package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassPerformanceResponse {

    private Course course;
    private int totalStudents;
    private Map<String, Object> gradeStats;
    private Map<String, Object> activityStats;
} 