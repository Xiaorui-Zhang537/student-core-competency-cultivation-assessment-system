package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface GradeMapper {

    /**
     * 插入成绩记录
     */
    int insertGrade(Grade grade);

    /**
     * 根据ID查询成绩
     */
    Grade selectGradeById(Long id);

    /**
     * 更新成绩
     */
    int updateGrade(Grade grade);

    /**
     * 删除成绩
     */
    int deleteGrade(Long id);

    /**
     * 根据学生ID查询成绩
     */
    List<Grade> selectByStudentId(Long studentId);

    /**
     * 根据作业ID查询成绩
     */
    List<Grade> selectByAssignmentId(Long assignmentId);

    /**
     * 分页查询学生成绩
     */
    List<Grade> selectByStudentIdWithPagination(@Param("studentId") Long studentId, 
                                               @Param("offset") int offset, 
                                               @Param("size") Integer size);

    /**
     * 分页查询作业成绩
     */
    List<Grade> selectByAssignmentIdWithPagination(@Param("assignmentId") Long assignmentId, 
                                                  @Param("offset") int offset, 
                                                  @Param("size") Integer size);

    /**
     * 统计学生成绩数量
     */
    Integer countByStudentId(Long studentId);

    /**
     * 统计作业成绩数量
     */
    Integer countByAssignmentId(Long assignmentId);

    /**
     * 根据学生和作业查询成绩
     */
    Grade selectByStudentAndAssignment(@Param("studentId") Long studentId, 
                                     @Param("assignmentId") Long assignmentId);

    /**
     * 根据学生和课程查询成绩
     */
    List<Grade> selectByStudentAndCourse(@Param("studentId") Long studentId, 
                                        @Param("courseId") Long courseId);

    /**
     * 根据课程查询成绩
     */
    List<Grade> selectByCourseId(Long courseId);

    /**
     * 获取待评分成绩
     */
    List<Grade> selectPendingGrades(Long teacherId);

    /**
     * 计算学生平均分
     */
    BigDecimal calculateAverageScore(Long studentId);

    /**
     * 计算学生课程平均分
     */
    BigDecimal calculateCourseAverageScore(@Param("studentId") Long studentId, 
                                         @Param("courseId") Long courseId);

    /**
     * 计算作业平均分
     */
    BigDecimal calculateAssignmentAverageScore(Long assignmentId);

    /**
     * 获取成绩分布
     */
    List<Map<String, Object>> getGradeDistribution(Long assignmentId);

    /**
     * 获取课程成绩统计
     */
    Map<String, Object> getCourseGradeStats(Long courseId);
} 