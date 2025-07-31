package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.dto.response.TeacherDashboardResponse;

public interface DashboardService {
    /**
     * 获取学生仪表盘数据
     *
     * @param studentId 学生ID
     * @return 包含所有仪表盘数据的聚合响应对象
     */
    StudentDashboardResponse getStudentDashboardData(Long studentId);

    /**
     * 获取教师仪表盘数据
     *
     * @param teacherId 教师ID
     * @return 包含所有仪表盘数据的聚合响应对象
     */
    TeacherDashboardResponse getTeacherDashboardData(Long teacherId);
} 