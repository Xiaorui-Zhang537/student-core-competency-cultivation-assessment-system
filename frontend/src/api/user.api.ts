import { api } from './config';
import type { ApiResponse } from '@/types/api';
import type { UserProfileResponse, UpdateProfileRequest, ChangePasswordRequest, ResetPasswordRequest } from '@/types/user';
import type { User } from '@/types/auth';

export const userApi = {
  getProfile: (): Promise<ApiResponse<UserProfileResponse>> => {
    return api.get('/users/profile');
  },
  updateProfile: (data: UpdateProfileRequest): Promise<ApiResponse<User>> => {
    return api.put('/users/profile', data);
  },
  changePassword: (data: ChangePasswordRequest): Promise<ApiResponse<void>> => {
    return api.post('/users/change-password', data);
  },
  forgotPassword: (email: string): Promise<ApiResponse<void>> => {
    return api.post('/users/forgot-password', null, { params: { email } });
  },
  resetPassword: (data: ResetPasswordRequest): Promise<ApiResponse<void>> => {
    return api.post('/users/reset-password', data);
  },
  verifyEmail: (token: string): Promise<ApiResponse<void>> => {
    return api.post('/users/verify-email', null, { params: { token } });
  },
  resendVerification: (): Promise<ApiResponse<void>> => {
    return api.post('/users/resend-verification');
  },
};
