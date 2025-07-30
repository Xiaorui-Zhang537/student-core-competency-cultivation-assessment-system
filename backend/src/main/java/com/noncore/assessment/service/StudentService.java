package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.LessonProgress;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 学生服务接口
 * 定义学生相关的业务方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface StudentService {

    /**
     * 获取学生仪表板数据
     *
     * @param studentId 学生ID
     * @return 学生仪表板数据
     */
    StudentDashboardResponse getDashboardData(Long studentId);

    /**
     * 获取学生个人资料
     *
     * @param studentId 学生ID
     * @return 学生个人资料
     */
    User getStudentProfile(Long studentId);

    /**
     * 更新学生个人资料
     *
     * @param studentId 学生ID
     * @param profile 个人资料信息
     * @return 更新后的个人资料
     */
    User updateStudentProfile(Long studentId, User profile);

    /**
     * 获取学生的课程列表
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> getStudentCourses(Long studentId);

    /**
     * 获取学生的作业列表
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return 作业列表
     */
    List<Assignment> getStudentAssignments(Long studentId, Long courseId);

    /**
     * 获取学生的待完成作业
     *
     * @param studentId 学生ID
     * @return 待完成作业列表
     */
    List<Assignment> getPendingAssignments(Long studentId);

    /**
     * 获取学生的学习进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 学习进度列表
     */
    List<LessonProgress> getStudentProgress(Long studentId, Long courseId);

    /**
     * 更新学习进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param lessonId 章节ID
     * @param progress 进度百分比
     * @param lastPosition 最后观看位置
     * @return 更新结果
     */
    boolean updateLearningProgress(Long studentId, Long courseId, Long lessonId, 
                                 BigDecimal progress, Integer lastPosition);

    /**
     * 标记章节完成
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param lessonId 章节ID
     * @return 操作结果
     */
    boolean markLessonCompleted(Long studentId, Long courseId, Long lessonId);

    /**
     * 计算学生平均成绩
     *
     * @param studentId 学生ID
     * @return 平均成绩
     */
    BigDecimal calculateAverageGrade(Long studentId);

    /**
     * 获取学生课程平均成绩
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 课程平均成绩
     */
    BigDecimal calculateCourseAverageGrade(Long studentId, Long courseId);

    /**
     * 获取学生学习统计
     *
     * @param studentId 学生ID
     * @return 学习统计数据
     */
    Map<String, Object> getStudentStats(Long studentId);

    /**
     * 获取学生学习时长统计
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return 学习时长（分钟）
     */
    Integer getStudyTimeStats(Long studentId, Long courseId);

    /**
     * 获取学生本周学习时长
     *
     * @param studentId 学生ID
     * @return 本周学习时长（分钟）
     */
    Integer getWeeklyStudyTime(Long studentId);

    /**
     * 计算学生整体学习进度
     *
     * @param studentId 学生ID
     * @return 整体进度百分比
     */
    BigDecimal calculateOverallProgress(Long studentId);

    /**
     * 获取学生最近学习的课程
     *
     * @param studentId 学生ID
     * @param limit 限制数量
     * @return 最近课程列表
     */
    List<Course> getRecentCourses(Long studentId, Integer limit);

    /**
     * 获取学生学习进度数据（用于图表）
     *
     * @param studentId 学生ID
     * @param days 查询天数
     * @return 进度数据列表
     */
    List<Map<String, Object>> getProgressData(Long studentId, Integer days);

    /**
     * 获取学生能力评估概览
     *
     * @param studentId 学生ID
     * @return 能力评估概览
     */
    Map<String, Object> getAbilityOverview(Long studentId);

    /**
     * 添加学习笔记
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @param notes 笔记内容
     * @return 操作结果
     */
    boolean addStudyNotes(Long studentId, Long lessonId, String notes);

    /**
     * 给章节评分
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @param rating 评分（1-5）
     * @return 操作结果
     */
    boolean rateLesion(Long studentId, Long lessonId, Integer rating);

    /**
     * 获取学生学习排名
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return 排名信息
     */
    Map<String, Object> getStudentRanking(Long studentId, Long courseId);

    /**
     * 获取学生学习热力图数据
     *
     * @param studentId 学生ID
     * @param days 查询天数
     * @return 热力图数据
     */
    List<Map<String, Object>> getStudyHeatmapData(Long studentId, Integer days);

    /**
     * 重置课程进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 操作结果
     */
    boolean resetCourseProgress(Long studentId, Long courseId);

    /**
     * 获取学生课程完成度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 完成度百分比
     */
    BigDecimal getCourseCompletionRate(Long studentId, Long courseId);

    /**
     * 检查学生是否有权限访问课程
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否有权限
     */
    boolean hasAccessToCourse(Long studentId, Long courseId);

    /**
     * 检查学生是否有权限访问章节
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @return 是否有权限
     */
    boolean hasAccessToLesson(Long studentId, Long lessonId);
} 