<template>
  <!-- 强制刷新标记: v2025-07-29-11:16 -->
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">欢迎回来</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        请登录您的账户以继续学习
      </p>
    </div>

    <!-- 开发环境快速登录提示 -->
    <div v-if="isDevelopment" class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
      <p class="text-sm text-blue-600 text-center">
        🔧 开发模式 - 快速登录测试账号：
      </p>
      <div class="flex gap-2 mt-2">
        <button
          @click="quickLogin('teacher')"
          class="flex-1 px-3 py-2 text-sm bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
        >
          教师登录 (teacher/password)
        </button>
        <button
          @click="quickLogin('student')"
          class="flex-1 px-3 py-2 text-sm bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
        >
          学生登录 (student/password)
        </button>
      </div>
    </div>

    <form @submit.prevent="handleLogin" class="space-y-6">
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          用户名或邮箱
        </label>
        <input
          id="username"
          v-model="form.username"
          type="text"
          required
          class="input"
          placeholder="请输入用户名或邮箱"
          :disabled="loading"
        />
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          密码
        </label>
        <div class="relative">
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            required
            class="input pr-10"
            placeholder="请输入密码"
            :disabled="loading"
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute inset-y-0 right-0 pr-3 flex items-center"
          >
            <svg v-if="showPassword" class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <svg v-else class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
            </svg>
          </button>
        </div>
      </div>

      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <input
            id="remember"
            v-model="form.remember"
            type="checkbox"
            class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
          />
          <label for="remember" class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
            记住我
          </label>
        </div>

        <div class="text-sm">
          <a href="#" class="font-medium text-primary-600 hover:text-primary-500">
            忘记密码？
          </a>
        </div>
      </div>

      <div>
        <button
          type="submit"
          :disabled="loading"
          class="w-full btn btn-primary"
        >
          <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </form>

    <div class="mt-6">
      <div class="relative">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-300 dark:border-gray-600" />
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="px-2 bg-white dark:bg-gray-800 text-gray-500">或</span>
        </div>
      </div>

      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600 dark:text-gray-400">
          还没有账户？
          <router-link to="/auth/register" class="font-medium text-primary-600 hover:text-primary-500">
            立即注册
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import type { LoginCredentials } from '@/types/auth'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

const loading = ref(false)
const showPassword = ref(false)
const isDevelopment = ref(import.meta.env.DEV)

const form = ref<LoginCredentials>({
  username: '',
  password: '',
  remember: false
})

// 快速登录
const quickLogin = async (role: 'teacher' | 'student') => {
  const credentials = {
    teacher: { username: 'teacher', password: 'password' },
    student: { username: 'student', password: 'password' }
  }
  
  form.value = credentials[role]
  loading.value = true
  
  try {
  await authStore.login(credentials[role])
    
    uiStore.showNotification({
      type: 'success',
      title: '登录成功',
      message: `欢迎回来，${authStore.user?.displayName || authStore.user?.username}！`
    })
    
    // 使用 nextTick 确保状态更新完成
    await nextTick()
    
    // 使用 Vue Router 进行跳转
    const userRole = authStore.user?.role
    const targetRoute = userRole === 'teacher' ? '/teacher/dashboard' : '/student/dashboard'
    
    // 使用 router.push 进行跳转
    await router.push(targetRoute)
    
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '登录失败',
      message: error.message || '登录时发生错误，请稍后重试'
    })
  } finally {
    loading.value = false
  }
}

const handleLogin = async () => {
  loading.value = true
  
  try {
    await authStore.login(form.value)
    
    uiStore.showNotification({
      type: 'success',
      title: '登录成功',
      message: `欢迎回来，${authStore.user?.displayName || authStore.user?.username}！`
    })
    
    // 使用 nextTick 确保状态更新完成
    await nextTick()
    
    // 使用 Vue Router 进行跳转
    const userRole = authStore.user?.role
    const targetRoute = userRole === 'teacher' ? '/teacher/dashboard' : '/student/dashboard'
    
    // 使用 router.push 进行跳转
    await router.push(targetRoute)
    
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '登录失败',
      message: error.message || '登录时发生错误，请稍后重试'
    })
  } finally {
    loading.value = false
  }
}
</script> 