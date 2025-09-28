import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // 状态
  const isDarkMode = ref(false)
  type ThemeName = 'retro' | 'dracula'
  const themeName = ref<ThemeName>('retro')
  const sidebarOpen = ref(true)
  const loading = ref(false)
  const notifications = ref<Array<{
    id: string
    type: 'success' | 'error' | 'warning' | 'info'
    title: string
    message: string
    timeout?: number
  }>>([])
  // 旧主题背景与玻璃强度已移除

  // 初始化暗黑模式
  const initDarkMode = () => {
    const stored = localStorage.getItem('darkMode')
    if (stored) {
      isDarkMode.value = JSON.parse(stored)
    } else {
      // 检查系统偏好
      isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    // 同步主题名称（第一次加载时依据 isDarkMode）
    const storedThemeRaw = localStorage.getItem('themeName')
    let resolved: ThemeName | null = null
    if (storedThemeRaw === 'retro' || storedThemeRaw === 'dracula') {
      resolved = storedThemeRaw
    }
    themeName.value = resolved ?? (isDarkMode.value ? 'dracula' : 'retro')
    updateThemeClasses()
  }

  // 旧主题相关初始化已移除

  // 更新暗黑模式类
  const updateThemeClasses = () => {
    const root = document.documentElement
    // dark class for existing styles
    const dark = (themeName.value === 'dracula')
    if (dark) root.classList.add('dark')
    else root.classList.remove('dark')

    // daisyUI theme via data-theme
    root.setAttribute('data-theme', themeName.value)
  }
  

  // 监听暗黑模式变化
  watch(isDarkMode, (newValue) => {
    localStorage.setItem('darkMode', JSON.stringify(newValue))
    // 若手动切换深浅，则同步主题名（仅当当前主题与深浅不一致时进行校正）
    const desired: ThemeName = (newValue ? 'dracula' : 'retro')
    if (themeName.value !== desired) themeName.value = desired
    updateThemeClasses()
  })
  watch(themeName, (v) => {
    localStorage.setItem('themeName', v)
    updateThemeClasses()
  })
  

  // 方法
  const toggleDarkMode = () => {
    try {
      const root = document.documentElement
      root.classList.add('theme-switching')
      isDarkMode.value = !isDarkMode.value
      themeName.value = isDarkMode.value ? 'dracula' : 'retro'
      // 移除过渡类（与 CSS 的 450ms 保持一致，稍加余量）
      window.setTimeout(() => root.classList.remove('theme-switching'), 520)
    } catch {
      isDarkMode.value = !isDarkMode.value
      themeName.value = isDarkMode.value ? 'dracula' : 'retro'
    }
  }

  const setTheme = (name: ThemeName) => {
    themeName.value = name
    isDarkMode.value = (name === 'dracula')
  }

  const toggleSidebar = () => {
    sidebarOpen.value = !sidebarOpen.value
  }
  
  const closeSidebar = () => {
      sidebarOpen.value = false
  }

  // 背景与玻璃强度相关功能已删除

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

  const setLoading = (v: boolean) => {
    loading.value = v
  }

  return {
    // 状态
    isDarkMode,
    sidebarOpen,
    loading,
    notifications,
    themeName,
    // 方法
    initDarkMode,
    toggleDarkMode,
    setTheme,
    toggleSidebar,
    closeSidebar,
    showNotification,
    removeNotification,
    clearNotifications,
    setLoading,
  }
})