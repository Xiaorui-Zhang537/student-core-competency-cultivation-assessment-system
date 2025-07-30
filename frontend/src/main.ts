import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import '@/styles/main.postcss'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化store
const uiStore = useUIStore()
const authStore = useAuthStore()

// 初始化暗黑模式
uiStore.initDarkMode()

// 尝试自动登录
authStore.initAuth().catch(() => {
  // 忽略初始化认证失败
})

app.mount('#app') 