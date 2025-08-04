import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import '@/styles/main.postcss'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)

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
  
  // 最后挂载应用
  app.mount('#app')
}

initializeApp()
