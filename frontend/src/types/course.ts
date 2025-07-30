export interface Course {
  id: string
  title: string
  description: string
  coverImage?: string
  instructor: {
    id: string
    name: string
    avatar?: string
  }
  category: string
  level: 'beginner' | 'intermediate' | 'advanced'
  duration: number // 课程总时长（分钟）
  totalLessons: number
  completedLessons: number
  progress: number // 进度百分比
  rating: number
  studentsCount: number
  isEnrolled: boolean
  status: 'draft' | 'published' | 'archived'
  tags: string[]
  createdAt: string
  updatedAt: string
}

export interface Lesson {
  id: string
  courseId: string
  title: string
  description: string
  duration: number // 课时时长（分钟）
  videoUrl?: string
  resources: LessonResource[]
  isCompleted: boolean
  order: number
  type: 'video' | 'reading' | 'quiz' | 'assignment'
}

export interface LessonResource {
  id: string
  name: string
  type: 'pdf' | 'doc' | 'ppt' | 'link' | 'video'
  url: string
  size?: number
}

export interface CourseEnrollment {
  id: string
  userId: string
  courseId: string
  enrolledAt: string
  completedAt?: string
  progress: number
  lastAccessedAt: string
}

export interface CourseFilter {
  category?: string
  level?: string
  instructor?: string
  search?: string
  tags?: string[]
  status?: string
}

export interface CourseStats {
  totalCourses: number
  completedCourses: number
  inProgressCourses: number
  totalStudyTime: number
  averageProgress: number
} 