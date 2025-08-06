<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">欢迎回来</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">请登录您的账户以继续学习</p>
    </div>

    <!-- Quick Login for Dev -->
    <div v-if="isDevelopment" class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
        <div class="flex gap-2 mt-2">
            <button @click="handleLogin({ username: 'teacher', password: 'password' })" class="flex-1 btn btn-sm bg-blue-500 hover:bg-blue-600 text-white">教师登录</button>
            <button @click="handleLogin({ username: 'student', password: 'password' })" class="flex-1 btn btn-sm bg-green-500 hover:bg-green-600 text-white">学生登录</button>
        </div>
    </div>

    <form @submit.prevent="handleLogin(form)" class="space-y-6">
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名或邮箱</label>
        <input
          id="username"
          v-model="form.username"
          type="text"
          autocomplete="username"
          required
          class="input"
          :disabled="authStore.loading"
        />
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">密码</label>
        <input
          id="password"
          v-model="form.password"
          type="password"
          autocomplete="current-password"
          required
          class="input"
          :disabled="authStore.loading"
        />
      </div>

      <div class="flex items-center justify-between">
        <div class="text-sm">
          <router-link to="/auth/forgot-password" class="font-medium text-primary-600 hover:text-primary-500">
            忘记密码？
          </router-link>
        </div>
      </div>

      <div>
        <button type="submit" :disabled="authStore.loading" class="w-full btn btn-primary">
          {{ authStore.loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        还没有账户？
        <router-link to="/auth/register" class="font-medium text-primary-600 hover:text-primary-500">
          立即注册
        </router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const isDevelopment = ref(import.meta.env.DEV)
const router = useRouter()

const form = ref<LoginRequest>({ username: '', password: '' })

const handleLogin = async (credentials: LoginRequest) => {
  await authStore.login(credentials)
  await router.push('/student/dashboard')
  window.location.reload()
}
</script>
