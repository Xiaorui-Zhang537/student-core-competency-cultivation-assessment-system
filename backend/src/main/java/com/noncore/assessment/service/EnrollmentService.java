package com.noncore.assessment.service;

import com.noncore.assessment.entity.User;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.dto.response.StudentCourseResponse;
import java.util.List;

public interface EnrollmentService {
    
    void enrollCourse(Long courseId, Long studentId);

    void unenrollCourse(Long courseId, Long studentId);

    boolean isStudentEnrolled(Long courseId, Long studentId);

    PageResult<User> getCourseStudents(Long teacherId, Long courseId, Integer page, Integer size, String search, String sortBy, String activity, String grade, String progress);

    void removeStudentFromCourse(Long teacherId, Long courseId, Long studentId);

    void addStudentsToCourse(Long teacherId, Long courseId, List<Long> studentIds);

    List<Course> getEnrolledCourses(Long studentId);

    /**
     * 分页获取学生的课程（包含进度与教师名）
     */
    PageResult<StudentCourseResponse> getStudentCoursesPaged(Long studentId, Integer page, Integer size, String keyword);

    /**
     * 教师重置某学生在某课程下的学习进度
     * 将 lesson_progress 以及 enrollments.progress 归零
     */
    void resetStudentCourseProgress(Long teacherId, Long courseId, Long studentId);
} 