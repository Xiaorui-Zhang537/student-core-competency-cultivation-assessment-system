<template>
  <div class="min-h-screen relative" :style="baseBgStyle">
    <div class="fixed inset-0 z-0 pointer-events-none" :style="blueVeilStyle"></div>
    <FuturisticBackground class="fixed inset-0 z-0 pointer-events-none" theme="auto" :intensity="0.18" :bits-density="0.8" :sweep-frequency="7" :parallax="true" :enable3D="true" :logo-glow="true" :emphasis="false" :interactions="{ mouseTrail: true, clickRipples: true }" :enabled="uiStore.bgEnabled" :respect-reduced-motion="true" />
    <!-- 顶部导航栏 -->
    <nav class="border-b border-gray-200 dark:border-gray-600 shadow-sm sticky top-0 z-40 glass-thin glass-interactive" v-glass="{ strength: 'thin', interactive: true }">
      <div class="px-4 sm:px-6 lg:px-12">
        <div class="flex justify-between h-14">
          <!-- 左侧：Logo和菜单切换 -->
          <div class="flex items-center space-x-6">
            <Button variant="glass" size="sm" @click="uiStore.toggleSidebar()">
              <template #icon>
                <Bars3Icon class="h-5 w-5" />
              </template>
            </Button>
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">
                {{ t('layout.teacher.title') }}
              </h1>
            </div>
          </div>

          <!-- 中间区域移除搜索框（预留占位以保持两侧布局） -->
          <div class="flex-1"></div>

          <!-- 右侧：主题切换、通知铃铛、用户菜单 -->
          <div class="ml-4 flex items-center md:ml-6 space-x-3">
            <!-- 主题切换 -->
            <Button variant="glass" size="sm" @click="uiStore.toggleDarkMode()">
              <template #icon>
                <SunIcon v-if="uiStore.isDarkMode" class="h-5 w-5" />
                <MoonIcon v-else class="h-5 w-5" />
              </template>
            </Button>

            <language-switcher />

            <!-- 玻璃强度切换 -->
            <div class="relative" @click.stop v-click-outside="() => (showGlassMenu=false)">
              <span ref="glassBtnRef">
                <Button variant="glass" size="sm" :title="t('layout.teacher.glass.title') || 'Glass Intensity'" @click="(showGlassMenu = !showGlassMenu, showBgMenu = false, emitCloseNotification())">
                  <template #icon>
                    <PaintBrushIcon class="h-5 w-5" />
                  </template>
                </Button>
              </span>
              <teleport to="body">
                <div v-if="showGlassMenu" class="fixed z-[1000] popover-glass border border-white/20 dark:border-white/12 shadow-md p-1 rounded-2xl"
                     :style="glassMenuStyle" @click.stop>
                  <div class="px-3 py-2 text-xs text-gray-600 dark:text-gray-300">
                    <div class="font-medium mb-1">{{ t('layout.teacher.glass.title') || 'Glass Intensity' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.glass.info.more') || 'More Transparent: lower opacity and lighter blur.' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.glass.info.normal') || 'Standard: balanced readability.' }}</div>
                  </div>
                  <div class="border-t border-white/10 my-1"></div>
                  <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setGlass('more')">
                    <span>{{ t('layout.teacher.glass.more') || 'More Transparent' }}</span>
                    <span v-if="uiStore.glassIntensity==='more'" class="text-primary-500">✓</span>
                  </button>
                  <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setGlass('normal')">
                    <span>{{ t('layout.teacher.glass.normal') || 'Standard' }}</span>
                    <span v-if="uiStore.glassIntensity==='normal'" class="text-primary-500">✓</span>
                  </button>
                </div>
              </teleport>
            </div>

            <div class="relative" @click.stop v-click-outside="() => (showBgMenu=false)">
              <span ref="bgBtnRef">
                <Button variant="glass" size="sm" :title="t('layout.teacher.bg.title') || 'Background Effects'" @click="(showBgMenu = !showBgMenu, showGlassMenu = false, emitCloseNotification())">
                  <template #icon>
                    <EyeIcon v-if="uiStore.bgEnabled" class="h-5 w-5" />
                    <EyeSlashIcon v-else class="h-5 w-5" />
                  </template>
                </Button>
              </span>
              <teleport to="body">
                <div v-if="showBgMenu" class="fixed z-[1000] popover-glass border border-white/20 dark:border-white/12 shadow-md p-1 rounded-2xl"
                     :style="bgMenuStyle" @click.stop>
                  <div class="px-3 py-2 text-xs text-gray-600 dark:text-gray-300">
                    <div class="font-medium mb-1">{{ t('layout.teacher.bg.title') || 'Background Effects' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.bg.info') || 'Toggle animated background and effects (smooth fade).' }}</div>
                  </div>
                  <div class="border-t border-white/10 my-1"></div>
                  <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setBg(true)">
                    <span>{{ t('layout.teacher.bg.on') || 'Enabled' }}</span>
                    <span v-if="uiStore.bgEnabled" class="text-primary-500">✓</span>
                  </button>
                  <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setBg(false)">
                    <span>{{ t('layout.teacher.bg.off') || 'Disabled' }}</span>
                    <span v-if="!uiStore.bgEnabled" class="text-primary-500">✓</span>
                  </button>
                </div>
              </teleport>
            </div>

            <!-- 通知铃铛 -->
            <NotificationBell />

            <!-- 全局聊天按钮：打开聊天抽屉（无选中人时显示列表） -->
            <Button variant="glass" size="sm" :title="t('shared.chat.open') as string || '聊天'" @click="chat.isOpen ? chat.closeChat() : chat.openChat()">
              <template #icon>
                <ChatBubbleLeftRightIcon class="h-5 w-5" />
              </template>
            </Button>

            <!-- 用户菜单 -->
            <div class="relative" v-click-outside="() => (showUserMenu=false)">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                ref="userMenuBtn"
              >
                <div class="flex items-center space-x-2">
                  <user-avatar :avatar="(authStore.user as any)?.avatar" :size="28">
                    <span class="text-white font-medium text-sm">{{ (authStore.user?.username || 'T').charAt(0).toUpperCase() }}</span>
                  </user-avatar>
                  <div class="hidden md:block text-left">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ (authStore.user as any)?.nickname || authStore.user?.username || t('layout.teacher.role') }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      {{ t('layout.teacher.role') }}
                    </p>
                  </div>
                  <chevron-down-icon class="h-4 w-4 text-gray-400" />
                </div>
              </button>

              <!-- 用户下拉菜单 -->
              <teleport to="body">
                <div
                  v-if="showUserMenu"
                  class="rounded-xl shadow-lg glass-thin"
                  v-glass="{ strength: 'thin', interactive: false }"
                  :style="userMenuStyle"
                  @click.stop
                >
                  <div class="py-1">
                    <router-link
                      to="/teacher/profile"
                      class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
                      @click="showUserMenu = false"
                    >
                      <div class="flex items-center space-x-2">
                        <user-icon class="h-4 w-4" />
                        <span>{{ t('layout.teacher.user.profile') }}</span>
                      </div>
                    </router-link>
                    <router-link
                      to="/teacher/help"
                      class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
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
                </div>
              </teleport>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <div class="flex pt-14 relative z-10">
      <!-- 侧边栏 -->
      <aside
        v-glass="{ strength: 'regular', interactive: true }"
        :class="[
          'fixed inset-y-0 left-0 z-30 w-64 glass-regular glass-interactive border-r border-gray-200/40 dark:border-gray-600/40 transform transition-transform duration-300 ease-in-out',
          uiStore.sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        ]"
        style="top: 3.5rem;"
      >
        <div class="h-full px-3 py-4 overflow-y-auto">
          <nav class="space-y-2">
            <router-link
              to="/teacher/dashboard"
              exact-active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <home-icon class="mr-3 h-5 w-5" />
              {{ t('layout.teacher.sidebar.dashboard') }}
            </router-link>
            
            <router-link
              to="/teacher/courses"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <academic-cap-icon class="mr-3 h-5 w-5" />
              {{ t('layout.teacher.sidebar.courses') }}
            </router-link>
            
            <router-link
              to="/teacher/analytics"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <chart-bar-icon class="mr-3 h-5 w-5" />
              {{ t('layout.teacher.sidebar.analytics') }}
            </router-link>
            
            <router-link
              to="/teacher/assistant"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <users-icon class="mr-3 h-5 w-5" />
              {{ t('layout.teacher.sidebar.ai') }}
            </router-link>
            
             <router-link
              to="/teacher/community"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <chat-bubble-left-right-icon class="mr-3 h-5 w-5" />
              {{ t('layout.teacher.sidebar.community') }}
            </router-link>
            
          </nav>
        </div>
      </aside>

      <!-- 遮罩层 (移动端) -->
      <div
        v-if="uiStore.sidebarOpen"
        class="fixed inset-0 z-20 bg-gray-600 bg-opacity-75 lg:hidden"
        @click="uiStore.closeSidebar()"
      ></div>

      <!-- 主要内容 -->
      <main :class="['flex-1', uiStore.sidebarOpen ? 'lg:pl-64' : 'pl-0']">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-0 pb-6">
          <router-view />
        </div>
      </main>
      
      <!-- 全局聊天抽屉：在布局层挂载，保证任意页面都能打开 -->
      <ChatDrawer
        v-if="chat.isOpen"
        :open="chat.isOpen"
        :peer-id="chat.peerId as any"
        :peer-name="chat.peerName as any"
        :course-id="chat.courseId as any"
        @close="chat.closeChat()"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/Button.vue'
