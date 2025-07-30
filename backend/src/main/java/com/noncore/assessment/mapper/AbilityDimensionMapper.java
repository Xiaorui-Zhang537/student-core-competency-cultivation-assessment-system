package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AbilityDimension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 能力维度Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface AbilityDimensionMapper {

    /**
     * 插入能力维度
     */
    int insertDimension(AbilityDimension dimension);

    /**
     * 根据ID查询能力维度
     */
    AbilityDimension selectDimensionById(Long id);

    /**
     * 更新能力维度
     */
    int updateDimension(AbilityDimension dimension);

    /**
     * 删除能力维度（软删除，设置is_active=false）
     */
    int deleteDimension(Long id);

    /**
     * 查询所有激活的能力维度
     */
    List<AbilityDimension> selectActiveDimensions();

    /**
     * 根据分类查询能力维度
     */
    List<AbilityDimension> selectDimensionsByCategory(@Param("category") String category);

    /**
     * 分页查询能力维度
     */
    List<AbilityDimension> selectDimensionsWithPagination(@Param("offset") int offset,
                                                          @Param("size") Integer size,
                                                          @Param("category") String category,
                                                          @Param("isActive") Boolean isActive);

    /**
     * 统计能力维度数量
     */
    Integer countDimensions(@Param("category") String category,
                           @Param("isActive") Boolean isActive);

    /**
     * 获取最大排序顺序
     */
    Integer getMaxSortOrder();

    /**
     * 批量更新排序顺序
     */
    int batchUpdateSortOrder(@Param("dimensions") List<AbilityDimension> dimensions);

    /**
     * 根据名称查询能力维度
     */
    AbilityDimension selectDimensionByName(@Param("name") String name);

    /**
     * 检查维度名称是否存在
     */
    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);
} 