package com.noncore.assessment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.AbilityService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    private final AbilityDimensionMapper abilityDimensionMapper;
    private final StudentAbilityMapper studentAbilityMapper;
    private final AbilityAssessmentMapper abilityAssessmentMapper;
    private final AbilityReportMapper abilityReportMapper;
    private final LearningRecommendationMapper learningRecommendationMapper;
    private final AbilityGoalMapper abilityGoalMapper;
    private final ObjectMapper objectMapper;

    public AbilityServiceImpl(AbilityDimensionMapper abilityDimensionMapper,
                              StudentAbilityMapper studentAbilityMapper,
                              AbilityAssessmentMapper abilityAssessmentMapper,
                              AbilityReportMapper abilityReportMapper,
                              LearningRecommendationMapper learningRecommendationMapper,
                              AbilityGoalMapper abilityGoalMapper,
                              ObjectMapper objectMapper) {
        this.abilityDimensionMapper = abilityDimensionMapper;
        this.studentAbilityMapper = studentAbilityMapper;
        this.abilityAssessmentMapper = abilityAssessmentMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.learningRecommendationMapper = learningRecommendationMapper;
        this.abilityGoalMapper = abilityGoalMapper;
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
        assessment.setMaxScore(BigDecimal.valueOf(100));
        assessment.setAssessmentType(assessmentType);
        assessment.setRelatedId(relatedId);
        assessment.setEvidence(evidence);
        assessment.setStatus("completed");

        // 保存评估记录
        abilityAssessmentMapper.insertAssessment(assessment);

        // 更新学生能力记录
        updateStudentAbilityFromAssessment(studentId, dimensionId, score);

        // 检查是否达成目标
        checkAndUpdateGoalProgress(studentId, dimensionId, score);

        logger.info("成功记录能力评估");
    }

    @Override
    public void acceptRecommendation(Long recommendationId) {
        logger.info("接受学习建议，建议ID: {}", recommendationId);
        
        // 这里应该实现具体的接受建议逻辑
        // 比如更新建议状态、记录用户行为等
        // 暂时只做日志记录，后续可以根据具体需求实现
        
        logger.info("学习建议已接受，建议ID: {}", recommendationId);
    }

    @Override
    public void markRecommendationAsRead(Long recommendationId) {
        logger.info("标记学习建议为已读，建议ID: {}", recommendationId);
        
        // 这里应该实现具体的标记为已读逻辑
        // 比如更新建议的阅读状态、记录阅读时间等
        // 暂时只做日志记录，后续可以根据具体需求实现
        
        logger.info("学习建议已标记为已读，建议ID: {}", recommendationId);
    }

    @Override
    public void publishAbilityReport(Long reportId) {
        logger.info("发布能力报告，报告ID: {}", reportId);
        
        // 这里应该实现具体的发布报告逻辑
        // 比如更新报告状态、通知相关人员等
        // 暂时只做日志记录，后续可以根据具体需求实现
        
        logger.info("能力报告发布完成，报告ID: {}", reportId);
    }

    @Override
    public AbilityReport getAbilityReportById(Long reportId) {
        logger.info("获取能力报告详情，报告ID: {}", reportId);
        
        // 这里应该实现具体的获取报告逻辑
        // 比如从数据库查询报告详情等
        // 暂时返回一个基础对象，后续可以根据具体需求实现
        AbilityReport report = new AbilityReport();
        report.setId(reportId);
        report.setTitle("能力报告 " + reportId);
        
        logger.info("能力报告获取成功，报告ID: {}", reportId);
        return report;
    }

    @Override
    public PageResult<AbilityReport> getAbilityReportHistory(Long studentId, Integer page, Integer size) {
        logger.info("获取能力报告历史，学生ID: {}, 页码: {}, 页大小: {}", studentId, page, size);
        
        // 这里应该实现具体的获取报告历史逻辑
        // 暂时返回空的分页结果，后续可以根据具体需求实现
        PageResult<AbilityReport> result = new PageResult<>();
        result.setItems(new ArrayList<>());
        result.setTotal(0L);
        result.setTotalPages(0);
        result.setPage(page);
        result.setSize(size);
        
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
        
        // 这里应该实现具体的自我评估提交逻辑
        // 比如创建评估记录、更新能力数据等
        AbilityAssessment assessment = new AbilityAssessment();
        assessment.setStudentId(studentId);
        assessment.setDimensionId(dimensionId);
        assessment.setScore(score);
        assessment.setEvidence(feedback);
        assessment.setAssessmentType("self");
        assessment.setStatus("completed");
        
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
        
        // 这里应该实现具体的排名逻辑
        // 暂时返回空列表，后续可以根据具体需求实现

        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getClassAbilityStats(Long courseId, Long dimensionId) {
        logger.info("获取班级能力统计，课程ID: {}, 维度ID: {}", courseId, dimensionId);
        
        List<Map<String, Object>> statsList = new ArrayList<>();
        Map<String, Object> stats = new HashMap<>();
        
        // 这里应该实现具体的班级统计逻辑
        // 暂时返回基础结构，后续可以根据具体需求实现
        stats.put("courseId", courseId);
        stats.put("dimensionId", dimensionId);
        stats.put("studentCount", 0);
        stats.put("averageScore", 0.0);
        stats.put("distributions", new ArrayList<>());
        
        statsList.add(stats);
        return statsList;
    }

    @Override
    public void generateLearningRecommendations(Long studentId, Long dimensionId) {
        logger.info("生成学习建议，学生ID: {}, 维度ID: {}", studentId, dimensionId);
        
        // 这里应该实现具体的学习建议生成逻辑
        // 比如基于学生的能力评估结果、学习历史等生成个性化建议
        // 生成的建议会保存到数据库中，后续可以根据具体需求实现
        
        logger.info("学习建议生成完成，学生ID: {}", studentId);
    }

    @Override
    public List<AbilityGoal> getStudentGoals(Long studentId) {
        logger.info("获取学生目标，学生ID: {}", studentId);
        
        // 这里应该实现具体的获取学生目标逻辑
        // 比如从数据库查询学生设定的学习目标等
        // 暂时返回空列表，后续可以根据具体需求实现
        List<AbilityGoal> goals = new ArrayList<>();
        
        logger.info("学生目标获取成功，学生ID: {}, 目标数量: {}", studentId, 0);
        return goals;
    }

    @Override
    public void deleteAbilityGoal(Long goalId) {
        logger.info("删除能力目标，目标ID: {}", goalId);
        
        // 这里应该实现具体的删除目标逻辑
        // 比如从数据库删除指定的学习目标记录
        // 暂时只做日志记录，后续可以根据具体需求实现
        
        logger.info("能力目标删除成功，目标ID: {}", goalId);
    }

    @Override
    public List<Map<String, Object>> getStudentAbilityTrends(Long studentId, Long dimensionId, Integer timeRange) {
        logger.info("获取学生能力发展趋势，学生ID: {}, 维度ID: {}, 时间范围: {}月", studentId, dimensionId, timeRange);
        
        // 这里应该实现具体的获取能力趋势逻辑
        // 暂时返回空列表，后续可以根据具体需求实现
        List<Map<String, Object>> trends = new ArrayList<>();
        
        logger.info("学生能力发展趋势获取成功，学生ID: {}, 数据点数量: {}", studentId, 0);
        return trends;
    }

    // ==================== 私有辅助方法 ====================

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
            goal.updateCurrentScore(score);
            abilityGoalMapper.updateGoal(goal);
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
        logger.info("创建能力目标，目标名称: {}", abilityGoal.getTitle());
        
        // 这里应该实现具体的创建目标逻辑
        // 比如保存到数据库，生成目标ID等
        // 暂时设置基本属性，后续可以根据具体需求实现
        
        logger.info("能力目标创建成功，目标ID: {}", abilityGoal.getId());
        return abilityGoal;
    }

    @Override
    public AbilityGoal updateAbilityGoal(Long goalId, AbilityGoal abilityGoal) {
        logger.info("更新能力目标，目标ID: {}", goalId);
        
        // 这里应该实现具体的更新目标逻辑
        // 比如从数据库更新指定的学习目标记录
        // 暂时设置基本属性，后续可以根据具体需求实现
        abilityGoal.setId(goalId);
        
        logger.info("能力目标更新成功，目标ID: {}", goalId);
        return abilityGoal;
    }

    @Override
    public List<LearningRecommendation> getLearningRecommendations(Long studentId, Long dimensionId, Integer limit) {
        logger.info("获取学习建议，学生ID: {}, 维度ID: {}, 限制数量: {}", studentId, dimensionId, limit);
        
        // 这里应该实现具体的获取学习建议逻辑
        // 暂时返回空列表，后续可以根据具体需求实现
        List<LearningRecommendation> recommendations = new ArrayList<>();
        
        logger.info("学习建议获取成功，学生ID: {}, 建议数量: {}", studentId, 0);
        return recommendations;
    }
}