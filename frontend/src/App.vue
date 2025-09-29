<template>
  <div id="app" class="min-h-screen transition-colors duration-300">
    <!-- 全局加载状态 -->
    <div 
      v-if="globalLoading" 
      class="fixed inset-0 bg-white/90 dark:bg-gray-900/90 backdrop-blur-sm z-50 flex items-center justify-center"
    >
      <div class="text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-primary-100 dark:bg-primary-900/30 rounded-2xl mb-4">
          <div class="w-8 h-8 border-3 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-1">{{ t('app.loading.title') }}</h3>
        <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('app.loading.subtitle') }}</p>
      </div>
    </div>

    <!-- 路由加载进度条 -->
    <div 
      v-if="routeLoading"
      class="fixed top-0 left-0 right-0 z-40"
    >
      <div class="h-1 bg-gradient-to-r from-primary-500 to-accent-500 animate-pulse"></div>
    </div>

    <!-- 主要内容区域 -->
    <router-view>
      <template #default="{ Component, route }">
        <transition 
          name="page" 
          mode="out-in"
          @before-enter="onBeforeEnter"
          @after-enter="onAfterEnter"
        >
          <component :is="Component" :key="route.path" />
        </transition>
      </template>
    </router-view>

    <!-- 全局通知系统（Animated List + 玻璃 + 主题色） -->
    <div class="fixed top-4 right-4 z-50 w-auto max-w-[90vw] sm:max-w-[560px]">
      <AnimatedList :items="notifications" :delay="120" :reverse="true">
        <template #item="{ item }">
          <div
            class="rounded-full pointer-events-auto overflow-hidden glass-thin glass-interactive border border-white/20 dark:border-white/12 shadow-md"
            :class="tintClass(item.type)"
            v-glass="{ strength: 'thin', interactive: true }"
          >
            <div class="p-4 sm:p-5 flex items-center">
              <div class="flex-shrink-0 w-7 h-7 mr-4 flex items-center justify-start">
                <CheckCircleSolid v-if="item.type === 'success'" class="h-7 w-7" :style="iconStyle('success')" />
                <ExclamationTriangleSolid v-else-if="item.type === 'warning'" class="h-7 w-7" :style="iconStyle('warning')" />
                <XCircleSolid v-else-if="item.type === 'error'" class="h-7 w-7" :style="iconStyle('error')" />
                <InformationCircleSolid v-else class="h-7 w-7" :style="iconStyle('info')" />
              </div>
              <div class="min-w-0 flex-1">
                <p class="text-[15px] sm:text-[17px] font-semibold break-words whitespace-normal">{{ item.title }}</p>
                <p v-if="item.message" class="mt-1 text-[14px] sm:text-[15px] break-words whitespace-normal leading-6 opacity-90">{{ item.message }}</p>
              </div>
              <div class="ml-3 flex-shrink-0 flex">
                <button size="sm" variant="glass" icon="close" @click="uiStore.removeNotification(item.id)" />
              </div>
            </div>
          </div>
        </template>
      </AnimatedList>
    </div>

    <!-- 全局错误边界弹窗 -->
    <div
      v-if="showErrorModal"
      class="fixed inset-0 bg-transparent flex items-center justify-center z-50 p-4"
      @click.self="showErrorModal = false"
    >
      <div class="modal glass-thick max-w-md w-full p-6" v-glass="{ strength: 'thick' }">
        <div class="flex items-center mb-4">
          <x-circle-icon class="w-8 h-8 text-red-500 mr-3" />
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('app.error.title') }}</h3>
        </div>
        <p class="text-gray-600 dark:text-gray-400 mb-6">
          {{ t('app.error.description') }}
        </p>
        <div class="flex space-x-3">
          <button class="flex-1" variant="primary" icon="confirm" @click="reloadPage">{{ t('app.error.button.reload') }}</button>
          <button class="flex-1" variant="secondary" icon="close" @click="showErrorModal = false">{{ t('app.error.button.close') }}</button>
        </div>
      </div>
    </div>

    <!-- 网络状态提示 -->
    <div
      v-if="!isOnline"
      class="fixed bottom-4 left-4 right-4 bg-orange-100 dark:bg-orange-900/30 border border-orange-200 dark:border-orange-800 rounded-lg p-4 z-40"
    >
      <div class="flex items-center">
        <wifi-icon class="w-5 h-5 text-orange-500 mr-3" />
        <div class="flex-1">
          <p class="text-sm font-medium text-orange-800 dark:text-orange-200">{{ t('app.network.offline.title') }}</p>
          <p class="text-xs text-orange-600 dark:text-orange-300">{{ t('app.network.offline.description') }}</p>
        </div>
      </div>
    </div>

    <!-- 开发模式指示器 -->
    <div
      v-if="isDevelopment"
      class="fixed bottom-4 left-4 bg-blue-100 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800 rounded-lg px-3 py-1 z-30"
    >
      <p class="text-xs font-medium text-blue-800 dark:text-blue-200">{{ t('app.devMode') }}</p>
    </div>
    
    <!-- 全局挂载聊天抽屉（仅登录后显示） -->
    <chat-drawer
      v-if="authStore.isAuthenticated"
      :open="chat.isOpen"
      :peer-id="chat.peerId as any"
      :peer-name="chat.peerName as any"
      :course-id="chat.courseId as any"
      @close="chat.closeChat()"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import AnimatedList from '@/components/ui/AnimatedList.vue'
