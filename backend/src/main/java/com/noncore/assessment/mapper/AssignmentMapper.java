package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.noncore.assessment.dto.response.StudentDashboardResponse;

/**
 * 作业数据访问接口
 * 处理assignments表的CRUD操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface AssignmentMapper {

    /**
     * 插入新作业
     *
     * @param assignment 作业对象
     * @return 影响的行数
     */
    int insertAssignment(Assignment assignment);

    /**
     * 根据ID查询作业
     *
     * @param id 作业ID
     * @return 作业对象
     */
    Assignment selectAssignmentById(@Param("id") Long id);

    /**
     * 更新作业信息
     *
     * @param assignment 作业对象
     * @return 影响的行数
     */
    int updateAssignment(Assignment assignment);

    /**
     * 软删除作业
     *
     * @param id 作业ID
     * @return 影响的行数
     */
    int deleteAssignment(@Param("id") Long id);

    /**
     * 根据课程ID查询作业列表
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    List<Assignment> selectAssignmentsByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据教师ID查询作业列表
     *
     * @param teacherId 教师ID
     * @return 作业列表
     */
    List<Assignment> selectAssignmentsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 分页查询作业列表
     *
     * @param courseId 课程ID（可选）
     * @param teacherId 教师ID（可选）
     * @param status 作业状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 作业列表
     */
    List<Assignment> selectAssignmentsWithPagination(@Param("courseId") Long courseId,
                                                    @Param("teacherId") Long teacherId,
                                                    @Param("status") String status,
                                                    @Param("keyword") String keyword);

    /**
     * 更新作业提交数量
     *
     * @param assignmentId 作业ID
     * @param increment 增量（正数表示增加，负数表示减少）
     * @return 影响的行数
     */
    int updateSubmissionCount(@Param("assignmentId") Long assignmentId, @Param("increment") Integer increment);

    /**
     * 根据状态查询作业
     *
     * @param status 作业状态
     * @return 作业列表
     */
    List<Assignment> selectAssignmentsByStatus(@Param("status") String status);

    /**
     * 统计作业数量
     *
     * @param courseId 课程ID（可选）
     * @param teacherId 教师ID（可选）
     * @param status 作业状态（可选）
     * @return 作业数量
     */
    long countAssignments(@Param("courseId") Long courseId, 
                        @Param("teacherId") Long teacherId, 
                        @Param("status") String status);

    /**
     * 检查作业是否存在
     *
     * @param id 作业ID
     * @return 存在数量（0或1）
     */
    int checkAssignmentExists(@Param("id") Long id);

    /**
     * 批量更新作业状态
     *
     * @param assignmentIds 作业ID列表
     * @param status 新状态
     * @return 影响的行数
     */
    int batchUpdateAssignmentStatus(@Param("assignmentIds") List<Long> assignmentIds, @Param("status") String status);

    /**
     * 查询即将到期的作业
     *
     * @param days 天数
     * @return 作业列表
     */
    List<Assignment> selectDueAssignments(@Param("days") Integer days);

    /**
     * 根据课程和学生查询作业（学生视角）
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 作业列表
     */
    List<Assignment> selectAssignmentsForStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);

    /**
     * 获取作业统计信息
     *
     * @param teacherId 教师ID（可选）
     * @param courseId 课程ID（可选）
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> getAssignmentStatistics(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    long countPendingAssignments(@Param("studentId") Long studentId);

    List<StudentDashboardResponse.PendingAssignmentDto> findPendingAssignments(@Param("studentId") Long studentId, @Param("limit") int limit);

    List<Assignment> findAssignmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据学生ID查询待处理的作业列表 (返回实体)
     *
     * @param studentId 学生ID
     * @return 待处理的作业列表
     */
    List<Assignment> findPendingAssignmentsForStudent(@Param("studentId") Long studentId);

    long countMonthlyByTeacher(@Param("teacherId") Long teacherId);
} 