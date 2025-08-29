import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import { createNotificationStream } from '@/composables/useNotificationStream'
import '@/styles/main.postcss'
import GlassDirective from '@/directives/glass'
import { i18n, loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(i18n)
  app.directive('glass', GlassDirective)

  const authStore = useAuthStore()
  const uiStore = useUIStore()
  let notificationStream: ReturnType<typeof createNotificationStream> | null = null

  try {
    if (authStore.token) {
      await authStore.fetchUser()
    }
  } catch (error) {
    console.error('自动登录失败，token可能已过期:', error)
    // 如果获取用户信息失败，可以选择登出，清理无效的token
    authStore.logout() 
  }

  // 只有在认证状态确定后才使用路由
  app.use(router)

  // 初始化UI相关的store
  uiStore.initDarkMode()

  // 初始化国际化：统一加载所需命名空间，避免初次渲染缺键
  const locale = (i18n.global.locale.value as 'zh-CN' | 'en-US')
  await loadLocaleMessages(locale, [...REQUIRED_NAMESPACES])
  
  // 最后挂载应用
  app.mount('#app')

  // 基于登录状态管理 SSE 连接
  const setupSse = () => {
    const isAuthed = authStore.isAuthenticated
    if (isAuthed) {
      if (!notificationStream) notificationStream = createNotificationStream()
      notificationStream.connect()
    } else {
      if (notificationStream) {
        notificationStream.disconnect()
        notificationStream = null
      }
    }
  }
  setupSse()
  window.addEventListener('storage', (e) => { if (e.key === 'token') setupSse() })
  window.addEventListener('beforeunload', () => { if (notificationStream) notificationStream.disconnect() })
}

initializeApp()
