import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import '@/styles/main.postcss'
import { i18n, loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(i18n)

  const authStore = useAuthStore()
  const uiStore = useUIStore()

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
}

initializeApp()
