package com.noncore.assessment.service;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.User;
import java.util.List;

public interface StudentService {

    List<Assignment> getPendingAssignments(Long studentId);

    void createStudentProfile(User user);
} 