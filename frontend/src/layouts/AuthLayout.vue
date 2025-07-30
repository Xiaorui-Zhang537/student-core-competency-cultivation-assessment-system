<template>
  <!-- 强制刷新标记: v2025-07-29-11:15 -->
  <div class="min-h-screen relative overflow-hidden">
    <!-- 动态背景 -->
    <div class="absolute inset-0 bg-gradient-to-br from-primary-50 via-blue-50 to-accent-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      <!-- 背景装饰图案 -->
      <div class="absolute inset-0 opacity-30 dark:opacity-20">
        <svg class="absolute top-0 left-0 w-full h-full" viewBox="0 0 1000 1000" preserveAspectRatio="xMidYMid slice">
          <defs>
            <linearGradient id="grid-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#3b82f6;stop-opacity:0.1" />
              <stop offset="50%" style="stop-color:#8b5cf6;stop-opacity:0.05" />
              <stop offset="100%" style="stop-color:#f97316;stop-opacity:0.1" />
            </linearGradient>
          </defs>
          <g stroke="url(#grid-gradient)" stroke-width="1" fill="none">
            <g v-for="i in 20" :key="`h-${i}`">
              <line :x1="0" :y1="i * 50" :x2="1000" :y2="i * 50" />
            </g>
            <g v-for="i in 20" :key="`v-${i}`">
              <line :x1="i * 50" :y1="0" :x2="i * 50" :y2="1000" />
            </g>
          </g>
        </svg>
      </div>

      <!-- 浮动装饰元素 -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div
          v-for="(shape, index) in floatingShapes"
          :key="index"
          :class="[
            'absolute rounded-full bg-gradient-to-br opacity-20 dark:opacity-10',
            shape.color,
            shape.size,
            'animate-float'
          ]"
          :style="{
            left: shape.left,
            top: shape.top,
            animationDelay: shape.delay,
            animationDuration: shape.duration
          }"
        ></div>
      </div>
    </div>

    <!-- 主容器 -->
    <div class="relative z-10 min-h-screen flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <!-- 顶部工具栏 -->
        <div class="flex justify-between items-center mb-8">
          <!-- 主题切换按钮 -->
          <button
            @click="toggleTheme"
            class="p-2 rounded-lg bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border border-gray-200/50 dark:border-gray-700/50 hover:bg-white dark:hover:bg-gray-800 transition-all duration-200 group"
            :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
          >
            <sun-icon v-if="isDark" class="w-5 h-5 text-yellow-500 group-hover:rotate-180 transition-transform duration-300" />
            <moon-icon v-else class="w-5 h-5 text-gray-600 group-hover:-rotate-12 transition-transform duration-300" />
          </button>

          <!-- 语言切换 -->
          <div class="relative">
            <button
              @click="showLanguageMenu = !showLanguageMenu"
              class="p-2 rounded-lg bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border border-gray-200/50 dark:border-gray-700/50 hover:bg-white dark:hover:bg-gray-800 transition-all duration-200 flex items-center space-x-2"
            >
              <globe-alt-icon class="w-5 h-5 text-gray-600 dark:text-gray-400" />
              <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ currentLanguage.label }}</span>
              <chevron-down-icon class="w-4 h-4 text-gray-500 transition-transform duration-200" 
                :class="{ 'rotate-180': showLanguageMenu }" />
            </button>
            
            <!-- 语言菜单 -->
            <div
              v-if="showLanguageMenu"
              class="absolute right-0 mt-2 w-32 bg-white/95 dark:bg-gray-800/95 backdrop-blur-sm rounded-lg shadow-lg border border-gray-200/50 dark:border-gray-700/50 py-1 z-50"
              @click.stop
            >
              <button
                v-for="lang in languages"
                :key="lang.code"
                @click="setLanguage(lang.code)"
                class="w-full px-3 py-2 text-left text-sm hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors duration-150"
                :class="{
                  'text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-900/20': currentLanguage.code === lang.code,
                  'text-gray-700 dark:text-gray-300': currentLanguage.code !== lang.code
                }"
              >
                {{ lang.label }}
              </button>
            </div>
          </div>
        </div>

        <!-- Logo 和标题区域 -->
        <div class="text-center mb-8 animate-fade-in">
          <!-- Logo 容器 -->
          <div class="relative inline-block mb-6">
            <div class="relative">
              <!-- 主 Logo -->
              <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-primary-500 to-primary-600 rounded-2xl shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 group">
                <academic-cap-icon class="w-10 h-10 text-white group-hover:scale-110 transition-transform duration-300" />
              </div>
              
              <!-- Logo 光晕效果 -->
              <div class="absolute inset-0 bg-gradient-to-br from-primary-400 to-primary-600 rounded-2xl blur-lg opacity-30 -z-10 animate-pulse"></div>
            </div>
            
            <!-- 状态指示器 -->
            <div class="absolute -top-1 -right-1 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center animate-pulse">
              <div class="w-2 h-2 bg-white rounded-full"></div>
            </div>
          </div>

          <!-- 系统标题 -->
          <div class="space-y-2">
            <h1 class="text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 dark:from-white dark:to-gray-300 bg-clip-text text-transparent leading-tight">
              学生非核心能力发展评估系统
            </h1>
            <div class="flex items-center justify-center space-x-2 text-gray-600 dark:text-gray-400">
              <sparkles-icon class="w-4 h-4 text-primary-500 animate-pulse" />
              <p class="text-sm font-medium">基于AI的智能学习与评估平台</p>
              <sparkles-icon class="w-4 h-4 text-accent-500 animate-pulse" />
            </div>
          </div>

          <!-- 版本信息 -->
          <div class="mt-4 inline-flex items-center px-3 py-1 rounded-full bg-primary-100 dark:bg-primary-900/30 text-xs font-medium text-primary-700 dark:text-primary-300">
            <span class="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></span>
            v2.0.1 正式版
          </div>
        </div>

        <!-- 内容卡片区域 -->
        <div class="relative">
          <!-- 卡片背景 -->
          <div class="absolute inset-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-2xl shadow-2xl border border-gray-200/50 dark:border-gray-700/50"></div>
          
          <!-- 内容区域 -->
          <div class="relative z-10 p-8">
            <!-- 加载状态覆盖 -->
            <div v-if="isLoading" class="absolute inset-0 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm rounded-2xl flex items-center justify-center z-20">
              <div class="text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 bg-primary-100 dark:bg-primary-900/30 rounded-xl mb-3">
                  <div class="w-6 h-6 border-2 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ loadingText }}</p>
              </div>
            </div>

            <!-- 路由内容 -->
            <div class="transition-all duration-300" :class="{ 'opacity-30 pointer-events-none': isLoading }">
              <router-view v-slot="{ Component, route }">
                <transition
                  name="page"
                  mode="out-in"
                  @before-enter="onBeforeEnter"
                  @after-enter="onAfterEnter"
                >
                  <component :is="Component" :key="route.path" />
                </transition>
              </router-view>
            </div>
          </div>
        </div>

        <!-- 功能特性展示 -->
        <div class="mt-8 grid grid-cols-3 gap-4">
          <div
            v-for="(feature, index) in features"
            :key="index"
            class="text-center p-4 rounded-xl bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm border border-gray-200/30 dark:border-gray-700/30 hover:bg-white/80 dark:hover:bg-gray-800/80 transition-all duration-300 hover:scale-105 group"
          >
            <component
              :is="feature.icon"
              class="w-6 h-6 mx-auto mb-2 text-primary-600 dark:text-primary-400 group-hover:scale-110 transition-transform duration-300"
            />
            <h3 class="text-xs font-medium text-gray-900 dark:text-white mb-1">{{ feature.title }}</h3>
            <p class="text-xs text-gray-600 dark:text-gray-400">{{ feature.description }}</p>
          </div>
        </div>

        <!-- 页脚信息 -->
        <div class="mt-8 text-center space-y-3">
          <!-- 社交链接 -->
          <div class="flex justify-center space-x-4">
            <a
              v-for="social in socialLinks"
              :key="social.name"
              :href="social.url"
              class="p-2 rounded-lg bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm border border-gray-200/30 dark:border-gray-700/30 hover:bg-white dark:hover:bg-gray-800 transition-all duration-200 hover:scale-110"
              :title="social.name"
            >
              <component :is="social.icon" class="w-4 h-4 text-gray-600 dark:text-gray-400" />
            </a>
          </div>

          <!-- 版权信息 -->
          <div class="text-xs text-gray-500 dark:text-gray-400 space-y-1">
            <p>&copy; 2024 学生非核心能力发展评估系统. 保留所有权利.</p>
            <div class="flex items-center justify-center space-x-4">
              <a href="#" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">隐私政策</a>
              <span>•</span>
              <a href="#" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">服务条款</a>
              <span>•</span>
              <a href="#" class="hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200">帮助中心</a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 点击外部关闭语言菜单 -->
    <div
      v-if="showLanguageMenu"
      @click="showLanguageMenu = false"
      class="fixed inset-0 z-40"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import {
  SunIcon,
  MoonIcon,
  GlobeAltIcon,
  ChevronDownIcon,
  AcademicCapIcon,
  SparklesIcon,
  ShieldCheckIcon,
  ChartBarIcon,
  UserGroupIcon,
  DevicePhoneMobileIcon,
  CloudIcon,
  CpuChipIcon
} from '@heroicons/vue/24/outline'

