import { api } from './config';
import type { UserProfileResponse, UpdateProfileRequest, ChangePasswordRequest, ResetPasswordRequest } from '@/types/user';
import type { User } from '@/types/auth';

export const userApi = {
  getProfile: (): Promise<UserProfileResponse> => {
    return api.get('/users/profile');
  },
  getProfileById: (userId: string | number): Promise<UserProfileResponse> => {
    return api.get(`/users/${userId}/profile`);
  },
  updateProfile: (data: UpdateProfileRequest): Promise<User> => {
    return api.put('/users/profile', data);
  },
  updateAvatar: (fileId: number): Promise<void> => {
    return api.put('/users/me/avatar', { fileId });
  },
  changePassword: (data: ChangePasswordRequest): Promise<void> => {
    return api.post('/users/change-password', data);
  },
  forgotPassword: (email: string): Promise<void> => {
    return api.post('/users/forgot-password', null, { params: { email } });
  },
  resetPassword: (data: ResetPasswordRequest): Promise<void> => {
    return api.post('/users/reset-password', data);
  },
  verifyEmail: (token: string): Promise<void> => {
    return api.post('/auth/verify-email', null, { params: { token } });
  },
  resendVerification: (email: string, lang = 'zh-CN'): Promise<void> => {
    // backend requires email; keep helper signature explicit to avoid 400.
    return api.post('/auth/resend-verification', null, { params: { email, lang } });
  }
};
