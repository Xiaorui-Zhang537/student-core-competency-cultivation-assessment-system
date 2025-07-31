package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatsResponse {
    private int totalCourses;
    private long activeCourses;
    private int totalStudents;
    private int totalAssignments;
    private int pendingGradings;
} 