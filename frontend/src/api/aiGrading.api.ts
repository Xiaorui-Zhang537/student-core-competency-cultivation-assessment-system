import { api } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

export const aiGradingApi = {
  gradeEssay: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    // 默认启用稳定化：两次取样，分差 > 0.8 则触发第 3 次并取“最近对”均值（后端实现）
    const payload = { jsonOnly: true, useGradingPrompt: true, samples: 2, diffThreshold: 0.8, ...data }
    return api.post('/ai/grade/essay', payload, { timeout: 120000 })
  },
  gradeEssayBatch: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }[]) => {
    const normalized = data.map(d => ({ jsonOnly: true, useGradingPrompt: true, samples: 2, diffThreshold: 0.8, ...d }))
    return api.post('/ai/grade/essay/batch', normalized, { timeout: 180000 })
  },
  gradeFiles: (data: { fileIds: number[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, samples: 2, diffThreshold: 0.8, ...data }
    return api.post('/ai/grade/files', payload, { timeout: 180000 })
  },
  listHistory: (params?: { q?: string; page?: number; size?: number }) => api.get('/ai/grade/history', { params }),
  getHistoryDetail: (id: number | string) => api.get(`/ai/grade/history/${id}`),
  deleteHistory: (id: number | string) => api.delete(`/ai/grade/history/${id}`).catch((err: any) => {
    // 兜底走 POST 兼容路径
    return api.post(`/ai/grade/history/${id}/delete`)
  })
}


