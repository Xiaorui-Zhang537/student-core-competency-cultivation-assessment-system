import { api } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

export const aiApi = {
  chat: (data: { messages: { role: ChatRole; content: string }[]; courseId?: number; studentIds?: number[]; model?: string; provider?: 'openrouter'|'deepseek' }): Promise<{ answer: string }> => {
    return api.post('/ai/chat', data)
  }
}

