import { defineStore } from 'pinia'
import { helpApi } from '@/api/help.api'
import type { HelpCategory, HelpArticle, HelpTicket } from '@/types/help'

export const useHelpStore = defineStore('help', {
  state: () => ({
    categories: [] as HelpCategory[],
    articles: [] as HelpArticle[],
    article: null as HelpArticle | null,
    tickets: [] as HelpTicket[],
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
      await helpApi.submitArticleFeedback(articleId, payload)
    },
    async submitTicket(title: string, description: string) {
      const t = await helpApi.createTicket(title, description)
      this.tickets.unshift(t)
      return t
    },
    async fetchMyTickets() {
      this.tickets = await helpApi.myTickets()
    }
    ,
    async updateTicket(id: number, title: string, description: string) {
      const t = await helpApi.updateTicket(id, title, description)
      const idx = this.tickets.findIndex(x => x.id === id)
      if (idx >= 0) this.tickets[idx] = t
      return t
    }
    ,
    async deleteTicket(id: number) {
      await helpApi.deleteTicket(id)
      this.tickets = this.tickets.filter(x => x.id !== id)
    }
  }
})


