import apiClient, { api, baseURL } from './config';
import type { ApiResponse } from '@/types/api';
import type { FileInfo } from '@/types/file';

const normalizeBase = () => String(apiClient.defaults.baseURL || baseURL || '/api').replace(/\/+$/, '');
const dedupApi = (v: string) => {
  let out = v;
  // 折叠任意重复的 /api 片段，直到不再出现
  while (/\/api\/api/i.test(out)) {
    out = out.replace(/\/api\/api/ig, '/api');
  }
  return out;
};
const buildUrl = (path: string) => {
  if (/^https?:\/\//i.test(path)) return path;
  const base = normalizeBase();
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  if (cleanPath.startsWith(base)) return dedupApi(cleanPath);
  if (base.endsWith('/api') && cleanPath.startsWith('/api/')) return dedupApi(`${base}${cleanPath.replace(/^\/api/, '')}`);
  return dedupApi(`${base}${cleanPath}`);
};

export const buildFileUrl = buildUrl;

const authHeaders = (): Record<string, string> => {
  const token = (() => {
    try { return localStorage.getItem('token'); } catch { return null; }
  })();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const appendTokenQuery = (url: string): string => {
  const token = (() => {
    try { return localStorage.getItem('token'); } catch { return null; }
  })();
  if (!token) return url;
  const join = url.includes('?') ? '&' : '?';
  return `${url}${join}token=${encodeURIComponent(String(token))}`;
};

export const fileApi = {
  // 兼容：部分旧代码以 fileApi.buildFileUrl(...) 调用
  buildFileUrl: buildUrl,
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
    const headers: Record<string, string> = {
      Accept: 'application/octet-stream'
    };
    const token = authHeaders();
    if (token.Authorization) headers.Authorization = token.Authorization;
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
    // 双保险：既带 Authorization，也把 token 拼到 query（避免某些环境 Authorization 被拦截）
    const url = appendTokenQuery(buildUrl(`/files/${encodeURIComponent(String(fileId))}/preview`));
    const headers: Record<string, string> = {
      // 关键：必须包含 application/json，否则后端在抛出 BusinessException（JSON）时可能因为 Accept 不匹配而触发 406，
      // 随后走 /error 分发，前端最终看到的会是误导性的 401。
      Accept: 'image/*,application/pdf,application/octet-stream,application/json'
    };
    const token = authHeaders();
    if (token.Authorization) headers.Authorization = token.Authorization;
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
