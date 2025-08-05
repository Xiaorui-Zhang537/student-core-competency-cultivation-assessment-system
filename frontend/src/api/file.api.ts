import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { FileInfo } from '@/types/file';

export const fileApi = {
  uploadFile: (file: File): Promise<ApiResponse<FileInfo>> => {
    const formData = new FormData();
    formData.append('file', file);

    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  getFileInfo: (fileId: string): Promise<ApiResponse<FileInfo>> => {
    return api.get(`/files/${fileId}/info`);
  },

  deleteFile: (fileId: string): Promise<ApiResponse<void>> => {
    return api.delete(`/files/${fileId}`);
  },

  // Note: The download URL is typically constructed directly, e.g., `/files/{fileId}/download`
  // So a dedicated API client function might not be necessary unless it needs special headers.
};
