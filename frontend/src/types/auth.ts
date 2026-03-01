// Based on backend LoginRequest.java, RegisterRequest.java, and AuthResponse.java

export interface LoginRequest {
  username: string;
  password?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: 'student' | 'teacher';
  avatar?: string;
  lang?: 'zh-CN' | 'en-US' | string;
}

export interface User {
  id: string;
  username: string;
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  token?: string;
  refreshToken?: string;
  emailVerified?: boolean;
  avatar?: string;
  nickname?: string;
}

export interface AuthResponse {
  // refreshToken flow may return token-only payload; keep fields optional for compatibility.
  user?: User;
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  issuedAt?: string;
}
