<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <nav class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-600 sticky top-0 z-40">
      <div class="px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-14">
          <div class="flex items-center">
            <button
              @click="uiStore.toggleSidebar()"
              class="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <bars3-icon class="h-6 w-6" />
            </button>
            <div class="flex-shrink-0 flex items-center ml-4 lg:ml-0">
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">学生工作台</h1>
            </div>
          </div>

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
                  class="block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md leading-5 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                  placeholder="搜索课程、作业..."
                  type="search"
                />
              </div>
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

              <div class="relative">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <UserAvatar :avatar="(authStore.user as any)?.avatar" :size="32">
                  <div class="h-8 w-8 rounded-full bg-gradient-to-r from-primary-500 to-purple-600 flex items-center justify-center text-white font-medium text-sm">
                    {{ (authStore.user?.username || 'S').charAt(0).toUpperCase() }}
                  </div>
                </UserAvatar>
                <div class="hidden md:block text-left ml-2">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ (authStore.user as any)?.nickname || authStore.user?.username || '学生' }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">学生</p>
                </div>
                <chevron-down-icon class="h-4 w-4 text-gray-400 ml-1" />
              </button>

              <div
                v-if="showUserMenu"
                class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 focus:outline-none z-50"
                @click.stop
              >
                <div class="py-1">
                  <router-link
                    to="/student/profile"
                    class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                    @click="showUserMenu = false"
                  >
                    <user-icon class="h-4 w-4 inline-block mr-2" /> 个人资料
                  </router-link>
                  <div class="border-t border-gray-100 dark:border-gray-600"></div>
                  <button
                    @click="handleLogout"
                    class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700"
                  >
                    <arrow-right-on-rectangle-icon class="h-4 w-4 inline-block mr-2" /> 退出登录
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <div class="flex pt-14">
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
              to="/student/dashboard"
              exact-active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <home-icon class="mr-3 h-5 w-5" /> 仪表盘
            </router-link>
            <router-link
              to="/student/assignments"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <clipboard-document-list-icon class="mr-3 h-5 w-5" /> 作业
            </router-link>
            <router-link
              to="/student/courses"
              active-class="bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900 dark:text-primary-300"
              class="group flex items-center px-2 py-2 text-sm font-medium rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white border-l-4 border-transparent"
            >
              <academic-cap-icon class="mr-3 h-5 w-5" /> 课程
            </router-link>
          </nav>
        </div>
      </aside>

      <div
        v-if="uiStore.sidebarOpen"
        class="fixed inset-0 z-20 bg-gray-600 bg-opacity-75 lg:hidden"
        @click="uiStore.closeSidebar()"
      ></div>

      <main :class="['flex-1', uiStore.sidebarOpen ? 'lg:pl-64' : 'pl-0']">
        <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
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
import {
  Bars3Icon,
  MagnifyingGlassIcon,
  SunIcon,
  MoonIcon,
  ChevronDownIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  AcademicCapIcon,
  ClipboardDocumentListIcon,
} from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const searchQuery = ref('')

const handleLogout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/auth/login')
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement | null
  if (!target || !target.closest('.relative')) showUserMenu.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>