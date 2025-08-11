import { api } from './config'

export const aiApi = {
  chat: (data: { messages: { role: 'user'|'assistant'; content: string }[]; courseId?: number }) => {
    return api.post('/ai/chat', data)
  }
}

