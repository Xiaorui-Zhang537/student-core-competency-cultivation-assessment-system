package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.noncore.assessment.dto.response.CourseStatisticsResponse;

/**
 * 课程服务实现类
 * 实现课程相关的业务逻辑
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;

    public CourseServiceImpl(CourseMapper courseMapper, EnrollmentMapper enrollmentMapper) {
        this.courseMapper = courseMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public Course createCourse(Course course) {
        logger.info("创建新课程: {}", course.getTitle());

        // 验证输入参数
        validateCourse(course);

        // 检查课程标题是否已存在
        if (courseMapper.checkTitleExists(course.getTitle(), null) > 0) {
            throw new BusinessException(ErrorCode.COURSE_TITLE_EXISTS);
        }

        // 设置创建时间
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.setStatus("draft"); // 默认为草稿状态
        course.setEnrollmentCount(0);
        course.setRating(0.0);
        course.setReviewCount(0);

        int result = courseMapper.insertCourse(course);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建课程失败");
        }

        logger.info("课程创建成功: ID={}, 标题={}", course.getId(), course.getTitle());
        return course;
    }

    @Override
    public Course updateCourse(Long courseId, Course course) {
        logger.info("更新课程: ID={}", courseId);

        // 验证课程是否存在
        Course existingCourse = courseMapper.selectCourseById(courseId);
        if (existingCourse == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 验证输入参数
        validateCourse(course);

        // 检查课程标题是否已存在（排除当前课程）
        if (!existingCourse.getTitle().equals(course.getTitle()) && 
            courseMapper.checkTitleExists(course.getTitle(), courseId) > 0) {
            throw new BusinessException(ErrorCode.COURSE_TITLE_EXISTS);
        }

        // 更新课程信息
        course.setId(courseId);
        course.setUpdatedAt(LocalDateTime.now());
        // 保持原有的统计数据
        course.setEnrollmentCount(existingCourse.getEnrollmentCount());
        course.setRating(existingCourse.getRating());
        course.setReviewCount(existingCourse.getReviewCount());
        course.setCreatedAt(existingCourse.getCreatedAt());

        int result = courseMapper.updateCourse(course);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新课程失败");
        }

        logger.info("课程更新成功: ID={}, 标题={}", courseId, course.getTitle());
        return courseMapper.selectCourseById(courseId);
    }

    @Override
    public void deleteCourse(Long courseId) {
        logger.info("删除课程: ID={}", courseId);

        // 验证课程是否存在
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 检查是否有学生选课
        if (enrollmentMapper.countByCourseId(courseId) > 0) {
            throw new BusinessException(ErrorCode.COURSE_HAS_STUDENTS);
        }

        if (courseMapper.deleteCourse(courseId) <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除课程失败");
        }

        logger.info("课程删除成功: ID={}", courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long courseId) {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseMapper.selectCoursesByTeacherId(teacherId);
    }

    @Override
    public void publishCourse(Long courseId, Long teacherId) {
        logger.info("发布课程: ID={}, 操作教师ID={}", courseId, teacherId);
        Course course = checkCourseOwnership(courseId, teacherId);

        if ("published".equals(course.getStatus())) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_PUBLISHED);
        }

        updateStatus(course, "published");
        logger.info("课程发布成功: ID={}", courseId);
    }

    @Override
    public void unpublishCourse(Long courseId, Long teacherId) {
        logger.info("下架课程: ID={}, 操作教师ID={}", courseId, teacherId);
        Course course = checkCourseOwnership(courseId, teacherId);
        updateStatus(course, "draft");
        logger.info("课程下架成功: ID={}", courseId);
    }

    @Override
    public void archiveCourse(Long courseId, Long teacherId) {
        logger.info("归档课程: ID={}, 操作教师ID={}", courseId, teacherId);
        Course course = checkCourseOwnership(courseId, teacherId);
        updateStatus(course, "archived");
        logger.info("课程归档成功: ID={}", courseId);
    }

    @Override
    public void updateCourseRating(Long courseId, Double newRating, Integer newReviewCount) {
        logger.info("更新课程评分: courseId={}, rating={}, reviewCount={}", courseId, newRating, newReviewCount);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // TODO: 此处评分更新逻辑已简化。未来应考虑更复杂的加权平均或重新计算逻辑。
        courseMapper.updateCourseRating(courseId, newRating, newReviewCount);

        logger.info("课程评分更新成功: courseId={}", courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStatisticsResponse getCourseStatistics(Long teacherId) {
        List<Map<String, Object>> stats = courseMapper.getCourseStatistics(teacherId);
        
        int totalCourses = 0;
        int publishedCourses = 0;
        long totalEnrollments = 0; // Use long for safety
        double totalRating = 0.0;
        int coursesWithRating = 0;

        for (Map<String, Object> stat : stats) {
            long count = (long) stat.getOrDefault("count", 0L);
            totalCourses += count;

            if ("published".equals(stat.get("status"))) {
                publishedCourses += count;
            }
            
            Number enrollments = (Number) stat.getOrDefault("total_enrollments", 0);
            totalEnrollments += enrollments.longValue();

            Number avgRating = (Number) stat.getOrDefault("avg_rating", 0.0);
            if (avgRating.doubleValue() > 0) {
                totalRating += avgRating.doubleValue() * count;
                coursesWithRating += count;
            }
        }

        double averageRating = (coursesWithRating > 0) ? totalRating / coursesWithRating : 0.0;

        return CourseStatisticsResponse.builder()
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .totalEnrollments((int) totalEnrollments) // Cast back if DTO requires int
                .averageRating(averageRating)
                .details(stats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTitleDuplicate(String title, Long excludeId) {
        return courseMapper.checkTitleExists(title, excludeId) > 0;
    }

    @Override
    public void batchUpdateStatus(List<Long> courseIds, String status) {
        logger.info("批量更新课程状态: courseIds={}, status={}", courseIds, status);

        if (courseIds == null || courseIds.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_ID_LIST_EMPTY);
        }

        int result = courseMapper.batchUpdateCourseStatus(courseIds, status);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "批量更新失败");
        }

        logger.info("批量更新课程状态成功: 影响{}条记录", result);
    }

    /**
     * 验证课程信息
     */
    private void validateCourse(Course course) {
        if (!StringUtils.hasText(course.getTitle())) {
            throw new BusinessException(ErrorCode.COURSE_TITLE_EMPTY);
        }
        if (!StringUtils.hasText(course.getDescription())) {
            throw new BusinessException(ErrorCode.COURSE_DESCRIPTION_EMPTY);
        }
        if (course.getTeacherId() == null) {
            throw new BusinessException(ErrorCode.TEACHER_ID_EMPTY);
        }
        if (course.getMaxStudents() != null && course.getMaxStudents() <= 0) {
            throw new BusinessException(ErrorCode.MAX_STUDENTS_INVALID);
        }
        if (course.getDuration() != null && course.getDuration() <= 0) {
            throw new BusinessException(ErrorCode.COURSE_DURATION_INVALID);
        }
    }

    private void updateStatus(Course course, String newStatus) {
        course.setStatus(newStatus);
        course.setUpdatedAt(LocalDateTime.now());
        if (courseMapper.updateCourse(course) <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新课程状态失败: " + newStatus);
        }
    }

    private Course checkCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "教师没有权限操作该课程");
        }
        return course;
    }
} 