package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.service.admin.AdminAbilityReportService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员-能力报告服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional(readOnly = true)
public class AdminAbilityReportServiceImpl implements AdminAbilityReportService {

    private final AbilityReportMapper abilityReportMapper;

    public AdminAbilityReportServiceImpl(AbilityReportMapper abilityReportMapper) {
        this.abilityReportMapper = abilityReportMapper;
    }

    @Override
    public PageResult<AbilityReport> pageReports(int page,
                                                 int size,
                                                 Long studentId,
                                                 String reportType,
                                                 Boolean isPublished,
                                                 Long courseId,
                                                 Long assignmentId,
                                                 Long submissionId,
                                                 LocalDateTime start,
                                                 LocalDateTime end) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = (p - 1) * s;

        List<AbilityReport> items = abilityReportMapper.selectAdminReports(
                studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end, offset, s
        );
        long total = safeInt(abilityReportMapper.countAdminReports(studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end));
        int totalPages = (int) Math.ceil(total / (double) s);
        return PageResult.of(items, p, s, total, totalPages);
    }

    @Override
    public AbilityReport getReport(Long reportId) {
        AbilityReport r = abilityReportMapper.selectReportById(reportId);
        if (r == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "能力报告不存在");
        return r;
    }

    private long safeInt(Integer v) {
        return v == null ? 0L : v.longValue();
    }
}

