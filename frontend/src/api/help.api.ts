import { api } from './config'
import type { HelpCategory, HelpArticle, HelpTicket } from '@/types/help'

export const helpApi = {
  listCategories: () => api.get<HelpCategory[]>('/help/categories'),

  listArticles: (params?: { q?: string; categoryId?: number; tag?: string; sort?: 'latest' | 'hot' }) =>
    api.get<HelpArticle[]>('/help/articles', { params }),

  getArticle: (slug: string, inc: boolean = true) =>
    api.get<HelpArticle>(`/help/articles/${encodeURIComponent(slug)}`, { params: { inc } }),

  submitArticleFeedback: (articleId: number, data: { helpful?: boolean; content?: string }) =>
    api.post<void>(`/help/articles/${articleId}/feedback`, null, { params: data }),

  createTicket: (title: string, description: string) =>
    api.post<HelpTicket>('/help/tickets', { title, description }),

  myTickets: () => api.get<HelpTicket[]>('/help/tickets')
  ,
  updateTicket: (id: number, title: string, description: string) =>
    api.put<HelpTicket>(`/help/tickets/${id}`, { title, description }),
  deleteTicket: (id: number) => api.delete<void>(`/help/tickets/${id}`)
}


