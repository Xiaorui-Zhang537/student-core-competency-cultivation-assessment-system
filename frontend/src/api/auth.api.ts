import { api } from './config'
import type { LoginCredentials, RegisterData, AuthResponse, User } from '@/types/auth'
import type { ApiResponse } from '@/types/api'

export const authAPI = {
  // 登录
  login: (credentials: LoginCredentials): Promise<ApiResponse<AuthResponse>> => {
    return api.post<AuthResponse>('/api/auth/login', credentials)
  },

  // 注册
  register: (data: RegisterData): Promise<ApiResponse<AuthResponse>> => {
    return api.post<AuthResponse>('/api/auth/register', data)
  },

  // 获取用户信息
  getProfile: (): Promise<ApiResponse<User>> => {
    return api.get<User>('/api/auth/profile')
  },

  // 更新用户信息
  updateProfile: (data: Partial<User>): Promise<ApiResponse<User>> => {
    return api.patch<User>('/api/auth/profile', data)
  },

  // 修改密码
  changePassword: (data: {
    currentPassword: string
    newPassword: string
    confirmPassword: string
  }): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/change-password', data)
  },

  // 刷新token
  refreshToken: (): Promise<ApiResponse<{ token: string; expiresIn: number }>> => {
    return api.post<{ token: string; expiresIn: number }>('/api/auth/refresh')
  },

  // 登出
  logout: (): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/logout')
  },

  // 忘记密码
  forgotPassword: (email: string): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/forgot-password', { email })
  },

  // 重置密码
  resetPassword: (data: {
    token: string
    password: string
    confirmPassword: string
  }): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/reset-password', data)
  },

  // 验证邮箱
  verifyEmail: (token: string): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/verify-email', { token })
  },

  // 重新发送验证邮件
  resendVerification: (): Promise<ApiResponse<void>> => {
    return api.post<void>('/api/auth/resend-verification')
  }
} 