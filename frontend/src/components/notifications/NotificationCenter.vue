<template>
  <div class="notification-center">
    <!-- 通知中心头部 -->
    <div class="notification-header">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-900">
          通知中心
          <span v-if="unreadCount > 0" class="notification-badge">
            {{ unreadCount }}
          </span>
        </h3>
        <div class="flex items-center space-x-2">
          <!-- 全部标记已读 -->
          <button
            v-if="hasUnread"
            @click="handleMarkAllAsRead"
            class="btn-secondary btn-sm"
            :disabled="loading"
          >
            <check-icon class="w-4 h-4 mr-1" />
            全部已读
          </button>
          <!-- 刷新 -->
          <button
            @click="handleRefresh"
            class="btn-ghost btn-sm"
            :disabled="loading"
          >
            <arrow-path-icon class="w-4 h-4" :class="{ 'animate-spin': loading }" />
          </button>
        </div>
      </div>

      <!-- 过滤器 -->
      <div class="notification-filters">
        <div class="filter-group">
          <label class="filter-label">类型</label>
          <select
            v-model="filters.type"
            @change="handleFilterChange"
            class="filter-select"
          >
            <option value="">全部</option>
            <option value="system">系统</option>
            <option value="assignment">作业</option>
            <option value="grade">成绩</option>
            <option value="course">课程</option>
            <option value="message">消息</option>
          </select>
        </div>

        <div class="filter-group">
          <label class="filter-label">状态</label>
          <select
            v-model="filters.isRead"
            @change="handleFilterChange"
            class="filter-select"
          >
            <option :value="undefined">全部</option>
            <option :value="false">未读</option>
            <option :value="true">已读</option>
          </select>
        </div>

        <div class="filter-group">
          <label class="filter-label">优先级</label>
          <select
            v-model="filters.priority"
            @change="handleFilterChange"
            class="filter-select"
          >
            <option value="">全部</option>
            <option value="urgent">紧急</option>
            <option value="high">高</option>
            <option value="normal">普通</option>
            <option value="low">低</option>
          </select>
        </div>

        <button
          v-if="hasActiveFilters"
          @click="handleClearFilters"
          class="btn-ghost btn-sm"
        >
          <x-mark-icon class="w-4 h-4 mr-1" />
          清除筛选
        </button>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <div v-if="loading && notifications.length === 0" class="loading-state">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p class="text-gray-500 text-center mt-2">加载中...</p>
      </div>

      <div v-else-if="notifications.length === 0" class="empty-state">
        <bell-slash-icon class="w-16 h-16 text-gray-300 mx-auto" />
        <p class="text-gray-500 text-center mt-4">暂无通知</p>
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{
            'notification-unread': !notification.isRead,
            'notification-read': notification.isRead
          }"
        >
          <!-- 通知内容 -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between">
              <div class="flex items-center space-x-2">
                <!-- 类型图标 -->
                <component
                  :is="getNotificationIcon(notification.type)"
                  class="w-5 h-5 flex-shrink-0"
                  :class="getNotificationIconColor(notification.type)"
                />
                
                <!-- 标题 -->
                <h4 class="notification-title">
                  {{ notification.title }}
                </h4>

                <!-- 优先级标签 -->
                <span
                  v-if="notification.priority !== 'normal'"
                  class="priority-badge"
                  :class="getPriorityClass(notification.priority)"
                >
                  {{ getPriorityText(notification.priority) }}
                </span>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center space-x-1">
                <button
                  v-if="!notification.isRead"
                  @click="handleMarkAsRead(notification.id)"
                  class="btn-ghost btn-xs"
                  title="标记已读"
                >
                  <check-icon class="w-4 h-4" />
                </button>
                <button
                  @click="handleDeleteNotification(notification.id)"
                  class="btn-ghost btn-xs text-red-600 hover:text-red-700"
                  title="删除"
                >
                  <trash-icon class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- 内容 -->
            <p class="notification-content">
              {{ notification.content }}
            </p>

            <!-- 时间和状态 -->
            <div class="notification-meta">
              <span class="notification-time">
                {{ formatTime(notification.createdAt) }}
              </span>
              <span
                v-if="notification.isRead"
                class="notification-status read"
              >
                已读
              </span>
              <span
                v-else
                class="notification-status unread"
              >
                未读
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div
        v-if="pagination.page < pagination.totalPages"
        class="load-more"
      >
        <button
          @click="handleLoadMore"
          class="btn-secondary"
          :disabled="loading"
        >
          <span v-if="loading">加载中...</span>
          <span v-else>加载更多</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useNotificationsStore } from '@/stores/notifications'
import { storeToRefs } from 'pinia'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import {
  BellIcon,
  BellSlashIcon,
  CheckIcon,
  TrashIcon,
  ArrowPathIcon,
  XMarkIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  AcademicCapIcon,
  DocumentTextIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline'

// Store
const notificationsStore = useNotificationsStore()
const { t } = useI18n()
const {
  notifications,
  loading,
  pagination,
  filters,
  unreadCount,
  hasUnread
} = storeToRefs(notificationsStore)

// 本地状态
const selectedNotifications = ref<string[]>([])

// 计算属性
const hasActiveFilters = computed(() => {
  return filters.value.type || 
         filters.value.isRead !== undefined || 
         filters.value.priority
})

// 方法
const handleRefresh = async () => {
  await notificationsStore.refresh()
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationsStore.markAllAsRead()
  } catch (error) {
    console.error('标记所有已读失败:', error)
  }
}

