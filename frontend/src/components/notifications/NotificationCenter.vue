<template>
  <div class="notification-center">
    <!-- 通知中心头部（重做：无边框/无阴影/透明背景） -->
    <div class="notification-header">
      <div class="flex items-end justify-between gap-3 flex-nowrap">
        <h3 class="text-2xl lg:text-3xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          {{ t('notifications.title') }}
          <span v-if="unreadCount > 0" class="notification-badge">
            {{ unreadCount }}
          </span>
        </h3>
        <div class="flex items-center gap-2 flex-nowrap justify-end">
          <!-- 顶部：全部已读 + 刷新（遵循仪表盘按钮主题样式） -->
          <Button size="sm" variant="primary"
            @click="handleMarkAllAsRead"
            :disabled="loading || !hasUnread"
            :title="t('notifications.actions.markAll') as string"
          >
            <check-icon class="w-4 h-4 mr-2" />
            {{ t('notifications.actions.markAll') }}
          </Button>

          <Button size="sm" variant="secondary" @click="handleRefresh" :disabled="loading">
            <arrow-path-icon class="w-4 h-4 mr-2" :class="{ 'animate-spin': loading }" />
            {{ t('notifications.actions.refresh') }}
          </Button>
        </div>
      </div>

      <!-- 页面副标题 -->
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ t('notifications.subtitle') }}</p>

      <!-- 过滤器容器：保留下方三个过滤器，横向一排 -->
      <div class="mt-4 rounded-lg p-3 glass-thin" v-glass="{ strength: 'thin', interactive: false }">
        <div class="flex flex-wrap items-end gap-3">
          <div class="w-36">
            <GlassPopoverSelect
              :label="t('notifications.filters.type') as string"
              v-model="filters.type"
              :options="[
                { label: t('notifications.filters.all') as string, value: '' },
                { label: t('notifications.types.system') as string, value: 'system' },
                { label: t('notifications.types.assignment') as string, value: 'assignment' },
                { label: t('notifications.types.grade') as string, value: 'grade' },
                { label: t('notifications.types.course') as string, value: 'course' },
                { label: t('notifications.types.message') as string, value: 'message' }
              ]"
              size="sm"
              stacked
              @change="handleFilterChange"
            />
          </div>

          <div class="w-32">
            <GlassPopoverSelect
              :label="t('notifications.filters.status') as string"
              :options="[
                { label: t('notifications.filters.all') as string, value: 'all' },
                { label: t('notifications.status.unread') as string, value: 'unread' },
                { label: t('notifications.status.read') as string, value: 'read' }
              ]"
              :model-value="filters.isRead===undefined ? 'all' : (filters.isRead ? 'read' : 'unread')"
              @update:modelValue="(v:any)=>{ filters.isRead = (v==='all') ? undefined : (v==='read'); handleFilterChange() }"
              size="sm"
              stacked
            />
          </div>

          <div class="w-28">
            <GlassPopoverSelect
              :label="t('notifications.filters.priority') as string"
              v-model="filters.priority"
              :options="[
                { label: t('notifications.filters.all') as string, value: '' },
                { label: t('notifications.priority.urgent') as string, value: 'urgent' },
                { label: t('notifications.priority.high') as string, value: 'high' },
                { label: t('notifications.priority.normal') as string, value: 'normal' },
                { label: t('notifications.priority.low') as string, value: 'low' }
              ]"
              size="sm"
              stacked
              @change="handleFilterChange"
            />
          </div>

          <div v-if="hasActiveFilters" class="ml-auto">
            <button @click="handleClearFilters" class="btn-ghost btn-sm">
              <x-mark-icon class="w-4 h-4 mr-1" />
              {{ t('notifications.actions.clearFilters') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <div v-if="loading && notifications.length === 0" class="loading-state">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p class="text-gray-500 dark:text-gray-400 text-center mt-2">{{ t('notifications.loading') }}</p>
      </div>

      <div v-else-if="notifications.length === 0" class="empty-state">
        <bell-slash-icon class="w-16 h-16 text-gray-300 mx-auto" />
        <p class="text-gray-500 dark:text-gray-400 text-center mt-4">{{ t('notifications.empty') }}</p>
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item p-3 rounded-lg transition cursor-pointer glass-regular glass-interactive"
          v-glass="{ strength: 'regular', interactive: true }"
          :class="[`type-${notification.type}`]"
          @click="openDetail(notification.id)"
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
                
                <!-- 标题（本地化类别标题） -->
                <h4 class="notification-title dark:text-white">
                  {{ getLocalizedTitle(notification) }}
                </h4>

                <!-- 优先级标签（所有通知均显示） -->
                <span
                  class="priority-badge"
                  :class="getPriorityClass(notification.priority)"
                >
                  {{ getPriorityText(notification.priority) }}
                </span>
              </div>

              <!-- 操作按钮 -->
              <div class="flex items-center space-x-1">
                <button
                  @click.stop="handleDeleteNotification(notification.id)"
                  class="btn-ghost btn-xs text-red-600 hover:text-red-700 rounded-md transition-colors dark:hover:bg-slate-700"
                  title="删除"
                >
                  <trash-icon class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- 内容 -->
            <p class="notification-content dark:text-gray-200">
              {{ getLocalizedContent(notification) }}
            </p>

            <!-- 时间和状态 -->
            <div class="notification-meta">
              <span class="notification-time dark:text-gray-400">
                {{ formatTime(notification.createdAt) }}
              </span>
              <span
                v-if="notification.isRead"
                class="notification-status read dark:text-gray-400"
              >
                {{ t('notifications.status.read') }}
              </span>
              <span
                v-else
                class="notification-status unread dark:text-blue-400"
              >
                {{ t('notifications.status.unread') }}
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
          <span v-if="loading">{{ t('notifications.loading') }}</span>
          <span v-else>{{ t('notifications.actions.loadMore') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useNotificationsStore } from '@/stores/notifications'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
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
  ChatBubbleLeftRightIcon,
  ChevronDownIcon
} from '@heroicons/vue/24/outline'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import Button from '@/components/ui/Button.vue'

// Store
const notificationsStore = useNotificationsStore()
const { t, locale } = useI18n()
const router = useRouter()
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

import { useUIStore } from '@/stores/ui'
const uiToast = (type: 'success'|'error'|'warning'|'info', title: string, message: string) => {
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

const openDetail = async (id: string) => {
  const found = notifications.value.find(n => n.id === id)
  try {
    if (found && !found.isRead) await notificationsStore.markAsRead(id)
  } catch (e) {
    console.error('进入详情前标记已读失败:', e)
  }

  const prefix = window.location.pathname.startsWith('/student') ? '/student' : '/teacher'

  // 帖子/评论通知：跳帖子详情
  if (found?.type === 'post') {
    const postId = found.relatedId || (() => { try { const d = typeof found.data === 'string' ? JSON.parse(found.data) : found.data; return d?.postId } catch { return undefined } })()
    if (postId) {
      router.push(`${prefix}/community/post/${postId}`)
      return
    }
  }

  // 聊天通知：打开聊天抽屉，复用会话
  if (found?.type === 'message') {
    const mod = await import('@/stores/chat')
    const chat = (mod as any).useChatStore()
    const cid = (found.relatedType === 'course') ? found.relatedId : undefined
    chat.openChat(found.senderId, (found as any).senderName || found.title || '', cid)
    return
  }

  // 其他：进入通知详情
  router.push(`${prefix}/notifications/${id}`)
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
  const p = normalizePriority(priority)
  const classes = {
    urgent: 'priority-urgent',
    high: 'priority-high',
    normal: 'priority-normal',
    low: 'priority-low'
  }
  return classes[p as keyof typeof classes] || 'priority-normal'
}

const getPriorityText = (priority: string) => {
  const p = normalizePriority(priority)
  const keyMap: Record<string, string> = {
    urgent: 'notifications.priority.urgent',
    high: 'notifications.priority.high',
    normal: 'notifications.priority.normal',
    low: 'notifications.priority.low'
  }
  const key = keyMap[p] || keyMap.normal
  return t(key) as string
}

// 统一优先级，缺省或非法值回落到 low（需显示 badge）
const normalizePriority = (priority?: string): 'urgent'|'high'|'normal'|'low' => {
  const p = String(priority || '').toLowerCase()
  if (p === 'urgent' || p === 'high' || p === 'normal' || p === 'low') return p as any
  return 'low'
}

// 本地化系统类标题（含通用回退）
const getLocalizedTitle = (notification: any) => {
  // 优先根据 relatedType/category 判定（如后端提供）
  const rt = (notification?.relatedType || '').toLowerCase()
  if (rt) {
    const keyByRelated: Record<string, string> = {
      'system_announcement': 'notifications.categoryTitles.systemAnnouncement',
      'course_update': 'notifications.categoryTitles.courseUpdate',
      'assignment_deadline': 'notifications.categoryTitles.assignmentDeadline',
      'grade_posted': 'notifications.categoryTitles.gradePosted',
      'message': 'notifications.categoryTitles.message'
    }
    if (keyByRelated[rt]) return t(keyByRelated[rt]) as string
  }
  // 其次根据类型提供一个总类标题
  const type = (notification?.type || '').toLowerCase()
  const keyByType: Record<string, string> = {
    system: 'notifications.categoryTitles.systemGeneral',
    course: 'notifications.categoryTitles.courseUpdate',
    assignment: 'notifications.categoryTitles.assignmentGeneral',
    grade: 'notifications.categoryTitles.gradePosted',
    message: 'notifications.categoryTitles.message',
    post: 'notifications.categoryTitles.postReply'
  }
  if (keyByType[type]) return t(keyByType[type]) as string
  // 最后回退原始标题
  return String(notification?.title || '')
}

// 本地化内容（支持 post 类型）
const getLocalizedContent = (notification: any) => {
  const type = (notification?.type || '').toLowerCase()
  if (type === 'post') {
    // 对于帖子回复，直接使用后端内容（可能为中文），不强制英文文案
    return String(notification?.content || '')
  }
  // 其他类型沿用后端 content
  return String(notification?.content || '')
}

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const isEn = String(locale.value || '').toLowerCase().startsWith('en')

  if (diff < 60000) {
    return t('notifications.time.justNow') as string
  }

  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    if (isEn) return `${minutes} minute${minutes === 1 ? '' : 's'} ago`
    return t('notifications.time.minutesAgo', { count: minutes }) as string
  }

  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    if (isEn) return `${hours} hour${hours === 1 ? '' : 's'} ago`
    return t('notifications.time.hoursAgo', { count: hours }) as string
  }

  const days = Math.floor(diff / 86400000)
  if (days < 7) {
    if (isEn) return `${days} day${days === 1 ? '' : 's'} ago`
    return t('notifications.time.daysAgo', { count: days }) as string
  }

  return date.toLocaleString(locale.value, {
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
/* 让最外层容器透明，由内部卡片与区块控制背景与边框 */
.notification-center {
  background-color: transparent;
  border: 0;
  box-shadow: none;
}

:global(.dark) .notification-center { background-color: transparent; }

.notification-header {
  padding: 0; /* 重做容器：不使用原有分割线 */
  border: 0;
  box-shadow: none;
  background: transparent;
}

:global(.dark) .notification-header {
  border: 0;
  box-shadow: none;
  background: transparent;
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

/* 过滤器区域样式已重构为原子类，以下旧样式删除 */

.notification-list {
  padding: 1rem;
  max-height: none; /* 放宽高度限制，使用页面滚动 */
  overflow-y: visible;
}

/* 暗黑模式全局滚动条（通知中心区域内） */
::global(.dark) .notification-list::-webkit-scrollbar { width: 10px; }
::global(.dark) .notification-list::-webkit-scrollbar-track { background: transparent; }
::global(.dark) .notification-list::-webkit-scrollbar-thumb { background-color: rgb(71 85 105); border-radius: 9999px; }
::global(.dark) .notification-list::-webkit-scrollbar-thumb:hover { background-color: rgb(100 116 139); }

.loading-state,
.empty-state {
  padding: 2rem 0;
}

.notification-item {
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 0; /* 去掉黑色描边 */
  transition: background-color 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.notification-unread {
  background-color: rgb(239 246 255);
}

:global(.dark) .notification-unread {
  background-color: rgb(30 58 138 / 0.3); /* blue-900/30 */
  border-color: rgb(191 219 254 / 0.2);
}

.notification-unread:hover {
  background-color: rgb(219 234 254);
}

.notification-read {
  background-color: white;
}

:global(.dark) .notification-read {
  background-color: rgb(55 65 81 / 0.6); /* gray-700/60 */
  border-color: rgb(255 255 255 / 0.1);
}

.notification-read:hover {
  background-color: rgb(249 250 251);
}

/* 仅在明亮模式下设置标题为深色，避免覆盖暗黑模式的白色 */
.notification-title {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
html:not(.dark) .notification-center .notification-title { color: rgb(17 24 39); }

/* 提升暗黑模式下的基础可读性（非悬停态） */
::global(.dark) .notification-center .notification-title { color: rgba(255, 255, 255, 0.98); }

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
::global(.dark) .notification-center .notification-content { color: rgba(229, 231, 235, 0.96); }

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
::global(.dark) .notification-center .notification-time { color: rgb(203, 213, 225); }

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
  border: 1px solid transparent;
}

.priority-urgent {
  background-color: rgb(254 226 226);
  color: rgb(153 27 27);
  border-color: rgb(248 113 113);
}

.priority-high {
  background-color: rgb(255 237 213);
  color: rgb(154 52 18);
  border-color: rgb(251 146 60);
}

.priority-normal {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
  border-color: rgb(96 165 250);
}

.priority-low {
  background-color: rgb(243 244 246);
  color: rgb(2 132 199); /* cyan-600 提升区分度 */
  border-color: rgb(103 232 249); /* cyan-300 边框更明显 */
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

html:not(.dark) .btn-ghost:hover {
  color: rgb(31 41 55);
  background-color: rgb(243 244 246);
}

/* Dark mode hover/active styles to avoid white flash */
::global(.dark) .btn-ghost { color: rgb(209 213 219); }
::global(.dark) .btn-ghost:hover,
::global(.dark) .btn-ghost:focus,
::global(.dark) .btn-ghost:active {
  color: rgb(255 255 255); /* 与侧栏逻辑一致 */
  background-color: rgb(55 65 81); /* gray-700 */
}
::global(.dark) .btn-ghost.text-red-600:hover { color: rgb(255 255 255); }

/* Dark mode hover for list items (scoped) - stronger contrast */
::global(html.dark) .notification-center .notification-item:hover,
::global(.dark) .notification-center .notification-item:hover,
::global(.dark) .notification-center .notification-item.notification-read:hover,
::global(.dark) .notification-center .notification-item.notification-unread:hover {
  /* slate-500 */
  background-color: rgb(100 116 139) !important;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.25) !important;
}
/* Dark mode hover text highlights (ensure visible) */
::global(html.dark) .notification-center .notification-item:hover .notification-title,
::global(.dark) .notification-center .notification-item:hover .notification-title { color: rgba(255, 255, 255, 0.98) !important; }
::global(html.dark) .notification-center .notification-item:hover .notification-content,
::global(.dark) .notification-center .notification-item:hover .notification-content { color: rgba(229, 231, 235, 0.95) !important; }
::global(html.dark) .notification-center .notification-item:hover .notification-time,
::global(.dark) .notification-center .notification-item:hover .notification-time { color: rgb(203, 213, 225) !important; }
/* Light mode hover for list items */
html:not(.dark) .notification-center .notification-item:hover { background-color: rgb(243 244 246) !important; }
/* 悬停时同步提亮文字，贴合侧栏 hover 文本为白色的逻辑 */
::global(.dark) .notification-center .notification-item:hover .notification-title { color: rgba(255, 255, 255, 0.98); }
::global(.dark) .notification-center .notification-item:hover .notification-content { color: rgba(229, 231, 235, 0.95); }
::global(.dark) .notification-center .notification-item:hover .notification-time { color: rgb(203, 213, 225); }
::global(.dark) .notification-center .notification-item:active { background-color: rgb(71 85 105); }

/* 针对 system 与 assignment 类型在暗黑模式下提升标题可读性 */
::global(.dark) .notification-center .notification-item.type-system .notification-title,
::global(.dark) .notification-center .notification-item.type-assignment .notification-title {
  color: rgba(255, 255, 255, 0.98);
  font-weight: 600;
}

/* 统一加强暗黑模式标题（所有类型，非悬停态） */
::global(.dark) .notification-center .notification-item .notification-title {
  color: rgb(255 255 255);
  font-weight: 600;
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

/* 只包 select 的小框，不包含标题标签 */
.filter-box {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgb(229 231 235);
  border-radius: 0.5rem;
  padding: 0 0.5rem;
}
::global(.dark) .filter-box {
  background-color: rgb(15 23 42);
  border-color: rgb(71 85 105);
}
:global(.dark) .filter-box select {
  background-color: transparent !important;
  color: rgb(229 231 235);
}
</style> 