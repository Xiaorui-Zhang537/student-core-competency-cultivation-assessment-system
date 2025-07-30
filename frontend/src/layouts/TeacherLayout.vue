<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 顶部导航栏 -->
    <nav class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-600 sticky top-0 z-40">
      <div class="px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <!-- 左侧：Logo和菜单切换 -->
          <div class="flex items-center">
            <button
              @click="uiStore.toggleSidebar()"
              class="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500 lg:hidden"
            >
              <bars3-icon class="h-6 w-6" />
            </button>
            <div class="flex-shrink-0 flex items-center ml-4 lg:ml-0">
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">
                教师工作台
              </h1>
            </div>
          </div>

          <!-- 中间：搜索栏 -->
          <div class="flex-1 flex items-center justify-center px-2 lg:ml-6 lg:justify-start">
            <div class="max-w-lg w-full lg:max-w-xs">
              <label for="search" class="sr-only">搜索</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <magnifying-glass-icon class="h-5 w-5 text-gray-400" />
                </div>
                <input
                  id="search"
                  v-model="searchQuery"
                  name="search"
                  class="block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md leading-5 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  placeholder="搜索学生、课程、作业..."
                  type="search"
                />
              </div>
            </div>
          </div>

          <!-- 右侧：通知、主题切换、用户菜单 -->
          <div class="ml-4 flex items-center md:ml-6 space-x-3">
            <!-- 快速统计 -->
            <div class="hidden md:flex items-center space-x-4 text-sm">
              <div class="text-gray-600 dark:text-gray-400">
                待评分: 
                <span class="font-medium text-orange-600 dark:text-orange-400">
                  {{ teacherStore.pendingGradingCount }}
                </span>
              </div>
              <div class="text-gray-600 dark:text-gray-400">
                活跃课程: 
                <span class="font-medium text-green-600 dark:text-green-400">
                  {{ teacherStore.activeCourses }}
                </span>
              </div>
            </div>

            <!-- 通知按钮 -->
            <div class="relative">
              <button
                @click="showNotifications = !showNotifications"
                class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 relative"
              >
                <bell-icon class="h-6 w-6" />
                <span
                  v-if="teacherStore.unreadNotifications.length > 0"
                  class="absolute -top-1 -right-1 h-4 w-4 bg-red-500 rounded-full flex items-center justify-center text-xs text-white font-medium"
                >
                  {{ teacherStore.unreadNotifications.length > 9 ? '9+' : teacherStore.unreadNotifications.length }}
                </span>
              </button>

              <!-- 通知下拉菜单 -->
              <div
                v-if="showNotifications"
                class="origin-top-right absolute right-0 mt-2 w-80 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 focus:outline-none z-50"
                @click.stop
              >
                <div class="py-1">
                  <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-600">
                    <div class="flex justify-between items-center">
                      <h3 class="text-sm font-medium text-gray-900 dark:text-white">通知</h3>
                      <button
                        v-if="teacherStore.unreadNotifications.length > 0"
                        @click="teacherStore.markAllNotificationsAsRead()"
                        class="text-xs text-primary-600 hover:text-primary-500"
                      >
                        全部已读
                      </button>
                    </div>
                  </div>
                  <div class="max-h-64 overflow-y-auto">
                    <div
                      v-for="notification in teacherStore.notifications.slice(0, 5)"
                      :key="notification.id"
                      class="px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer border-b border-gray-100 dark:border-gray-700 last:border-b-0"
                      @click="handleNotificationClick(notification)"
                    >
                      <div class="flex items-start space-x-3">
                        <div
                          :class="[
                            'flex-shrink-0 w-2 h-2 mt-2 rounded-full',
                            notification.isRead ? 'bg-gray-300 dark:bg-gray-600' : 'bg-primary-500'
                          ]"
                        ></div>
                        <div class="flex-1 min-w-0">
                          <p class="text-sm font-medium text-gray-900 dark:text-white">
                            {{ notification.title }}
                          </p>
                          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">
                            {{ notification.message }}
                          </p>
                          <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">
                            {{ formatRelativeTime(notification.timestamp) }}
                          </p>
                        </div>
                      </div>
                    </div>
                    <div v-if="teacherStore.notifications.length === 0" class="px-4 py-8 text-center">
                      <p class="text-sm text-gray-500 dark:text-gray-400">暂无通知</p>
                    </div>
                  </div>
                  <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-600">
                    <button
                      @click="$router.push('/teacher/notifications')"
                      class="text-sm text-primary-600 hover:text-primary-500 w-full text-center"
                    >
                      查看所有通知
                    </button>
                  </div>
                </div>
              </div>
            </div>

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
                  <div class="h-8 w-8 rounded-full bg-gradient-to-r from-primary-500 to-purple-600 flex items-center justify-center text-white font-medium text-sm">
                    {{ teacherStore.profile?.name?.charAt(0) || 'T' }}
                  </div>
                  <div class="hidden md:block text-left">
                    <p class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ teacherStore.profile?.name || '教师' }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      {{ teacherStore.profile?.title || '讲师' }}
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
                  <router-link
                    to="/teacher/settings"
                    class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    @click="showUserMenu = false"
                  >
                    <div class="flex items-center space-x-2">
                      <cog-icon class="h-4 w-4" />
                      <span>系统设置</span>
                    </div>
                  </router-link>
                  <router-link
                    to="/teacher/help"
                    class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    @click="showUserMenu = false"
                  >
                    <div class="flex items-center space-x-2">
                      <question-mark-circle-icon class="h-4 w-4" />
                      <span>帮助中心</span>
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
    <div class="flex">
      <!-- 侧边栏 -->
      <aside
        :class="[
          'fixed inset-y-0 left-0 z-30 w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-600 transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0',
          uiStore.sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        ]"
        style="top: 4rem;"
      >
        <div class="h-full px-3 py-4 overflow-y-auto">
          <nav class="space-y-2">
            <!-- 主要导航 -->
            <div class="space-y-1">
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
                to="/teacher/grading"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent relative"
              >
                <clipboard-document-check-icon class="mr-3 h-5 w-5" />
                作业评分
                <span
                  v-if="teacherStore.pendingGradingCount > 0"
                  class="ml-auto bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center"
                >
                  {{ teacherStore.pendingGradingCount > 9 ? '9+' : teacherStore.pendingGradingCount }}
                </span>
              </router-link>
              
              <router-link
                to="/teacher/students"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <users-icon class="mr-3 h-5 w-5" />
                学生管理
              </router-link>
              
              <router-link
                to="/teacher/analytics"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <chart-bar-icon class="mr-3 h-5 w-5" />
                数据分析
              </router-link>
            </div>

            <!-- 分隔线 -->
            <div class="border-t border-gray-200 dark:border-gray-600 pt-4 mt-4">
              <p class="px-3 text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                工具
              </p>
            </div>

            <!-- 工具导航 -->
            <div class="space-y-1">
              <router-link
                to="/teacher/messages"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <chat-bubble-left-right-icon class="mr-3 h-5 w-5" />
                消息中心
              </router-link>
              
              <router-link
                to="/teacher/calendar"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <calendar-icon class="mr-3 h-5 w-5" />
                日程安排
              </router-link>
              
              <router-link
                to="/teacher/resources"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <folder-icon class="mr-3 h-5 w-5" />
                教学资源
              </router-link>
              
              <router-link
                to="/teacher/reports"
                active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
                class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
              >
                <document-chart-bar-icon class="mr-3 h-5 w-5" />
                报告导出
              </router-link>
            </div>
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
      <main class="flex-1 lg:ml-0">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useTeacherStore } from '@/stores/teacher'
