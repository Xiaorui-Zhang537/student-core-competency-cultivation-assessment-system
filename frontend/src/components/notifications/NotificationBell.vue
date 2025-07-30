<template>
  <div class="notification-bell" v-click-outside="closeDropdown">
    <!-- 铃铛图标 -->
    <button
      @click="toggleDropdown"
      class="relative p-2 text-gray-600 hover:text-gray-800 focus:ring-2 focus:ring-blue-500 rounded-lg"
      :class="{ 'text-blue-600': hasUnread }"
    >
      <bell-icon class="w-6 h-6" />
      
      <!-- 未读数量徽章 -->
      <span
        v-if="unreadCount > 0"
        class="absolute -top-1 -right-1 inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-red-500 rounded-full animate-pulse"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <!-- 下拉通知面板 -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="isDropdownOpen"
        class="notification-dropdown"
      >
        <!-- 下拉面板头部 -->
        <div class="dropdown-header">
          <h3 class="text-lg font-semibold text-gray-900">
            通知
            <span v-if="unreadCount > 0" class="text-sm font-normal text-gray-500 ml-2">
              ({{ unreadCount }}条未读)
            </span>
          </h3>
          <div class="flex items-center space-x-2">
            <button
              v-if="hasUnread"
              @click="handleMarkAllAsRead"
              class="text-sm text-blue-600 hover:text-blue-700"
              :disabled="loading"
            >
              全部已读
            </button>
            <button
              @click="openNotificationCenter"
              class="text-sm text-gray-600 hover:text-gray-700"
            >
              查看全部
            </button>
          </div>
        </div>

        <!-- 通知列表 -->
        <div class="dropdown-content">
          <div v-if="loading" class="loading-state">
            <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
            <p class="text-gray-500 text-center text-sm mt-2">加载中...</p>
          </div>

          <div v-else-if="recentNotifications.length === 0" class="empty-state">
            <bell-slash-icon class="w-12 h-12 text-gray-300 mx-auto" />
            <p class="text-gray-500 text-center text-sm mt-2">暂无通知</p>
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="notification in recentNotifications"
              :key="notification.id"
              class="notification-item"
              :class="{ 'notification-unread': !notification.isRead }"
              @click="handleNotificationClick(notification)"
            >
              <div class="flex items-start space-x-3">
                <!-- 类型图标 -->
                <component
                  :is="getNotificationIcon(notification.type)"
                  class="w-5 h-5 flex-shrink-0 mt-0.5"
                  :class="getNotificationIconColor(notification.type)"
                />
                
                <div class="flex-1 min-w-0">
                  <!-- 标题 -->
                  <p class="notification-title">
                    {{ notification.title }}
                  </p>
                  
                  <!-- 内容预览 -->
                  <p class="notification-preview">
                    {{ notification.content }}
                  </p>
                  
                  <!-- 时间 -->
                  <p class="notification-time">
                    {{ formatTime(notification.createdAt) }}
                  </p>
                </div>

                <!-- 未读指示器 -->
                <div
                  v-if="!notification.isRead"
                  class="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-2"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 下拉面板底部 -->
        <div class="dropdown-footer">
          <button
            @click="openNotificationCenter"
            class="w-full py-2 text-sm text-center text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-md transition-colors"
          >
            查看所有通知
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { storeToRefs } from 'pinia'
import {
  BellIcon,
  BellSlashIcon,
  InformationCircleIcon,
  DocumentTextIcon,
  AcademicCapIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline'

// Props
interface Props {
  maxItems?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxItems: 5
})

// Router
const router = useRouter()

// Store
const notificationsStore = useNotificationsStore()
const {
  notifications,
  loading,
  unreadCount,
  hasUnread
} = storeToRefs(notificationsStore)

// 本地状态
const isDropdownOpen = ref(false)

// 计算属性
const recentNotifications = computed(() => {
  return notifications.value.slice(0, props.maxItems)
})

// 方法
const toggleDropdown = async () => {
  isDropdownOpen.value = !isDropdownOpen.value
  
  if (isDropdownOpen.value) {
    // 打开下拉框时刷新未读数量
    await notificationsStore.fetchUnreadCount()
    // 如果没有通知数据，则获取最新通知
    if (notifications.value.length === 0) {
      await notificationsStore.fetchNotifications(true)
    }
  }
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationsStore.markAllAsRead()
    closeDropdown()
  } catch (error) {
    console.error('标记所有已读失败:', error)
  }
}

const handleNotificationClick = async (notification: any) => {
  // 如果未读，标记为已读
  if (!notification.isRead) {
    try {
      await notificationsStore.markAsRead(notification.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 根据通知类型跳转到相应页面
  if (notification.relatedType && notification.relatedId) {
    switch (notification.relatedType) {
      case 'assignment':
        await router.push(`/assignments/${notification.relatedId}`)
        break
      case 'course':
        await router.push(`/courses/${notification.relatedId}`)
        break
      case 'grade':
        await router.push('/grades')
        break
      default:
        // 跳转到通知中心
        openNotificationCenter()
        break
    }
  } else {
    openNotificationCenter()
  }

  closeDropdown()
}

const openNotificationCenter = () => {
  router.push('/notifications')
  closeDropdown()
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
  return `${days}天前`
}

// 定期刷新未读数量
let refreshInterval: NodeJS.Timeout | null = null

const startRefreshInterval = () => {
  // 每30秒刷新一次未读数量
  refreshInterval = setInterval(() => {
    if (!isDropdownOpen.value) {
      notificationsStore.fetchUnreadCount()
    }
  }, 30000)
}

const stopRefreshInterval = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

// 生命周期
onMounted(() => {
  // 初始化时获取未读数量
  notificationsStore.fetchUnreadCount()
  // 开始定期刷新
  startRefreshInterval()
})

// 清理定时器
onBeforeUnmount(() => {
  stopRefreshInterval()
})

// 点击外部关闭下拉框的指令
interface HTMLElementWithClickOutside extends HTMLElement {
  clickOutsideEvent?: (event: Event) => void
}

const vClickOutside = {
  beforeMount(el: HTMLElementWithClickOutside, binding: any) {
    el.clickOutsideEvent = (event: Event) => {
      if (!(el === event.target || el.contains(event.target as Node))) {
        binding.value()
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el: HTMLElementWithClickOutside) {
    if (el.clickOutsideEvent) {
      document.removeEventListener('click', el.clickOutsideEvent)
    }
  }
}
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 0.5rem;
  width: 20rem;
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -2px rgb(0 0 0 / 0.05);
  border: 1px solid rgb(229 231 235);
  z-index: 50;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid rgb(229 231 235);
}

.dropdown-content {
  max-height: 20rem;
  overflow-y: auto;
  padding: 0.5rem;
}

.dropdown-footer {
  padding: 0.5rem;
  border-top: 1px solid rgb(229 231 235);
}

.loading-state,
.empty-state {
  padding: 1.5rem 0;
}

.notification-item {
  padding: 0.75rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: rgb(249 250 251);
}

.notification-unread {
  background-color: rgb(239 246 255);
}

.notification-unread:hover {
  background-color: rgb(219 234 254);
}

.notification-title {
  font-weight: 500;
  font-size: 0.875rem;
  color: rgb(17 24 39);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-preview {
  font-size: 0.75rem;
  color: rgb(75 85 99);
  margin-top: 0.25rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 0.75rem;
  color: rgb(107 114 128);
  margin-top: 0.25rem;
}
</style> 