package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.StudentAnalysisResponse;

/**
 * 学生成绩分析服务
 */
public interface StudentAnalysisService {

    /**
     * 获取学生成绩分析聚合数据
     * @param studentId 学生ID
     * @param range 时间范围（7d|30d|term），可为空默认30d
     * @return 学生成绩分析
     */
    StudentAnalysisResponse getStudentAnalysis(Long studentId, String range);
}


