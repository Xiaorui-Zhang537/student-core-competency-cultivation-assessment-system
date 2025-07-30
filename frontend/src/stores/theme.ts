import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const isDark = ref(false)

  // 计算属性
  const currentTheme = computed(() => isDark.value ? 'dark' : 'light')

  // 方法
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  const setTheme = (theme: 'light' | 'dark') => {
    isDark.value = theme === 'dark'
  }

  const initTheme = () => {
    // 从本地存储读取主题设置
    const savedTheme = localStorage.getItem('theme')
    
    if (savedTheme) {
      isDark.value = savedTheme === 'dark'
    } else {
      // 如果没有保存的主题，使用系统偏好
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    
    // 应用主题到文档
    applyTheme()
  }

  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 监听主题变化并保存到本地存储
  watch(isDark, (newValue) => {
    localStorage.setItem('theme', newValue ? 'dark' : 'light')
    applyTheme()
  })

  return {
    // 状态
    isDark,
    
    // 计算属性
    currentTheme,
    
    // 方法
    toggleTheme,
    setTheme,
    initTheme
  }
}) 