// Based on backend UpdateProfileRequest.java, ChangePasswordRequest.java, etc.
import type { User } from './auth';

export interface UpdateProfileRequest {
  nickname?: string;
  avatar?: string;
  gender?: string;
  bio?: string;
}

export interface ChangePasswordRequest {
  currentPassword?: string;
  newPassword?: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword?: string;
}

// UserProfileResponse matches the structure returned by /api/users/profile
export interface UserProfileResponse extends Omit<User, 'token' | 'refreshToken'> {
  displayName: string;
}
