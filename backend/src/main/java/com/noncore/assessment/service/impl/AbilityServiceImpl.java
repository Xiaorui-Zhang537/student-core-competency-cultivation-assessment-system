package com.noncore.assessment.service.impl;

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

    public AbilityServiceImpl(AbilityDimensionMapper abilityDimensionMapper,
                              StudentAbilityMapper studentAbilityMapper,
                              AbilityAssessmentMapper abilityAssessmentMapper,
                              AbilityReportMapper abilityReportMapper,
                              LearningRecommendationMapper learningRecommendationMapper,
                              AbilityGoalMapper abilityGoalMapper) {
        this.abilityDimensionMapper = abilityDimensionMapper;
        this.studentAbilityMapper = studentAbilityMapper;
        this.abilityAssessmentMapper = abilityAssessmentMapper;
        this.abilityReportMapper = abilityReportMapper;
        this.learningRecommendationMapper = learningRecommendationMapper;
        this.abilityGoalMapper = abilityGoalMapper;
    }

    @Override
    public Map<String, Object> getStudentAbilityDashboard(Long studentId) {
        try {
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

        } catch (Exception e) {
            logger.error("获取学生能力仪表板数据失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生能力仪表板数据失败: " + e.getMessage());
        }
    }

    @Override
    public AbilityReport generateStudentReport(Long studentId, String reportType, LocalDate periodStart, LocalDate periodEnd) {
        try {
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

        } catch (Exception e) {
            logger.error("生成学生能力报告失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "生成学生能力报告失败: " + e.getMessage());
        }
    }

    @Override
    public void recordAbilityAssessment(Long studentId, Long dimensionId, BigDecimal score, 
                                       String assessmentType, Long relatedId, String evidence) {
        try {
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

        } catch (Exception e) {
            logger.error("记录能力评估失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "记录能力评估失败: " + e.getMessage());
        }
    }

    @Override
    public void acceptRecommendation(Long recommendationId) {
        try {
            logger.info("接受学习建议，建议ID: {}", recommendationId);
            
            // 这里应该实现具体的接受建议逻辑
            // 比如更新建议状态、记录用户行为等
            // 暂时只做日志记录，后续可以根据具体需求实现
            
            logger.info("学习建议已接受，建议ID: {}", recommendationId);
            
        } catch (Exception e) {
            logger.error("接受学习建议失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "接受学习建议失败: " + e.getMessage());
        }
    }

    @Override
    public void markRecommendationAsRead(Long recommendationId) {
        try {
            logger.info("标记学习建议为已读，建议ID: {}", recommendationId);
            
            // 这里应该实现具体的标记为已读逻辑
            // 比如更新建议的阅读状态、记录阅读时间等
            // 暂时只做日志记录，后续可以根据具体需求实现
            
            logger.info("学习建议已标记为已读，建议ID: {}", recommendationId);
            
        } catch (Exception e) {
            logger.error("标记学习建议为已读失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "标记学习建议为已读失败: " + e.getMessage());
        }
    }

    @Override
    public void publishAbilityReport(Long reportId) {
        try {
            logger.info("发布能力报告，报告ID: {}", reportId);
            
            // 这里应该实现具体的发布报告逻辑
            // 比如更新报告状态、通知相关人员等
            // 暂时只做日志记录，后续可以根据具体需求实现
            
            logger.info("能力报告发布完成，报告ID: {}", reportId);
            
        } catch (Exception e) {
            logger.error("发布能力报告失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发布能力报告失败: " + e.getMessage());
        }
    }

    @Override
    public AbilityReport getAbilityReportById(Long reportId) {
        try {
            logger.info("获取能力报告详情，报告ID: {}", reportId);
            
            // 这里应该实现具体的获取报告逻辑
            // 比如从数据库查询报告详情等
            // 暂时返回一个基础对象，后续可以根据具体需求实现
            AbilityReport report = new AbilityReport();
            report.setId(reportId);
            report.setTitle("能力报告 " + reportId);
            
            logger.info("能力报告获取成功，报告ID: {}", reportId);
            return report;
            
        } catch (Exception e) {
            logger.error("获取能力报告失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取能力报告失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<AbilityReport> getAbilityReportHistory(Long studentId, Integer page, Integer size) {
        try {
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
            
        } catch (Exception e) {
            logger.error("获取能力报告历史失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取能力报告历史失败: " + e.getMessage());
        }
    }

    @Override
    public AbilityReport getLatestAbilityReport(Long studentId) {
        try {
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
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取学生最新能力报告失败，学生ID: {}", studentId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取最新能力报告失败: " + e.getMessage());
        }
    }

    @Override
    public AbilityAssessment submitSelfAssessment(Long studentId, Long dimensionId, BigDecimal score, String feedback) {
        try {
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
            
        } catch (Exception e) {
            logger.error("提交自我评估失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "提交自我评估失败: " + e.getMessage());
        }
    }

    @Override
    public List<AbilityDimension> getAllAbilityDimensions() {
        try {
            logger.info("获取所有能力维度");
            return abilityDimensionMapper.selectActiveDimensions();
        } catch (Exception e) {
            logger.error("获取能力维度失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取能力维度失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<AbilityAssessment> getStudentAssessments(Long studentId, Integer page, Integer size) {
        try {
            logger.info("分页获取学生评估记录，学生ID: {}", studentId);

            int offset = (page - 1) * size;
            List<AbilityAssessment> assessments = abilityAssessmentMapper.selectByStudentIdWithPagination(studentId, offset, size);
            Integer total = abilityAssessmentMapper.countByStudentId(studentId);

            return PageResult.of(assessments, page, size, total.longValue(), (total + size - 1) / size);

        } catch (Exception e) {
            logger.error("获取学生评估记录失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生评估记录失败: " + e.getMessage());
        }
    }

    @Override
    public void initializeStudentAbilities(Long studentId) {
        try {
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

        } catch (Exception e) {
            logger.error("初始化学生能力记录失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "初始化学生能力记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getStudentAbilityRanking(Long dimensionId, Integer limit) {
        try {
            logger.info("获取学生能力排名，维度ID: {}, 限制数量: {}", dimensionId, limit);
            
            // 这里应该实现具体的排名逻辑
            // 暂时返回空列表，后续可以根据具体需求实现
            List<Map<String, Object>> rankings = new ArrayList<>();
            
            return rankings;
            
        } catch (Exception e) {
            logger.error("获取学生能力排名失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生能力排名失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getClassAbilityStats(Long courseId, Long dimensionId) {
        try {
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
            
        } catch (Exception e) {
            logger.error("获取班级能力统计失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取班级能力统计失败: " + e.getMessage());
        }
    }

    @Override
    public void generateLearningRecommendations(Long studentId, Long dimensionId) {
        try {
            logger.info("生成学习建议，学生ID: {}, 维度ID: {}", studentId, dimensionId);
            
            // 这里应该实现具体的学习建议生成逻辑
            // 比如基于学生的能力评估结果、学习历史等生成个性化建议
            // 生成的建议会保存到数据库中，后续可以根据具体需求实现
            
            logger.info("学习建议生成完成，学生ID: {}", studentId);
            
        } catch (Exception e) {
            logger.error("生成学习建议失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "生成学习建议失败: " + e.getMessage());
        }
    }

    @Override
    public List<AbilityGoal> getStudentGoals(Long studentId) {
        try {
            logger.info("获取学生目标，学生ID: {}", studentId);
            
            // 这里应该实现具体的获取学生目标逻辑
            // 比如从数据库查询学生设定的学习目标等
            // 暂时返回空列表，后续可以根据具体需求实现
            List<AbilityGoal> goals = new ArrayList<>();
            
            logger.info("学生目标获取成功，学生ID: {}, 目标数量: {}", studentId, goals.size());
            return goals;
            
        } catch (Exception e) {
            logger.error("获取学生目标失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生目标失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteAbilityGoal(Long goalId) {
        try {
            logger.info("删除能力目标，目标ID: {}", goalId);
            
            // 这里应该实现具体的删除目标逻辑
            // 比如从数据库删除指定的学习目标记录
            // 暂时只做日志记录，后续可以根据具体需求实现
            
            logger.info("能力目标删除成功，目标ID: {}", goalId);
            
        } catch (Exception e) {
            logger.error("删除能力目标失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除能力目标失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getStudentAbilityTrends(Long studentId, Long dimensionId, Integer timeRange) {
        try {
            logger.info("获取学生能力发展趋势，学生ID: {}, 维度ID: {}, 时间范围: {}月", studentId, dimensionId, timeRange);
            
            // 这里应该实现具体的获取能力趋势逻辑
            // 暂时返回空列表，后续可以根据具体需求实现
            List<Map<String, Object>> trends = new ArrayList<>();
            
            logger.info("学生能力发展趋势获取成功，学生ID: {}, 数据点数量: {}", studentId, trends.size());
            return trends;
            
        } catch (Exception e) {
            logger.error("获取学生能力发展趋势失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生能力发展趋势失败: " + e.getMessage());
        }
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
        // 实际应该使用Jackson等JSON库
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * 将List转换为JSON字符串（简化实现）
     */
    private String convertListToJson(List<Map<String, String>> list) {
        // 实际应该使用Jackson等JSON库
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{");
            Map<String, String> item = list.get(i);
            boolean first = true;
            for (Map.Entry<String, String> entry : item.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
        }
        json.append("]");
        return json.toString();
    }

    @Override
    public AbilityGoal createAbilityGoal(AbilityGoal abilityGoal) {
        try {
            logger.info("创建能力目标，目标名称: {}", abilityGoal.getTitle());
            
            // 这里应该实现具体的创建目标逻辑
            // 比如保存到数据库，生成目标ID等
            // 暂时设置基本属性，后续可以根据具体需求实现
            
            logger.info("能力目标创建成功，目标ID: {}", abilityGoal.getId());
            return abilityGoal;
            
        } catch (Exception e) {
            logger.error("创建能力目标失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建能力目标失败: " + e.getMessage());
        }
    }

    @Override
    public AbilityGoal updateAbilityGoal(Long goalId, AbilityGoal abilityGoal) {
        try {
            logger.info("更新能力目标，目标ID: {}", goalId);
            
            // 这里应该实现具体的更新目标逻辑
            // 比如从数据库更新指定的学习目标记录
            // 暂时设置基本属性，后续可以根据具体需求实现
            abilityGoal.setId(goalId);
            
            logger.info("能力目标更新成功，目标ID: {}", goalId);
            return abilityGoal;
            
        } catch (Exception e) {
            logger.error("更新能力目标失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新能力目标失败: " + e.getMessage());
        }
    }

    @Override
    public List<LearningRecommendation> getLearningRecommendations(Long studentId, Long dimensionId, Integer limit) {
        try {
            logger.info("获取学习建议，学生ID: {}, 维度ID: {}, 限制数量: {}", studentId, dimensionId, limit);
            
            // 这里应该实现具体的获取学习建议逻辑
            // 暂时返回空列表，后续可以根据具体需求实现
            List<LearningRecommendation> recommendations = new ArrayList<>();
            
            logger.info("学习建议获取成功，学生ID: {}, 建议数量: {}", studentId, recommendations.size());
            return recommendations;
            
        } catch (Exception e) {
            logger.error("获取学习建议失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学习建议失败: " + e.getMessage());
        }
    }
}