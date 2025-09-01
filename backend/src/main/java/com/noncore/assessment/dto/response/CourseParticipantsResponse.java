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
        private String name;
        private String avatar;
        private String role; // teacher | student
    }
}


