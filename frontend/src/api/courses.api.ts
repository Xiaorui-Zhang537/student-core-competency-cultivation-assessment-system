import { api } from './config'
import type { Course, Lesson, CourseEnrollment, CourseFilter, CourseStats } from '@/types/course'
import type { ApiResponse, PaginatedResponse } from '@/types/api'

// 课程创建/更新请求类型
export interface CreateCourseRequest {
  title: string
  description: string
  category: string
  level: 'beginner' | 'intermediate' | 'advanced'
  duration: number
  price: number
  tags: string[]
  thumbnail?: string
  requirements?: string[]
  objectives?: string[]
  status?: 'draft' | 'published'
}

export interface UpdateCourseRequest extends Partial<CreateCourseRequest> {
  id: string
}

// 课程发现相关类型
export interface PopularCourse extends Course {
  enrollmentCount: number
  rating: number
  reviewCount: number
}

export interface CourseCategory {
  id: string
  name: string
  description: string
  courseCount: number
  icon?: string
}

export const coursesAPI = {
  // ========== 学生端API ==========
  
  // 获取课程列表
  getCourses: (params?: CourseFilter & { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get<PaginatedResponse<Course>>('/api/courses', { params })
  },

  // 获取我的课程
  getMyCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get<Course[]>('/api/courses/my')
  },

  // 获取课程详情
  getCourse: (id: string): Promise<ApiResponse<Course>> => {
    return api.get<Course>(`/api/courses/${id}`)
  },

  // 获取课程统计
  getCourseStats: (): Promise<ApiResponse<CourseStats>> => {
    return api.get<CourseStats>('/api/courses/stats')
  },

  // 报名课程
  enrollCourse: (courseId: string): Promise<ApiResponse<CourseEnrollment>> => {
    return api.post<CourseEnrollment>(`/api/courses/${courseId}/enroll`)
  },

  // 退出课程
  unenrollCourse: (courseId: string): Promise<ApiResponse<void>> => {
    return api.delete<void>(`/api/courses/${courseId}/enroll`)
  },

  // 获取课程章节
  getLessons: (courseId: string): Promise<ApiResponse<Lesson[]>> => {
    return api.get<Lesson[]>(`/api/courses/${courseId}/lessons`)
  },

  // 标记课时完成
  completeLesson: (courseId: string, lessonId: string): Promise<ApiResponse<void>> => {
    return api.post<void>(`/api/courses/${courseId}/lessons/${lessonId}/complete`)
  },

  // 更新学习进度
  updateProgress: (courseId: string, progress: number): Promise<ApiResponse<void>> => {
    return api.patch<void>(`/api/courses/${courseId}/progress`, { progress })
  },

  // 搜索课程
  searchCourses: (query: string): Promise<ApiResponse<Course[]>> => {
    return api.get<Course[]>('/api/courses/search', { params: { q: query } })
  },

  // 获取推荐课程
  getRecommendedCourses: (): Promise<ApiResponse<Course[]>> => {
    return api.get<Course[]>('/api/courses/recommended')
  },

  // ========== 教师端API ==========

  // 创建课程
  createCourse: (data: CreateCourseRequest): Promise<ApiResponse<Course>> => {
    return api.post<Course>('/api/courses', data)
  },

  // 更新课程
  updateCourse: (id: string, data: Partial<CreateCourseRequest>): Promise<ApiResponse<Course>> => {
    return api.put<Course>(`/api/courses/${id}`, data)
  },

  // 删除课程
  deleteCourse: (id: string): Promise<ApiResponse<void>> => {
    return api.delete<void>(`/api/courses/${id}`)
  },

  // 发布课程
  publishCourse: (id: string): Promise<ApiResponse<Course>> => {
    return api.post<Course>(`/api/courses/${id}/publish`)
  },

  // 下架课程
  unpublishCourse: (id: string): Promise<ApiResponse<Course>> => {
    return api.post<Course>(`/api/courses/${id}/unpublish`)
  },

  // 获取我教授的课程
  getMyTeachingCourses: (params?: { status?: string; page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get<PaginatedResponse<Course>>('/api/courses/my-teaching', { params })
  },

  // 获取课程学生列表
  getCourseStudents: (courseId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<any>>> => {
    return api.get<PaginatedResponse<any>>(`/api/courses/${courseId}/students`, { params })
  },

  // 检查注册状态
  checkEnrollmentStatus: (courseId: string): Promise<ApiResponse<{ enrolled: boolean; enrolledAt?: string }>> => {
    return api.get<{ enrolled: boolean; enrolledAt?: string }>(`/api/courses/${courseId}/enrollment-status`)
  },

  // 批量更新课程状态
  batchUpdateStatus: (data: { courseIds: string[]; status: string }): Promise<ApiResponse<void>> => {
    return api.put<void>('/api/courses/batch-status', data)
  },

  // ========== 课程发现API ==========

  // 获取热门课程
  getPopularCourses: (params?: { limit?: number; category?: string }): Promise<ApiResponse<PopularCourse[]>> => {
    return api.get<PopularCourse[]>('/api/courses/popular', { params })
  },

  // 获取课程分类
  getCategories: (): Promise<ApiResponse<CourseCategory[]>> => {
    return api.get<CourseCategory[]>('/api/courses/categories')
  },

  // 按分类获取课程
  getCoursesByCategory: (categoryId: string, params?: { page?: number; size?: number }): Promise<ApiResponse<PaginatedResponse<Course>>> => {
    return api.get<PaginatedResponse<Course>>(`/api/courses/category/${categoryId}`, { params })
  },

  // 获取课程统计信息（教师端）
  getCourseStatistics: (courseId: string): Promise<ApiResponse<{
    totalStudents: number
    completionRate: number
    averageRating: number
    totalRevenue: number
    recentEnrollments: any[]
  }>> => {
    return api.get(`/api/courses/${courseId}/statistics`)
  },

  // ========== 课程内容管理API ==========

  // 创建课程章节
  createLesson: (courseId: string, data: {
    title: string
    description?: string
    content: string
    duration: number
    sortOrder: number
    videoUrl?: string
    resources?: string[]
  }): Promise<ApiResponse<Lesson>> => {
    return api.post<Lesson>(`/api/courses/${courseId}/lessons`, data)
  },

  // 更新课程章节
  updateLesson: (courseId: string, lessonId: string, data: Partial<{
    title: string
    description: string
    content: string
    duration: number
    sortOrder: number
    videoUrl: string
    resources: string[]
  }>): Promise<ApiResponse<Lesson>> => {
    return api.put<Lesson>(`/api/courses/${courseId}/lessons/${lessonId}`, data)
  },

  // 删除课程章节
  deleteLesson: (courseId: string, lessonId: string): Promise<ApiResponse<void>> => {
    return api.delete<void>(`/api/courses/${courseId}/lessons/${lessonId}`)
  },

  // 调整章节顺序
  reorderLessons: (courseId: string, data: { lessonIds: string[] }): Promise<ApiResponse<void>> => {
    return api.put<void>(`/api/courses/${courseId}/lessons/reorder`, data)
  }
} 