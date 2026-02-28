package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminLessonNoteListItemResponse;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.mapper.AdminUserMapper;
import com.noncore.assessment.mapper.AiVoiceSessionMapper;
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.service.AnalyticsQueryService;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminAuditLogService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.CsvUtils;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 管理员-数据导出（轻量同步版）。
 *
 * <p>说明：首期以同步 CSV 导出为主，避免前端拉取全量数据；后续可扩展为异步任务+文件存储。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/exports")
@Tag(name = "管理员-数据导出", description = "管理员数据导出（CSV）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExportsController extends BaseController {

    private final AdminUserMapper adminUserMapper;
    private final AbilityReportMapper abilityReportMapper;
    private final AdminAuditLogService adminAuditLogService;
    private final UserMapper userMapper;
    private final AiConversationService aiConversationService;
    private final AiVoiceSessionMapper aiVoiceSessionMapper;
    private final LessonProgressMapper lessonProgressMapper;
    private final AssignmentMapper assignmentMapper;
    private final GradeMapper gradeMapper;
    private final AnalyticsQueryService analyticsQueryService;

    public AdminExportsController(AdminUserMapper adminUserMapper,
                                  AbilityReportMapper abilityReportMapper,
                                  AdminAuditLogService adminAuditLogService,
                                  UserMapper userMapper,
                                  AiConversationService aiConversationService,
                                  AiVoiceSessionMapper aiVoiceSessionMapper,
                                  LessonProgressMapper lessonProgressMapper,
                                  AssignmentMapper assignmentMapper,
                                  GradeMapper gradeMapper,
                                  AnalyticsQueryService analyticsQueryService,
                                  UserService userService) {
        super(userService);
        this.adminUserMapper = adminUserMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.adminAuditLogService = adminAuditLogService;
        this.userMapper = userMapper;
        this.aiConversationService = aiConversationService;
        this.aiVoiceSessionMapper = aiVoiceSessionMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/users.csv")
    @Operation(summary = "导出用户列表（CSV）")
    public ResponseEntity<byte[]> exportUsersCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            HttpServletRequest httpRequest
    ) {
        // 导出上限：避免一次性拉取过大
        int size = 50000;
        List<AdminUserListItemResponse> items = adminUserMapper.pageUsers(keyword, role, status, includeDeleted, 0, size);

        StringBuilder sb = new StringBuilder();
        sb.append("id,username,email,role,status,nickname,mbti,studentNo,teacherNo,emailVerified,deleted,createdAt\n");
        if (items != null) {
            for (AdminUserListItemResponse u : items) {
                sb.append(CsvUtils.nullToEmpty(u.getId())).append(',')
                        .append(CsvUtils.escape(u.getUsername())).append(',')
                        .append(CsvUtils.escape(u.getEmail())).append(',')
                        .append(CsvUtils.escape(u.getRole())).append(',')
                        .append(CsvUtils.escape(u.getStatus())).append(',')
                        .append(CsvUtils.escape(u.getNickname())).append(',')
                        .append(CsvUtils.escape(u.getMbti())).append(',')
                        .append(CsvUtils.escape(u.getStudentNo())).append(',')
                        .append(CsvUtils.escape(u.getTeacherNo())).append(',')
                        .append(CsvUtils.nullToEmpty(u.isEmailVerified())).append(',')
                        .append(CsvUtils.nullToEmpty(u.isDeleted())).append(',')
                        .append(CsvUtils.escape(u.getCreatedAt() == null ? "" : String.valueOf(u.getCreatedAt())))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.users.csv",
                "export",
                null,
                Map.of("keyword", keyword, "role", role, "status", status, "includeDeleted", includeDeleted, "rows", items == null ? 0 : items.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/ability-reports.csv")
    @Operation(summary = "导出能力报告列表（CSV）")
    public ResponseEntity<byte[]> exportAbilityReportsCsv(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long submissionId,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        List<AbilityReport> items = abilityReportMapper.selectAdminReports(
                null, studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end, 0, size
        );

        StringBuilder sb = new StringBuilder();
        sb.append("id,studentId,studentName,studentNo,reportType,title,overallScore,courseId,assignmentId,submissionId,isPublished,createdAt\n");
        if (items != null) {
            for (AbilityReport r : items) {
                sb.append(CsvUtils.nullToEmpty(r.getId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getStudentId())).append(',')
                        .append(CsvUtils.escape(r.getStudentName())).append(',')
                        .append(CsvUtils.escape(r.getStudentNumber())).append(',')
                        .append(CsvUtils.escape(r.getReportType())).append(',')
                        .append(CsvUtils.escape(r.getTitle())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getOverallScore())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getCourseId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getAssignmentId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getSubmissionId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getIsPublished())).append(',')
                        .append(CsvUtils.escape(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString()))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.abilityReports.csv",
                "export",
                null,
                Map.of(
                        "studentId", studentId,
                        "reportType", reportType,
                        "isPublished", isPublished,
                        "courseId", courseId,
                        "assignmentId", assignmentId,
                        "submissionId", submissionId,
                        "start", start == null ? null : start.toString(),
                        "end", end == null ? null : end.toString(),
                        "rows", items == null ? 0 : items.size()
                ),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ability_reports.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/course-students.csv")
    @Operation(summary = "导出课程学生名单（CSV）", description = "管理员导出指定课程下的学生列表（最多50000行）")
    public ResponseEntity<byte[]> exportCourseStudentsCsv(
            @RequestParam Long courseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "joinDate") String sortBy,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String progress,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        String kw = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        PageHelper.startPage(1, size);
        List<User> items = userMapper.selectStudentsByCourseIdAdvanced(courseId, kw, activity, grade, progress, sortBy);
        PageInfo<User> pi = new PageInfo<>(items);
        List<User> rows = pi.getList();

        StringBuilder sb = new StringBuilder();
        sb.append("courseId,studentId,username,nickname,studentNo,email,role,status,createdAt\n");
        if (rows != null) {
            for (User u : rows) {
                sb.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(u.getId())).append(',')
                        .append(CsvUtils.escape(u.getUsername())).append(',')
                        .append(CsvUtils.escape(u.getNickname())).append(',')
                        .append(CsvUtils.escape(u.getStudentNo())).append(',')
                        .append(CsvUtils.escape(u.getEmail())).append(',')
                        .append(CsvUtils.escape(u.getRole())).append(',')
                        .append(CsvUtils.escape(u.getStatus())).append(',')
                        .append(CsvUtils.escape(u.getCreatedAt() == null ? "" : String.valueOf(u.getCreatedAt())))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.courseStudents.csv",
                "export",
                courseId,
                Map.of("courseId", courseId, "search", search, "sortBy", sortBy, "activity", activity, "grade", grade, "progress", progress, "rows", rows == null ? 0 : rows.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_students.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/ai-conversations.csv")
    @Operation(summary = "导出AI会话索引（CSV）", description = "管理员按学生导出 AI 会话列表（最多50000行）")
    public ResponseEntity<byte[]> exportAiConversationsCsv(
            @RequestParam Long studentId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(required = false) Boolean archived,
            HttpServletRequest httpRequest
    ) {
        PageResult<AiConversation> res = aiConversationService.listConversations(studentId, q, pinned, archived, 1, 50000);
        List<AiConversation> rows = res == null ? null : res.getItems();

        StringBuilder sb = new StringBuilder();
        sb.append("studentId,conversationId,title,model,provider,pinned,archived,lastMessageAt,createdAt,updatedAt\n");
        if (rows != null) {
            for (AiConversation c : rows) {
                sb.append(CsvUtils.nullToEmpty(studentId)).append(',')
                        .append(CsvUtils.nullToEmpty(c.getId())).append(',')
                        .append(CsvUtils.escape(c.getTitle())).append(',')
                        .append(CsvUtils.escape(c.getModel())).append(',')
                        .append(CsvUtils.escape(c.getProvider())).append(',')
                        .append(CsvUtils.nullToEmpty(c.getPinned())).append(',')
                        .append(CsvUtils.nullToEmpty(c.getArchived())).append(',')
                        .append(CsvUtils.escape(c.getLastMessageAt() == null ? "" : c.getLastMessageAt().toString())).append(',')
                        .append(CsvUtils.escape(c.getCreatedAt() == null ? "" : c.getCreatedAt().toString())).append(',')
                        .append(CsvUtils.escape(c.getUpdatedAt() == null ? "" : c.getUpdatedAt().toString()))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.aiConversations.csv",
                "export",
                studentId,
                Map.of("studentId", studentId, "q", q, "pinned", pinned, "archived", archived, "rows", rows == null ? 0 : rows.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ai_conversations.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/voice-sessions.csv")
    @Operation(summary = "导出口语训练会话索引（CSV）", description = "管理员按学生导出口语会话列表（最多50000行）")
    public ResponseEntity<byte[]> exportVoiceSessionsCsv(
            @RequestParam Long studentId,
            @RequestParam(required = false) String q,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        String qq = (q == null || q.isBlank()) ? null : q.trim();
        List<AiVoiceSession> rows = aiVoiceSessionMapper.listByUser(studentId, qq, 0, size);

        StringBuilder sb = new StringBuilder();
        sb.append("studentId,sessionId,title,model,mode,locale,scenario,pinned,createdAt,updatedAt\n");
        if (rows != null) {
            for (AiVoiceSession s : rows) {
                sb.append(CsvUtils.nullToEmpty(studentId)).append(',')
                        .append(CsvUtils.nullToEmpty(s.getId())).append(',')
                        .append(CsvUtils.escape(s.getTitle())).append(',')
                        .append(CsvUtils.escape(s.getModel())).append(',')
                        .append(CsvUtils.escape(s.getMode())).append(',')
                        .append(CsvUtils.escape(s.getLocale())).append(',')
                        .append(CsvUtils.escape(s.getScenario())).append(',')
                        .append(CsvUtils.nullToEmpty(s.getPinned())).append(',')
                        .append(CsvUtils.escape(s.getCreatedAt() == null ? "" : s.getCreatedAt().toString())).append(',')
                        .append(CsvUtils.escape(s.getUpdatedAt() == null ? "" : s.getUpdatedAt().toString()))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.voiceSessions.csv",
                "export",
                studentId,
                Map.of("studentId", studentId, "q", q, "rows", rows == null ? 0 : rows.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=voice_sessions.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/course-lesson-notes.csv")
    @Operation(summary = "导出课程课堂笔记（CSV）", description = "管理员导出指定课程下的课堂笔记（最多50000行），支持按学生/节次/关键词筛选")
    public ResponseEntity<byte[]> exportCourseLessonNotesCsv(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false, name = "q") String q,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        String qq = (q == null || q.isBlank()) ? null : q.trim();
        List<AdminLessonNoteListItemResponse> rows = lessonProgressMapper.selectAdminCourseLessonNotes(courseId, studentId, lessonId, qq, 0, size);

        StringBuilder sb = new StringBuilder();
        sb.append("courseId,lessonId,lessonTitle,chapterTitle,studentId,studentName,studentNo,updatedAt,notes\n");
        if (rows != null) {
            for (AdminLessonNoteListItemResponse it : rows) {
                sb.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(it.getLessonId())).append(',')
                        .append(CsvUtils.escape(it.getLessonTitle())).append(',')
                        .append(CsvUtils.escape(it.getChapterTitle())).append(',')
                        .append(CsvUtils.nullToEmpty(it.getStudentId())).append(',')
                        .append(CsvUtils.escape(it.getStudentName())).append(',')
                        .append(CsvUtils.escape(it.getStudentNo())).append(',')
                        .append(CsvUtils.escape(it.getUpdatedAt() == null ? "" : it.getUpdatedAt().toString())).append(',')
                        .append(CsvUtils.escape(it.getNotes()))
                        .append('\n');
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        Map<String, Object> auditDetail = new HashMap<>();
        auditDetail.put("courseId", courseId);
        auditDetail.put("studentId", studentId);
        auditDetail.put("lessonId", lessonId);
        auditDetail.put("q", qq);
        auditDetail.put("rows", rows == null ? 0 : rows.size());
        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.courseLessonNotes.csv",
                "export",
                courseId,
                auditDetail,
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_lesson_notes.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/course-lesson-notes.zip")
    @Operation(summary = "导出课程课堂笔记（ZIP）", description = "ZIP 内含 lesson_notes.csv，支持按学生/节次/关键词筛选")
    public ResponseEntity<byte[]> exportCourseLessonNotesZip(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false, name = "q") String q,
            HttpServletRequest httpRequest
    ) {
        int size = 50000;
        String qq = (q == null || q.isBlank()) ? null : q.trim();
        List<AdminLessonNoteListItemResponse> rows = lessonProgressMapper.selectAdminCourseLessonNotes(courseId, studentId, lessonId, qq, 0, size);

        StringBuilder sb = new StringBuilder();
        sb.append("courseId,lessonId,lessonTitle,chapterTitle,studentId,studentName,studentNo,updatedAt,notes\n");
        if (rows != null) {
            for (AdminLessonNoteListItemResponse it : rows) {
                sb.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(it.getLessonId())).append(',')
                        .append(CsvUtils.escape(it.getLessonTitle())).append(',')
                        .append(CsvUtils.escape(it.getChapterTitle())).append(',')
                        .append(CsvUtils.nullToEmpty(it.getStudentId())).append(',')
                        .append(CsvUtils.escape(it.getStudentName())).append(',')
                        .append(CsvUtils.escape(it.getStudentNo())).append(',')
                        .append(CsvUtils.escape(it.getUpdatedAt() == null ? "" : it.getUpdatedAt().toString())).append(',')
                        .append(CsvUtils.escape(it.getNotes()))
                        .append('\n');
            }
        }
        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        byte[] zipBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("lesson_notes.csv"));
            zos.write(csvBytes);
            zos.closeEntry();
            zos.finish();
            zipBytes = baos.toByteArray();
        } catch (Exception e) {
            // 退化：返回 CSV（不影响主要功能）
            zipBytes = csvBytes;
        }

        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.courseLessonNotes.zip",
                "export",
                courseId,
                auditDetailForLessonNotesZip(courseId, studentId, lessonId, qq, rows == null ? 0 : rows.size()),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_lesson_notes.zip");
        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }

    /**
     * 导出：单学生在某课程下的全量学习数据（ZIP）。
     *
     * <p>注意：按需求不包含个人敏感信息（如姓名/邮箱/学号等）。仅包含 studentId 与学习/成绩数据。</p>
     */
    @GetMapping("/course-student-data.zip")
    @Operation(summary = "导出单学生课程全量数据（ZIP）", description = "包含作业成绩、能力报告、课堂笔记与聚合摘要（不含个人信息）")
    public ResponseEntity<byte[]> exportCourseStudentDataZip(
            @RequestParam Long courseId,
            @RequestParam Long studentId,
            HttpServletRequest httpRequest
    ) {
        // 1) 基础数据
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        Map<Long, String> assignmentTitleMap = (assignments == null ? java.util.Collections.<Assignment>emptyList() : assignments)
                .stream()
                .filter(a -> a != null && a.getId() != null)
                .collect(Collectors.toMap(Assignment::getId, a -> a.getTitle() == null ? "" : a.getTitle(), (a, b) -> a));

        List<Grade> grades = gradeMapper.selectByStudentAndCourse(studentId, courseId);
        List<AbilityReport> reports = abilityReportMapper.selectAdminReports(
                null, studentId, null, null, courseId, null, null, null, null, 0, 50000
        );
        List<AdminLessonNoteListItemResponse> notesRows = lessonProgressMapper.selectAdminCourseLessonNotes(
                courseId, studentId, null, null, 0, 50000
        );

        // 2) summary（聚合）
        java.math.BigDecimal avgScore = gradeMapper.calculateCourseAverageScore(studentId, courseId);
        // 复用分析服务生成雷达（会包含 dimensionScores / radarArea / classification 等）
        com.noncore.assessment.dto.response.CourseStudentPerformanceResponse perf = analyticsQueryService.getCourseStudentPerformance(
                getCurrentUserId(), courseId, 1, 50000, null, "radar", null, null, null
        );
        Map<String, Object> summary = new HashMap<>();
        summary.put("courseId", courseId);
        summary.put("studentId", studentId);
        summary.put("courseAverageScore", avgScore == null ? null : avgScore);
        try {
            if (perf != null && perf.getItems() != null) {
                com.noncore.assessment.dto.response.CourseStudentPerformanceItem me = perf.getItems().stream()
                        .filter(i -> i != null && studentId.equals(i.getStudentId()))
                        .findFirst().orElse(null);
                if (me != null) {
                    summary.put("progress", me.getProgress());
                    summary.put("completedLessons", me.getCompletedLessons());
                    summary.put("totalLessons", me.getTotalLessons());
                    summary.put("radarArea", me.getRadarArea());
                    summary.put("radarClassification", me.getRadarClassification());
                    summary.put("dimensionScores", me.getDimensionScores());
                }
            }
        } catch (Exception ignore) {}

        // 3) CSV：assignments_grades.csv（作业成绩）
        StringBuilder gradesCsv = new StringBuilder();
        gradesCsv.append("courseId,studentId,assignmentId,assignmentTitle,submissionId,score,maxScore,percentage,gradeLevel,status,publishedAt,updatedAt\n");
        if (grades != null) {
            for (Grade g : grades) {
                Long aid = g.getAssignmentId();
                gradesCsv.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(studentId)).append(',')
                        .append(CsvUtils.nullToEmpty(aid)).append(',')
                        .append(CsvUtils.escape(assignmentTitleMap.getOrDefault(aid, ""))).append(',')
                        .append(CsvUtils.nullToEmpty(g.getSubmissionId())).append(',')
                        .append(CsvUtils.escape(g.getScore() == null ? "" : String.valueOf(g.getScore()))).append(',')
                        .append(CsvUtils.escape(g.getMaxScore() == null ? "" : String.valueOf(g.getMaxScore()))).append(',')
                        .append(CsvUtils.escape(g.getPercentage() == null ? "" : String.valueOf(g.getPercentage()))).append(',')
                        .append(CsvUtils.escape(g.getGradeLevel())).append(',')
                        .append(CsvUtils.escape(g.getStatus())).append(',')
                        .append(CsvUtils.escape(g.getPublishedAt() == null ? "" : g.getPublishedAt().toString())).append(',')
                        .append(CsvUtils.escape(g.getUpdatedAt() == null ? "" : g.getUpdatedAt().toString()))
                        .append('\n');
            }
        }

        // 4) CSV：ability_reports.csv（能力报告历史）
        StringBuilder reportsCsv = new StringBuilder();
        reportsCsv.append("courseId,studentId,reportId,reportType,title,overallScore,assignmentId,submissionId,createdAt,dimensionScores\n");
        if (reports != null) {
            for (AbilityReport r : reports) {
                reportsCsv.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(studentId)).append(',')
                        .append(CsvUtils.nullToEmpty(r.getId())).append(',')
                        .append(CsvUtils.escape(r.getReportType())).append(',')
                        .append(CsvUtils.escape(r.getTitle())).append(',')
                        .append(CsvUtils.escape(r.getOverallScore() == null ? "" : String.valueOf(r.getOverallScore()))).append(',')
                        .append(CsvUtils.nullToEmpty(r.getAssignmentId())).append(',')
                        .append(CsvUtils.nullToEmpty(r.getSubmissionId())).append(',')
                        .append(CsvUtils.escape(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString())).append(',')
                        .append(CsvUtils.escape(r.getDimensionScores()))
                        .append('\n');
            }
        }

        // 5) CSV：lesson_notes.csv（课堂笔记）
        StringBuilder notesCsv = new StringBuilder();
        notesCsv.append("courseId,studentId,lessonId,lessonTitle,chapterTitle,updatedAt,notes\n");
        if (notesRows != null) {
            for (AdminLessonNoteListItemResponse it : notesRows) {
                notesCsv.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(studentId)).append(',')
                        .append(CsvUtils.nullToEmpty(it.getLessonId())).append(',')
                        .append(CsvUtils.escape(it.getLessonTitle())).append(',')
                        .append(CsvUtils.escape(it.getChapterTitle())).append(',')
                        .append(CsvUtils.escape(it.getUpdatedAt() == null ? "" : it.getUpdatedAt().toString())).append(',')
                        .append(CsvUtils.escape(it.getNotes()))
                        .append('\n');
            }
        }

        // 6) ZIP 打包
        byte[] zipBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("assignments_grades.csv"));
            zos.write(gradesCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("ability_reports.csv"));
            zos.write(reportsCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("lesson_notes.csv"));
            zos.write(notesCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // summary.json（简单 JSON）
            String summaryJson;
            try {
                summaryJson = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(summary);
            } catch (Exception e) {
                summaryJson = String.valueOf(summary);
            }
            zos.putNextEntry(new ZipEntry("summary.json"));
            zos.write(summaryJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.finish();
            zipBytes = baos.toByteArray();
        } catch (Exception e) {
            zipBytes = ("export failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }

        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.courseStudentData.zip",
                "export",
                courseId,
                Map.of("courseId", courseId, "studentId", studentId, "zipBytes", zipBytes.length),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_student_data.zip");
        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }

    /**
     * 导出：某课程全体学生全量数据（ZIP）。
     *
     * <p>注意：按需求不包含个人敏感信息（如姓名/邮箱/学号等）。</p>
     */
    @GetMapping("/course-data.zip")
    @Operation(summary = "导出课程全量数据（ZIP）", description = "包含全体学生汇总、作业成绩宽表、课堂笔记宽表与能力报告（不含个人信息）")
    public ResponseEntity<byte[]> exportCourseDataZip(
            @RequestParam Long courseId,
            HttpServletRequest httpRequest
    ) {
        // 1) 课程学生汇总（复用分析服务，包含雷达与平均分）
        com.noncore.assessment.dto.response.CourseStudentPerformanceResponse perf = null;
        try {
            perf = analyticsQueryService.getCourseStudentPerformance(
                    getCurrentUserId(), courseId, 1, 50000, null, "radar", null, null, null
            );
        } catch (Exception ignore) {}
        List<com.noncore.assessment.dto.response.CourseStudentPerformanceItem> items = perf == null ? null : perf.getItems();

        // 2) 作业成绩宽表
        List<Assignment> assignments;
        try {
            assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        } catch (Exception e) {
            assignments = java.util.Collections.emptyList();
        }
        Map<Long, String> assignmentTitleMap = (assignments == null ? java.util.Collections.<Assignment>emptyList() : assignments)
                .stream()
                .filter(a -> a != null && a.getId() != null)
                .collect(Collectors.toMap(Assignment::getId, a -> a.getTitle() == null ? "" : a.getTitle(), (a, b) -> a));

        List<Grade> grades;
        try {
            grades = gradeMapper.selectByCourseId(courseId);
        } catch (Exception e) {
            grades = loadGradesByExportFallback(courseId);
        }

        // 构建学生维度与成绩索引
        Map<Long, com.noncore.assessment.dto.response.CourseStudentPerformanceItem> perfByStudentId = new HashMap<>();
        if (items != null) {
            for (com.noncore.assessment.dto.response.CourseStudentPerformanceItem it : items) {
                if (it != null && it.getStudentId() != null) perfByStudentId.put(it.getStudentId(), it);
            }
        }
        Map<Long, Map<Long, Grade>> gradeByStudentAssignment = new HashMap<>();
        Map<Long, List<Grade>> gradeByStudent = new HashMap<>();
        if (grades != null) {
            for (Grade g : grades) {
                if (g == null || g.getStudentId() == null) continue;
                gradeByStudentAssignment.computeIfAbsent(g.getStudentId(), k -> new HashMap<>()).put(g.getAssignmentId(), g);
                gradeByStudent.computeIfAbsent(g.getStudentId(), k -> new java.util.ArrayList<>()).add(g);
            }
        }

        // 补充学生姓名/学号（优先 perf，其次用户表）
        java.util.Set<Long> studentIdSet = new java.util.LinkedHashSet<>();
        studentIdSet.addAll(perfByStudentId.keySet());
        if (grades != null) {
            for (Grade g : grades) {
                if (g != null && g.getStudentId() != null) studentIdSet.add(g.getStudentId());
            }
        }
        Map<Long, User> usersById = new HashMap<>();
        if (!studentIdSet.isEmpty()) {
            try {
                List<User> users = userMapper.selectUsersByIds(new java.util.ArrayList<>(studentIdSet));
                if (users != null) {
                    for (User u : users) {
                        if (u != null && u.getId() != null) usersById.put(u.getId(), u);
                    }
                }
            } catch (Exception ignore) {}
        }

        final List<String> dimCodes = java.util.List.of(
                "MORAL_COGNITION",
                "LEARNING_ATTITUDE",
                "LEARNING_ABILITY",
                "LEARNING_METHOD",
                "ACADEMIC_GRADE"
        );

        // 2.1 课程学生汇总（增强列：姓名/学号 + 维度分数/等级 + 每作业分数 + 平均分/维度均分）
        StringBuilder summaryCsv = new StringBuilder();
        List<String> summaryHeaders = new java.util.ArrayList<>();
        summaryHeaders.add("courseId");
        summaryHeaders.add("studentId");
        summaryHeaders.add("studentName");
        summaryHeaders.add("studentNo");
        summaryHeaders.add("progress");
        summaryHeaders.add("completedLessons");
        summaryHeaders.add("totalLessons");
        summaryHeaders.add("averageGrade");
        summaryHeaders.add("activityLevel");
        summaryHeaders.add("lastActiveAt");
        summaryHeaders.add("radarArea");
        summaryHeaders.add("radarClassification");
        for (String code : dimCodes) {
            summaryHeaders.add("dim_" + code + "_score");
            summaryHeaders.add("dim_" + code + "_level");
        }
        summaryHeaders.add("dimensionAverageScore");
        summaryHeaders.add("dimensionAverageLevel");
        List<Assignment> sortedAssignments = assignments == null ? java.util.Collections.emptyList() :
                assignments.stream().filter(a -> a != null && a.getId() != null).sorted(java.util.Comparator.comparing(Assignment::getId)).collect(Collectors.toList());
        for (Assignment a : sortedAssignments) {
            String aid = String.valueOf(a.getId());
            summaryHeaders.add("assignment_" + aid + "_title");
            summaryHeaders.add("assignment_" + aid + "_score");
            summaryHeaders.add("assignment_" + aid + "_percentage");
            summaryHeaders.add("assignment_" + aid + "_gradeLevel");
        }
        summaryCsv.append(String.join(",", summaryHeaders.stream().map(CsvUtils::escape).collect(Collectors.toList()))).append('\n');

        for (Long sid : studentIdSet) {
            com.noncore.assessment.dto.response.CourseStudentPerformanceItem it = perfByStudentId.get(sid);
            User user = usersById.get(sid);
            String studentName = it != null && it.getStudentName() != null && !it.getStudentName().isBlank()
                    ? it.getStudentName()
                    : (user == null ? "" : (user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername()));
            String studentNo = it != null && it.getStudentNo() != null && !it.getStudentNo().isBlank()
                    ? it.getStudentNo()
                    : (user == null ? "" : user.getStudentNo());
            Map<String, Double> dimMap = it == null ? java.util.Collections.emptyMap() :
                    (it.getDimensionScores() == null ? java.util.Collections.emptyMap() : it.getDimensionScores());

            List<String> row = new java.util.ArrayList<>();
            row.add(CsvUtils.nullToEmpty(courseId));
            row.add(CsvUtils.nullToEmpty(sid));
            row.add(CsvUtils.escape(studentName));
            row.add(CsvUtils.escape(studentNo));
            row.add(CsvUtils.nullToEmpty(it == null ? null : it.getProgress()));
            row.add(CsvUtils.nullToEmpty(it == null ? null : it.getCompletedLessons()));
            row.add(CsvUtils.nullToEmpty(it == null ? null : it.getTotalLessons()));
            row.add(CsvUtils.escape(it == null || it.getAverageGrade() == null ? "" : String.valueOf(it.getAverageGrade())));
            row.add(CsvUtils.escape(it == null ? "" : it.getActivityLevel()));
            row.add(CsvUtils.escape(it == null ? "" : it.getLastActiveAt()));
            row.add(CsvUtils.escape(it == null || it.getRadarArea() == null ? "" : String.valueOf(it.getRadarArea())));
            row.add(CsvUtils.escape(it == null ? "" : it.getRadarClassification()));

            double dimSum = 0D;
            int dimCount = 0;
            for (String code : dimCodes) {
                Double v = dimMap.get(code);
                row.add(CsvUtils.escape(v == null ? "" : String.format(java.util.Locale.US, "%.1f", v)));
                String lv = "";
                if (v != null) {
                    lv = Grade.getString(java.math.BigDecimal.valueOf(v));
                    dimSum += v;
                    dimCount++;
                }
                row.add(CsvUtils.escape(lv));
            }
            Double dimAvg = dimCount == 0 ? null : (dimSum / dimCount);
            row.add(CsvUtils.escape(dimAvg == null ? "" : String.format(java.util.Locale.US, "%.1f", dimAvg)));
            row.add(CsvUtils.escape(dimAvg == null ? "" : Grade.getString(java.math.BigDecimal.valueOf(dimAvg))));

            Map<Long, Grade> studentGrades = gradeByStudentAssignment.getOrDefault(sid, java.util.Collections.emptyMap());
            for (Assignment a : sortedAssignments) {
                Grade g = studentGrades.get(a.getId());
                row.add(CsvUtils.escape(a.getTitle() == null ? "" : a.getTitle()));
                row.add(CsvUtils.escape(g == null || g.getScore() == null ? "" : String.valueOf(g.getScore())));
                row.add(CsvUtils.escape(g == null || g.getPercentage() == null ? "" : String.valueOf(g.getPercentage())));
                row.add(CsvUtils.escape(g == null ? "" : g.getGradeLevel()));
            }
            summaryCsv.append(String.join(",", row)).append('\n');
        }

        // 2.2 作业成绩明细（增强列：学生姓名/学号 + 维度分数/等级 + 维度均分）
        StringBuilder gradesCsv = new StringBuilder();
        List<String> gradeHeaders = new java.util.ArrayList<>();
        gradeHeaders.add("courseId");
        gradeHeaders.add("studentId");
        gradeHeaders.add("studentName");
        gradeHeaders.add("studentNo");
        gradeHeaders.add("assignmentId");
        gradeHeaders.add("assignmentTitle");
        gradeHeaders.add("submissionId");
        gradeHeaders.add("score");
        gradeHeaders.add("maxScore");
        gradeHeaders.add("percentage");
        gradeHeaders.add("gradeLevel");
        gradeHeaders.add("status");
        gradeHeaders.add("publishedAt");
        gradeHeaders.add("updatedAt");
        for (String code : dimCodes) {
            gradeHeaders.add("dim_" + code + "_score");
            gradeHeaders.add("dim_" + code + "_level");
        }
        gradeHeaders.add("dimensionAverageScore");
        gradeHeaders.add("dimensionAverageLevel");
        gradesCsv.append(String.join(",", gradeHeaders.stream().map(CsvUtils::escape).collect(Collectors.toList()))).append('\n');

        if (grades != null) {
            for (Grade g : grades) {
                if (g == null) continue;
                Long sid = g.getStudentId();
                com.noncore.assessment.dto.response.CourseStudentPerformanceItem it = perfByStudentId.get(sid);
                User user = usersById.get(sid);
                String studentName = it != null && it.getStudentName() != null && !it.getStudentName().isBlank()
                        ? it.getStudentName()
                        : (user == null ? "" : (user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername()));
                String studentNo = it != null && it.getStudentNo() != null && !it.getStudentNo().isBlank()
                        ? it.getStudentNo()
                        : (user == null ? "" : user.getStudentNo());
                Map<String, Double> dimMap = it == null ? java.util.Collections.emptyMap() :
                        (it.getDimensionScores() == null ? java.util.Collections.emptyMap() : it.getDimensionScores());

                List<String> row = new java.util.ArrayList<>();
                Long aid = g.getAssignmentId();
                row.add(CsvUtils.nullToEmpty(courseId));
                row.add(CsvUtils.nullToEmpty(sid));
                row.add(CsvUtils.escape(studentName));
                row.add(CsvUtils.escape(studentNo));
                row.add(CsvUtils.nullToEmpty(aid));
                row.add(CsvUtils.escape(assignmentTitleMap.getOrDefault(aid, "")));
                row.add(CsvUtils.nullToEmpty(g.getSubmissionId()));
                row.add(CsvUtils.escape(g.getScore() == null ? "" : String.valueOf(g.getScore())));
                row.add(CsvUtils.escape(g.getMaxScore() == null ? "" : String.valueOf(g.getMaxScore())));
                row.add(CsvUtils.escape(g.getPercentage() == null ? "" : String.valueOf(g.getPercentage())));
                row.add(CsvUtils.escape(g.getGradeLevel()));
                row.add(CsvUtils.escape(g.getStatus()));
                row.add(CsvUtils.escape(g.getPublishedAt() == null ? "" : g.getPublishedAt().toString()));
                row.add(CsvUtils.escape(g.getUpdatedAt() == null ? "" : g.getUpdatedAt().toString()));

                double dimSum = 0D;
                int dimCount = 0;
                for (String code : dimCodes) {
                    Double v = dimMap.get(code);
                    row.add(CsvUtils.escape(v == null ? "" : String.format(java.util.Locale.US, "%.1f", v)));
                    String lv = "";
                    if (v != null) {
                        lv = Grade.getString(java.math.BigDecimal.valueOf(v));
                        dimSum += v;
                        dimCount++;
                    }
                    row.add(CsvUtils.escape(lv));
                }
                Double dimAvg = dimCount == 0 ? null : (dimSum / dimCount);
                row.add(CsvUtils.escape(dimAvg == null ? "" : String.format(java.util.Locale.US, "%.1f", dimAvg)));
                row.add(CsvUtils.escape(dimAvg == null ? "" : Grade.getString(java.math.BigDecimal.valueOf(dimAvg))));

                gradesCsv.append(String.join(",", row)).append('\n');
            }
        }

        // 3) 能力报告（历史）
        List<AbilityReport> reports;
        try {
            reports = abilityReportMapper.selectAdminReports(
                    null, null, null, null, courseId, null, null, null, null, 0, 50000
            );
        } catch (Exception e) {
            reports = java.util.Collections.emptyList();
        }
        StringBuilder reportsCsv = new StringBuilder();
        List<String> reportHeaders = new java.util.ArrayList<>();
        reportHeaders.add("courseId");
        reportHeaders.add("studentId");
        reportHeaders.add("studentName");
        reportHeaders.add("studentNo");
        reportHeaders.add("reportId");
        reportHeaders.add("reportType");
        reportHeaders.add("title");
        reportHeaders.add("overallScore");
        reportHeaders.add("assignmentId");
        reportHeaders.add("submissionId");
        reportHeaders.add("createdAt");
        for (String code : dimCodes) {
            reportHeaders.add("dim_" + code + "_score");
            reportHeaders.add("dim_" + code + "_level");
        }
        reportHeaders.add("dimensionAverageScore");
        reportHeaders.add("dimensionAverageLevel");
        reportsCsv.append(String.join(",", reportHeaders.stream().map(CsvUtils::escape).collect(Collectors.toList()))).append('\n');
        if (reports != null) {
            for (AbilityReport r : reports) {
                Long sid = r.getStudentId();
                com.noncore.assessment.dto.response.CourseStudentPerformanceItem it = perfByStudentId.get(sid);
                User user = usersById.get(sid);
                String studentName = (r.getStudentName() != null && !r.getStudentName().isBlank())
                        ? r.getStudentName()
                        : (it != null && it.getStudentName() != null && !it.getStudentName().isBlank()
                        ? it.getStudentName()
                        : (user == null ? "" : (user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername())));
                String studentNo = (r.getStudentNumber() != null && !r.getStudentNumber().isBlank())
                        ? r.getStudentNumber()
                        : (it != null && it.getStudentNo() != null && !it.getStudentNo().isBlank()
                        ? it.getStudentNo()
                        : (user == null ? "" : user.getStudentNo()));

                Map<String, Double> reportDimRaw = parseDimensionScores(r.getDimensionScores());
                Map<String, Double> perfDimRaw = (it != null && it.getDimensionScores() != null) ? it.getDimensionScores() : java.util.Collections.emptyMap();
                Map<String, Double> dimMap = normalizeDimensionMap(reportDimRaw, perfDimRaw);

                List<String> row = new java.util.ArrayList<>();
                row.add(CsvUtils.nullToEmpty(courseId));
                row.add(CsvUtils.nullToEmpty(sid));
                row.add(CsvUtils.escape(studentName));
                row.add(CsvUtils.escape(studentNo));
                row.add(CsvUtils.nullToEmpty(r.getId()));
                row.add(CsvUtils.escape(r.getReportType()));
                row.add(CsvUtils.escape(r.getTitle()));
                row.add(CsvUtils.escape(r.getOverallScore() == null ? "" : String.valueOf(r.getOverallScore())));
                row.add(CsvUtils.nullToEmpty(r.getAssignmentId()));
                row.add(CsvUtils.nullToEmpty(r.getSubmissionId()));
                row.add(CsvUtils.escape(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString()));

                double dimSum = 0D;
                int dimCount = 0;
                for (String code : dimCodes) {
                    Double v = dimMap.get(code);
                    row.add(CsvUtils.escape(v == null ? "" : String.format(java.util.Locale.US, "%.1f", v)));
                    String lv = "";
                    if (v != null) {
                        lv = Grade.getString(java.math.BigDecimal.valueOf(v));
                        dimSum += v;
                        dimCount++;
                    }
                    row.add(CsvUtils.escape(lv));
                }
                Double dimAvg = dimCount == 0 ? null : (dimSum / dimCount);
                row.add(CsvUtils.escape(dimAvg == null ? "" : String.format(java.util.Locale.US, "%.1f", dimAvg)));
                row.add(CsvUtils.escape(dimAvg == null ? "" : Grade.getString(java.math.BigDecimal.valueOf(dimAvg))));
                reportsCsv.append(String.join(",", row)).append('\n');
            }
        }

        // 4) 课堂笔记宽表（只取 notes 非空）
        List<AdminLessonNoteListItemResponse> notesRows;
        try {
            notesRows = lessonProgressMapper.selectAdminCourseLessonNotes(courseId, null, null, null, 0, 50000);
        } catch (Exception e) {
            notesRows = java.util.Collections.emptyList();
        }
        StringBuilder notesCsv = new StringBuilder();
        notesCsv.append("courseId,studentId,studentName,studentNo,lessonId,lessonTitle,chapterTitle,updatedAt,notes\n");
        if (notesRows != null) {
            for (AdminLessonNoteListItemResponse it : notesRows) {
                User user = usersById.get(it.getStudentId());
                String studentName = (it.getStudentName() != null && !it.getStudentName().isBlank())
                        ? it.getStudentName()
                        : (user == null ? "" : (user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : user.getUsername()));
                String studentNo = (it.getStudentNo() != null && !it.getStudentNo().isBlank())
                        ? it.getStudentNo()
                        : (user == null ? "" : user.getStudentNo());
                notesCsv.append(CsvUtils.nullToEmpty(courseId)).append(',')
                        .append(CsvUtils.nullToEmpty(it.getStudentId())).append(',')
                        .append(CsvUtils.escape(studentName)).append(',')
                        .append(CsvUtils.escape(studentNo)).append(',')
                        .append(CsvUtils.nullToEmpty(it.getLessonId())).append(',')
                        .append(CsvUtils.escape(it.getLessonTitle())).append(',')
                        .append(CsvUtils.escape(it.getChapterTitle())).append(',')
                        .append(CsvUtils.escape(it.getUpdatedAt() == null ? "" : it.getUpdatedAt().toString())).append(',')
                        .append(CsvUtils.escape(it.getNotes()))
                        .append('\n');
            }
        }

        // 5) ZIP
        byte[] zipBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("course_student_summary.csv"));
            zos.write(summaryCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("course_student_assignment_grades.csv"));
            zos.write(gradesCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("ability_reports.csv"));
            zos.write(reportsCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("course_lesson_notes.csv"));
            zos.write(notesCsv.toString().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.finish();
            zipBytes = baos.toByteArray();
        } catch (Exception e) {
            zipBytes = ("export failed: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }

        adminAuditLogService.record(
                getCurrentUserId(),
                "admin.export.courseData.zip",
                "export",
                courseId,
                buildCourseDataZipAudit(courseId, zipBytes == null ? 0 : zipBytes.length),
                httpRequest
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_data.zip");
        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }

    private Map<String, Object> auditDetailForLessonNotesZip(Long courseId, Long studentId, Long lessonId, String q, int rows) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("courseId", courseId);
        detail.put("studentId", studentId);
        detail.put("lessonId", lessonId);
        detail.put("q", q);
        detail.put("rows", rows);
        return detail;
    }

    private Map<String, Object> buildCourseDataZipAudit(Long courseId, int zipBytes) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("courseId", courseId);
        detail.put("zipBytes", zipBytes);
        return detail;
    }

    private List<Grade> loadGradesByExportFallback(Long courseId) {
        try {
            List<Map<String, Object>> rows = gradeMapper.selectGradesForExport(courseId, null, null);
            if (rows == null || rows.isEmpty()) return java.util.Collections.emptyList();
            List<Grade> list = new java.util.ArrayList<>();
            for (Map<String, Object> row : rows) {
                if (row == null) continue;
                Grade g = new Grade();
                g.setStudentId(toLong(row.get("studentId")));
                g.setAssignmentId(toLong(row.get("assignmentId")));
                g.setSubmissionId(toLong(row.get("submissionId")));
                g.setScore(toBigDecimal(row.get("score")));
                g.setMaxScore(toBigDecimal(row.get("maxScore")));
                g.setPercentage(toBigDecimal(row.get("percentage")));
                g.setGradeLevel(stringVal(row.get("gradeLevel")));
                g.setStatus(stringVal(row.get("status")));
                list.add(g);
            }
            return list;
        } catch (Exception ignore) {
            return java.util.Collections.emptyList();
        }
    }

    private Map<String, Double> parseDimensionScores(String json) {
        if (json == null || json.isBlank()) return java.util.Collections.emptyMap();
        try {
            Map<?, ?> raw = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
            Map<String, Double> out = new HashMap<>();
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                if (e == null || e.getKey() == null || e.getValue() == null) continue;
                String k = String.valueOf(e.getKey());
                try {
                    out.put(k, Double.parseDouble(String.valueOf(e.getValue())));
                } catch (Exception ignore) {}
            }
            return out;
        } catch (Exception ignore) {
            return java.util.Collections.emptyMap();
        }
    }

    private Map<String, Double> normalizeDimensionMap(Map<String, Double> preferred, Map<String, Double> fallback) {
        Map<String, Double> out = new HashMap<>();
        for (String code : java.util.List.of("MORAL_COGNITION", "LEARNING_ATTITUDE", "LEARNING_ABILITY", "LEARNING_METHOD", "ACADEMIC_GRADE")) {
            Double p = readDimensionValueByAnyKey(preferred, code);
            Double f = readDimensionValueByAnyKey(fallback, code);
            if (p != null) out.put(code, p);
            else if (f != null) out.put(code, f);
        }
        return out;
    }

    private Double readDimensionValueByAnyKey(Map<String, Double> source, String canonicalCode) {
        if (source == null || source.isEmpty()) return null;
        // direct hit
        if (source.containsKey(canonicalCode)) return source.get(canonicalCode);
        // case-insensitive hit
        for (Map.Entry<String, Double> e : source.entrySet()) {
            if (e.getKey() != null && canonicalCode.equalsIgnoreCase(e.getKey())) return e.getValue();
        }
        // known Chinese aliases
        String zh = switch (canonicalCode) {
            case "MORAL_COGNITION" -> "道德认知";
            case "LEARNING_ATTITUDE" -> "学习态度";
            case "LEARNING_ABILITY" -> "学习能力";
            case "LEARNING_METHOD" -> "学习方法";
            case "ACADEMIC_GRADE" -> "学习成绩";
            default -> "";
        };
        if (!zh.isEmpty()) {
            for (Map.Entry<String, Double> e : source.entrySet()) {
                if (e.getKey() != null && (e.getKey().contains(zh) || zh.contains(e.getKey()))) return e.getValue();
            }
        }
        // fuzzy alias hit (different separators/casing)
        String normCode = canonicalCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        for (Map.Entry<String, Double> e : source.entrySet()) {
            if (e.getKey() == null) continue;
            String k = e.getKey().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            if (k.equals(normCode)) return e.getValue();
        }
        return null;
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private java.math.BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        try { return new java.math.BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private String stringVal(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    /**
     * 简单健康检查/占位，避免前端导出中心初期空白。
     */
    @GetMapping("/capabilities")
    @Operation(summary = "导出能力列表")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "formats", java.util.List.of("csv", "zip"),
                "maxRows", 50000,
                "exports", java.util.List.of(
                        java.util.Map.of("key", "users", "path", "/api/admin/exports/users.csv"),
                        java.util.Map.of("key", "abilityReports", "path", "/api/admin/exports/ability-reports.csv"),
                        java.util.Map.of("key", "courseStudents", "path", "/api/admin/exports/course-students.csv?courseId={courseId}"),
                        java.util.Map.of("key", "courseLessonNotes", "path", "/api/admin/exports/course-lesson-notes.csv?courseId={courseId}"),
                        java.util.Map.of("key", "courseDataZip", "path", "/api/admin/exports/course-data.zip?courseId={courseId}"),
                        java.util.Map.of("key", "courseStudentDataZip", "path", "/api/admin/exports/course-student-data.zip?courseId={courseId}&studentId={studentId}"),
                        java.util.Map.of("key", "aiConversations", "path", "/api/admin/exports/ai-conversations.csv?studentId={studentId}"),
                        java.util.Map.of("key", "voiceSessions", "path", "/api/admin/exports/voice-sessions.csv?studentId={studentId}")
                )
        )));
    }
}

