package com.noncore.assessment.service;

import com.noncore.assessment.entity.Report;
import com.noncore.assessment.util.PageResult;

public interface ReportService {
    Report createReport(Report report);
    Report getReport(Long id);
    PageResult<Report> pageReports(String status, Integer page, Integer size);
    void updateStatus(Long id, String status);
}


