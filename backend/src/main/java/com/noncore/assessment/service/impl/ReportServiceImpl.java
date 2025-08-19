package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.Report;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.ReportMapper;
import com.noncore.assessment.service.ReportService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public Report createReport(Report report) {
        logger.info("创建举报: reporter={}, reported={}", report.getReporterId(), report.getReportedStudentId());
        reportMapper.insert(report);
        return reportMapper.selectById(report.getId());
    }

    @Override
    public Report getReport(Long id) {
        Report r = reportMapper.selectById(id);
        if (r == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "举报不存在");
        return r;
    }

    @Override
    public PageResult<Report> pageReports(String status, Integer page, Integer size) {
        PageHelper.startPage(page, size);
        List<Report> list = reportMapper.page(status, 0, size);
        PageInfo<Report> info = new PageInfo<>(list);
        return PageResult.of(info.getList(), page, size, info.getTotal(), info.getPages());
    }

    @Override
    public void updateStatus(Long id, String status) {
        int n = reportMapper.updateStatus(id, status);
        if (n == 0) throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新举报状态失败");
    }
}


