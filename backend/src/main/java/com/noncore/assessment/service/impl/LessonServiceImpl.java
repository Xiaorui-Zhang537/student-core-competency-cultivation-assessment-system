package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.LessonMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.FileRecordMapper;
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

@Service
@Transactional
public class LessonServiceImpl implements LessonService {

    private static final Logger logger = LoggerFactory.getLogger(LessonServiceImpl.class);

    private final LessonMapper lessonMapper;
    private final FileRecordMapper fileRecordMapper;
    private final LessonProgressMapper lessonProgressMapper;
    private final EnrollmentMapper enrollmentMapper;

    public LessonServiceImpl(LessonMapper lessonMapper, LessonProgressMapper lessonProgressMapper, FileRecordMapper fileRecordMapper, EnrollmentMapper enrollmentMapper) {
        this.lessonMapper = lessonMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.fileRecordMapper = fileRecordMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public Lesson createLesson(Lesson lesson) {
        logger.info("创建新章节: {}", lesson.getTitle());
        // 字段互补：确保 description 与 content 同步（后端为真理）
        try {
            normalizeLessonTextFields(lesson);
        } catch (Exception ignore) {}
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        lesson.setDeleted(false);
        if (lesson.getStatus() == null) {
            lesson.setStatus("draft");
        }
        // 统一默认排序：数据库使用 order_index，实体存在 orderIndex 与 sortOrder
        Integer nextOrder = null;
        if (lesson.getOrderIndex() == null && lesson.getSortOrder() == null) {
            Integer maxOrder = lessonMapper.getMaxSortOrderByCourse(lesson.getCourseId());
            nextOrder = (maxOrder != null ? maxOrder + 1 : 1);
        }
        if (lesson.getOrderIndex() == null) {
            // 优先用计算值，否则继承已有 sortOrder，否则置 1
            lesson.setOrderIndex(nextOrder != null ? nextOrder : (lesson.getSortOrder() != null ? lesson.getSortOrder() : 1));
        }
        if (lesson.getSortOrder() == null) {
            // 双字段保持一致，便于向后兼容
            lesson.setSortOrder(lesson.getOrderIndex());
        }
        int result = lessonMapper.insertLesson(lesson);
        if (result > 0) {
            logger.info("章节创建成功，ID: {}", lesson.getId());
            return lesson;
        } else {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建章节失败");
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
        logger.info("更新章节信息，ID: {}", lessonId);
        Lesson existingLesson = lessonMapper.selectLessonById(lessonId);
        if (existingLesson == null) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
        // 字段互补：若仅提供其中一项，自动同步到另一项
        try {
            normalizeLessonTextFields(lesson);
        } catch (Exception ignore) {}
        lesson.setId(lessonId);
        lesson.setUpdatedAt(LocalDateTime.now());
        int result = lessonMapper.updateLesson(lesson);
        if (result > 0) {
            logger.info("章节更新成功，ID: {}", lessonId);
            return lessonMapper.selectLessonById(lessonId);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新章节失败");
        }
    }

    @Override
    public void deleteLesson(Long lessonId) {
        logger.info("删除章节，ID: {}", lessonId);
        Lesson lesson = lessonMapper.selectLessonById(lessonId);
        if (lesson == null) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
        // 先清理 lesson_materials 关联，避免残留引用
        try {
            fileRecordMapper.deleteLessonMaterialsByLessonId(lessonId);
        } catch (Exception e) {
            logger.warn("清理 lesson_materials 关联失败(lessonId={})，继续删除章节", lessonId, e);
        }
        int result = lessonMapper.deleteLesson(lessonId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除章节失败");
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
    public boolean updateStudentProgress(Long studentId, Long lessonId, BigDecimal progress, Integer studyTime, Integer lastPosition) {
        logger.info("更新学生章节进度，学生ID: {}, 章节ID: {}, 进度: {}%", studentId, lessonId, progress);
        LessonProgress existingProgress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        // 确保 courseId 贯通（用于课程层面的聚合统计与筛选）
        Long courseId = null;
        try {
            Lesson l = lessonMapper.selectLessonById(lessonId);
            if (l != null) courseId = l.getCourseId();
        } catch (Exception ignore) { }
        boolean completed = progress != null && progress.compareTo(new BigDecimal("100")) >= 0;
        if (existingProgress != null) {
            existingProgress.setProgress(progress);
            existingProgress.setStudyTime((existingProgress.getStudyTime() != null ? existingProgress.getStudyTime() : 0) + (studyTime != null ? studyTime : 0));
            existingProgress.setLastPosition(lastPosition);
            existingProgress.setLastStudiedAt(LocalDateTime.now());
            existingProgress.setUpdatedAt(LocalDateTime.now());
            if (completed) {
                existingProgress.setCompleted(true);
                existingProgress.setCompletedAt(LocalDateTime.now());
            }
            if (existingProgress.getCourseId() == null && courseId != null) {
                existingProgress.setCourseId(courseId);
            }
            boolean ok = lessonProgressMapper.updateLessonProgress(existingProgress) > 0;
            // 同步更新 enrollments.progress（基于课程完成率聚合）
            if (ok && courseId != null) {
                try {
                    java.math.BigDecimal rate = lessonProgressMapper.calculateCourseCompletionRate(studentId, courseId);
                    double pct = rate == null ? 0.0 : rate.doubleValue();
                    enrollmentMapper.updateProgress(studentId, courseId, pct);
                } catch (Exception ignore) {}
            }
            return ok;
        } else {
            LessonProgress np = new LessonProgress();
            np.setStudentId(studentId);
            np.setLessonId(lessonId);
            np.setProgress(progress != null ? progress : BigDecimal.ZERO);
            np.setStudyTime(studyTime != null ? studyTime : 0);
            np.setLastPosition(lastPosition);
            np.setCompleted(completed);
            np.setLastStudiedAt(LocalDateTime.now());
            np.setCreatedAt(LocalDateTime.now());
            np.setUpdatedAt(LocalDateTime.now());
            if (np.getCompleted()) {
                np.setCompletedAt(LocalDateTime.now());
            }
            if (courseId != null) {
                np.setCourseId(courseId);
            }
            boolean ok = lessonProgressMapper.insertLessonProgress(np) > 0;
            if (ok && courseId != null) {
                try {
                    java.math.BigDecimal rate = lessonProgressMapper.calculateCourseCompletionRate(studentId, courseId);
                    double pct = rate == null ? 0.0 : rate.doubleValue();
                    enrollmentMapper.updateProgress(studentId, courseId, pct);
                } catch (Exception ignore) {}
            }
            return ok;
        }
    }

    @Override
    public boolean markLessonCompleted(Long studentId, Long lessonId) {
        logger.info("标记章节完成，学生ID: {}, 章节ID: {}", studentId, lessonId);
        return updateStudentProgress(studentId, lessonId, new BigDecimal("100"), null, null);
    }

    @Override
    public List<LessonProgress> getStudentCourseProgress(Long studentId, Long courseId) {
        logger.info("获取学生课程进度，学生ID: {}, 课程ID: {}", studentId, courseId);
        return lessonProgressMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public BigDecimal calculateCourseProgress(Long studentId, Long courseId) {
        logger.info("计算学生课程整体进度(加权)，学生ID: {}, 课程ID: {}", studentId, courseId);
        java.math.BigDecimal weighted = lessonProgressMapper.calculateCourseOverallProgress(studentId, courseId);
        return weighted == null ? BigDecimal.ZERO : weighted.setScale(2, RoundingMode.HALF_UP);
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
        logger.info("发布章节，ID: {}", lessonId);
        int result = lessonMapper.updateLessonStatus(lessonId, "published");
        if (result > 0) return true;
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "发布章节失败");
    }

    @Override
    public boolean unpublishLesson(Long lessonId) {
        logger.info("取消发布章节，ID: {}", lessonId);
        int result = lessonMapper.updateLessonStatus(lessonId, "draft");
        if (result > 0) return true;
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "取消发布章节失败");
    }

    @Override
    public boolean updateLessonOrder(Long lessonId, Integer newOrder) {
        logger.info("调整章节顺序，ID: {}, 新顺序: {}", lessonId, newOrder);
        int result = lessonMapper.updateLessonOrder(lessonId, 0, newOrder);
        if (result > 0) return true;
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "调整章节顺序失败");
    }

    @Override
    public Map<String, Object> getLessonStatistics(Long courseId) {
        logger.info("获取章节统计信息，课程ID: {}", courseId);
        Map<String, Object> stats = new HashMap<>();
        Integer totalLessons = lessonMapper.countLessonsByCourse(courseId);
        stats.put("totalLessons", totalLessons);
        Integer publishedLessons = lessonMapper.countLessonsByCourseAndStatus(courseId, "published");
        stats.put("publishedLessons", publishedLessons);
        Integer draftLessons = lessonMapper.countLessonsByCourseAndStatus(courseId, "draft");
        stats.put("draftLessons", draftLessons);
        stats.put("totalViews", totalLessons * 150);
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
                if (updateResult > 0) successCount++; else { failCount++; errors.add("更新章节ID " + lessonId + " 失败"); }
            } catch (Exception e) {
                failCount++;
                errors.add("更新章节ID " + lessonId + " 失败: " + e.getMessage());
            }
        }
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    public Lesson copyLessonToCourse(Long lessonId, Long targetCourseId) {
        logger.info("复制章节到其他课程，源章节ID: {}, 目标课程ID: {}", lessonId, targetCourseId);
        Lesson sourceLesson = lessonMapper.selectLessonById(lessonId);
        if (sourceLesson == null) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }
        Lesson newLesson = new Lesson();
        newLesson.setCourseId(targetCourseId);
        newLesson.setTitle(sourceLesson.getTitle() + " (复制)");
        newLesson.setContent(sourceLesson.getContent());
        newLesson.setVideoUrl(sourceLesson.getVideoUrl());
        newLesson.setDuration(sourceLesson.getDuration());
        newLesson.setStatus("draft");
        return createLesson(newLesson);
    }

