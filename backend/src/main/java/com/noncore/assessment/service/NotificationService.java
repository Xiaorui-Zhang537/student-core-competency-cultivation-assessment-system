package com.noncore.assessment.service;

import com.noncore.assessment.entity.Notification;
import com.noncore.assessment.util.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 处理系统通知的发送、接收、管理等功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface NotificationService {

    /**
     * 发送通知
     *
     * @param recipientId 接收者ID
     * @param senderId 发送者ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param category 通知分类
     * @param priority 优先级
     * @param relatedType 关联对象类型
     * @param relatedId 关联对象ID
     * @return 通知对象
     */
    Notification sendNotification(Long recipientId, Long senderId, String title, String content, 
                                String type, String category, String priority, 
                                String relatedType, Long relatedId);

    /**
     * 批量发送通知
     *
     * @param recipientIds 接收者ID列表
     * @param senderId 发送者ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param category 通知分类
     * @param priority 优先级
     * @param relatedType 关联对象类型
     * @param relatedId 关联对象ID
     * @return 发送结果统计
     */
    Map<String, Object> batchSendNotification(List<Long> recipientIds, Long senderId, String title, String content, 
                                            String type, String category, String priority, 
                                            String relatedType, Long relatedId);

    /**
     * 获取用户通知列表（分页）
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 通知列表分页结果
     */
    PageResult<Notification> getUserNotifications(Long userId, String type, Integer page, Integer size);

    /**
     * 获取用户所有通知
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 通知列表
     */
    List<Notification> getAllUserNotifications(Long userId, String type);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 操作结果统计
     */
    Map<String, Object> batchMarkAsRead(List<Long> notificationIds, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 标记数量
     */
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 批量删除通知
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 操作结果统计
     */
    Map<String, Object> batchDeleteNotifications(List<Long> notificationIds, Long userId);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @return 未读通知数量
     */
    Integer getUnreadCount(Long userId, String type);

    /**
     * 获取通知详情
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 通知详情
     */
    Notification getNotificationDetail(Long notificationId, Long userId);

    /**
     * 发送系统通知
     *
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param targetType 目标类型（all, role, specific）
     * @param targetIds 目标用户ID列表（当targetType为specific时使用）
     * @param role 角色（当targetType为role时使用）
     * @return 发送结果
     */
    Map<String, Object> sendSystemNotification(String title, String content, String type, 
                                             String targetType, List<Long> targetIds, String role);

    /**
     * 发送作业相关通知
     *
     * @param assignmentId 作业ID
     * @param type 通知类型（assignment_created, assignment_updated, deadline_reminder等）
     * @param customMessage 自定义消息（可选）
     * @return 发送结果
     */
    Map<String, Object> sendAssignmentNotification(Long assignmentId, String type, String customMessage);

    /**
     * 发送课程相关通知
     *
     * @param courseId 课程ID
     * @param type 通知类型（course_updated, new_lesson, course_completed等）
     * @param customMessage 自定义消息（可选）
     * @return 发送结果
     */
    Map<String, Object> sendCourseNotification(Long courseId, String type, String customMessage);

    /**
     * 发送成绩相关通知
     *
     * @param gradeId 成绩ID
     * @param type 通知类型（grade_published, feedback_available等）
     * @param customMessage 自定义消息（可选）
     * @return 发送结果
     */
    Map<String, Object> sendGradeNotification(Long gradeId, String type, String customMessage);

    /**
     * 获取通知统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getNotificationStats(Long userId);

    /**
     * 清理过期通知
     *
     * @param days 保留天数
     * @return 清理统计
     */
    Map<String, Object> cleanupExpiredNotifications(int days);

    /**
     * 获取与某人的会话记录
     */
    PageResult<Notification> getConversation(Long userId, Long peerId, Integer page, Integer size, Long courseId);

    /**
     * 将与某人的会话全部标记为已读
     */
    int markConversationAsRead(Long userId, Long peerId, Long courseId);

    /**
     * 发送聊天消息（面向双方：教师或学生）。
     * 若为课程上下文，需校验发送者与接收者皆为该课程的参与者（教师或学生）。
     */
    Notification sendMessage(Long senderId, Long recipientId, String content, String relatedType, Long relatedId,
                             java.util.List<Long> attachmentFileIds);
} 