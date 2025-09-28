<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <!-- 顶部导航栏 -->
    <nav class="sticky top-0 z-40 px-6 pt-6 pb-6">
      <div class="flex items-center gap-3">
        <!-- 左：标题（药丸，高度60px，i18n） -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[60px] px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-3xl" />
          </div>
        </liquid-glass>

        <!-- 中间留白 -->
        <div class="flex-1"></div>

        <!-- 右：按钮组（药丸，高度60px） -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-1 h-full" containerClass="rounded-full h-[60px] px-4">
          <ripple-button pill :title="t('layout.common.toggleTheme') as string || '主题'" @click="uiStore.toggleDarkMode()">
            <sun-icon v-if="uiStore.isDarkMode" class="w-5 h-5" />
            <moon-icon v-else class="w-5 h-5" />
          </ripple-button>
          <!-- 主题家族选择（使用外层 span 捕获 DOM 以正确定位弹层） -->
          <span ref="themeBtnRef" class="inline-flex">
            <ripple-button pill :title="t('layout.common.themeFamily') as string || '主题家族'" @click="onToggleThemeMenu">
              <paint-brush-icon class="w-5 h-5" />
            </ripple-button>
          </span>
          <language-switcher buttonClass="px-4 h-11 flex items-center rounded-full" />
          <notification-bell>
            <template #trigger="{ toggle }">
              <ripple-button pill :title="t('notifications.title') as string" @click="toggle">
                <bell-icon class="w-5 h-5" />
              </ripple-button>
            </template>
          </notification-bell>
          <span class="relative inline-flex">
            <ripple-button pill :title="t('shared.chat.open') as string || '聊天'" @click.stop="toggleChatDrawer($event)">
              <chat-bubble-left-right-icon class="w-5 h-5" />
            </ripple-button>
            <span v-if="chat.totalUnread > 0 && !chat.isOpen" class="absolute -top-0.5 -right-0.5 glass-badge glass-badge-xs text-[10px] leading-none px-[6px]">{{ Math.min(chat.totalUnread as any, 99) }}</span>
          </span>
          <span ref="userMenuBtn" class="inline-flex">
            <ripple-button pill class="pl-2 pr-3 h-11 items-center whitespace-nowrap" :title="t('layout.common.me') as string || '我'" @click="onToggleUserMenu">
              <user-avatar :avatar="(authStore.user as any)?.avatar" :size="30">
                <div class="w-[30px] h-[30px] rounded-full bg-gray-200 dark:bg-gray-700"></div>
              </user-avatar>
              <span class="text-sm font-medium text-base-content whitespace-nowrap">{{ displayName }}</span>
            </ripple-button>
          </span>
        </liquid-glass>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <div class="flex pt-2 pb-20 relative z-10">
      <!-- 主要内容 -->
      <main class="flex-1">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-0 pb-6">
          <router-view />
        </div>
      </main>
      
      <!-- 全局聊天抽屉：在布局层挂载，保证任意页面都能打开 -->
      <chat-drawer
        :open="chat.isOpen"
        :peer-id="chat.peerId as any"
        :peer-name="chat.peerName as any"
        :course-id="chat.courseId as any"
        @close="chat.closeChat()"
      />
    </div>

    <!-- 底部 DockBar -->
    <dock-bar
      :items="dockItems"
      v-model="activeDock"
      :bottom-offset="24"
      @select="onSelectDock"
    />

    <!-- 用户菜单弹层（模板内挂载） -->
    <teleport to="body">
      <!-- 点击外部关闭：主题菜单遮罩 -->
      <div v-if="showThemeMenu" class="fixed inset-0 z-[999]" @click="showThemeMenu = false"></div>
      <liquid-glass
        v-if="showThemeMenu"
        :style="themeMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl"
        class="p-1"
        :radius="16"
        :frost="0.05"
        @click.stop
      >
        <div class="px-3 py-2 text-xs text-subtle">
          <div class="font-medium mb-1">主题</div>
          <div class="opacity-90">在新版（静态背景+新配色）与旧版（动态背景+旧配色）之间切换</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setTheme('retro')">
          <span>Retro</span>
          <span v-if="isTheme('retro')" class="text-primary-500">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setTheme('dracula')">
          <span>Dracula</span>
          <span v-if="isTheme('dracula')" class="text-primary-500">✓</span>
        </button>
      </liquid-glass>
      <!-- 点击外部关闭：头像菜单遮罩 -->
      <div v-if="showUserMenu" class="fixed inset-0 z-[999]" @click="showUserMenu = false"></div>
      <liquid-glass
        v-if="showUserMenu"
        :style="userMenuStyle"
        containerClass="fixed z-[1000] rounded-xl shadow-lg"
        :radius="16"
        :frost="0.05"
        @click.stop
      >
        <div class="py-1">
          <router-link
            to="/teacher/profile"
            class="block px-4 py-2 text-sm text-subtle hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
            @click="showUserMenu = false"
          >
            <div class="flex items-center space-x-2">
              <user-icon class="h-4 w-4" />
              <span>{{ t('layout.teacher.user.profile') }}</span>
            </div>
          </router-link>
          <router-link
            to="/teacher/help"
            class="block px-4 py-2 text-sm text-subtle hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
            @click="showUserMenu = false"
          >
            <div class="flex items-center space-x-2">
              <question-mark-circle-icon class="h-4 w-4" />
              <span>{{ t('layout.common.help') || '帮助' }}</span>
            </div>
          </router-link>
          <div class="border-t border-gray-100 dark:border-gray-600"></div>
          <button
            @click="handleLogout"
            class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
          >
            <div class="flex items-center space-x-2">
              <arrow-right-on-rectangle-icon class="h-4 w-4" />
              <span>{{ t('layout.teacher.user.logout') }}</span>
            </div>
          </button>
        </div>
      </liquid-glass>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/Button.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import NotificationBell from '@/components/notifications/NotificationBell.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import DockBar from '@/components/ui/DockBar.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import { useChatStore } from '@/stores/chat'
