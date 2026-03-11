<template>
  <!-- 强制刷新标记: v2025-07-29-11:15 -->
  <div class="authPage min-h-screen relative overflow-hidden text-base-content" :style="baseBgStyle">

    <!-- 主容器：顶部药丸导航 + 分屏布局 -->
    <div class="relative z-10 min-h-screen">
      <!-- 顶部导航：与主页药丸尺寸/位置对齐 -->
      <nav class="sticky top-0 z-40 px-3 sm:px-6 pt-3 sm:pt-6 pb-3 sm:pb-6">
        <div class="flex flex-wrap items-center gap-2 sm:gap-3">
          <liquid-glass
            effect="occlusionBlur" :tint="false" :radius="30"
            class="flex items-center justify-center gap-3 h-full"
            containerClass="rounded-full h-[52px] sm:h-[60px] px-4 sm:px-5 whitespace-nowrap"
          >
            <div class="flex items-center justify-center gap-3">
              <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
              <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-lg sm:text-2xl md:text-3xl" />
            </div>
          </liquid-glass>
          <div class="hidden sm:block flex-1"></div>

          <!-- 右上 Dock：恢复外层玻璃容器，保持原始包裹感 -->
          <liquid-glass effect="occlusionBlur" :tint="false" :radius="30" class="flex items-center justify-center h-full max-w-full w-full sm:w-auto" containerClass="rounded-full h-[52px] sm:h-[60px] px-1 sm:px-1.5 max-w-full">
            <dock :magnification="0" :distance="0" :animate="false" variant="transparent" paddingClass="pl-0.5 pr-2 sm:pl-1 sm:pr-4" heightClass="h-[48px] sm:h-[56px]" roundedClass="rounded-full" gapClass="gap-2 sm:gap-3" class="!mt-0 min-w-0 overflow-x-auto">
              <dock-icon :animate="false" class="mr-[-6px] sm:relative sm:left-[8px] sm:mr-[-10px]">
                <glass-tooltip :content="(t('layout.common.toggleTheme') as string) || '主题'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :title="t('layout.common.toggleTheme') as string" @click="uiStore.toggleDarkMode()">
                    <sun-icon v-if="uiStore.isDarkMode" class="w-4 h-4 sm:w-5 sm:h-5" />
                    <moon-icon v-else class="w-4 h-4 sm:w-5 sm:h-5 relative top-[1px]" />
                  </ripple-button>
                </glass-tooltip>
              </dock-icon>
              <dock-icon :animate="false" class="hidden sm:block">
                <span ref="cursorBtnRef" class="inline-flex">
                  <glass-tooltip :content="(t('layout.common.cursorTrail') as string) || '鼠标轨迹'">
                    <ripple-button pill :duration="0" :class="topbarBtnClass" :title="t('layout.common.cursorTrail') as string" @click="onToggleCursorMenu">
                      <cursor-arrow-rays-icon class="w-4 h-4 sm:w-5 sm:h-5 relative -left-[1px]" />
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
                <glass-tooltip content="返回首页">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :title="('返回首页')" @click="goHome()">
                    <home-icon class="w-4 h-4 sm:w-5 sm:h-5" />
                  </ripple-button>
                </glass-tooltip>
              </dock-icon>
            </dock>
          </liquid-glass>
        </div>
      </nav>
      <!-- 光标菜单弹层 -->
      <teleport to="body">
        <div v-if="showCursorMenu" class="fixed inset-0 z-[999]" @click="showCursorMenu = false"></div>
        <liquid-glass
          v-if="showCursorMenu"
          effect="occlusionBlur"
          :style="cursorMenuStyle"
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
            <div class="font-medium mb-1">{{ t('layout.common.cursorTrailTitle') }}</div>
            <div class="opacity-90">{{ t('layout.common.cursorTrailDesc') }}</div>
          </div>
          <div class="border-t border-white/10 my-1"></div>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('off')">
            <span>{{ t('layout.common.cursorTrailOff') }}</span>
            <span v-if="uiStore.cursorTrailMode==='off'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('fluid')">
            <span>{{ t('layout.common.cursorTrailFluid') }}</span>
            <span v-if="uiStore.cursorTrailMode==='fluid'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('smooth')">
            <span>{{ t('layout.common.cursorTrailSmooth') }}</span>
            <span v-if="uiStore.cursorTrailMode==='smooth'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('tailed')">
            <span>{{ t('layout.common.cursorTrailTailed') }}</span>
            <span v-if="uiStore.cursorTrailMode==='tailed'" class="text-theme-primary">✓</span>
          </button>
        </liquid-glass>
      </teleport>
      <!-- 分屏栅格 -->
      <div class="pb-6 sm:pb-8 md:pb-10 pl-4 pr-4 sm:pl-8 sm:pr-8 md:pl-12 md:pr-12 xl:pl-16 xl:pr-16 2xl:pl-20 2xl:pr-20 max-w-8xl mx-auto">
      <div class="grid grid-cols-1 md:grid-cols-12 gap-6 xl:gap-8 2xl:gap-10 items-start xl:items-center mt-2 sm:mt-4 md:mt-8 lg:mt-12">
        <!-- 左侧品牌与卖点 -->
        <div class="hidden xl:block xl:col-span-5 xl:col-start-1 order-2 xl:order-1 xl:w-full xl:max-w-[620px] xl:justify-self-end">
          <liquid-glass
            :radius="24"
            :frost="0.08"
            :alpha="0.9"
            :blur="14"
            :border="0.09"
            :tint="true"
            :tint-from="'var(--color-theme-primary)'"
            :tint-to="'var(--color-theme-accent)'"
            class="animate-fade-in"
            containerClass="authReplicaPanel rounded-3xl"
          >
            <div class="relative z-20 flex items-end justify-center h-[400px] xl:h-[450px] 2xl:h-[500px]">
              <animated-characters
                :is-typing="authAnimState.isTyping"
                :is-password-focused="authAnimState.isPasswordFocused"
                :show-password="authAnimState.showPassword"
                :password-length="authAnimState.passwordLength"
              />
            </div>
          </liquid-glass>
        </div>

        <!-- 右侧表单卡片 -->
        <div class="md:col-span-8 md:col-start-3 xl:col-span-5 xl:col-start-8 order-1 xl:order-2 w-full max-w-xl md:mx-auto xl:w-full xl:max-w-[620px] xl:mx-0 xl:justify-self-start mt-8 md:mt-10 xl:mt-0">
          <card :hoverable="false" padding="lg" tint="primary" class="relative border border-white/20 dark:border-white/15 bg-base-100/60 dark:bg-base-200/30 backdrop-blur-xl">
            <!-- 加载状态覆盖 -->
            <div v-if="isLoading" class="absolute inset-0 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm rounded-2xl flex items-center justify-center z-20">
              <div class="text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl mb-3" :style="{ backgroundColor: 'color-mix(in oklab, var(--color-primary) 18%, transparent)' }">
                  <div class="w-6 h-6 border-2 rounded-full animate-spin" :style="{ borderColor: 'var(--color-primary)', borderTopColor: 'transparent' }"></div>
                </div>
                <p class="text-sm text-theme-primary">{{ loadingText }}</p>
              </div>
            </div>

            <!-- 路由内容 -->
            <div class="transition-all duration-300" :class="{ 'opacity-30 pointer-events-none': isLoading }">
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
            </div>

            <template #footer>
              <div class="flex items-center justify-between text-xs text-info">
                <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full font-medium bg-info/15 dark:bg-info/20 border border-info/30 dark:border-info/30">
                  <span class="w-1.5 h-1.5 bg-current rounded-full mr-1.5"></span>
                  {{ `v${version}` }}
                </span>
                <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full bg-info/15 dark:bg-info/20 border border-info/30 dark:border-info/30">
                  <span class="w-1.5 h-1.5 bg-current rounded-full mr-1.5"></span>
                  {{ formattedTime }}
                </span>
              </div>
            </template>
          </card>
        </div>
      </div>
      </div>
    </div>

    <cursor-trail-layer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'