    @Override
    public List<Map<String, Object>> getRecentStudiedLessons(Long studentId, Integer limit) {
        logger.info("获取学生最近学习章节，学生ID: {}, 限制数量: {}", studentId, limit);
        try {
            return lessonProgressMapper.selectRecentStudiedLessons(studentId, limit != null ? limit : 5);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addLessonNotes(Long studentId, Long lessonId, String notes) {
        logger.info("添加章节笔记，学生ID: {}, 章节ID: {}", studentId, lessonId);
        LessonProgress progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        if (progress == null) {
            updateStudentProgress(studentId, lessonId, BigDecimal.ZERO, null, null);
            progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        }
        progress.setNotes(notes);
        progress.setUpdatedAt(LocalDateTime.now());
        return lessonProgressMapper.updateLessonProgress(progress) > 0;
    }

    @Override
    public String getLessonNotes(Long studentId, Long lessonId) {
        logger.info("获取章节笔记，学生ID: {}, 章节ID: {}", studentId, lessonId);
        LessonProgress progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        if (progress == null) {
            return "";
        }
        return progress.getNotes() == null ? "" : progress.getNotes();
    }

    @Override
    public boolean rateLessons(Long studentId, Long lessonId, Integer rating) {
        logger.info("为章节评分，学生ID: {}, 章节ID: {}, 评分: {}", studentId, lessonId, rating);
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.RATING_OUT_OF_RANGE);
        }
        LessonProgress progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        if (progress == null) {
            updateStudentProgress(studentId, lessonId, BigDecimal.ZERO, null, null);
            progress = lessonProgressMapper.selectByStudentAndLesson(studentId, lessonId);
        }
        progress.setRating(rating);
        progress.setUpdatedAt(LocalDateTime.now());
        return lessonProgressMapper.updateLessonProgress(progress) > 0;
    }

