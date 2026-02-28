<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <background-layer />
    <!-- 顶部导航栏 -->
    <nav class="sticky top-0 z-40 px-6 pt-6 pb-6">
      <div class="flex items-center gap-3">
        <!-- 左：标题（药丸，高度60px，i18n） -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[60px] px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('admin.title') as string" :sparklesCount="18" class="text-3xl" />
          </div>
        </liquid-glass>

        <div class="flex-1"></div>

        <!-- 右：Dock（复用通用逻辑） -->
        <liquid-glass :radius="30" class="flex items-center justify-center h-full" containerClass="rounded-full h-[60px] px-2">
          <dock :magnification="0" :distance="0" :animate="false" variant="transparent" paddingClass="pl-1.5 pr-5" heightClass="h-[56px]" roundedClass="rounded-full" gapClass="gap-3">
            <dock-icon :animate="false">
              <glass-tooltip :content="(isDockVisible ? (t('layout.common.hideDock') as string) : (t('layout.common.showDock') as string)) || '切换 Dock'">
                <ripple-button
                  pill
                  :duration="0"
                  :aria-label="(isDockVisible ? (t('layout.common.hideDock') as string) : (t('layout.common.showDock') as string)) || '切换 Dock'"
                  :class="topbarBtnClass"
                  @click="isDockVisible = !isDockVisible"
                >
                  <eye-icon v-if="isDockVisible" class="w-5 h-5" />
                  <eye-slash-icon v-else class="w-5 h-5" />
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false">
              <glass-tooltip :content="(t('layout.common.toggleTheme') as string) || '主题'">
                <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="(t('layout.common.toggleTheme') as string) || '主题'" @click="uiStore.toggleDarkMode()">
                  <sun-icon v-if="uiStore.isDarkMode" class="w-5 h-5" />
                  <moon-icon v-else class="w-5 h-5" />
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false">
              <span ref="bgBtnRef" class="inline-flex">
                <glass-tooltip :content="(t('layout.common.bgPickerTitle') as string) || '背景'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="(t('layout.common.bgPickerTitle') as string) || '背景'" @click="onToggleBgPicker">
                    <photo-icon class="w-5 h-5" />
                  </ripple-button>
                </glass-tooltip>
              </span>
            </dock-icon>
            <dock-icon :animate="false">
              <span ref="themeBtnRef" class="inline-flex">
                <glass-tooltip :content="(t('layout.common.themeFamily') as string) || '主题家族'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="(t('layout.common.themeFamily') as string) || '主题家族'" @click="onToggleThemeMenu">
                    <paint-brush-icon class="w-5 h-5" />
                  </ripple-button>
                </glass-tooltip>
              </span>
            </dock-icon>
            <dock-icon :animate="false">
              <span ref="cursorBtnRef" class="inline-flex">
                <glass-tooltip :content="(t('layout.common.cursorTrail') as string) || '鼠标轨迹'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="(t('layout.common.cursorTrail') as string) || '鼠标轨迹'" @click="onToggleCursorMenu">
                    <cursor-arrow-rays-icon class="w-5 h-5" />
                  </ripple-button>
                </glass-tooltip>
              </span>
            </dock-icon>
            <dock-icon class="-ml-2" :animate="false">
              <glass-tooltip :content="(t('layout.common.language') as string) || '语言'">
                <language-switcher buttonClass="px-3 h-10 flex items-center rounded-full min-w-[56px] whitespace-nowrap" />
              </glass-tooltip>
            </dock-icon>

            <dock-icon :animate="false">
              <notification-bell>
                <template #trigger="{ toggle }">
                  <glass-tooltip :content="t('notifications.title') as string">
                    <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="t('notifications.title') as string" @click="toggle">
                      <bell-icon class="w-5 h-5" />
                    </ripple-button>
                  </glass-tooltip>
                </template>
              </notification-bell>
            </dock-icon>
            <dock-icon :animate="false">
              <span class="relative inline-flex">
                <glass-tooltip :content="(t('shared.chat.open') as string) || '聊天'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :aria-label="(t('shared.chat.open') as string) || '聊天'" @click.stop="toggleChatDrawer($event)">
                    <chat-bubble-left-right-icon class="w-5 h-5" />
                  </ripple-button>
                </glass-tooltip>
                <span v-if="chat.totalUnread > 0 && !chat.isOpen" class="absolute -top-0.5 -right-0.5 glass-badge glass-badge-xs text-[10px] leading-none px-[6px]">{{ Math.min(chat.totalUnread as any, 99) }}</span>
              </span>
            </dock-icon>

            <dock-icon :square="false" :baseSize="56" :ml="8" :animate="false">
              <span ref="userMenuBtn" class="inline-flex">
                <glass-tooltip :content="(t('layout.common.me') as string) || '我'">
                  <ripple-button pill class="pl-2 pr-4 h-full items-center gap-2 whitespace-nowrap" :duration="0" :class="topbarBtnClass" :aria-label="(t('layout.common.me') as string) || '我'" @click="onToggleUserMenu">
                    <user-avatar :avatar="(authStore.user as any)?.avatar" :size="30">
                      <div class="w-[30px] h-[30px] rounded-full bg-gray-200 dark:bg-gray-700 flex-shrink-0"></div>
                    </user-avatar>
                    <span class="text-sm font-medium text-base-content whitespace-nowrap">{{ displayName }}</span>
                  </ripple-button>
                </glass-tooltip>
              </span>
            </dock-icon>
          </dock>
        </liquid-glass>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <div class="flex pt-2 pb-20 relative z-10">
      <main class="flex-1">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-0 pb-6">
          <router-view />
        </div>
      </main>

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
      :visible="isDockVisible"
      @select="onSelectDock"
    />

    <!-- 用户菜单弹层 -->
    <teleport to="body">
      <div v-if="showThemeMenu" class="fixed inset-0 z-[999]" @click="showThemeMenu = false"></div>
      <liquid-glass
        v-if="showThemeMenu"
        effect="occlusionBlur"
        :style="themeMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl border border-white/20 dark:border-white/12 overflow-hidden shadow-lg"
        class="p-1"
        :radius="16"
        :frost="0.14"
        :alpha="0.96"
        :blur="14"
        :tint="false"
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

      <div v-if="showBgPicker" class="fixed inset-0 z-[999]" @click="showBgPicker = false"></div>
      <liquid-glass
        v-if="showBgPicker"
        effect="occlusionBlur"
        :style="bgMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl border border-white/20 dark:border-white/12 overflow-hidden shadow-lg"
        class="p-1"
        :radius="16"
        :frost="0.14"
        :alpha="0.96"
        :blur="14"
        :tint="false"
        @click.stop
      >
        <div class="px-3 py-2 text-xs text-subtle">
          <div class="font-medium mb-1">{{ t('layout.common.bgPickerTitle') || '背景与主题' }}</div>
          <div class="opacity-90">{{ t('layout.common.bgPickerDesc') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <div class="px-2 py-2">
          <div class="text-xs text-subtle mb-2">{{ t('layout.common.lightMode') || '明亮模式' }}</div>
          <div class="flex flex-col gap-2">
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('none')">
              <span>{{ t('layout.common.bg.none') || '无' }}</span>
              <span v-if="uiStore.backgroundLight==='none'" class="text-theme-primary">✓</span>
            </button>
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('aurora')">
              <span>{{ t('layout.common.bg.aurora') || '极光' }}</span>
              <span v-if="uiStore.backgroundLight==='aurora'" class="text-theme-primary">✓</span>
            </button>
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('tetris')">
              <span>{{ t('layout.common.bg.tetris') || '俄罗斯方块' }}</span>
              <span v-if="uiStore.backgroundLight==='tetris'" class="text-theme-primary">✓</span>
            </button>
          </div>
        </div>
        <div class="px-2 py-2 pt-0">
          <div class="text-xs text-subtle mb-2">{{ t('layout.common.darkMode') || '暗黑模式' }}</div>
          <div class="flex flex-col gap-2">
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('none')">
              <span>{{ t('layout.common.bg.none') || '无' }}</span>
              <span v-if="uiStore.backgroundDark==='none'" class="text-theme-primary">✓</span>
            </button>
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('neural')">
              <span>{{ t('layout.common.bg.neural') || '神经网络' }}</span>
              <span v-if="uiStore.backgroundDark==='neural'" class="text-theme-primary">✓</span>
            </button>
            <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('meteors')">
              <span>{{ t('layout.common.bg.meteors') || '流星' }}</span>
              <span v-if="uiStore.backgroundDark==='meteors'" class="text-theme-primary">✓</span>
            </button>
          </div>
        </div>
      </liquid-glass>

      <div v-if="showUserMenu" class="fixed inset-0 z-[999]" @click="showUserMenu = false"></div>
      <liquid-glass
        v-if="showUserMenu"
        effect="occlusionBlur"
        :style="userMenuStyle"
        containerClass="fixed z-[1000] rounded-xl shadow-lg border border-white/20 dark:border-white/12 overflow-hidden"
        :radius="16"
        :frost="0.14"
        :alpha="0.96"
        :blur="14"
        :tint="false"
        @click.stop
      >
        <div class="py-1">
          <router-link
            to="/admin/profile"
            class="block px-4 py-2 text-sm text-subtle hover:bg-gray-100/50 dark:hover:bg-gray-700/50 rounded-lg"
            @click="showUserMenu = false"
          >
            <div class="flex items-center space-x-2">
              <user-icon class="h-4 w-4" />
              <span>{{ t('layout.teacher.user.profile') }}</span>
            </div>
          </router-link>
          <router-link
            to="/admin/help"
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

    <cursor-trail-layer />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import NotificationBell from '@/components/notifications/NotificationBell.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import DockBar from '@/components/ui/DockBar.vue'
import Dock from '@/components/ui/inspira/Dock.vue'
import DockIcon from '@/components/ui/inspira/DockIcon.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'
import BackgroundLayer from '@/components/ui/BackgroundLayer.vue'
import GlassTooltip from '@/components/ui/GlassTooltip.vue'
import { useLayoutCommon } from './useLayoutCommon'
import { loadLocaleMessages } from '@/i18n'
import { useLocale } from '@/i18n/useLocale'
import {
  SunIcon, MoonIcon, PaintBrushIcon, PhotoIcon,
  UserIcon, ArrowRightOnRectangleIcon,
  HomeIcon, AcademicCapIcon, UsersIcon,
  ArrowDownTrayIcon, ShieldCheckIcon,
  ChatBubbleLeftRightIcon, QuestionMarkCircleIcon,
  BellIcon, CursorArrowRaysIcon, EyeIcon, EyeSlashIcon,
} from '@heroicons/vue/24/outline'

const {
  uiStore, authStore, chat, t,
  baseBgStyle, isDockVisible,
  showUserMenu, userMenuBtn, userMenuStyle,
  showThemeMenu, themeBtnRef, themeMenuStyle,
  showCursorMenu, cursorBtnRef, cursorMenuStyle,
  showBgPicker, bgBtnRef, bgMenuStyle,
  displayName, topbarBtnClass,
  activeDock,
  handleLogout,
  setTheme, setCursor, setLight, setDark,
  onToggleThemeMenu, onToggleCursorMenu, onToggleBgPicker, onToggleUserMenu,
  toggleChatDrawer,
  onSelectDock,
} = useLayoutCommon({
  rolePrefix: 'admin',
  dockItems: [],
  dockRoutes: {
    console: '/admin/console',
    people: '/admin/people',
    moderation: '/admin/moderation',
    courses: '/admin/courses',
    tools: '/admin/tools',
  },
})

// 进入管理员布局时确保 admin 命名空间已加载（兼容热更新/首次进入 admin 路由时缺键警告）
const { locale } = useLocale()
onMounted(async () => {
  await loadLocaleMessages(locale.value as any, ['admin'])
})
watch(locale, async (l) => {
  await loadLocaleMessages(l as any, ['admin'])
})

const dockItems = computed(() => ([
  { key: 'console', label: t('admin.sidebar.console') as string, icon: HomeIcon },
  { key: 'people', label: t('admin.sidebar.people') as string, icon: UsersIcon },
  { key: 'moderation', label: t('admin.sidebar.moderation') as string, icon: ShieldCheckIcon },
  { key: 'courses', label: t('admin.sidebar.courses') as string, icon: AcademicCapIcon },
  { key: 'tools', label: t('admin.sidebar.tools') as string, icon: ArrowDownTrayIcon },
]))
</script>

<style scoped>
/* 与学生/教师端保持一致的圆角风格 */
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

