package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作业提交实体类
 * 对应数据库中的submissions表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content; // 提交内容
    private String fileName; // 提交文件名
    private String filePath; // 文件存储路径
    @Builder.Default
    private Integer submissionCount = 1; // 第几次提交
    @Builder.Default
    private String status = "submitted"; // submitted, graded, returned
    private LocalDateTime submittedAt;
    @Builder.Default
    private Boolean isLate = false; // 是否迟交
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 冗余字段，用于显示
    private String assignmentTitle;
    private String studentName;
    private String courseName;
    
    /**
     * 检查是否已批改
     */
    public boolean isGraded() {
        return "graded".equals(this.status);
    }

    /**
     * 检查是否已提交
     */
    public boolean isSubmitted() {
        return "submitted".equals(this.status) || "graded".equals(this.status);
    }
} 