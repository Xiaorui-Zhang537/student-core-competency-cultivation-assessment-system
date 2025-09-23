package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.GradeStatsResponse;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.dto.response.GradeListItem;
import com.noncore.assessment.util.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理服务接口
 * 定义成绩相关的业务操作方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface GradeService {

    /**
     * 创建成绩记录
     *
     * @param grade 成绩信息
     * @return 创建的成绩记录
     */
    Grade createGrade(Grade grade);

    /**
     * 根据ID获取成绩详情
     *
     * @param gradeId 成绩ID
     * @return 成绩详情
     */
    Grade getGradeById(Long gradeId);

    /**
     * 更新成绩信息
     *
     * @param gradeId 成绩ID
     * @param grade 成绩信息
     * @return 更新后的成绩
     */
    Grade updateGrade(Long gradeId, Grade grade);

    /**
     * 删除成绩记录
     *
     * @param gradeId 成绩ID
     */
    void deleteGrade(Long gradeId);

    /**
     * 获取学生的所有成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Grade> getGradesByStudent(Long studentId);

    /**
     * 分页获取学生成绩
     *
     * @param studentId 学生ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<GradeListItem> getStudentGradesWithPagination(Long studentId, Integer page, Integer size, Long courseId);

    /**
     * 获取作业的所有成绩
     *
     * @param assignmentId 作业ID
     * @return 成绩列表
     */
    List<Grade> getGradesByAssignment(Long assignmentId);

    /**
     * 分页获取作业成绩
     *
     * @param assignmentId 作业ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<Grade> getAssignmentGradesWithPagination(Long assignmentId, Integer page, Integer size);

    /**
     * 获取学生在特定作业的成绩
     *
     * @param studentId 学生ID
     * @param assignmentId 作业ID
     * @return 成绩记录
     */
    Grade getStudentAssignmentGrade(Long studentId, Long assignmentId);

    /**
     * 批量评分
     *
     * @param grades 成绩列表
     * @return 操作结果
     */
    Map<String, Object> batchGrading(List<Grade> grades);

    /**
     * 计算学生平均分
     *
     * @param studentId 学生ID
     * @return 平均分
     */
    BigDecimal calculateStudentAverageScore(Long studentId);

    /**
     * 计算学生在特定课程的平均分
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 平均分
     */
    BigDecimal calculateStudentCourseAverageScore(Long studentId, Long courseId);

    /**
     * 计算作业平均分
     *
     * @param assignmentId 作业ID
     * @return 平均分
     */
    BigDecimal calculateAssignmentAverageScore(Long assignmentId);

    /**
     * 获取作业成绩分布
     *
     * @param assignmentId 作业ID
     * @return 成绩分布统计
     */
    List<Map<String, Object>> getGradeDistribution(Long assignmentId);

    /**
     * 获取课程成绩统计
     *
     * @param courseId 课程ID
     * @return 成绩统计信息
     */
    GradeStatsResponse getCourseGradeStatistics(Long courseId);

    /**
     * 获取学生成绩统计
     *
     * @param studentId 学生ID
     * @return 成绩统计信息
     */
    Map<String, Object> getStudentGradeStatistics(Long studentId);

    /**
     * 获取教师待评分作业
     *
     * @param teacherId 教师ID
     * @param page 页码
     * @param size 每页大小
     * @return 待评分作业列表
     */
    PageResult<Map<String, Object>> getPendingGrades(Long teacherId, Integer page, Integer size);

    /**
     * 发布成绩
     *
     * @param gradeId 成绩ID
     * @return 操作结果
     */
    boolean publishGrade(Long gradeId);

    /**
     * 批量发布成绩
     *
     * @param gradeIds 成绩ID列表
     * @return 操作结果
     */
    Map<String, Object> batchPublishGrades(List<Long> gradeIds);

    /**
     * 获取成绩排名
     *
     * @param courseId 课程ID
     * @param assignmentId 作业ID（可选）
     * @return 成绩排名列表
     */
    List<Map<String, Object>> getGradeRanking(Long courseId, Long assignmentId);

    /**
     * 获取学生排名
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 排名信息
     */
    Map<String, Object> getStudentRanking(Long studentId, Long courseId);

    /**
     * 导出成绩数据
     *
     * @param courseId 课程ID
     * @param assignmentId 作业ID（可选）
     * @param format 导出格式（excel, csv）
     * @return 导出文件信息
     */
    Map<String, Object> exportGrades(Long courseId, Long assignmentId, String format);

    /**
     * 获取成绩趋势数据
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param days 统计天数
     * @return 趋势数据
     */
    List<Map<String, Object>> getGradeTrend(Long studentId, Long courseId, Integer days);

    /**
     * 检查成绩是否可以修改
     *
     * @param gradeId 成绩ID
     * @param userId 用户ID
     * @return 是否可以修改
     */
    boolean canModifyGrade(Long gradeId, Long userId);

    /**
     * 添加成绩评语
     *
     * @param gradeId  成绩ID
     * @param feedback 评语内容
     */
    void addGradeFeedback(Long gradeId, String feedback);

    /**
     * 重新评分
     *
     * @param gradeId  成绩ID
     * @param newScore 新分数
     * @param reason   重评原因
     */
    void regrade(Long gradeId, BigDecimal newScore, String reason);

    /**
     * 获取成绩历史记录
     *
     * @param gradeId 成绩ID
     * @return 历史记录列表
     */
    List<Map<String, Object>> getGradeHistory(Long gradeId);

    /**
     * 计算GPA
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return GPA值
     */
    BigDecimal calculateGPA(Long studentId, Long courseId);

    /**
     * 创建或更新成绩，并可选立即发布
     * @param grade 成绩数据
     * @param publishImmediately 是否立即发布
     * @return 最新成绩
     */
    Grade upsertGradeAndMaybePublish(Grade grade, boolean publishImmediately);

    /**
     * 打回重做：设置状态为 returned，并可设置重交截止时间。
     */
    void returnForResubmission(Long gradeId, Long teacherId, String reason, java.time.LocalDateTime resubmitUntil);
} 