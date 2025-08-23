import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationAPI, type Notification, type NotificationStats } from '@/api/notification.api'
import type { PaginatedResponse } from '@/types/api'

export const useNotificationsStore = defineStore('notifications', () => {
  // 状态
  const notifications = ref<Notification[]>([])
  const currentNotification = ref<Notification | null>(null)
  const stats = ref<NotificationStats>({
    totalCount: 0,
    unreadCount: 0,
    todayCount: 0,
    typeDistribution: {}
  })
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // 分页信息
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0,
    totalPages: 0
  })

  // 过滤器
  const filters = ref({
    type: '',
    isRead: undefined as boolean | undefined,
    priority: ''
  })

  // 计算属性
  const unreadNotifications = computed(() => notifications.value.filter(n => !n.isRead))
  const unreadCount = computed(() => stats.value.unreadCount)

  const hasUnread = computed(() => unreadCount.value > 0)

  const notificationsByType = computed(() => {
    const grouped: Record<string, Notification[]> = {}
    notifications.value.forEach(notification => {
      if (!grouped[notification.type]) {
        grouped[notification.type] = []
      }
      grouped[notification.type].push(notification)
    })
    return grouped
  })

  // 获取通知列表
  const fetchNotifications = async (refresh = false) => {
    if (refresh) {
      pagination.value.page = 1
      notifications.value = []
    }

    loading.value = true
    error.value = null

    try {
      const response = await notificationAPI.getMyNotifications({
        page: pagination.value.page,
        size: pagination.value.size,
        type: filters.value.type || undefined,
        isRead: filters.value.isRead,
        priority: filters.value.priority || undefined
      })
      const data = response as unknown as PaginatedResponse<Notification>
      
      if (refresh) {
        notifications.value = data.items
      } else {
        notifications.value.push(...data.items)
      }
      
      pagination.value = {
        page: data.page,
        size: data.size,
        total: data.total,
        totalPages: data.totalPages
      }

    } catch (err: any) {
      console.error('获取通知列表失败:', err)
      error.value = err.message || '获取通知列表失败'
    } finally {
      loading.value = false
    }
  }

  // 获取通知详情
  const fetchNotificationDetail = async (notificationId: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await notificationAPI.getNotification(notificationId)
      currentNotification.value = response as unknown as Notification
      return currentNotification.value
    } catch (err: any) {
      console.error('获取通知详情失败:', err)
      error.value = err.message || '获取通知详情失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  // 标记为已读
  const markAsRead = async (notificationId: string) => {
    try {
      await notificationAPI.markAsRead(notificationId)
      
      // 更新本地状态
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
      }
      
      if (currentNotification.value?.id === notificationId) {
        currentNotification.value.isRead = true
      }

      // 更新统计信息
      await fetchStats()
      
    } catch (err: any) {
      console.error('标记已读失败:', err)
      throw err
    }
  }

  // 批量标记为已读
  const batchMarkAsRead = async (notificationIds: string[]) => {
    try {
      await notificationAPI.batchMarkAsRead(notificationIds)
      
      // 更新本地状态
      notifications.value.forEach(notification => {
        if (notificationIds.includes(notification.id)) {
          notification.isRead = true
        }
      })

      // 更新统计信息
      await fetchStats()
      
    } catch (err: any) {
      console.error('批量标记已读失败:', err)
      throw err
    }
  }

  // 全部标记为已读
  const markAllAsRead = async () => {
    try {
      await notificationAPI.markAllAsRead()
      
      // 更新本地状态
      notifications.value.forEach(notification => {
        notification.isRead = true
      })

      // 更新统计信息
      await fetchStats()
      
    } catch (err: any) {
      console.error('全部标记已读失败:', err)
      throw err
    }
  }

  // 删除通知
  const deleteNotification = async (notificationId: string) => {
    try {
      await notificationAPI.deleteNotification(notificationId)
      
      // 从本地状态中移除
      const index = notifications.value.findIndex(n => n.id === notificationId)
      if (index > -1) {
        notifications.value.splice(index, 1)
      }

      if (currentNotification.value?.id === notificationId) {
        currentNotification.value = null
      }

      // 更新统计信息
      await fetchStats()
      
    } catch (err: any) {
      console.error('删除通知失败:', err)
      throw err
    }
  }

  // 批量删除通知
  const batchDeleteNotifications = async (notificationIds: string[]) => {
    try {
      await notificationAPI.batchDeleteNotifications(notificationIds)
      
      // 从本地状态中移除
      notifications.value = notifications.value.filter(
        notification => !notificationIds.includes(notification.id)
      )

      // 更新统计信息
      await fetchStats()
      
    } catch (err: any) {
      console.error('批量删除通知失败:', err)
      throw err
    }
  }

  // 获取统计信息
  const fetchStats = async () => {
    try {
      const response: any = await notificationAPI.getNotificationStats()
      stats.value.totalCount = Number(response?.total || 0)
      stats.value.unreadCount = Number(response?.unread || 0)
      stats.value.todayCount = Number(response?.today || 0)
      stats.value.typeDistribution = (response?.byType || {}) as Record<string, number>
    } catch (err: any) {
      console.error('获取通知统计失败:', err)
    }
  }

  // 获取未读数量
  const fetchUnreadCount = async () => {
    try {
      const response: any = await notificationAPI.getUnreadCount()
      stats.value.unreadCount = Number(response?.unreadCount || 0)
    } catch (err: any) {
      console.error('获取未读数量失败:', err)
    }
  }

  // 设置过滤器
  const setFilter = <K extends keyof typeof filters.value>(
    key: K, 
    value: typeof filters.value[K]
  ) => {
    filters.value[key] = value
    fetchNotifications(true) // 重新加载数据
  }

  // 清除过滤器
  const clearFilters = () => {
    filters.value = {
      type: '',
      isRead: undefined,
      priority: ''
    }
    fetchNotifications(true)
  }

  // 加载更多
  const loadMore = async () => {
    if (pagination.value.page < pagination.value.totalPages) {
      pagination.value.page++
      await fetchNotifications(false)
    }
  }

  // 刷新数据
  const refresh = async () => {
    await Promise.all([
      fetchNotifications(true),
      fetchStats()
    ])
  }

  // 重置状态
  const reset = () => {
    notifications.value = []
    currentNotification.value = null
    stats.value = {
      totalCount: 0,
      unreadCount: 0,
      todayCount: 0,
      typeDistribution: {}
    }
    loading.value = false
    error.value = null
    pagination.value = {
      page: 1,
      size: 20,
      total: 0,
      totalPages: 0
    }
    clearFilters()
  }

  return {
    // 状态
    notifications,
    currentNotification,
    stats,
    loading,
    error,
    pagination,
    filters,
    
    // 计算属性
    unreadNotifications,
    unreadCount,
    hasUnread,
    notificationsByType,
    
    // 方法
    fetchNotifications,
    fetchNotificationDetail,
    markAsRead,
    batchMarkAsRead,
    markAllAsRead,
    deleteNotification,
    batchDeleteNotifications,
    fetchStats,
    fetchUnreadCount,
    setFilter,
    clearFilters,
    loadMore,
    refresh,
    reset
  }
}) 