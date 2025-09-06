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
  }
}

export default chatApi


