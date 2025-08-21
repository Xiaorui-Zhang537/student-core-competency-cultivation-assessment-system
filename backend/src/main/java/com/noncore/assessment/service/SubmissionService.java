package com.noncore.assessment.service;

import com.noncore.assessment.entity.Submission;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.dto.request.SubmissionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 作业提交服务接口
 * 定义作业提交相关的业务逻辑操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface SubmissionService {

    /**
     * 提交作业
     *
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param content 提交内容
     * @param file 上传文件（可选）
     * @return 提交记录
     */
    Submission submitAssignment(Long assignmentId, Long studentId, String content, MultipartFile file);

    /**
     * 学生提交作业（JSON流程，fileIds为已上传文件ID列表，取首个作为主附件）
     */
    Submission submitAssignment(Long assignmentId, Long studentId, SubmissionRequest request);

    /**
     * 保存作业草稿
     *
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param content 草稿内容
     * @return 提交记录
     */
    Submission saveDraft(Long assignmentId, Long studentId, String content);

    /**
     * 根据ID获取提交详情
     *
     * @param submissionId 提交ID
     * @return 提交详情
     */
    Submission getSubmissionById(Long submissionId);

    /**
     * 获取学生的某个作业提交
     *
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 提交记录，如果没有则返回null
     */
    Submission getStudentSubmission(Long assignmentId, Long studentId);

    /**
     * 获取学生的所有提交
     *
     * @param studentId 学生ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<Submission> getStudentSubmissions(Long studentId, Integer page, Integer size);

    /**
     * 获取作业的所有提交
     *
     * @param assignmentId 作业ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<Submission> getAssignmentSubmissions(Long assignmentId, Integer page, Integer size);

    /**
     * 获取学生的成绩列表
     *
     * @param studentId 学生ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getStudentGrades(Long studentId, Integer page, Integer size);

    /**
     * 获取提交的成绩详情
     *
     * @param submissionId 提交ID
     * @return 成绩详情
     */
    Map<String, Object> getSubmissionGrade(Long submissionId);

    /**
     * 更新提交状态
     *
     * @param submissionId 提交ID
     * @param status 新状态
     */
    void updateSubmissionStatus(Long submissionId, String status);

    /**
     * 删除提交
     *
     * @param submissionId 提交ID
     */
    void deleteSubmission(Long submissionId);

    /**
     * 检查学生是否已提交作业
     *
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 是否已提交
     */
    boolean hasSubmitted(Long assignmentId, Long studentId);

    /**
     * 获取提交统计信息
     *
     * @param assignmentId 作业ID
     * @return 统计信息
     */
    Map<String, Object> getSubmissionStatistics(Long assignmentId);

    /**
     * 导出提交为ZIP（包含提交文本、附件与评分摘要）
     *
     * @param submissionId 提交ID
     * @return ZIP字节数组
     */
    byte[] exportSubmissionZip(Long submissionId);
} 