package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程数据访问接口
 * 处理courses表的CRUD操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface CourseMapper {

    /**
     * 插入新课程
     *
     * @param course 课程对象
     * @return 影响的行数
     */
    int insertCourse(Course course);

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程对象
     */
    Course selectCourseById(@Param("id") Long id);

    /**
     * 更新课程信息
     *
     * @param course 课程对象
     * @return 影响的行数
     */
    int updateCourse(Course course);

    /**
     * 软删除课程
     *
     * @param id 课程ID
     * @return 影响的行数
     */
    int deleteCourse(@Param("id") Long id);

    /**
     * 分页查询课程列表
     *
     * @param keyword 搜索关键词
     * @param category 课程分类
     * @param difficulty 难度级别
     * @param status 课程状态
     * @param teacherId 教师ID（可选）
     * @return 课程列表
     */
    List<Course> selectCoursesWithPagination(@Param("keyword") String keyword,
                                           @Param("category") String category,
                                           @Param("difficulty") String difficulty,
                                           @Param("status") String status,
                                           @Param("teacherId") Long teacherId);

    /**
     * 根据教师ID查询课程
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> selectCoursesByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据学生ID查询已选课程
     *
     * @param studentId 学生ID
     * @return 课程列表
     */
    List<Course> selectCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询热门课程（按报名人数排序）
     *
     * @param limit 限制数量
     * @return 课程列表
     */
    List<Course> selectPopularCourses(@Param("limit") Integer limit);

    /**
     * 查询推荐课程（按评分排序）
     *
     * @param limit 限制数量
     * @return 课程列表
     */
    List<Course> selectRecommendedCourses(@Param("limit") Integer limit);

    /**
     * 根据分类查询课程
     *
     * @param category 课程分类
     * @return 课程列表
     */
    List<Course> selectCoursesByCategory(@Param("category") String category);

    /**
     * 更新课程报名人数
     *
     * @param courseId 课程ID
     * @param increment 增量（正数表示增加，负数表示减少）
     * @return 影响的行数
     */
    int updateEnrollmentCount(@Param("courseId") Long courseId, @Param("increment") Integer increment);

    /**
     * 更新课程评分和评价数量
     *
     * @param courseId 课程ID
     * @param rating 新评分
     * @param reviewCount 评价数量
     * @return 影响的行数
     */
    int updateCourseRating(@Param("courseId") Long courseId, 
                          @Param("rating") Double rating, 
                          @Param("reviewCount") Integer reviewCount);

    /**
     * 统计课程数量
     *
     * @param teacherId 教师ID（可选）
     * @param status 课程状态（可选）
     * @return 课程数量
     */
    int countCourses(@Param("teacherId") Long teacherId, @Param("status") String status);

    /**
     * 检查课程是否存在
     *
     * @param id 课程ID
     * @return 存在数量（0或1）
     */
    int checkCourseExists(@Param("id") Long id);

    /**
     * 根据标题查找课程（用于检查重复）
     *
     * @param title 课程标题
     * @param excludeId 排除的课程ID
     * @return 课程列表
     */
    List<Course> selectCoursesByTitle(@Param("title") String title, @Param("excludeId") Long excludeId);

    /**
     * 批量更新课程状态
     *
     * @param courseIds 课程ID列表
     * @param status 新状态
     * @return 影响的行数
     */
    int batchUpdateCourseStatus(@Param("courseIds") List<Long> courseIds, @Param("status") String status);

    /**
     * 查询即将开始的课程
     *
     * @param days 天数
     * @return 课程列表
     */
    List<Course> selectUpcomingCourses(@Param("days") Integer days);

    /**
     * 查询即将结束的课程
     *
     * @param days 天数
     * @return 课程列表
     */
    List<Course> selectEndingCourses(@Param("days") Integer days);

    /**
     * 根据关键词搜索课程
     *
     * @param keyword 搜索关键词
     * @return 课程列表
     */
    List<Course> searchCourses(@Param("keyword") String keyword);

    /**
     * 获取课程统计信息
     *
     * @param teacherId 教师ID（可选）
     * @return 统计结果Map
     */
    List<java.util.Map<String, Object>> getCourseStatistics(@Param("teacherId") Long teacherId);

    /**
     * 检查课程标题是否存在
     *
     * @param title 课程标题
     * @param excludeId 排除的课程ID（用于编辑时检查）
     * @return 存在的课程数量
     */
    int checkTitleExists(@Param("title") String title, @Param("excludeId") Long excludeId);

    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    long countActiveCoursesByStudent(@Param("studentId") Long studentId);

    List<com.noncore.assessment.dto.response.StudentDashboardResponse.RecentCourseDto> findRecentCoursesByStudent(@Param("studentId") Long studentId, @Param("limit") int limit);

    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

    long countActiveByTeacher(@Param("teacherId") Long teacherId);

    List<com.noncore.assessment.dto.response.TeacherDashboardResponse.ActiveCourseDto> findActiveWithStatsByTeacher(@Param("teacherId") Long teacherId, @Param("limit") int limit);
} 