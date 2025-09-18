<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-6">
    <div class="max-w-4xl w-full text-center">
      <!-- 主要错误内容 -->
      <div class="mb-12">
        <!-- 动画404数字 -->
        <div class="relative mb-8">
          <h1 class="text-8xl md:text-9xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600 dark:from-blue-400 dark:to-purple-400 select-none">
            404
          </h1>
          <!-- 装饰性元素 -->
          <div class="absolute inset-0 flex items-center justify-center">
            <div class="w-32 h-32 bg-blue-100 dark:bg-blue-900 rounded-full opacity-20 animate-pulse"></div>
          </div>
        </div>

        <!-- 错误信息 -->
        <div class="space-y-4 mb-8">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">
            页面走丢了
          </h2>
          <p class="text-lg text-gray-600 dark:text-gray-400 max-w-2xl mx-auto">
            抱歉，您访问的页面不存在。可能是链接错误、页面已被移动或您没有访问权限。
          </p>
        </div>

        <!-- 搜索建议 -->
        <card padding="lg" class="max-w-md mx-auto mb-8">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              尝试搜索您需要的内容
            </h3>
          </template>
          
          <div class="space-y-4">
            <div class="relative">
              <GlassSearchInput v-model="searchQuery" placeholder="搜索课程、作业、学生..." size="md" @keyup.enter="handleSearch" />
            </div>
            <button variant="primary" class="w-full" @click="handleSearch" :disabled="!searchQuery.trim()">
              <magnifying-glass-icon class="w-4 h-4 mr-2" />
              搜索
            </button>
          </div>
        </card>
      </div>

      <!-- 导航选项 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <!-- 返回首页 -->
        <card padding="lg" hoverable class="cursor-pointer" @click="goHome">
          <div class="text-center">
            <div class="flex items-center justify-center w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-lg mx-auto mb-4">
              <home-icon class="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">返回首页</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">回到主页继续浏览</p>
          </div>
        </card>

        <!-- 浏览课程 -->
        <card padding="lg" hoverable class="cursor-pointer" @click="browseCourses">
          <div class="text-center">
            <div class="flex items-center justify-center w-12 h-12 bg-green-100 dark:bg-green-900 rounded-lg mx-auto mb-4">
              <book-open-icon class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">浏览课程</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">查看所有可用课程</p>
          </div>
        </card>

        <!-- 联系支持 -->
        <card padding="lg" hoverable class="cursor-pointer" @click="contactSupport">
          <div class="text-center">
            <div class="flex items-center justify-center w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-lg mx-auto mb-4">
              <question-mark-circle-icon class="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">获取帮助</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">联系技术支持</p>
          </div>
        </card>
      </div>

      <!-- 常用链接 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">您可能在寻找</h3>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <router-link
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="flex items-center space-x-2 text-sm text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 transition-colors"
          >
            <component :is="link.icon" class="w-4 h-4" />
            <span>{{ link.title }}</span>
          </router-link>
        </div>
      </div>

      <!-- 返回按钮 -->
      <div class="mt-8 flex justify-center space-x-4">
        <button variant="outline" @click="goBack" v-if="canGoBack">
          <arrow-left-icon class="w-4 h-4 mr-2" />
          返回上页
        </button>
        <button variant="primary" @click="goHome">
          <home-icon class="w-4 h-4 mr-2" />
          返回首页
        </button>
      </div>

      <!-- 装饰性元素 -->
      <div class="fixed top-10 right-10 opacity-10 pointer-events-none">
        <div class="w-64 h-64 bg-gradient-to-br from-blue-400 to-purple-500 rounded-full blur-3xl"></div>
      </div>
      <div class="fixed bottom-10 left-10 opacity-10 pointer-events-none">
        <div class="w-48 h-48 bg-gradient-to-br from-green-400 to-blue-500 rounded-full blur-3xl"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import {
  MagnifyingGlassIcon,
  HomeIcon,
  BookOpenIcon,
  QuestionMarkCircleIcon,
  ArrowLeftIcon,
  AcademicCapIcon,
  ClipboardDocumentListIcon,
  ChartBarIcon,
  UserGroupIcon,
  Cog6ToothIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

// 状态
const searchQuery = ref('')

// 计算属性
const canGoBack = computed(() => window.history.length > 1)

// 根据用户角色动态生成快速链接
const quickLinks = computed(() => {
  const commonLinks = [
    { title: '帮助中心', path: '/help', icon: InformationCircleIcon },
    { title: '系统设置', path: '/settings', icon: Cog6ToothIcon }
  ]

  if (authStore.user?.role === 'STUDENT') {
    return [
      { title: '学生工作台', path: '/student', icon: HomeIcon },
      { title: '我的课程', path: '/student/courses', icon: BookOpenIcon },
              { title: '我的成绩', path: '/student/grades', icon: ClipboardDocumentListIcon },
      { title: '学习社区', path: '/community', icon: UserGroupIcon },
      ...commonLinks
    ]
  } else if (authStore.user?.role === 'TEACHER') {
    return [
              { title: '教师工作台', path: '/teacher/dashboard', icon: HomeIcon },
      { title: '课程管理', path: '/teacher/courses', icon: AcademicCapIcon },
      { title: '作业批改', path: '/teacher/grading', icon: ClipboardDocumentListIcon },
      { title: '数据分析', path: '/teacher/analytics', icon: ChartBarIcon },
      ...commonLinks
    ]
  } else {
    return [
      { title: '登录', path: '/auth/login', icon: HomeIcon },
      { title: '注册', path: '/auth/register', icon: UserGroupIcon },
      ...commonLinks
    ]
  }
})

// 方法
const goHome = () => {
  if (authStore.isAuthenticated) {
    if (authStore.user?.role === 'STUDENT') {
      router.push('/student')
    } else if (authStore.user?.role === 'TEACHER') {
              router.push('/teacher/dashboard')
    } else {
      router.push('/')
    }
  } else {
    router.push('/auth/login')
  }
}

const goBack = () => {
  if (canGoBack.value) {
    router.go(-1)
  } else {
    goHome()
  }
}

const browseCourses = () => {
  if (authStore.user?.role === 'STUDENT') {
    router.push('/student/courses')
  } else if (authStore.user?.role === 'TEACHER') {
    router.push('/teacher/courses')
  } else {
    router.push('/auth/login')
  }
}

const contactSupport = () => {
  router.push('/help')
}

const handleSearch = () => {
  const query = searchQuery.value.trim()
  if (!query) return

  // 基于用户角色和搜索内容进行智能导航
  const lowerQuery = query.toLowerCase()
  
  if (lowerQuery.includes('课程') || lowerQuery.includes('course')) {
    browseCourses()
  } else if (lowerQuery.includes('作业') || lowerQuery.includes('assignment')) {
    if (authStore.user?.role === 'STUDENT') {
              router.push('/student/grades')
    } else if (authStore.user?.role === 'TEACHER') {
      router.push('/teacher/grading')
    }
  } else if (lowerQuery.includes('成绩') || lowerQuery.includes('grade')) {
    if (authStore.user?.role === 'STUDENT') {
      router.push('/student/grades')
    } else if (authStore.user?.role === 'TEACHER') {
      router.push('/teacher/analytics')
    }
  } else if (lowerQuery.includes('帮助') || lowerQuery.includes('help')) {
    router.push('/help')
  } else if (lowerQuery.includes('社区') || lowerQuery.includes('community')) {
    router.push('/community')
  } else {
    // 默认跳转到首页并显示搜索结果
    uiStore.showNotification({
      type: 'info',
      title: '搜索提示',
      message: `正在为您搜索"${query}"相关内容...`
    })
    goHome()
  }
  
  // 清空搜索框
  searchQuery.value = ''
}

// 生命周期
onMounted(() => {
  // 记录404错误用于分析
  console.warn(`404 页面访问: ${window.location.pathname}`)
  
  // 可以在这里添加错误上报逻辑
  // errorReporting.log('404_error', { path: window.location.pathname })
})
</script>

<style scoped>
/* 自定义动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 渐变文字效果 */
.bg-clip-text {
  background-clip: text;
  -webkit-background-clip: text;
}

/* 毛玻璃效果 */
.backdrop-blur {
  backdrop-filter: blur(12px);
}
</style> 