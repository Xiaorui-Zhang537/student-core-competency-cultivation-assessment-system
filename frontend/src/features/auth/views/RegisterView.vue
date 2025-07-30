<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">创建账户</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
        加入我们的学习平台，开启您的学习之旅
      </p>
    </div>

    <form @submit.prevent="handleRegister" class="space-y-6">
      <!-- 用户类型选择 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
          账户类型
        </label>
        <div class="grid grid-cols-2 gap-4">
          <label class="relative">
            <input
              v-model="form.role"
              type="radio"
              value="student"
              class="sr-only peer"
            />
            <div class="p-4 border-2 border-gray-300 rounded-lg cursor-pointer peer-checked:border-primary-500 peer-checked:bg-primary-50 dark:peer-checked:bg-primary-900 dark:border-gray-600 hover:border-gray-400">
              <div class="text-center">
                <svg class="w-8 h-8 mx-auto mb-2 text-gray-400 peer-checked:text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 14l9-5-9-5-9 5 9 5z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z" />
                </svg>
                <span class="font-medium">学生</span>
              </div>
            </div>
          </label>
          
          <label class="relative">
            <input
              v-model="form.role"
              type="radio"
              value="teacher"
              class="sr-only peer"
            />
            <div class="p-4 border-2 border-gray-300 rounded-lg cursor-pointer peer-checked:border-primary-500 peer-checked:bg-primary-50 dark:peer-checked:bg-primary-900 dark:border-gray-600 hover:border-gray-400">
              <div class="text-center">
                <svg class="w-8 h-8 mx-auto mb-2 text-gray-400 peer-checked:text-primary-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
                <span class="font-medium">教师</span>
              </div>
            </div>
          </label>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label for="firstName" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            姓
          </label>
          <input
            id="firstName"
            v-model="form.firstName"
            type="text"
            required
            class="input"
            placeholder="请输入姓氏"
          />
        </div>
        
        <div>
          <label for="lastName" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            名
          </label>
          <input
            id="lastName"
            v-model="form.lastName"
            type="text"
            required
            class="input"
            placeholder="请输入名字"
          />
        </div>
      </div>

      <!-- 用户名和邮箱 -->
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          用户名
        </label>
        <input
          id="username"
          v-model="form.username"
          type="text"
          required
          class="input"
          placeholder="请输入用户名"
        />
      </div>

      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          邮箱地址
        </label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          required
          class="input"
          placeholder="请输入邮箱地址"
        />
      </div>

      <!-- 角色特定字段 -->
      <div v-if="form.role === 'student'">
        <label for="grade" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          年级
        </label>
        <select
          id="grade"
                      v-model="form.grade"
          class="input"
        >
          <option value="">请选择年级</option>
          <option value="grade1">一年级</option>
          <option value="grade2">二年级</option>
          <option value="grade3">三年级</option>
          <option value="grade4">四年级</option>
          <option value="grade5">五年级</option>
          <option value="grade6">六年级</option>
        </select>
      </div>

      <div v-if="form.role === 'teacher'">
        <label for="subject" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          任教科目
        </label>
        <input
          id="subject"
                      v-model="form.subject"
          type="text"
          class="input"
          placeholder="请输入任教科目"
        />
      </div>

      <!-- 密码 -->
      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          密码
        </label>
        <input
          id="password"
          v-model="form.password"
          type="password"
          required
          class="input"
          placeholder="请输入密码（至少8位）"
        />
      </div>

      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          确认密码
        </label>
        <input
          id="confirmPassword"
          v-model="form.confirmPassword"
          type="password"
          required
          class="input"
          placeholder="请再次输入密码"
        />
      </div>

      <!-- 提交按钮 -->
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
          {{ loading ? '注册中...' : '创建账户' }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        已有账户？
        <router-link to="/auth/login" class="font-medium text-primary-600 hover:text-primary-500">
          立即登录
        </router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import type { RegisterData } from '@/types/auth'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

const loading = ref(false)

const form = ref<RegisterData>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'student',
    firstName: '',
    lastName: '',
    grade: '',
    subject: '',
    school: ''
})

const handleRegister = async () => {
  // 验证密码
  if (form.value.password !== form.value.confirmPassword) {
    uiStore.showNotification({
      type: 'error',
      title: '注册失败',
      message: '两次输入的密码不一致'
    })
    return
  }

  if (form.value.password.length < 8) {
    uiStore.showNotification({
      type: 'error',
      title: '注册失败',
      message: '密码长度至少为8位'
    })
    return
  }

  loading.value = true
  
  try {
    await authStore.register(form.value)
    
    uiStore.showNotification({
      type: 'success',
      title: '注册成功',
      message: '欢迎加入我们的学习平台！'
    })
    
    // 根据用户角色跳转到对应的仪表盘
    const userRole = authStore.user?.role
    if (userRole === 'teacher') {
      router.push('/teacher/dashboard')
    } else {
      router.push('/student/dashboard')
    }
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '注册失败',
      message: error.message || '注册时发生错误，请稍后重试'
    })
  } finally {
    loading.value = false
  }
}
</script> 