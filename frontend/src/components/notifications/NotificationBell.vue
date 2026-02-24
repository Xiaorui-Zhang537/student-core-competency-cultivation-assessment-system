<template>
  <div class="notification-bell" v-click-outside="closeDropdown">
    <!-- 触发器：支持自定义；默认使用 RippleButton(icon) -->
    <span ref="btnRef" class="relative inline-flex">
      <slot name="trigger" :toggle="toggleDropdown">
        <ripple-button icon :title="t('notifications.title') as string" @click="toggleDropdown">
          <bell-icon class="w-4 h-4" />
        </ripple-button>
      </slot>
      <span v-if="unreadCount > 0" class="absolute -top-1 -right-1 glass-badge glass-badge-xs glass-badge-error text-[10px] leading-none px-[6px]">
        {{ Math.min(unreadCount as any, 99) }}
      </span>
    </span>

    <!-- 下拉通知面板 -->
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <teleport to="body">
        <liquid-glass
          v-if="isDropdownOpen"
          effect="occlusionBlur"
          :style="dropdownStyle"
          containerClass="notification-dropdown fixed z-[1000] rounded-2xl border border-white/20 overflow-hidden shadow"
          :radius="16"
          :frost="0.14"
          :alpha="0.96"
          :blur="14"
          :tint="false"
        >
          <!-- 下拉面板头部 -->
          <div class="dropdown-header px-4 py-3 bg-white/5 dark:bg-white/5" style="box-shadow: inset 0 -1px 0 rgba(255,255,255,0.14);">
            <h3 class="text-[15px] font-semibold text-base-content flex items-center">
              <span>{{ t('notifications.title') }}</span>
              <span v-if="unreadCount > 0" class="text-sm font-normal text-gray-500 dark:text-gray-400 ml-2">
                ({{ unreadCount }}{{ t('notifications.unreadSuffix') }})
              </span>
              <ripple-button
                v-if="hasUnread"
                class="ml-3 px-3 py-1 text-xs rounded-full"
                :duration="0"
                :aria-label="t('notifications.actions.markAll') as string"
                :title="t('notifications.actions.markAll') as string"
                @click="handleMarkAllAsRead"
              >
                {{ t('notifications.actions.markAll') }}
              </ripple-button>
            </h3>
          </div>

          <!-- 通知列表 -->
          <div class="dropdown-content">
            <div v-if="loading" class="loading-state">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
              <p class="text-gray-500 dark:text-gray-400 text-center text-sm mt-2">{{ t('notifications.loading') }}</p>
            </div>

            <div v-else-if="recentNotifications.length === 0" class="empty-state">
              <bell-slash-icon class="w-12 h-12 text-gray-300 mx-auto" />
              <p class="text-gray-500 dark:text-gray-400 text-center text-sm mt-2">{{ t('notifications.empty') }}</p>
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="notification in recentNotifications"
                :key="notification.id"
                class="notification-item rounded-md px-2 py-2 transition-colors hover:bg-gray-100 dark:hover:bg-slate-700"
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
                    <!-- 标题 + 优先级徽章 -->
                    <div class="flex items-center space-x-2">
                      <p class="notification-title font-semibold">
                        {{ getLocalizedTitle(notification) }}
                      </p>
                      <badge size="sm" :variant="priorityVariant(notification.priority)">
                        {{ getPriorityText(notification.priority) }}
                      </badge>
                    </div>
                    
                    <!-- 内容预览 -->
                    <p class="notification-preview">
                      {{ getLocalizedContent(notification) }}
                    </p>
                    
                    <!-- 时间 -->
                    <p class="notification-time">
                      {{ formatTime(notification.createdAt) }}
                    </p>
                  </div>

                  <!-- 未读指示器：主题玻璃小圆点（使用 accent 以与 bell 匹配） -->
                  <div
                    v-if="!notification.isRead"
                    class="glass-dot glass-dot-accent flex-shrink-0 mt-2"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 下拉面板底部 -->
          <div class="dropdown-footer bg-white/5 dark:bg-white/5" style="box-shadow: inset 0 1px 0 rgba(255,255,255,0.14);">
            <button
              variant="primary"
              class="w-full justify-center !rounded-none !rounded-b-2xl py-3"
              :title="t('notifications.actions.viewAll') as string"
              :aria-label="t('notifications.actions.viewAll') as string"
              @click="openNotificationCenter"
            >
              {{ t('notifications.actions.viewAll') }}
            </button>
          </div>
        </liquid-glass>
      </teleport>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { storeToRefs } from 'pinia'
