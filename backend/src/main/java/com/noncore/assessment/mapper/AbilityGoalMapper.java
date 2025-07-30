package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AbilityGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 能力目标Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface AbilityGoalMapper {

    /**
     * 插入能力目标
     */
    int insertGoal(AbilityGoal goal);

    /**
     * 根据ID查询能力目标
     */
    AbilityGoal selectGoalById(Long id);

    /**
     * 更新能力目标
     */
    int updateGoal(AbilityGoal goal);

    /**
     * 删除能力目标
     */
    int deleteGoal(Long id);

    /**
     * 根据学生ID查询所有目标
     */
    List<AbilityGoal> selectGoalsByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据学生ID查询活跃目标
     */
    List<AbilityGoal> selectActiveGoalsByStudent(@Param("studentId") Long studentId);

    /**
     * 根据学生ID和维度ID查询活跃目标
     */
    List<AbilityGoal> selectActiveGoalsByStudentAndDimension(@Param("studentId") Long studentId,
                                                             @Param("dimensionId") Long dimensionId);

    /**
     * 分页查询能力目标
     */
    List<AbilityGoal> selectGoalsWithPagination(@Param("studentId") Long studentId,
                                                @Param("dimensionId") Long dimensionId,
                                                @Param("status") String status,
                                                @Param("offset") int offset,
                                                @Param("size") Integer size);

    /**
     * 统计能力目标数量
     */
    Integer countGoals(@Param("studentId") Long studentId,
                      @Param("dimensionId") Long dimensionId,
                      @Param("status") String status);

    /**
     * 根据维度ID查询目标
     */
    List<AbilityGoal> selectGoalsByDimensionId(@Param("dimensionId") Long dimensionId);

    /**
     * 批量更新目标状态
     */
    int batchUpdateGoalStatus(@Param("goalIds") List<Long> goalIds,
                             @Param("status") String status);

    /**
     * 获取即将到期的目标
     */
    List<AbilityGoal> selectUpcomingDeadlineGoals(@Param("studentId") Long studentId,
                                                  @Param("days") Integer days);

    /**
     * 获取已达成的目标数量
     */
    Integer countAchievedGoals(@Param("studentId") Long studentId);

    /**
     * 获取学生目标统计
     */
    java.util.Map<String, Object> getStudentGoalStats(@Param("studentId") Long studentId);
} 