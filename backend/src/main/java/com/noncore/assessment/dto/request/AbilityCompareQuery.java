package com.noncore.assessment.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * 对比查询：支持两段时间与两组作业（取交集），同一学生、同一课程
 */
@Data
public class AbilityCompareQuery {
    private Long courseId;
    private Long classId; // 可选
    private Long studentId; // 必填

    // 区间A
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateA; // 已废弃（仅作业对比时可为空）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateA;   // 已废弃（仅作业对比时可为空）
    private List<Long> assignmentIdsA; // 可选

    // 区间B
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateB; // 已废弃（仅作业对比时可为空）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateB;   // 已废弃（仅作业对比时可为空）
    private List<Long> assignmentIdsB; // 可选

    /**
     * 班级均值包含选项：none | A | B | both
     */
    private String includeClassAvg; // 默认 both
}


