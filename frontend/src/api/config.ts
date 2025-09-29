import axios from 'axios';
import { i18n } from '@/i18n'
import type { ApiResponse, ApiError } from '@/types/api';

const rawBase = import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, '') || '';
const baseURL =
  rawBase && !rawBase.endsWith('/api') ? `${rawBase}/api` : rawBase || '/api';

const apiClient = axios.create({
  // 主机地址（如需切换生产，只改环境变量即可）
  // 若未指定则默认走本地代理 /api
  baseURL,
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

if (import.meta.env.DEV) {
  // eslint-disable-next-line no-console
  console.log('🌐 Axios baseURL =>', apiClient.defaults.baseURL);
}

apiClient.interceptors.request.use(
  (config) => {
    // 对登录 / 注册接口不附加旧 token，避免 401 回环
    if (config.url && /\/auth\/(login|register)/.test(config.url)) {
      return config;
    }

    const token = localStorage.getItem('token');
    if (token) {
      // 其它请求统一带上 Bearer
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 统一附带语言首选项，便于后端返回对应语言内容（如评语/报告）
    try {
      const locale = (i18n.global as any)?.locale?.value || localStorage.getItem('locale') || 'zh-CN'
      if (locale) {
        config.headers['Accept-Language'] = String(locale)
        // 可选的自定义头，后端若不识别则忽略
        config.headers['X-Locale'] = String(locale)
      }
    } catch {}
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<any>;

    // 如果后端按照统一格式返回 { code, message, data }
    if (typeof res?.code !== 'undefined' && typeof res?.data !== 'undefined') {
      if (res.code !== 200) {
        // 非 200 视为业务错误，抛给调用方
        const apiError: ApiError = {
          code: res.code,
          message: res.message || '业务请求失败',
        };
        return Promise.reject(apiError);
      }
      // 200 => 直接返回真正的业务数据
      return res.data;
    }

    // 兼容其它直出格式（如文件流）——直接返回
    return response.data;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      const cfg: any = (error.config || error.response?.config) as any;
      const suppressLog = cfg && cfg.suppressLog === true;
      switch (status) {
        case 401:
          try { localStorage.removeItem('token'); } catch {}
          try {
            const p = window.location.pathname || ''
            // 处于任何 /auth/* 来客页时，不要强制跳回 /auth/login，避免“忘记密码/注册”被打断
            if (!/^\/?auth(\/|$)/i.test(p)) {
              if (p !== '/auth/login') {
                window.location.href = '/auth/login'
              }
            }
          } catch {}
          break;
        default:
          if (!suppressLog) {
            // eslint-disable-next-line no-console
            console.error(`API error with status ${status}:`, data?.message || data);
          }
      }
      const apiError: ApiError = {
        code: status,
        message: (data && (data.message || data.error || data.msg)) || 'An error occurred',
      };
      return Promise.reject(apiError);
    } else if (error.request) {
      // eslint-disable-next-line no-console
      console.error('API network error:', error?.message || error);
      const isTimeout = String(error?.code || '').toUpperCase() === 'ECONNABORTED' || /timeout/i.test(String(error?.message || ''));
      const networkError: ApiError = {
        code: 0,
        message: isTimeout ? 'Request timeout. Please retry.' : 'Network error. Please check your connection.',
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

export { baseURL };

export const api = {
  get: <T>(url: string, config?: any): Promise<T> =>
    apiClient.get(url, config),

  post: <T>(url: string, data?: any, config?: any): Promise<T> =>
    apiClient.post(url, data, config),

  put: <T>(url: string, data?: any, config?: any): Promise<T> =>
    apiClient.put(url, data, config),

  delete: <T>(url: string, config?: any): Promise<T> =>
    apiClient.delete(url, config),
};

export default apiClient;