import { useI18n } from 'vue-i18n'
import {
  Bars3Icon,
  SunIcon,
  MoonIcon,
  ChevronDownIcon,
  PaintBrushIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  AcademicCapIcon,
  ChartBarIcon,
  UsersIcon,
  ChatBubbleLeftRightIcon,
  QuestionMarkCircleIcon,
  BellIcon,
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const chat = useChatStore()
const { t } = useI18n()

// 状态
const baseBgStyle = computed(() => ({ background: 'var(--color-base-100)' }))
const showUserMenu = ref(false)
const userMenuBtn = ref<HTMLElement | null>(null)
const userMenuStyle = ref<Record<string, string>>({})
// 玻璃强度菜单移除
const showThemeMenu = ref(false)
const themeBtnRef = ref<HTMLElement | null>(null)
const themeMenuStyle = ref<Record<string, string>>({})
const displayName = computed(() => (authStore.user as any)?.nickname || (authStore.user as any)?.name || (authStore.user as any)?.username || t('layout.common.me') || 'Me')

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

// 生命周期
onMounted(() => {
  try {
    window.addEventListener('ui:close-topbar-popovers', closeTopbarPopovers)
  } catch {}
})
onUnmounted(() => {
  try {
    window.removeEventListener('ui:close-topbar-popovers', closeTopbarPopovers)
  } catch {}
})

watch(showUserMenu, async (v: boolean) => {
  if (!v) return
  await nextTick()
  try {
    const el = userMenuBtn.value as HTMLElement
    const rect = el.getBoundingClientRect()
    userMenuStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 8}px`,
      left: `${Math.max(8, rect.right - 192)}px`,
      width: '12rem',
      zIndex: '1000'
    }
  } catch {}
})


watch(showThemeMenu, async (v: boolean) => {
  if (!v) return
  await nextTick()
  try {
    const el = themeBtnRef.value as HTMLElement
    const rect = el.getBoundingClientRect()
    themeMenuStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 10}px`,
      left: `${Math.max(8, rect.right - 160)}px`,
      width: '10rem',
      zIndex: '1000'
    }
  } catch {}
})
// 调试：观察 isOpen 变化
watch(() => chat.isOpen, (v: boolean) => { try { console.debug('[TeacherLayout] chat.isOpen changed ->', v) } catch {} })