import {
  Bars3Icon,
  BellIcon,
  MagnifyingGlassIcon,
  SunIcon,
  MoonIcon,
  ChevronDownIcon,
  UserIcon,
  CogIcon,
  QuestionMarkCircleIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  AcademicCapIcon,
  ClipboardDocumentCheckIcon,
  UsersIcon,
  ChartBarIcon,
  ChatBubbleLeftRightIcon,
  CalendarIcon,
  FolderIcon,
  DocumentChartBarIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const teacherStore = useTeacherStore()

// 状态
const showNotifications = ref(false)
const showUserMenu = ref(false)
const searchQuery = ref('')

// 方法
const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const date = new Date(timestamp)
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) return '刚刚'
  if (diffInHours < 24) return `${diffInHours}小时前`
  if (diffInHours < 48) return '昨天'
  return `${Math.floor(diffInHours / 24)}天前`
}

const handleNotificationClick = (notification: any) => {
  teacherStore.markNotificationAsRead(notification.id)
  showNotifications.value = false
  
  if (notification.metadata?.url) {
    router.push(notification.metadata.url)
  }
}

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  
  if (!target.closest('.relative')) {
    showNotifications.value = false
    showUserMenu.value = false
  }
}

// 生命周期
onMounted(() => {
  // 初始化教师数据
  teacherStore.initTeacherData()
  
  // 添加全局点击监听
  document.addEventListener('click', handleClickOutside)
  
  // 获取通知
  teacherStore.fetchNotifications()
})

// 清理事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 