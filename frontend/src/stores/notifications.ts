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
  // 全部已读后，短时间内避免后端旧计数回弹角标
  const unreadOptimisticZeroUntil = ref(0)
  
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

  const setUnreadCountGuarded = (next: number) => {
    const value = Math.max(0, Number(next || 0))
    const now = Date.now()
    const current = Math.max(0, Number(stats.value.unreadCount || 0))
    if (now < unreadOptimisticZeroUntil.value && current === 0 && value > 0) {
      return
    }
    stats.value.unreadCount = value
  }

  type FetchNotificationsMode = boolean | {
    resetPage?: boolean
    append?: boolean
  }

  // 获取通知列表
  const fetchNotifications = async (mode: FetchNotificationsMode = true) => {
    // 兼容旧调用：true=重置并替换，false=追加（用于 loadMore）
    const isLegacyBoolean = typeof mode === 'boolean'
    const resetPage = isLegacyBoolean ? mode : !!mode.resetPage
    const append = isLegacyBoolean ? !mode : !!mode.append

    if (resetPage) {
      pagination.value.page = 1
      // 筛选/刷新时清空旧数据，避免视觉混淆
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
      
      if (append) {
        notifications.value.push(...data.items)
      } else {
        notifications.value = data.items
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
      const unreadBefore = Number(stats.value.unreadCount || 0)
      let localReduced = 0
      await notificationAPI.markAsRead(notificationId)
      
      // 更新本地状态
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        const wasUnread = !notification.isRead
        notification.isRead = true
        if (wasUnread) {
          localReduced += 1
          stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - 1)
        }
      }
      
      if (currentNotification.value?.id === notificationId) {
        const wasUnreadCurrent = !currentNotification.value.isRead
        currentNotification.value.isRead = true
        // 详情页直接操作时，列表项可能不在当前页，仍需立即更新角标
        if (wasUnreadCurrent && !notification) {
          localReduced += 1
          stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - 1)
        }
      }

      // 更新统计信息
      await Promise.allSettled([fetchStats(), fetchUnreadCount()])
      // 防止后端短暂延迟导致已读后角标回弹
      const optimisticTarget = Math.max(0, unreadBefore - localReduced)
      stats.value.unreadCount = Math.min(Number(stats.value.unreadCount || 0), optimisticTarget)
      
    } catch (err: any) {
      console.error('标记已读失败:', err)
      throw err
    }
  }

  // 批量标记为已读
  const batchMarkAsRead = async (notificationIds: string[]) => {
    try {
      const unreadBefore = Number(stats.value.unreadCount || 0)
      await notificationAPI.batchMarkAsRead(notificationIds)
      
      // 更新本地状态
      let newlyReadCount = 0
      notifications.value.forEach(notification => {
        if (notificationIds.includes(notification.id)) {
          if (!notification.isRead) newlyReadCount++
          notification.isRead = true
        }
      })
      if (newlyReadCount > 0) {
        stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - newlyReadCount)
      }

      // 更新统计信息
      await Promise.allSettled([fetchStats(), fetchUnreadCount()])
      const optimisticTarget = Math.max(0, unreadBefore - newlyReadCount)
      stats.value.unreadCount = Math.min(Number(stats.value.unreadCount || 0), optimisticTarget)
      
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
      if (currentNotification.value) currentNotification.value.isRead = true
      stats.value.unreadCount = 0
      unreadOptimisticZeroUntil.value = Date.now() + 8000

      // 更新统计信息
      await Promise.allSettled([fetchStats(), fetchUnreadCount()])
      stats.value.unreadCount = 0
      
    } catch (err: any) {
      console.error('全部标记已读失败:', err)
      throw err
    }
  }

  // 删除通知
  const deleteNotification = async (notificationId: string) => {
    try {
      const unreadBefore = Number(stats.value.unreadCount || 0)
      const toDelete = notifications.value.find(n => n.id === notificationId)
      const wasUnread = !!toDelete && !toDelete.isRead
      await notificationAPI.deleteNotification(notificationId)
      
      // 从本地状态中移除
      const index = notifications.value.findIndex(n => n.id === notificationId)
      if (index > -1) {
        notifications.value.splice(index, 1)
      }

      if (currentNotification.value?.id === notificationId) {
        if (!currentNotification.value.isRead) {
          stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - 1)
        }
        currentNotification.value = null
      }
      if (wasUnread) {
        stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - 1)
      }

      // 更新统计信息
      await Promise.allSettled([fetchStats(), fetchUnreadCount()])
      const localReduced = wasUnread ? 1 : 0
      const optimisticTarget = Math.max(0, unreadBefore - localReduced)
      stats.value.unreadCount = Math.min(Number(stats.value.unreadCount || 0), optimisticTarget)
      
    } catch (err: any) {
      console.error('删除通知失败:', err)
      throw err
    }
  }

  // 批量删除通知
  const batchDeleteNotifications = async (notificationIds: string[]) => {
    try {
      const unreadBefore = Number(stats.value.unreadCount || 0)
      const unreadToDelete = notifications.value.filter(
        notification => notificationIds.includes(notification.id) && !notification.isRead
      ).length
      await notificationAPI.batchDeleteNotifications(notificationIds)
      
      // 从本地状态中移除
      notifications.value = notifications.value.filter(
        notification => !notificationIds.includes(notification.id)
      )
      if (unreadToDelete > 0) {
        stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) - unreadToDelete)
      }

      // 更新统计信息
      await Promise.allSettled([fetchStats(), fetchUnreadCount()])
      const optimisticTarget = Math.max(0, unreadBefore - unreadToDelete)
      stats.value.unreadCount = Math.min(Number(stats.value.unreadCount || 0), optimisticTarget)
      
    } catch (err: any) {
      console.error('批量删除通知失败:', err)
      throw err
    }
  }

  // 获取统计信息
  const fetchStats = async () => {
    try {
      const response: any = await notificationAPI.getNotificationStats()
      const total = (response?.totalCount ?? response?.total ?? response?.count ?? 0)
      const unread = (response?.unreadCount ?? response?.unread ?? response?.unread_count ?? 0)
      const today = (response?.todayCount ?? response?.today ?? 0)
      const dist = (response?.byType ?? response?.typeDistribution ?? {}) as Record<string, number>
      stats.value.totalCount = Number(total)
      setUnreadCountGuarded(Number(unread))
      stats.value.todayCount = Number(today)
      stats.value.typeDistribution = dist
    } catch (err: any) {
      console.error('获取通知统计失败:', err)
    }
  }

  // 获取未读数量
  const fetchUnreadCount = async () => {
    try {
      const response: any = await notificationAPI.getUnreadCount()
      const unread = (response?.unreadCount ?? response?.unread ?? response?.count ?? 0)
      setUnreadCountGuarded(Number(unread))
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

  // --- SSE helpers ---
  const toId = (v: any) => String(v)

  const insertOrUpdateFromSse = (incoming: Notification) => {
    if (!incoming || !(incoming as any).id) return
    const id = toId((incoming as any).id)
    const idx = notifications.value.findIndex(n => toId(n.id) === id)
    if (idx >= 0) {
      notifications.value[idx] = { ...notifications.value[idx], ...incoming, id }
    } else {
      notifications.value.unshift({ ...incoming, id } as Notification)
      const max = pagination.value.page * pagination.value.size
      if (notifications.value.length > max) notifications.value.length = max
      // 新通知到达时解除“全部已读”后的回弹保护
      if (!incoming.isRead) {
        unreadOptimisticZeroUntil.value = 0
        stats.value.unreadCount = Math.max(0, Number(stats.value.unreadCount || 0) + 1)
      }
    }
  }

  const applyReadUpdateFromSse = async (payload: { id?: string | number; ids?: Array<string | number>; peerId?: string | number; conversationRead?: boolean; isRead?: boolean }) => {
    try {
      const markIds: string[] = []
      if (payload?.id != null) markIds.push(toId(payload.id))
      if (Array.isArray(payload?.ids)) markIds.push(...(payload.ids as any[]).map(toId))
      if (markIds.length) {
        for (const nid of markIds) {
          const item = notifications.value.find(n => toId(n.id) === nid)
          if (item) item.isRead = payload?.isRead ?? true
        }
      }
      if (payload?.peerId != null && payload?.conversationRead) {
        await fetchNotifications(true)
      }
      await fetchUnreadCount()
      await fetchStats()
    } catch (e) {
      await refresh()
    }
  }

  const removeByIdFromSse = (idLike: string | number) => {
    const id = toId(idLike)
    const idx = notifications.value.findIndex(n => toId(n.id) === id)
    if (idx >= 0) notifications.value.splice(idx, 1)
    if (currentNotification.value && toId(currentNotification.value.id) === id) {
      currentNotification.value = null
    }
  }

  const refreshStatsFromSse = async () => {
    await fetchUnreadCount()
    await fetchStats()
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
    // SSE helpers
    insertOrUpdateFromSse,
    applyReadUpdateFromSse,
    removeByIdFromSse,
    refreshStatsFromSse,
    reset
  }
}) 