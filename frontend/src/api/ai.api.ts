import { api } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

export const aiApi = {
  chat: (data: { messages: { role: ChatRole; content: string }[]; courseId?: number; studentIds?: number[]; model?: string; conversationId?: number; attachmentFileIds?: number[]; jsonOnly?: boolean; useGradingPrompt?: boolean }): Promise<{ answer: string }> => {
    return api.post('/ai/chat', data)
  },
  // Conversations
  createConversation: (data: { title?: string; model?: string; provider?: string }) => api.post('/ai/conversations', data),
  listConversations: (params?: { q?: string; pinned?: boolean; archived?: boolean; page?: number; size?: number }) => api.get('/ai/conversations', { params }),
  updateConversation: (
    id: string | number,
    data: { title?: string; pinned?: boolean; archived?: boolean; model?: string; provider?: string }
  ) => api.put(`/ai/conversations/${id}`, data),
  removeConversation: (id: string | number) => api.delete(`/ai/conversations/${id}`),
  listMessages: (id: string | number, params?: { page?: number; size?: number }) => api.get(`/ai/conversations/${id}/messages`, { params }),
  // Memory
  getMemory: () => api.get('/ai/memory'),
  updateMemory: (data: { enabled?: boolean; content?: string }) => api.put('/ai/memory', data)
}

