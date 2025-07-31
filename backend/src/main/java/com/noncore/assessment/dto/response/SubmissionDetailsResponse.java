package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Submission;
import com.noncore.assessment.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDetailsResponse {
    private Submission submission;
    private User student;
    private Assignment assignment;
} 