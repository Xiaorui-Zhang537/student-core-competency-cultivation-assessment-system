package com.noncore.assessment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "教师视角-学生概况")
public class TeacherStudentProfileDto {
    private Long id;
    private String name;
    private String avatar;
    private String email;
    private String className;
    private Integer enrolledCourseCount;
    private BigDecimal averageScore;
    private Integer rank;           // 可选：在老师课程中的综合排名
    private BigDecimal percentile;  // 百分位
}