import {
  SunIcon,
  MoonIcon,
  AcademicCapIcon,
  SparklesIcon,
  ShieldCheckIcon,
  ChartBarIcon,
  UserGroupIcon,
  DevicePhoneMobileIcon,
  CloudIcon,
  CpuChipIcon,
  
  QuestionMarkCircleIcon
} from '@heroicons/vue/24/outline'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import Dock from '@/components/ui/inspira/Dock.vue'
import DockIcon from '@/components/ui/inspira/DockIcon.vue'
import { HomeIcon, CursorArrowRaysIcon } from '@heroicons/vue/24/outline'
// FuturisticBackground 已移除
import Card from '@/components/ui/Card.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'
import AnimatedCharacters from '@/features/auth/components/AnimatedCharacters.vue'
import GlassTooltip from '@/components/ui/GlassTooltip.vue'

// 组合式API
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 响应式状态
const isLoading = ref(false)
const loadingText = ref('加载中...')
const showLanguageMenu = ref(false)
const currentLanguage = ref({ code: 'zh-CN', label: '中文' })
const version = '2.3.2'
const currentTime = ref(new Date())
let timer: number | null = null
const { locale, setLocale } = useLocale()
const { t, d } = useI18n()
const formattedTime = computed(() => d(currentTime.value, 'medium'))
const baseBgStyle = computed(() => ({
  backgroundColor: 'color-mix(in oklab, var(--color-base-100) 90%, var(--color-primary) 10%)'
}))
const topbarBtnClass = [
  'hover:bg-black/5 active:bg-black/10',
  'dark:hover:bg-white/10 dark:active:bg-white/15',
  'focus-visible:shadow-[0_0_0_2px_rgba(59,130,246,0.35)]',
].join(' ')
const authAnimState = ref({
  isTyping: false,
  isPasswordFocused: false,
  showPassword: false,
  passwordLength: 0,
})
let authMutationObserver: MutationObserver | null = null
let authSyncFrame: number | null = null

