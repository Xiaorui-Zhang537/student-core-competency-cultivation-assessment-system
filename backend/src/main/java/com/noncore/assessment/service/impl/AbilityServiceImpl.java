package com.noncore.assessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.behavior.BehaviorEventRecorder;
import com.noncore.assessment.behavior.BehaviorEventType;
import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.AbilityService;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 能力评估服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class AbilityServiceImpl implements AbilityService {

    private static final Logger logger = LoggerFactory.getLogger(AbilityServiceImpl.class);
    private static final long GOAL_REMINDER_LOOKAHEAD_DAYS = 3;
    private static final long GOAL_REMINDER_ON_DEMAND_THROTTLE_SECONDS = 60;

    private final AbilityDimensionMapper abilityDimensionMapper;
    private final StudentAbilityMapper studentAbilityMapper;
    private final AbilityAssessmentMapper abilityAssessmentMapper;
    private final AbilityReportMapper abilityReportMapper;
    private final LearningRecommendationMapper learningRecommendationMapper;
    private final AbilityGoalMapper abilityGoalMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final BehaviorEventRecorder behaviorEventRecorder;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final Map<Long, LocalDateTime> goalReminderOnDemandSyncedAt = new ConcurrentHashMap<>();

    public AbilityServiceImpl(AbilityDimensionMapper abilityDimensionMapper,
                              StudentAbilityMapper studentAbilityMapper,
                              AbilityAssessmentMapper abilityAssessmentMapper,
                              AbilityReportMapper abilityReportMapper,
                              LearningRecommendationMapper learningRecommendationMapper,
                              AbilityGoalMapper abilityGoalMapper,
                              EnrollmentMapper enrollmentMapper,
                              BehaviorEventRecorder behaviorEventRecorder,
                              NotificationService notificationService,
                              ObjectMapper objectMapper) {
        this.abilityDimensionMapper = abilityDimensionMapper;
        this.studentAbilityMapper = studentAbilityMapper;
        this.abilityAssessmentMapper = abilityAssessmentMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.learningRecommendationMapper = learningRecommendationMapper;
        this.abilityGoalMapper = abilityGoalMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.behaviorEventRecorder = behaviorEventRecorder;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> getStudentAbilityDashboard(Long studentId) {
        logger.info("获取学生能力仪表板数据，学生ID: {}", studentId);

        Map<String, Object> dashboard = new HashMap<>();

        // 获取学生所有能力记录
        List<StudentAbility> abilities = studentAbilityMapper.selectByStudentId(studentId);
        dashboard.put("abilities", abilities);

        // 计算综合能力分数
        BigDecimal overallScore = calculateOverallScore(abilities);
        dashboard.put("overallScore", overallScore);

        // 获取能力统计信息
        Map<String, Object> stats = studentAbilityMapper.getStudentAbilityStats(studentId);
        dashboard.put("stats", stats);

        // 获取最近的评估记录
        List<AbilityAssessment> recentAssessments = abilityAssessmentMapper.getRecentAssessments(studentId, 5);
        dashboard.put("recentAssessments", recentAssessments);

        // 获取活跃目标
        List<AbilityGoal> activeGoals = abilityGoalMapper.selectActiveGoalsByStudent(studentId);
        dashboard.put("activeGoals", activeGoals);

        // 获取学习建议
        List<LearningRecommendation> recommendations = learningRecommendationMapper.selectByStudentId(studentId, 5);
        dashboard.put("recommendations", recommendations);

        logger.info("成功获取学生能力仪表板数据");
        return dashboard;
    }

    @Override
    public AbilityReport generateStudentReport(Long studentId, String reportType, LocalDate periodStart, LocalDate periodEnd) {
        logger.info("生成学生能力报告，学生ID: {}, 类型: {}", studentId, reportType);

        // 创建报告对象
        AbilityReport report = new AbilityReport(studentId, reportType, periodStart, periodEnd);

        // 获取报告期间的评估数据
        List<AbilityAssessment> assessments = abilityAssessmentMapper.selectByStudentAndPeriod(
            studentId, periodStart.atStartOfDay(), periodEnd.atTime(23, 59, 59));

        // 计算各维度得分
        Map<String, BigDecimal> dimensionScores = calculateDimensionScores(assessments);
        report.setDimensionScores(convertMapToJson(dimensionScores));

        // 计算综合得分
        BigDecimal overallScore = calculateWeightedOverallScore(dimensionScores);
        report.setOverallScore(overallScore);

        // 生成趋势分析
        String trendsAnalysis = generateTrendsAnalysis(studentId, assessments);
        report.setTrendsAnalysis(trendsAnalysis);

        // 生成成就列表
        List<Map<String, String>> achievements = generateAchievements(assessments);
        report.setAchievements(convertListToJson(achievements));

        // 生成改进建议
        List<Map<String, String>> improvements = generateImprovementAreas(studentId, dimensionScores);
        report.setImprovementAreas(convertListToJson(improvements));

        // 生成综合建议
        String recommendations = generateRecommendations(dimensionScores, achievements.size());
        report.setRecommendations(recommendations);

        // 保存报告
        abilityReportMapper.insertReport(report);

        logger.info("成功生成学生能力报告，报告ID: {}", report.getId());
        return report;
    }

    // 解析任意对象为 double，失败返回 0.0
    private static double toDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    @Override
    public void recordAbilityAssessment(Long studentId, Long dimensionId, BigDecimal score, 
                                       String assessmentType, Long relatedId, String evidence) {
        logger.info("记录能力评估，学生ID: {}, 维度ID: {}, 得分: {}", studentId, dimensionId, score);

        // 创建评估记录
        AbilityAssessment assessment = new AbilityAssessment();
        assessment.setStudentId(studentId);
        assessment.setDimensionId(dimensionId);
        assessment.setScore(score);
        // 量纲修正：若前端传入 0-5 刻度（AI 四维与学习成绩均按照 0-5 写入），则将满分记为 5；
        // 其他场景保持 100。
        double s = score == null ? 0.0 : score.doubleValue();
        assessment.setMaxScore(BigDecimal.valueOf(s <= 5.0 ? 5 : 100));
        assessment.setAssessmentType(assessmentType);
        assessment.setRelatedId(relatedId);
        assessment.setEvidence(evidence);
        assessment.setStatus("completed");
        // 关键：设置评估时间，避免 assessed_at 为空导致聚合查询（按时间过滤）统计不到
        assessment.setAssessedAt(java.time.LocalDateTime.now());

        // 保存评估记录
        abilityAssessmentMapper.insertAssessment(assessment);

        // 更新学生能力记录
        updateStudentAbilityFromAssessment(studentId, dimensionId, score);

        // 检查是否达成目标
        checkAndUpdateGoalProgress(studentId, dimensionId, score);

        logger.info("成功记录能力评估");
    }

    @Override
    public void acceptRecommendation(Long recommendationId, Long studentId) {
        logger.info("接受学习建议，建议ID: {}, 学生ID: {}", recommendationId, studentId);

        LearningRecommendation recommendation = requireOwnedRecommendation(recommendationId, studentId);
        if (!Boolean.TRUE.equals(recommendation.getIsRead())) {
            learningRecommendationMapper.markAsRead(recommendation.getId());
        }
        if (!Boolean.TRUE.equals(recommendation.getIsAccepted())) {
            learningRecommendationMapper.markAsAccepted(recommendation.getId());
        }

        logger.info("学习建议已接受，建议ID: {}, 学生ID: {}", recommendationId, studentId);
    }

    @Override
    public void markRecommendationAsRead(Long recommendationId, Long studentId) {
        logger.info("标记学习建议为已读，建议ID: {}, 学生ID: {}", recommendationId, studentId);

        LearningRecommendation recommendation = requireOwnedRecommendation(recommendationId, studentId);
        if (!Boolean.TRUE.equals(recommendation.getIsRead())) {
            learningRecommendationMapper.markAsRead(recommendation.getId());
        }

        logger.info("学习建议已标记为已读，建议ID: {}, 学生ID: {}", recommendationId, studentId);
    }

    @Override
    public void publishAbilityReport(Long reportId) {
        logger.info("发布能力报告，报告ID: {}", reportId);

        if (reportId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "报告ID不能为空");
        }

        AbilityReport report = abilityReportMapper.selectReportById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "能力报告不存在");
        }

        if (!Boolean.TRUE.equals(report.getIsPublished())) {
            int updated = abilityReportMapper.publishReport(reportId);
            if (updated < 1) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "能力报告发布失败");
            }
        }

        logger.info("能力报告发布完成，报告ID: {}", reportId);
    }

    @Override
    public AbilityReport getAbilityReportById(Long reportId) {
        logger.info("获取能力报告详情，报告ID: {}", reportId);

        AbilityReport report = abilityReportMapper.selectReportById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "能力报告不存在");
        }

        logger.info("能力报告获取成功，报告ID: {}", reportId);
        return report;
    }

    @Override
    public PageResult<AbilityReport> getAbilityReportHistory(Long studentId, Integer page, Integer size) {
        logger.info("获取能力报告历史，学生ID: {}, 页码: {}, 页大小: {}", studentId, page, size);

        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }

        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : Math.min(size, 100);
        int offset = (safePage - 1) * safeSize;

        List<AbilityReport> items = abilityReportMapper.selectReportsWithPagination(studentId, null, null, offset, safeSize);
        Integer total = abilityReportMapper.countReports(studentId, null, null);

        PageResult<AbilityReport> result = new PageResult<>();
        result.setItems(items == null ? new ArrayList<>() : items);
        result.setTotal(total == null ? 0L : total.longValue());
        result.setTotalPages(result.getTotal() == 0 ? 0 : (int) Math.ceil((double) result.getTotal() / safeSize));
        result.setPage(safePage);
        result.setSize(safeSize);

        logger.info("能力报告历史获取成功，学生ID: {}", studentId);
        return result;
    }

    @Override
    public AbilityReport getLatestAbilityReport(Long studentId) {
        logger.info("获取学生最新能力报告，学生ID: {}", studentId);
        
        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }
        
        AbilityReport latestReport = abilityReportMapper.selectLatestReportByStudent(studentId);
        
        if (latestReport == null) {
            logger.info("学生尚无能力报告，学生ID: {}", studentId);
            return null;
        }
        
        logger.info("获取学生最新能力报告成功，学生ID: {}, 报告ID: {}", studentId, latestReport.getId());
        return latestReport;
    }

    @Override
    public AbilityReport getLatestAbilityReportByContext(Long studentId, Long courseId, Long assignmentId, Long submissionId) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("studentId", studentId);
        if (submissionId != null) {
            params.put("submissionId", submissionId);
        } else if (assignmentId != null) {
            params.put("assignmentId", assignmentId);
        } else if (courseId != null) {
            params.put("courseId", courseId);
        }
        try {
            AbilityReport r = abilityReportMapper.selectLatestReportByContext(params);
            if (r != null) return r;
        } catch (Exception ignored) {}
        return getLatestAbilityReport(studentId);
    }

    @Override
    public AbilityReport createReportFromAi(Long studentId,
                                            String normalizedJson,
                                            String title,
                                            Long courseId,
                                            Long assignmentId,
                                            Long submissionId,
                                            Long aiHistoryId) {
        logger.info("基于AI结果创建能力报告，学生ID: {}", studentId);
        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }
        AbilityReport report = new AbilityReport();
        report.setStudentId(studentId);
        report.setReportType("ai");
        report.setTitle(title != null ? title : "AI 批改报告");
        // 设定报告周期（AI 报告按当天周期持久化，满足非空约束）
        java.time.LocalDate today = java.time.LocalDate.now();
        report.setReportPeriodStart(today);
        report.setReportPeriodEnd(today);
        // 标记生成方式
        report.setGeneratedBy("teacher");
        report.setIsPublished(Boolean.FALSE);
        // 将规范化JSON中的维度均分作为 dimensionScores；若无法解析则直接存文本
        try {
            Map<?,?> obj = objectMapper.readValue(String.valueOf(normalizedJson), Map.class);
            Object overall = obj.get("overall");
            Map<String, Object> dim = null;
            if (overall instanceof Map) {
                Object dm = ((Map<?,?>) overall).get("dimension_averages");
                if (dm instanceof Map) {
                    dim = new HashMap<>();
                    dim.putAll((Map<String, Object>) dm);
                }
            }
            if (dim != null) {
                report.setDimensionScores(objectMapper.writeValueAsString(dim));
            } else {
                report.setDimensionScores(objectMapper.writeValueAsString(Collections.emptyMap()));
            }
            // 保存完整归一化 JSON 以供前端展示小维度分析
            report.setTrendsAnalysis(String.valueOf(normalizedJson));
            // overall.final_score → overallScore(0-5)转百分制简单乘20
            double final5 = 0.0;
            if (overall instanceof Map) {
                Object s = ((Map<?,?>) overall).get("final_score");
                if (s != null) final5 = Double.parseDouble(String.valueOf(s));
            }
            report.setOverallScore(BigDecimal.valueOf(Math.round(final5 * 20.0 * 100) / 100.0));
            // 综合建议
            String holistic = null;
            if (overall instanceof Map) {
                Object h = ((Map<?,?>) overall).get("holistic_feedback");
                if (h != null) holistic = String.valueOf(h);
            }
            // 1) 先从四个维度聚合所有小维度的 suggestions
            java.util.List<String> allSuggestions = new java.util.ArrayList<>();
            String[] groupKeys = new String[]{"moral_reasoning","attitude_development","ability_growth","strategy_optimization"};
            for (String gk : groupKeys) {
                Object grpRaw = obj.get(gk);
                if (grpRaw instanceof Map<?,?> grp) {
                    for (Object sk : grp.keySet()) {
                        Object secRaw = grp.get(sk);
                        if (secRaw instanceof Map<?,?> sec) {
                            Object sraw = sec.get("suggestions");
                            if (sraw instanceof java.util.Collection<?> coll) {
                                for (Object s : coll) {
                                    String sv = s == null ? "" : String.valueOf(s).trim();
                                    if (!sv.isEmpty()) allSuggestions.add(sv);
                                    if (allSuggestions.size() >= 24) break;
                                }
                            } else if (sraw != null) {
                                String sv = String.valueOf(sraw).trim();
                                if (!sv.isEmpty()) allSuggestions.add(sv);
                            }
                        }
                    }
                }
            }
            // 2) 再从 holistic 中追加 "Key suggestions" 段
            if (holistic != null && !holistic.isBlank()) {
                String[] lines = holistic.replace("\r", "\n").split("\n");
                boolean inKeys = false;
                for (String raw : lines) {
                    String ln = raw == null ? "" : raw.trim();
                    if (ln.isEmpty()) continue;
                    if (!inKeys) {
                        if (ln.toLowerCase().contains("key suggestions")) { inKeys = true; }
                        continue;
                    }
                    String item = ln;
                    if (item.startsWith("- ")) item = item.substring(2).trim();
                    if (item.startsWith("• ")) item = item.substring(2).trim();
                    if (!item.isEmpty()) allSuggestions.add(item);
                    if (allSuggestions.size() >= 32) break;
                }
            }
            // 写入改进建议（去重）
            if (!allSuggestions.isEmpty()) {
                java.util.LinkedHashSet<String> dedup = new java.util.LinkedHashSet<>(allSuggestions);
                report.setImprovementAreas(objectMapper.writeValueAsString(new java.util.ArrayList<>(dedup)));
            }
            // 若 holistic 为空，则用维度均分与部分建议拼接为 recommendations，避免前端空白
            if (holistic != null && !holistic.isBlank()) {
                report.setRecommendations(holistic);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Overall: %.1f/5. ", final5));
                if (dim != null) {
                    double mr = toDouble(dim.get("moral_reasoning"));
                    double at = toDouble(dim.get("attitude"));
                    double ab = toDouble(dim.get("ability"));
                    double st = toDouble(dim.get("strategy"));
                    sb.append(String.format("Moral: %.1f, Attitude: %.1f, Ability: %.1f, Strategy: %.1f. ", mr, at, ab, st));
                }
                if (!allSuggestions.isEmpty()) {
                    sb.append("Key suggestions:\n");
                    int max = Math.min(6, allSuggestions.size());
                    for (int i = 0; i < max; i++) sb.append("- ").append(allSuggestions.get(i)).append("\n");
                }
                report.setRecommendations(sb.toString().trim());
            }
        } catch (Exception e) {
            logger.warn("解析AI规范化结果失败，将原文保存到recommendations字段: {}", e.getMessage());
            report.setRecommendations(String.valueOf(normalizedJson));
        }
        // 保存并带上上下文关联
        report.setCourseId(courseId);
        report.setAssignmentId(assignmentId);
        report.setSubmissionId(submissionId);
        if (aiHistoryId != null) {
            report.setAiHistoryId(aiHistoryId);
        }
        abilityReportMapper.insertReport(report);
        return report;
    }

    @Override
    public AbilityAssessment submitSelfAssessment(Long studentId, Long dimensionId, BigDecimal score, String feedback) {
        logger.info("提交自我评估，学生ID: {}, 维度ID: {}, 得分: {}", studentId, dimensionId, score);

        if (studentId == null || dimensionId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID和维度ID不能为空");
        }

        requireActiveDimension(dimensionId);
        BigDecimal normalizedScore = normalizeGoalScore(score);
        if (normalizedScore == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "自评分数不能为空");
        }

        AbilityAssessment assessment = new AbilityAssessment();
        assessment.setStudentId(studentId);
        assessment.setDimensionId(dimensionId);
        assessment.setScore(normalizedScore);
        assessment.setMaxScore(BigDecimal.valueOf(5).setScale(2, java.math.RoundingMode.HALF_UP));
        assessment.setEvidence(normalizeOptionalText(feedback));
        assessment.setAssessmentType("self");
        assessment.setStatus("completed");
        assessment.setAssessorId(studentId);
        assessment.setAssessedAt(LocalDateTime.now());

        abilityAssessmentMapper.insertAssessment(assessment);
        updateStudentAbilityFromAssessment(studentId, dimensionId, normalizedScore);
        checkAndUpdateGoalProgress(studentId, dimensionId, normalizedScore);

        logger.info("自我评估提交成功，学生ID: {}", studentId);
        return assessment;
    }

    @Override
    public List<AbilityDimension> getAllAbilityDimensions() {
        logger.info("获取所有能力维度");
        return abilityDimensionMapper.selectActiveDimensions();
    }

    @Override
    public PageResult<AbilityAssessment> getStudentAssessments(Long studentId, Integer page, Integer size) {
        logger.info("分页获取学生评估记录，学生ID: {}", studentId);

        int offset = (page - 1) * size;
        List<AbilityAssessment> assessments = abilityAssessmentMapper.selectByStudentIdWithPagination(studentId, offset, size);
        Integer total = abilityAssessmentMapper.countByStudentId(studentId);

        return PageResult.of(assessments, page, size, total.longValue(), (total + size - 1) / size);
    }

    @Override
    public void initializeStudentAbilities(Long studentId) {
        logger.info("初始化学生能力记录，学生ID: {}", studentId);

        // 获取所有激活的能力维度
        List<AbilityDimension> dimensions = abilityDimensionMapper.selectActiveDimensions();

        // 为每个维度创建初始记录
        for (AbilityDimension dimension : dimensions) {
            if (!studentAbilityMapper.existsByStudentAndDimension(studentId, dimension.getId())) {
                StudentAbility studentAbility = new StudentAbility(studentId, dimension.getId());
                studentAbilityMapper.insertStudentAbility(studentAbility);
            }
        }

        logger.info("成功初始化学生能力记录");
    }

    @Override
    public List<Map<String, Object>> getStudentAbilityRanking(Long dimensionId, Integer limit) {
        logger.info("获取学生能力排名，维度ID: {}, 限制数量: {}", dimensionId, limit);

        AbilityDimension dimension = requireActiveDimension(dimensionId);
        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);

        List<Map<String, Object>> ranking = studentAbilityMapper.getStudentAbilityRanking(dimensionId, safeLimit);
        if (ranking == null) {
            return new ArrayList<>();
        }

        for (Map<String, Object> item : ranking) {
            if (item == null) {
                continue;
            }
            item.putIfAbsent("dimensionId", dimensionId);
            item.putIfAbsent("dimensionName", dimension.getName());
            item.putIfAbsent("dimensionCode", dimension.getCode());
        }
        return ranking;
    }

    @Override
    public List<Map<String, Object>> getClassAbilityStats(Long courseId, Long dimensionId) {
        logger.info("获取班级能力统计，课程ID: {}, 维度ID: {}", courseId, dimensionId);

        if (courseId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "课程ID不能为空");
        }

        List<Long> enrolledStudentIds = enrollmentMapper.findActiveStudentIdsByCourse(courseId);
        if (enrolledStudentIds == null || enrolledStudentIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> studentIdSet = new LinkedHashSet<>(enrolledStudentIds);
        List<AbilityDimension> targetDimensions = dimensionId == null
                ? Optional.ofNullable(abilityDimensionMapper.selectActiveDimensions()).orElse(List.of())
                : List.of(requireActiveDimension(dimensionId));

        List<Map<String, Object>> statsList = new ArrayList<>();
        for (AbilityDimension dimension : targetDimensions) {
            if (dimension == null || dimension.getId() == null) {
                continue;
            }

            List<StudentAbility> dimensionAbilities = Optional.ofNullable(studentAbilityMapper.selectByDimensionId(dimension.getId()))
                    .orElse(List.of());
            List<StudentAbility> enrolledAbilities = new ArrayList<>();
            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal maxScore = null;
            BigDecimal minScore = null;

            for (StudentAbility ability : dimensionAbilities) {
                if (ability == null || ability.getStudentId() == null || ability.getCurrentScore() == null) {
                    continue;
                }
                if (!studentIdSet.contains(ability.getStudentId())) {
                    continue;
                }
                enrolledAbilities.add(ability);
                totalScore = totalScore.add(ability.getCurrentScore());
                if (maxScore == null || ability.getCurrentScore().compareTo(maxScore) > 0) {
                    maxScore = ability.getCurrentScore();
                }
                if (minScore == null || ability.getCurrentScore().compareTo(minScore) < 0) {
                    minScore = ability.getCurrentScore();
                }
            }

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("courseId", courseId);
            stats.put("dimensionId", dimension.getId());
            stats.put("dimensionName", dimension.getName());
            stats.put("dimensionCode", dimension.getCode());
            stats.put("studentCount", studentIdSet.size());
            stats.put("assessedCount", enrolledAbilities.size());
            stats.put("averageScore", enrolledAbilities.isEmpty()
                    ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)
                    : totalScore.divide(BigDecimal.valueOf(enrolledAbilities.size()), 2, java.math.RoundingMode.HALF_UP));
            stats.put("maxScore", maxScore == null ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP) : maxScore);
            stats.put("minScore", minScore == null ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP) : minScore);
            stats.put("distributions", buildLevelDistribution(enrolledAbilities));
            statsList.add(stats);
        }

        return statsList;
    }

    @Override
    public void generateLearningRecommendations(Long studentId, Long dimensionId) {
        logger.info("生成学习建议，学生ID: {}, 维度ID: {}", studentId, dimensionId);

        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }

        learningRecommendationMapper.deleteExpiredRecommendations();

        List<AbilityDimension> targetDimensions = resolveRecommendationDimensions(studentId, dimensionId);
        if (targetDimensions.isEmpty()) {
            logger.info("学习建议生成跳过，无可用维度，学生ID: {}", studentId);
            return;
        }

        Map<Long, StudentAbility> abilityByDimension = new HashMap<>();
        List<StudentAbility> abilities = studentAbilityMapper.selectByStudentId(studentId);
        if (abilities != null) {
            for (StudentAbility ability : abilities) {
                if (ability != null && ability.getDimensionId() != null) {
                    abilityByDimension.put(ability.getDimensionId(), ability);
                }
            }
        }

        int created = 0;
        for (AbilityDimension dimension : targetDimensions) {
            if (dimension == null || dimension.getId() == null) {
                continue;
            }
            List<LearningRecommendation> existing = learningRecommendationMapper.selectByStudentAndDimension(studentId, dimension.getId(), null);
            boolean hasPending = existing != null && existing.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(item -> !Boolean.TRUE.equals(item.getIsAccepted()) && !item.isExpired());
            if (hasPending) {
                continue;
            }

            LearningRecommendation recommendation = buildRecommendation(studentId, dimension, abilityByDimension.get(dimension.getId()));
            learningRecommendationMapper.insertRecommendation(recommendation);
            created++;
        }

        logger.info("学习建议生成完成，学生ID: {}, 新增数量: {}", studentId, created);
    }

    @Override
    public List<AbilityGoal> getStudentGoals(Long studentId) {
        logger.info("获取学生目标，学生ID: {}", studentId);

        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }

        List<AbilityGoal> goals = abilityGoalMapper.selectGoalsByStudentId(studentId);
        if (goals == null) {
            goals = new ArrayList<>();
        }

        logger.info("学生目标获取成功，学生ID: {}, 目标数量: {}", studentId, goals == null ? 0 : goals.size());
        return goals;
    }

    @Override
    public void syncGoalReminderNotifications(Long studentId) {
        syncGoalReminderNotifications(studentId, false);
    }

    private void syncGoalReminderNotifications(Long studentId, boolean force) {
        if (studentId == null) {
            return;
        }
        if (!force && shouldSkipOnDemandGoalReminderSync(studentId)) {
            return;
        }

        List<AbilityGoal> goals = abilityGoalMapper.selectGoalsByStudentId(studentId);
        if (goals == null || goals.isEmpty()) {
            if (!force) {
                markOnDemandGoalReminderSync(studentId);
            }
            return;
        }

        List<com.noncore.assessment.entity.Notification> existingNotifications = notificationService.getAllUserNotifications(studentId, "system");
        LocalDate today = LocalDate.now();

        for (AbilityGoal goal : goals) {
            if (goal == null || goal.getId() == null || goal.getTargetDate() == null) {
                continue;
            }
            if (!"active".equals(goal.getStatus())) {
                continue;
            }

            long daysUntilDue = ChronoUnit.DAYS.between(today, goal.getTargetDate());
            if (daysUntilDue < 0) {
                if (!hasRecentGoalReminder(existingNotifications, goal.getId(), "goal_overdue", 24)) {
                    notificationService.sendNotification(
                            studentId,
                            null,
                            "能力目标已逾期",
                            buildGoalOverdueMessage(goal),
                            "system",
                            "goal_overdue",
                            "high",
                            "goal_overdue",
                            goal.getId()
                    );
                }
                continue;
            }

            if (daysUntilDue <= GOAL_REMINDER_LOOKAHEAD_DAYS
                    && !hasRecentGoalReminder(existingNotifications, goal.getId(), "goal_deadline", 24)) {
                notificationService.sendNotification(
                        studentId,
                        null,
                        "能力目标即将到期",
                        buildGoalDeadlineMessage(goal, daysUntilDue),
                        "system",
                        "goal_deadline",
                        daysUntilDue == 0 ? "high" : "normal",
                        "goal_deadline",
                        goal.getId()
                );
            }
        }

        if (!force) {
            markOnDemandGoalReminderSync(studentId);
        }
    }

    /**
     * 每天早上预先同步一次所有学生的目标提醒。
     *
     * <p>保留按需同步兜底；该任务主要用于让用户未主动打开通知中心时也能收到提醒。</p>
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void syncScheduledGoalReminderNotifications() {
        List<Long> studentIds = abilityGoalMapper.selectStudentIdsWithActiveGoals();
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        for (Long studentId : studentIds) {
            try {
                syncGoalReminderNotifications(studentId, true);
            } catch (Exception ex) {
                logger.warn("同步学生能力目标提醒失败，studentId={}", studentId, ex);
            }
        }
    }

    private boolean shouldSkipOnDemandGoalReminderSync(Long studentId) {
        LocalDateTime last = goalReminderOnDemandSyncedAt.get(studentId);
        if (last == null) {
            return false;
        }
        return last.plusSeconds(GOAL_REMINDER_ON_DEMAND_THROTTLE_SECONDS).isAfter(LocalDateTime.now());
    }

    private void markOnDemandGoalReminderSync(Long studentId) {
        if (studentId == null) {
            return;
        }
        goalReminderOnDemandSyncedAt.put(studentId, LocalDateTime.now());
    }

    @Override
    public void deleteAbilityGoal(Long goalId, Long studentId) {
        logger.info("删除能力目标，目标ID: {}", goalId);

        AbilityGoal existing = requireOwnedGoal(goalId, studentId);
        abilityGoalMapper.deleteGoal(existing.getId());

        logger.info("能力目标删除成功，目标ID: {}", goalId);
    }

    @Override
    public List<Map<String, Object>> getStudentAbilityTrends(Long studentId, Long dimensionId, Integer timeRange) {
        logger.info("获取学生能力发展趋势，学生ID: {}, 维度ID: {}, 时间范围: {}月", studentId, dimensionId, timeRange);

        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }
        if (dimensionId != null) {
            requireActiveDimension(dimensionId);
        }

        int safeMonths = timeRange == null || timeRange < 1 ? 6 : Math.min(timeRange, 24);
        List<Map<String, Object>> trends = Optional.ofNullable(
                abilityAssessmentMapper.getAssessmentTrends(studentId, dimensionId, safeMonths)
        ).orElse(new ArrayList<>());

        for (Map<String, Object> item : trends) {
            if (item == null) {
                continue;
            }
            Object month = item.get("month");
            Object averageScore = item.get("averageScore");
            item.putIfAbsent("x", month == null ? "" : String.valueOf(month));
            item.putIfAbsent("y", averageScore == null ? 0 : averageScore);
        }

        logger.info("学生能力发展趋势获取成功，学生ID: {}, 数据点数量: {}", studentId, trends.size());
        return trends;
    }

    // ==================== 私有辅助方法 ====================

    private AbilityDimension requireActiveDimension(Long dimensionId) {
        if (dimensionId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "能力维度ID不能为空");
        }

        AbilityDimension dimension = abilityDimensionMapper.selectDimensionById(dimensionId);
        if (dimension == null || !Boolean.TRUE.equals(dimension.getIsActive())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "能力维度不存在或未启用");
        }
        return dimension;
    }

    private List<Map<String, Object>> buildLevelDistribution(List<StudentAbility> abilities) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("expert", 0);
        counts.put("advanced", 0);
        counts.put("intermediate", 0);
        counts.put("beginner", 0);

        if (abilities != null) {
            for (StudentAbility ability : abilities) {
                if (ability == null) {
                    continue;
                }
                String level = ability.getLevel();
                if (level == null || !counts.containsKey(level)) {
                    level = "beginner";
                }
                counts.put(level, counts.get(level) + 1);
            }
        }

        List<Map<String, Object>> distributions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", entry.getKey());
            item.put("count", entry.getValue());
            distributions.add(item);
        }
        return distributions;
    }

    /**
     * 计算综合能力分数
     */
    private BigDecimal calculateOverallScore(List<StudentAbility> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (StudentAbility ability : abilities) {
            if (ability.getCurrentScore() != null) {
                // 这里需要从维度信息获取权重，暂时使用1.0
                BigDecimal weight = BigDecimal.ONE;
                totalWeightedScore = totalWeightedScore.add(ability.getCurrentScore().multiply(weight));
                totalWeight = totalWeight.add(weight);
            }
        }

        return totalWeight.compareTo(BigDecimal.ZERO) > 0 ?
            totalWeightedScore.divide(totalWeight, 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 更新学生能力记录
     */
    private void updateStudentAbilityFromAssessment(Long studentId, Long dimensionId, BigDecimal score) {
        StudentAbility ability = studentAbilityMapper.selectByStudentAndDimension(studentId, dimensionId);
        
        if (ability == null) {
            // 创建新记录
            ability = new StudentAbility(studentId, dimensionId);
            ability.updateAssessmentStats(score);
            studentAbilityMapper.insertStudentAbility(ability);
        } else {
            // 更新现有记录
            ability.updateAssessmentStats(score);
            studentAbilityMapper.updateStudentAbility(ability);
        }
    }

    /**
     * 检查并更新目标进度
     */
    private void checkAndUpdateGoalProgress(Long studentId, Long dimensionId, BigDecimal score) {
        List<AbilityGoal> goals = abilityGoalMapper.selectActiveGoalsByStudentAndDimension(studentId, dimensionId);
        
        for (AbilityGoal goal : goals) {
            boolean wasAchieved = "achieved".equals(goal.getStatus());
            goal.updateCurrentScore(score);
            abilityGoalMapper.updateGoal(goal);
            if (!wasAchieved && "achieved".equals(goal.getStatus())) {
                recordGoalBehaviorEvent(goal, BehaviorEventType.GOAL_ACHIEVE, Map.of(
                        "source", "assessment_sync",
                        "triggerDimensionId", dimensionId
                ));
            }
        }
    }

    /**
     * 计算各维度得分
     */
    private Map<String, BigDecimal> calculateDimensionScores(List<AbilityAssessment> assessments) {
        Map<String, List<BigDecimal>> dimensionScoreMap = new HashMap<>();
        
        for (AbilityAssessment assessment : assessments) {
            String dimensionName = assessment.getDimensionName(); // 需要在Mapper中关联查询
            if (dimensionName != null) {
                dimensionScoreMap.computeIfAbsent(dimensionName, k -> new ArrayList<>())
                    .add(assessment.getPercentage());
            }
        }
        
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<String, List<BigDecimal>> entry : dimensionScoreMap.entrySet()) {
            BigDecimal average = entry.getValue().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(entry.getValue().size()), 2, java.math.RoundingMode.HALF_UP);
            result.put(entry.getKey(), average);
        }
        
        return result;
    }

    /**
     * 计算加权综合得分
     */
    private BigDecimal calculateWeightedOverallScore(Map<String, BigDecimal> dimensionScores) {
        if (dimensionScores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = dimensionScores.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(BigDecimal.valueOf(dimensionScores.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 生成趋势分析
     */
    private String generateTrendsAnalysis(Long studentId, List<AbilityAssessment> assessments) {
        // 简化实现，实际应该分析时间序列数据
        int totalAssessments = assessments.size();
        if (totalAssessments == 0) {
            return "暂无足够数据进行趋势分析";
        }
        
        return String.format("在报告周期内共完成 %d 次能力评估，整体呈现稳步提升的趋势", totalAssessments);
    }

    /**
     * 生成成就列表
     */
    private List<Map<String, String>> generateAchievements(List<AbilityAssessment> assessments) {
        List<Map<String, String>> achievements = new ArrayList<>();
        
        // 统计高分评估
        long excellentCount = assessments.stream()
            .mapToLong(a -> a.getPercentage().compareTo(BigDecimal.valueOf(90)) >= 0 ? 1 : 0)
            .sum();
            
        if (excellentCount > 0) {
            Map<String, String> achievement = new HashMap<>();
            achievement.put("title", "优秀表现");
            achievement.put("description", String.format("获得 %d 次优秀评估（90分以上）", excellentCount));
            achievements.add(achievement);
        }
        
        return achievements;
    }

    /**
     * 生成改进建议
     */
    private List<Map<String, String>> generateImprovementAreas(Long studentId, Map<String, BigDecimal> dimensionScores) {
        List<Map<String, String>> improvements = new ArrayList<>();
        
        // 找出得分最低的维度
        String lowestDimension = null;
        BigDecimal lowestScore = BigDecimal.valueOf(100);
        
        for (Map.Entry<String, BigDecimal> entry : dimensionScores.entrySet()) {
            if (entry.getValue().compareTo(lowestScore) < 0) {
                lowestScore = entry.getValue();
                lowestDimension = entry.getKey();
            }
        }
        
        if (lowestDimension != null && lowestScore.compareTo(BigDecimal.valueOf(80)) < 0) {
            Map<String, String> improvement = new HashMap<>();
            improvement.put("dimension", lowestDimension);
            improvement.put("suggestion", String.format("当前 %s 能力得分为 %.1f 分，建议加强相关练习和学习", 
                lowestDimension, lowestScore.doubleValue()));
            improvements.add(improvement);
        }
        
        return improvements;
    }

    /**
     * 生成综合建议
     */
    private String generateRecommendations(Map<String, BigDecimal> dimensionScores, int achievementCount) {
        StringBuilder recommendations = new StringBuilder();
        
        BigDecimal overallScore = calculateWeightedOverallScore(dimensionScores);
        
        if (overallScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            recommendations.append("您的综合能力表现优秀，请继续保持！");
        } else if (overallScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            recommendations.append("您的能力发展良好，建议在薄弱环节加强练习。");
        } else {
            recommendations.append("建议制定针对性的学习计划，重点提升核心能力。");
        }
        
        if (achievementCount > 0) {
            recommendations.append("您已经取得了不错的成就，继续努力！");
        }
        
        return recommendations.toString();
    }

    /**
     * 将Map转换为JSON字符串（简化实现）
     */
    private String convertMapToJson(Map<String, BigDecimal> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize map to JSON", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "无法序列化数据");
        }
    }

    /**
     * 将List转换为JSON字符串（简化实现）
     */
    private String convertListToJson(List<Map<String, String>> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize list to JSON", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "无法序列化数据");
        }
    }

    @Override
    public AbilityGoal createAbilityGoal(AbilityGoal abilityGoal) {
        if (abilityGoal == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标信息不能为空");
        }

        logger.info("创建能力目标，目标名称: {}", abilityGoal.getTitle());

        Long studentId = abilityGoal.getStudentId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }

        Long dimensionId = abilityGoal.getDimensionId();
        validateGoalDimension(dimensionId);

        AbilityGoal record = new AbilityGoal();
        record.setStudentId(studentId);
        record.setDimensionId(dimensionId);
        record.setTitle(requireGoalTitle(abilityGoal.getTitle()));
        record.setDescription(normalizeOptionalText(abilityGoal.getDescription()));
        record.setTargetDate(requireGoalTargetDate(abilityGoal.getTargetDate()));
        record.setPriority(normalizeGoalPriority(abilityGoal.getPriority()));
        record.setTargetScore(requireGoalTargetScore(abilityGoal.getTargetScore()));
        record.setCurrentScore(resolveGoalCurrentScore(studentId, dimensionId, abilityGoal.getCurrentScore()));
        record.setStatus(normalizeGoalStatus(abilityGoal.getStatus(), "active"));
        record.setAchievedAt(null);
        applyGoalLifecycle(record, abilityGoal.getStatus() != null && !abilityGoal.getStatus().isBlank());

        abilityGoalMapper.insertGoal(record);
        AbilityGoal createdGoal = abilityGoalMapper.selectGoalById(record.getId());
        recordGoalBehaviorEvent(createdGoal, BehaviorEventType.GOAL_SET, null);
        if (createdGoal != null && "achieved".equals(createdGoal.getStatus())) {
            recordGoalBehaviorEvent(createdGoal, BehaviorEventType.GOAL_ACHIEVE, Map.of("source", "goal_create"));
        }

        logger.info("能力目标创建成功，目标ID: {}", record.getId());
        return createdGoal != null ? createdGoal : record;
    }

    @Override
    public AbilityGoal updateAbilityGoal(Long goalId, Long studentId, AbilityGoal abilityGoal) {
        logger.info("更新能力目标，目标ID: {}", goalId);

        if (abilityGoal == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标信息不能为空");
        }

        AbilityGoal existing = requireOwnedGoal(goalId, studentId);
        boolean wasAchieved = "achieved".equals(existing.getStatus());
        String previousStatus = existing.getStatus();

        Long originalDimensionId = existing.getDimensionId();
        Long effectiveDimensionId = abilityGoal.getDimensionId() != null ? abilityGoal.getDimensionId() : existing.getDimensionId();
        validateGoalDimension(effectiveDimensionId);

        existing.setDimensionId(effectiveDimensionId);
        if (abilityGoal.getTitle() != null) {
            existing.setTitle(requireGoalTitle(abilityGoal.getTitle()));
        }
        if (abilityGoal.getDescription() != null) {
            existing.setDescription(normalizeOptionalText(abilityGoal.getDescription()));
        }
        if (abilityGoal.getTargetDate() != null) {
            existing.setTargetDate(requireGoalTargetDate(abilityGoal.getTargetDate()));
        }
        if (abilityGoal.getPriority() != null) {
            existing.setPriority(normalizeGoalPriority(abilityGoal.getPriority()));
        }
        if (abilityGoal.getTargetScore() != null) {
            existing.setTargetScore(requireGoalTargetScore(abilityGoal.getTargetScore()));
        }
        BigDecimal currentScoreFallback = abilityGoal.getCurrentScore() != null
                ? abilityGoal.getCurrentScore()
                : (Objects.equals(originalDimensionId, effectiveDimensionId) ? existing.getCurrentScore() : BigDecimal.ZERO);
        existing.setCurrentScore(resolveGoalCurrentScore(studentId, effectiveDimensionId, currentScoreFallback));
        if (abilityGoal.getStatus() != null) {
            existing.setStatus(normalizeGoalStatus(abilityGoal.getStatus(), existing.getStatus()));
        }

        applyGoalLifecycle(existing, abilityGoal.getStatus() != null && !abilityGoal.getStatus().isBlank());
        abilityGoalMapper.updateGoal(existing);
        AbilityGoal updatedGoal = abilityGoalMapper.selectGoalById(goalId);
        Map<String, Object> updateMeta = new LinkedHashMap<>();
        updateMeta.put("previousStatus", previousStatus);
        updateMeta.put("source", "goal_update");
        recordGoalBehaviorEvent(updatedGoal, BehaviorEventType.GOAL_UPDATE, updateMeta);
        if (!wasAchieved && updatedGoal != null && "achieved".equals(updatedGoal.getStatus())) {
            recordGoalBehaviorEvent(updatedGoal, BehaviorEventType.GOAL_ACHIEVE, Map.of("source", "goal_update"));
        }

        logger.info("能力目标更新成功，目标ID: {}", goalId);
        return updatedGoal != null ? updatedGoal : existing;
    }

    private AbilityGoal requireOwnedGoal(Long goalId, Long studentId) {
        if (goalId == null || studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标ID和学生ID不能为空");
        }

        AbilityGoal existing = abilityGoalMapper.selectGoalById(goalId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "能力目标不存在");
        }
        if (existing.getStudentId() == null || !existing.getStudentId().equals(studentId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_OPERATION, "无权操作该能力目标");
        }
        return existing;
    }

    private void validateGoalDimension(Long dimensionId) {
        if (dimensionId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "能力维度不能为空");
        }
        AbilityDimension dimension = abilityDimensionMapper.selectDimensionById(dimensionId);
        if (dimension == null || Boolean.FALSE.equals(dimension.getIsActive())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "能力维度不存在或未启用");
        }
    }

    private String requireGoalTitle(String title) {
        String normalized = normalizeOptionalText(title);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标标题不能为空");
        }
        return normalized;
    }

    private LocalDate requireGoalTargetDate(LocalDate targetDate) {
        if (targetDate == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标日期不能为空");
        }
        return targetDate;
    }

    private BigDecimal requireGoalTargetScore(BigDecimal targetScore) {
        BigDecimal normalized = normalizeGoalScore(targetScore);
        if (normalized == null || normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标分数必须大于0");
        }
        return normalized;
    }

    private BigDecimal resolveGoalCurrentScore(Long studentId, Long dimensionId, BigDecimal fallbackScore) {
        StudentAbility snapshot = studentAbilityMapper.selectByStudentAndDimension(studentId, dimensionId);
        if (snapshot != null && snapshot.getCurrentScore() != null) {
            return normalizeGoalScore(snapshot.getCurrentScore());
        }
        BigDecimal normalizedFallback = normalizeGoalScore(fallbackScore);
        return normalizedFallback == null ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP) : normalizedFallback;
    }

    private BigDecimal normalizeGoalScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "分数不能为负数");
        }

        BigDecimal normalized = score;
        if (normalized.compareTo(BigDecimal.valueOf(5)) > 0) {
            normalized = normalized.divide(BigDecimal.valueOf(20), 2, java.math.RoundingMode.HALF_UP);
        } else {
            normalized = normalized.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        if (normalized.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "分数超出允许范围");
        }
        return normalized;
    }

    private String normalizeGoalPriority(String priority) {
        String normalized = normalizeOptionalText(priority);
        if (normalized == null) {
            return "medium";
        }
        if (!Set.of("low", "medium", "high").contains(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标优先级不合法");
        }
        return normalized;
    }

    private String normalizeGoalStatus(String status, String defaultValue) {
        String normalized = normalizeOptionalText(status);
        if (normalized == null) {
            return defaultValue;
        }
        if (!Set.of("active", "achieved", "paused", "cancelled").contains(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标状态不合法");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void applyGoalLifecycle(AbilityGoal goal, boolean explicitStatus) {
        if (goal.getCurrentScore() == null) {
            goal.setCurrentScore(BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
        }

        if ("cancelled".equals(goal.getStatus())) {
            goal.setAchievedAt(null);
            goal.updateTimestamp();
            return;
        }

        if (goal.getTargetScore() != null && goal.getCurrentScore().compareTo(goal.getTargetScore()) >= 0) {
            if (!"achieved".equals(goal.getStatus())) {
                goal.markAsAchieved();
            } else if (goal.getAchievedAt() == null) {
                goal.setAchievedAt(java.time.LocalDateTime.now());
                goal.updateTimestamp();
            } else {
                goal.updateTimestamp();
            }
            return;
        }

        if ("achieved".equals(goal.getStatus()) && !explicitStatus) {
            goal.setStatus("active");
        }
        if (goal.getStatus() == null || goal.getStatus().isBlank()) {
            goal.setStatus("active");
        }
        if (!"achieved".equals(goal.getStatus())) {
            goal.setAchievedAt(null);
        } else if (goal.getAchievedAt() == null) {
            goal.setAchievedAt(java.time.LocalDateTime.now());
        }
        goal.updateTimestamp();
    }

    private void recordGoalBehaviorEvent(AbilityGoal goal, BehaviorEventType eventType, Map<String, Object> extraMetadata) {
        if (goal == null || goal.getStudentId() == null || goal.getId() == null || eventType == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("goalId", goal.getId());
        metadata.put("dimensionId", goal.getDimensionId());
        if (goal.getDimensionName() != null && !goal.getDimensionName().isBlank()) {
            metadata.put("dimensionName", goal.getDimensionName());
        }
        metadata.put("title", goal.getTitle());
        metadata.put("targetScore", goal.getTargetScore());
        metadata.put("currentScore", goal.getCurrentScore());
        metadata.put("targetDate", goal.getTargetDate() == null ? null : goal.getTargetDate().toString());
        metadata.put("priority", goal.getPriority());
        metadata.put("status", goal.getStatus());
        metadata.put("overdue", goal.isOverdue());

        if (extraMetadata != null && !extraMetadata.isEmpty()) {
            metadata.putAll(extraMetadata);
        }

        behaviorEventRecorder.record(
                goal.getStudentId(),
                null,
                eventType,
                "ability_goal",
                goal.getId(),
                metadata
        );
    }

    private boolean hasRecentGoalReminder(List<com.noncore.assessment.entity.Notification> notifications,
                                          Long goalId,
                                          String relatedType,
                                          long hoursWindow) {
        if (notifications == null || notifications.isEmpty() || goalId == null || relatedType == null) {
            return false;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusHours(Math.max(1, hoursWindow));
        for (com.noncore.assessment.entity.Notification notification : notifications) {
            if (notification == null) {
                continue;
            }
            if (!goalId.equals(notification.getRelatedId())) {
                continue;
            }
            if (!relatedType.equalsIgnoreCase(String.valueOf(notification.getRelatedType()))) {
                continue;
            }
            if (notification.getCreatedAt() != null && notification.getCreatedAt().isAfter(cutoff)) {
                return true;
            }
        }
        return false;
    }

    private String buildGoalDeadlineMessage(AbilityGoal goal, long daysUntilDue) {
        String dimension = goal.getDimensionName() == null || goal.getDimensionName().isBlank()
                ? ("维度#" + goal.getDimensionId())
                : goal.getDimensionName();
        String dueText;
        if (daysUntilDue <= 0) {
            dueText = "今天";
        } else if (daysUntilDue == 1) {
            dueText = "明天";
        } else {
            dueText = daysUntilDue + " 天后";
        }
        return String.format("你设定的目标“%s”（%s）将在%s到期，当前 %.2f / %.2f，建议尽快推进或调整计划。",
                goal.getTitle(),
                dimension,
                dueText,
                goal.getCurrentScore() == null ? 0.0 : goal.getCurrentScore().doubleValue(),
                goal.getTargetScore() == null ? 0.0 : goal.getTargetScore().doubleValue());
    }

    private String buildGoalOverdueMessage(AbilityGoal goal) {
        String dimension = goal.getDimensionName() == null || goal.getDimensionName().isBlank()
                ? ("维度#" + goal.getDimensionId())
                : goal.getDimensionName();
        long overdueDays = Math.max(1, ChronoUnit.DAYS.between(goal.getTargetDate(), LocalDate.now()));
        return String.format("目标“%s”（%s）已逾期 %d 天，当前 %.2f / %.2f。你仍然可以继续完成，或调整目标分数与截止日期。",
                goal.getTitle(),
                dimension,
                overdueDays,
                goal.getCurrentScore() == null ? 0.0 : goal.getCurrentScore().doubleValue(),
                goal.getTargetScore() == null ? 0.0 : goal.getTargetScore().doubleValue());
    }

    @Override
    public List<LearningRecommendation> getLearningRecommendations(Long studentId, Long dimensionId, Integer limit) {
        logger.info("获取学习建议，学生ID: {}, 维度ID: {}, 限制数量: {}", studentId, dimensionId, limit);

        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生ID不能为空");
        }

        learningRecommendationMapper.deleteExpiredRecommendations();
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 20);

        List<LearningRecommendation> recommendations = loadRecommendations(studentId, dimensionId, safeLimit);
        if (recommendations.isEmpty()) {
            generateLearningRecommendations(studentId, dimensionId);
            recommendations = loadRecommendations(studentId, dimensionId, safeLimit);
        }

        logger.info("学习建议获取成功，学生ID: {}, 建议数量: {}", studentId, recommendations.size());
        return recommendations;
    }

    private LearningRecommendation requireOwnedRecommendation(Long recommendationId, Long studentId) {
        if (recommendationId == null || studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "建议ID和学生ID不能为空");
        }

        LearningRecommendation recommendation = learningRecommendationMapper.selectRecommendationById(recommendationId);
        if (recommendation == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "学习建议不存在");
        }
        if (!Objects.equals(recommendation.getStudentId(), studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "无权操作该学习建议");
        }
        return recommendation;
    }

    private List<LearningRecommendation> loadRecommendations(Long studentId, Long dimensionId, int limit) {
        List<LearningRecommendation> source = dimensionId == null
                ? learningRecommendationMapper.selectByStudentId(studentId, null)
                : learningRecommendationMapper.selectByStudentAndDimension(studentId, dimensionId, null);
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }

        List<LearningRecommendation> filtered = new ArrayList<>();
        for (LearningRecommendation item : source) {
            if (item == null || item.isExpired()) {
                continue;
            }
            filtered.add(item);
            if (filtered.size() >= limit) {
                break;
            }
        }
        return filtered;
    }

    private List<AbilityDimension> resolveRecommendationDimensions(Long studentId, Long dimensionId) {
        List<AbilityDimension> activeDimensions = Optional.ofNullable(abilityDimensionMapper.selectActiveDimensions())
                .orElse(List.of());
        if (activeDimensions.isEmpty()) {
            return List.of();
        }

        Map<Long, AbilityDimension> dimensionMap = new LinkedHashMap<>();
        for (AbilityDimension dimension : activeDimensions) {
            if (dimension != null && dimension.getId() != null) {
                dimensionMap.put(dimension.getId(), dimension);
            }
        }

        if (dimensionId != null) {
            AbilityDimension dimension = dimensionMap.get(dimensionId);
            return dimension == null ? List.of() : List.of(dimension);
        }

        List<StudentAbility> abilities = Optional.ofNullable(studentAbilityMapper.selectByStudentId(studentId))
                .orElse(List.of());
        List<StudentAbility> sortedAbilities = abilities.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getDimensionId() != null && dimensionMap.containsKey(item.getDimensionId()))
                .sorted(Comparator
                        .comparing((StudentAbility item) -> item.getCurrentScore() == null ? BigDecimal.ZERO : item.getCurrentScore())
                        .thenComparing(item -> item.getLastAssessmentAt(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        LinkedHashSet<Long> picked = new LinkedHashSet<>();
        for (StudentAbility item : sortedAbilities) {
            BigDecimal score = item.getCurrentScore();
            if (score == null || score.compareTo(new BigDecimal("4.60")) < 0) {
                picked.add(item.getDimensionId());
            }
            if (picked.size() >= 2) {
                break;
            }
        }

        if (picked.isEmpty() && !sortedAbilities.isEmpty()) {
            picked.add(sortedAbilities.get(0).getDimensionId());
        }
        if (picked.isEmpty()) {
            for (AbilityDimension dimension : activeDimensions) {
                if (dimension == null || dimension.getId() == null) {
                    continue;
                }
                picked.add(dimension.getId());
                if (picked.size() >= 2) {
                    break;
                }
            }
        }

        List<AbilityDimension> dimensions = new ArrayList<>();
        for (Long id : picked) {
            AbilityDimension dimension = dimensionMap.get(id);
            if (dimension != null) {
                dimensions.add(dimension);
            }
        }
        return dimensions;
    }

    private LearningRecommendation buildRecommendation(Long studentId, AbilityDimension dimension, StudentAbility ability) {
        String dimensionName = dimension.getName() == null || dimension.getName().isBlank()
                ? ("维度#" + dimension.getId())
                : dimension.getName();
        BigDecimal currentScore = ability == null || ability.getCurrentScore() == null
                ? BigDecimal.ZERO
                : ability.getCurrentScore().setScale(2, java.math.RoundingMode.HALF_UP);
        double currentValue = currentScore.doubleValue();
        BigDecimal gap = BigDecimal.valueOf(Math.max(0.0, 5.0 - currentValue)).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal priority = BigDecimal.valueOf(Math.min(0.99, 0.55 + (gap.doubleValue() / 5.0) * 0.4))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        String recommendationType = switch (String.valueOf(dimension.getCode())) {
            case "LEARNING_ATTITUDE" -> "practice";
            case "LEARNING_ABILITY" -> "practice";
            case "LEARNING_METHOD" -> "project";
            case "MORAL_COGNITION" -> "resource";
            default -> "course";
        };

        String difficulty = currentValue >= 4.2
                ? "advanced"
                : (currentValue >= 3.2 ? "intermediate" : "beginner");

        String estimatedTime = switch (difficulty) {
            case "advanced" -> "每周30-45分钟";
            case "intermediate" -> "每周20-30分钟";
            default -> "每天15-20分钟";
        };

        String title = "优先提升" + dimensionName;
        String description = switch (String.valueOf(dimension.getCode())) {
            case "LEARNING_ATTITUDE" -> String.format(
                    "当前%s为 %.2f/5。建议把本周学习任务拆成更小的每日目标，并固定一个复盘时间，优先保持连续投入而不是一次性突击。",
                    dimensionName, currentValue);
            case "LEARNING_ABILITY" -> String.format(
                    "当前%s为 %.2f/5。建议针对最近一次作业中失分最多的知识点做 1 次专项练习，并在练习后总结一条可复用的解题步骤。",
                    dimensionName, currentValue);
            case "LEARNING_METHOD" -> String.format(
                    "当前%s为 %.2f/5。建议为下一次学习任务提前列出“计划-执行-复盘”三步清单，并在完成后记录最有效的一条方法。",
                    dimensionName, currentValue);
            case "MORAL_COGNITION" -> String.format(
                    "当前%s为 %.2f/5。建议结合近期课堂/作业案例，补充记录 1-2 条判断依据与责任边界，提升表达的完整性与自我校准能力。",
                    dimensionName, currentValue);
            default -> String.format(
                    "当前%s为 %.2f/5。建议先从最近一次任务中选择一个最薄弱环节，制定一个可在一周内完成的改进动作并跟踪执行。",
                    dimensionName, currentValue);
        };

        return LearningRecommendation.builder()
                .studentId(studentId)
                .dimensionId(dimension.getId())
                .title(title)
                .description(description)
                .recommendationType(recommendationType)
                .difficultyLevel(difficulty)
                .estimatedTime(estimatedTime)
                .priorityScore(priority)
                .isRead(false)
                .isAccepted(false)
                .expiresAt(LocalDateTime.now().plusDays(currentValue >= 4.2 ? 21 : 14))
                .build();
    }
}
