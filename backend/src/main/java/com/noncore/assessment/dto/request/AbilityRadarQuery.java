package com.noncore.assessment.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 教师端五维能力雷达图查询参数
 */
@Data
public class AbilityRadarQuery {
    private Long courseId;
    private Long classId; // 可选
    private Long studentId; // 可选：传入则返回“学生 vs 班级”

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

