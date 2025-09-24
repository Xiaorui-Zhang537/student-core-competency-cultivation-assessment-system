package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseParticipantsResponse {
    private List<Person> teachers;
    private List<Person> classmates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        private Long id;
        private String name;      // 显示名（默认 username）
        private String username;  // 登录名/用户名
        private String nickname;  // 昵称
        private String avatar;
        private String role; // teacher | student
        private String email;
        private String phone;
        private String school;
        private String subject;
        private String studentNo;
        private String teacherNo;
        private String birthday;   // yyyy-MM-dd
        private String country;
        private String province;
        private String city;
        private String gender;
        private String mbti;
        private String bio;
    }
}


