export interface StudentProfile {
  id: string
  name: string
  email: string
  studentId: string
  major: string
  year: number
  avatar: string
}

export interface Course {
  id: string
  title: string
  description: string
  instructor: string
  category: string
  coverUrl?: string
  totalLessons: number
  completedLessons: number
  progress: number
  studyHours: number
  status: 'active' | 'completed' | 'paused' | 'archived'
  isFavorite: boolean
  startDate: string
  lastStudied?: string
}

export interface Assignment {
  id: string
  title: string
  courseName: string
  dueDate: string
  status: 'pending' | 'submitted' | 'graded' | 'overdue'
}

export interface LearningActivity {
  id: string
  title: string
  description: string
  type: 'course_completed' | 'assignment_submitted' | 'achievement_earned' | 'group_joined'
  timestamp: string
}

export interface StudyStats {
  totalStudyTime: number
  weeklyStudyTime: number
  completedCourses: number
  averageScore: number
  overallProgress: number
} 