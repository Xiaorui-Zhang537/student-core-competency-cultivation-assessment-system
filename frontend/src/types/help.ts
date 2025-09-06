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
  status: 'open' | 'in_progress' | 'resolved' | 'closed'
  createdAt?: string
  updatedAt?: string
}


