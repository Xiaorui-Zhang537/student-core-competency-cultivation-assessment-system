package com.noncore.assessment.service;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.util.PageResult;
import java.util.List;

public interface CourseDiscoveryService {

    PageResult<Course> getCourses(Integer page, Integer size, String keyword, 
                                 String category, String difficulty, String status);

    List<Course> getPopularCourses(Integer limit);

    List<Course> getRecommendedCourses(Integer limit);

    List<Course> getCoursesByCategory(String category);

    List<Course> searchCourses(String keyword);
    
    List<Course> getUpcomingCourses(Integer days);

    List<Course> getEndingCourses(Integer days);
} 