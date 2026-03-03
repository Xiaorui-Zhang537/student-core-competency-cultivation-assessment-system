package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.StudentAnalysisResponse;
import com.noncore.assessment.entity.StudentAbility;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.mapper.SubmissionMapper;
import com.noncore.assessment.mapper.StudentAbilityMapper;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.StudentAnalysisService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StudentAnalysisServiceImpl implements StudentAnalysisService {

    private final GradeMapper gradeMapper;
    private final LessonProgressMapper lessonProgressMapper;
    @SuppressWarnings("unused")
    private final SubmissionMapper submissionMapper; // reserved for completion rate trend
    private final StudentAbilityMapper studentAbilityMapper; // reserved for radar dimensions
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;

    public StudentAnalysisServiceImpl(GradeMapper gradeMapper,
                                      LessonProgressMapper lessonProgressMapper,
                                      SubmissionMapper submissionMapper,
                                      StudentAbilityMapper studentAbilityMapper,
                                      AssignmentMapper assignmentMapper,
                                      CourseMapper courseMapper) {
        this.gradeMapper = gradeMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.submissionMapper = submissionMapper;
        this.studentAbilityMapper = studentAbilityMapper;
        this.assignmentMapper = assignmentMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public StudentAnalysisResponse getStudentAnalysis(Long studentId, String range) {
        String safeRange = (range == null || range.isBlank()) ? "30d" : range;

        // KPI
        BigDecimal avgScoreBD = gradeMapper.calculateAverageScore(studentId);
        double avgScore = avgScoreBD != null ? avgScoreBD.doubleValue() : 0.0;

        // 完成率：用“学生已提交作业数 / 学生关联作业总数”粗略估算
        double completionRate = 0.0;
        try {
            // 已提交（去重到作业级别）
            java.util.List<java.util.Map<String, Object>> submitted = submissionMapper.selectStudentGradesWithPagination(studentId, 0, Integer.MAX_VALUE);
            java.util.Set<Object> submittedAssignmentIds = new java.util.HashSet<>();
            for (var row : submitted) {
                Object aid = row.get("assignment_id");
                if (aid != null) submittedAssignmentIds.add(aid);
            }
            int submittedCount = submittedAssignmentIds.size();
            // 总作业：根据学生-课程关联推导（此处用成绩导出接口的轻量数据作为兜底）
            java.util.List<java.util.Map<String, Object>> allGradesExport = gradeMapper.selectGradesForExport(null, null, studentId);
            java.util.Set<Object> allAssignmentIds = new java.util.HashSet<>();
            for (var row : allGradesExport) {
                Object aid = row.get("assignment_id");
                if (aid != null) allAssignmentIds.add(aid);
            }
            int totalRelated = Math.max(allAssignmentIds.size(), submittedCount);
            if (totalRelated > 0) {
                completionRate = Math.min(100.0, (submittedCount * 100.0) / totalRelated);
            }
        } catch (Exception ignore) {}

        // 学习时长：按区间小时
        long studyMinutes = toRangeStudyMinutes(studentId, safeRange);
        long studyHours = Math.max(0, Math.round(studyMinutes / 60.0));

        long activeDays = estimateActiveDays(safeRange, studyMinutes);

        StudentAnalysisResponse.Kpi kpi = StudentAnalysisResponse.Kpi.builder()
                .avgScore(avgScore)
                .completionRate(completionRate)
                .studyHours(studyHours)
                .activeDays(activeDays)
                .build();

        StudentAnalysisResponse.Radar radar = buildRadar(studentId, avgScore, completionRate);

        // 趋势：成绩趋势（近区间）使用 GradeMapper 已有方法
        List<StudentAnalysisResponse.Point> scoreTrend = new ArrayList<>();
        LocalDateTime[] rangeTimes = toRange(safeRange);
        List<Map<String, Object>> scoreList = gradeMapper.selectDailyAvgScore(studentId, rangeTimes[0], rangeTimes[1]);
        for (Map<String, Object> row : scoreList) {
            String x = Objects.toString(row.getOrDefault("date", row.getOrDefault("x", "")));
            Double y = toDouble(row.get("score"), row.get("y"));
            scoreTrend.add(StudentAnalysisResponse.Point.builder().x(x).y(y).build());
        }

        // 完成率趋势：按天 M/N（当天提交/当天需完成）
        List<StudentAnalysisResponse.Point> completionTrend = new ArrayList<>();
        try {
            java.time.LocalDate startD = rangeTimes[0].toLocalDate();
            java.time.LocalDate endD = rangeTimes[1].toLocalDate();

            // 学生所有课程及其作业（内存过滤）
            List<Course> myCourses = courseMapper.selectCoursesByStudentId(studentId);
            List<Assignment> allAssignments = new ArrayList<>();
            for (Course c : myCourses) {
                if (c == null || c.getId() == null) continue;
                List<Assignment> list = assignmentMapper.selectAssignmentsByCourseId(c.getId());
                if (list != null) allAssignments.addAll(list);
            }
            // 学生所有提交（用于当天提交计数）
            List<com.noncore.assessment.entity.Submission> mySubs = submissionMapper.selectByStudentId(studentId);

            for (java.time.LocalDate d = startD; !d.isAfter(endD); d = d.plusDays(1)) {
                // 分母 N：当天需要完成的作业（状态published 且 截止 >= d）
                int need = 0;
                for (Assignment a : allAssignments) {
                    if (a == null) continue;
                    String st = a.getStatus() == null ? "" : a.getStatus().toLowerCase();
                    java.time.LocalDate due = null;
                    try { if (a.getDueDate() != null) due = a.getDueDate().toLocalDate(); } catch (Exception ignore) {}
                    boolean published = "published".equals(st);
                    boolean notExpired = (due == null) || !due.isBefore(d);
                    if (published && notExpired) need++;
                }
                // 分子 M：当天提交的作业数量（去重 assignmentId）
                int done = 0;
                if (mySubs != null && !mySubs.isEmpty()) {
                    java.util.Set<Long> daily = new java.util.HashSet<>();
                    for (var s : mySubs) {
                        if (s == null || s.getSubmittedAt() == null) continue;
                        java.time.LocalDate sd = null;
                        try { sd = s.getSubmittedAt().toLocalDate(); } catch (Exception ignore) {}
                        if (sd != null && sd.equals(d) && s.getAssignmentId() != null) {
                            daily.add(s.getAssignmentId());
                        }
                    }
                    done = daily.size();
                }
                double rate = (need > 0) ? Math.min(100.0, (done * 100.0) / need) : 0.0;
                completionTrend.add(StudentAnalysisResponse.Point.builder().x(d.toString()).y(rate).build());
            }
        } catch (Exception ignore) {}
        // 学习时长趋势：复用热力图数据（天粒度的分钟数→小时）
        List<StudentAnalysisResponse.Point> hoursTrend = new ArrayList<>();
        try {
            int days = switch (safeRange) { case "7d" -> 7; case "term" -> 120; default -> 30; };
            List<Map<String, Object>> heat = lessonProgressMapper.getStudyHeatmapData(studentId, days);
            for (Map<String, Object> r : heat) {
                String x = Objects.toString(r.getOrDefault("date", r.getOrDefault("day", r.getOrDefault("x", ""))));
                Double minutes = toDouble(r.get("minutes"), r.get("value"), r.get("y"));
                double hours = minutes == null ? 0.0 : minutes / 60.0;
                hoursTrend.add(StudentAnalysisResponse.Point.builder().x(x).y(hours).build());
            }
        } catch (Exception ignore) {}

        StudentAnalysisResponse.Trends trends = StudentAnalysisResponse.Trends.builder()
                .score(scoreTrend)
                .completion(completionTrend)
                .hours(hoursTrend)
                .build();

        // 最近成绩：可从 GradeMapper.selectByStudentId 限制TopN（无limit方法，先取全部再截断）
        List<Map<String, Object>> recent = gradeMapper.selectGradesForExport(null, null, studentId);
        List<StudentAnalysisResponse.RecentGradeItem> recentGrades = new ArrayList<>();
        int count = 0;
        for (Map<String, Object> m : recent) {
            if (count >= 10) break;
            StudentAnalysisResponse.RecentGradeItem item = StudentAnalysisResponse.RecentGradeItem.builder()
                    .assignmentTitle(Objects.toString(m.getOrDefault("assignment_title", "")))
                    .courseTitle(Objects.toString(m.getOrDefault("course_title", "")))
                    .score((m.get("score") instanceof BigDecimal) ? (BigDecimal) m.get("score") : null)
                    .gradedAt(Objects.toString(m.getOrDefault("graded_at", m.getOrDefault("created_at", ""))))
                    .build();
            recentGrades.add(item);
            count++;
        }

        return StudentAnalysisResponse.builder()
                .kpi(kpi)
                .radar(radar)
                .trends(trends)
                .recentGrades(recentGrades)
                .build();
    }

    private LocalDateTime[] toRange(String range) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (range) {
            case "7d" -> start = end.minusDays(7);
            case "term" -> start = end.minusMonths(4); // 简化：一学期按4个月
            default -> start = end.minusDays(30);
        }
        return new LocalDateTime[]{start, end};
    }

    private long toRangeStudyMinutes(Long studentId, String range) {
        // 目前 LessonProgressMapper 只提供 total/weekly，暂用 total 在区间内估算（简化）
        Integer minutes = lessonProgressMapper.calculateTotalStudyTime(studentId, null);
        return minutes == null ? 0L : minutes.longValue();
    }

    private long estimateActiveDays(String range, long studyMinutes) {
        // 占位估算：有学习记录则按范围天数的三分之一
        int days = switch (range) { case "7d" -> 7; case "term" -> 120; default -> 30; };
        if (studyMinutes <= 0) return 0;
        return Math.max(1, days / 3);
    }

    private Double toDouble(Object... candidates) {
        for (Object o : candidates) {
            if (o == null) continue;
            if (o instanceof Number n) return n.doubleValue();
            try {
                return Double.parseDouble(String.valueOf(o));
            } catch (Exception ignore) {}
        }
        return 0.0;
    }

    private StudentAnalysisResponse.Radar buildRadar(Long studentId, double avgScore, double completionRate) {
        List<StudentAbility> snapshots = new ArrayList<>();
        try {
            List<StudentAbility> fetched = studentAbilityMapper.selectByStudentId(studentId);
            if (fetched != null) {
                snapshots = fetched;
            }
        } catch (Exception ignore) {}

        Map<String, Double> dims = extractDimensionScores(snapshots);
        double overall = average(dims.values());

        double invest = firstPositive(
                dims.get("LEARNING_ATTITUDE"),
                completionRate,
                overall
        );
        double quality = firstPositive(
                clamp100(avgScore),
                overall
        );
        double mastery = firstPositive(
                dims.get("LEARNING_ABILITY"),
                overall
        );
        double stability = firstPositive(
                dims.get("MORAL_COGNITION"),
                overall
        );
        double growth = firstPositive(
                dims.get("LEARNING_METHOD"),
                average(List.of(invest, mastery, stability)),
                overall
        );

        return StudentAnalysisResponse.Radar.builder()
                .invest(round2(invest))
                .quality(round2(quality))
                .mastery(round2(mastery))
                .stability(round2(stability))
                .growth(round2(growth))
                .build();
    }

    private Map<String, Double> extractDimensionScores(List<StudentAbility> snapshots) {
        Map<String, Double> scores = new HashMap<>();
        if (snapshots == null || snapshots.isEmpty()) {
            return scores;
        }

        for (StudentAbility item : snapshots) {
            if (item == null || item.getDimensionName() == null || item.getCurrentScore() == null) {
                continue;
            }
            double normalized = normalizeAbilityScore(item.getCurrentScore());
            switch (item.getDimensionName()) {
                case "道德认知" -> scores.put("MORAL_COGNITION", normalized);
                case "学习态度" -> scores.put("LEARNING_ATTITUDE", normalized);
                case "学习能力" -> scores.put("LEARNING_ABILITY", normalized);
                case "学习方法" -> scores.put("LEARNING_METHOD", normalized);
                default -> {
                    // Ignore unknown dimensions in the student overview radar.
                }
            }
        }

        return scores;
    }

    private double normalizeAbilityScore(BigDecimal score) {
        if (score == null) {
            return 0.0;
        }
        double value = score.doubleValue();
        if (value <= 5.0) {
            value = value * 20.0;
        }
        return clamp100(value);
    }

    private double clamp100(double value) {
        if (value < 0) return 0.0;
        if (value > 100) return 100.0;
        return value;
    }

    private double firstPositive(Double... candidates) {
        for (Double candidate : candidates) {
            if (candidate != null && candidate > 0) {
                return candidate;
            }
        }
        return 0.0;
    }

    private double average(Iterable<Double> values) {
        if (values == null) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (Double value : values) {
            if (value == null || value <= 0) {
                continue;
            }
            sum += value;
            count++;
        }
        if (count == 0) {
            return 0.0;
        }
        return sum / count;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

