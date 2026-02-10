import { api, baseURL } from './config'

type ChatRole = 'user' | 'assistant' | 'system'

const AI_CHAT_TIMEOUT = 120000

export interface AiStreamCallbacks {
  onMeta?: (data: { conversationId: number; model: string }) => void
  onToken?: (text: string) => void
  onDone?: (data: { messageId: number; fullText: string }) => void
  onError?: (message: string) => void
}

export const aiApi = {
  chat: (data: { messages: { role: ChatRole; content: string }[]; courseId?: number; studentIds?: number[]; model?: string; conversationId?: number; attachmentFileIds?: number[]; jsonOnly?: boolean; useGradingPrompt?: boolean }): Promise<{ answer: string }> => {
    return api.post('/ai/chat', data, { timeout: AI_CHAT_TIMEOUT })
  },

  /**
   * 流式聊天：通过 fetch + ReadableStream 接收 SSE 事件
   * 返回 AbortController 以支持中断生成
   */
  chatStream: (
    data: { messages: { role: ChatRole; content: string }[]; courseId?: number; studentIds?: number[]; model?: string; conversationId?: number; attachmentFileIds?: number[] },
    callbacks: AiStreamCallbacks
  ): AbortController => {
    const controller = new AbortController()
    const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
    const url = `${baseURL}/ai/chat/stream`

    fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      },
      body: JSON.stringify(data),
      signal: controller.signal,
    }).then(async (response) => {
      if (!response.ok) {
        let msg = 'AI 请求失败'
        try {
          const errBody = await response.json()
          msg = errBody?.message || errBody?.data?.message || msg
        } catch { /* ignore */ }
        callbacks.onError?.(msg)
        return
      }
      const reader = response.body?.getReader()
      if (!reader) { callbacks.onError?.('浏览器不支持流式读取'); return }

      const decoder = new TextDecoder()
      let buffer = ''

      /** 解析并分派缓冲区中的所有完整 SSE 事件 */
      function flushEvents() {
        // SSE 事件之间以 \n\n 分隔
        const blocks = buffer.split('\n\n')
        buffer = blocks.pop() || '' // 保留最后一个不完整的块
        for (const block of blocks) {
          if (!block.trim()) continue
          let evName = ''
          let evData = ''
          for (const line of block.split('\n')) {
            if (line.startsWith('event:')) evName = line.slice(6).trim()
            else if (line.startsWith('data:')) evData += (evData ? '\n' : '') + line.slice(5)
            else if (line.startsWith(':')) { /* SSE comment, ignore */ }
          }
          if (!evName || !evData) continue
          try {
            const parsed = JSON.parse(evData.trim())
            switch (evName) {
              case 'meta': callbacks.onMeta?.(parsed); break
              case 'token': callbacks.onToken?.(parsed.text || ''); break
              case 'done': callbacks.onDone?.(parsed); break
              case 'error': callbacks.onError?.(parsed.message || 'AI 请求失败'); break
              // ping 事件忽略
            }
          } catch { /* 非 JSON 数据忽略 */ }
        }
      }

      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          flushEvents()
        }
        // 流结束后刷新剩余缓冲
        if (buffer.trim()) { buffer += '\n\n'; flushEvents() }
      } catch (readErr: any) {
        // ERR_INCOMPLETE_CHUNKED_ENCODING 等网络错误：如果已经收到了 done 事件则忽略
        if (readErr?.name !== 'AbortError') {
          // 尝试刷新缓冲区中可能残留的最终事件
          if (buffer.trim()) { buffer += '\n\n'; flushEvents() }
        }
      }
    }).catch((err) => {
      if (err.name === 'AbortError') return // 用户主动中断
      callbacks.onError?.(err.message || 'AI 请求失败')
    })

    return controller
  },
  // Voice practice turn persistence
  saveVoiceTurn: (data: {
    conversationId: number
    model?: string
    userTranscript?: string
    assistantText?: string
    userAudioFileId?: number
    assistantAudioFileId?: number
    scenario?: string
    locale?: string
  }) => api.post('/ai/voice/turns', data),
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

