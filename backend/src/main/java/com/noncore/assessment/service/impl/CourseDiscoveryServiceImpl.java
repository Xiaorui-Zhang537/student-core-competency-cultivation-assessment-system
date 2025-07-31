package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.service.CourseDiscoveryService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseDiscoveryServiceImpl implements CourseDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(CourseDiscoveryServiceImpl.class);

    private final CourseMapper courseMapper;

    public CourseDiscoveryServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public PageResult<Course> getCourses(Integer page, Integer size, String keyword, String category, String difficulty, String status) {
        logger.info("分页查询课程: page={}, size={}, keyword={}", page, size, keyword);
        PageHelper.startPage(page != null ? page : 1, size != null ? size : 10);
        List<Course> courses = courseMapper.selectCoursesWithPagination(keyword, category, difficulty, status, null);
        PageInfo<Course> pageInfo = new PageInfo<>(courses);
        return PageResult.of(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public List<Course> getPopularCourses(Integer limit) {
        return courseMapper.selectPopularCourses(limit != null ? limit : 10);
    }

    @Override
    public List<Course> getRecommendedCourses(Integer limit) {
        return courseMapper.selectRecommendedCourses(limit != null ? limit : 10);
    }

    @Override
    public List<Course> getCoursesByCategory(String category) {
        return courseMapper.selectCoursesByCategory(category);
    }

    @Override
    public List<Course> searchCourses(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return courseMapper.searchCourses(keyword);
    }
    
    @Override
    public List<Course> getUpcomingCourses(Integer days) {
        return courseMapper.selectUpcomingCourses(days != null ? days : 7);
    }

    @Override
    public List<Course> getEndingCourses(Integer days) {
        return courseMapper.selectEndingCourses(days != null ? days : 7);
    }
} 