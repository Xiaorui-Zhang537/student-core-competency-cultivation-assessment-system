// 能力发展相关类型定义

export interface AbilityDimension {
  id: string
  name: string
  description: string
  weight: number
  color: string
}

export interface AbilityScore {
  dimensionId: string
  dimensionName: string
  score: number
  maxScore: number
  level: 'beginner' | 'intermediate' | 'advanced' | 'expert'
  percentile: number
  lastUpdated: string
}

export interface AbilityTrend {
  dimensionId: string
  dimensionName: string
  data: {
    date: string
    score: number
  }[]
}

export interface AbilityAnalysis {
  id: string
  studentId: string
  overallScore: number
  overallLevel: string
  strengths: string[]
  weaknesses: string[]
  recommendations: string[]
  reportDate: string
  nextAssessmentDate: string
}

export interface AbilityReport {
  id: string
  studentId: string
  reportDate: string
  overallScore: number
  dimensions: AbilityScore[]
  trends: AbilityTrend[]
  analysis: AbilityAnalysis
  progressSummary: {
    improved: number
    stable: number
    declined: number
  }
}

export interface AbilityGoal {
  id: string
  studentId: string
  dimensionId: string
  targetScore: number
  currentScore: number
  deadline: string
  status: 'active' | 'completed' | 'paused'
  createdAt: string
  updatedAt: string
}

export interface LearningRecommendation {
  id: string
  dimensionId: string
  title: string
  description: string
  type: 'course' | 'practice' | 'assessment' | 'reading'
  difficulty: 'easy' | 'medium' | 'hard'
  estimatedTime: number // minutes
  priority: 'low' | 'medium' | 'high'
  resources: {
    url: string
    title: string
    type: string
  }[]
}

export interface AbilityAssessment {
  id: string
  studentId: string
  dimensionId: string
  assessmentType: 'self' | 'peer' | 'teacher' | 'ai'
  score: number
  feedback: string
  assessedAt: string
  assessorId: string
  confidence: number
}

export interface AbilityDashboard {
  student: {
    id: string
    name: string
    grade: string
  }
  lastAssessment: string
  nextAssessment: string
  overallProgress: number
  dimensions: AbilityScore[]
  recentTrends: AbilityTrend[]
  currentGoals: AbilityGoal[]
  recommendations: LearningRecommendation[]
} 