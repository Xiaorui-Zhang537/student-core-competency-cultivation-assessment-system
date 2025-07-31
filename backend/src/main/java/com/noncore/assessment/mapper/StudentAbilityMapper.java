package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.StudentAbility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 学生能力记录Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface StudentAbilityMapper {

    /**
     * 插入学生能力记录
     */
    int insertStudentAbility(StudentAbility studentAbility);

    /**
     * 根据ID查询学生能力记录
     */
    StudentAbility selectStudentAbilityById(Long id);

    /**
     * 更新学生能力记录
     */
    int updateStudentAbility(StudentAbility studentAbility);

    /**
     * 删除学生能力记录
     */
    int deleteStudentAbility(Long id);

    /**
     * 根据学生ID和维度ID查询能力记录
     */
    StudentAbility selectByStudentAndDimension(@Param("studentId") Long studentId,
                                              @Param("dimensionId") Long dimensionId);

    /**
     * 根据学生ID查询所有能力记录
     */
    List<StudentAbility> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据维度ID查询所有学生的能力记录
     */
    List<StudentAbility> selectByDimensionId(@Param("dimensionId") Long dimensionId);

    /**
     * 分页查询学生能力记录
     */
    List<StudentAbility> selectStudentAbilitiesWithPagination(@Param("studentId") Long studentId,
                                                              @Param("dimensionId") Long dimensionId,
                                                              @Param("category") String category,
                                                              @Param("level") String level,
                                                              @Param("offset") int offset,
                                                              @Param("size") Integer size);

    /**
     * 统计学生能力记录数量
     */
    Integer countStudentAbilities(@Param("studentId") Long studentId,
                                 @Param("dimensionId") Long dimensionId,
                                 @Param("category") String category,
                                 @Param("level") String level);

    /**
     * 更新学生能力分数
     */
    int updateStudentScore(@Param("studentId") Long studentId,
                          @Param("dimensionId") Long dimensionId,
                          @Param("newScore") BigDecimal newScore);

    /**
     * 批量更新学生能力记录
     */
    int batchUpdateStudentAbilities(@Param("abilities") List<StudentAbility> abilities);

    /**
     * 获取学生能力统计信息
     */
    Map<String, Object> getStudentAbilityStats(@Param("studentId") Long studentId);

    /**
     * 获取班级/课程能力统计
     */
    List<Map<String, Object>> getClassAbilityStats(@Param("courseId") Long courseId,
                                                   @Param("dimensionId") Long dimensionId);

    /**
     * 获取学生能力排名
     */
    List<Map<String, Object>> getStudentAbilityRanking(@Param("dimensionId") Long dimensionId,
                                                       @Param("limit") Integer limit);

    /**
     * 初始化学生能力记录（为新学生创建所有维度的初始记录）
     */
    int initializeStudentAbilities(@Param("studentId") Long studentId);

    /**
     * 获取学生能力发展趋势数据
     */
    List<Map<String, Object>> getStudentAbilityTrends(@Param("studentId") Long studentId,
                                                      @Param("dimensionId") Long dimensionId,
                                                      @Param("months") Integer months);

    /**
     * 检查学生能力记录是否存在
     */
    boolean existsByStudentAndDimension(@Param("studentId") Long studentId,
                                       @Param("dimensionId") Long dimensionId);

    BigDecimal selectOverallScoreByStudentId(@Param("studentId") Long studentId);
} 