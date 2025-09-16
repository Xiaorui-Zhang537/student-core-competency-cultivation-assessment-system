import { api } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

export const aiGradingApi = {
  gradeEssay: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, ...data }
    return api.post('/ai/grade/essay', payload, { timeout: 120000 })
  },
  gradeEssayBatch: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }[]) => {
    const normalized = data.map(d => ({ jsonOnly: true, useGradingPrompt: true, ...d }))
    return api.post('/ai/grade/essay/batch', normalized, { timeout: 180000 })
  },
  gradeFiles: (data: { fileIds: number[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, ...data }
    return api.post('/ai/grade/files', payload, { timeout: 180000 })
  },
  listHistory: (params?: { q?: string; page?: number; size?: number }) => api.get('/ai/grade/history', { params }),
  getHistoryDetail: (id: number | string) => api.get(`/ai/grade/history/${id}`)
}