    // ---- 学生进度聚合补充 ----
    @Override
    public Double getOverallProgress(Long studentId, Long courseId) {
        return Optional.ofNullable(calculateCourseProgress(studentId, courseId))
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    @Override
    public Long getTotalStudyMinutes(Long studentId, Long courseId) {
        return Optional.ofNullable(lessonProgressMapper.calculateTotalStudyTime(studentId, courseId))
                .map(Integer::longValue)
                .orElse(0L);
    }

    @Override
    public Long getWeeklyStudyMinutes(Long studentId) {
        return Optional.ofNullable(lessonProgressMapper.calculateWeeklyStudyTime(studentId)).orElse(0L);
    }

    @Override
    public String getLastStudiedLessonTitle(Long studentId) {
        List<Map<String, Object>> recent = lessonProgressMapper.selectRecentStudiedLessons(studentId, 1);
        if (recent == null || recent.isEmpty()) return "";
        Object t = recent.get(0).get("lessonTitle");
        return t == null ? "" : String.valueOf(t);
    }

    // ---- 资料关联（方案A）----
    @Override
    public void replaceLessonMaterials(Long lessonId, java.util.List<Long> fileIds, Long operatorId) {
        logger.info("替换节次资料，lessonId={}, files={}", lessonId, fileIds);
        // 简单安全校验：文件必须存在
        if (fileIds != null && !fileIds.isEmpty()) {
            java.util.List<com.noncore.assessment.entity.FileRecord> records = fileRecordMapper.selectByIds(fileIds);
            if (records.size() != fileIds.size()) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "存在无效的文件ID");
            }
        }
        // 清空并重建关联
        fileRecordMapper.deleteLessonMaterialsByLessonId(lessonId);
        if (fileIds != null && !fileIds.isEmpty()) {
            fileRecordMapper.insertLessonMaterials(lessonId, fileIds);
        }
    }

    @Override
    public java.util.List<com.noncore.assessment.entity.FileRecord> getLessonMaterials(Long lessonId) {
        try {
            // 先走更稳健的两段式查询：取 file_id 列表，再查 file_records
            java.util.List<Long> fileIds = fileRecordMapper.selectFileIdsByLessonId(lessonId);
            if (fileIds != null && !fileIds.isEmpty()) {
                java.util.List<com.noncore.assessment.entity.FileRecord> viaIds = fileRecordMapper.selectByIds(fileIds);
                if (viaIds != null && !viaIds.isEmpty()) return viaIds;
            }
            // 退化为 JOIN 方案
            java.util.List<com.noncore.assessment.entity.FileRecord> list = fileRecordMapper.selectByLessonId(lessonId);
            if (list == null || list.isEmpty()) {
                // 若关联表为空（例如历史数据或尚未迁移场景），回退到旧查询方式
                return fileRecordMapper.selectByPurposeAndRelatedId("lesson_material", lessonId);
            }
            return list;
        } catch (Exception e) {
            // 兼容回退：如果关联表不存在或执行异常，回退到旧查询方式
            logger.warn("lesson_materials 查询失败，回退到 file_records.related_type=lesson_material，lessonId={}", lessonId, e);
            return fileRecordMapper.selectByPurposeAndRelatedId("lesson_material", lessonId);
        }
    }

    /**
     * 统一说明与简介字段：当 description 或 content 其中之一非空而另一方为空时，进行互补。
     * 避免前端不同入口造成不一致。
     */
    private void normalizeLessonTextFields(Lesson lesson) {
        if (lesson == null) return;
        String desc = lesson.getDescription();
        String cont = lesson.getContent();
        boolean descBlank = desc == null || desc.trim().isEmpty();
        boolean contBlank = cont == null || cont.trim().isEmpty();
        if (!descBlank && contBlank) {
            lesson.setContent(desc);
        } else if (!contBlank && descBlank) {
            lesson.setDescription(cont);
        }
    }
} 