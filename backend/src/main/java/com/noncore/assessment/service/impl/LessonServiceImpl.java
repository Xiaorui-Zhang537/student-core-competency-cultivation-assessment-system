package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.LessonMapper;
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.service.LessonService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程章节服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class LessonServiceImpl implements LessonService {

    private static final Logger logger = LoggerFactory.getLogger(LessonServiceImpl.class);

    private final LessonMapper lessonMapper;
    private final LessonProgressMapper lessonProgressMapper;

    public LessonServiceImpl(LessonMapper lessonMapper, LessonProgressMapper lessonProgressMapper) {
        this.lessonMapper = lessonMapper;
        this.lessonProgressMapper = lessonProgressMapper;
    }

    @Override
    public Lesson createLesson(Lesson lesson) {
        try {
            logger.info("创建新章节: {}", lesson.getTitle());
            
            // 设置创建时间和默认值
            lesson.setCreatedAt(LocalDateTime.now());
            lesson.setUpdatedAt(LocalDateTime.now());
            lesson.setDeleted(false);
            
            // 设置默认状态为草稿
            if (lesson.getStatus() == null) {
                lesson.setStatus("draft");
            }
            
            // 设置默认排序号
            if (lesson.getSortOrder() == null) {
                Integer maxOrder = lessonMapper.getMaxSortOrderByCourse(lesson.getCourseId());
                lesson.setSortOrder(maxOrder != null ? maxOrder + 1 : 1);
            }
            
            int result = lessonMapper.insertLesson(lesson);
            if (result > 0) {
                logger.info("章节创建成功，ID: {}", lesson.getId());
                return lesson;
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建章节失败");
            }
            
        } catch (Exception e) {
            logger.error("创建章节失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建章节失败: " + e.getMessage());
        }
    }

    @Override
    public Lesson getLessonById(Long lessonId) {
        logger.info("获取章节详情，ID: {}", lessonId);
        Lesson lesson = lessonMapper.selectLessonById(lessonId);
        
        if (lesson == null) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
        
        return lesson;
    }

    @Override
    public Lesson updateLesson(Long lessonId, Lesson lesson) {
        try {
            logger.info("更新章节信息，ID: {}", lessonId);
            
            // 检查章节是否存在
            Lesson existingLesson = lessonMapper.selectLessonById(lessonId);
            if (existingLesson == null) {
                throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
            }
            
            // 更新字段
            lesson.setId(lessonId);
            lesson.setUpdatedAt(LocalDateTime.now());
            
            int result = lessonMapper.updateLesson(lesson);
            if (result > 0) {
                logger.info("章节更新成功，ID: {}", lessonId);
                return lessonMapper.selectLessonById(lessonId);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新章节失败");
            }
            
        } catch (Exception e) {
            logger.error("更新章节失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新章节失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteLesson(Long lessonId) {
        try {
            logger.info("删除章节，ID: {}", lessonId);
            
            // 检查章节是否存在
            Lesson lesson = lessonMapper.selectLessonById(lessonId);
            if (lesson == null) {
                throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
            }
            
            // 软删除
            int result = lessonMapper.deleteLesson(lessonId);
            if (result > 0) {
                logger.info("章节删除成功，ID: {}", lessonId);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除章节失败");
            }
            
        } catch (Exception e) {
            logger.error("删除章节失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除章节失败: " + e.getMessage());
        }
    }

    @Override
    public List<Lesson> getLessonsByCourse(Long courseId) {
        logger.info("获取课程章节列表，课程ID: {}", courseId);
        return lessonMapper.selectLessonsByCourseId(courseId);
    }

    @Override
    public PageResult<Lesson> getLessonsWithPagination(Long courseId, Integer page, Integer size) {
        logger.info("分页获取章节列表，课程ID: {}, 页码: {}, 每页大小: {}", courseId, page, size);
        
        int offset = (page - 1) * size;
        List<Lesson> lessons = lessonMapper.selectLessonsWithPagination(courseId, offset, size);
        Integer total = lessonMapper.countLessonsByCourse(courseId);
        
        return PageResult.of(lessons, page, size, total.longValue(), (total + size - 1) / size);
    }

    @Override
    public LessonProgress getStudentProgress(Long studentId, Long lessonId) {
        logger.info("获取学生章节进度，学生ID: {}, 章节ID: {}", studentId, lessonId);
        return lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
    }

    @Override
    public boolean updateStudentProgress(Long studentId, Long lessonId, BigDecimal progress, 
                                       Integer studyTime, Integer lastPosition) {
        try {
            logger.info("更新学生章节进度，学生ID: {}, 章节ID: {}, 进度: {}%", studentId, lessonId, progress);
            
            // 查找现有进度记录
            LessonProgress existingProgress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);

            boolean b = progress != null && progress.compareTo(new BigDecimal("100")) >= 0;
            if (existingProgress != null) {
                // 更新现有记录
                existingProgress.setProgress(progress);
                existingProgress.setStudyTime((existingProgress.getStudyTime() != null ? 
                    existingProgress.getStudyTime() : 0) + (studyTime != null ? studyTime : 0));
                existingProgress.setLastPosition(lastPosition);
                existingProgress.setLastStudiedAt(LocalDateTime.now());
                existingProgress.setUpdatedAt(LocalDateTime.now());
                
                // 如果进度达到100%，标记为完成
                if (b) {
                    existingProgress.setCompleted(true);
                    existingProgress.setCompletedAt(LocalDateTime.now());
                }
                
                return lessonProgressMapper.updateLessonProgress(existingProgress) > 0;
            } else {
                // 创建新记录
                LessonProgress newProgress = new LessonProgress();
                newProgress.setStudentId(studentId);
                newProgress.setLessonId(lessonId);
                newProgress.setProgress(progress != null ? progress : BigDecimal.ZERO);
                newProgress.setStudyTime(studyTime != null ? studyTime : 0);
                newProgress.setLastPosition(lastPosition);
                newProgress.setCompleted(b);
                newProgress.setLastStudiedAt(LocalDateTime.now());
                newProgress.setCreatedAt(LocalDateTime.now());
                newProgress.setUpdatedAt(LocalDateTime.now());
                
                if (newProgress.getCompleted()) {
                    newProgress.setCompletedAt(LocalDateTime.now());
                }
                
                return lessonProgressMapper.insertLessonProgress(newProgress) > 0;
            }
            
        } catch (Exception e) {
            logger.error("更新学生章节进度失败", e);
            return false;
        }
    }

    @Override
    public boolean markLessonCompleted(Long studentId, Long lessonId) {
        try {
            logger.info("标记章节完成，学生ID: {}, 章节ID: {}", studentId, lessonId);
            return updateStudentProgress(studentId, lessonId, new BigDecimal("100"), null, null);
        } catch (Exception e) {
            logger.error("标记章节完成失败", e);
            return false;
        }
    }

    @Override
    public List<LessonProgress> getStudentCourseProgress(Long studentId, Long courseId) {
        logger.info("获取学生课程进度，学生ID: {}, 课程ID: {}", studentId, courseId);
        return lessonProgressMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public BigDecimal calculateCourseProgress(Long studentId, Long courseId) {
        try {
            logger.info("计算学生课程整体进度，学生ID: {}, 课程ID: {}", studentId, courseId);
            
            // 获取课程所有章节
            List<Lesson> lessons = lessonMapper.selectLessonsByCourseId(courseId);
            if (lessons.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            // 获取学生进度
            List<LessonProgress> progressList = lessonProgressMapper.selectByStudentAndCourse(studentId, courseId);
            Map<Long, LessonProgress> progressMap = progressList.stream()
                .collect(Collectors.toMap(LessonProgress::getLessonId, p -> p));
            
            // 计算平均进度
            BigDecimal totalProgress = BigDecimal.ZERO;
            for (Lesson lesson : lessons) {
                LessonProgress progress = progressMap.get(lesson.getId());
                if (progress != null && progress.getProgress() != null) {
                    totalProgress = totalProgress.add(progress.getProgress());
                }
            }
            
            return totalProgress.divide(new BigDecimal(lessons.size()), 2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            logger.error("计算课程整体进度失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public List<Lesson> getPopularLessons(Integer limit) {
        logger.info("获取热门章节，限制数量: {}", limit);
        return lessonMapper.selectPopularLessons(limit != null ? limit : 10);
    }

    @Override
    public List<Lesson> searchLessons(String keyword, Long courseId) {
        logger.info("搜索章节，关键词: {}, 课程ID: {}", keyword, courseId);
        return lessonMapper.searchLessons(keyword, courseId);
    }

    @Override
    public boolean publishLesson(Long lessonId) {
        try {
            logger.info("发布章节，ID: {}", lessonId);
            int result = lessonMapper.updateLessonStatus(lessonId, "published");
            return result > 0;
        } catch (Exception e) {
            logger.error("发布章节失败", e);
            return false;
        }
    }

    @Override
    public boolean unpublishLesson(Long lessonId) {
        try {
            logger.info("取消发布章节，ID: {}", lessonId);
            int result = lessonMapper.updateLessonStatus(lessonId, "draft");
            return result > 0;
        } catch (Exception e) {
            logger.error("取消发布章节失败", e);
            return false;
        }
    }

    @Override
    public boolean updateLessonOrder(Long lessonId, Integer newOrder) {
        try {
            logger.info("调整章节顺序，ID: {}, 新顺序: {}", lessonId, newOrder);
            int result = lessonMapper.updateLessonOrder(lessonId, 0, newOrder); // 传递旧顺序为0作为占位符
            return result > 0;
        } catch (Exception e) {
            logger.error("调整章节顺序失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getLessonStatistics(Long courseId) {
        logger.info("获取章节统计信息，课程ID: {}", courseId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总章节数
        Integer totalLessons = lessonMapper.countLessonsByCourse(courseId);
        stats.put("totalLessons", totalLessons);
        
        // 已发布章节数
        Integer publishedLessons = lessonMapper.countLessonsByCourseAndStatus(courseId, "published");
        stats.put("publishedLessons", publishedLessons);
        
        // 草稿章节数
        Integer draftLessons = lessonMapper.countLessonsByCourseAndStatus(courseId, "draft");
        stats.put("draftLessons", draftLessons);
        
        // 总观看次数（模拟数据）
        stats.put("totalViews", totalLessons * 150);
        
        // 平均完成率（模拟数据）
        stats.put("averageCompletionRate", 75.5);
        
        return stats;
    }

    @Override
    public Map<String, Object> batchUpdateLessonStatus(List<Long> lessonIds, String status) {
        logger.info("批量更新章节状态，章节数量: {}, 新状态: {}", lessonIds.size(), status);
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long lessonId : lessonIds) {
            try {
                int updateResult = lessonMapper.updateLessonStatus(lessonId, status);
                if (updateResult > 0) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("更新章节ID " + lessonId + " 失败");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("更新章节ID " + lessonId + " 失败: " + e.getMessage());
                logger.error("批量更新章节状态失败", e);
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public Lesson copyLessonToCourse(Long lessonId, Long targetCourseId) {
        try {
            logger.info("复制章节到其他课程，源章节ID: {}, 目标课程ID: {}", lessonId, targetCourseId);
            
            // 获取源章节
            Lesson sourceLesson = lessonMapper.selectLessonById(lessonId);
            if (sourceLesson == null) {
                throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
            }
            
            // 创建新章节
            Lesson newLesson = new Lesson();
            newLesson.setCourseId(targetCourseId);
            newLesson.setTitle(sourceLesson.getTitle() + " (复制)");
            newLesson.setContent(sourceLesson.getContent());
            newLesson.setVideoUrl(sourceLesson.getVideoUrl());
            newLesson.setDuration(sourceLesson.getDuration());
            newLesson.setStatus("draft"); // 复制的章节默认为草稿状态
            
            return createLesson(newLesson);
            
        } catch (Exception e) {
            logger.error("复制章节失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "复制章节失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getRecentStudiedLessons(Long studentId, Integer limit) {
        logger.info("获取学生最近学习章节，学生ID: {}, 限制数量: {}", studentId, limit);
        
        // 由于selectRecentStudiedLessons返回的是List<Map<String, Object>>，直接使用
        try {
            return lessonProgressMapper.selectRecentStudiedLessons(studentId, limit != null ? limit : 5);
        } catch (Exception e) {
            logger.error("获取最近学习章节失败", e);
            // 返回空列表作为fallback
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addLessonNotes(Long studentId, Long lessonId, String notes) {
        try {
            logger.info("添加章节笔记，学生ID: {}, 章节ID: {}", studentId, lessonId);
            
            // 查找或创建进度记录
            LessonProgress progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
            if (progress == null) {
                // 创建新的进度记录
                updateStudentProgress(studentId, lessonId, BigDecimal.ZERO, null, null);
                progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
            }
            
            // 更新笔记
            progress.setNotes(notes);
            progress.setUpdatedAt(LocalDateTime.now());
            
            return lessonProgressMapper.updateLessonProgress(progress) > 0;
            
        } catch (Exception e) {
            logger.error("添加章节笔记失败", e);
            return false;
        }
    }

    @Override
    public boolean rateLessons(Long studentId, Long lessonId, Integer rating) {
        try {
            logger.info("为章节评分，学生ID: {}, 章节ID: {}, 评分: {}", studentId, lessonId, rating);
            
            if (rating < 1 || rating > 5) {
                throw new BusinessException(ErrorCode.RATING_OUT_OF_RANGE);
            }
            
            // 查找或创建进度记录
            LessonProgress progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
            if (progress == null) {
                updateStudentProgress(studentId, lessonId, BigDecimal.ZERO, null, null);
                progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
            }
            
            // 更新评分
            progress.setRating(rating);
            progress.setUpdatedAt(LocalDateTime.now());
            
            return lessonProgressMapper.updateLessonProgress(progress) > 0;
            
        } catch (Exception e) {
            logger.error("章节评分失败", e);
            return false;
        }
    }
} 