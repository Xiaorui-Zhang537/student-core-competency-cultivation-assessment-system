<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <nav class="sticky top-0 z-40 px-6 pt-6 pb-6">
      <div class="flex items-center gap-3">
        <!-- 左：系统名称（药丸，SparklesText，不换行，自适应宽度，高度60px） -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[60px] px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-3xl" />
          </div>
        </liquid-glass>

        <div class="flex-1"></div>

        <!-- 右：按钮组（药丸，图标为主，语言为文字，头像+昵称单独小药丸，整体高度60px） -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-1 h-full" containerClass="rounded-full h-[60px] px-4">
          <ripple-button pill :title="t('layout.common.toggleTheme') as string || '主题'" @click="uiStore.toggleDarkMode()">
            <sun-icon v-if="uiStore.isDarkMode" class="w-5 h-5" />
            <moon-icon v-else class="w-5 h-5" />
          </ripple-button>
          <span ref="themeBtnRef" class="inline-flex">
            <ripple-button pill :title="t('layout.common.themeFamily') as string || '主题家族'" @click="onToggleThemeMenu">
              <paint-brush-icon class="w-5 h-5" />
            </ripple-button>
          </span>
          <span ref="cursorBtnRef" class="inline-flex">
            <ripple-button pill :title="t('layout.common.cursorTrail') as string || '鼠标轨迹'" @click="onToggleCursorMenu">
              <sparkles-icon class="w-5 h-5" />
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
          <!-- 头像与昵称：使用 Ripple 风格的药丸 -->
          <span ref="userMenuBtn" class="inline-flex">
            <ripple-button pill class="pl-2 pr-3 h-11 items-center whitespace-nowrap" :title="t('layout.common.me') as string || '我'" @click="showUserMenu = !showUserMenu">
              <user-avatar :avatar="(authStore.user as any)?.avatar" :size="30">
                <div class="w-[30px] h-[30px] rounded-full bg-gray-200 dark:bg-gray-700"></div>
              </user-avatar>
              <span class="text-sm font-medium text-base-content whitespace-nowrap">{{ displayName }}</span>
            </ripple-button>
          </span>
        </liquid-glass>
      </div>
    </nav>

    <div class="flex pt-2 pb-20">
      <main class="flex-1">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <router-view />
        </div>
      </main>

      <!-- 全局聊天抽屉（如学生端也需要，可保留；否则可后续移除） -->
      <chat-drawer
        :open="chat.isOpen"
        :peer-id="chat.peerId as any"
        :peer-name="chat.peerName as any"
        :course-id="chat.courseId as any"
        @close="chat.closeChat()"
      />
    </div>

    <dock-bar
      :items="dockItems"
      v-model="activeDock"
      :bottom-offset="24"
      @select="onSelectDock"
    />

    <!-- 用户菜单弹层（模板内挂载） -->
    <teleport to="body">
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
            to="/student/profile"
            class="block px-4 py-2 text-sm text-subtle hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
            @click="showUserMenu = false"
          >
            <div class="flex items-center space-x-2">
              <user-icon class="h-4 w-4" />
              <span>{{ t('layout.student.user.profile') || '个人资料' }}</span>
            </div>
          </router-link>
          <router-link
            to="/student/help"
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
              <span>{{ t('layout.student.user.logout') || '退出登录' }}</span>
            </div>
          </button>
        </div>
      </liquid-glass>
    </teleport>

    <!-- 主题菜单弹层（与教师端一致） -->
    <teleport to="body">
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
          <div class="font-medium mb-1">{{ t('layout.common.themeSelectTitle') || '主题配色' }}</div>
          <div class="opacity-90">{{ t('layout.common.themeSelectDesc') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setTheme(uiStore.isDarkMode ? 'coffee' : 'cupcake')">
          <span>Cupcake + Coffee</span>
          <span v-if="uiStore.themeName==='cupcake' || uiStore.themeName==='coffee'" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setTheme(uiStore.isDarkMode ? 'dracula' : 'retro')">
          <span>{{ t('layout.common.themeOption.retroDracula') || 'Retro + Dracula' }}</span>
          <span v-if="uiStore.themeName==='retro' || uiStore.themeName==='dracula'" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setTheme(uiStore.isDarkMode ? 'dark' : 'light')">
          <span>Light + Dark</span>
          <span v-if="uiStore.themeName==='light' || uiStore.themeName==='dark'" class="text-theme-primary">✓</span>
        </button>
      </liquid-glass>
    </teleport>

    <!-- 鼠标轨迹菜单 -->
    <teleport to="body">
      <div v-if="showCursorMenu" class="fixed inset-0 z-[999]" @click="showCursorMenu = false"></div>
      <liquid-glass
        v-if="showCursorMenu"
        :style="cursorMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl"
        class="p-1"
        :radius="16"
        :frost="0.05"
        @click.stop
      >
        <div class="px-3 py-2 text-xs text-subtle">
          <div class="font-medium mb-1">{{ t('layout.common.cursorTrailTitle') || '鼠标轨迹' }}</div>
          <div class="opacity-90">{{ t('layout.common.cursorTrailDesc') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('off')">
          <span>{{ t('layout.common.cursorTrailOff') || '关闭' }}</span>
          <span v-if="uiStore.cursorTrailMode==='off'" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('fluid')">
          <span>{{ t('layout.common.cursorTrailFluid') || '流体光标' }}</span>
          <span v-if="uiStore.cursorTrailMode==='fluid'" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('smooth')">
          <span>{{ t('layout.common.cursorTrailSmooth') || '顺滑光标' }}</span>
          <span v-if="uiStore.cursorTrailMode==='smooth'" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('tailed')">
          <span>{{ t('layout.common.cursorTrailTailed') || '带尾迹光标' }}</span>
          <span v-if="uiStore.cursorTrailMode==='tailed'" class="text-theme-primary">✓</span>
        </button>
      </liquid-glass>
    </teleport>

    <cursor-trail-layer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/Button.vue'
import NotificationBell from '@/components/notifications/NotificationBell.vue'
import FuturisticBackground from '@/components/ui/FuturisticBackground.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import { useChatStore } from '@/stores/chat'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import DockBar from '@/components/ui/DockBar.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'
import {
  Bars3Icon,
  SunIcon,
  MoonIcon,
  ChevronDownIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  AcademicCapIcon,
  ClipboardDocumentListIcon,
  ClipboardDocumentCheckIcon,
  ChartBarIcon,
  ChatBubbleLeftRightIcon,
  SparklesIcon,
  BellIcon,
  EyeIcon,
  EyeSlashIcon,
  PaintBrushIcon,
  QuestionMarkCircleIcon,
} from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const chat = useChatStore()
const { t } = useI18n()

const baseBgStyle = computed(() => ({ background: 'var(--color-base-100)' }))

const showUserMenu = ref(false)
const userMenuBtn = ref<HTMLElement | null>(null)
const userMenuStyle = ref<Record<string, string>>({})
const showThemeMenu = ref(false)
const themeBtnRef = ref<HTMLElement | null>(null)
const themeMenuStyle = ref<Record<string, string>>({})
const showCursorMenu = ref(false)
const cursorBtnRef = ref<HTMLElement | null>(null)
const cursorMenuStyle = ref<Record<string, string>>({})

const displayName = computed(() => (authStore.user as any)?.nickname || (authStore.user as any)?.name || (authStore.user as any)?.username || t('layout.common.me') || 'Me')

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

onMounted(() => { try { window.addEventListener('ui:close-topbar-popovers', closeTopbarPopovers) } catch {} })
onUnmounted(() => { try { window.removeEventListener('ui:close-topbar-popovers', closeTopbarPopovers) } catch {} })

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
      zIndex: '1100'
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
      top: `${rect.bottom + 6}px`,
      left: `${Math.max(8, rect.right - 224)}px`,
      width: '14rem',
      zIndex: '1000'
    }
  } catch {}
})

