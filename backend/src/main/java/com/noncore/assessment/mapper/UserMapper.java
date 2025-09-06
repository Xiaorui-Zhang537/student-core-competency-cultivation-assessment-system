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
     * 根据课程ID查询学生（带搜索）
     */
    List<User> selectStudentsByCourseIdWithSearch(@Param("courseId") Long courseId, @Param("keyword") String keyword);

    /**
     * 根据课程ID查询学生（高级筛选与排序）
     */
    List<User> selectStudentsByCourseIdAdvanced(
            @Param("courseId") Long courseId,
            @Param("keyword") String keyword,
            @Param("activity") String activity,
            @Param("gradeFilter") String gradeFilter,
            @Param("progressFilter") String progressFilter,
            @Param("sortBy") String sortBy
    );

    /**
     * 判定用户是否为课程的在读学生（active/completed）
     */
    int isStudentInCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

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
     * 检查学号是否存在
     */
    int checkStudentNoExists(@Param("studentNo") String studentNo, @Param("excludeId") Long excludeId);

    /**
     * 检查工号是否存在
     */
    int checkTeacherNoExists(@Param("teacherNo") String teacherNo, @Param("excludeId") Long excludeId);

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
    int batchUpdateUserStatus(@Param("userIds") List<Long> userIds, @Param("deleted") boolean deleted);

    List<com.noncore.assessment.dto.response.TeacherDashboardResponse.StudentOverviewDto> findStudentOverviewsByTeacher(@Param("teacherId") Long teacherId, @Param("limit") int limit);

    /**
     * 查询所有用户ID
     * @return 用户ID列表
     */
    List<Long> selectAllUserIds();

    /**
     * 根据角色查询用户ID列表
     * @param role 角色
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRole(@Param("role") String role);

    /**
     * 更新用户邮箱验证状态
     *
     * @param userId 用户ID
     * @param isVerified 是否已验证
     * @return 影响的行数
     */
    int updateEmailVerified(@Param("userId") Long userId, @Param("isVerified") boolean isVerified);

    /**
     * 根据用户名或邮箱检查用户是否存在
     * @param identifier 用户名或邮箱
     * @return 是否存在
     */
    int checkUserExistsByIdentifier(@Param("identifier") String identifier);
} 