export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PaginatedResponse<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
}

export interface ApiError {
  code: number
  message: string
  details?: string
  errors?: Record<string, string[]>
}

export interface RequestConfig {
  timeout?: number
  retry?: number
  cache?: boolean
} 