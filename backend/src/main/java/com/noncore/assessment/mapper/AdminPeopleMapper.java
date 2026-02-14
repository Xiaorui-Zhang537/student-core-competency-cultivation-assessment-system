package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员-学生/教师数据中心聚合 Mapper。
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminPeopleMapper {

    Long countEnrolledCourses(@Param("studentId") Long studentId);

    Double avgGradePercentage(@Param("studentId") Long studentId);

    String lastActiveAt(@Param("userId") Long userId);

    Long countAbilityReports(@Param("studentId") Long studentId);

    Long countCoursesByTeacher(@Param("teacherId") Long teacherId);

    Long countAssignmentsByTeacher(@Param("teacherId") Long teacherId);

    Long countActiveEnrollmentsForTeacher(@Param("teacherId") Long teacherId);
}

