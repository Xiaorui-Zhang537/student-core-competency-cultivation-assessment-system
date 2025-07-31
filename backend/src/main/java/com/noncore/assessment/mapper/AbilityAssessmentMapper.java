package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AbilityAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 能力评估Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface AbilityAssessmentMapper {

    /**
     * 插入能力评估记录
     */
    int insertAssessment(AbilityAssessment assessment);

    /**
     * 根据ID查询能力评估记录
     */
    AbilityAssessment selectAssessmentById(Long id);

    /**
     * 更新能力评估记录
     */
    int updateAssessment(AbilityAssessment assessment);

    /**
     * 删除能力评估记录
     */
    int deleteAssessment(Long id);

    /**
     * 根据学生ID查询评估记录
     */
    List<AbilityAssessment> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 分页查询学生评估记录
     */
    List<AbilityAssessment> selectByStudentIdWithPagination(@Param("studentId") Long studentId,
                                                            @Param("offset") int offset,
                                                            @Param("size") Integer size);

    /**
     * 统计学生评估记录数量
     */
    Integer countByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据维度ID查询评估记录
     */
    List<AbilityAssessment> selectByDimensionId(@Param("dimensionId") Long dimensionId);

    /**
     * 根据学生ID和维度ID查询评估记录
     */
    List<AbilityAssessment> selectByStudentAndDimension(@Param("studentId") Long studentId,
                                                        @Param("dimensionId") Long dimensionId);

    /**
     * 根据学生和时间范围查询评估记录
     */
    List<AbilityAssessment> selectByStudentAndPeriod(@Param("studentId") Long studentId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近的评估记录
     */
    List<AbilityAssessment> getRecentAssessments(@Param("studentId") Long studentId,
                                                 @Param("limit") Integer limit);

    /**
     * 根据评估类型查询记录
     */
    List<AbilityAssessment> selectByAssessmentType(@Param("assessmentType") String assessmentType,
                                                   @Param("studentId") Long studentId);

    /**
     * 分页查询评估记录（支持多条件）
     */
    List<AbilityAssessment> selectAssessmentsWithPagination(@Param("studentId") Long studentId,
                                                            @Param("dimensionId") Long dimensionId,
                                                            @Param("assessmentType") String assessmentType,
                                                            @Param("status") String status,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("offset") int offset,
                                                            @Param("size") Integer size);

    /**
     * 统计评估记录数量（支持多条件）
     */
    Integer countAssessments(@Param("studentId") Long studentId,
                            @Param("dimensionId") Long dimensionId,
                            @Param("assessmentType") String assessmentType,
                            @Param("status") String status,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取学生能力评估统计
     */
    Map<String, Object> getStudentAssessmentStats(@Param("studentId") Long studentId);

    /**
     * 获取维度评估统计
     */
    Map<String, Object> getDimensionAssessmentStats(@Param("dimensionId") Long dimensionId);

    /**
     * 获取评估趋势数据
     */
    List<Map<String, Object>> getAssessmentTrends(@Param("studentId") Long studentId,
                                                  @Param("dimensionId") Long dimensionId,
                                                  @Param("months") Integer months);

    /**
     * 批量插入评估记录
     */
    int batchInsertAssessments(@Param("assessments") List<AbilityAssessment> assessments);

    /**
     * 批量更新评估状态
     */
    int batchUpdateAssessmentStatus(@Param("assessmentIds") List<Long> assessmentIds,
                                   @Param("status") String status);

    /**
     * 获取待审核的评估记录
     */
    List<AbilityAssessment> selectPendingAssessments(@Param("assessorId") Long assessorId,
                                                     @Param("limit") Integer limit);

    /**
     * 根据置信度范围查询评估记录
     */
    List<AbilityAssessment> selectByConfidenceRange(@Param("studentId") Long studentId,
                                                    @Param("minConfidence") java.math.BigDecimal minConfidence,
                                                    @Param("maxConfidence") java.math.BigDecimal maxConfidence);

    /**
     * 获取高分评估记录
     */
    List<AbilityAssessment> selectHighScoreAssessments(@Param("studentId") Long studentId,
                                                       @Param("minScore") java.math.BigDecimal minScore,
                                                       @Param("limit") Integer limit);

    /**
     * 删除旧的评估记录
     */
    int deleteOldAssessments(@Param("beforeDate") LocalDateTime beforeDate);
}