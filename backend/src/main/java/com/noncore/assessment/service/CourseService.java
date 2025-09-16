package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.CourseStatisticsResponse;
import com.noncore.assessment.entity.Course;
import java.util.List;

/**
 * 课程服务接口
 * 定义课程相关的业务逻辑操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface CourseService {

    /**
     * 创建新课程
     *
     * @param course 课程信息
     * @return 创建的课程
     */
    Course createCourse(Course course);

    /**
     * 更新课程信息
     *
     * @param courseId 课程ID
     * @param course 更新的课程信息
     * @return 更新后的课程
     */
    Course updateCourse(Long courseId, Course course);

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     */
    void deleteCourse(Long courseId);

    /**
     * 根据ID获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    Course getCourseById(Long courseId);

    /**
     * 获取教师的课程列表
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> getCoursesByTeacher(Long teacherId);

    /**
     * 发布课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID，用于权限验证
     */
    void publishCourse(Long courseId, Long teacherId);

    /**
     * 下架课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID，用于权限验证
     */
    void unpublishCourse(Long courseId, Long teacherId);

    /**
     * 归档课程
     *
     * @param courseId 课程ID
     * @param teacherId 教师ID，用于权限验证
     */
    void archiveCourse(Long courseId, Long teacherId);

    /**
     * 更新课程评分
     *
     * @param courseId 课程ID
     * @param newRating 新评分
     * @param newReviewCount 新评价数量
     */
    void updateCourseRating(Long courseId, Double newRating, Integer newReviewCount);

    /**
     * 获取课程统计信息
     *
     * @param teacherId 教师ID（可选）
     * @return 统计信息
     */
    CourseStatisticsResponse getCourseStatistics(Long teacherId);

    /**
     * 检查课程标题是否重复
     *
     * @param title 课程标题
     * @param excludeId 排除的课程ID
     * @return 是否重复
     */
    boolean isTitleDuplicate(String title, Long excludeId);

    /**
     * 批量更新课程状态
     *
     * @param courseIds 课程ID列表
     * @param status 新状态
     */
    void batchUpdateStatus(List<Long> courseIds, String status);

    /**
     * 教师设置课程入课密钥与开关。仅课程教师可设置。
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param require 是否需要密钥
     * @param plaintextKey 明文密钥（可为空；为空时仅关闭开关或清空密钥）
     */
    void setCourseEnrollKey(Long courseId, Long teacherId, boolean require, String plaintextKey);
} 