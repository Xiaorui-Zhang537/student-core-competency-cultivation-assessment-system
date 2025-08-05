import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // 状态
  const isDarkMode = ref(false)
  const sidebarOpen = ref(true)
  const loading = ref(false)
  const notifications = ref<Array<{
    id: string
    type: 'success' | 'error' | 'warning' | 'info'
    title: string
    message: string
    timeout?: number
  }>>([])

  // 初始化暗黑模式
  const initDarkMode = () => {
    const stored = localStorage.getItem('darkMode')
    if (stored) {
      isDarkMode.value = JSON.parse(stored)
    } else {
      // 检查系统偏好
      isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    updateDarkModeClass()
  }

  // 更新暗黑模式类
  const updateDarkModeClass = () => {
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 监听暗黑模式变化
  watch(isDarkMode, (newValue) => {
    localStorage.setItem('darkMode', JSON.stringify(newValue))
    updateDarkModeClass()
  })

  // 方法
  const toggleDarkMode = () => {
    isDarkMode.value = !isDarkMode.value
  }

  const toggleSidebar = () => {
    sidebarOpen.value = !sidebarOpen.value
  }
  
  const closeSidebar = () => {
      sidebarOpen.value = false
  }

  const showNotification = (notification: {
    type: 'success' | 'error' | 'warning' | 'info'
    title: string
    message: string
    timeout?: number
  }) => {
    const id = Date.now().toString()
    const newNotification = { id, ...notification }
    notifications.value.push(newNotification)

    // 自动移除通知
    if (notification.timeout !== 0) {
      setTimeout(() => {
        removeNotification(id)
      }, notification.timeout || 5000)
    }
  }

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  const clearNotifications = () => {
    notifications.value = []
  }

  return {
    // 状态
    isDarkMode,
    sidebarOpen,
    loading,
    notifications,
    // 方法
    initDarkMode,
    toggleDarkMode,
    toggleSidebar,
    closeSidebar,
    showNotification,
    removeNotification,
    clearNotifications
  }
}) 