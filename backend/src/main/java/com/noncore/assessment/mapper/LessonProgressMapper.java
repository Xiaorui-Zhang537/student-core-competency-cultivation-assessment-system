package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.LessonProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 学习进度Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface LessonProgressMapper {

    /**
     * 插入学习进度记录
     */
    int insertLessonProgress(LessonProgress lessonProgress);

    /**
     * 根据ID查询学习进度
     */
    LessonProgress selectLessonProgressById(Long id);

    /**
     * 更新学习进度
     */
    int updateLessonProgress(LessonProgress lessonProgress);

    /**
     * 删除学习进度
     */
    int deleteLessonProgress(Long id);

    /**
     * 根据学生和章节查询进度
     */
    LessonProgress selectByStudentAndLesson(@Param("studentId") Long studentId, 
                                           @Param("lessonId") Long lessonId);

    /**
     * 根据学生和课程查询进度列表
     */
    List<LessonProgress> selectByStudentAndCourse(@Param("studentId") Long studentId, 
                                                 @Param("courseId") Long courseId);

    /**
     * 获取最近学习的章节
     */
    List<Map<String, Object>> selectRecentStudiedLessons(@Param("studentId") Long studentId, 
                                                         @Param("limit") Integer limit);

    /**
     * 计算学生总学习时长
     */
    Integer calculateTotalStudyTime(@Param("studentId") Long studentId, 
                                   @Param("courseId") Long courseId);

    /**
     * 计算学生整体进度
     */
    java.math.BigDecimal calculateOverallProgress(@Param("studentId") Long studentId);

    /**
     * 获取学生学习统计
     */
    Map<String, Object> getStudentStudyStats(@Param("studentId") Long studentId);

    /**
     * 计算课程完成率
     */
    java.math.BigDecimal calculateCourseCompletionRate(@Param("studentId") Long studentId, 
                                                      @Param("courseId") Long courseId);

    /**
     * 获取学习热力图数据
     */
    List<Map<String, Object>> getStudyHeatmapData(@Param("studentId") Long studentId, 
                                                  @Param("days") Integer days);

    /**
     * 重置课程进度
     */
    int resetCourseProgress(@Param("studentId") Long studentId, 
                           @Param("courseId") Long courseId);

    /**
     * 更新进度
     */
    int updateProgress(@Param("studentId") Long studentId, 
                      @Param("lessonId") Long lessonId, 
                      @Param("progress") java.math.BigDecimal progress, 
                      @Param("lastPosition") Integer lastPosition);

    /**
     * 标记章节完成
     */
    int markLessonCompleted(@Param("studentId") Long studentId, 
                           @Param("lessonId") Long lessonId);

    /**
     * 更新评分
     */
    int updateRating(@Param("studentId") Long studentId, 
                    @Param("lessonId") Long lessonId, 
                    @Param("rating") Integer rating);

    /**
     * 根据学生、课程和章节查询进度
     */
    LessonProgress selectByStudentCourseLesson(@Param("studentId") Long studentId, 
                                              @Param("courseId") Long courseId, 
                                              @Param("lessonId") Long lessonId);

    int deleteByCourseId(@Param("courseId") Long courseId);

    Long calculateWeeklyStudyTime(@Param("studentId") Long studentId);

    Double getAverageRatingByTeacher(@Param("teacherId") Long teacherId);

    Double getAverageCompletionRateByTeacher(@Param("teacherId") Long teacherId);
} 