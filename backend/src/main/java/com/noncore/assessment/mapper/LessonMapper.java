package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Lesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程章节Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface LessonMapper {

    /**
     * 插入章节记录
     */
    int insertLesson(Lesson lesson);

    /**
     * 根据ID查询章节
     */
    Lesson selectLessonById(Long id);

    /**
     * 更新章节
     */
    int updateLesson(Lesson lesson);

    /**
     * 删除章节
     */
    int deleteLesson(Long id);

    /**
     * 根据课程ID查询章节列表
     */
    List<Lesson> selectLessonsByCourseId(Long courseId);

    /**
     * 分页查询章节
     */
    List<Lesson> selectLessonsWithPagination(@Param("courseId") Long courseId, 
                                           @Param("offset") int offset, 
                                           @Param("size") Integer size);

    /**
     * 统计课程章节数量
     */
    Integer countLessonsByCourse(Long courseId);

    /**
     * 根据课程和状态统计章节数量
     */
    Integer countLessonsByCourseAndStatus(@Param("courseId") Long courseId, 
                                        @Param("status") String status);

    /**
     * 获取课程中最大排序号
     */
    Integer getMaxSortOrderByCourse(@Param("courseId") Long courseId);

    /**
     * 更新章节状态
     */
    int updateLessonStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新章节排序
     */
    int updateLessonOrder(@Param("id") Long id, 
                         @Param("oldOrder") Integer oldOrder, 
                         @Param("newOrder") Integer newOrder);

    /**
     * 获取热门章节
     */
    List<Lesson> selectPopularLessons(@Param("limit") int limit);

    /**
     * 搜索章节
     */
    List<Lesson> searchLessons(@Param("keyword") String keyword, 
                              @Param("courseId") Long courseId);
} 