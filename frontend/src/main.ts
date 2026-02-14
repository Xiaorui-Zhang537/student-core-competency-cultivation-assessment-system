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
import PageScaffold from '@/components/ui/PageScaffold.vue'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(i18n)
  app.directive('glass', GlassDirective)
  app.directive('click-outside', ClickOutsideDirective)
  // 全局注册常用 UI 组件，避免各页面重复导入遗漏
  app.component('PageHeader', PageHeader)
  app.component('PageScaffold', PageScaffold)
  app.component('LoadingOverlay', LoadingOverlay)

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

  // 初始化国际化：在安装路由前完成语言归一化与命名空间加载，避免首次进入路由时出现 zh/en 缺键告警
  const raw = String(i18n.global.locale.value)
  const normalized = raw === 'zh' ? 'zh-CN' : raw === 'en' ? 'en-US' : raw
  await setLocale(normalized as any)

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
  window.addEventListener('storage', (e) => {
    if (e.key === 'token') {
      setupSse()
      // 登出时也断开聊天 SSE
      try { if (!e.newValue) { const cs = useChatStore(pinia); cs.disconnectChatSse() } } catch {}
    }
  })
  window.addEventListener('beforeunload', () => {
    if (notificationStream) notificationStream.disconnect()
    try { const cs = useChatStore(pinia); cs.disconnectChatSse() } catch {}
  })
}

initializeApp()
