package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AbilityAnalyticsMapper {

    /**
     * 查询指定学生在课程/班级维度下的各能力维度平均分（原始分, 非归一化）
     */
    List<Map<String, Object>> selectStudentDimensionAvg(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 查询班级/课程层面的各能力维度平均分（原始分, 非归一化）
     */
    List<Map<String, Object>> selectClassOrCourseDimensionAvg(
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 查询课程（或班级）在时间区间内的成绩均分（原始分与满分）
     */
    Map<String, Object> selectCourseGradeAvg(
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("studentId") Long studentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 读取课程的权重配置
     */
    List<Map<String, Object>> selectCourseWeights(@Param("courseId") Long courseId);

    /**
     * 插入或更新课程维度权重
     */
    int upsertCourseWeight(@Param("courseId") Long courseId,
                           @Param("dimensionCode") String dimensionCode,
                           @Param("weight") Double weight);

    // 带作业集合过滤的扩展查询
    List<Map<String, Object>> selectStudentDimensionAvgWithAssignments(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("assignmentIds") List<Long> assignmentIds
    );

    List<Map<String, Object>> selectClassOrCourseDimensionAvgWithAssignments(
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("assignmentIds") List<Long> assignmentIds
    );

    Map<String, Object> selectCourseGradeAvgWithAssignments(
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("studentId") Long studentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("assignmentIds") List<Long> assignmentIds
    );

    /**
     * 学生能力快照（student_abilities），用于当 ability_assessments 暂无数据时兜底
     */
    List<Map<String, Object>> selectStudentAbilitySnapshot(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    /**
     * 班级/课程能力快照（student_abilities 聚合）
     */
    List<Map<String, Object>> selectClassAbilitySnapshot(
            @Param("courseId") Long courseId
    );
}

