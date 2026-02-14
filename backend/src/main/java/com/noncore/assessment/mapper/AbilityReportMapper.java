package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AbilityReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能力报告Mapper接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface AbilityReportMapper {

    /**
     * 插入能力报告
     */
    int insertReport(AbilityReport report);

    /**
     * 根据ID查询能力报告
     */
    AbilityReport selectReportById(Long id);

    /**
     * 更新能力报告
     */
    int updateReport(AbilityReport report);

    /**
     * 删除能力报告
     */
    int deleteReport(Long id);

    /**
     * 根据学生ID查询报告
     */
    List<AbilityReport> selectReportsByStudentId(@Param("studentId") Long studentId);

    /**
     * 分页查询能力报告
     */
    List<AbilityReport> selectReportsWithPagination(@Param("studentId") Long studentId,
                                                    @Param("reportType") String reportType,
                                                    @Param("isPublished") Boolean isPublished,
                                                    @Param("offset") int offset,
                                                    @Param("size") Integer size);

    /**
     * 统计能力报告数量
     */
    Integer countReports(@Param("studentId") Long studentId,
                        @Param("reportType") String reportType,
                        @Param("isPublished") Boolean isPublished);

    /**
     * 获取学生最新的报告
     */
    AbilityReport selectLatestReportByStudent(@Param("studentId") Long studentId);

    /**
     * 根据报告类型和时间范围查询报告
     */
    List<AbilityReport> selectReportsByTypeAndPeriod(@Param("studentId") Long studentId,
                                                     @Param("reportType") String reportType,
                                                     @Param("periodStart") LocalDate periodStart,
                                                     @Param("periodEnd") LocalDate periodEnd);

    /**
     * 检查报告是否存在
     */
    boolean existsByStudentAndPeriod(@Param("studentId") Long studentId,
                                    @Param("reportType") String reportType,
                                    @Param("periodStart") LocalDate periodStart,
                                    @Param("periodEnd") LocalDate periodEnd);

    /**
     * 发布报告
     */
    int publishReport(@Param("id") Long id);

    /**
     * 取消发布报告
     */
    int unpublishReport(@Param("id") Long id);

    /**
     * 批量删除旧报告
     */
    int deleteOldReports(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 获取报告统计信息
     */
    java.util.Map<String, Object> getReportStats(@Param("studentId") Long studentId);

    /**
     * 根据类型统计报告数量
     */
    List<java.util.Map<String, Object>> countReportsByType(@Param("studentId") Long studentId);

    /**
     * 获取最高分报告
     */
    List<AbilityReport> selectTopScoreReports(@Param("studentId") Long studentId,
                                             @Param("limit") Integer limit);

    /**
     * 查询需要生成的报告（系统自动生成）
     */
    List<Long> selectStudentsForReportGeneration(@Param("reportType") String reportType,
                                                @Param("lastGeneratedBefore") LocalDate lastGeneratedBefore);

    /**
     * 按作业/提交上下文获取最新AI报告
     */
    AbilityReport selectLatestReportByContext(java.util.Map<String, Object> params);                                         

    /**
     * 雷达统计使用：按课程/学生/作业筛选AI报告
     */
    List<AbilityReport> selectReportsForRadar(@Param("studentId") Long studentId,
                                              @Param("courseId") Long courseId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              @Param("assignmentIds") List<Long> assignmentIds);

    /**
     * 管理员：跨学生检索能力报告（分页）。
     */
    List<AbilityReport> selectAdminReports(@Param("studentId") Long studentId,
                                           @Param("reportType") String reportType,
                                           @Param("isPublished") Boolean isPublished,
                                           @Param("courseId") Long courseId,
                                           @Param("assignmentId") Long assignmentId,
                                           @Param("submissionId") Long submissionId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("offset") int offset,
                                           @Param("size") Integer size);

    /**
     * 管理员：跨学生检索能力报告（计数）。
     */
    Integer countAdminReports(@Param("studentId") Long studentId,
                              @Param("reportType") String reportType,
                              @Param("isPublished") Boolean isPublished,
                              @Param("courseId") Long courseId,
                              @Param("assignmentId") Long assignmentId,
                              @Param("submissionId") Long submissionId,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);
} 

