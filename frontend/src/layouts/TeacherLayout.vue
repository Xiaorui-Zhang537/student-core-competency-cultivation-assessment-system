<template>
  <div class="min-h-screen relative">
    <AnimatedBackground class="fixed inset-0 z-0 pointer-events-none" :particle-count="80" :connect-distance="120" :max-speed="0.4" :opacity="0.5" />
    <!-- 顶部导航栏 -->
    <nav class="bg-white/90 dark:bg-gray-800/80 backdrop-blur-md border-b border-gray-200 dark:border-gray-600 shadow-sm sticky top-0 z-20">
      <div class="px-4 sm:px-6 lg:px-12">
        <div class="flex justify-between h-14">
          <!-- 左侧：Logo和菜单切换 -->
          <div class="flex items-center space-x-6">
            <button
              @click="uiStore.toggleSidebar()"
              class="p-2 -ml-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <bars3-icon class="h-6 w-6" />
            </button>
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">
                学生核心能力培养评估系统
              </h1>
            </div>
          </div>

          <!-- 中间区域移除搜索框（预留占位以保持两侧布局） -->
          <div class="flex-1"></div>

          <!-- 右侧：主题切换、用户菜单 -->
          <div class="ml-4 flex items-center md:ml-6 space-x-3">
            <!-- 主题切换 -->
            <button
              @click="uiStore.toggleDarkMode()"
              class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <sun-icon v-if="uiStore.isDarkMode" class="h-6 w-6" />
              <moon-icon v-else class="h-6 w-6" />
            </button>

            <!-- 用户菜单 -->
            <div class="relative">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <div class="flex items-center space-x-2">
                  <UserAvatar :avatar="(authStore.user as any)?.avatar" :size="28">
                    <span class="text-white font-medium text-sm">{{ (authStore.user?.username || 'T').charAt(0).toUpperCase() }}</span>
                  </UserAvatar>
                  <div class="hidden md:block text-left">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ (authStore.user as any)?.nickname || authStore.user?.username || '教师' }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      教师
                    </p>
                  </div>
                  <chevron-down-icon class="h-4 w-4 text-gray-400" />
                </div>
              </button>

              <!-- 用户下拉菜单 -->
              <div
                v-if="showUserMenu"
                class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 focus:outline-none z-50"
                @click.stop
              >
                <div class="py-1">
                  <router-link
                    to="/teacher/profile"
                    class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    @click="showUserMenu = false"
                  >
                    <div class="flex items-center space-x-2">
                      <user-icon class="h-4 w-4" />
                      <span>个人资料</span>
                    </div>
                  </router-link>
                  <div class="border-t border-gray-100 dark:border-gray-600"></div>
                  <button
                    @click="handleLogout"
                    class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <div class="flex items-center space-x-2">
                      <arrow-right-on-rectangle-icon class="h-4 w-4" />
                      <span>退出登录</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <div class="flex pt-14 relative z-10">
      <!-- 侧边栏 -->
      <aside
        :class="[
          'fixed inset-y-0 left-0 z-30 w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-600 transform transition-transform duration-300 ease-in-out',
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
              工作台
            </router-link>
            
            <router-link
              to="/teacher/courses"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <academic-cap-icon class="mr-3 h-5 w-5" />
              课程管理
            </router-link>
            
            <router-link
              to="/teacher/analytics"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <chart-bar-icon class="mr-3 h-5 w-5" />
              数据分析
            </router-link>
            
            <router-link
              to="/teacher/ai"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <users-icon class="mr-3 h-5 w-5" />
              AI 助理
            </router-link>
            
             <router-link
              to="/teacher/community"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <chat-bubble-left-right-icon class="mr-3 h-5 w-5" />
              学习社区
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import AnimatedBackground from '@/components/ui/AnimatedBackground.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import {
  Bars3Icon,
  SunIcon,
  MoonIcon,
  ChevronDownIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  AcademicCapIcon,
  ChartBarIcon,
  UsersIcon,
  ChatBubbleLeftRightIcon,
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()

// 状态
const showUserMenu = ref(false)

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.relative')) {
    showUserMenu.value = false
  }
}

// 生命周期
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
