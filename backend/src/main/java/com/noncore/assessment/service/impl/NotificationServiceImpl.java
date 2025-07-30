package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final GradeMapper gradeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public NotificationServiceImpl(NotificationMapper notificationMapper, UserMapper userMapper,
                                 CourseMapper courseMapper, AssignmentMapper assignmentMapper,
                                 GradeMapper gradeMapper, RedisTemplate<String, Object> redisTemplate) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Notification sendNotification(Long recipientId, Long senderId, String title, String content, 
                                       String type, String category, String priority, 
                                       String relatedType, Long relatedId) {
        try {
            logger.info("发送通知，接收者ID: {}, 发送者ID: {}, 标题: {}", recipientId, senderId, title);

            Notification notification = new Notification();
            notification.setRecipientId(recipientId);
            notification.setSenderId(senderId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type != null ? type : "info");
            notification.setCategory(category != null ? category : "general");
            notification.setPriority(priority != null ? priority : "normal");
            notification.setRelatedType(relatedType);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUpdatedAt(LocalDateTime.now());
            notification.setDeleted(false);

            int result = notificationMapper.insertNotification(notification);
            if (result > 0) {
                logger.info("通知发送成功，通知ID: {}", notification.getId());
                return notification;
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送通知失败");
            }

        } catch (Exception e) {
            logger.error("发送通知失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> batchSendNotification(List<Long> recipientIds, Long senderId, String title, String content, 
                                                   String type, String category, String priority, 
                                                   String relatedType, Long relatedId) {
        logger.info("批量发送通知，接收者数量: {}, 发送者ID: {}", recipientIds.size(), senderId);

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long recipientId : recipientIds) {
            try {
                sendNotification(recipientId, senderId, title, content, type, category, priority, relatedType, relatedId);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("发送给用户ID " + recipientId + " 失败: " + e.getMessage());
                logger.error("批量发送通知失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    @Override
    public PageResult<Notification> getUserNotifications(Long userId, String type, Integer page, Integer size) {
        try {
            logger.info("获取用户通知列表，用户ID: {}, 类型: {}", userId, type);

            int offset = (page - 1) * size;
            List<Notification> notifications = notificationMapper.selectByRecipientWithPagination(userId, type, offset, size);
            int total = notificationMapper.countByRecipient(userId, type);

            return PageResult.of(notifications, page, size, (long) total, (total + size - 1) / size);

        } catch (Exception e) {
            logger.error("获取用户通知列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取用户通知列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<Notification> getAllUserNotifications(Long userId, String type) {
        logger.info("获取用户所有通知，用户ID: {}, 类型: {}", userId, type);
        
        if (type != null) {
            return notificationMapper.selectByRecipientAndType(userId, type);
        } else {
            return notificationMapper.selectByRecipientId(userId);
        }
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        try {
            logger.info("标记通知为已读，通知ID: {}, 用户ID: {}", notificationId, userId);

            // 检查权限
            Notification notification = notificationMapper.selectNotificationById(notificationId);
            if (notification == null || !userId.equals(notification.getRecipientId())) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限操作该通知");
            }

            int result = notificationMapper.markAsRead(notificationId);
            return result > 0;

        } catch (Exception e) {
            logger.error("标记通知为已读失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "标记通知为已读失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> batchMarkAsRead(List<Long> notificationIds, Long userId) {
        logger.info("批量标记通知为已读，通知数量: {}, 用户ID: {}", notificationIds.size(), userId);

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long notificationId : notificationIds) {
            try {
                if (markAsRead(notificationId, userId)) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("标记通知ID " + notificationId + " 失败");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("标记通知ID " + notificationId + " 失败: " + e.getMessage());
                logger.error("批量标记通知为已读失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    @Override
    public int markAllAsRead(Long userId) {
        logger.info("标记所有通知为已读，用户ID: {}", userId);
        return notificationMapper.markAllAsReadByRecipient(userId);
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            logger.info("删除通知，通知ID: {}, 用户ID: {}", notificationId, userId);

            // 检查权限
            Notification notification = notificationMapper.selectNotificationById(notificationId);
            if (notification == null || !userId.equals(notification.getRecipientId())) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限删除该通知");
            }

            int result = notificationMapper.deleteNotification(notificationId);
            return result > 0;

        } catch (Exception e) {
            logger.error("删除通知失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除通知失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> batchDeleteNotifications(List<Long> notificationIds, Long userId) {
        logger.info("批量删除通知，通知数量: {}, 用户ID: {}", notificationIds.size(), userId);

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long notificationId : notificationIds) {
            try {
                if (deleteNotification(notificationId, userId)) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("删除通知ID " + notificationId + " 失败");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("删除通知ID " + notificationId + " 失败: " + e.getMessage());
                logger.error("批量删除通知失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    @Override
    public Integer getUnreadCount(Long userId, String type) {
        logger.info("获取未读通知数量，用户ID: {}, 类型: {}", userId, type);
        return notificationMapper.countUnreadByRecipient(userId);
    }

    @Override
    public Notification getNotificationDetail(Long notificationId, Long userId) {
        try {
            logger.info("获取通知详情，通知ID: {}, 用户ID: {}", notificationId, userId);

            Notification notification = notificationMapper.selectNotificationById(notificationId);
            if (notification == null || !userId.equals(notification.getRecipientId())) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限查看该通知");
            }

            // 如果未读，自动标记为已读
            if (!notification.getIsRead()) {
                markAsRead(notificationId, userId);
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
            }

            return notification;

        } catch (Exception e) {
            logger.error("获取通知详情失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取通知详情失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendSystemNotification(String title, String content, String type, 
                                                     String targetType, List<Long> targetIds, String role) {
        logger.info("发送系统通知，标题: {}, 目标类型: {}, 角色: {}", title, targetType, role);

        List<Long> recipientIds = new ArrayList<>();

        switch (targetType) {
            case "all":
                // 获取所有用户ID（简化实现）
                recipientIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
                break;
            case "role":
                // 根据角色获取用户ID（简化实现）
                if ("teacher".equals(role)) {
                    recipientIds = Arrays.asList(1L, 2L);
                } else if ("student".equals(role)) {
                    recipientIds = Arrays.asList(3L, 4L, 5L);
                }
                break;
            case "specific":
                recipientIds = targetIds;
                break;
            default:
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的目标类型: " + targetType);
        }

        return batchSendNotification(recipientIds, null, title, content, type, "system", "normal", null, null);
    }

    @Override
    public Map<String, Object> sendAssignmentNotification(Long assignmentId, String type, String customMessage) {
        try {
            logger.info("发送作业相关通知，作业ID: {}, 类型: {}", assignmentId, type);

            Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
            if (assignment == null) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND, "作业不存在");
            }

            Course course = courseMapper.selectCourseById(assignment.getCourseId());
            if (course == null) {
                throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程不存在");
            }

            // 获取课程学生列表
            List<User> students = userMapper.selectStudentsByCourseId(assignment.getCourseId());
            List<Long> studentIds = students.stream().map(User::getId).toList();

            String title;
            String content = switch (type) {
                case "assignment_created" -> {
                    title = "新作业发布";
                    yield String.format("课程《%s》发布了新作业：%s", course.getTitle(), assignment.getTitle());
                }
                case "assignment_updated" -> {
                    title = "作业更新";
                    yield String.format("作业《%s》已更新，请查看最新要求", assignment.getTitle());
                }
                case "deadline_reminder" -> {
                    title = "作业截止提醒";
                    yield String.format("作业《%s》即将截止，请尽快提交", assignment.getTitle());
                }
                default -> {
                    title = "作业通知";
                    yield customMessage != null ? customMessage : "您有新的作业通知";
                }
            };

            return batchSendNotification(studentIds, course.getTeacherId(), title, content,
                                       "assignment", "academic", "normal", "assignment", assignmentId);

        } catch (Exception e) {
            logger.error("发送作业相关通知失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送作业相关通知失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendCourseNotification(Long courseId, String type, String customMessage) {
        try {
            logger.info("发送课程相关通知，课程ID: {}, 类型: {}", courseId, type);

            Course course = courseMapper.selectCourseById(courseId);
            if (course == null) {
                throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程不存在");
            }

            // 获取课程学生列表
            List<User> students = userMapper.selectStudentsByCourseId(courseId);
            List<Long> studentIds = students.stream().map(User::getId).toList();

            String title;
            String content = switch (type) {
                case "course_updated" -> {
                    title = "课程更新";
                    yield String.format("课程《%s》已更新，请查看最新内容", course.getTitle());
                }
                case "new_lesson" -> {
                    title = "新课时发布";
                    yield String.format("课程《%s》发布了新的课时，快来学习吧！", course.getTitle());
                }
                case "course_completed" -> {
                    title = "课程完成";
                    yield String.format("恭喜您完成了课程《%s》的学习！", course.getTitle());
                }
                default -> {
                    title = "课程通知";
                    yield customMessage != null ? customMessage : "您有新的课程通知";
                }
            };

            return batchSendNotification(studentIds, course.getTeacherId(), title, content,
                                       "course", "academic", "normal", "course", courseId);

        } catch (Exception e) {
            logger.error("发送课程相关通知失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送课程相关通知失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendGradeNotification(Long gradeId, String type, String customMessage) {
        try {
            logger.info("发送成绩相关通知，成绩ID: {}, 类型: {}", gradeId, type);

            Grade grade = gradeMapper.selectGradeById(gradeId);
            if (grade == null) {
                throw new BusinessException(ErrorCode.GRADE_NOT_FOUND, "成绩不存在");
            }

            Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
            if (assignment == null) {
                throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND, "作业不存在");
            }

            String title;
            String content = switch (type) {
                case "grade_published" -> {
                    title = "成绩发布";
                    yield String.format("您的作业《%s》已评分，成绩为：%.1f分", assignment.getTitle(), grade.getScore());
                }
                case "feedback_available" -> {
                    title = "评价反馈";
                    yield String.format("您的作业《%s》收到了详细的评价反馈，请查看", assignment.getTitle());
                }
                default -> {
                    title = "成绩通知";
                    yield customMessage != null ? customMessage : "您有新的成绩通知";
                }
            };

            // 发送通知给学生
            sendNotification(grade.getStudentId(), assignment.getTeacherId(), title, content, 
                           "grade", "academic", "normal", "grade", gradeId);

            return Map.of("successCount", 1, "failCount", 0, "errors", new ArrayList<>());

        } catch (Exception e) {
            logger.error("发送成绩相关通知失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送成绩相关通知失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getNotificationStats(Long userId) {
        logger.info("获取通知统计信息，用户ID: {}", userId);

        Map<String, Object> stats = new HashMap<>();
        
        // 总通知数
        List<Notification> allNotifications = notificationMapper.selectByRecipientId(userId);
        stats.put("total", allNotifications.size());
        
        // 未读通知数
        Integer unreadCount = notificationMapper.countUnreadByRecipient(userId);
        stats.put("unread", unreadCount);
        
        // 已读通知数
        stats.put("read", allNotifications.size() - unreadCount);
        
        // 按类型统计
        Map<String, Integer> typeStats = new HashMap<>();
        for (Notification notification : allNotifications) {
            String type = notification.getType();
            typeStats.put(type, typeStats.getOrDefault(type, 0) + 1);
        }
        stats.put("byType", typeStats);
        
        return stats;
    }

    @Override
    public Map<String, Object> cleanupExpiredNotifications(int days) {
        logger.info("清理过期通知，保留天数: {}", days);

        Map<String, Object> result = new HashMap<>();
        // 简化实现，返回模拟数据
        result.put("cleanedCount", 0);
        result.put("totalCleaned", 0);
        
        return result;
    }
} 