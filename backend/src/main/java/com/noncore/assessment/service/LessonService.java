package com.noncore.assessment.service;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.util.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 课程章节服务接口
 * 定义课程章节相关的业务操作方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface LessonService {

    /**
     * 创建新章节
     *
     * @param lesson 章节信息
     * @return 创建的章节
     */
    Lesson createLesson(Lesson lesson);

    /**
     * 根据ID获取章节详情
     *
     * @param lessonId 章节ID
     * @return 章节详情
     */
    Lesson getLessonById(Long lessonId);

    /**
     * 更新章节信息
     *
     * @param lessonId 章节ID
     * @param lesson 章节信息
     * @return 更新后的章节
     */
    Lesson updateLesson(Long lessonId, Lesson lesson);

    /**
     * 删除章节
     *
     * @param lessonId 章节ID
     */
    void deleteLesson(Long lessonId);

    /**
     * 获取课程的所有章节
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<Lesson> getLessonsByCourse(Long courseId);

    /**
     * 分页获取章节列表
     *
     * @param courseId 课程ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<Lesson> getLessonsWithPagination(Long courseId, Integer page, Integer size);

    /**
     * 获取学生的章节进度
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @return 学习进度
     */
    LessonProgress getStudentProgress(Long studentId, Long lessonId);

    /**
     * 更新学生章节进度
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @param progress 进度百分比
     * @param studyTime 学习时长（分钟）
     * @param lastPosition 最后学习位置
     * @return 更新结果
     */
    boolean updateStudentProgress(Long studentId, Long lessonId, BigDecimal progress, 
                                Integer studyTime, Integer lastPosition);

    /**
     * 标记章节完成
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @return 操作结果
     */
    boolean markLessonCompleted(Long studentId, Long lessonId);

    /**
     * 获取学生在课程中的所有章节进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 进度列表
     */
    List<LessonProgress> getStudentCourseProgress(Long studentId, Long courseId);

    /**
     * 计算学生课程整体进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 整体进度百分比
     */
    BigDecimal calculateCourseProgress(Long studentId, Long courseId);

    /**
     * 获取热门章节
     *
     * @param limit 数量限制
     * @return 热门章节列表
     */
    List<Lesson> getPopularLessons(Integer limit);

    /**
     * 搜索章节
     *
     * @param keyword 关键词
     * @param courseId 课程ID（可选）
     * @return 搜索结果
     */
    List<Lesson> searchLessons(String keyword, Long courseId);

    /**
     * 发布章节
     *
     * @param lessonId 章节ID
     * @return 操作结果
     */
    boolean publishLesson(Long lessonId);

    /**
     * 取消发布章节
     *
     * @param lessonId 章节ID
     * @return 操作结果
     */
    boolean unpublishLesson(Long lessonId);

    /**
     * 调整章节顺序
     *
     * @param lessonId 章节ID
     * @param newOrder 新的顺序号
     * @return 操作结果
     */
    boolean updateLessonOrder(Long lessonId, Integer newOrder);

    /**
     * 获取章节统计信息
     *
     * @param courseId 课程ID
     * @return 统计信息
     */
    Map<String, Object> getLessonStatistics(Long courseId);

    /**
     * 批量更新章节状态
     *
     * @param lessonIds 章节ID列表
     * @param status 新状态
     * @return 操作结果
     */
    Map<String, Object> batchUpdateLessonStatus(List<Long> lessonIds, String status);

    /**
     * 复制章节到其他课程
     *
     * @param lessonId 源章节ID
     * @param targetCourseId 目标课程ID
     * @return 复制的章节
     */
    Lesson copyLessonToCourse(Long lessonId, Long targetCourseId);

    /**
     * 获取学生最近学习的章节
     *
     * @param studentId 学生ID
     * @param limit 数量限制
     * @return 最近学习章节列表
     */
    List<Map<String, Object>> getRecentStudiedLessons(Long studentId, Integer limit);

    /**
     * 添加章节笔记
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @param notes 笔记内容
     * @return 操作结果
     */
    boolean addLessonNotes(Long studentId, Long lessonId, String notes);

    /**
     * 为章节评分
     *
     * @param studentId 学生ID
     * @param lessonId 章节ID
     * @param rating 评分（1-5分）
     * @return 操作结果
     */
    boolean rateLessons(Long studentId, Long lessonId, Integer rating);

    // ---- 学生进度聚合补充 ----
    /** 课程整体进度（0-100） */
    Double getOverallProgress(Long studentId, Long courseId);
    /** 总学习时长（分钟） */
    Long getTotalStudyMinutes(Long studentId, Long courseId);
    /** 本周学习时长（分钟） */
    Long getWeeklyStudyMinutes(Long studentId);
    /** 最近学习章节标题 */
    String getLastStudiedLessonTitle(Long studentId);
} 