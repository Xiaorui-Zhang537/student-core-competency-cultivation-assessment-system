import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useUIStore } from '@/stores/ui'
import { createNotificationStream } from '@/composables/useNotificationStream'
import '@/styles/main.postcss'
import GlassDirective from '@/directives/glass'
import ClickOutsideDirective from '@/directives/clickOutside'
import { i18n, loadLocaleMessages, REQUIRED_NAMESPACES, setLocale } from '@/i18n'
import PageHeader from '@/components/ui/PageHeader.vue'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(i18n)
  app.directive('glass', GlassDirective)
  app.directive('click-outside', ClickOutsideDirective)
  // 全局注册常用 UI 组件，避免各页面重复导入遗漏
  app.component('PageHeader', PageHeader)

  // 辅助调试：在控制台暴露聊天 store（开发/生产均可，用后可移除）
  try {
    const chatDebug = useChatStore(pinia)
    ;(window as any).__chat = chatDebug
    ;(window as any).__openChat = (id?: any, name?: any, courseId?: any) => chatDebug.openChat(id, name, courseId)
    ;(window as any).__closeChat = () => chatDebug.closeChat()
    // 便捷别名（无下划线）：
    ;(window as any).chat = chatDebug
    ;(window as any).openChat = (id?: any, name?: any, courseId?: any) => chatDebug.openChat(id, name, courseId)
    ;(window as any).closeChat = () => chatDebug.closeChat()
  } catch {}

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

  // 初始化国际化：归一化语言代码并加载命名空间，避免出现 zh/en 缺键
  const raw = String(i18n.global.locale.value)
  const normalized = raw === 'zh' ? 'zh-CN' : raw === 'en' ? 'en-US' : (raw as 'zh-CN' | 'en-US')
  if (normalized !== raw) {
    await setLocale(normalized)
  } else {
    await loadLocaleMessages(normalized, [...REQUIRED_NAMESPACES])
  }
  
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
