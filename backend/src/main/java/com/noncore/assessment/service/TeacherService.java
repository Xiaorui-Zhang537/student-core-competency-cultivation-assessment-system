package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.TeacherDashboardResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.util.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 教师服务接口
 * 定义教师相关的业务方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface TeacherService {

    /**
     * 获取教师仪表板数据
     *
     * @param teacherId 教师ID
     * @return 教师仪表板数据
     */
    TeacherDashboardResponse getDashboardData(Long teacherId);

    /**
     * 获取教师个人资料
     *
     * @param teacherId 教师ID
     * @return 教师个人资料
     */
    User getTeacherProfile(Long teacherId);

    /**
     * 更新教师个人资料
     *
     * @param teacherId 教师ID
     * @param profile 个人资料信息
     * @return 更新后的个人资料
     */
    User updateTeacherProfile(Long teacherId, User profile);

    /**
     * 获取教师统计数据
     *
     * @param teacherId 教师ID
     * @return 教师统计数据
     */
    Map<String, Object> getTeacherStats(Long teacherId);

    /**
     * 获取教师的课程列表
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> getTeacherCourses(Long teacherId);

    /**
     * 获取教师的活跃课程列表
     *
     * @param teacherId 教师ID
     * @return 活跃课程列表
     */
    List<Course> getActiveCourses(Long teacherId);

    /**
     * 获取学生概览列表
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @param status 学生状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 学生概览分页结果
     */
    PageResult<User> getStudentOverview(Long teacherId, Long courseId, String status, Integer page, Integer size);

    /**
     * 获取特定学生的详细进度报告
     *
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param courseId 课程ID（可选）
     * @return 学生进度报告
     */
    Map<String, Object> getStudentProgressReport(Long teacherId, Long studentId, Long courseId);

    /**
     * 获取课程分析数据
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param timeRange 时间范围
     * @return 课程分析数据
     */
    Map<String, Object> getCourseAnalytics(Long teacherId, Long courseId, String timeRange);

    /**
     * 获取教师所有课程的管理数据
     *
     * @param teacherId 教师ID
     * @return 课程管理数据列表
     */
    List<Map<String, Object>> getCourseManagementData(Long teacherId);

    /**
     * 获取作业分析数据
     *
     * @param teacherId 教师ID
     * @param assignmentId 作业ID
     * @return 作业分析数据
     */
    Map<String, Object> getAssignmentAnalytics(Long teacherId, Long assignmentId);

    /**
     * 获取班级整体表现数据
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param timeRange 时间范围
     * @return 班级表现数据
     */
    Map<String, Object> getClassPerformance(Long teacherId, Long courseId, String timeRange);

    /**
     * 获取待评分作业列表
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @return 待评分作业列表
     */
    List<Map<String, Object>> getPendingGradings(Long teacherId, Long courseId);

    /**
     * 批量评分作业
     *
     * @param teacherId 教师ID
     * @param grades 成绩列表
     * @return 评分结果
     */
    Map<String, Object> batchGradeAssignments(Long teacherId, List<Grade> grades);

    /**
     * 发布课程
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 操作结果
     */
    boolean publishCourse(Long teacherId, Long courseId);

    /**
     * 取消发布课程
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 操作结果
     */
    boolean unpublishCourse(Long teacherId, Long courseId);

    /**
     * 归档课程
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 操作结果
     */
    boolean archiveCourse(Long teacherId, Long courseId);

    /**
     * 获取课程学生列表
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param page 页码
     * @param size 每页大小
     * @return 学生列表分页结果
     */
    PageResult<User> getCourseStudents(Long teacherId, Long courseId, Integer page, Integer size);

    /**
     * 移除课程学生
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 操作结果
     */
    boolean removeStudentFromCourse(Long teacherId, Long courseId, Long studentId);

    /**
     * 批量添加学生到课程
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param studentIds 学生ID列表
     * @return 操作结果
     */
    Map<String, Object> addStudentsToCourse(Long teacherId, Long courseId, List<Long> studentIds);

    /**
     * 获取教师日程安排
     *
     * @param teacherId 教师ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程安排列表
     */
    List<Map<String, Object>> getTeacherSchedule(Long teacherId, String startDate, String endDate);

    /**
     * 获取教师通知列表
     *
     * @param teacherId 教师ID
     * @param type 通知类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 通知列表分页结果
     */
    PageResult<Map<String, Object>> getTeacherNotifications(Long teacherId, String type, Integer page, Integer size);

    /**
     * 发送通知给学生
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @param studentIds 学生ID列表（可选）
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 发送结果
     */
    Map<String, Object> sendNotificationToStudents(Long teacherId, Long courseId, List<Long> studentIds, 
                                                  String title, String content, String type);

    /**
     * 获取教学数据分析
     *
     * @param teacherId 教师ID
     * @param timeRange 时间范围
     * @return 教学数据分析
     */
    Map<String, Object> getTeachingAnalytics(Long teacherId, String timeRange);

    /**
     * 生成教学报告
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID（可选）
     * @param reportType 报告类型
     * @param timeRange 时间范围
     * @return 教学报告数据
     */
    Map<String, Object> generateTeachingReport(Long teacherId, Long courseId, String reportType, String timeRange);

    /**
     * 导出学生成绩
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param format 导出格式
     * @return 导出文件信息
     */
    Map<String, Object> exportStudentGrades(Long teacherId, Long courseId, String format);

    /**
     * 获取课程统计数据
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 课程统计数据
     */
    Map<String, Object> getCourseStatistics(Long teacherId, Long courseId);

    /**
     * 获取作业提交统计
     *
     * @param teacherId 教师ID
     * @param assignmentId 作业ID
     * @return 提交统计数据
     */
    Map<String, Object> getAssignmentSubmissionStats(Long teacherId, Long assignmentId);

    /**
     * 检查教师是否有权限访问课程
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @return 是否有权限
     */
    boolean hasAccessToCourse(Long teacherId, Long courseId);

    /**
     * 检查教师是否有权限访问作业
     *
     * @param teacherId 教师ID
     * @param assignmentId 作业ID
     * @return 是否有权限
     */
    boolean hasAccessToAssignment(Long teacherId, Long assignmentId);

    /**
     * 检查教师是否有权限管理学生
     *
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否有权限
     */
    boolean hasAccessToStudent(Long teacherId, Long studentId, Long courseId);

    /**
     * 获取教师评价统计
     *
     * @param teacherId 教师ID
     * @return 评价统计数据
     */
    Map<String, Object> getTeacherRatingStats(Long teacherId);

    /**
     * 获取学生学习活跃度
     *
     * @param teacherId 教师ID
     * @param courseId 课程ID
     * @param timeRange 时间范围
     * @return 学生活跃度数据
     */
    List<Map<String, Object>> getStudentActivityData(Long teacherId, Long courseId, String timeRange);
} 