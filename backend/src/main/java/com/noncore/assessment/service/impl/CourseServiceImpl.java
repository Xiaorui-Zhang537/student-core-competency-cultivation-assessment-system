package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.util.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public PageResult<Course> getCourses(Integer page, Integer size, String keyword, 
                                        String category, String difficulty, String status) {
        logger.info("分页查询课程: page={}, size={}, keyword={}", page, size, keyword);

        // 设置分页参数
        PageHelper.startPage(page != null ? page : 1, size != null ? size : 10);

        // 执行查询
        List<Course> courses = courseMapper.selectCoursesWithPagination(
            keyword, category, difficulty, status, null);

        // 构建分页结果
        PageInfo<Course> pageInfo = new PageInfo<>(courses);
        return PageResult.of(
            courses,
            pageInfo.getPageNum(),
            pageInfo.getPageSize(),
            pageInfo.getTotal(),
            pageInfo.getPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseMapper.selectCoursesByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getEnrolledCourses(Long studentId) {
        return courseMapper.selectCoursesByStudentId(studentId);
    }

    @Override
    public void enrollCourse(Long courseId, Long studentId) {
        logger.info("学生选课: courseId={}, studentId={}", courseId, studentId);

        // 验证课程是否存在且可选
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!"published".equals(course.getStatus())) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }
        if (course.isFull()) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }

        // 检查是否已选课
        if (isStudentEnrolled(courseId, studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }

        // 添加选课记录
        int result = enrollmentMapper.insertEnrollment(studentId, courseId, "enrolled", LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "选课失败");
        }

        // 更新课程报名人数
        courseMapper.updateEnrollmentCount(courseId, 1);

        logger.info("选课成功: courseId={}, studentId={}", courseId, studentId);
    }

    @Override
    public void unenrollCourse(Long courseId, Long studentId) {
        logger.info("学生退课: courseId={}, studentId={}", courseId, studentId);

        // 检查是否已选课
        if (!isStudentEnrolled(courseId, studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED);
        }

        // 删除选课记录
        int result = enrollmentMapper.deleteEnrollment(studentId, courseId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "退课失败");
        }

        // 更新课程报名人数
        courseMapper.updateEnrollmentCount(courseId, -1);

        logger.info("退课成功: courseId={}, studentId={}", courseId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        return enrollmentMapper.checkEnrollmentExists(studentId, courseId) > 0;
    }

    @Override
    public void publishCourse(Long courseId) {
        logger.info("发布课程: ID={}", courseId);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if ("published".equals(course.getStatus())) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_PUBLISHED);
        }

        course.setStatus("published");
        course.setUpdatedAt(LocalDateTime.now());
        int result = courseMapper.updateCourse(course);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发布课程失败");
        }

        logger.info("课程发布成功: ID={}", courseId);
    }

    @Override
    public void unpublishCourse(Long courseId) {
        logger.info("下架课程: ID={}", courseId);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        course.setStatus("draft");
        course.setUpdatedAt(LocalDateTime.now());
        int result = courseMapper.updateCourse(course);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "下架课程失败");
        }

        logger.info("课程下架成功: ID={}", courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getPopularCourses(Integer limit) {
        return courseMapper.selectPopularCourses(limit != null ? limit : 10);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getRecommendedCourses(Integer limit) {
        return courseMapper.selectRecommendedCourses(limit != null ? limit : 10);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(String category) {
        return courseMapper.selectCoursesByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> searchCourses(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return courseMapper.searchCourses(keyword);
    }

    @Override
    public void updateCourseRating(Long courseId, Double rating) {
        logger.info("更新课程评分: courseId={}, rating={}", courseId, rating);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 简化评分更新逻辑（实际应该根据所有评分计算平均值）
        int newReviewCount = course.getReviewCount() + 1;
        courseMapper.updateCourseRating(courseId, rating, newReviewCount);

        logger.info("课程评分更新成功: courseId={}", courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseStatistics(Long teacherId) {
        List<Map<String, Object>> stats = courseMapper.getCourseStatistics(teacherId);
        
        Map<String, Object> result = new HashMap<>();
        int totalCourses = 0;
        int totalEnrollments = 0;
        double totalRating = 0.0;
        int publishedCourses = 0;

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("status");
            Integer count = (Integer) stat.get("count");
            Double avgRating = (Double) stat.get("avg_rating");
            Integer enrollments = (Integer) stat.get("total_enrollments");

            totalCourses += count;
            if ("published".equals(status)) {
                publishedCourses = count;
                if (avgRating != null) {
                    totalRating = avgRating;
                }
            }
            if (enrollments != null) {
                totalEnrollments += enrollments;
            }
        }

        result.put("totalCourses", totalCourses);
        result.put("publishedCourses", publishedCourses);
        result.put("totalEnrollments", totalEnrollments);
        result.put("averageRating", totalRating);
        result.put("details", stats);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getUpcomingCourses(Integer days) {
        return courseMapper.selectUpcomingCourses(days != null ? days : 7);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getEndingCourses(Integer days) {
        return courseMapper.selectEndingCourses(days != null ? days : 7);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTitleDuplicate(String title, Long excludeId) {
        List<Course> courses = courseMapper.selectCoursesByTitle(title, excludeId);
        return courses != null && !courses.isEmpty();
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
} 