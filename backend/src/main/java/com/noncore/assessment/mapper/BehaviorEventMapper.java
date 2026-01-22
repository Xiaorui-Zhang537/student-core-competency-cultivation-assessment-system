package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.BehaviorEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BehaviorEventMapper {
    int insert(BehaviorEvent event);

    List<BehaviorEvent> listByStudentCourseRange(@Param("studentId") Long studentId,
                                                 @Param("courseId") Long courseId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to,
                                                 @Param("limit") Integer limit);

    /**
     * 检查是否存在晚于指定时间点的事件（用于判断快照是否过期）。
     *
     * @param studentId 学生ID
     * @param courseId 课程ID（可空）
     * @param since 时间点（不包含该时间）
     * @return 若存在返回 1，否则返回 null
     */
    Integer existsAfter(@Param("studentId") Long studentId,
                        @Param("courseId") Long courseId,
                        @Param("since") LocalDateTime since);
}

