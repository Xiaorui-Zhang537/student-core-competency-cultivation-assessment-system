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
public class CourseStudentBasicResponse {
    private Long courseId;
    private String courseTitle;
    private Long total;
    private Integer page;
    private Integer size;
    private List<CourseStudentBasicItem> items;
}


