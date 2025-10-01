<template>
  <!-- 强制刷新标记: v2025-07-29-11:15 -->
  <div class="min-h-screen relative overflow-hidden text-base-content">
    <!-- 动态背景 -->
    <div class="absolute inset-0" :style="authBgStyle"></div>

    <!-- 主容器：分屏布局 -->
    <div class="relative z-10 min-h-screen p-6 md:p-10">
      <!-- 顶部工具栏（右上角） -->
      <div class="flex items-center justify-end gap-3 mb-6">
          <!-- 主题切换按钮 -->
          <button
            @click="toggleTheme"
            v-glass="{ strength: 'thin', interactive: true }"
            class="p-2 rounded-lg transition-all duration-200 group"
            :title="isDark ? t('layout.auth.theme.switchToLight') : t('layout.auth.theme.switchToDark')"
          >
            <sun-icon v-if="isDark" class="w-5 h-5 text-yellow-500 group-hover:rotate-180 transition-transform duration-300" />
            <moon-icon v-else class="w-5 h-5 text-gray-600 group-hover:-rotate-12 transition-transform duration-300" />
          </button>
      </div>
      <!-- 分屏栅格 -->
      <div class="grid grid-cols-1 md:grid-cols-12 gap-8 items-center">
        <!-- 左侧品牌与卖点 -->
        <div class="md:col-span-7 order-2 md:order-1">
          <div class="animate-fade-in">
            <!-- Logo 与标题 -->
            <div class="flex items-center gap-4 mb-6">
              <div class="relative">
                <div class="inline-flex items-center justify-center w-16 h-16 md:w-20 md:h-20 bg-gradient-to-br from-primary-500 to-primary-600 rounded-2xl shadow-lg">
                  <academic-cap-icon class="w-8 h-8 md:w-10 md:h-10 text-white" />
                </div>
                <div class="absolute inset-0 bg-gradient-to-br from-primary-400 to-primary-600 rounded-2xl blur-lg opacity-30 -z-10"></div>
              </div>
              <div>
                <h1 class="text-2xl md:text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 dark:from-white dark:to-gray-300 bg-clip-text text-transparent leading-tight">
                  {{ t('app.title') }}
                </h1>
                <p class="mt-1 text-sm md:text-base text-gray-600 dark:text-gray-400">{{ t('layout.auth.subtitle') }}</p>
              </div>
            </div>

            <!-- 卖点列表 -->
            <div class="grid sm:grid-cols-2 gap-4 mb-6">
              <div v-glass="{ strength: 'thin' }" class="p-4 rounded-xl">
                <div class="text-sm font-medium text-base-content mb-1">{{ t('layout.auth.points.radar.title') }}</div>
                <div class="text-xs text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.radar.desc') }}</div>
              </div>
              <div v-glass="{ strength: 'thin' }" class="p-4 rounded-xl">
                <div class="text-sm font-medium text-base-content mb-1">{{ t('layout.auth.points.ai.title') }}</div>
                <div class="text-xs text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.ai.desc') }}</div>
              </div>
              <div v-glass="{ strength: 'thin' }" class="p-4 rounded-xl">
                <div class="text-sm font-medium text-base-content mb-1">{{ t('layout.auth.points.growth.title') }}</div>
                <div class="text-xs text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.growth.desc') }}</div>
              </div>
            </div>

            <!-- 可信背书条（占位） -->
            <div class="flex flex-wrap items-center gap-3 text-xs text-gray-500 dark:text-gray-400">
              <span class="opacity-80">{{ t('layout.auth.trust.title') }}</span>
              <div class="h-6 w-[1px] bg-gray-300 dark:bg-gray-700"></div>
              <div class="flex items-center gap-3">
                <span v-glass="{ strength: 'ultraThin' }" class="px-2 py-1 rounded">Your School</span>
                <span v-glass="{ strength: 'ultraThin' }" class="px-2 py-1 rounded">Your Institute</span>
                <span v-glass="{ strength: 'ultraThin' }" class="px-2 py-1 rounded">Project Name</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单卡片 -->
        <div class="md:col-span-5 order-1 md:order-2">
          <div class="flex justify-between items-center mb-4">
            <!-- 语言切换放表单上方 -->
            <div class="ml-auto"><language-switcher /></div>
          </div>
          <card :hoverable="false" padding="lg" class="relative">
            <!-- 加载状态覆盖 -->
            <div v-if="isLoading" class="absolute inset-0 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm rounded-2xl flex items-center justify-center z-20">
              <div class="text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 bg-primary-100 dark:bg-primary-900/30 rounded-xl mb-3">
                  <div class="w-6 h-6 border-2 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
                <p class="text-sm text-subtle">{{ loadingText }}</p>
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
              <div class="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                <div class="inline-flex items-center gap-2">
                  <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full font-medium text-primary-700 dark:text-primary-300">
                    <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-1.5"></span>
                    {{ `v${version}` }}
                  </span>
                  <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full">
                    <span class="w-1.5 h-1.5 bg-blue-500 rounded-full mr-1.5"></span>
                    {{ formattedTime }}
                  </span>
                </div>
                <div class="inline-flex items-center gap-3">
                  <a href="#" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">{{ t('layout.auth.footer.privacy') }}</a>
                  <span>•</span>
                  <a href="#" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">{{ t('layout.auth.footer.terms') }}</a>
                  <span>•</span>
                  <button type="button" @click="goAndReload('/help')" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">{{ t('layout.auth.footer.help') }}</button>
                </div>
              </div>
            </template>
          </card>
        </div>
      </div>
    </div>

    
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
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
// FuturisticBackground 已移除
import Card from '@/components/ui/Card.vue'

// 组合式API
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 响应式状态
const isLoading = ref(false)
const loadingText = ref('加载中...')
const showLanguageMenu = ref(false)
const currentLanguage = ref({ code: 'zh-CN', label: '中文' })
const version = import.meta.env.VITE_APP_VERSION || '1.0.0'
const currentTime = ref(new Date())
let timer: number | null = null
const { locale, setLocale } = useLocale()
const { t, d } = useI18n()
const formattedTime = computed(() => d(currentTime.value, 'medium'))

// 主题相关
const isDark = computed(() => uiStore.isDarkMode)
const isNewTheme = computed(() => {
  const theme = document.documentElement.getAttribute('data-theme') || ''
  return theme === 'retro' || theme === 'dracula'
})
const authBgStyle = computed(() => {
  return isNewTheme.value
    ? { background: 'var(--color-base-100)' }
    : { background: isDark.value
        ? 'linear-gradient(to bottom right, #0f172a, #111827, #0f172a)'
        : 'linear-gradient(to bottom right, #eff6ff, #e0f2fe, #fff7ed)'
      }
})

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
const toggleTheme = () => {
  uiStore.toggleDarkMode()
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
    toggleTheme()
  }
  
  // Esc 关闭语言菜单
  if (event.key === 'Escape') {
    showLanguageMenu.value = false
  }
}

// 视差已在 FuturisticBackground 内部处理，这里不再重复实现

// 生命周期
onMounted(() => {
  // 监听键盘事件
  document.addEventListener('keydown', handleKeydown)

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