import Button from '@/components/ui/Button.vue'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  XCircleIcon,
  InformationCircleIcon,
  XMarkIcon,
  WifiIcon
} from '@heroicons/vue/24/outline'
import {
  CheckCircleIcon as CheckCircleSolid,
  ExclamationTriangleIcon as ExclamationTriangleSolid,
  XCircleIcon as XCircleSolid,
  InformationCircleIcon as InformationCircleSolid
} from '@heroicons/vue/24/solid'

// Stores
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const chat = useChatStore()
const { notifications, themeName } = storeToRefs(uiStore)
// 读取主题主色和强调色，保证与背景对比
const themePrimary = ref<string>('')
const themeAccent = ref<string>('')
const themeBg = ref<string>('')

function parseColor(input: string): { r: number; g: number; b: number } | null {
  const v = (input || '').trim()
  if (!v) return null
  // hex #rrggbb
  const hex = v.startsWith('#') ? v.slice(1) : (v.startsWith('0x') ? v.slice(2) : '')
  if (hex && (hex.length === 6 || hex.length === 3)) {
    const h = hex.length === 3 ? hex.split('').map(c => c + c).join('') : hex
    const r = parseInt(h.slice(0, 2), 16)
    const g = parseInt(h.slice(2, 4), 16)
    const b = parseInt(h.slice(4, 6), 16)
    return { r, g, b }
  }
  // rgb(r,g,b)
  const m = v.match(/rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)/i)
  if (m) return { r: Number(m[1]), g: Number(m[2]), b: Number(m[3]) }
  return null
}

function relativeLuminance({ r, g, b }: { r: number; g: number; b: number }): number {
  const srgb = [r, g, b].map(v => v / 255).map(v => (v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4)))
  return 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2]
}

function contrastRatio(c1: string, c2: string): number {
  const a = parseColor(c1)
  const b = parseColor(c2)
  if (!a || !b) return 1
  const L1 = relativeLuminance(a)
  const L2 = relativeLuminance(b)
  const [hi, lo] = L1 >= L2 ? [L1, L2] : [L2, L1]
  return (hi + 0.05) / (lo + 0.05)
}

function ensureContrast(pref: string, bg: string, fallback: string): string {
  // 优先使用主色；若对比度 < 3，则用强调色；若仍不足，回退安全色
  try {
    if (pref && bg && contrastRatio(pref, bg) >= 3) return pref
    if (fallback && bg && contrastRatio(fallback, bg) >= 3) return fallback
  } catch {}
  // 根据暗黑/明亮回退到高对比安全色
  const isDark = document.documentElement.classList.contains('dark')
  return isDark ? '#60a5fa' : '#2563eb'
}

