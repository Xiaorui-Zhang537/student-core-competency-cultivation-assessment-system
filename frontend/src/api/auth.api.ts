import { api } from './config';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth';

export const authApi = {
  // NOTE: axios response interceptor unwraps ApiResponse<T> => T (see frontend/src/api/config.ts)
  login: (data: LoginRequest): Promise<AuthResponse> => {
    return api.post('/auth/login', data);
  },
  register: (data: RegisterRequest): Promise<void> => {
    return api.post('/auth/register', data);
  },
  refreshToken: (refreshToken: string): Promise<AuthResponse> => {
    return api.post('/auth/refresh', null, { params: { refreshToken } });
  },
  logout: (): Promise<void> => {
    return api.post('/auth/logout');
  },
};
