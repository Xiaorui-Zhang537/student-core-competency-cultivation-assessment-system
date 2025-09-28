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
    private final com.noncore.assessment.mapper.ChatMapper chatMapper;
    private final com.noncore.assessment.mapper.ChatAttachmentMapper chatAttachmentMapper;
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
                                 GradeMapper gradeMapper, CacheService cacheService,
                                 com.noncore.assessment.mapper.ChatAttachmentMapper chatAttachmentMapper,
                                 com.noncore.assessment.mapper.ChatMapper chatMapper) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.cacheService = cacheService;
        this.chatAttachmentMapper = chatAttachmentMapper;
        this.chatMapper = chatMapper;
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
            case "assignment_returned" -> {
                title = "作业被打回";
                String untilStr = null;
                try {
                    java.time.LocalDateTime until = grade.getResubmitUntil();
                    if (until != null) {
                        untilStr = until.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }
                } catch (Exception ignore) {}
                if (untilStr != null) {
                    yield String.format("您的作业《%s》已被打回，重交截止：%s。%s", assignment.getTitle(), untilStr, (customMessage != null ? customMessage : ""));
                } else {
                    yield String.format("您的作业《%s》已被打回，请根据教师要求修改后重新提交。%s", assignment.getTitle(), (customMessage != null ? customMessage : ""));
                }
            }
            default -> {
                title = "成绩通知";
                yield customMessage != null ? customMessage : "您有新的成绩通知";
            }
        };

        // 发送通知给学生：
        // - assignment_returned 场景：通知类型改为 assignment，便于前端直接跳到作业提交页
        // - 其他成绩相关：仍用 grade 类型
        if ("assignment_returned".equals(type)) {
            sendNotification(grade.getStudentId(), assignment.getTeacherId(), title, content,
                    "assignment", "academic", "normal", "assignment", assignment.getId());
        } else {
            sendNotification(grade.getStudentId(), assignment.getTeacherId(), title, content,
                    "grade", "academic", "normal", "assignment", assignment.getId());
        }

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
    public PageResult<Notification> getConversation(Long userId, Long peerId, Integer page, Integer size, Long courseId) {
        logger.info("获取会话：userId={}, peerId={}, page={}, size={}, courseId={}", userId, peerId, page, size, courseId);
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 20 : size;
        int offset = (p - 1) * s;
        List<Notification> items = notificationMapper.selectConversationBetween(userId, peerId, offset, s, courseId);
        // 附件回填（批量查询）
        try {
            java.util.List<Long> ids = items.stream().map(Notification::getId).toList();
            if (!ids.isEmpty()) {
                java.util.List<com.noncore.assessment.entity.ChatMessageAttachment> atts =
                        chatAttachmentMapper.selectByNotificationIds(ids);
                java.util.Map<Long, java.util.List<Long>> map = new java.util.HashMap<>();
                for (var a : atts) {
                    map.computeIfAbsent(a.getNotificationId(), k -> new java.util.ArrayList<>()).add(a.getFileId());
                }
                for (var n : items) {
                    n.setAttachmentFileIds(map.getOrDefault(n.getId(), java.util.Collections.emptyList()));
                }
            }
        } catch (Exception ignore) {}
        Integer total = notificationMapper.countConversationBetween(userId, peerId, courseId);
        int totalPages = (int) Math.ceil((total == null ? 0 : total) / (double) s);
        return PageResult.of(items, p, s, total == null ? 0L : total.longValue(), totalPages);
    }

    @Override
    public int markConversationAsRead(Long userId, Long peerId, Long courseId) {
        logger.info("标记会话已读：userId={}, peerId={}, courseId={}", userId, peerId, courseId);
        // 简化处理：取我作为接收者的未读消息，逐条标记
        List<Notification> mine = notificationMapper.selectConversationBetween(userId, peerId, 0, Integer.MAX_VALUE, courseId);
        int count = 0;
        for (Notification n : mine) {
            if (Boolean.FALSE.equals(n.getIsRead()) && userId.equals(n.getRecipientId())) {
                count += notificationMapper.markAsRead(n.getId());
            }
        }
        if (count > 0) {
            // 重算该会话在成员表的未读（若能定位 conversationId）
            try {
                Long cId = chatMapper.findConversationId(Math.min(userId, peerId), Math.max(userId, peerId), (courseId == null ? 0L : courseId));
                if (cId != null) {
                    chatMapper.recalcUnread(userId, cId);
                    chatMapper.resetUnread(cId, userId);
                }
            } catch (Exception ignore) {}
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

    @Override
    public Notification sendMessage(Long senderId, Long recipientId, String content, String relatedType, Long relatedId,
                                    java.util.List<Long> attachmentFileIds) {
        if (senderId == null || recipientId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "发送者或接收者不能为空");
        }
        if (Objects.equals(senderId, recipientId)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不能给自己发送消息");
        }
        // 若在课程上下文中，校验双方均属于该课程（教师或学生）
        if ("course".equalsIgnoreCase(relatedType) && relatedId != null) {
            Course c = courseMapper.selectCourseById(relatedId);
            if (c == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND, "课程不存在");
            boolean senderOk = Objects.equals(c.getTeacherId(), senderId) || userMapper.isStudentInCourse(senderId, relatedId) > 0;
            boolean recipientOk = Objects.equals(c.getTeacherId(), recipientId) || userMapper.isStudentInCourse(recipientId, relatedId) > 0;
            if (!senderOk || !recipientOk) throw new BusinessException(ErrorCode.PERMISSION_DENIED, "非课程成员无法发送消息");
        }
        // 附带基础元数据，便于前端显示用户名（避免仅凭最近消息反推）
        com.noncore.assessment.entity.User sender = userMapper.selectUserById(senderId);
        String meta = null;
        try {
            if (sender != null) {
                meta = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                    java.util.Map.of(
                        "username", sender.getUsername(),
                        "senderName", (sender.getNickname() != null && !sender.getNickname().isBlank()) ? sender.getNickname() : (sender.getFirstName() != null ? sender.getFirstName() : sender.getUsername())
                    )
                );
            }
        } catch (Exception ignore) {}
        Notification n = sendNotification(recipientId, senderId, "", content, "message", "chat", "low", relatedType, relatedId);
        if (meta != null) {
            n.setData(meta);
        }
        // 附件入库（可选）
        if (attachmentFileIds != null && !attachmentFileIds.isEmpty()) {
            try {
                chatAttachmentMapper.batchInsert(n.getId(), attachmentFileIds, senderId);
            } catch (Exception e) {
                logger.warn("附加聊天附件失败 notificationId={}, fileIds={}", n.getId(), attachmentFileIds, e);
            }
        }
        // 更新/创建会话与未读
        try {
            Long courseId = ("course".equalsIgnoreCase(relatedType) && relatedId != null) ? relatedId : 0L;
            Long cId = chatMapper.findConversationId(Math.min(senderId, recipientId), Math.max(senderId, recipientId), courseId);
            if (cId == null) {
                chatMapper.insertConversation(senderId, recipientId, courseId, n.getId(), java.time.LocalDateTime.now());
                cId = chatMapper.findConversationId(Math.min(senderId, recipientId), Math.max(senderId, recipientId), courseId);
            } else {
                chatMapper.updateConversationLast(cId, n.getId(), java.time.LocalDateTime.now());
            }
            try { if (cId != null) notificationMapper.setConversationId(n.getId(), cId); } catch (Exception ignore) {}
            if (cId != null) {
                // 双方成员确保存在
                chatMapper.ensureMember(cId, senderId);
                chatMapper.ensureMember(cId, recipientId);
                // 接收方未读+1
                chatMapper.incrementUnread(cId, recipientId);
                // 若任一方曾归档，则自动解除归档，确保“最近”可见
                try {
                    chatMapper.setArchived(cId, senderId, false);
                    chatMapper.setArchived(cId, recipientId, false);
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            logger.warn("更新会话失败 sender={}, recipient={}, err={}", senderId, recipientId, String.valueOf(e.getMessage()));
        }
        // 构造对“最近”所需的镜像字段，便于前端直接 upsert
        try {
            Long courseId = ("course".equalsIgnoreCase(relatedType) && relatedId != null) ? relatedId : 0L;
            Long peerId = recipientId;
            com.noncore.assessment.entity.User peer = userMapper.selectUserById(peerId);
            Map<String, Object> recentMirror = new java.util.HashMap<>();
            recentMirror.put("conversationId", n.getConversationId());
            recentMirror.put("peerId", peerId);
            String peerDisplay = null;
            try {
                if (peer != null) {
                    String nn = peer.getNickname();
                    peerDisplay = (nn != null && !nn.isBlank()) ? nn : peer.getUsername();
                }
            } catch (Exception ignore) {}
            recentMirror.put("peerUsername", peerDisplay != null ? peerDisplay : String.valueOf(peerId));
            recentMirror.put("peerAvatar", peer != null ? peer.getAvatar() : null);
            recentMirror.put("courseId", courseId);
            recentMirror.put("lastContent", n.getContent());
            recentMirror.put("lastMessageAt", java.time.LocalDateTime.now());
            recentMirror.put("unread", 0);
            // 暂存到 data 返回：方便前端直接入列
            String mirrorJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(recentMirror);
            n.setData(mirrorJson);
        } catch (Exception ignore) {}
        return n;
    }
} 