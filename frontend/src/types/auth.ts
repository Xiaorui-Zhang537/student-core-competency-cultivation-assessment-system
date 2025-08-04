// Based on backend LoginRequest.java, RegisterRequest.java, and AuthResponse.java

export interface LoginRequest {
  usernameOrEmail: string;
  password?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password?: string;
  role: 'STUDENT' | 'TEACHER';
}

export interface User {
  id: string;
  username: string;
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  token: string;
  refreshToken: string;
  emailVerified?: boolean;
  avatar?: string;
  nickname?: string;
}

export interface AuthResponse {
  user: User;
  token: string;
  refreshToken: string;
}