import FuturisticBackground from '@/components/ui/FuturisticBackground.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import NotificationBell from '@/components/notifications/NotificationBell.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
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
  EyeIcon,
  EyeSlashIcon,
  QuestionMarkCircleIcon,
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const chat = useChatStore()
const { t } = useI18n()

// 状态
const htmlIsDark = ref<boolean>(document.documentElement.classList.contains('dark'))
const baseBgStyle = computed(() => {
  return htmlIsDark.value
    ? { background: 'linear-gradient(160deg, #060B1A 0%, #0E1A39 60%, #060B1A 100%)' }
    : { background: 'linear-gradient(140deg, #F9FBFF 0%, #EAF3FF 45%, #E6F7FD 100%)' }
})
const blueVeilStyle = computed(() => {
  return htmlIsDark.value
    ? {
        backgroundImage: [
          'radial-gradient(circle at 30% 30%, rgba(139,92,246,0.06) 0%, rgba(0,0,0,0) 65%)',
          'radial-gradient(circle at 50% 40%, rgba(12,20,40,0.35) 0%, rgba(0,0,0,0) 80%)'
        ].join(', ')
      }
    : {
        backgroundImage: [
          'radial-gradient(circle at 30% 30%, rgba(124,58,237,0.05) 0%, rgba(0,0,0,0) 65%)',
          'radial-gradient(circle at 60% 25%, rgba(0,0,0,0.08) 0%, rgba(0,0,0,0) 70%)'
        ].join(', ')
      }
})
const showUserMenu = ref(false)
const userMenuBtn = ref<HTMLElement | null>(null)
const userMenuStyle = ref<Record<string, string>>({})
const showGlassMenu = ref(false)
const glassBtnRef = ref<HTMLElement | null>(null)
const glassMenuStyle = ref<Record<string, string>>({})
const showBgMenu = ref(false)
const bgBtnRef = ref<HTMLElement | null>(null)
const bgMenuStyle = ref<Record<string, string>>({})

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

