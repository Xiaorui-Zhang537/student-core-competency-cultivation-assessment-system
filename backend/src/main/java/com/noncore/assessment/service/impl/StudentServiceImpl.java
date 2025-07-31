package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final AssignmentMapper assignmentMapper;

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    public StudentServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public List<Assignment> getPendingAssignments(Long studentId) {
        logger.info("获取待处理作业, studentId: {}", studentId);
        return assignmentMapper.findPendingAssignmentsForStudent(studentId); 
    }

    @Override
    @Transactional
    public void createStudentProfile(User user) {
        // This method is now responsible for creating student-specific records
        // after the core user record has been created by AuthService.
        // For now, as there's no separate student_profiles table,
        // we can just log the action.
        logger.info("Initializing student profile for new user, ID: {}", user.getId());
        // In the future, you might add logic here to create entries in
        // other tables like student_abilities, initial learning_plans, etc.
    }
} 