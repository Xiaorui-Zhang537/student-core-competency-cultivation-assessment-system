export interface HelpCategory {
  id: number
  name: string
  slug: string
  sort: number
  createdAt?: string
  updatedAt?: string
}

export interface HelpArticle {
  id: number
  categoryId: number
  title: string
  slug: string
  contentMd?: string
  contentHtml?: string
  tags?: string
  views?: number
  upVotes?: number
  downVotes?: number
  published?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface HelpTicket {
  id: number
  userId: number
  title: string
  description: string
  channel?: 'support' | 'feedback'
  ticketType?: string
  priority?: 'low' | 'medium' | 'high' | 'urgent'
  sourceRole?: 'student' | 'teacher' | 'admin' | string
  contact?: string
  anonymous?: boolean
  status: 'open' | 'in_progress' | 'resolved' | 'closed'
  assigneeAdminId?: number
  lastReplyAt?: string
  closedAt?: string
  username?: string
  userRole?: string
  assigneeName?: string
  createdAt?: string
  updatedAt?: string
}

export interface HelpTicketMessage {
  id: number
  ticketId: number
  senderId: number
  senderRole?: string
  senderSide: 'user' | 'admin'
  senderName?: string
  content: string
  createdAt?: string
}

export interface HelpTicketDetail {
  ticket: HelpTicket
  messages: HelpTicketMessage[]
}

export interface HelpTicketCreateRequest {
  title: string
  content: string
  channel?: 'support' | 'feedback'
  ticketType?: string
  priority?: 'low' | 'medium' | 'high' | 'urgent'
  contact?: string
  anonymous?: boolean
}

export interface HelpArticleUpsertRequest {
  categoryId: number
  title: string
  slug?: string
  contentMd: string
  contentHtml?: string
  tags?: string
  published?: boolean
}

export interface HelpCategoryCreateRequest {
  name: string
  slug?: string
  sort?: number
}
