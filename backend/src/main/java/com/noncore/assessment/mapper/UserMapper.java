package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层Mapper接口
 * 定义用户相关的数据库操作方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface UserMapper {

    /**
     * 插入新用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int insertUser(User user);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User selectUserById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User selectUserByEmail(String email);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int updateUser(User user);

    /**
     * 软删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteUser(Long id);

    /**
     * 根据角色查询用户列表
     *
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> selectUsersByRole(String role);

    /**
     * 根据课程ID查询已选课学生
     *
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<User> selectStudentsByCourseId(Long courseId);

    /**
     * 分页查询用户列表
     *
     * @param role 角色筛选（可选）
     * @param school 学校筛选（可选）
     * @param grade 年级筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return 用户列表
     */
    List<User> selectUsersWithConditions(
            @Param("role") String role,
            @Param("school") String school,
            @Param("grade") String grade,
            @Param("keyword") String keyword
    );

    /**
     * 统计用户总数
     *
     * @param role 角色筛选（可选）
     * @return 用户总数
     */
    Long countUsers(@Param("role") String role);

    /**
     * 统计各角色用户数量
     *
     * @return 统计结果
     */
    List<Object> countUsersByRole();

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    int checkUsernameExists(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    int checkEmailExists(@Param("email") String email, @Param("excludeId") Long excludeId);

    /**
     * 更新用户最后登录时间
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int updateLastLoginTime(Long id);

    /**
     * 更新用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（已加密）
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String newPassword);

    /**
     * 根据ID列表批量查询用户
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> selectUsersByIds(@Param("userIds") List<Long> userIds);

    /**
     * 搜索用户（模糊查询）
     *
     * @param keyword 搜索关键词
     * @param role 角色筛选（可选）
     * @return 用户列表
     */
    List<User> searchUsers(@Param("keyword") String keyword, @Param("role") String role);

    /**
     * 获取活跃用户列表（最近登录）
     *
     * @param days 天数
     * @return 用户列表
     */
    List<User> getActiveUsers(@Param("days") Integer days);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param deleted 删除状态
     * @return 影响行数
     */
    int batchUpdateUserStatus(@Param("userIds") List<Long> userIds, @Param("deleted") Boolean deleted);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User selectByEmail(@Param("email") String email);
} 