import RippleButton from '@/components/ui/RippleButton.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import {
  BellIcon,
  BellSlashIcon,
  InformationCircleIcon,
  DocumentTextIcon,
  AcademicCapIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline'
// i18n
const { t } = useI18n()

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
const btnRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref<Record<string, string>>({})

// 计算属性
const recentNotifications = computed(() => {
  return notifications.value.slice(0, props.maxItems)
})

// 方法
const toggleDropdown = async () => {
  // 已打开时，二次点击应直接关闭（不要再广播全局关闭事件，否则会出现“关了又开”）
  if (isDropdownOpen.value) {
    closeDropdown()
    return
  }
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  isDropdownOpen.value = true
  
  if (isDropdownOpen.value) {
    // 打开下拉框时刷新未读数量
    await notificationsStore.fetchUnreadCount()
    // 始终获取最新通知，避免后台注入后前端不更新
    await notificationsStore.fetchNotifications(true)
    await nextTick()
    // 计算按钮位置，将下拉固定定位到 body，避免被父元素 overflow/transform 影响
    try {
      const el = btnRef.value as HTMLElement
      const rect = el.getBoundingClientRect()
      dropdownStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 18}px`,
        left: `${Math.max(8, rect.right - 320)}px`,
        width: '20rem',
        zIndex: '1000'
      }
    } catch {}
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

  // 统一先进入详情页
  const prefix = window.location.pathname.startsWith('/student') ? '/student' : '/teacher'
  await router.push(`${prefix}/notifications/${notification.id}`)
  closeDropdown()
}

const openNotificationCenter = () => {
  const prefix = window.location.pathname.startsWith('/student') ? '/student' : '/teacher'
  router.push(`${prefix}/notifications`)
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

// 内容本地化（含 post 类型）
const getLocalizedContent = (notification: any) => {
  const type = (notification?.type || '').toLowerCase()
  if (type === 'post') {
    // 直接显示后端内容（可为中文），避免与“消息”类型混淆
    return String(notification?.content || '')
  }
  return String(notification?.content || '')
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

// 本地化系统类标题（含通用回退）
const getLocalizedTitle = (notification: any) => {
  const rt = String(notification?.relatedType || '').toLowerCase()
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
  const type = String(notification?.type || '').toLowerCase()
  const keyByType: Record<string, string> = {
    system: 'notifications.categoryTitles.systemGeneral',
    course: 'notifications.categoryTitles.courseUpdate',
    assignment: 'notifications.categoryTitles.assignmentGeneral',
    grade: 'notifications.categoryTitles.gradePosted',
    message: 'notifications.categoryTitles.message',
    post: 'notifications.categoryTitles.postReply'
  }
  if (keyByType[type]) return t(keyByType[type]) as string
  return String(notification?.title || '')
}

// 优先级：缺省或非法值回退为 low，文案使用 i18n
const normalizePriority = (priority?: string): 'urgent'|'high'|'normal'|'low' => {
  const p = String(priority || '').toLowerCase()
  if (p === 'urgent' || p === 'high' || p === 'normal' || p === 'low') return p as any
  return 'low'
}

const priorityVariant = (priority?: string) => {
  const p = normalizePriority(priority)
  if (p === 'urgent') return 'danger'
  if (p === 'high') return 'warning'
  if (p === 'normal') return 'info'
  return 'secondary'
}

const getPriorityClass = (priority?: string) => {
  const p = normalizePriority(priority)
  const classes = {
    urgent: 'priority-urgent',
    high: 'priority-high',
    normal: 'priority-normal',
    low: 'priority-low'
  }
  return classes[p as keyof typeof classes] || 'priority-normal'
}

const getPriorityText = (priority?: string) => {
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
  // 监听外部请求关闭通知下拉
  try { window.addEventListener('ui:close-notification-dropdown', closeDropdown) } catch {}
  try { window.addEventListener('ui:close-topbar-popovers', closeDropdown) } catch {}
})

// 清理定时器
onBeforeUnmount(() => {
  stopRefreshInterval()
  try { window.removeEventListener('ui:close-notification-dropdown', closeDropdown) } catch {}
  try { window.removeEventListener('ui:close-topbar-popovers', closeDropdown) } catch {}
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
  /* now positioned with fixed via inline style; make it glass-friendly */
  width: 20rem;
  border-radius: 0.75rem; /* rounded-xl */
  background-color: transparent;
  border: 1px solid transparent;
  box-shadow: var(--glass-inner-shadow, inset 0 1px 0 rgba(255,255,255,0.14)), 0 6px 16px rgba(0,0,0,0.10);
}

:global(.dark) .notification-dropdown {
  background-color: transparent;
  border-color: transparent;
  box-shadow: var(--glass-inner-shadow, inset 0 1px 0 rgba(255,255,255,0.08)), 0 6px 16px rgba(0,0,0,0.32);
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid rgb(229 231 235);
}

:global(.dark) .dropdown-header { border-bottom-color: rgb(55 65 81); }

.dropdown-content {
  max-height: 20rem;
  overflow-y: auto;
  padding: 0.75rem;
  /* Hide scrollbars but keep scroll (Firefox/IE/Edge legacy) */
  -ms-overflow-style: none; /* IE 10+ */
  scrollbar-width: none; /* Firefox */
}

.dropdown-footer {
  padding: 0; /* 让底部按钮贴合容器边框 */
}

/* Hide scrollbars but keep scroll (WebKit) */
:deep(.dropdown-content::-webkit-scrollbar) {
  width: 0px;
  height: 0px;
}

.notification-title {
  font-weight: 500;
  font-size: 0.875rem;
  color: rgb(17 24 39);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.dark) .notification-title { color: rgba(255, 255, 255, 0.95); }

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

:global(.dark) .notification-preview { color: rgba(229, 231, 235, 0.9); }

.notification-time {
  font-size: 0.75rem;
  color: rgb(107 114 128);
  margin-top: 0.25rem;
}

:global(.dark) .notification-time { color: rgb(156 163 175); }

/* Dark mode hover feedback only inside bell dropdown */
:global(html.dark) .notification-bell .dropdown-content .notification-item { background-color: transparent; }
:global(html.dark) .notification-bell .dropdown-content .notification-item:hover {
  background-color: rgb(71 85 105);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.18);
}

/* Bell dropdown item spacing and hover (light) */
.notification-bell .dropdown-content .notification-item {
  border-radius: 0.5rem;
  padding: 0.5rem 0.625rem;
  cursor: pointer;
  transition: background-color 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;
}
/* Light mode only: avoid overriding dark hover */
html:not(.dark) .notification-bell .dropdown-content .notification-item:hover {
  background-color: rgb(243, 244, 246);
}
/* Dark mode hover text highlights */
::global(.dark) .notification-bell .dropdown-content .notification-item:hover .notification-title { color: rgba(255, 255, 255, 0.98); }
::global(.dark) .notification-bell .dropdown-content .notification-item:hover .notification-preview { color: rgba(229, 231, 235, 0.95); }
::global(.dark) .notification-bell .dropdown-content .notification-item:hover .notification-time { color: rgb(203, 213, 225); }

/* 提升 bell 暗黑模式标题可读性 */
html:not(.dark) .notification-bell .notification-title { color: rgb(17 24 39); }
::global(.dark) .notification-bell .notification-title { color: rgb(255 255 255); font-weight: 600; }

/* 优先级徽章样式（与通知中心保持一致） */
.priority-badge {
  padding: 0.125rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: 9999px;
  border: 1px solid transparent;
  display: inline-flex;
  align-items: center;
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
  color: rgb(2 132 199);
  border-color: rgb(103 232 249);
}

/* Force title colors to override any prior rules */
::global(.dark) .notification-bell .notification-title { color: rgb(255 255 255) !important; font-weight: 600; }
html:not(.dark) .notification-bell .notification-title { color: rgb(17 24 39) !important; }

/* Ensure title colors within scoped styles using :deep to bypass hashing */
.dark :deep(.notification-bell .notification-title) { color: rgb(255 255 255) !important; font-weight: 600; }
:root:not(.dark) :deep(.notification-bell .notification-title), html:not(.dark) :deep(.notification-bell .notification-title) { color: rgb(17 24 39) !important; }
</style> 