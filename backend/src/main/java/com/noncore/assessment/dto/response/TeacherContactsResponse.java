package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 教师联系人聚合返回：按课程分组的学生清单 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherContactsResponse {
    private List<Group> groups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Group {
        private Long courseId;
        private String courseTitle;
        private List<Person> students;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        private Long id;
        private String username;
        private String avatar;
    }
}


