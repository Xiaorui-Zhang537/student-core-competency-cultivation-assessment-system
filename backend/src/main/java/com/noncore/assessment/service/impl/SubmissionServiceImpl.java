package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.FileRecord;
import com.noncore.assessment.entity.Submission;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.SubmissionMapper;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.service.SubmissionService;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.dto.request.SubmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 作业提交服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class SubmissionServiceImpl implements SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    private final SubmissionMapper submissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final FileStorageService fileStorageService;
    private final GradeMapper gradeMapper;
    private final AbilityReportMapper abilityReportMapper;

    public SubmissionServiceImpl(SubmissionMapper submissionMapper, AssignmentMapper assignmentMapper,
                                 FileStorageService fileStorageService, GradeMapper gradeMapper,
                                 AbilityReportMapper abilityReportMapper) {
        this.submissionMapper = submissionMapper;
        this.assignmentMapper = assignmentMapper;
        this.fileStorageService = fileStorageService;
        this.gradeMapper = gradeMapper;
        this.abilityReportMapper = abilityReportMapper;
    }

    @Override
    public Submission submitAssignment(Long assignmentId, Long studentId, String content, MultipartFile file) {
        logger.info("学生提交作业，作业ID: {}, 学生ID: {}", assignmentId, studentId);

        // 检查作业是否存在和可提交
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        if (!"published".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_PUBLISHED);
        }

        // 截止时间：考虑“打回重做”的个性化重交截止
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveDeadline = assignment.getDueDate();
        try {
            Grade sg = gradeMapper.selectByStudentAndAssignment(studentId, assignmentId);
            if (sg != null && "returned".equalsIgnoreCase(String.valueOf(sg.getStatus())) && sg.getResubmitUntil() != null) {
                effectiveDeadline = sg.getResubmitUntil();
            }
        } catch (Exception ignored) {}
        // 规则：只要在截止时间之前都可以重复提交；若无截止时间（如课程作业），视为永不过期
        boolean isLate = false;
        if (effectiveDeadline != null) {
            isLate = now.isAfter(effectiveDeadline);
            if (isLate && !assignment.getAllowLate()) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_EXPIRED);
            }
        }

        // 检查是否已提交
        Submission existingSubmission = submissionMapper.selectByAssignmentAndStudent(assignmentId, studentId);
        
        Submission submission;
        boolean isNewSubmission = false;
        if (existingSubmission != null) {
            // 更新现有提交
            submission = existingSubmission;
            submission.setContent(content);
            int currentCount = submission.getSubmissionCount() == null ? 0 : submission.getSubmissionCount();
            submission.setSubmissionCount(currentCount + 1);
            submission.setStatus("submitted");  // 重新提交后状态变为已提交
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIsLate(isLate);
            submission.setUpdatedAt(LocalDateTime.now());
        } else {
            // 创建新提交
            isNewSubmission = true;
            submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(content);
            submission.setStatus("submitted");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIsLate(isLate);
            submission.setSubmissionCount(1);
            submission.setCreatedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
        }

        // 处理文件上传
        if (file != null && !file.isEmpty()) {
            if (fileStorageService != null) {
                FileRecord fileRecord = fileStorageService.uploadFile(file, studentId, "submission", assignmentId);
                submission.setFileName(fileRecord.getOriginalName());
                submission.setFilePath(fileRecord.getFilePath());
            } else {
                logger.warn("文件存储服务不可用，跳过文件上传");
            }
        }

        // 保存提交记录
        if (existingSubmission != null) {
            submissionMapper.updateSubmission(submission);
        } else {
            submissionMapper.insertSubmission(submission);
        }
        
        // 如果是全新的提交，更新作业的提交计数
        if(isNewSubmission) {
            assignmentMapper.updateSubmissionCount(assignmentId, 1);
        }

        logger.info("作业提交成功，提交ID: {}", submission.getId());
        return submission;
    }

    @Override
    public Submission submitAssignment(Long assignmentId, Long studentId, SubmissionRequest request) {
        logger.info("学生提交作业(JSON)，作业ID: {}, 学生ID: {}", assignmentId, studentId);

        // 参数解构
        String content = request != null ? request.getContent() : null;
        Long fileId = null;
        if (request != null && request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileId = request.getFileIds().get(0);
        }

        // 检查作业是否存在和可提交
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        if (!"published".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_PUBLISHED);
        }
        // 截止时间：考虑“打回重做”的个性化重交截止
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime effectiveDeadline = assignment.getDueDate();
        try {
            Grade sg = gradeMapper.selectByStudentAndAssignment(studentId, assignmentId);
            if (sg != null && "returned".equalsIgnoreCase(String.valueOf(sg.getStatus())) && sg.getResubmitUntil() != null) {
                effectiveDeadline = sg.getResubmitUntil();
            }
        } catch (Exception ignored) {}
        // 规则：只要在截止时间之前都可以重复提交；若无截止时间（如课程作业），视为永不过期
        boolean isLate = false;
        if (effectiveDeadline != null) {
            isLate = now.isAfter(effectiveDeadline);
            if (isLate && !assignment.getAllowLate()) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_EXPIRED);
            }
        }

        // 查是否已有提交
        Submission existingSubmission = submissionMapper.selectByAssignmentAndStudent(assignmentId, studentId);

        Submission submission;
        boolean isNewSubmission = false;
        if (existingSubmission != null) {
            submission = existingSubmission;
            submission.setContent(content);
            int currentCount = submission.getSubmissionCount() == null ? 0 : submission.getSubmissionCount();
            submission.setSubmissionCount(currentCount + 1);
            submission.setStatus("submitted");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIsLate(isLate);
            submission.setUpdatedAt(LocalDateTime.now());
        } else {
            isNewSubmission = true;
            submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(content);
            submission.setStatus("submitted");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIsLate(isLate);
            submission.setSubmissionCount(1);
            submission.setCreatedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
        }

        // 若传入 fileIds，从文件记录表读取首个文件信息回填名称与路径（保持最小改动兼容现有Submission字段）
        if (fileId != null) {
            try {
                // 直接通过 mapper 读取，避免强耦合 service
                FileRecord fileRecord = null;
                // 轻量查询：通过 FileStorageService 暂无直接方法，改为简单复用私有查询逻辑由 mapper 完成
                // 由于当前类未持有 FileRecordMapper，引入更改较大，采用委托 fileStorageService.getFileInfo
                fileRecord = fileStorageService.getFileInfo(fileId);
                if (fileRecord != null) {
                    submission.setFileName(fileRecord.getOriginalName());
                    submission.setFilePath(fileRecord.getFilePath());
                }
            } catch (Exception e) {
                logger.warn("回填文件信息失败，fileId={}", fileId, e);
            }
        }

        if (existingSubmission != null) {
            submissionMapper.updateSubmission(submission);
        } else {
            submissionMapper.insertSubmission(submission);
        }

        if (isNewSubmission) {
            assignmentMapper.updateSubmissionCount(assignmentId, 1);
        }

        // 将预上传的所有文件重绑到具体的 submissionId，目的为 submission
        if (request != null && request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            try {
                fileStorageService.relinkFilesTo(request.getFileIds(), "submission", submission.getId(), studentId);
            } catch (Exception e) {
                logger.warn("重绑定提交文件失败 submissionId={}, fileIds={}", submission.getId(), request.getFileIds(), e);
            }
        }

        logger.info("作业提交成功(JSON)，提交ID: {}", submission.getId());
        return submission;
    }

    @Override
    public Submission saveDraft(Long assignmentId, Long studentId, String content) {
        logger.info("保存作业草稿，作业ID: {}, 学生ID: {}", assignmentId, studentId);

        // 检查是否已有提交记录
        Submission existingSubmission = submissionMapper.selectByAssignmentAndStudent(assignmentId, studentId);

        Submission submission;
        // 若已存在且不是草稿，判断是否为“已打回”场景；若已打回，则允许转为草稿并更新内容
        if (existingSubmission != null && !"draft".equalsIgnoreCase(String.valueOf(existingSubmission.getStatus()))) {
            try {
                Grade sg = gradeMapper.selectByStudentAndAssignment(studentId, assignmentId);
                if (sg != null && "returned".equalsIgnoreCase(String.valueOf(sg.getStatus()))) {
                    submission = existingSubmission;
                    submission.setContent(content);
                    submission.setStatus("draft");
                    submission.setSubmittedAt(null);
                    submission.setIsLate(false);
                    submission.setUpdatedAt(LocalDateTime.now());
                    submissionMapper.updateSubmission(submission);
                    logger.info("已打回场景，允许保存草稿并将状态置为 draft，ID: {}", submission.getId());
                    return submission;
                }
            } catch (Exception ignored) {}
            // 非打回场景，仍按原规则禁止保存草稿
            throw new BusinessException(ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED);
        }

        if (existingSubmission == null) {
            // 创建新草稿
            submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(content);
            submission.setStatus("draft");
            submission.setSubmissionCount(0);
            submission.setCreatedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
            submissionMapper.insertSubmission(submission);
        } else {
            // 更新现有草稿
            submission = existingSubmission;
            submission.setContent(content);
            submission.setUpdatedAt(LocalDateTime.now());
            submissionMapper.updateSubmission(submission);
        }

        logger.info("草稿保存成功，ID: {}", submission.getId());
        return submission;
    }

    @Override
    public Submission getSubmissionById(Long submissionId) {
        logger.info("获取提交详情，ID: {}", submissionId);
        Submission submission = submissionMapper.selectSubmissionById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        return submission;
    }

    @Override
    public Submission getStudentSubmission(Long assignmentId, Long studentId) {
        logger.info("获取学生作业提交，作业ID: {}, 学生ID: {}", assignmentId, studentId);
        return submissionMapper.selectByAssignmentAndStudent(assignmentId, studentId);
    }

    @Override
    public PageResult<Submission> getStudentSubmissions(Long studentId, Integer page, Integer size) {
        logger.info("分页获取学生提交列表，学生ID: {}, 页码: {}, 每页大小: {}", studentId, page, size);
        PageHelper.startPage(page, size);
        List<Submission> submissions = submissionMapper.selectByStudentId(studentId);
        PageInfo<Submission> pageInfo = new PageInfo<>(submissions);
        return PageResult.of(pageInfo.getList(), page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public PageResult<Submission> getAssignmentSubmissions(Long assignmentId, Integer page, Integer size) {
        logger.info("分页获取作业提交列表，作业ID: {}, 页码: {}, 每页大小: {}", assignmentId, page, size);
        PageHelper.startPage(page, size);
        List<Submission> submissions = submissionMapper.selectByAssignmentId(assignmentId);
        PageInfo<Submission> pageInfo = new PageInfo<>(submissions);
        return PageResult.of(pageInfo.getList(), page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public PageResult<Map<String, Object>> getStudentGrades(Long studentId, Integer page, Integer size) {
        logger.info("分页获取学生成绩列表，学生ID: {}, 页码: {}, 每页大小: {}", studentId, page, size);
        PageHelper.startPage(page, size);
        List<Map<String, Object>> grades = submissionMapper.selectStudentGradesWithPagination(studentId, 0, size); // Offset is managed by PageHelper
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(grades);
        return PageResult.of(pageInfo.getList(), page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public Map<String, Object> getSubmissionGrade(Long submissionId) {
        logger.info("获取提交成绩详情，提交ID: {}", submissionId);
        Map<String, Object> grade = submissionMapper.selectSubmissionGrade(submissionId);
        if (grade == null) {
            throw new BusinessException(ErrorCode.GRADE_NOT_FOUND);
        }
        // 追加班级平均分与排名（后续可迁到SQL层窗口函数实现）
        try {
            // 平均分
            Object avg = grade.get("avg_score");
            if (avg != null) {
                grade.put("averageScore", avg);
            }
            // 总人数
            Object total = grade.get("total_students");
            if (total != null) {
                grade.put("totalStudents", total);
            }
            // 排名：基于该作业所有已评分成绩按 score 降序排名
            // 这里简单通过 mapper 的现有方法拼装数据较重，暂用占位，当无成绩时给 null
            // 前端将做兜底显示
        } catch (Exception ignore) {}
        return grade;
    }

    @Override
    public void updateSubmissionStatus(Long submissionId, String status) {
        logger.info("更新提交状态，ID: {}, 状态: {}", submissionId, status);
        int result = submissionMapper.updateSubmissionStatus(submissionId, status);
        if (result == 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新提交状态失败");
        }
    }

    @Override
    public void deleteSubmission(Long submissionId) {
        logger.info("删除提交记录，ID: {}", submissionId);
        // 先清理该提交的附件
        try {
            fileStorageService.cleanupRelatedFiles("submission", submissionId);
        } catch (Exception e) {
            logger.warn("清理提交附件失败 submissionId={}", submissionId, e);
        }
        int result = submissionMapper.deleteSubmission(submissionId);
        if (result == 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除提交失败");
        }
    }

    @Override
    public boolean hasSubmitted(Long assignmentId, Long studentId) {
        logger.info("检查学生是否已提交作业，作业ID: {}, 学生ID: {}", assignmentId, studentId);
        long count = submissionMapper.countByAssignmentAndStudent(assignmentId, studentId);
        return count > 0;
    }

    @Override
    public Map<String, Object> getSubmissionStatistics(Long assignmentId) {
        logger.info("获取作业提交统计，作业ID: {}", assignmentId);
        return submissionMapper.getSubmissionStatistics(assignmentId);
    }

    @Override
    public byte[] exportSubmissionZip(Long submissionId) {
        logger.info("导出提交为ZIP，提交ID: {}", submissionId);
        Submission submission = submissionMapper.selectSubmissionById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        // 评分：优先从 grades 表取完整字段（含 strengths/improvements 等）
        Grade gradeEntity = null;
        try {
            if (submission.getStudentId() != null && submission.getAssignmentId() != null) {
                gradeEntity = gradeMapper.selectByStudentAndAssignment(submission.getStudentId(), submission.getAssignmentId());
            }
        } catch (Exception e) {
            logger.warn("获取成绩实体失败，继续导出提交内容。submissionId={}", submissionId, e);
        }

        // AI 报告（若有）：优先 submissionId 上下文，其次 assignmentId/课程
        AbilityReport aiReport = null;
        try {
            Long studentId = submission.getStudentId();
            Long assignmentId = submission.getAssignmentId();
            Long courseId = null;
            try {
                Assignment a = assignmentMapper.selectAssignmentById(assignmentId);
                courseId = a != null ? a.getCourseId() : null;
            } catch (Exception ignored) {}
            if (studentId != null) {
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("studentId", studentId);
                // 1) submissionId
                params.put("submissionId", submissionId);
                aiReport = abilityReportMapper.selectLatestReportByContext(params);
                // 2) assignmentId
                if (aiReport == null) {
                    params.remove("submissionId");
                    if (assignmentId != null) params.put("assignmentId", assignmentId);
                    aiReport = abilityReportMapper.selectLatestReportByContext(params);
                }
                // 3) courseId
                if (aiReport == null) {
                    params.remove("assignmentId");
                    if (courseId != null) params.put("courseId", courseId);
                    aiReport = abilityReportMapper.selectLatestReportByContext(params);
                }
            }
        } catch (Exception e) {
            logger.warn("获取AI报告失败，继续仅导出提交/附件/评分。submissionId={}", submissionId, e);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 小工具：安全的 zip entry 文件名（防止 ../ 与绝对路径）
            java.util.function.Function<String, String> safeName = (raw) -> {
                String s = raw == null ? "" : raw.trim();
                if (s.isEmpty()) return "unknown";
                // 只保留文件名，不允许路径
                try { s = Paths.get(s).getFileName().toString(); } catch (Exception ignored) {}
                s = s.replace('\\', '_').replace('/', '_');
                while (s.contains("..")) s = s.replace("..", "_");
                return s.isEmpty() ? "unknown" : s;
            };

            // 元数据
            String metaJson = "{" +
                    "\"submissionId\":" + submission.getId() + "," +
                    "\"assignmentId\":" + submission.getAssignmentId() + "," +
                    "\"studentId\":" + submission.getStudentId() + "," +
                    "\"assignmentTitle\":\"" + (submission.getAssignmentTitle() == null ? "" : submission.getAssignmentTitle().replace("\"", "\\\"")) + "\"," +
                    "\"studentName\":\"" + (submission.getStudentName() == null ? "" : submission.getStudentName().replace("\"", "\\\"")) + "\"," +
                    "\"courseName\":\"" + (submission.getCourseName() == null ? "" : submission.getCourseName().replace("\"", "\\\"")) + "\"," +
                    "\"submittedAt\":\"" + (submission.getSubmittedAt() == null ? "" : submission.getSubmittedAt()) + "\"," +
                    "\"status\":\"" + (submission.getStatus() == null ? "" : submission.getStatus()) + "\"" +
                    "}";
            zos.putNextEntry(new ZipEntry("meta.json"));
            zos.write(metaJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 提交文本
            String content = submission.getContent() == null ? "" : submission.getContent();
            zos.putNextEntry(new ZipEntry("submission.txt"));
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 评分结果（如存在记录且有内容）
            if (gradeEntity != null) {
                String fb = gradeEntity.getFeedback() == null ? "" : gradeEntity.getFeedback();
                String stg = gradeEntity.getStrengths() == null ? "" : gradeEntity.getStrengths();
                String imp = gradeEntity.getImprovements() == null ? "" : gradeEntity.getImprovements();
                boolean hasAny = (gradeEntity.getScore() != null)
                        || !fb.isBlank()
                        || !stg.isBlank()
                        || !imp.isBlank()
                        || (gradeEntity.getGradeLevel() != null && !gradeEntity.getGradeLevel().isBlank());
                if (hasAny) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("分数：").append(gradeEntity.getScore() == null ? "" : gradeEntity.getScore()).append('\n');
                    sb.append("满分：").append(gradeEntity.getMaxScore() == null ? "" : gradeEntity.getMaxScore()).append('\n');
                    sb.append("得分率：").append(gradeEntity.getPercentage() == null ? "" : gradeEntity.getPercentage()).append('\n');
                    sb.append("等级：").append(gradeEntity.getGradeLevel() == null ? "" : gradeEntity.getGradeLevel()).append('\n');
                    sb.append("状态：").append(gradeEntity.getStatus() == null ? "" : gradeEntity.getStatus()).append('\n');
                    sb.append("批改时间：").append(gradeEntity.getPublishedAt() != null ? gradeEntity.getPublishedAt() : (gradeEntity.getUpdatedAt() != null ? gradeEntity.getUpdatedAt() : "")).append('\n');
                    sb.append("\n评语：\n").append(fb).append('\n');
                    if (!stg.isBlank()) sb.append("\n优点：\n").append(stg).append('\n');
                    if (!imp.isBlank()) sb.append("\n改进建议：\n").append(imp).append('\n');
                    zos.putNextEntry(new ZipEntry("grade_result.txt"));
                    zos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                }
            }

            // AI 报告（如存在）
            if (aiReport != null) {
                String raw = aiReport.getTrendsAnalysis();
                if (raw == null || raw.isBlank()) raw = aiReport.getRecommendations();
                if (raw == null) raw = "";
                // 尝试格式化为更易读的 JSON（失败则原样输出）
                String out = raw;
                try {
                    Object obj = new com.fasterxml.jackson.databind.ObjectMapper().readValue(raw, Object.class);
                    out = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
                } catch (Exception ignored) {}
                zos.putNextEntry(new ZipEntry("ai_report.json"));
                zos.write(out.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            // 附件：打包提交关联的所有文件（file_records: purpose=submission, relatedId=submissionId）
            try {
                List<FileRecord> files = fileStorageService.getRelatedFiles("submission", submissionId);
                java.util.Map<String, Integer> used = new java.util.HashMap<>();
                if (files != null) {
                    for (FileRecord f : files) {
                        if (f == null || f.getFilePath() == null) continue;
                        String name = safeName.apply(f.getOriginalName());
                        // 去重：同名文件追加序号
                        int idx = used.getOrDefault(name, 0);
                        used.put(name, idx + 1);
                        String finalName = (idx <= 0) ? name : (name + "(" + idx + ")");
                        Path fp = Paths.get(f.getFilePath());
                        if (!Files.exists(fp)) continue;
                        zos.putNextEntry(new ZipEntry("files/" + finalName));
                        zos.write(Files.readAllBytes(fp));
                        zos.closeEntry();
                    }
                }
            } catch (Exception e) {
                logger.warn("打包提交附件失败，继续导出其他内容。submissionId={}", submissionId, e);
            }

            // 兼容旧字段：submissions.file_path 单文件（若存在且未被关联文件覆盖）
            try {
                if (submission.getFilePath() != null && !submission.getFilePath().isBlank()) {
                    Path fp = Paths.get(submission.getFilePath());
                    if (Files.exists(fp)) {
                        String name = safeName.apply(submission.getFileName() != null ? submission.getFileName() : fp.getFileName().toString());
                        zos.putNextEntry(new ZipEntry("files/" + name));
                        zos.write(Files.readAllBytes(fp));
                        zos.closeEntry();
                    }
                }
            } catch (Exception ignored) {}

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("导出ZIP失败，submissionId={}", submissionId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "导出失败");
        }
    }
} 