const handleMarkAsRead = async (notificationId: string) => {
  try {
    await notificationsStore.markAsRead(notificationId)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const handleDeleteNotification = async (notificationId: string) => {
  if (confirm(t('shared.notifications.confirm.delete') as string)) {
    try {
      await notificationsStore.deleteNotification(notificationId)
      uiToast('success', t('shared.notifications.notify.deletedTitle') as string, t('shared.notifications.notify.deletedMsg') as string)
    } catch (error) {
      console.error('删除通知失败:', error)
    }
  }
}

const uiToast = (type: 'success'|'error'|'warning'|'info', title: string, message: string) => {
  const { useUIStore } = require('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type, title, message })
}

const handleFilterChange = () => {
  // 过滤器改变时重新加载数据
  notificationsStore.fetchNotifications(true)
}

const handleClearFilters = () => {
  notificationsStore.clearFilters()
}

const handleLoadMore = () => {
  notificationsStore.loadMore()
}

// 工具方法
const getNotificationIcon = (type: string) => {
  const icons = {
    system: InformationCircleIcon,
    assignment: DocumentTextIcon,
    grade: AcademicCapIcon,
    course: AcademicCapIcon,
    message: ChatBubbleLeftRightIcon
  }
  return icons[type as keyof typeof icons] || BellIcon
}

const getNotificationIconColor = (type: string) => {
  const colors = {
    system: 'text-blue-500',
    assignment: 'text-orange-500',
    grade: 'text-green-500',
    course: 'text-purple-500',
    message: 'text-indigo-500'
  }
  return colors[type as keyof typeof colors] || 'text-gray-500'
}

const getPriorityClass = (priority: string) => {
  const classes = {
    urgent: 'priority-urgent',
    high: 'priority-high',
    normal: 'priority-normal',
    low: 'priority-low'
  }
  return classes[priority as keyof typeof classes] || 'priority-normal'
}

const getPriorityText = (priority: string) => {
  const texts = {
    urgent: '紧急',
    high: '高',
    normal: '普通',
    low: '低'
  }
  return texts[priority as keyof typeof texts] || '普通'
}

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 1分钟内
  if (diff < 60000) {
    return '刚刚'
  }
  
  // 1小时内
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  
  // 1天内
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // 超过1天
  const days = Math.floor(diff / 86400000)
  if (days < 7) {
    return `${days}天前`
  }
  
  // 显示具体日期
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  notificationsStore.refresh()
})
</script>

<style scoped>
.notification-center {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  border: 1px solid rgb(229 231 235);
}

.notification-header {
  padding: 1rem;
  border-bottom: 1px solid rgb(229 231 235);
}

.notification-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.25rem;
  height: 1.25rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: white;
  background-color: rgb(239 68 68);
  border-radius: 9999px;
  margin-left: 0.5rem;
}

.notification-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: rgb(249 250 251);
  border-radius: 0.5rem;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(55 65 81);
  white-space: nowrap;
}

.filter-select {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  border: 1px solid rgb(209 213 219);
  border-radius: 0.375rem;
}

.filter-select:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
  box-shadow: 0 0 0 2px rgb(59 130 246);
  border-color: rgb(59 130 246);
}

.notification-list {
  padding: 1rem;
  max-height: 24rem;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  padding: 2rem 0;
}

.notification-item {
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid;
  transition: background-color 0.2s;
  cursor: pointer;
}

.notification-unread {
  background-color: rgb(239 246 255);
  border-color: rgb(191 219 254);
}

.notification-unread:hover {
  background-color: rgb(219 234 254);
}

.notification-read {
  background-color: white;
  border-color: rgb(229 231 235);
}

.notification-read:hover {
  background-color: rgb(249 250 251);
}

.notification-title {
  font-weight: 500;
  color: rgb(17 24 39);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-content {
  font-size: 0.875rem;
  color: rgb(75 85 99);
  margin-top: 0.25rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.75rem;
}

.notification-time {
  color: rgb(107 114 128);
}

.notification-status.read {
  color: rgb(107 114 128);
}

.notification-status.unread {
  color: rgb(37 99 235);
  font-weight: 500;
}

.priority-badge {
  padding: 0.125rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: 9999px;
}

.priority-urgent {
  background-color: rgb(254 226 226);
  color: rgb(153 27 27);
}

.priority-high {
  background-color: rgb(255 237 213);
  color: rgb(154 52 18);
}

.priority-normal {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
}

.priority-low {
  background-color: rgb(243 244 246);
  color: rgb(55 65 81);
}

.load-more {
  text-align: center;
  padding-top: 1rem;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background-color: rgb(37 99 235);
  color: white;
  border-radius: 0.375rem;
}

.btn-primary:hover {
  background-color: rgb(29 78 216);
}

.btn-secondary {
  padding: 0.5rem 1rem;
  background-color: rgb(243 244 246);
  color: rgb(55 65 81);
  border-radius: 0.375rem;
}

.btn-secondary:hover {
  background-color: rgb(229 231 235);
}

.btn-ghost {
  padding: 0.75rem;
  color: rgb(75 85 99);
  border-radius: 0.375rem;
}

.btn-ghost:hover {
  color: rgb(31 41 55);
  background-color: rgb(243 244 246);
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.btn-xs {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.btn-primary:disabled,
.btn-secondary:disabled,
.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style> 