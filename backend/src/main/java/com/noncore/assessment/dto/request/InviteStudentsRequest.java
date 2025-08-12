package com.noncore.assessment.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InviteStudentsRequest {
    @NotEmpty(message = "studentIds 不能为空")
    private List<Long> studentIds;
}

