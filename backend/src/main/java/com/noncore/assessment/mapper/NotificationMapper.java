package com.noncore.assessment.mapper;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通知数据访问层Mapper接口
 * 定义通知相关的数据库操作方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入通知
     *
     * @param notification 通知对象
     * @return 影响行数
     */
    int insertNotification(Notification notification);

    /**
     * 批量插入通知
     *
     * @param notifications 通知列表
     * @return 影响行数
     */
    int batchInsertNotifications(@Param("notifications") List<Notification> notifications);

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知对象
     */
    Notification selectNotificationById(@Param("id") Long id);

    /**
     * 根据接收者ID查询通知列表
     *
     * @param recipientId 接收者ID
     * @return 通知列表
     */
    List<Notification> selectByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * 根据发送者ID查询通知列表
     *
     * @param senderId 发送者ID
     * @return 通知列表
     */
    List<Notification> selectBySenderId(@Param("senderId") Long senderId);

    /**
     * 分页查询接收者的通知
     *
     * @param recipientId 接收者ID
     * @param type 通知类型（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 通知列表
     */
    List<Notification> selectByRecipientWithPagination(@Param("recipientId") Long recipientId, 
                                                      @Param("type") String type, 
                                                      @Param("offset") int offset, 
                                                      @Param("size") Integer size);

    /**
     * 统计通知总数（用于分页）
     *
     * @param recipientId 接收者ID
     * @param type 通知类型（可选）
     * @return 通知总数
     */
    Integer countByRecipient(@Param("recipientId") Long recipientId, @Param("type") String type);

    /**
     * 根据类型查询通知列表
     *
     * @param recipientId 接收者ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<Notification> selectByRecipientAndType(@Param("recipientId") Long recipientId, @Param("type") String type);

    /**
     * 更新通知
     *
     * @param notification 通知对象
     * @return 影响行数
     */
    int updateNotification(Notification notification);

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 影响行数
     */
    int markAsRead(@Param("id") Long id);

    /**
     * 批量标记指定ID的通知为已读
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchMarkAsRead(@Param("notificationIds") List<Long> notificationIds, @Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     *
     * @param recipientId 接收者ID
     * @return 影响行数
     */
    int markAllAsReadByRecipient(@Param("recipientId") Long recipientId);

    /**
     * 删除通知（软删除）
     *
     * @param id 通知ID
     * @return 影响行数
     */
    int deleteNotification(@Param("id") Long id);

    /**
     * 统计未读通知数量
     *
     * @param recipientId 接收者ID
     * @return 未读通知数量
     */
    Integer countUnreadByRecipient(@Param("recipientId") Long recipientId);
    
    /**
     * 根据类型统计未读通知数量
     *
     * @param recipientId 接收者ID
     * @param type 通知类型
     * @return 未读通知数量
     */
    Integer countUnreadByRecipientWithType(@Param("recipientId") Long recipientId, @Param("type") String type);

    /**
     * 统计未读通知数量（StudentServiceImpl调用）
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Integer countUnreadNotifications(@Param("userId") Long userId);

    /**
     * 获取最近的通知列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近通知列表
     */
    List<Notification> selectRecentNotifications(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 按类型统计通知数量
     * @param userId 用户ID
     * @return 统计结果
     */
    Map<String, Long> countByRecipientGroupByType(@Param("userId") Long userId);

    /**
     * 清理过期的通知
     * @param cutoffDate 过期时间点
     * @return 影响行数
     */
    int deleteExpiredNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    List<StudentDashboardResponse.RecentNotificationDto> findRecentNotifications(@Param("recipientId") Long recipientId, @Param("limit") int limit);
} 