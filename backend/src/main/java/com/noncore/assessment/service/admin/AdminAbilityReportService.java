package com.noncore.assessment.service.admin;

import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.util.PageResult;

import java.time.LocalDateTime;

/**
 * 管理员-能力报告服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminAbilityReportService {

    PageResult<AbilityReport> pageReports(int page,
                                          int size,
                                          String search,
                                          Long studentId,
                                          String reportType,
                                          Boolean isPublished,
                                          Long courseId,
                                          Long assignmentId,
                                          Long submissionId,
                                          LocalDateTime start,
                                          LocalDateTime end);

    AbilityReport getReport(Long reportId);
}

