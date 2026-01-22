import { api } from './config'

export type BehaviorSummaryResponse = {
  schemaVersion?: string
  meta?: {
    studentId?: number
    courseId?: number | null
    range?: string
    from?: string
    to?: string
    generatedAt?: string
    inputEventCount?: number
    eventTypesIncluded?: string[]
  }
  activityStats?: {
    ai?: { questionCount?: number; followUpCount?: number; followUpRate?: number; topicTags?: string[] }
    assignment?: { submitCount?: number; resubmitCount?: number; resubmitAfterFeedbackCount?: number }
    community?: { askCount?: number; answerCount?: number }
    feedback?: { viewCount?: number }
    resource?: { viewCount?: number; byCategory?: Record<string, number> }
  }
  evidenceItems?: Array<{
    evidenceId?: string
    evidenceType?: string
    title?: string
    description?: string
    eventRefs?: number[]
    occurredAt?: string
    link?: string
  }>
  nonEvaluative?: {
    items?: Array<{ eventType?: string; count?: number; meta?: Record<string, any> }>
  }
  signals?: Record<string, any>
}

export const behaviorEvidenceApi = {
  getSummary(params: { studentId?: string | number; courseId?: string | number; range?: string }) {
    return api.get<BehaviorSummaryResponse>('/behavior/summary', { params })
  }
}
