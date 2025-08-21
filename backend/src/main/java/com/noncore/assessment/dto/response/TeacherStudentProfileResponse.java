package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudentProfileResponse {
    private Long id;
    private String name;
    private String avatar;
    private String email;
    private String className;
    private Integer enrolledCourseCount;
    private Double averageScore;
    private Integer rank;       // 可为 null
    private Double percentile;  // 可为 null
}


