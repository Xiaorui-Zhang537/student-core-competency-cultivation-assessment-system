import { api } from './config'
import type { PaginatedResponse } from '@/types/api'
import type { HelpCategory, HelpArticle, HelpArticleUpsertRequest, HelpCategoryCreateRequest, HelpTicket, HelpTicketCreateRequest, HelpTicketDetail } from '@/types/help'

export const helpApi = {
  listCategories: () => api.get<HelpCategory[]>('/help/categories'),

  listArticles: (params?: { q?: string; categoryId?: number; tag?: string; sort?: 'latest' | 'hot' }) =>
    api.get<HelpArticle[]>('/help/articles', { params }),

  getArticle: (slug: string, inc: boolean = true) =>
    api.get<HelpArticle>(`/help/articles/${encodeURIComponent(slug)}`, { params: { inc } }),

  submitArticleFeedback: (articleId: number, data: { helpful?: boolean; content?: string }) =>
    api.post<HelpArticle>(`/help/articles/${articleId}/feedback`, null, { params: data }),

  createTicket: (data: HelpTicketCreateRequest) =>
    api.post<HelpTicket>('/help/tickets', data),

  myTickets: () => api.get<HelpTicket[]>('/help/tickets'),
  getTicket: (id: number) => api.get<HelpTicketDetail>(`/help/tickets/${id}`),
  replyTicket: (id: number, content: string) => api.post<HelpTicketDetail>(`/help/tickets/${id}/reply`, { content }),
  deleteTicket: (id: number) => api.delete<void>(`/help/tickets/${id}`),

  adminPageTickets: (params?: {
    page?: number
    size?: number
    status?: string
    channel?: string
    priority?: string
    sourceRole?: string
    q?: string
  }) => api.get<PaginatedResponse<HelpTicket>>('/admin/help/tickets', { params }),
  adminGetTicket: (id: number | string) => api.get<HelpTicketDetail>(`/admin/help/tickets/${id}`),
  adminReplyTicket: (id: number | string, content: string) =>
    api.post<HelpTicketDetail>(`/admin/help/tickets/${id}/reply`, { content }),
  adminUpdateTicketStatus: (id: number | string, status: string) =>
    api.put<HelpTicket>(`/admin/help/tickets/${id}/status`, { status }),
  adminListArticles: (params?: { q?: string; categoryId?: number; published?: boolean }) =>
    api.get<HelpArticle[]>('/admin/help/articles', { params }),
  adminCreateArticle: (data: HelpArticleUpsertRequest) =>
    api.post<HelpArticle>('/admin/help/articles', data),
  adminUpdateArticle: (id: number | string, data: HelpArticleUpsertRequest) =>
    api.put<HelpArticle>(`/admin/help/articles/${id}`, data),
  adminDeleteArticle: (id: number | string) =>
    api.delete<void>(`/admin/help/articles/${id}`),
  adminCreateCategory: (data: HelpCategoryCreateRequest) =>
    api.post<HelpCategory>('/admin/help/categories', data)
}
