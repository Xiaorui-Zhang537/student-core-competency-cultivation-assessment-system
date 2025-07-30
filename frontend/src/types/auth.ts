export interface User {
  id: string
  username: string
  email: string
  role: 'student' | 'teacher' | 'admin'
  avatar?: string
  firstName?: string
  lastName?: string
  displayName?: string
  bio?: string
  grade?: string
  subject?: string
  school?: string
  phone?: string
  createdAt: string
  updatedAt: string
}



export interface LoginCredentials {
  username: string
  password: string
  remember?: boolean
}

export interface RegisterData {
  username: string
  email: string
  password: string
  confirmPassword: string
  role: 'student' | 'teacher'
    firstName: string
    lastName: string
    grade?: string
    subject?: string
    school?: string
}

export interface AuthResponse {
  user: User
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType?: string
} 