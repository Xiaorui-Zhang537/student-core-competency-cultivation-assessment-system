import axios from 'axios'
import type { ApiResponse, ApiError } from '@/types/api'

// 创建 Axios 实例
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 检查是否启用Mock模式 - 临时启用Mock模式进行测试
const isMockMode = true // import.meta.env.VITE_MOCK_API === 'true'

// 只在明确启用Mock模式时才使用Mock拦截器
if (isMockMode) {
  console.log('🎭 Mock模式已启用')
  
  // Mock API拦截器 - 在请求发送前直接返回Mock数据
  apiClient.interceptors.request.use(
    (config) => {
      // 拦截登录请求
      if (config.url === '/api/auth/login' && config.method === 'post') {
        console.log('🔄 拦截登录请求，返回Mock数据')
        
        const requestData = JSON.parse(config.data || '{}')
        
        // 创建Mock响应
        const mockResponse = {
          data: {
            user: {
              id: '1',
              username: requestData.username,
              email: `${requestData.username}@example.com`,
              role: requestData.username === 'teacher' ? 'teacher' : 'student',
              displayName: requestData.username === 'teacher' ? '张老师' : '李同学',
              firstName: requestData.username === 'teacher' ? '张' : '李',
              lastName: requestData.username === 'teacher' ? '老师' : '同学',
              createdAt: '2024-01-01 00:00:00',
              updatedAt: '2024-01-01 00:00:00'
            },
            accessToken: 'mock-jwt-token-' + Date.now(),
            refreshToken: 'mock-refresh-token-' + Date.now(),
            expiresIn: 3600,
            tokenType: 'Bearer'
          },
          status: 200,
          statusText: 'OK',
          headers: {},
          config: config
        }
        
        // 直接返回Promise，不发送实际请求
        throw { response: mockResponse, isMock: true }
      }
      
      return config
    }
  )
  
  // 处理Mock响应
  apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      // 如果是Mock响应，直接返回
      if (error.isMock && error.response) {
        console.log('✅ 返回Mock登录数据:', error.response.data)
        return Promise.resolve(error.response)
      }
      throw error
    }
  )
} else {
  console.log('🌐 使用真实API模式')
}

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳
    config.headers['X-Request-Time'] = Date.now().toString()
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    // 统一处理成功响应
    return response
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      const { status, data } = error.response
      
      // 处理不同的HTTP状态码
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('auth_token')
          window.location.href = '/auth/login'
          break
        case 403:
          // 禁止访问
          console.error('Access denied:', data.message)
          break
        case 404:
          console.error('Resource not found:', data.message)
          break
        case 422:
          // 验证错误
          console.error('Validation error:', data.errors)
          break
        case 500:
          console.error('Server error:', data.message)
          break
        default:
          console.error('API error:', data.message)
      }
      
      // 返回标准化的错误对象
      const apiError: ApiError = {
        code: status,
        message: data.message || 'An error occurred',
        details: data.details,
        errors: data.errors
      }
      
      return Promise.reject(apiError)
    } else if (error.request) {
      // 网络错误
      const networkError: ApiError = {
        code: 0,
        message: 'Network error. Please check your connection.'
      }
      return Promise.reject(networkError)
    } else {
      // 其他错误
      const unknownError: ApiError = {
        code: -1,
        message: error.message || 'An unknown error occurred'
      }
      return Promise.reject(unknownError)
    }
  }
)

// 封装常用的请求方法
export const api = {
  get: <T>(url: string, config?: any): Promise<ApiResponse<T>> =>
    apiClient.get(url, config).then(response => response.data),
    
  post: <T>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> =>
    apiClient.post(url, data, config).then(response => response.data),
    
  put: <T>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> =>
    apiClient.put(url, data, config).then(response => response.data),
    
  patch: <T>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> =>
    apiClient.patch(url, data, config).then(response => response.data),
    
  delete: <T>(url: string, config?: any): Promise<ApiResponse<T>> =>
    apiClient.delete(url, config).then(response => response.data),
    
  upload: <T>(url: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> => {
    const formData = new FormData()
    formData.append('file', file)
    
    return apiClient.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    }).then(response => response.data)
  }
}

export default apiClient 