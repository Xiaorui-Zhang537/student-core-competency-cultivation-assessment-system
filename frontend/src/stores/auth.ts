import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginCredentials, RegisterData } from '@/types/auth'
import { authAPI } from '@/api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const loading = ref(false)

  // 如果有token但没有用户信息，尝试从localStorage恢复用户信息
  if (token.value && !user.value) {
    const savedUser = localStorage.getItem('auth_user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('恢复用户信息失败:', error)
        // 如果恢复失败，清除相关数据
        token.value = null
        localStorage.removeItem('auth_token')
        localStorage.removeItem('auth_user')
      }
    }
    // 注意：不要在没有用户信息时立即清除token，可能是Mock模式
  }

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const userRole = computed(() => user.value?.role)

  // 方法
  const login = async (credentials: LoginCredentials) => {
    loading.value = true
    try {
      // 使用真实API登录
      const response = await authAPI.login(credentials)
      user.value = response.data.user
      token.value = response.data.accessToken
      localStorage.setItem('auth_token', token.value)
              localStorage.setItem('auth_user', JSON.stringify(response.data.user))
      return response
    } finally {
      loading.value = false
    }
  }

  const register = async (data: RegisterData) => {
    loading.value = true
    try {
      const response = await authAPI.register(data)
      user.value = response.data.user
      token.value = response.data.accessToken
      localStorage.setItem('auth_token', token.value)
      return response
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  const fetchUser = async () => {
    if (!token.value) return
    
    try {
      const response = await authAPI.getProfile()
      user.value = response.data
    } catch (error) {
      // Token 无效，清除认证状态
      logout()
      throw error
    }
  }

  // 初始化时尝试获取用户信息
  const initAuth = async () => {
    if (token.value) {
      await fetchUser()
    }
  }

  return {
    // 状态
    user,
    token,
    loading,
    // 计算属性
    isAuthenticated,
    userRole,
    // 方法
    login,
    register,
    logout,
    fetchUser,
    initAuth
  }
}) 