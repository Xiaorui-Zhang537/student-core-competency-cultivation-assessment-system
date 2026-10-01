import { api } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

type GradingOptions = {
  model?: string
  jsonOnly?: boolean
  useGradingPrompt?: boolean
  samples?: number
  diffThreshold?: number
}

export const aiGradingApi = {
  gradeEssay: (data: { messages: { role: ChatRole; content: string }[] } & GradingOptions) => {
    // 非流式接口默认单次取样，避免豆包 Pro 多次生成导致页面长时间无反馈；需要稳定化时走 /essay/stream。
    const payload = { jsonOnly: true, useGradingPrompt: true, samples: 1, diffThreshold: 0.8, ...data }
    return api.post('/ai/grade/essay', payload, { timeout: 360000 })
  },
  gradeFiles: (data: { fileIds: number[] } & GradingOptions) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, samples: 1, diffThreshold: 0.8, ...data }
    return api.post('/ai/grade/files', payload, { timeout: 360000 })
  },
  listHistory: (params?: { q?: string; page?: number; size?: number }) => api.get('/ai/grade/history', { params }),
  getHistoryDetail: (id: number | string) => api.get(`/ai/grade/history/${id}`),
  deleteHistory: (id: number | string) => api.delete(`/ai/grade/history/${id}`).catch((err: any) => {
    // 兜底走 POST 兼容路径
    return api.post(`/ai/grade/history/${id}/delete`)
  })
}
