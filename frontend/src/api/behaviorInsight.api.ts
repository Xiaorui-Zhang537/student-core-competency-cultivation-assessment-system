import { api } from './config'

export type BehaviorInsightResponse = {
  schemaVersion?: string
  snapshotId?: number
  explainScore?: { text?: string; evidenceIds?: string[] }
  stageJudgements?: Array<{
    dimensionCode?: 'MORAL_COGNITION' | 'LEARNING_ATTITUDE' | 'LEARNING_ABILITY' | 'LEARNING_METHOD'
    level?: 'EMERGING' | 'DEVELOPING' | 'PROFICIENT' | 'ADVANCED'
    rationale?: string
    evidenceIds?: string[]
  }>
  formativeSuggestions?: Array<{
    title?: string
    description?: string
    nextActions?: string[]
    evidenceIds?: string[]
  }>
  // v2：结构化预警/建议（用于前端直接展示）
  riskAlerts?: Array<{
    severity?: 'info' | 'warn' | 'critical'
    title?: string
    message?: string
    dimensionCode?: 'MORAL_COGNITION' | 'LEARNING_ATTITUDE' | 'LEARNING_ABILITY' | 'LEARNING_METHOD' | null
    evidenceIds?: string[]
  }>
  actionRecommendations?: Array<{
    title?: string
    description?: string
    nextActions?: string[]
    dimensionCode?: 'MORAL_COGNITION' | 'LEARNING_ATTITUDE' | 'LEARNING_ABILITY' | 'LEARNING_METHOD' | null
    evidenceIds?: string[]
  }>
  meta?: {
    generatedAt?: string
    model?: string
    promptVersion?: string
    status?: 'success' | 'failed' | 'partial'
  }
  extra?: any
}

export const behaviorInsightApi = {
  getLatest(params: { studentId: string | number; courseId?: string | number; range?: string }) {
    return api.get<BehaviorInsightResponse>('/behavior/insights', { params })
  },
  generate(params: {
    studentId: string | number
    courseId?: string | number
    range?: string
    model?: string
    force?: boolean
  }) {
    return api.post<BehaviorInsightResponse>('/behavior/insights/generate', null, { params })
  }
}

