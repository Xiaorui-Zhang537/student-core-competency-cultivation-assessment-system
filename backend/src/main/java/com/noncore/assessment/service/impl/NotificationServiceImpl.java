package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.CacheService;
import com.noncore.assessment.service.NotificationService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final CacheService cacheService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.noncore.assessment.realtime.NotificationSseService sseService;
    private static final String UNREAD_COUNT_CACHE_PREFIX = "notification:unread_count:";


    public NotificationServiceImpl(NotificationMapper notificationMapper, UserMapper userMapper,
                                 CourseMapper courseMapper, AssignmentMapper assignmentMapper,
                                 GradeMapper gradeMapper, CacheService cacheService) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.cacheService = cacheService;
    }

    @Override
    public Notification sendNotification(Long recipientId, Long senderId, String title, String content, 
                                       String type, String category, String priority, 
                                       String relatedType, Long relatedId) {
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
            // SSE 推送新通知与统计刷新
            if (sseService != null && recipientId != null) {
                try {
                    sseService.sendToUser(recipientId, "new", notification);
                    sseService.sendToUser(recipientId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
            return notification;
        } else {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送通知失败");
        }
    }

    @Override
    public Map<String, Object> batchSendNotification(List<Long> recipientIds, Long senderId, String title, String content, 
                                                   String type, String category, String priority, 
                                                   String relatedType, Long relatedId) {
        logger.info("批量发送通知，接收者数量: {}, 发送者ID: {}", recipientIds.size(), senderId);

        if (recipientIds.isEmpty()) {
            return Map.of("successCount", 0, "failCount", 0, "errors", List.of("接收者列表为空"));
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        List<Notification> notifications = recipientIds.stream().map(recipientId -> {
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
            notification.setCreatedAt(now);
            notification.setUpdatedAt(now);
            notification.setDeleted(false);
            return notification;
        }).collect(Collectors.toList());

        int successCount = notificationMapper.batchInsertNotifications(notifications);
        
        // Invalidate cache for all affected users
        recipientIds.forEach(id -> cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + id));

        if (successCount > 0 && sseService != null) {
            for (Notification n : notifications) {
                try {
                    sseService.sendToUser(n.getRecipientId(), "new", n);
                    sseService.sendToUser(n.getRecipientId(), "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }

        return Map.of("successCount", successCount, "failCount", recipientIds.size() - successCount);
    }

    @Override
    public PageResult<Notification> getUserNotifications(Long userId, String type, Integer page, Integer size) {
        logger.info("获取用户通知列表，用户ID: {}, 类型: {}", userId, type);

        int offset = (page - 1) * size;
        List<Notification> notifications = notificationMapper.selectByRecipientWithPagination(userId, type, offset, size);
        int total = notificationMapper.countByRecipient(userId, type);

        return PageResult.of(notifications, page, size, (long) total, (total + size - 1) / size);
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
        logger.info("标记通知为已读，通知ID: {}, 用户ID: {}", notificationId, userId);

        Notification notification = notificationMapper.selectNotificationById(notificationId);
        if (notification == null || !userId.equals(notification.getRecipientId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限操作该通知");
        }

        int result = notificationMapper.markAsRead(notificationId);
        if (result > 0) {
            cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + userId);
            if (sseService != null) {
                try {
                    sseService.sendToUser(userId, "update", java.util.Map.of("id", notificationId, "isRead", true));
                    sseService.sendToUser(userId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }
        return result > 0;
    }

    @Override
    public Map<String, Object> batchMarkAsRead(List<Long> notificationIds, Long userId) {
        logger.info("批量标记通知为已读，通知数量: {}, 用户ID: {}", notificationIds.size(), userId);

        if (notificationIds.isEmpty()) {
            return Map.of("successCount", 0, "failCount", 0);
        }

        int successCount = notificationMapper.batchMarkAsRead(notificationIds, userId);
        if (successCount > 0) {
            cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + userId);
            if (sseService != null) {
                try {
                    sseService.sendToUser(userId, "update", java.util.Map.of("ids", notificationIds, "isRead", true));
                    sseService.sendToUser(userId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }

        return Map.of("successCount", successCount, "failCount", notificationIds.size() - successCount);
    }

    @Override
    public int markAllAsRead(Long userId) {
        logger.info("标记所有通知为已读，用户ID: {}", userId);
        int updated = notificationMapper.markAllAsReadByRecipient(userId);
        if (updated > 0) {
            cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + userId);
            if (sseService != null) {
                try {
                    sseService.sendToUser(userId, "update", java.util.Map.of("allRead", true));
                    sseService.sendToUser(userId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }
        return updated;
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        logger.info("删除通知，通知ID: {}, 用户ID: {}", notificationId, userId);

        // 检查权限
        Notification notification = notificationMapper.selectNotificationById(notificationId);
        if (notification == null || !userId.equals(notification.getRecipientId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "没有权限删除该通知");
        }

        int result = notificationMapper.deleteNotification(notificationId);
        if (result > 0) {
            cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + userId);
            if (sseService != null) {
                try {
                    sseService.sendToUser(userId, "delete", java.util.Map.of("id", notificationId));
                    sseService.sendToUser(userId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }
        return result > 0;
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
        
        // Simple cache implementation for unread count (ignoring type for now)
        if (type == null) {
            String cacheKey = UNREAD_COUNT_CACHE_PREFIX + userId;
            Optional<Integer> cachedCount = cacheService.get(cacheKey, Integer.class);
            if (cachedCount.isPresent()) {
                logger.debug("Cache hit for unread count for user {}", userId);
                return cachedCount.get();
            }
            Integer count = notificationMapper.countUnreadByRecipient(userId);
            cacheService.set(cacheKey, count, 300); // Cache for 5 minutes
            return count;
        }
        
        return notificationMapper.countUnreadByRecipientWithType(userId, type);
    }

    @Override
    public Notification getNotificationDetail(Long notificationId, Long userId) {
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
    }

    @Override
    @Transactional
    public Map<String, Object> sendSystemNotification(String title, String content, String type, 
                                                     String targetType, List<Long> targetIds, String role) {
        logger.info("发送系统通知，标题: {}, 目标类型: {}, 角色: {}", title, targetType, role);

        List<Long> recipientIds = switch (targetType) {
            case "all" ->
                // 从数据库获取所有用户ID
                    userMapper.selectAllUserIds();
            case "role" -> {
                if (role == null || role.isBlank()) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER, "角色不能为空");
                }
                yield userMapper.selectUserIdsByRole(role);
            }
            case "specific" -> targetIds;
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不支持的目标类型: " + targetType);
        };

        return batchSendNotification(recipientIds, null, title, content, type, "system", "normal", null, null);
    }

    @Override
    public Map<String, Object> sendAssignmentNotification(Long assignmentId, String type, String customMessage) {
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
    }

    @Override
    public Map<String, Object> sendCourseNotification(Long courseId, String type, String customMessage) {
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
    }

    @Override
    public Map<String, Object> sendGradeNotification(Long gradeId, String type, String customMessage) {
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
    }

    @Override
    public Map<String, Object> getNotificationStats(Long userId) {
        logger.info("获取通知统计信息，用户ID: {}", userId);

        Map<String, Object> stats = new HashMap<>();
        
        // 总通知数和未读数
        Integer total = notificationMapper.countByRecipient(userId, null);
        Integer unreadCount = getUnreadCount(userId, null); // Use cached method
        
        stats.put("total", total);
        stats.put("unread", unreadCount);
        stats.put("read", total - unreadCount);
        
        // 按类型统计（XML 返回多行 map：{type, count}）
        java.util.List<java.util.Map<String, Object>> rows = notificationMapper.countByRecipientGroupByType(userId);
        java.util.Map<String, Long> typeStats = new java.util.HashMap<>();
        if (rows != null) {
            for (java.util.Map<String, Object> row : rows) {
                Object t = row.get("type");
                Object c = row.get("count");
                if (t != null && c instanceof Number num) {
                    typeStats.put(String.valueOf(t), num.longValue());
                }
            }
        }
        stats.put("byType", typeStats);
        
        return stats;
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Transactional
    public Map<String, Object> cleanupExpiredNotifications(int days) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            int cleanedCount = notificationMapper.deleteExpiredNotifications(cutoffDate);
            result.put("cleanedCount", cleanedCount);
            logger.info("成功清理了 {} 条 {} 天前的过期通知。", cleanedCount, days);
        } catch (Exception e) {
            logger.error("清理过期通知失败", e);
            result.put("cleanedCount", -1);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public PageResult<Notification> getConversation(Long userId, Long peerId, Integer page, Integer size) {
        logger.info("获取会话：userId={}, peerId={}, page={}, size={}", userId, peerId, page, size);
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 20 : size;
        int offset = (p - 1) * s;
        List<Notification> items = notificationMapper.selectConversationBetween(userId, peerId, offset, s);
        Integer total = notificationMapper.countConversationBetween(userId, peerId);
        int totalPages = (int) Math.ceil((total == null ? 0 : total) / (double) s);
        return PageResult.of(items, p, s, total == null ? 0L : total.longValue(), totalPages);
    }

    @Override
    public int markConversationAsRead(Long userId, Long peerId) {
        logger.info("标记会话已读：userId={}, peerId={}", userId, peerId);
        // 简化处理：取我作为接收者的未读消息，逐条标记
        List<Notification> mine = notificationMapper.selectConversationBetween(userId, peerId, 0, Integer.MAX_VALUE);
        int count = 0;
        for (Notification n : mine) {
            if (Boolean.FALSE.equals(n.getIsRead()) && userId.equals(n.getRecipientId())) {
                count += notificationMapper.markAsRead(n.getId());
            }
        }
        if (count > 0) {
            cacheService.delete(UNREAD_COUNT_CACHE_PREFIX + userId);
            if (sseService != null) {
                try {
                    sseService.sendToUser(userId, "update", java.util.Map.of("peerId", peerId, "conversationRead", true));
                    sseService.sendToUser(userId, "stats", "refresh");
                } catch (Exception ignore) {}
            }
        }
        return count;
    }
} 