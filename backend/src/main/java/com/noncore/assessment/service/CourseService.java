package com.noncore.assessment.service;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.util.PageResult;

import java.util.List;
import java.util.Map;

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
     * 分页查询课程列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param difficulty 难度级别
     * @param status 课程状态
     * @return 分页结果
     */
    PageResult<Course> getCourses(Integer page, Integer size, String keyword, 
                                 String category, String difficulty, String status);

    /**
     * 获取教师的课程列表
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> getCoursesByTeacher(Long teacherId);

    /**
     * 获取学生的已选课程
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> getEnrolledCourses(Long studentId);

    /**
     * 学生选课
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     */
    void enrollCourse(Long courseId, Long studentId);

    /**
     * 学生退课
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     */
    void unenrollCourse(Long courseId, Long studentId);

    /**
     * 检查学生是否已选课
     *
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 是否已选课
     */
    boolean isStudentEnrolled(Long courseId, Long studentId);

    /**
     * 发布课程
     *
     * @param courseId 课程ID
     */
    void publishCourse(Long courseId);

    /**
     * 下架课程
     *
     * @param courseId 课程ID
     */
    void unpublishCourse(Long courseId);

    /**
     * 获取热门课程
     *
     * @param limit 限制数量
     * @return 课程列表
     */
    List<Course> getPopularCourses(Integer limit);

    /**
     * 获取推荐课程
     *
     * @param limit 限制数量
     * @return 课程列表
     */
    List<Course> getRecommendedCourses(Integer limit);

    /**
     * 根据分类获取课程
     *
     * @param category 课程分类
     * @return 课程列表
     */
    List<Course> getCoursesByCategory(String category);

    /**
     * 搜索课程
     *
     * @param keyword 搜索关键词
     * @return 课程列表
     */
    List<Course> searchCourses(String keyword);

    /**
     * 更新课程评分
     *
     * @param courseId 课程ID
     * @param rating 新评分
     */
    void updateCourseRating(Long courseId, Double rating);

    /**
     * 获取课程统计信息
     *
     * @param teacherId 教师ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getCourseStatistics(Long teacherId);

    /**
     * 获取即将开始的课程
     *
     * @param days 天数
     * @return 课程列表
     */
    List<Course> getUpcomingCourses(Integer days);

    /**
     * 获取即将结束的课程
     *
     * @param days 天数
     * @return 课程列表
     */
    List<Course> getEndingCourses(Integer days);

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
} 