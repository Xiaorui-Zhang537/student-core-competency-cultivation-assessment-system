package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.BehaviorInsight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BehaviorInsightMapper {
    int insert(BehaviorInsight insight);

    BehaviorInsight getLatestBySnapshot(@Param("snapshotId") Long snapshotId,
                                        @Param("schemaVersion") String schemaVersion);

    /**
     * 获取学生在课程（可空）下的最新洞察记录（用于冷却治理：7天仅一次）。
     */
    BehaviorInsight getLatestByStudentCourse(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId,
                                             @Param("schemaVersion") String schemaVersion);

    /**
     * 获取学生在课程（可空）+ 时间窗(rangeKey) 下的最新洞察（用于 getLatest 回退与历史列表）。
     *
     * <p>通过 snapshot_id 关联 behavior_summary_snapshots，确保 rangeKey 一致。</p>
     */
    BehaviorInsight getLatestByStudentCourseRange(@Param("studentId") Long studentId,
                                                  @Param("courseId") Long courseId,
                                                  @Param("rangeKey") String rangeKey,
                                                  @Param("schemaVersion") String schemaVersion);

    /**
     * 获取学生在任意课程（含 course_id 为空）+ 时间窗(rangeKey) 下的最新洞察。
     */
    BehaviorInsight getLatestByStudentAnyCourseRange(@Param("studentId") Long studentId,
                                                     @Param("rangeKey") String rangeKey,
                                                     @Param("schemaVersion") String schemaVersion);

    /**
     * 获取学生在任意课程（含 course_id 为空）下的最新洞察。
     */
    BehaviorInsight getLatestByStudentAnyCourse(@Param("studentId") Long studentId,
                                                @Param("schemaVersion") String schemaVersion);

    /**
     * 按学生+课程（可空）+时间窗分页查询历史洞察（倒序）。
     */
    java.util.List<BehaviorInsight> pageByStudentCourseRange(@Param("studentId") Long studentId,
                                                              @Param("courseId") Long courseId,
                                                              @Param("rangeKey") String rangeKey,
                                                              @Param("schemaVersion") String schemaVersion,
                                                              @Param("offset") int offset,
                                                              @Param("size") int size);

    /**
     * 统计学生+课程（可空）+时间窗的洞察总数。
     */
    long countByStudentCourseRange(@Param("studentId") Long studentId,
                                   @Param("courseId") Long courseId,
                                   @Param("rangeKey") String rangeKey,
                                   @Param("schemaVersion") String schemaVersion);

    /**
     * 根据主键查询洞察记录。
     */
    BehaviorInsight getById(@Param("id") Long id);

    /**
     * 统计某学生在指定时间点之后生成的洞察次数（用于限流）。
     *
     * <p>注意：仅统计 status=success 的记录（failed/partial 均不应计入调用额度）。</p>
     */
    long countByStudentSince(@Param("studentId") Long studentId,
                             @Param("schemaVersion") String schemaVersion,
                             @Param("since") java.time.LocalDateTime since);

    /**
     * 获取指定时间点之后、计入额度的最早一条洞察生成时间。
     */
    java.time.LocalDateTime getEarliestCountedByStudentSince(@Param("studentId") Long studentId,
                                                             @Param("since") java.time.LocalDateTime since);
}
