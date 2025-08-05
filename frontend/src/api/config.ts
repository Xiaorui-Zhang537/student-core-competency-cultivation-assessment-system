import axios from 'axios';
import type { ApiResponse, ApiError } from '@/types/api';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // 彻底移除 /api
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

console.log('🌐 使用真实API模式');

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
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
