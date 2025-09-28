import { api } from './config'

export interface ChatConversationItem {
  id: string | number
  courseId?: string | number | null
  lastMessageId?: string | number | null
  lastMessageAt?: string
  unread: number
  pinned?: boolean
  archived?: boolean
  peerId: string | number
  peerUsername: string
  peerAvatar?: string | null
  lastContent?: string | null
}

export interface Paged<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
}

export const chatApi = {
  getMyConversations: (params?: { page?: number; size?: number; pinned?: boolean; archived?: boolean }) => {
    return api.get<Paged<ChatConversationItem>>('/chat/conversations/my', { params })
  },
  markConversationRead: (conversationId: string | number) => {
    return api.put(`/chat/conversations/${conversationId}/read`)
  },
  getMessages: (peerId: string|number, params?: { page?: number; size?: number; courseId?: string|number }) => {
    return api.get('/chat/messages', { params: { peerId, ...(params||{}) } })
  },
  sendMessage: (data: { recipientId: string|number; content: string; relatedType?: string; relatedId?: string|number; attachmentFileIds?: Array<string|number> }) => {
    return api.post('/chat/messages', data)
  },
  markReadByPeer: (peerId: string|number, courseId?: string|number) => {
    return api.put(`/chat/conversations/peer/${peerId}/read`, undefined, { params: { courseId } })
  },
  archiveConversation: (conversationId: string|number, archived: boolean) => {
    return api.put(`/chat/conversations/${conversationId}/archive`, undefined, { params: { archived } })
  }
}

export default chatApi


