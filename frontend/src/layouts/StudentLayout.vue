<template>
  <div class="min-h-screen relative bg-gray-50 dark:bg-gray-900">
    <FuturisticBackground class="fixed inset-0 z-0 pointer-events-none" theme="auto" :intensity="0.18" :bits-density="0.8" :sweep-frequency="7" :parallax="true" :enable3D="true" :logo-glow="true" :emphasis="false" :interactions="{ mouseTrail: true, clickRipples: true }" :enabled="uiStore.bgEnabled" :respect-reduced-motion="true" />
    <nav class="sticky top-0 z-40 glass-thin glass-interactive" v-glass="{ strength: 'thin', interactive: true }">
      <div class="px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-14">
          <div class="flex items-center space-x-6">
            <button
              @click="uiStore.toggleSidebar()"
              class="p-2 -ml-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <bars3-icon class="h-6 w-6" />
            </button>
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">{{ t('app.title') }}</h1>
            </div>
          </div>

          

          <div class="ml-4 flex items-center md:ml-6 space-x-3">
            <button
              @click="uiStore.toggleDarkMode()"
              class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <sun-icon v-if="uiStore.isDarkMode" class="h-6 w-6" />
              <moon-icon v-else class="h-6 w-6" />
            </button>

            <language-switcher />

            <div class="relative" @click.stop v-click-outside="() => (showGlassMenu=false)">
              <button
                class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                :title="t('layout.teacher.glass.title') || 'Glass Intensity'"
                @click="showGlassMenu = !showGlassMenu"
                ref="glassBtnRef"
              >
                <paint-brush-icon class="h-6 w-6" />
              </button>
              <teleport to="body">
                <div v-if="showGlassMenu" class="fixed z-[1000] popover-glass border border-white/20 dark:border-white/12 shadow-md p-1 rounded-lg"
                     :style="glassMenuStyle" @click.stop>
                  <div class="px-3 py-2 text-xs text-gray-600 dark:text-gray-300">
                    <div class="font-medium mb-1">{{ t('layout.teacher.glass.title') || 'Glass Intensity' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.glass.info.more') || 'More Transparent: lower opacity and lighter blur.' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.glass.info.normal') || 'Standard: balanced readability.' }}</div>
                  </div>
                  <div class="border-t border-white/10 my-1"></div>
                  <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setGlass('more')">
                    <span>{{ t('layout.teacher.glass.more') || 'More Transparent' }}</span>
                    <span v-if="uiStore.glassIntensity==='more'" class="text-primary-500">✓</span>
                  </button>
                  <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setGlass('normal')">
                    <span>{{ t('layout.teacher.glass.normal') || 'Standard' }}</span>
                    <span v-if="uiStore.glassIntensity==='normal'" class="text-primary-500">✓</span>
                  </button>
                </div>
              </teleport>
            </div>

            <div class="relative" @click.stop v-click-outside="() => (showBgMenu=false)">
              <button
                class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                :title="t('layout.teacher.bg.title') || 'Background Effects'"
                @click="showBgMenu = !showBgMenu"
                ref="bgBtnRef"
              >
                <eye-icon v-if="uiStore.bgEnabled" class="h-6 w-6" />
                <eye-slash-icon v-else class="h-6 w-6" />
              </button>
              <teleport to="body">
                <div v-if="showBgMenu" class="fixed z-[1000] popover-glass border border-white/20 dark:border-white/12 shadow-md p-1 rounded-lg"
                     :style="bgMenuStyle" @click.stop>
                  <div class="px-3 py-2 text-xs text-gray-600 dark:text-gray-300">
                    <div class="font-medium mb-1">{{ t('layout.teacher.bg.title') || 'Background Effects' }}</div>
                    <div class="opacity-90">{{ t('layout.teacher.bg.info') || 'Toggle animated background and effects (smooth fade).' }}</div>
                  </div>
                  <div class="border-t border-white/10 my-1"></div>
                  <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setBg(true)">
                    <span>{{ t('layout.teacher.bg.on') || 'Enabled' }}</span>
                    <span v-if="uiStore.bgEnabled" class="text-primary-500">✓</span>
                  </button>
                  <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between"
                          @click="setBg(false)">
                    <span>{{ t('layout.teacher.bg.off') || 'Disabled' }}</span>
                    <span v-if="!uiStore.bgEnabled" class="text-primary-500">✓</span>
                  </button>
                </div>
              </teleport>
            </div>

            <!-- 通知铃铛 -->
            <notification-bell />

            <!-- 聊天开关按钮（学生端与教师端一致） -->
            <button
              @click="chat.isOpen ? chat.closeChat() : chat.openChat()"
              class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              :title="t('shared.chat.open') as string || '聊天'"
            >
              <ChatBubbleLeftRightIcon class="h-6 w-6" />
            </button>

              <div class="relative" v-click-outside="() => (showUserMenu=false)">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                ref="userMenuBtn"
              >
                <user-avatar :avatar="(authStore.user as any)?.avatar" :size="32">
                  <div class="h-8 w-8 rounded-full bg-gradient-to-r from-primary-500 to-purple-600 flex items-center justify-center text-white font-medium text-sm">
                    {{ (authStore.user?.username || 'S').charAt(0).toUpperCase() }}
                  </div>
                </user-avatar>
                <div class="hidden md:block text-left ml-2">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ (authStore.user as any)?.nickname || authStore.user?.username || (t('layout.student.title') as string || '学生') }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('layout.student.role') || '学生' }}</p>
                </div>
                <chevron-down-icon class="h-4 w-4 text-gray-400 ml-1" />
              </button>

              <teleport to="body">
                <div
                  v-if="showUserMenu"
                  class="origin-top-right w-48 rounded-xl shadow-lg glass-thin focus:outline-none"
                  v-glass="{ strength: 'thin', interactive: false }"
                  :style="userMenuStyle"
                  @click.stop
                >
                  <div class="py-1">
                    <router-link
                      to="/student/profile"
                      class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
                      @click="showUserMenu = false"
                    >
                      <user-icon class="h-4 w-4 inline-block mr-2" /> {{ t('layout.student.user.profile') || '个人资料' }}
                    </router-link>
                    <div class="border-t border-gray-100 dark:border-gray-600"></div>
                    <button
                      @click="handleLogout"
                      class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
                    >
                      <arrow-right-on-rectangle-icon class="h-4 w-4 inline-block mr-2" /> {{ t('layout.student.user.logout') || '退出登录' }}
                    </button>
                  </div>
                </div>
              </teleport>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <div class="flex pt-14">
      <aside
        v-glass="{ strength: 'regular', interactive: true }"
        :class="[
          'fixed inset-y-0 left-0 z-30 w-64 glass-regular glass-interactive transform transition-transform duration-300 ease-in-out',
          uiStore.sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        ]"
        style="top: 3.5rem;"
      >
        <div class="h-full px-3 py-4 overflow-y-auto">
          <nav class="space-y-2">
            <!-- 主页从侧边栏移除：独立顶层页面，不显示在左菜单 -->
            <router-link
              to="/student/dashboard"
              exact-active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <home-icon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.dashboard') || '工作台' }}
            </router-link>
            <router-link
              to="/student/assignments"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <clipboard-document-list-icon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.assignments') || '我的作业' }}
            </router-link>
            <router-link
              to="/student/courses"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <academic-cap-icon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.courses') || '我的课程' }}
            </router-link>
            <router-link
              to="/student/analysis"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <ChartBarIcon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.analysis') || '成绩分析' }}
            </router-link>
            <router-link
              to="/student/assistant"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <SparklesIcon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.ai') || 'AI 助手' }}
            </router-link>
            <router-link
              to="/student/community"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <ChatBubbleLeftRightIcon class="mr-3 h-5 w-5" /> {{ t('layout.student.sidebar.community') || '学习社区' }}
            </router-link>
            <!-- 成绩菜单合并到成绩分析页，移除此项 -->
          </nav>
        </div>
      </aside>

      <div
        v-if="uiStore.sidebarOpen"
        class="fixed inset-0 z-20 lg:hidden"
        v-glass="{ strength: 'thick', interactive: false }"
        @click="uiStore.closeSidebar()"
      ></div>

      <main :class="['flex-1', uiStore.sidebarOpen ? 'lg:pl-64' : 'pl-0']">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <router-view />
        </div>
      </main>

      <!-- 全局聊天抽屉（如学生端也需要，可保留；否则可后续移除） -->
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
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/notifications/NotificationBell.vue'
import FuturisticBackground from '@/components/ui/FuturisticBackground.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import { useChatStore } from '@/stores/chat'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
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
  EyeIcon,
  EyeSlashIcon,
  PaintBrushIcon,
} from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const chat = useChatStore()
const { t } = useI18n()

const showUserMenu = ref(false)
const showGlassMenu = ref(false)
const glassBtnRef = ref<HTMLElement | null>(null)
const glassMenuStyle = ref<Record<string, string>>({})
const userMenuBtn = ref<HTMLElement | null>(null)
const userMenuStyle = ref<Record<string, string>>({})
const showBgMenu = ref(false)
const bgBtnRef = ref<HTMLElement | null>(null)
const bgMenuStyle = ref<Record<string, string>>({})

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

onMounted(() => {
  uiStore.initBackgroundEnabled()
  uiStore.initGlassIntensity()
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
      zIndex: '1100'
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

function setGlass(v: 'normal' | 'more') {
  uiStore.setGlassIntensity(v)
  showGlassMenu.value = false
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}
</script>