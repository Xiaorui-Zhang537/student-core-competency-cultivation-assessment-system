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

    <!-- 全局通知系统（玻璃） -->
    <div class="fixed top-4 right-4 z-50 space-y-2">
      <div class="space-y-2">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="w-auto max-w-[90vw] sm:max-w-[560px] rounded-full pointer-events-auto overflow-hidden transform transition-all duration-300 ease-in-out glass-thin glass-interactive border border-white/20 dark:border-white/12 shadow-md"
          v-glass="{ strength: 'thin', interactive: true }"
        >
          <div class="p-3 sm:p-4 flex items-start">
              <div class="flex-shrink-0">
                <CheckCircleIcon 
                  v-if="notification.type === 'success'" 
                  class="h-6 w-6 text-green-400" 
                />
                <ExclamationTriangleIcon 
                  v-else-if="notification.type === 'warning'" 
                  class="h-6 w-6 text-yellow-400" 
                />
                <XCircleIcon 
                  v-else-if="notification.type === 'error'" 
                  class="h-6 w-6 text-red-400" 
                />
                <InformationCircleIcon 
                  v-else 
                  class="h-6 w-6 text-blue-400" 
                />
              </div>
              <div class="ml-3 min-w-0 flex-1">
                <p class="text-sm font-medium text-gray-900 dark:text-white break-words whitespace-normal">
                  {{ notification.title }}
                </p>
                <p v-if="notification.message" class="mt-1 text-sm text-gray-700 dark:text-gray-300 break-words whitespace-normal">
                  {{ notification.message }}
                </p>
              </div>
              <div class="ml-3 flex-shrink-0 flex">
                <Button size="xs" variant="glass" icon="close" @click="uiStore.removeNotification(notification.id)" />
              </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 全局错误边界弹窗 -->
    <div
      v-if="showErrorModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
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
          <Button class="flex-1" variant="primary" icon="confirm" @click="reloadPage">{{ t('app.error.button.reload') }}</Button>
          <Button class="flex-1" variant="secondary" icon="close" @click="showErrorModal = false">{{ t('app.error.button.close') }}</Button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
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