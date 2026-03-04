package com.noncore.assessment.service;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.util.PageResult;

public interface CourseDiscoveryService {

    PageResult<Course> getCourses(Integer page, Integer size, String keyword,
                                 String category, String difficulty, String status, Long teacherId);
}
