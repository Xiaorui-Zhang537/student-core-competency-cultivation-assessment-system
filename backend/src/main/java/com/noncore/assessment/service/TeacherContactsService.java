package com.noncore.assessment.service;

import com.noncore.assessment.dto.response.TeacherContactsResponse;

public interface TeacherContactsService {
    TeacherContactsResponse listContactsByCourses(Long teacherId, String keyword);
}


