<template>
  <div id="app" class="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-300">
    <!-- 全局加载状态 -->
    <div 
      v-if="globalLoading" 
      class="fixed inset-0 bg-white/90 dark:bg-gray-900/90 backdrop-blur-sm z-50 flex items-center justify-center"
    >
      <div class="text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-primary-100 dark:bg-primary-900/30 rounded-2xl mb-4">
          <div class="w-8 h-8 border-3 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-1">{{ loadingText }}</h3>
        <p class="text-sm text-gray-600 dark:text-gray-400">请稍候...</p>
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

    <!-- 全局通知系统 -->
    <div class="fixed top-4 right-4 z-50 space-y-2">
      <transition-group name="notification" tag="div">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="[
            'max-w-sm w-full bg-white dark:bg-gray-800 shadow-lg rounded-lg pointer-events-auto ring-1 ring-black ring-opacity-5 overflow-hidden',
            'transform transition-all duration-300 ease-in-out'
          ]"
        >
          <div class="p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
                <check-circle-icon 
                  v-if="notification.type === 'success'" 
                  class="h-6 w-6 text-green-400" 
                />
                <exclamation-triangle-icon 
                  v-else-if="notification.type === 'warning'" 
                  class="h-6 w-6 text-yellow-400" 
                />
                <x-circle-icon 
                  v-else-if="notification.type === 'error'" 
                  class="h-6 w-6 text-red-400" 
                />
                <information-circle-icon 
                  v-else 
                  class="h-6 w-6 text-blue-400" 
                />
              </div>
              <div class="ml-3 w-0 flex-1">
                <p class="text-sm font-medium text-gray-900 dark:text-white">
                  {{ notification.title }}
                </p>
                <p v-if="notification.message" class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {{ notification.message }}
                </p>
              </div>
              <div class="ml-4 flex-shrink-0 flex">
                <button
                  @click="uiStore.removeNotification(notification.id)"
                  class="bg-white dark:bg-gray-800 rounded-md inline-flex text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                >
                  <x-mark-icon class="h-5 w-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 全局错误边界弹窗 -->
    <div
      v-if="showErrorModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="showErrorModal = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full p-6">
        <div class="flex items-center mb-4">
          <x-circle-icon class="w-8 h-8 text-red-500 mr-3" />
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">应用程序错误</h3>
        </div>
        <p class="text-gray-600 dark:text-gray-400 mb-6">
          应用程序遇到了一个意外错误。我们已经记录了这个问题，请稍后重试。
        </p>
        <div class="flex space-x-3">
          <button
            @click="reloadPage"
            class="flex-1 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
          >
            刷新页面
          </button>
          <button
            @click="showErrorModal = false"
            class="flex-1 bg-gray-200 dark:bg-gray-600 text-gray-900 dark:text-white px-4 py-2 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-500 transition-colors"
          >
            关闭
          </button>
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
          <p class="text-sm font-medium text-orange-800 dark:text-orange-200">网络连接断开</p>
          <p class="text-xs text-orange-600 dark:text-orange-300">请检查您的网络连接</p>
        </div>
      </div>
    </div>

    <!-- 开发模式指示器 -->
    <div
      v-if="isDevelopment"
      class="fixed bottom-4 left-4 bg-blue-100 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800 rounded-lg px-3 py-1 z-30"
    >
      <p class="text-xs font-medium text-blue-800 dark:text-blue-200">开发模式</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  XCircleIcon,
  InformationCircleIcon,
  XMarkIcon,
  WifiIcon
} from '@heroicons/vue/24/outline'

// Stores
const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const { notifications } = storeToRefs(uiStore)

// 响应式状态
const globalLoading = ref(false)
const routeLoading = ref(false)
const loadingText = ref('加载中')
const showErrorModal = ref(false)
const isOnline = ref(navigator.onLine)
const isDevelopment = ref(import.meta.env.DEV)

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
    title: '发生错误',
    message: error.message || '未知错误',
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
    title: '网络已连接',
    message: '网络连接已恢复',
    timeout: 3000
  })
}

const handleOffline = () => {
  isOnline.value = false
}

// 生命周期
onMounted(() => {
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