package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.entity.AiVoiceSession;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.mapper.AdminUserMapper;
import com.noncore.assessment.mapper.AiVoiceSessionMapper;
import com.noncore.assessment.mapper.UserMapper;
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
import java.util.List;
import java.util.Map;

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

    public AdminExportsController(AdminUserMapper adminUserMapper,
                                  AbilityReportMapper abilityReportMapper,
                                  AdminAuditLogService adminAuditLogService,
                                  UserMapper userMapper,
                                  AiConversationService aiConversationService,
                                  AiVoiceSessionMapper aiVoiceSessionMapper,
                                  UserService userService) {
        super(userService);
        this.adminUserMapper = adminUserMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.adminAuditLogService = adminAuditLogService;
        this.userMapper = userMapper;
        this.aiConversationService = aiConversationService;
        this.aiVoiceSessionMapper = aiVoiceSessionMapper;
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
        sb.append("id,username,email,role,status,nickname,studentNo,teacherNo,emailVerified,deleted,createdAt\n");
        if (items != null) {
            for (AdminUserListItemResponse u : items) {
                sb.append(CsvUtils.nullToEmpty(u.getId())).append(',')
                        .append(CsvUtils.escape(u.getUsername())).append(',')
                        .append(CsvUtils.escape(u.getEmail())).append(',')
                        .append(CsvUtils.escape(u.getRole())).append(',')
                        .append(CsvUtils.escape(u.getStatus())).append(',')
                        .append(CsvUtils.escape(u.getNickname())).append(',')
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
                studentId, reportType, isPublished, courseId, assignmentId, submissionId, start, end, 0, size
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

    /**
     * 简单健康检查/占位，避免前端导出中心初期空白。
     */
    @GetMapping("/capabilities")
    @Operation(summary = "导出能力列表")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "formats", java.util.List.of("csv"),
                "maxRows", 50000,
                "exports", java.util.List.of(
                        java.util.Map.of("key", "users", "path", "/api/admin/exports/users.csv"),
                        java.util.Map.of("key", "abilityReports", "path", "/api/admin/exports/ability-reports.csv"),
                        java.util.Map.of("key", "courseStudents", "path", "/api/admin/exports/course-students.csv?courseId={courseId}"),
                        java.util.Map.of("key", "aiConversations", "path", "/api/admin/exports/ai-conversations.csv?studentId={studentId}"),
                        java.util.Map.of("key", "voiceSessions", "path", "/api/admin/exports/voice-sessions.csv?studentId={studentId}")
                )
        )));
    }
}

