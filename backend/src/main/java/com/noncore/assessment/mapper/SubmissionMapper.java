package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 作业提交Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface SubmissionMapper {

    /**
     * 插入提交记录
     */
    int insertSubmission(Submission submission);

    /**
     * 根据ID查询提交
     */
    Submission selectSubmissionById(Long id);

    /**
     * 更新提交记录
     */
    int updateSubmission(Submission submission);

    /**
     * 删除提交记录
     */
    int deleteSubmission(Long id);

    /**
     * 根据作业ID和学生ID查询提交
     */
    Submission selectByAssignmentAndStudent(@Param("assignmentId") Long assignmentId, 
                                           @Param("studentId") Long studentId);

    /**
     * 根据学生ID查询提交列表
     */
    List<Submission> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 分页查询学生提交
     */
    List<Submission> selectByStudentIdWithPagination(@Param("studentId") Long studentId,
                                                     @Param("offset") int offset,
                                                     @Param("size") Integer size);

    /**
     * 统计学生提交数量
     */
    long countByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据作业ID查询提交列表
     */
    List<Submission> selectByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 分页查询作业提交
     */
    List<Submission> selectByAssignmentIdWithPagination(@Param("assignmentId") Long assignmentId,
                                                        @Param("offset") int offset,
                                                        @Param("size") Integer size);

    /**
     * 统计作业提交数量
     */
    long countByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 获取学生成绩列表（关联查询）
     */
    List<Map<String, Object>> selectStudentGradesWithPagination(@Param("studentId") Long studentId,
                                                                @Param("offset") int offset,
                                                                @Param("size") Integer size);

    /**
     * 统计学生成绩数量
     */
    long countStudentGrades(@Param("studentId") Long studentId);

    /**
     * 获取提交的成绩详情
     */
    Map<String, Object> selectSubmissionGrade(@Param("submissionId") Long submissionId);

    /**
     * 更新提交状态
     */
    int updateSubmissionStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 获取作业提交统计
     */
    Map<String, Object> getSubmissionStatistics(@Param("assignmentId") Long assignmentId);

    /**
     * 检查学生是否已提交
     */
    long countByAssignmentAndStudent(@Param("assignmentId") Long assignmentId,
                                       @Param("studentId") Long studentId);

    long countPendingByTeacher(@Param("teacherId") Long teacherId);

    List<com.noncore.assessment.dto.response.TeacherDashboardResponse.PendingGradingDto> findPendingByTeacher(@Param("teacherId") Long teacherId, @Param("limit") int limit);

    /** 统计作业提交（排除软删除） */
    long countByAssignment(@Param("assignmentId") Long assignmentId);
} 