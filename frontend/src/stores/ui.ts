import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // 状态
  const isDarkMode = ref(false)
  type ThemeName = 'retro' | 'dracula' | 'light' | 'dark' | 'cupcake' | 'coffee'
  const themeName = ref<ThemeName>('retro')
  // 新增：背景选择（明/暗各自独立）
  type LightBackground = 'none' | 'aurora' | 'tetris'
  type DarkBackground = 'none' | 'neural' | 'meteors'
  const backgroundLight = ref<LightBackground>('none')
  const backgroundDark = ref<DarkBackground>('none')
  type CursorTrailMode = 'off' | 'fluid' | 'smooth' | 'tailed'
  const cursorTrailMode = ref<CursorTrailMode>('off')
  type CursorStyle = 'arrow' | 'triangle' | 'teardrop'
  const cursorStyle = ref<CursorStyle>('arrow')
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
    if (storedThemeRaw === 'retro' || storedThemeRaw === 'dracula' || storedThemeRaw === 'light' || storedThemeRaw === 'dark' || storedThemeRaw === 'cupcake' || storedThemeRaw === 'coffee') {
      resolved = storedThemeRaw as ThemeName
    }
    themeName.value = resolved ?? (isDarkMode.value ? 'dracula' : 'retro')
    updateThemeClasses()

    // 初始化鼠标轨迹模式
    try {
      const storedCursor = localStorage.getItem('cursorTrailMode')
      if (storedCursor === 'off' || storedCursor === 'fluid' || storedCursor === 'smooth') {
        cursorTrailMode.value = storedCursor as CursorTrailMode
      } else {
        cursorTrailMode.value = 'off'
      }
      const storedStyle = localStorage.getItem('cursorStyle')
      if (storedStyle === 'arrow' || storedStyle === 'triangle' || storedStyle === 'teardrop') {
        cursorStyle.value = storedStyle as CursorStyle
      } else {
        cursorStyle.value = 'arrow'
      }
    } catch {
      cursorTrailMode.value = 'off'
      cursorStyle.value = 'arrow'
    }
    // 初始化背景偏好
    try {
      const lb = localStorage.getItem('backgroundLight')
      if (lb === 'none' || lb === 'aurora' || lb === 'tetris') {
        backgroundLight.value = lb as LightBackground
      } else {
        backgroundLight.value = 'none'
      }
      const db = localStorage.getItem('backgroundDark')
      if (db === 'none' || db === 'neural' || db === 'meteors') {
        backgroundDark.value = db as DarkBackground
      } else {
        backgroundDark.value = 'none'
      }
    } catch {
      backgroundLight.value = 'none'
      backgroundDark.value = 'none'
    }
  }

  // 旧主题相关初始化已移除

  // 更新暗黑模式类
  const updateThemeClasses = () => {
    const root = document.documentElement
    // dark class for existing styles
    const dark = (themeName.value === 'dracula' || themeName.value === 'dark' || themeName.value === 'coffee')
    if (dark) root.classList.add('dark')
    else root.classList.remove('dark')

    // daisyUI theme via data-theme
    root.setAttribute('data-theme', themeName.value)

    // 派发全局主题变更事件，供图表等组件进行轻量重绘而无需各自监听 DOM 变更
    try {
      const detail = { themeName: themeName.value, isDark: dark }
      // 使用 rAF 确保 DOM 属性已更新完成
      requestAnimationFrame(() => {
        try { window.dispatchEvent(new CustomEvent('theme:changed', { detail })) } catch {}
      })
    } catch {}
  }
  

  // 监听暗黑模式变化
  watch(isDarkMode, (newValue) => {
    localStorage.setItem('darkMode', JSON.stringify(newValue))
    // 若手动切换深浅，则同步主题名（保持当前主题家族不变：retro/dracula 或 light/dark）
    const inLightDark = (themeName.value === 'light' || themeName.value === 'dark')
    const inCupCoffee = (themeName.value === 'cupcake' || themeName.value === 'coffee')
    const desired: ThemeName = newValue
      ? (inLightDark ? 'dark' : (inCupCoffee ? 'coffee' : 'dracula'))
      : (inLightDark ? 'light' : (inCupCoffee ? 'cupcake' : 'retro'))
    if (themeName.value !== desired) themeName.value = desired
    updateThemeClasses()
  })
  watch(themeName, (v) => {
    localStorage.setItem('themeName', v)
    updateThemeClasses()
  })

  // 监听鼠标轨迹模式变化并持久化
  watch(cursorTrailMode, (v) => {
    try { localStorage.setItem('cursorTrailMode', v) } catch {}
  })
  watch(cursorStyle, (v) => {
    try { localStorage.setItem('cursorStyle', v) } catch {}
  })
  // 持久化背景偏好
  watch(backgroundLight, (v) => {
    try { localStorage.setItem('backgroundLight', v) } catch {}
  })
  watch(backgroundDark, (v) => {
    try { localStorage.setItem('backgroundDark', v) } catch {}
  })
  

  // 方法
  const toggleDarkMode = () => {
    try {
      const root = document.documentElement
      root.classList.add('theme-switching')
      isDarkMode.value = !isDarkMode.value
      const inLightDark = (themeName.value === 'light' || themeName.value === 'dark')
      const inCupCoffee = (themeName.value === 'cupcake' || themeName.value === 'coffee')
      themeName.value = isDarkMode.value
        ? (inLightDark ? 'dark' : (inCupCoffee ? 'coffee' : 'dracula'))
        : (inLightDark ? 'light' : (inCupCoffee ? 'cupcake' : 'retro'))
      // 移除过渡类（与 CSS 的 450ms 保持一致，稍加余量）
      window.setTimeout(() => root.classList.remove('theme-switching'), 520)
    } catch {
      isDarkMode.value = !isDarkMode.value
      const inLightDark = (themeName.value === 'light' || themeName.value === 'dark')
      const inCupCoffee = (themeName.value === 'cupcake' || themeName.value === 'coffee')
      themeName.value = isDarkMode.value
        ? (inLightDark ? 'dark' : (inCupCoffee ? 'coffee' : 'dracula'))
        : (inLightDark ? 'light' : (inCupCoffee ? 'cupcake' : 'retro'))
    }
  }

  const setTheme = (name: ThemeName) => {
    try {
      const dark = (name === 'dracula' || name === 'dark' || name === 'coffee')
      // 先派发 theme:changing，组件可提前降级动画/隐藏 tooltip
      try { window.dispatchEvent(new CustomEvent('theme:changing', { detail: { nextTheme: name, isDark: dark } })) } catch {}
      themeName.value = name
      isDarkMode.value = dark
    } catch {
      themeName.value = name
      isDarkMode.value = (name === 'dracula' || name === 'dark' || name === 'coffee')
    }
  }

  const setCursorTrailMode = (mode: CursorTrailMode) => {
    cursorTrailMode.value = mode
  }
  const setCursorStyle = (style: CursorStyle) => {
    cursorStyle.value = style
  }

  // 背景设置方法
  const setBackgroundLight = (bg: LightBackground) => {
    backgroundLight.value = bg
  }
  const setBackgroundDark = (bg: DarkBackground) => {
    backgroundDark.value = bg
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
    backgroundLight,
    backgroundDark,
    cursorTrailMode,
    cursorStyle,
    // 方法
    initDarkMode,
    toggleDarkMode,
    setTheme,
    setBackgroundLight,
    setBackgroundDark,
    setCursorTrailMode,
    setCursorStyle,
    toggleSidebar,
    closeSidebar,
    showNotification,
    removeNotification,
    clearNotifications,
    setLoading,
  }
})