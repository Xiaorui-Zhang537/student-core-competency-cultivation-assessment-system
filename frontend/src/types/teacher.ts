// 教师端相关类型定义

export interface TeacherProfile {
  id: string
  userId: string
  teacherCode: string
  name: string
  email: string
  phone?: string
  department: string
  subject: string[]
  title: string // 职称：教授、副教授、讲师等
  avatar?: string
  bio?: string
  education: string
  experience: number // 教学经验年限
  rating: number
  reviewCount: number
  joinDate: string
  status: 'active' | 'inactive' | 'on_leave'
  permissions: string[]
}

export interface TeacherStats {
  totalCourses: number
  activeCourses: number
  totalStudents: number
  activeStudents: number
  pendingAssignments: number
  gradedAssignments: number
  averageRating: number
  teachingHours: number
  onlineHours: number
  responseTime: number // 平均响应时间(小时)
}

export interface StudentOverview {
  id: string
  name: string
  studentCode: string
  avatar?: string
  grade: string
  major: string
  enrolledCourses: number
  completedAssignments: number
  averageScore: number
  lastActivity: string
  status: 'active' | 'inactive' | 'at_risk'
  abilityScores: {
    dimensionId: string
    dimensionName: string
    score: number
    level: string
  }[]
}

export interface CourseAnalytics {
  courseId: string
  courseName: string
  enrollmentCount: number
  completionRate: number
  averageProgress: number
  averageScore: number
  engagementRate: number
  dropoutRate: number
  studentFeedback: {
    rating: number
    reviewCount: number
    satisfaction: number
  }
  weeklyProgress: {
    week: string
    completions: number
    averageScore: number
  }[]
}

export interface AssignmentAnalytics {
  assignmentId: string
  assignmentTitle: string
  submissionCount: number
  totalStudents: number
  submissionRate: number
  averageScore: number
  gradeDistribution: {
    range: string
    count: number
    percentage: number
  }[]
  commonMistakes: string[]
  timeSpent: {
    average: number
    median: number
    min: number
    max: number
  }
}

export interface GradingTask {
  id: string
  assignmentId: string
  assignmentTitle: string
  courseId: string
  courseName: string
  studentId: string
  studentName: string
  submittedAt: string
  status: 'pending' | 'in_progress' | 'completed'
  priority: 'low' | 'medium' | 'high'
  dueDate: string
  estimatedTime: number // 预估评分时间(分钟)
  aiSuggestions?: {
    suggestedScore: number
    confidence: number
    strengths: string[]
    improvements: string[]
    feedback: string
  }
}

export interface TeacherDashboard {
  teacher: TeacherProfile
  stats: TeacherStats
  recentActivities: {
    id: string
    type: 'assignment_submitted' | 'student_enrolled' | 'course_completed' | 'feedback_received'
    title: string
    description: string
    timestamp: string
    metadata?: Record<string, any>
  }[]
  upcomingDeadlines: {
    id: string
    type: 'assignment_due' | 'course_end' | 'meeting' | 'evaluation'
    title: string
    date: string
    priority: 'low' | 'medium' | 'high'
  }[]
  quickActions: {
    pendingGrading: number
    newMessages: number
    courseRequests: number
    systemNotifications: number
  }
}

export interface ClassPerformance {
  courseId: string
  courseName: string
  totalStudents: number
  performanceMetrics: {
    excellent: number // 90-100分
    good: number // 80-89分
    average: number // 70-79分
    needsImprovement: number // 60-69分
    failing: number // <60分
  }
  abilityDevelopment: {
    dimensionId: string
    dimensionName: string
    classAverage: number
    improvement: number
    topStudents: {
      studentId: string
      studentName: string
      score: number
    }[]
  }[]
  engagementMetrics: {
    activeParticipation: number
    assignmentCompletion: number
    onlineActivity: number
    collaborationScore: number
  }
}

export interface TeacherNotification {
  id: string
  type: 'assignment_submitted' | 'student_question' | 'system_update' | 'deadline_reminder' | 'performance_alert'
  title: string
  message: string
  timestamp: string
  isRead: boolean
  priority: 'low' | 'medium' | 'high'
  actionRequired: boolean
  metadata?: {
    courseId?: string
    studentId?: string
    assignmentId?: string
    url?: string
  }
}

export interface TeacherSchedule {
  id: string
  title: string
  type: 'class' | 'meeting' | 'office_hours' | 'evaluation' | 'other'
  startTime: string
  endTime: string
  location?: string
  courseId?: string
  courseName?: string
  students?: string[]
  description?: string
  isRecurring: boolean
  recurringPattern?: {
    frequency: 'daily' | 'weekly' | 'monthly'
    interval: number
    endDate?: string
  }
}

export interface TeacherCourseManagement {
  courseId: string
  courseName: string
  status: 'draft' | 'published' | 'archived'
  enrolledStudents: StudentOverview[]
  analytics: CourseAnalytics
  pendingTasks: {
    type: 'grading' | 'feedback' | 'content_review' | 'student_inquiry'
    count: number
    urgent: number
  }[]
  recentActivity: {
    timestamp: string
    action: string
    studentName?: string
    details: string
  }[]
}

export interface StudentProgressReport {
  studentId: string
  studentName: string
  courseId: string
  courseName: string
  overallProgress: number
  abilityScores: {
    dimensionId: string
    dimensionName: string
    currentScore: number
    previousScore: number
    improvement: number
    rank: number
  }[]
  assignmentPerformance: {
    assignmentId: string
    assignmentTitle: string
    score: number
    submittedAt: string
    feedback: string
    strengths: string[]
    improvements: string[]
  }[]
  recommendations: {
    type: 'learning_material' | 'practice_exercise' | 'peer_collaboration' | 'instructor_meeting'
    title: string
    description: string
    priority: 'low' | 'medium' | 'high'
  }[]
} 