// 组合式API
const route = useRoute()
const themeStore = useThemeStore()

// 响应式状态
const isLoading = ref(false)
const loadingText = ref('加载中...')
const showLanguageMenu = ref(false)
const currentLanguage = ref({ code: 'zh-CN', label: '中文' })

// 主题相关
const isDark = computed(() => themeStore.isDark)

// 语言选项
const languages = [
  { code: 'zh-CN', label: '中文' },
  { code: 'en-US', label: 'English' },
  { code: 'ja-JP', label: '日本語' }
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
  {
    icon: ShieldCheckIcon,
    title: '安全可靠',
    description: '企业级安全保障'
  },
  {
    icon: ChartBarIcon,
    title: '智能分析',
    description: 'AI驱动的数据洞察'
  },
  {
    icon: UserGroupIcon,
    title: '协作学习',
    description: '社区互动学习'
  },
  {
    icon: DevicePhoneMobileIcon,
    title: '移动优先',
    description: '跨平台响应式'
  },
  {
    icon: CloudIcon,
    title: '云端同步',
    description: '数据实时同步'
  },
  {
    icon: CpuChipIcon,
    title: '高性能',
    description: '极速响应体验'
  }
]

// 社交链接
const socialLinks = [
  {
    name: 'GitHub',
    url: 'https://github.com',
    icon: DevicePhoneMobileIcon // 实际项目中应该使用专门的社交图标
  },
  {
    name: '微信',
    url: '#',
    icon: CloudIcon
  },
  {
    name: '邮箱',
    url: 'mailto:support@example.com',
    icon: CpuChipIcon
  }
]

// 方法
const toggleTheme = () => {
  themeStore.toggleTheme()
}

const setLanguage = (langCode: string) => {
  const lang = languages.find(l => l.code === langCode)
  if (lang) {
    currentLanguage.value = lang
    showLanguageMenu.value = false
    
    // 这里可以集成国际化库
    console.log('切换语言到:', lang.label)
  }
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

// 生命周期
onMounted(() => {
  // 监听键盘事件
  document.addEventListener('keydown', handleKeydown)
  
  // 初始加载动画
  nextTick(() => {
    handleRouteChange()
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
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
@media (prefers-contrast: high) {
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