// 主题相关
const isDark = computed(() => uiStore.isDarkMode)
const isNewTheme = computed(() => {
  const theme = document.documentElement.getAttribute('data-theme') || ''
  return theme === 'retro' || theme === 'dracula'
})
// 认证页背景保持纯色主题底

// 动画偏好与设备能力（背景组件内部已做处理，此处无需额外逻辑）

// 语言选项
const languages = [
  { code: 'zh-CN', label: '中文' },
  { code: 'en-US', label: 'English' }
]

// 浮动装饰形状
const floatingShapes = ref([
  {
    color: 'from-primary-400 to-blue-500',
    size: 'w-32 h-32',
    left: '10%',
    top: '20%',
    delay: '0s',
    duration: '20s'
  },
  {
    color: 'from-accent-400 to-orange-500',
    size: 'w-24 h-24',
    left: '80%',
    top: '60%',
    delay: '5s',
    duration: '25s'
  },
  {
    color: 'from-purple-400 to-pink-500',
    size: 'w-40 h-40',
    left: '70%',
    top: '10%',
    delay: '10s',
    duration: '30s'
  },
  {
    color: 'from-green-400 to-teal-500',
    size: 'w-28 h-28',
    left: '20%',
    top: '70%',
    delay: '15s',
    duration: '22s'
  },
  {
    color: 'from-indigo-400 to-purple-500',
    size: 'w-36 h-36',
    left: '50%',
    top: '80%',
    delay: '8s',
    duration: '28s'
  }
])

// 功能特性
const features = [
  { icon: ShieldCheckIcon, titleKey: 'layout.auth.features.secure.title', descKey: 'layout.auth.features.secure.desc' },
  { icon: ChartBarIcon, titleKey: 'layout.auth.features.analytics.title', descKey: 'layout.auth.features.analytics.desc' },
  { icon: UserGroupIcon, titleKey: 'layout.auth.features.collab.title', descKey: 'layout.auth.features.collab.desc' },
  { icon: DevicePhoneMobileIcon, titleKey: 'layout.auth.features.mobile.title', descKey: 'layout.auth.features.mobile.desc' },
  { icon: CloudIcon, titleKey: 'layout.auth.features.cloud.title', descKey: 'layout.auth.features.cloud.desc' },
  { icon: CpuChipIcon, titleKey: 'layout.auth.features.performance.title', descKey: 'layout.auth.features.performance.desc' }
]

