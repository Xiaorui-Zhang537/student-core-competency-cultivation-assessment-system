package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.LearningRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习建议Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface LearningRecommendationMapper {

    /**
     * 插入学习建议
     */
    int insertRecommendation(LearningRecommendation recommendation);

    /**
     * 根据ID查询学习建议
     */
    LearningRecommendation selectRecommendationById(Long id);

    /**
     * 更新学习建议
     */
    int updateRecommendation(LearningRecommendation recommendation);

    /**
     * 删除学习建议
     */
    int deleteRecommendation(Long id);

    /**
     * 根据学生ID查询学习建议
     */
    List<LearningRecommendation> selectByStudentId(@Param("studentId") Long studentId,
                                                   @Param("limit") Integer limit);

    /**
     * 根据学生ID和维度ID查询学习建议
     */
    List<LearningRecommendation> selectByStudentAndDimension(@Param("studentId") Long studentId,
                                                             @Param("dimensionId") Long dimensionId,
                                                             @Param("limit") Integer limit);

    /**
     * 分页查询学习建议
     */
    List<LearningRecommendation> selectRecommendationsWithPagination(@Param("studentId") Long studentId,
                                                                     @Param("dimensionId") Long dimensionId,
                                                                     @Param("recommendationType") String recommendationType,
                                                                     @Param("isRead") Boolean isRead,
                                                                     @Param("offset") int offset,
                                                                     @Param("size") Integer size);

    /**
     * 统计学习建议数量
     */
    Integer countRecommendations(@Param("studentId") Long studentId,
                                @Param("dimensionId") Long dimensionId,
                                @Param("recommendationType") String recommendationType,
                                @Param("isRead") Boolean isRead);

    /**
     * 根据优先级查询学习建议
     */
    List<LearningRecommendation> selectByPriority(@Param("studentId") Long studentId,
                                                  @Param("minPriority") java.math.BigDecimal minPriority,
                                                  @Param("limit") Integer limit);

    /**
     * 标记建议为已读
     */
    int markAsRead(@Param("id") Long id);

    /**
     * 标记建议为已采纳
     */
    int markAsAccepted(@Param("id") Long id);

    /**
     * 批量删除过期建议
     */
    int deleteExpiredRecommendations();

    /**
     * 获取未读建议数量
     */
    Integer countUnreadRecommendations(@Param("studentId") Long studentId);

    /**
     * 根据类型统计建议数量
     */
    List<java.util.Map<String, Object>> countRecommendationsByType(@Param("studentId") Long studentId);

    /**
     * 获取最受欢迎的建议类型
     */
    List<java.util.Map<String, Object>> getPopularRecommendationTypes(@Param("limit") Integer limit);
} 