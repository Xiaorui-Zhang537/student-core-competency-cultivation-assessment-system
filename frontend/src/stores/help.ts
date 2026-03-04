import { defineStore } from 'pinia'
import { helpApi } from '@/api/help.api'
import type { HelpCategory, HelpArticle, HelpTicket, HelpTicketCreateRequest, HelpTicketDetail } from '@/types/help'

export const useHelpStore = defineStore('help', {
  state: () => ({
    categories: [] as HelpCategory[],
    articles: [] as HelpArticle[],
    article: null as HelpArticle | null,
    tickets: [] as HelpTicket[],
    ticketDetail: null as HelpTicketDetail | null,
    loading: false,
    error: '' as string | ''
  }),
  actions: {
    async fetchCategories() {
      this.categories = await helpApi.listCategories()
    },
    async searchArticles(q?: string, categoryId?: number, tag?: string, sort: 'latest' | 'hot' = 'latest') {
      this.loading = true
      try {
        this.articles = await helpApi.listArticles({ q, categoryId, tag, sort })
      } finally {
        this.loading = false
      }
    },
    async fetchArticle(slug: string, inc: boolean = true) {
      this.article = await helpApi.getArticle(slug, inc)
    },
    async voteOrFeedback(articleId: number, payload: { helpful?: boolean; content?: string }) {
      const updated = await helpApi.submitArticleFeedback(articleId, payload)
      this.article = updated
      const idx = this.articles.findIndex(item => item.id === updated.id)
      if (idx >= 0) {
        this.articles[idx] = updated
      }
      return updated
    },
    async submitTicket(payload: HelpTicketCreateRequest) {
      const t = await helpApi.createTicket(payload)
      this.tickets.unshift(t)
      return t
    },
    async fetchMyTickets() {
      this.tickets = await helpApi.myTickets()
    },
    async fetchTicketDetail(id: number) {
      this.ticketDetail = await helpApi.getTicket(id)
      return this.ticketDetail
    },
    async replyTicket(id: number, content: string) {
      this.ticketDetail = await helpApi.replyTicket(id, content)
      const latest = this.ticketDetail?.ticket
      if (latest) {
        const idx = this.tickets.findIndex(x => x.id === id)
        if (idx >= 0) this.tickets[idx] = latest
        else this.tickets.unshift(latest)
      }
      return this.ticketDetail
    },
    async deleteTicket(id: number) {
      await helpApi.deleteTicket(id)
      this.tickets = this.tickets.filter(x => x.id !== id)
      if (this.ticketDetail?.ticket?.id === id) this.ticketDetail = null
    }
  }
})
