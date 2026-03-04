import { api } from './config'

/**
 * 通知数据类型定义
 */
export interface Notification {
  id: string
  title: string
  content: string
  // add 'post' to align with backend enum and UI routing
  type: 'system' | 'assignment' | 'grade' | 'course' | 'message' | 'post'
  priority: 'low' | 'normal' | 'high' | 'urgent'
  isRead: boolean
  relatedType?: string
  relatedId?: string | number
  expiredAt?: string
  createdAt: string
  // optional fields used by UI
  senderId?: string | number
  senderName?: string
  category?: string
  actionUrl?: string
  data?: any
}

export interface NotificationStats {
  totalCount: number
  unreadCount: number
  todayCount: number
  typeDistribution: Record<string, number>
}

export interface AdminBroadcastNotificationRequest {
  title: string
  content: string
  type?: string
  category?: string
  priority?: string
  targetType: 'all' | 'role' | 'specific'
  role?: 'student' | 'teacher' | 'admin'
  targetIds?: (string | number)[]
}

/**
 * 通知API调用
 */
export const notificationAPI = {
  // 获取我的通知列表
  getMyNotifications: (params?: { 
    page?: number
    size?: number
    type?: string
    isRead?: boolean
    priority?: string
  }): Promise<PaginatedResponse<Notification>> => {
    return api.get('/notifications/my', { params })
  },

  // 获取通知详情
  getNotification: (notificationId: string): Promise<Notification> => {
    return api.get(`/notifications/${notificationId}`)
  },

  // 标记通知为已读
  markAsRead: (notificationId: string): Promise<void> => {
    return api.put(`/notifications/${notificationId}/read`)
  },

  // 全部标记为已读
  markAllAsRead: (): Promise<void> => {
    return api.put('/notifications/all/read')
  },

  // 删除通知
  deleteNotification: (notificationId: string): Promise<void> => {
    return api.delete(`/notifications/${notificationId}`)
  },

  // 获取未读通知数量
  getUnreadCount: (): Promise<{ unreadCount: number }> => {
    return api.get('/notifications/unread/count')
  },

  // 获取通知统计
  getNotificationStats: (): Promise<NotificationStats> => {
    return api.get('/notifications/stats')
  },

  // 批量发送通知（管理员/教师功能）
  batchSend: (data: {
    recipientIds: (string|number)[]
    title: string
    content: string
    type?: string
    category?: string
    priority?: string
    relatedType?: string
    relatedId?: string|number
  }): Promise<{ sentCount: number }> => {
    return api.post('/notifications/batch/send', data)
  },

  // 管理员群发通知（全体/按角色/指定用户）
  adminBroadcast: (data: AdminBroadcastNotificationRequest): Promise<any> => {
    return api.post('/notifications/admin/broadcast', data)
  }
}

export default notificationAPI 