watch(showCursorMenu, async (v: boolean) => {
  if (!v) return
  await nextTick()
  try {
    const el = cursorBtnRef.value as HTMLElement
    const rect = el.getBoundingClientRect()
    cursorMenuStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 6}px`,
      left: `${Math.max(8, rect.right - 220)}px`,
      width: '14rem',
      zIndex: '1000'
    }
  } catch {}
})

function setTheme(v: 'retro' | 'dracula' | 'light' | 'dark' | 'cupcake' | 'coffee') {
  uiStore.setTheme(v)
  showThemeMenu.value = false
}

function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
  uiStore.setCursorTrailMode(v)
  showCursorMenu.value = false
}

// setGlass 移除：旧主题强制 more，新主题强制 normal（由 store 控制）

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function emitCloseNotification() {
  try { window.dispatchEvent(new CustomEvent('ui:close-notification-dropdown')) } catch {}
}
function closeTopbarPopovers() {
  showThemeMenu.value = false
  showUserMenu.value = false
  showCursorMenu.value = false
}

function onToggleThemeMenu() {
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  showThemeMenu.value = !showThemeMenu.value
}

function onToggleCursorMenu() {
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  showCursorMenu.value = !showCursorMenu.value
}

// 主题切换刷新交由图表组件或页面内图表自行处理

// Dock 配置
const activeDock = computed<string>({
  get() {
    const p = router.currentRoute.value.path
    if (p.startsWith('/student/assignments')) return 'assignments'
    if (p.startsWith('/student/courses')) return 'courses'
    if (p.startsWith('/student/analysis')) return 'analysis'
    if (p.startsWith('/student/assistant')) return 'assistant'
    if (p.startsWith('/student/community')) return 'community'
    if (p.startsWith('/student/dashboard')) return 'dashboard'
    return ''
  },
  set(v: string) {}
})

const dockItems = computed(() => ([
  { key: 'dashboard', label: (t('layout.student.sidebar.dashboard') as string) || '工作台', icon: HomeIcon },
  { key: 'assignments', label: (t('layout.student.sidebar.assignments') as string) || '作业', icon: ClipboardDocumentListIcon },
  { key: 'courses', label: (t('layout.student.sidebar.courses') as string) || '课程', icon: AcademicCapIcon },
  { key: 'analysis', label: (t('layout.student.sidebar.analysis') as string) || '分析', icon: ChartBarIcon },
  { key: 'assistant', label: (t('layout.student.sidebar.ai') as string) || '助手', icon: SparklesIcon },
  { key: 'community', label: (t('layout.student.sidebar.community') as string) || '社区', icon: ChatBubbleLeftRightIcon },
]))

function onSelectDock(k: string) {
  const map: Record<string, string> = {
    dashboard: '/student/dashboard',
    assignments: '/student/assignments',
    courses: '/student/courses',
    analysis: '/student/analysis',
    assistant: '/student/assistant',
    community: '/student/community',
  }
  const to = map[k] || '/student/dashboard'
  if (router.currentRoute.value.path !== to) router.push(to)
}

function toggleChatDrawer(ev?: Event) {
  try { ev?.stopPropagation(); ev?.preventDefault() } catch {}
  try { console.debug('[StudentLayout] chat toggle clicked. isOpen(before)=', chat.isOpen) } catch {}
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  if (chat.isOpen) return chat.closeChat()
  chat.openChat()
  try { console.debug('[StudentLayout] chat toggled. isOpen(after)=', chat.isOpen) } catch {}
}

// 调试：观察 isOpen 变化
watch(() => chat.isOpen, (v: boolean) => { try { console.debug('[StudentLayout] chat.isOpen changed ->', v) } catch {} })

// 主题家族弹层（与教师端一致）
</script>

<style scoped>
/* 仅在学生端提高玻璃卡片的圆角，避免影响顶部导航与侧边栏等容器 */
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