package com.noncore.assessment.service;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.util.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 作业服务接口
 * 定义作业相关的业务逻辑操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface AssignmentService {

    /**
     * 创建新作业
     *
     * @param assignment 作业信息
     * @return 创建的作业
     */
    Assignment createAssignment(Assignment assignment);

    /**
     * 更新作业信息
     *
     * @param assignmentId 作业ID
     * @param assignment 更新的作业信息
     * @return 更新后的作业
     */
    Assignment updateAssignment(Long assignmentId, Assignment assignment);

    /** 仅绑定作业到某节课（不触发完整字段校验） */
    boolean bindAssignmentToLesson(Long assignmentId, Long lessonId);

    /**
     * 删除作业
     *
     * @param assignmentId 作业ID
     */
    void deleteAssignment(Long assignmentId);

    /**
     * 根据ID获取作业详情
     *
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    Assignment getAssignmentById(Long assignmentId);

    /**
     * 分页查询作业列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param courseId 课程ID（可选）
     * @param teacherId 教师ID（可选）
     * @param status 作业状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    PageResult<Assignment> getAssignments(Integer page, Integer size, Long courseId, 
                                         Long teacherId, String status, String keyword);

    /**
     * 根据课程ID获取作业列表
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    List<Assignment> getAssignmentsByCourse(Long courseId);

    /**
     * 根据教师ID获取作业列表
     *
     * @param teacherId 教师ID
     * @return 作业列表
     */
    List<Assignment> getAssignmentsByTeacher(Long teacherId);

    /**
     * 获取学生的作业列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return 作业列表
     */
    List<Assignment> getAssignmentsForStudent(Long studentId, Long courseId);

    /**
     * 学生端分页获取作业（DB侧过滤，含关键词/状态/课程筛选）
     *
     * @param includeHistory 是否包含历史课程（非 active 的选课记录）。
     *                       为 true 时不过滤 enrollment 的状态；默认为 false，仅返回在读(active)课程作业。
     */
    PageResult<Assignment> getAssignmentsForStudent(Long studentId, Integer page, Integer size, Long courseId, String status, String keyword, Boolean includeHistory, Boolean onlyPending);

    /**
     * 发布作业
     *
     * @param assignmentId 作业ID
     */
    void publishAssignment(Long assignmentId);

    /**
     * 下架作业
     *
     * @param assignmentId 作业ID
     */
    void unpublishAssignment(Long assignmentId);

    /**
     * 关闭作业（不再接收提交）
     *
     * @param assignmentId 作业ID
     */
    void closeAssignment(Long assignmentId);

    /**
     * 获取即将到期的作业
     *
     * @param days 天数
     * @return 作业列表
     */
    List<Assignment> getDueAssignments(Integer days);

    /**
     * 检查作业是否可以提交
     *
     * @param assignmentId 作业ID
     * @return 是否可以提交
     */
    boolean canSubmit(Long assignmentId);

    /**
     * 获取作业统计信息
     *
     * @param teacherId 教师ID（可选）
     * @param courseId 课程ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getAssignmentStatistics(Long teacherId, Long courseId);

    /**
     * 批量更新作业状态
     *
     * @param assignmentIds 作业ID列表
     * @param status 新状态
     */
    void batchUpdateStatus(List<Long> assignmentIds, String status);

    /**
     * 更新作业提交计数
     *
     * @param assignmentId 作业ID
     * @param increment 增量
     */
    void updateSubmissionCount(Long assignmentId, Integer increment);

    /**
     * 获取指定作业的提交统计（课程总人数、已提交、未提交）
     */
    com.noncore.assessment.dto.response.AssignmentSubmissionStatsResponse getSubmissionStats(Long assignmentId, Long currentUserId);

    /**
     * 向未提交的学生发送提醒
     * @return 发送结果统计 { sent, failed }
     */
    java.util.Map<String, Object> remindUnsubmitted(Long assignmentId, Long currentUserId, String customMessage);
} 