// 生命周期
let darkMo: MutationObserver | null = null
onMounted(() => {
  uiStore.initBackgroundEnabled()
  uiStore.initGlassIntensity()
  try {
    darkMo = new MutationObserver(() => {
      htmlIsDark.value = document.documentElement.classList.contains('dark')
    })
    darkMo.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
  } catch {}
})
onUnmounted(() => { try { darkMo?.disconnect() } catch {} darkMo = null })

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

watch(showGlassMenu, async (v: boolean) => {
  if (!v) return
  await nextTick()
  try {
    const el = glassBtnRef.value as HTMLElement
    const rect = el.getBoundingClientRect()
    glassMenuStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 6}px`,
      left: `${Math.max(8, rect.right - 220)}px`,
      width: '14rem',
      zIndex: '1000'
    }
  } catch {}
})

function setGlass(v: 'normal' | 'more') {
  uiStore.setGlassIntensity(v)
  showGlassMenu.value = false
}

watch(showBgMenu, async (v: boolean) => {
  if (!v) return
  await nextTick()
  try {
    const el = bgBtnRef.value as HTMLElement
    const rect = el.getBoundingClientRect()
    bgMenuStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 6}px`,
      left: `${Math.max(8, rect.right - 240)}px`,
      width: '15rem',
      zIndex: '1000'
    }
  } catch {}
})

function setBg(v: boolean) {
  uiStore.bgEnabled = v
  showBgMenu.value = false
}

function emitCloseNotification() {
  try { window.dispatchEvent(new CustomEvent('ui:close-notification-dropdown')) } catch {}
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
