import axios from 'axios';
import type { ApiResponse, ApiError } from '@/types/api';

const apiClient = axios.create({
  // 主机地址（如需切换生产，只改环境变量即可）
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

console.log('🌐 Axios baseURL =>', apiClient.defaults.baseURL);

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 自动补全后端统一前缀 /api，避免各接口手写
    if (typeof config.url === 'string'
        && config.url.startsWith('/')          // 只处理绝对路径
        && !config.url.startsWith('/api/')) {  // 已有 /api 的不再重复
      config.url = `/api${config.url}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      switch (status) {
        case 401:
          localStorage.removeItem('token');
          window.location.href = '/auth/login';
          break;
        default:
          console.error(`API error with status ${status}:`, data.message);
      }
      const apiError: ApiError = {
        code: status,
        message: data.message || 'An error occurred',
      };
      return Promise.reject(apiError);
    } else if (error.request) {
      const networkError: ApiError = {
        code: 0,
        message: 'Network error. Please check your connection.',
      };
      return Promise.reject(networkError);
    } else {
      const unknownError: ApiError = {
        code: -1,
        message: error.message || 'An unknown error occurred',
      };
      return Promise.reject(unknownError);
    }
  }
);

export const api = {
  get: <T>(url: string, config?: any): Promise<ApiResponse<T>> =>
    apiClient.get(url, config).then(response => response.data),
    
  post: <T>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> =>
    apiClient.post(url, data, config).then(response => response.data),
    
  put: <T>(url: string, data?: any, config?: any): Promise<ApiResponse<T>> =>
    apiClient.put(url, data, config).then(response => response.data),

  delete: <T>(url: string, config?: any): Promise<ApiResponse<T>> =>
    apiClient.delete(url, config).then(response => response.data),
};

export default apiClient;