const effectivePrimary = computed(() => ensureContrast(themePrimary.value || '#22d3ee', themeBg.value || '#ffffff', themeAccent.value || '#a78bfa'))
const effectiveAccent = computed(() => ensureContrast(themeAccent.value || '#a78bfa', themeBg.value || '#ffffff', themePrimary.value || '#22d3ee'))

const accentBarStyle = computed(() => {
  const from = effectivePrimary.value
  const to = effectiveAccent.value
  return { background: `linear-gradient(180deg, ${from}, ${to})` }
})

// 根据通知类型返回对应的玻璃 tint 类
function tintClass(type?: string): string {
  const t = String(type || 'info').toLowerCase()
  if (t === 'success') return 'glass-tint-success'
  if (t === 'warning') return 'glass-tint-warning'
  if (t === 'error') return 'glass-tint-error'
  return 'glass-tint-info'
}

// 图标颜色随主题语义色
function iconStyle(kind: 'success'|'warning'|'error'|'info') {
  // 直接使用主题 CSS 变量，天然随主题/暗黑模式切换
  const map: Record<string, string> = {
    success: 'var(--color-success)',
    warning: 'var(--color-warning)',
    error: 'var(--color-error)',
    info: 'var(--color-info)'
  }
  return { color: map[kind] }
}

// 响应式状态
const globalLoading = ref(false)
const routeLoading = ref(false)
const loadingText = ref('')
const showErrorModal = ref(false)
const isOnline = ref(navigator.onLine)
const isDevelopment = ref(import.meta.env.DEV)
const { t } = useI18n()

// 路由加载处理
const onBeforeEnter = () => {
  routeLoading.value = true
}

const onAfterEnter = () => {
  nextTick(() => {
    routeLoading.value = false
  })
}

// 全局错误处理
const handleGlobalError = (error: Error, info?: string) => {
  console.error('Global error:', error, info)
  
  uiStore.showNotification({
    type: 'error',
    title: t('app.notifications.error.title'),
    message: error.message || t('app.notifications.error.unknown'),
    timeout: 0 // 不自动消失
  })

  if (error.name === 'ChunkLoadError' || error.message.includes('Loading chunk')) {
    showErrorModal.value = true
  }
}

// 页面重新加载
const reloadPage = () => {
  window.location.reload()
}

// 网络状态监听
const handleOnline = () => {
  isOnline.value = true
  uiStore.showNotification({
    type: 'success',
    title: t('app.network.online.title'),
    message: t('app.network.online.description'),
    timeout: 3000
  })
}

const handleOffline = () => {
  isOnline.value = false
}

// 生命周期
function readThemeVars() {
  try {
    const rs = getComputedStyle(document.documentElement)
    themePrimary.value = rs.getPropertyValue('--color-theme-primary').trim() || rs.getPropertyValue('--p').trim() || '#22d3ee'
    themeAccent.value = rs.getPropertyValue('--color-theme-accent').trim() || rs.getPropertyValue('--a').trim() || '#a78bfa'
    themeBg.value = rs.getPropertyValue('--color-base-100').trim() || getComputedStyle(document.body).backgroundColor || '#ffffff'
  } catch {}
}

onMounted(() => {
  readThemeVars()
  window.addEventListener('error', (event) => {
    handleGlobalError(event.error || new Error(event.message))
  })

  window.addEventListener('unhandledrejection', (event) => {
    handleGlobalError(new Error(event.reason))
  })

  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
  
  router.onError((error) => {
    console.error('路由错误:', error)
    handleGlobalError(error, '路由导航')
  })
})

watch(themeName, () => {
  // 主题切换后重读 CSS 变量，更新有效颜色
  readThemeVars()
})

onUnmounted(() => {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
})
</script>

<style scoped lang="postcss">
/* 页面切换动画 */
.page-enter-active {
  transition: all 0.3s ease-out;
}

.page-leave-active {
  transition: all 0.2s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 通知动画 */
.notification-enter-active {
  transition: all 0.3s ease-out;
}

.notification-leave-active {
  transition: all 0.2s ease-in;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.notification-move {
  transition: transform 0.3s ease;
}

/* 自定义边框宽度 */
.border-3 {
  border-width: 3px;
}

/* 加载动画优化 */
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 