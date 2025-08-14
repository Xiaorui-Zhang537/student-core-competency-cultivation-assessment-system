package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.service.GradeService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.noncore.assessment.entity.Grade.getString;
import com.noncore.assessment.dto.response.GradeStatsResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 成绩管理服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class GradeServiceImpl implements GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeServiceImpl.class);

    private final GradeMapper gradeMapper;
    private final AssignmentMapper assignmentMapper;

    public GradeServiceImpl(GradeMapper gradeMapper, AssignmentMapper assignmentMapper) {
        this.gradeMapper = gradeMapper;
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public Grade createGrade(Grade grade) {
        logger.info("创建成绩记录，学生ID: {}, 作业ID: {}, 分数: {}",
                    grade.getStudentId(), grade.getAssignmentId(), grade.getScore());
        // 基础校验与补全：作业存在、maxScore 兜底
        if (grade.getAssignmentId() == null || grade.getStudentId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "assignmentId 与 studentId 不能为空");
        }

        Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        // 兜底 maxScore
        if (grade.getMaxScore() == null && assignment.getMaxScore() != null) {
            grade.setMaxScore(assignment.getMaxScore());
        }

        // 时间与删除标记
        LocalDateTime now = LocalDateTime.now();
        if (grade.getCreatedAt() == null) {
            grade.setCreatedAt(now);
        }
        grade.setUpdatedAt(now);
        if (grade.getDeleted() == null) {
            grade.setDeleted(false);
        }

        // 默认状态
        if (grade.getStatus() == null || grade.getStatus().isBlank()) {
            grade.setStatus("draft");
        }

        // upsert：如已存在该学生在该作业的成绩，则走更新
        Grade existing = gradeMapper.selectByStudentAndAssignment(grade.getStudentId(), grade.getAssignmentId());
        calculateGradeMetrics(grade);

        if (existing != null && existing.getId() != null) {
            grade.setId(existing.getId());
            int u = gradeMapper.updateGrade(grade);
            if (u > 0) {
                logger.info("成绩记录更新成功（upsert），ID: {}", existing.getId());
                return gradeMapper.selectGradeById(existing.getId());
            }
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新成绩记录失败");
        }

        int result = gradeMapper.insertGrade(grade);
        if (result > 0) {
            logger.info("成绩记录创建成功，ID: {}", grade.getId());
            return grade;
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建成绩记录失败");
    }

    @Override
    public Grade getGradeById(Long gradeId) {
        logger.info("获取成绩详情，ID: {}", gradeId);
        Grade grade = gradeMapper.selectGradeById(gradeId);
        
        if (grade == null) {
            throw new BusinessException(ErrorCode.GRADE_NOT_FOUND);
        }
        
        return grade;
    }

    @Override
    public Grade updateGrade(Long gradeId, Grade grade) {
        logger.info("更新成绩记录，ID: {}", gradeId);
        
        // 检查成绩是否存在
        Grade existingGrade = gradeMapper.selectGradeById(gradeId);
        if (existingGrade == null) {
            throw new BusinessException(ErrorCode.GRADE_NOT_FOUND);
        }
        
        // 更新字段
        grade.setId(gradeId);
        grade.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算百分比和等级
        calculateGradeMetrics(grade);
        
        int result = gradeMapper.updateGrade(grade);
        if (result > 0) {
            logger.info("成绩记录更新成功，ID: {}", gradeId);
            return gradeMapper.selectGradeById(gradeId);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新成绩记录失败");
        }
    }

    @Override
    public void deleteGrade(Long gradeId) {
        logger.info("删除成绩记录，ID: {}", gradeId);
        
        // 检查成绩是否存在
        Grade grade = gradeMapper.selectGradeById(gradeId);
        if (grade == null) {
            throw new BusinessException(ErrorCode.GRADE_NOT_FOUND);
        }
        
        // 软删除
        int result = gradeMapper.deleteGrade(gradeId);
        if (result > 0) {
            logger.info("成绩记录删除成功，ID: {}", gradeId);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除成绩记录失败");
        }
    }

    @Override
    public List<Grade> getGradesByStudent(Long studentId) {
        logger.info("获取学生成绩列表，学生ID: {}", studentId);
        return gradeMapper.selectByStudentId(studentId);
    }

    @Override
    public PageResult<Grade> getStudentGradesWithPagination(Long studentId, Integer page, Integer size, Long courseId) {
        logger.info("分页获取学生成绩，学生ID: {}, 页码: {}, 每页大小: {}, 课程ID: {}", studentId, page, size, courseId);
        PageHelper.startPage(page, size);
        List<Grade> grades = gradeMapper.selectByStudentIdFiltered(studentId, courseId);
        PageInfo<Grade> pageInfo = new PageInfo<>(grades);
        return PageResult.of(pageInfo.getList(), page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public List<Grade> getGradesByAssignment(Long assignmentId) {
        logger.info("获取作业成绩列表，作业ID: {}", assignmentId);
        return gradeMapper.selectByAssignmentId(assignmentId);
    }

    @Override
    public PageResult<Grade> getAssignmentGradesWithPagination(Long assignmentId, Integer page, Integer size) {
        logger.info("分页获取作业成绩，作业ID: {}, 页码: {}, 每页大小: {}", assignmentId, page, size);
        PageHelper.startPage(page, size);
        List<Grade> grades = gradeMapper.selectByAssignmentId(assignmentId);
        PageInfo<Grade> pageInfo = new PageInfo<>(grades);
        return PageResult.of(pageInfo.getList(), page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public Grade getStudentAssignmentGrade(Long studentId, Long assignmentId) {
        logger.info("获取学生作业成绩，学生ID: {}, 作业ID: {}", studentId, assignmentId);
        return gradeMapper.selectByStudentAndAssignment(studentId, assignmentId);
    }

    @Override
    public Map<String, Object> batchGrading(List<Grade> grades) {
        logger.info("批量评分，成绩数量: {}", grades.size());
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Grade grade : grades) {
            try {
                if (grade.getId() != null) {
                    // 更新现有成绩
                    updateGrade(grade.getId(), grade);
                } else {
                    // 创建新成绩
                    createGrade(grade);
                }
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("评分失败: " + e.getMessage());
                logger.error("批量评分失败", e);
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public BigDecimal calculateStudentAverageScore(Long studentId) {
        logger.info("计算学生平均分，学生ID: {}", studentId);
        BigDecimal average = gradeMapper.calculateAverageScore(studentId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateStudentCourseAverageScore(Long studentId, Long courseId) {
        logger.info("计算学生课程平均分，学生ID: {}, 课程ID: {}", studentId, courseId);
        BigDecimal average = gradeMapper.calculateCourseAverageScore(studentId, courseId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateAssignmentAverageScore(Long assignmentId) {
        logger.info("计算作业平均分，作业ID: {}", assignmentId);
        BigDecimal average = gradeMapper.calculateAssignmentAverageScore(assignmentId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> getGradeDistribution(Long assignmentId) {
        logger.info("获取作业成绩分布，作业ID: {}", assignmentId);
        return gradeMapper.getGradeDistribution(assignmentId);
    }

    @Override
    public GradeStatsResponse getCourseGradeStatistics(Long courseId) {
        logger.info("获取课程成绩统计，课程ID: {}", courseId);

        Map<String, Object> stats = gradeMapper.getCourseGradeStats(courseId);
        if (stats == null || stats.isEmpty()) {
            return new GradeStatsResponse();
        }

        return GradeStatsResponse.builder()
                .averageScore((BigDecimal) stats.get("averageScore"))
                .maxScore((BigDecimal) stats.get("maxScore"))
                .minScore((BigDecimal) stats.get("minScore"))
                .submissionCount(((Number) stats.get("submissionCount")).intValue())
                .gradeDistribution(safeCastGradeDistribution(stats.get("gradeDistribution")))
                .build();
    }

    @Override
    public Map<String, Object> getStudentGradeStatistics(Long studentId) {
        logger.info("获取学生成绩统计，学生ID: {}", studentId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 获取学生所有成绩
        List<Grade> grades = gradeMapper.selectByStudentId(studentId);
        
        if (grades.isEmpty()) {
            stats.put("totalGrades", 0);
            stats.put("averageScore", BigDecimal.ZERO);
            stats.put("highestScore", BigDecimal.ZERO);
            stats.put("lowestScore", BigDecimal.ZERO);
            return stats;
        }
        
        // 计算统计数据
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal highestScore = null;
        BigDecimal lowestScore = null;
        int gradedCount = 0;

        for (Grade grade : grades) {
            if (grade.getScore() != null) {
                BigDecimal currentScore = grade.getScore();
                totalScore = totalScore.add(currentScore);
                gradedCount++;

                if (highestScore == null || currentScore.compareTo(highestScore) > 0) {
                    highestScore = currentScore;
                }
                if (lowestScore == null || currentScore.compareTo(lowestScore) < 0) {
                    lowestScore = currentScore;
                }
            }
        }

        stats.put("totalGrades", grades.size());
        stats.put("gradedCount", gradedCount);
        if (gradedCount > 0) {
            stats.put("averageScore", totalScore.divide(new BigDecimal(gradedCount), 2, RoundingMode.HALF_UP));
        } else {
            stats.put("averageScore", BigDecimal.ZERO);
        }
        stats.put("highestScore", highestScore != null ? highestScore : BigDecimal.ZERO);
        stats.put("lowestScore", lowestScore != null ? lowestScore : BigDecimal.ZERO);
        
        return stats;
    }

    @Override
    public PageResult<Map<String, Object>> getPendingGrades(Long teacherId, Integer page, Integer size) {
        logger.info("获取教师待评分作业，教师ID: {}, 页码: {}, 每页大小: {}", teacherId, page, size);

        PageHelper.startPage(page, size);
        List<Grade> pendingGrades = gradeMapper.selectPendingGradesWithPagination(teacherId);
        PageInfo<Grade> pageInfo = new PageInfo<>(pendingGrades);

        // 转换为包含详细信息的Map
        List<Map<String, Object>> result = pageInfo.getList().stream()
                .map(this::convertGradeToDetailMap)
                .collect(Collectors.toList());

        return PageResult.of(result, page, size, pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public boolean publishGrade(Long gradeId) {
        logger.info("发布成绩，ID: {}", gradeId);
        
        Grade grade = new Grade();
        grade.setId(gradeId);
        grade.setStatus("published");
        grade.setUpdatedAt(LocalDateTime.now());
        
        int result = gradeMapper.updateGrade(grade);
        if (result > 0) {
            return true;
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "发布成绩失败");
    }

    @Override
    public Map<String, Object> batchPublishGrades(List<Long> gradeIds) {
        logger.info("批量发布成绩，成绩数量: {}", gradeIds.size());
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long gradeId : gradeIds) {
            try {
                if (publishGrade(gradeId)) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("发布成绩ID " + gradeId + " 失败");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("发布成绩ID " + gradeId + " 失败: " + e.getMessage());
                logger.error("批量发布成绩失败", e);
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getGradeRanking(Long courseId, Long assignmentId) {
        logger.info("获取成绩排名，课程ID: {}, 作业ID: {}", courseId, assignmentId);
        
        if (assignmentId != null) {
            return gradeMapper.selectGradeRankingForAssignment(assignmentId);
        } else if (courseId != null) {
            return gradeMapper.selectGradeRankingForCourse(courseId);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getStudentRanking(Long studentId, Long courseId) {
        logger.info("获取学生排名，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        List<Map<String, Object>> rankings = gradeMapper.selectGradeRankingForCourse(courseId);
        
        Optional<Map<String, Object>> studentRankOptional = rankings.stream()
            .filter(rank -> studentId.equals(rank.get("student_id")))
            .findFirst();
            
        if (studentRankOptional.isPresent()) {
            Map<String, Object> studentRank = new HashMap<>(studentRankOptional.get());
            long totalStudents = rankings.size();
            long rank = (long) studentRank.get("rank");
            
            double percentile = 0.0;
            if (totalStudents > 0) {
                percentile = ((double) (totalStudents - rank + 1) / totalStudents) * 100.0;
            }
            
            studentRank.put("totalStudents", totalStudents);
            studentRank.put("percentile", new BigDecimal(percentile).setScale(2, RoundingMode.HALF_UP));
            return studentRank;
        }
        
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> exportGrades(Long courseId, Long assignmentId, String format) {
        logger.info("Exporting grades for courseId: {}, assignmentId: {}, format: {}", courseId, assignmentId, format);
        
        List<Map<String, Object>> gradesData = gradeMapper.selectGradesForExport(courseId, assignmentId);
        
        // In a real application, you would convert this data to the specified format (e.g., CSV, PDF)
        // For this simplified version, we'll just return the data.
        Map<String, Object> result = new HashMap<>();
        result.put("format", format);
        result.put("data", gradesData);
        result.put("message", "Grades exported successfully.");
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getGradeTrend(Long studentId, Long courseId, Integer days) {
        logger.info("获取成绩趋势，学生ID: {}, 课程ID: {}, 周期天数: {}", studentId, courseId, days);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        return gradeMapper.selectGradeTrendByStudentAndCourse(studentId, courseId, startDate, endDate);
    }

    @Override
    public boolean canModifyGrade(Long gradeId, Long userId) {
        logger.info("检查成绩修改权限，成绩ID: {}, 用户ID: {}", gradeId, userId);
        
        Grade grade = gradeMapper.selectGradeById(gradeId);
        if (grade == null) {
            return true;
        }
        
        // 检查是否是教师且是该作业的创建者
        Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
        return assignment == null || !userId.equals(assignment.getTeacherId());
    }

    @Override
    public void addGradeFeedback(Long gradeId, String feedback) {
        logger.info("添加成绩评语，成绩ID: {}", gradeId);
        
        Grade grade = new Grade();
        grade.setId(gradeId);
        grade.setFeedback(feedback);
        grade.setUpdatedAt(LocalDateTime.now());
        
        int result = gradeMapper.updateGrade(grade);
        if(result > 0) {
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "添加评语失败");
    }

    @Override
    public void regrade(Long gradeId, BigDecimal newScore, String reason) {
        logger.info("重新评分，成绩ID: {}, 新分数: {}, 原因: {}", gradeId, newScore, reason);
        
        Grade grade = gradeMapper.selectGradeById(gradeId);
        if (grade == null) {
            throw new BusinessException(ErrorCode.GRADE_NOT_FOUND);
        }
        
        // 保存重评记录（这里简化实现）
        grade.setScore(newScore);
        grade.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算百分比和等级
        calculateGradeMetrics(grade);
        
        int result = gradeMapper.updateGrade(grade);
        if(result > 0) {
            return;
        }
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "重新评分失败");
    }

    @Override
    public List<Map<String, Object>> getGradeHistory(Long gradeId) {
        logger.info("获取成绩历史记录，成绩ID: {}", gradeId);
        // This is a simplified implementation as we don't have a separate audit/history table.
        // It returns the current state as a single history entry.
        return gradeMapper.selectGradeHistoryByGradeId(gradeId);
    }

    @Override
    public BigDecimal calculateGPA(Long studentId, Long courseId) {
        logger.info("计算GPA，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        List<Grade> grades;
        if (courseId != null) {
            grades = gradeMapper.selectByStudentAndCourse(studentId, courseId);
        } else {
            grades = gradeMapper.selectByStudentId(studentId);
        }
        
        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 计算GPA（4.0制）
        BigDecimal totalPoints = BigDecimal.ZERO;
        int count = 0;
        
        for (Grade grade : grades) {
            if (grade.getScore() != null) {
                BigDecimal gpaPoint = convertScoreToGPA(grade.getScore());
                totalPoints = totalPoints.add(gpaPoint);
                count++;
            }
        }
        
        return count > 0 ? totalPoints.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    // 私有辅助方法

    /**
     * 计算成绩相关指标
     */
    private void calculateGradeMetrics(Grade grade) {
        if (grade.getScore() != null && grade.getMaxScore() != null && 
            grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
            
            // 计算百分比
            BigDecimal percentage = grade.getScore()
                .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            grade.setPercentage(percentage);
            
            // 计算等级
            grade.setGradeLevel(calculateGradeLevel(percentage));
        }
    }

    /**
     * 根据百分比计算等级
     */
    private String calculateGradeLevel(BigDecimal percentage) {
        return getString(percentage);
    }

    /**
     * 将分数转换为GPA点数
     */
    private BigDecimal convertScoreToGPA(BigDecimal score) {
        // 简化的分数到GPA转换（假设满分100分）
        if (score.compareTo(new BigDecimal("90")) >= 0) return new BigDecimal("4.0");
        if (score.compareTo(new BigDecimal("80")) >= 0) return new BigDecimal("3.0");
        if (score.compareTo(new BigDecimal("70")) >= 0) return new BigDecimal("2.0");
        if (score.compareTo(new BigDecimal("60")) >= 0) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }

    /**
     * 将成绩转换为详细信息Map
     */
    private Map<String, Object> convertGradeToDetailMap(Grade grade) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("gradeId", grade.getId());
        detail.put("studentId", grade.getStudentId());
        detail.put("assignmentId", grade.getAssignmentId());
        detail.put("score", grade.getScore());
        detail.put("status", grade.getStatus());
        detail.put("createdAt", grade.getCreatedAt());
        
        // 添加学生和作业信息（简化实现）
        detail.put("studentName", "学生姓名");
        detail.put("assignmentTitle", "作业标题");
        
        return detail;
    }

    /**
     * Safely casts an object to a list of maps, returning an empty list if the cast is not possible.
     * This method encapsulates the unavoidable unchecked cast.
     *
     * @param obj The object to cast.
     * @return A list of maps, or an empty list.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> safeCastGradeDistribution(Object obj) {
        if (obj instanceof List) {
            // We assume the list contains Map<String, Object> as returned by the mapper.
            // A more robust implementation would iterate and check each element, but this is a reasonable trade-off.
            return (List<Map<String, Object>>) obj;
        }
        return Collections.emptyList();
    }

} 