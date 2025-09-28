import apiClient, { api, baseURL } from './config';
import type { ApiResponse } from '@/types/api';
import type { FileInfo } from '@/types/file';

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
    const res: any = await apiClient.get(`/files/${encodeURIComponent(String(fileId))}/download`, { responseType: 'blob' });
    const blob: Blob = res instanceof Blob ? res : new Blob([res]);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename || `file_${fileId}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
  ,
  // 预览图片（Blob，带鉴权）- 使用 fetch 避免 axios 错误日志刷屏
  getPreview: async (fileId: string | number): Promise<Blob> => {
    const token = ((): string | null => { try { return localStorage.getItem('token') } catch { return null } })();
    const url = `${baseURL || '/api'}/files/${encodeURIComponent(String(fileId))}/preview`;
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        'Accept': 'image/*,application/pdf,application/octet-stream'
      },
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
