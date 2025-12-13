import apiClient, { api, baseURL } from './config';
import type { ApiResponse } from '@/types/api';
import type { FileInfo } from '@/types/file';

const normalizeBase = () => String(apiClient.defaults.baseURL || baseURL || '/api').replace(/\/+$/, '');
const buildUrl = (path: string) => {
  if (/^https?:\/\//i.test(path)) return path;
  const base = normalizeBase();
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  if (cleanPath.startsWith(base)) return cleanPath;
  if (base.endsWith('/api') && cleanPath.startsWith('/api/')) return `${base}${cleanPath.replace(/^\/api/, '')}`;
  return `${base}${cleanPath}`;
};

const authHeaders = (): Record<string, string> => {
  const token = (() => {
    try { return localStorage.getItem('token'); } catch { return null; }
  })();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const fileApi = {
  // 返回值已被 axios 拦截器解包为 data
  uploadFile: (file: File, extra?: { purpose?: string; relatedId?: string | number }): Promise<FileInfo | any> => {
    const formData = new FormData();
    formData.append('file', file);
    if (extra) {
      Object.entries(extra).forEach(([k, v]) => {
        if (v !== undefined && v !== null) formData.append(k, String(v));
      });
    }

    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  getFileInfo: (fileId: string): Promise<FileInfo | any> => {
    return api.get(`/files/${fileId}/info`);
  },

  deleteFile: (fileId: string): Promise<ApiResponse<void>> => {
    return api.delete(`/files/${fileId}`);
  },

  // Note: The download URL is typically constructed directly, e.g., `/files/{fileId}/download`
  // So a dedicated API client function might not be necessary unless it needs special headers.
  getRelatedFiles: (purpose: string, relatedId: string | number): Promise<ApiResponse<any[]>> => {
    return api.get(`/files/related`, { params: { purpose, relatedId } });
  },
  
  // 下载文件（带鉴权，避免 <a> 直链 401）
  downloadFile: async (fileId: string | number, filename?: string): Promise<void> => {
    const url = buildUrl(`/files/${encodeURIComponent(String(fileId))}/download`);
    const headers: HeadersInit = {
      Accept: 'application/octet-stream',
      ...authHeaders()
    };
    const res = await fetch(url, {
      method: 'GET',
      headers,
      credentials: 'include'
    });
    if (!res.ok) {
      const err: any = new Error('Download failed');
      err.code = res.status;
      throw err;
    }
    const blob: Blob = await res.blob();
    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = blobUrl;
    a.download = filename || `file_${fileId}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(blobUrl);
  }
  ,
  // 预览图片（Blob，带鉴权）- 使用 fetch 避免 axios 错误日志刷屏
  getPreview: async (fileId: string | number): Promise<Blob> => {
    const url = buildUrl(`/files/${encodeURIComponent(String(fileId))}/preview`);
    const headers: HeadersInit = {
      Accept: 'image/*,application/pdf,application/octet-stream',
      ...authHeaders()
    };
    const res = await fetch(url, {
      method: 'GET',
      headers,
      credentials: 'include'
    });
    if (!res.ok) {
      const ct = res.headers.get('content-type') || '';
      if (ct.includes('application/json')) {
        try {
          const j = await res.json();
          const err: any = new Error(j?.message || 'Preview failed');
          err.code = j?.code || res.status;
          throw err;
        } catch {
          const err: any = new Error('Preview failed');
          err.code = res.status;
          throw err;
        }
      }
      const err: any = new Error('Preview failed');
      err.code = res.status;
      throw err;
    }
    return await res.blob();
  }
};
