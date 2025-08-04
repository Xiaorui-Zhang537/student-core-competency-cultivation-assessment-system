import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';

export const authApi = {
  login: (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    return api.post('/api/auth/login', data);
  },
  register: (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    return api.post('/api/auth/register', data);
  },
  refreshToken: (refreshToken: string): Promise<ApiResponse<AuthResponse>> => {
    return api.post('/api/auth/refresh', null, { params: { refreshToken } });
  },
  logout: (): Promise<ApiResponse<void>> => {
    return api.post('/api/auth/logout');
  },
};
