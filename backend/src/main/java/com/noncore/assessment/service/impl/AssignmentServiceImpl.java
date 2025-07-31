package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.SubmissionMapper;
import com.noncore.assessment.service.AssignmentService;
import com.noncore.assessment.util.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作业服务实现类
 * 实现作业相关的业务逻辑
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;

    public AssignmentServiceImpl(AssignmentMapper assignmentMapper, SubmissionMapper submissionMapper) {
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        logger.info("创建新作业: {}", assignment.getTitle());

        // 验证输入参数
        validateAssignment(assignment);

        // 设置创建时间
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setStatus("draft"); // 默认为草稿状态
        assignment.setSubmissionCount(0);

        int result = assignmentMapper.insertAssignment(assignment);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建作业失败");
        }

        logger.info("作业创建成功: ID={}, 标题={}", assignment.getId(), assignment.getTitle());
        return assignment;
    }

    @Override
    public Assignment updateAssignment(Long assignmentId, Assignment assignment) {
        logger.info("更新作业: ID={}", assignmentId);

        // 验证作业是否存在
        Assignment existingAssignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (existingAssignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 验证输入参数
        validateAssignment(assignment);

        // 更新作业信息
        assignment.setId(assignmentId);
        assignment.setUpdatedAt(LocalDateTime.now());
        // 保持原有的统计数据
        assignment.setSubmissionCount(existingAssignment.getSubmissionCount());
        assignment.setCreatedAt(existingAssignment.getCreatedAt());

        int result = assignmentMapper.updateAssignment(assignment);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新作业失败");
        }

        logger.info("作业更新成功: ID={}, 标题={}", assignmentId, assignment.getTitle());
        return assignmentMapper.selectAssignmentById(assignmentId);
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        logger.info("删除作业: ID={}", assignmentId);

        // 验证作业是否存在
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 检查是否有提交记录
        if (submissionMapper.countByAssignmentId(assignmentId) > 0) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED, "作业已有提交记录，无法删除");
        }

        int result = assignmentMapper.deleteAssignment(assignmentId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除作业失败");
        }

        logger.info("作业删除成功: ID={}", assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        return assignment;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Assignment> getAssignments(Integer page, Integer size, Long courseId, 
                                                Long teacherId, String status, String keyword) {
        logger.info("分页查询作业: page={}, size={}, courseId={}", page, size, courseId);

        // 设置分页参数
        PageHelper.startPage(page != null ? page : 1, size != null ? size : 10);

        // 执行查询
        List<Assignment> assignments = assignmentMapper.selectAssignmentsWithPagination(
            courseId, teacherId, status, keyword);

        // 构建分页结果
        PageInfo<Assignment> pageInfo = new PageInfo<>(assignments);
        return PageResult.of(
            assignments,
            pageInfo.getPageNum(),
            pageInfo.getPageSize(),
            pageInfo.getTotal(),
            pageInfo.getPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        return assignmentMapper.selectAssignmentsByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsByTeacher(Long teacherId) {
        return assignmentMapper.selectAssignmentsByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsForStudent(Long studentId, Long courseId) {
        return assignmentMapper.selectAssignmentsForStudent(courseId, studentId);
    }

    @Override
    public void publishAssignment(Long assignmentId) {
        logger.info("发布作业: ID={}", assignmentId);
        Assignment assignment = getAssignmentById(assignmentId);

        if ("published".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED, "作业已发布");
        }
        
        updateAssignmentStatus(assignment, "published");
        logger.info("作业发布成功: ID={}", assignmentId);
    }

    @Override
    public void unpublishAssignment(Long assignmentId) {
        logger.info("下架作业: ID={}", assignmentId);
        Assignment assignment = getAssignmentById(assignmentId);
        updateAssignmentStatus(assignment, "draft");
        logger.info("作业下架成功: ID={}", assignmentId);
    }

    @Override
    public void closeAssignment(Long assignmentId) {
        logger.info("关闭作业: ID={}", assignmentId);
        Assignment assignment = getAssignmentById(assignmentId);
        updateAssignmentStatus(assignment, "closed");
        logger.info("作业关闭成功: ID={}", assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> getDueAssignments(Integer days) {
        return assignmentMapper.selectDueAssignments(days != null ? days : 7);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmit(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            return false;
        }
        // 逻辑实现：作业必须是“已发布”状态，并且（未过截止日期 或 允许迟交）
        boolean isPublished = "published".equals(assignment.getStatus());
        boolean isNotExpired = assignment.getDueDate() == null || LocalDateTime.now().isBefore(assignment.getDueDate());
        boolean allowLate = assignment.getAllowLate() != null && assignment.getAllowLate();

        return isPublished && (isNotExpired || allowLate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAssignmentStatistics(Long teacherId, Long courseId) {
        List<Map<String, Object>> stats = assignmentMapper.getAssignmentStatistics(teacherId, courseId);
        
        Map<String, Object> result = new HashMap<>();
        int totalAssignments = 0;
        int totalSubmissions = 0;
        int publishedAssignments = 0;
        int draftAssignments = 0;

        for (Map<String, Object> stat : stats) {
            String status = (String) stat.get("status");
            Integer count = (Integer) stat.get("count");
            Integer submissions = (Integer) stat.get("total_submissions");

            totalAssignments += count;
            if ("published".equals(status)) {
                publishedAssignments = count;
            } else if ("draft".equals(status)) {
                draftAssignments = count;
            }
            if (submissions != null) {
                totalSubmissions += submissions;
            }
        }

        result.put("totalAssignments", totalAssignments);
        result.put("publishedAssignments", publishedAssignments);
        result.put("draftAssignments", draftAssignments);
        result.put("totalSubmissions", totalSubmissions);
        result.put("details", stats);

        return result;
    }

    @Override
    public void batchUpdateStatus(List<Long> assignmentIds, String status) {
        logger.info("批量更新作业状态: assignmentIds={}, status={}", assignmentIds, status);

        if (assignmentIds == null || assignmentIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "作业ID列表不能为空");
        }

        int result = assignmentMapper.batchUpdateAssignmentStatus(assignmentIds, status);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "批量更新失败");
        }

        logger.info("批量更新作业状态成功: 影响{}条记录", result);
    }

    @Override
    public void updateSubmissionCount(Long assignmentId, Integer increment) {
        logger.info("更新作业提交计数: assignmentId={}, increment={}", assignmentId, increment);

        int result = assignmentMapper.updateSubmissionCount(assignmentId, increment);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新提交计数失败");
        }

        logger.info("作业提交计数更新成功: assignmentId={}", assignmentId);
    }

    private void updateAssignmentStatus(Assignment assignment, String newStatus) {
        assignment.setStatus(newStatus);
        assignment.setUpdatedAt(LocalDateTime.now());
        if (assignmentMapper.updateAssignment(assignment) <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新作业状态失败: " + newStatus);
        }
    }

    /**
     * 验证作业信息
     */
    private void validateAssignment(Assignment assignment) {
        if (!StringUtils.hasText(assignment.getTitle())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "作业标题不能为空");
        }
        if (!StringUtils.hasText(assignment.getDescription())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "作业描述不能为空");
        }
        if (assignment.getCourseId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "课程ID不能为空");
        }
        if (assignment.getTeacherId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "教师ID不能为空");
        }
        if (assignment.getMaxScore() == null || assignment.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "满分必须大于0");
        }
        if (assignment.getMaxAttempts() != null && assignment.getMaxAttempts() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最大提交次数必须大于0");
        }
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "截止时间不能早于当前时间");
        }
    }
} 