// setGlass 移除：旧主题强制 more，新主题强制 normal（由 store 控制）

function setTheme(v: 'retro' | 'dracula') {
  uiStore.setTheme(v)
  showThemeMenu.value = false
}

const currentTheme = computed(() => (document?.documentElement?.getAttribute('data-theme') || ''))
function isTheme(v: 'retro'|'dracula') { return currentTheme.value === v }

function emitCloseNotification() {
  try { window.dispatchEvent(new CustomEvent('ui:close-notification-dropdown')) } catch {}
}

function onToggleThemeMenu() {
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  showThemeMenu.value = !showThemeMenu.value
}

function onToggleUserMenu() {
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  showUserMenu.value = !showUserMenu.value
}

function closeTopbarPopovers() {
  showThemeMenu.value = false
  showUserMenu.value = false
}

// Dock 配置
const activeDock = computed<string>({
  get() {
    const p = router.currentRoute.value.path
    if (p.startsWith('/teacher/courses')) return 'courses'
    if (p.startsWith('/teacher/analytics')) return 'analytics'
    if (p.startsWith('/teacher/assistant')) return 'assistant'
    if (p.startsWith('/teacher/community')) return 'community'
    if (p.startsWith('/teacher/dashboard')) return 'dashboard'
    return ''
  },
  set(v: string) { /* no-op, handled in onSelectDock */ }
})

const dockItems = computed(() => ([
  { key: 'dashboard', label: t('layout.teacher.sidebar.dashboard') as string, icon: HomeIcon },
  { key: 'courses', label: t('layout.teacher.sidebar.courses') as string, icon: AcademicCapIcon },
  { key: 'analytics', label: t('layout.teacher.sidebar.analytics') as string, icon: ChartBarIcon },
  { key: 'assistant', label: t('layout.teacher.sidebar.ai') as string, icon: UsersIcon },
  { key: 'community', label: t('layout.teacher.sidebar.community') as string, icon: ChatBubbleLeftRightIcon },
]))

function onSelectDock(k: string) {
  const map: Record<string, string> = {
    dashboard: '/teacher/dashboard',
    courses: '/teacher/courses',
    analytics: '/teacher/analytics',
    assistant: '/teacher/assistant',
    community: '/teacher/community',
  }
  const to = map[k] || '/teacher/dashboard'
  if (router.currentRoute.value.path !== to) router.push(to)
}

function toggleChatDrawer(ev?: Event) {
  try { ev?.stopPropagation(); ev?.preventDefault() } catch {}
  try { console.debug('[TeacherLayout] chat toggle clicked. isOpen(before)=', chat.isOpen) } catch {}
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  if (chat.isOpen) return chat.closeChat()
  chat.openChat()
  try { console.debug('[TeacherLayout] chat toggled. isOpen(after)=', chat.isOpen) } catch {}
}
</script>

<style scoped>
/* 仅在教师端提高玻璃卡片的圆角，保持与学生端一致 */
:deep(.glass-ultraThin[class*="rounded"]),
:deep(.glass-thin[class*="rounded"]),
:deep(.glass-regular[class*="rounded"]),
:deep(.glass-thick[class*="rounded"]),
:deep(.glass-ultraThick[class*="rounded"]),
:deep(.popover-glass[class*="rounded"]),
:deep(.card) {
  border-radius: 20px !important;
}
</style>
