package com.noncore.assessment.service;

import com.noncore.assessment.entity.AbilityAssessment;
import com.noncore.assessment.entity.AbilityDimension;
import com.noncore.assessment.entity.AbilityGoal;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.entity.LearningRecommendation;
import com.noncore.assessment.util.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 能力评估服务接口
 * 定义能力评估相关的业务逻辑操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface AbilityService {

    /**
     * 获取学生能力发展仪表板数据
     *
     * @param studentId 学生ID
     * @return 仪表板数据
     */
    Map<String, Object> getStudentAbilityDashboard(Long studentId);

    /**
     * 生成学生能力报告
     *
     * @param studentId 学生ID
     * @param reportType 报告类型
     * @param periodStart 报告周期开始日期
     * @param periodEnd 报告周期结束日期
     * @return 能力报告
     */
    AbilityReport generateStudentReport(Long studentId, String reportType, LocalDate periodStart, LocalDate periodEnd);

    /**
     * 记录能力评估
     *
     * @param studentId 学生ID
     * @param dimensionId 能力维度ID
     * @param score 得分
     * @param assessmentType 评估类型
     * @param relatedId 关联对象ID
     * @param evidence 评估依据
     */
    void recordAbilityAssessment(Long studentId, Long dimensionId, BigDecimal score, 
                                String assessmentType, Long relatedId, String evidence);

    /**
     * 获取所有能力维度
     *
     * @return 能力维度列表
     */
    List<AbilityDimension> getAllAbilityDimensions();

    /**
     * 分页获取学生评估记录
     *
     * @param studentId 学生ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<AbilityAssessment> getStudentAssessments(Long studentId, Integer page, Integer size);

    /**
     * 初始化学生能力记录
     *
     * @param studentId 学生ID
     */
    void initializeStudentAbilities(Long studentId);

    /**
     * 获取学生能力发展趋势
     *
     * @param studentId 学生ID
     * @param dimensionId 能力维度ID（可选）
     * @param timeRange 时间范围（月数）
     * @return 趋势数据
     */
    List<Map<String, Object>> getStudentAbilityTrends(Long studentId, Long dimensionId, Integer timeRange);

    /**
     * 获取学习建议
     *
     * @param studentId 学生ID
     * @param dimensionId 能力维度ID（可选）
     * @param limit 返回数量限制
     * @return 学习建议列表
     */
    List<LearningRecommendation> getLearningRecommendations(Long studentId, Long dimensionId, Integer limit);

    /**
     * 创建能力目标
     *
     * @param goal 能力目标
     * @return 创建的目标
     */
    AbilityGoal createAbilityGoal(AbilityGoal goal);

    /**
     * 更新能力目标
     *
     * @param goalId 目标ID
     * @param goal 更新的目标信息
     * @return 更新后的目标
     */
    AbilityGoal updateAbilityGoal(Long goalId, AbilityGoal goal);

    /**
     * 删除能力目标
     *
     * @param goalId 目标ID
     */
    void deleteAbilityGoal(Long goalId);

    /**
     * 获取学生的能力目标
     *
     * @param studentId 学生ID
     * @return 目标列表
     */
    List<AbilityGoal> getStudentGoals(Long studentId);

    /**
     * 提交自评
     *
     * @param studentId 学生ID
     * @param dimensionId 能力维度ID
     * @param score 自评分数
     * @param feedback 自评反馈
     * @return 评估记录
     */
    AbilityAssessment submitSelfAssessment(Long studentId, Long dimensionId, BigDecimal score, String feedback);

    /**
     * 获取能力报告历史
     *
     * @param studentId 学生ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<AbilityReport> getAbilityReportHistory(Long studentId, Integer page, Integer size);

    /**
     * 根据ID获取能力报告
     *
     * @param reportId 报告ID
     * @return 能力报告
     */
    AbilityReport getAbilityReportById(Long reportId);

    /**
     * 获取学生最新的能力报告
     *
     * @param studentId 学生ID
     * @return 最新的能力报告，如果没有则返回null
     */
    AbilityReport getLatestAbilityReport(Long studentId);

    /**
     * 获取学生在特定上下文下的最新AI报告（优先 submissionId，其次 assignmentId，再次 courseId）
     */
    AbilityReport getLatestAbilityReportByContext(Long studentId, Long courseId, Long assignmentId, Long submissionId);

    /**
     * 发布能力报告
     *
     * @param reportId 报告ID
     */
    void publishAbilityReport(Long reportId);

    /**
     * 基于AI规范化结果创建能力报告（教师端）
     */
    AbilityReport createReportFromAi(Long studentId,
                                     String normalizedJson,
                                     String title,
                                     Long courseId,
                                     Long assignmentId,
                                     Long submissionId,
                                     Long aiHistoryId);

    /**
     * 生成学习建议
     *
     * @param studentId 学生ID
     * @param dimensionId 能力维度ID
     */
    void generateLearningRecommendations(Long studentId, Long dimensionId);

    /**
     * 标记学习建议为已读
     *
     * @param recommendationId 建议ID
     */
    void markRecommendationAsRead(Long recommendationId);

    /**
     * 采纳学习建议
     *
     * @param recommendationId 建议ID
     */
    void acceptRecommendation(Long recommendationId);

    /**
     * 获取班级能力统计
     *
     * @param courseId 课程ID
     * @param dimensionId 能力维度ID（可选）
     * @return 统计数据
     */
    List<Map<String, Object>> getClassAbilityStats(Long courseId, Long dimensionId);

    /**
     * 获取学生能力排名
     *
     * @param dimensionId 能力维度ID
     * @param limit 返回数量
     * @return 排名数据
     */
    List<Map<String, Object>> getStudentAbilityRanking(Long dimensionId, Integer limit);
} 