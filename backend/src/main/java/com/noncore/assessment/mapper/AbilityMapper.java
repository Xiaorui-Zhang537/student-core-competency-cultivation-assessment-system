package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Ability;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author NonCore
 * @version 1.0
 * @date 2025/05/20
 */
@Mapper
public interface AbilityMapper {

    /**
     * 插入新的能力项
     *
     * @param ability a
     * @return int
     */
    int insertAbility(Ability ability);

    /**
     * 根据ID更新能力项
     *
     * @param ability a
     * @return int
     */
    int updateAbility(Ability ability);

    /**
     * 根据ID删除能力项 (软删除)
     *
     * @param abilityId a
     * @return int
     */
    int deleteAbilityById(@Param("abilityId") String abilityId);

    /**
     * 根据ID查询能力项
     *
     * @param abilityId a
     * @return {@link Ability}
     */
    Ability selectAbilityById(@Param("abilityId") String abilityId);

    /**
     * 查询所有能力项 (未删除)
     *
     * @return {@link List}<{@link Ability}>
     */
    List<Ability> selectAllAbilities();

    /**
     * 根据能力维度ID查询能力项
     *
     * @param dimensionId d
     * @return {@link List}<{@link Ability}>
     */
    List<Ability> selectAbilitiesByDimensionId(@Param("dimensionId") String dimensionId);

    /**
     * 批量插入能力项
     *
     * @param abilityList a
     * @return int
     */
    int batchInsertAbilities(@Param("abilityList") List<Ability> abilityList);

    /**
     * 根据名称模糊查询能力项
     *
     * @param name n
     * @return {@link List}<{@link Ability}>
     */
    List<Ability> searchAbilitiesByName(@Param("name") String name);
} 