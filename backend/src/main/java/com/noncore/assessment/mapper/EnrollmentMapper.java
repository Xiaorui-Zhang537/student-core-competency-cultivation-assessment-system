package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 选课记录数据访问接口
 * 处理enrollments表的CRUD操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface EnrollmentMapper {

    /**
     * 插入选课记录
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param status 选课状态
     * @param enrolledAt 选课时间
     * @return 影响的行数
     */
    int insertEnrollment(@Param("studentId") Long studentId, 
                        @Param("courseId") Long courseId,
                        @Param("status") String status,
                        @Param("enrolledAt") LocalDateTime enrolledAt);

    /**
     * 删除选课记录（退课）
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 影响的行数
     */
    int deleteEnrollment(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 更新选课记录
     *
     * @param enrollment 选课记录对象
     * @return 影响的行数
     */
    int updateEnrollment(Enrollment enrollment);

    /**
     * 根据ID查询选课记录
     *
     * @param id 选课记录ID
     * @return 选课记录
     */
    Enrollment selectEnrollmentById(@Param("id") Long id);

    /**
     * 根据学生ID和课程ID查询选课记录
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 选课记录
     */
    Enrollment selectEnrollmentByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 检查选课记录是否存在
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 存在数量（0或1）
     */
    int checkEnrollmentExists(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 根据学生ID查询所有选课记录
     *
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<Enrollment> selectEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据课程ID查询所有选课记录
     *
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    List<Enrollment> selectEnrollmentsByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据状态查询选课记录
     *
     * @param status 选课状态
     * @return 选课记录列表
     */
    List<Enrollment> selectEnrollmentsByStatus(@Param("status") String status);

    /**
     * 更新学习进度
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param progress 学习进度
     * @return 影响的行数
     */
    int updateProgress(@Param("studentId") Long studentId, 
                      @Param("courseId") Long courseId, 
                      @Param("progress") Double progress);

    /**
     * 更新最后访问时间
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 影响的行数
     */
    int updateLastAccessTime(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * 完成课程学习
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param grade 课程成绩
     * @return 影响的行数
     */
    int completeCourse(@Param("studentId") Long studentId, 
                      @Param("courseId") Long courseId, 
                      @Param("grade") Double grade);

    /**
     * 批量更新选课状态
     *
     * @param enrollmentIds 选课记录ID列表
     * @param status 新状态
     * @return 影响的行数
     */
    int batchUpdateStatus(@Param("enrollmentIds") List<Long> enrollmentIds, @Param("status") String status);

    /**
     * 统计选课数量
     *
     * @param studentId 学生ID（可选）
     * @param courseId 课程ID（可选）
     * @param status 选课状态（可选）
     * @return 选课数量
     */
    long countEnrollments(@Param("studentId") Long studentId, 
                        @Param("courseId") Long courseId, 
                        @Param("status") String status);

    long countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 获取学生的学习统计
     *
     * @param studentId 学生ID
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> getStudentLearningStats(@Param("studentId") Long studentId);

    /**
     * 获取课程的选课统计
     *
     * @param courseId 课程ID
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> getCourseEnrollmentStats(@Param("courseId") Long courseId);

    /**
     * 查询即将过期的课程（学生视角）
     *
     * @param studentId 学生ID
     * @param days 天数
     * @return 选课记录列表
     */
    List<Enrollment> selectExpiringEnrollments(@Param("studentId") Long studentId, @Param("days") Integer days);

    /**
     * 查询活跃学习的学生（最近有学习活动）
     *
     * @param courseId 课程ID
     * @param days 天数
     * @return 选课记录列表
     */
    List<Enrollment> selectActiveStudents(@Param("courseId") Long courseId, @Param("days") Integer days);

    /**
     * 分页查询选课记录
     *
     * @param studentId 学生ID（可选）
     * @param courseId 课程ID（可选）
     * @param status 选课状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 选课记录列表
     */
    List<Enrollment> selectEnrollmentsWithPagination(@Param("studentId") Long studentId,
                                                    @Param("courseId") Long courseId,
                                                    @Param("status") String status,
                                                    @Param("keyword") String keyword);

    long countStudentsByTeacher(@Param("teacherId") Long teacherId);

    long countWeeklyActiveStudentsByTeacher(@Param("teacherId") Long teacherId);
    
    List<Long> selectEnrolledStudentIds(@Param("courseId") Long courseId, @Param("studentIds") List<Long> studentIds);

    void batchInsertEnrollments(@Param("enrollments") List<Enrollment> enrollments);

    /** 统计课程在读（active）选课人数 */
    long countActiveByCourse(@Param("courseId") Long courseId);

    /** 查询课程在读（active）学生ID列表 */
    List<Long> findActiveStudentIdsByCourse(@Param("courseId") Long courseId);

    /**
     * 分页查询学生的课程（含教师名、报名时间、进度），支持课程标题关键字
     */
    List<java.util.Map<String, Object>> selectStudentCoursesPaged(@Param("studentId") Long studentId,
                                                                 @Param("keyword") String keyword);
} 