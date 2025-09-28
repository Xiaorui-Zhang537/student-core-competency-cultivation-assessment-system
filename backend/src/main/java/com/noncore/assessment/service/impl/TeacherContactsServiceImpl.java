package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.TeacherContactsResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.TeacherContactsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TeacherContactsServiceImpl implements TeacherContactsService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public TeacherContactsServiceImpl(CourseMapper courseMapper, UserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public TeacherContactsResponse listContactsByCourses(Long teacherId, String keyword) {
        List<Course> courses = courseMapper.selectCoursesByTeacherId(teacherId);
        List<TeacherContactsResponse.Group> groups = new ArrayList<>();
        String kw = (keyword == null || keyword.isBlank()) ? null : ("%" + keyword.trim() + "%");
        for (Course c : courses) {
            if (!Objects.equals(teacherId, c.getTeacherId())) continue;
            List<User> students = (kw == null)
                    ? userMapper.selectStudentsByCourseId(c.getId())
                    : userMapper.selectStudentsByCourseIdWithSearch(c.getId(), kw);
            if (students == null || students.isEmpty()) continue;
            List<TeacherContactsResponse.Person> persons = new ArrayList<>();
            for (User u : students) {
                final String nick = u.getNickname();
                final String guessName = (nick != null && !nick.isBlank()) ? nick : u.getUsername();
                persons.add(TeacherContactsResponse.Person.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .nickname(nick)
                        .displayName(guessName)
                        .avatar(u.getAvatar())
                        .build());
            }
            groups.add(TeacherContactsResponse.Group.builder()
                    .courseId(c.getId())
                    .courseTitle(c.getTitle())
                    .students(persons)
                    .build());
        }
        return TeacherContactsResponse.builder().groups(groups).build();
    }
}


