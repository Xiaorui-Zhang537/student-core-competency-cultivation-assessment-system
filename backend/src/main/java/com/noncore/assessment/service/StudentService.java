package com.noncore.assessment.service;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.dto.response.CourseParticipantsResponse;
import com.noncore.assessment.entity.User;
import java.util.List;

public interface StudentService {

    List<Assignment> getPendingAssignments(Long studentId);

    void createStudentProfile(User user);

    /**
     * 获取课程参与者（教师与同学）
     */
    CourseParticipantsResponse getCourseParticipants(Long currentStudentId, Long courseId, String keyword);
} 