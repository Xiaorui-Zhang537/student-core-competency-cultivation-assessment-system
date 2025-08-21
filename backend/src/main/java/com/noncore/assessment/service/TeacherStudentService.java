package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.TeacherStudentProfileResponse;
import com.noncore.assessment.entity.Course;
import java.util.List;

public interface TeacherStudentService {
    TeacherStudentProfileResponse getStudentProfile(Long teacherId, Long studentId);
    List<Course> getStudentCourses(Long teacherId, Long studentId);

    /**
     * 获取课程下学生基础列表（用于老师端雷达图下拉）
     *
     * @param teacherId 当前教师ID，用于权限校验
     * @param courseId  课程ID（必填）
     * @param page      页码（从1开始，可空）
     * @param size      每页数量（可空）
     * @param keyword   关键字（按学生姓名/学号筛选，可空）
     * @return 基础列表响应，包含 items/total 等
     */
    com.noncore.assessment.dto.response.CourseStudentBasicResponse getCourseStudentsBasic(
            Long teacherId,
            Long courseId,
            Integer page,
            Integer size,
            String keyword
    );
}
