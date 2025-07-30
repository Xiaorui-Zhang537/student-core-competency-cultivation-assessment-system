package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.service.GradeService;
import com.noncore.assessment.service.NotificationService;
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
    private final NotificationService notificationService;

    public GradeServiceImpl(GradeMapper gradeMapper, AssignmentMapper assignmentMapper, NotificationService notificationService) {
        this.gradeMapper = gradeMapper;
        this.assignmentMapper = assignmentMapper;
        this.notificationService = notificationService;
    }

    @Override
    public Grade createGrade(Grade grade) {
        try {
            logger.info("创建成绩记录，学生ID: {}, 作业ID: {}, 分数: {}", 
                       grade.getStudentId(), grade.getAssignmentId(), grade.getScore());
            
            // 设置创建时间和默认值
            grade.setCreatedAt(LocalDateTime.now());
            grade.setUpdatedAt(LocalDateTime.now());
            grade.setDeleted(false);
            
            // 设置默认状态
            if (grade.getStatus() == null) {
                grade.setStatus("draft");
            }
            
            // 计算百分比和等级
            calculateGradeMetrics(grade);
            
            int result = gradeMapper.insertGrade(grade);
            if (result > 0) {
                logger.info("成绩记录创建成功，ID: {}", grade.getId());
                return grade;
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建成绩记录失败");
            }
            
        } catch (Exception e) {
            logger.error("创建成绩记录失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建成绩记录失败: " + e.getMessage());
        }
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
        try {
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
            
        } catch (Exception e) {
            logger.error("更新成绩记录失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新成绩记录失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteGrade(Long gradeId) {
        try {
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
            
        } catch (Exception e) {
            logger.error("删除成绩记录失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除成绩记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<Grade> getGradesByStudent(Long studentId) {
        logger.info("获取学生成绩列表，学生ID: {}", studentId);
        return gradeMapper.selectByStudentId(studentId);
    }

    @Override
    public PageResult<Grade> getStudentGradesWithPagination(Long studentId, Integer page, Integer size) {
        logger.info("分页获取学生成绩，学生ID: {}, 页码: {}, 每页大小: {}", studentId, page, size);
        
        int offset = (page - 1) * size;
        List<Grade> grades = gradeMapper.selectByStudentIdWithPagination(studentId, offset, size);
        Integer total = gradeMapper.countByStudentId(studentId);
        
        return PageResult.of(grades, page, size, total.longValue(), (total + size - 1) / size);
    }

    @Override
    public List<Grade> getGradesByAssignment(Long assignmentId) {
        logger.info("获取作业成绩列表，作业ID: {}", assignmentId);
        return gradeMapper.selectByAssignmentId(assignmentId);
    }

    @Override
    public PageResult<Grade> getAssignmentGradesWithPagination(Long assignmentId, Integer page, Integer size) {
        logger.info("分页获取作业成绩，作业ID: {}, 页码: {}, 每页大小: {}", assignmentId, page, size);
        
        int offset = (page - 1) * size;
        List<Grade> grades = gradeMapper.selectByAssignmentIdWithPagination(assignmentId, offset, size);
        Integer total = gradeMapper.countByAssignmentId(assignmentId);
        
        return PageResult.of(grades, page, size, total.longValue(), (total + size - 1) / size);
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
    public Map<String, Object> getCourseGradeStatistics(Long courseId) {
        logger.info("获取课程成绩统计，课程ID: {}", courseId);
        return gradeMapper.getCourseGradeStats(courseId);
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
        BigDecimal highestScore = grades.get(0).getScore();
        BigDecimal lowestScore = grades.get(0).getScore();
        
        for (Grade grade : grades) {
            if (grade.getScore() != null) {
                totalScore = totalScore.add(grade.getScore());
                if (grade.getScore().compareTo(highestScore) > 0) {
                    highestScore = grade.getScore();
                }
                if (grade.getScore().compareTo(lowestScore) < 0) {
                    lowestScore = grade.getScore();
                }
            }
        }
        
        stats.put("totalGrades", grades.size());
        stats.put("averageScore", totalScore.divide(new BigDecimal(grades.size()), 2, RoundingMode.HALF_UP));
        stats.put("highestScore", highestScore);
        stats.put("lowestScore", lowestScore);
        
        return stats;
    }

    @Override
    public PageResult<Map<String, Object>> getPendingGrades(Long teacherId, Integer page, Integer size) {
        logger.info("获取教师待评分作业，教师ID: {}, 页码: {}, 每页大小: {}", teacherId, page, size);
        
        int offset = (page - 1) * size;
        List<Grade> pendingGrades = gradeMapper.selectPendingGrades(teacherId);
        
        // 转换为包含详细信息的Map
        List<Map<String, Object>> result = pendingGrades.stream()
            .skip(offset)
            .limit(size)
            .map(this::convertGradeToDetailMap)
            .collect(Collectors.toList());
        
        int total = pendingGrades.size();
        return PageResult.of(result, page, size, (long) total, (total + size - 1) / size);
    }

    @Override
    public boolean publishGrade(Long gradeId) {
        try {
            logger.info("发布成绩，ID: {}", gradeId);
            
            Grade grade = new Grade();
            grade.setId(gradeId);
            grade.setStatus("published");
            grade.setUpdatedAt(LocalDateTime.now());
            
            int result = gradeMapper.updateGrade(grade);
            return result > 0;
        } catch (Exception e) {
            logger.error("发布成绩失败", e);
            return false;
        }
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
        
        List<Grade> grades;
        if (assignmentId != null) {
            grades = gradeMapper.selectByAssignmentId(assignmentId);
        } else {
            grades = gradeMapper.selectByCourseId(courseId);
        }
        
        // 按分数排序并添加排名
        return grades.stream()
            .filter(grade -> grade.getScore() != null)
            .sorted((g1, g2) -> g2.getScore().compareTo(g1.getScore()))
            .map(this::convertGradeToRankingMap)
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStudentRanking(Long studentId, Long courseId) {
        logger.info("获取学生排名，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        Map<String, Object> ranking = new HashMap<>();
        
        // 获取课程所有成绩并排序
        List<Grade> allGrades = gradeMapper.selectByCourseId(courseId);
        List<Grade> sortedGrades = allGrades.stream()
            .filter(grade -> grade.getScore() != null)
            .sorted((g1, g2) -> g2.getScore().compareTo(g1.getScore()))
            .toList();
        
        // 找到学生排名
        for (int i = 0; i < sortedGrades.size(); i++) {
            if (sortedGrades.get(i).getStudentId().equals(studentId)) {
                ranking.put("rank", i + 1);
                ranking.put("totalStudents", sortedGrades.size());
                ranking.put("percentile", ((double) (sortedGrades.size() - i) / sortedGrades.size()) * 100);
                break;
            }
        }
        
        return ranking;
    }

    @Override
    public Map<String, Object> exportGrades(Long courseId, Long assignmentId, String format) {
        logger.info("导出成绩数据，课程ID: {}, 作业ID: {}, 格式: {}", courseId, assignmentId, format);
        
        Map<String, Object> result = new HashMap<>();
        
        // 模拟导出过程
        String fileName = generateExportFileName(courseId, assignmentId, format);
        String downloadUrl = "/api/files/download/" + fileName;
        
        result.put("fileName", fileName);
        result.put("downloadUrl", downloadUrl);
        result.put("format", format);
        result.put("exportTime", LocalDateTime.now());
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getGradeTrend(Long studentId, Long courseId, Integer days) {
        logger.info("获取成绩趋势，学生ID: {}, 课程ID: {}, 天数: {}", studentId, courseId, days);
        
        // 模拟趋势数据
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", now.minusDays(i).toLocalDate());
            point.put("averageScore", 75 + (Math.random() * 20)); // 模拟数据
            trend.add(point);
        }
        
        return trend;
    }

    @Override
    public boolean canModifyGrade(Long gradeId, Long userId) {
        logger.info("检查成绩修改权限，成绩ID: {}, 用户ID: {}", gradeId, userId);
        
        try {
            Grade grade = gradeMapper.selectGradeById(gradeId);
            if (grade == null) {
                return false;
            }
            
            // 检查是否是教师且是该作业的创建者
            Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
            return assignment != null && userId.equals(assignment.getTeacherId());
            
        } catch (Exception e) {
            logger.error("检查成绩修改权限失败", e);
            return false;
        }
    }

    @Override
    public boolean addGradeFeedback(Long gradeId, String feedback) {
        try {
            logger.info("添加成绩评语，成绩ID: {}", gradeId);
            
            Grade grade = new Grade();
            grade.setId(gradeId);
            grade.setFeedback(feedback);
            grade.setUpdatedAt(LocalDateTime.now());
            
            int result = gradeMapper.updateGrade(grade);
            return result > 0;
        } catch (Exception e) {
            logger.error("添加成绩评语失败", e);
            return false;
        }
    }

    @Override
    public boolean regrade(Long gradeId, BigDecimal newScore, String reason) {
        try {
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
            return result > 0;
        } catch (Exception e) {
            logger.error("重新评分失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getGradeHistory(Long gradeId) {
        logger.info("获取成绩历史记录，成绩ID: {}", gradeId);
        
        // 模拟历史记录
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("action", "创建");
        record.put("score", "85.0");
        record.put("operator", "张老师");
        record.put("timestamp", LocalDateTime.now().minusDays(1));
        history.add(record);
        
        return history;
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
     * 将成绩转换为排名Map
     */
    private Map<String, Object> convertGradeToRankingMap(Grade grade) {
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("studentId", grade.getStudentId());
        ranking.put("score", grade.getScore());
        ranking.put("percentage", grade.getPercentage());
        ranking.put("gradeLevel", grade.getGradeLevel());
        
        // 添加学生信息（简化实现）
        ranking.put("studentName", "学生姓名");
        
        return ranking;
    }

    /**
     * 生成导出文件名
     */
    private String generateExportFileName(Long courseId, Long assignmentId, String format) {
        String timestamp = LocalDateTime.now().toString().replace(":", "-");
        String prefix = assignmentId != null ? "assignment_" + assignmentId : "course_" + courseId;
        return prefix + "_grades_" + timestamp + "." + format.toLowerCase();
    }
} 