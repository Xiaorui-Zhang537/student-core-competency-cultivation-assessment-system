import { api } from './config'
import type { ApiResponse, PaginatedResponse } from '@/types/api'

/**
 * 通知数据类型定义
 */
export interface Notification {
  id: string
  title: string
  content: string
  type: 'system' | 'assignment' | 'grade' | 'course' | 'message'
  priority: 'low' | 'normal' | 'high' | 'urgent'
  isRead: boolean
  relatedType?: string
  relatedId?: string
  expiredAt?: string
  createdAt: string
}

export interface NotificationStats {
  totalCount: number
  unreadCount: number
  todayCount: number
  typeDistribution: Record<string, number>
}

export interface CreateNotificationRequest {
  title: string
  content: string
  type: 'system' | 'assignment' | 'grade' | 'course' | 'message'
  priority?: 'low' | 'normal' | 'high' | 'urgent'
  userId?: string
  userIds?: string[]
  relatedType?: string
  relatedId?: string
  expiredAt?: string
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
  }): Promise<ApiResponse<PaginatedResponse<Notification>>> => {
    return api.get('/notifications/my', { params })
  },

  // 获取通知详情
  getNotification: (notificationId: string): Promise<ApiResponse<Notification>> => {
    return api.get(`/notifications/${notificationId}`)
  },

  // 标记通知为已读
  markAsRead: (notificationId: string): Promise<ApiResponse<void>> => {
    return api.put(`/notifications/${notificationId}/read`)
  },

  // 批量标记通知为已读
  batchMarkAsRead: (notificationIds: string[]): Promise<ApiResponse<void>> => {
    return api.put('/notifications/batch/read', { notificationIds })
  },

  // 全部标记为已读
  markAllAsRead: (): Promise<ApiResponse<void>> => {
    return api.put('/notifications/all/read')
  },

  // 删除通知
  deleteNotification: (notificationId: string): Promise<ApiResponse<void>> => {
    return api.delete(`/notifications/${notificationId}`)
  },

  // 批量删除通知
  batchDeleteNotifications: (notificationIds: string[]): Promise<ApiResponse<void>> => {
    return api.delete('/notifications/batch', { data: { notificationIds } })
  },

  // 获取未读通知数量
  getUnreadCount: (): Promise<ApiResponse<{ unreadCount: number }>> => {
    return api.get('/notifications/unread/count')
  },

  // 获取通知统计
  getNotificationStats: (): Promise<ApiResponse<NotificationStats>> => {
    return api.get('/notifications/stats')
  },

  // 发送通知（管理员/教师功能）
  sendNotification: (data: CreateNotificationRequest): Promise<ApiResponse<Notification>> => {
    return api.post('/notifications/send', data)
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
  }): Promise<ApiResponse<{ sentCount: number }>> => {
    return api.post('/notifications/batch/send', data)
  },

  // 获取与某人的会话
  getConversation: (peerId: string | number, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Notification>>> => {
    return api.get('/notifications/conversation', { params: { peerId, ...(params || {}) } })
  },

  // 标记会话已读
  readConversation: (peerId: string | number): Promise<ApiResponse<{ marked: number }>> => {
    return api.post('/notifications/conversation/read', undefined, { params: { peerId } })
  },

  // 发送作业通知（教师功能）
  sendAssignmentNotification: (assignmentId: string, data: {
    title: string
    content: string
    type: 'assignment' | 'grade'
    priority?: string
  }): Promise<ApiResponse<void>> => {
    return api.post(`/notifications/assignment/${assignmentId}`, data)
  },

  // 发送课程通知（教师功能）
  sendCourseNotification: (courseId: string, data: {
    title: string
    content: string
    type: 'course' | 'system'
    priority?: string
  }): Promise<ApiResponse<void>> => {
    return api.post(`/notifications/course/${courseId}`, data)
  }
}

export default notificationAPI 