// 社交链接
const socialLinks = [
  {
    name: 'GitHub',
    url: 'https://github.com/Xiaorui-Zhang537/',
    icon: DevicePhoneMobileIcon // 实际项目中应该使用专门的社交图标
  },
  {
    name: '微信',
    url: '#',
    icon: CloudIcon
  },
  {
    name: '邮箱',
    url: '1519939545@qq.com',
    icon: CpuChipIcon
  },
  {
    name: '帮助中心',
    url: '/help',
    route: true,
    icon: QuestionMarkCircleIcon
  }
]

// 方法
function goHome() { router.push('/') }

function goLogin() {
  router.push('/auth/login')
}

// 光标选择菜单
const cursorBtnRef = ref<HTMLElement | null>(null)
const showCursorMenu = ref(false)
const cursorMenuStyle = ref<Record<string, string>>({})

function onToggleCursorMenu() {
  showCursorMenu.value = !showCursorMenu.value
  if (!showCursorMenu.value) return
  nextTick(() => {
    try {
      const el = cursorBtnRef.value as HTMLElement
      const rect = el.getBoundingClientRect()
      cursorMenuStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 10}px`,
        left: `${Math.max(8, rect.right - 220)}px`,
        width: '14rem',
        zIndex: '1000'
      }
    } catch {}
  })
}

watch(
  () => route.path,
  () => {
    nextTick(() => {
      syncAuthAnimationState()
    })
  }
)

function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
  uiStore.setCursorTrailMode(v)
  showCursorMenu.value = false
}
const goAndReload = async (path: string) => {
  try {
    await router.push(path)
  } finally {
    requestAnimationFrame(() => window.location.reload())
  }
}


const setLanguage = async (langCode: string) => {
  if (langCode !== 'zh-CN' && langCode !== 'en-US') return
  const lang = languages.find(l => l.code === langCode)
  if (!lang) return
  await loadLocaleMessages(langCode, [...REQUIRED_NAMESPACES])
  await setLocale(langCode)
  currentLanguage.value = lang
  showLanguageMenu.value = false
}

const simulateLoading = (text: string, duration: number = 1000) => {
  isLoading.value = true
  loadingText.value = text
  
  setTimeout(() => {
    isLoading.value = false
  }, duration)
}

// 路由动画事件
const onBeforeEnter = () => {
  // 路由切换前的处理
}

const onAfterEnter = () => {
  // 路由切换后的处理
}

// 监听路由变化
const handleRouteChange = () => {
  // 根据路由显示不同的加载文本
  const routeLoadingTexts: Record<string, string> = {
    '/auth/login': '正在加载登录页面...',
    '/auth/register': '正在加载注册页面...',
    '/auth/forgot-password': '正在加载密码重置...',
    '/auth/reset-password': '正在加载密码重置...'
  }
  
  const loadingText = routeLoadingTexts[route.path] || '加载中...'
  simulateLoading(loadingText, 800)
}

// 键盘快捷键
const handleKeydown = (event: KeyboardEvent) => {
  // Ctrl+Shift+T 切换主题
  if (event.ctrlKey && event.shiftKey && event.key === 'T') {
    event.preventDefault()
    uiStore.toggleDarkMode()
  }
  
  // Esc 关闭语言菜单
  if (event.key === 'Escape') {
    showLanguageMenu.value = false
  }
}

function pickAuthInput(selector: string): HTMLInputElement | null {
  const scoped = document.querySelector(`.max-w-md ${selector}`) as HTMLInputElement | null
  if (scoped) return scoped
  return document.querySelector(selector) as HTMLInputElement | null
}

function syncAuthAnimationState() {
  if (route.path !== '/auth/login') {
    authAnimState.value = { isTyping: false, isPasswordFocused: false, showPassword: false, passwordLength: 0 }
    return
  }

  const usernameInput = pickAuthInput('#username')
  const passwordInput = pickAuthInput('#password')

  const activeEl = document.activeElement
  const isTyping = !!usernameInput && activeEl === usernameInput
  const isPasswordFocused = !!passwordInput && activeEl === passwordInput
  const passwordLength = Number(passwordInput?.value?.length || 0)
  const showPassword = !!passwordInput && passwordInput.type === 'text'

  authAnimState.value = { isTyping, isPasswordFocused, showPassword, passwordLength }
}

function scheduleAuthAnimationStateSync() {
  if (authSyncFrame) window.cancelAnimationFrame(authSyncFrame)
  authSyncFrame = window.requestAnimationFrame(() => {
    authSyncFrame = null
    syncAuthAnimationState()
  })
}

const handleAuthInteraction = () => {
  scheduleAuthAnimationStateSync()
}

// 视差已在 FuturisticBackground 内部处理，这里不再重复实现

// 生命周期
onMounted(() => {
  // 监听键盘事件
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('input', handleAuthInteraction, true)
  document.addEventListener('focusin', handleAuthInteraction, true)
  document.addEventListener('focusout', handleAuthInteraction, true)
  document.addEventListener('click', handleAuthInteraction, true)

  authMutationObserver = new MutationObserver(() => {
    scheduleAuthAnimationStateSync()
  })
  try {
    authMutationObserver.observe(document.body, {
      subtree: true,
      childList: true,
      attributes: true,
      attributeFilter: ['type']
    })
  } catch {}
  syncAuthAnimationState()

  // 实时时间更新
  timer = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
  
  // 初始化语言标签
  const init = languages.find(l => l.code === locale.value)
  if (init) currentLanguage.value = init
  
  // 初始加载动画
  nextTick(() => {
    handleRouteChange()
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('input', handleAuthInteraction, true)
  document.removeEventListener('focusin', handleAuthInteraction, true)
  document.removeEventListener('focusout', handleAuthInteraction, true)
  document.removeEventListener('click', handleAuthInteraction, true)
  try {
    authMutationObserver?.disconnect()
  } catch {}
  authMutationObserver = null
  if (authSyncFrame) window.cancelAnimationFrame(authSyncFrame)
  authSyncFrame = null
  if (timer) window.clearInterval(timer)
})

// 监听路由变化
// watch(() => route.path, handleRouteChange)
</script>

<style scoped lang="postcss">
/* 页面切换动画 */
.page-enter-active {
  transition: all 0.3s ease-out;
}

.page-leave-active {
  transition: all 0.3s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 淡入动画 */
.animate-fade-in {
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.authReplicaPanel {
  position: relative;
  overflow: hidden;
  min-height: 500px;
  max-height: 560px;
  padding: 1rem 1.1rem 1.2rem;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  isolation: isolate;
}

.authReplicaPanel::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background: radial-gradient(120% 90% at 50% 100%, rgba(0, 0, 0, 0.18) 0%, rgba(0, 0, 0, 0) 72%);
  opacity: 0.9;
}

@media (max-height: 920px) and (min-width: 1280px) {
  .authReplicaPanel {
    min-height: 460px;
    max-height: 500px;
  }
}

/* 浮动动画 */
.animate-float {
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(90deg);
  }
  50% {
    transform: translateY(0px) rotate(180deg);
  }
  75% {
    transform: translateY(20px) rotate(270deg);
  }
}

/* 背景渐变动画 */
.bg-gradient-to-br {
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 文字渐变效果 */
.bg-clip-text {
  -webkit-background-clip: text;
  background-clip: text;
}

/* 毛玻璃效果增强 */
.backdrop-blur-xl {
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}

.backdrop-blur-sm {
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

/* 响应式调整 */
@media (max-width: 640px) {
  .animate-float {
    animation-duration: 15s;
  }
  
  .floatingShapes {
    display: none; /* 移动端隐藏装饰元素以提升性能 */
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: more) {
  .bg-white\/80 {
    background-color: rgb(255 255 255 / 0.95);
  }
  
  .dark .bg-gray-800\/80 {
    background-color: rgb(31 41 55 / 0.95);
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .animate-float,
  .animate-pulse,
  .animate-spin,
  .bg-gradient-to-br {
    animation: none;
  }
  
  .transition-all,
  .transition-colors,
  .transition-transform {
    transition: none;
  }
}
</style> 
