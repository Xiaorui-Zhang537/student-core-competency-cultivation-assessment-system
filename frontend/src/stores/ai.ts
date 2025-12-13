import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { aiApi } from '@/api/ai.api'
import { handleApiCall } from '@/utils/api-handler'
import { useUIStore } from '@/stores/ui'

export interface AiConversation {
  id: number
  title: string
  model?: string
  provider?: string
  pinned?: boolean
  archived?: boolean
  lastMessageAt?: string
}

export interface AiMessage {
  id: number
  role: 'user' | 'assistant' | 'system'
  content: string
  attachments?: number[]
  createdAt?: string
}

export const useAIStore = defineStore('ai', () => {
  const ui = useUIStore()
  const conversations = ref<AiConversation[]>([])
  const activeConversationId = ref<number | null>(null)
  const messagesByConvId = reactive<Record<string, AiMessage[]>>({})
  const draftsByConvId = reactive<Record<string, string>>({})
  const pendingAttachmentIds = reactive<Record<string, number[]>>({})
  const searchQuery = ref('')
  const model = ref('google/gemini-2.5-pro')

  const fetchConversations = async (params?: { q?: string; pinned?: boolean; archived?: boolean; page?: number; size?: number }) => {
    const res: any = await handleApiCall(() => aiApi.listConversations(params), ui, '加载会话失败')
    const data = res?.data ?? res
    conversations.value = data?.items || data?.list || data || []
  }

  const createConversation = async (payload?: { title?: string; model?: string; provider?: string }) => {
    const res: any = await handleApiCall(() => aiApi.createConversation(payload || {}), ui, '创建会话失败')
    const c = res?.data ?? res
    if (c?.id) {
      conversations.value = [c, ...conversations.value]
      activeConversationId.value = c.id
      messagesByConvId[String(c.id)] = []
      draftsByConvId[String(c.id)] = ''
      pendingAttachmentIds[String(c.id)] = []
    }
    return c
  }

  // 查找“未发送会话”：消息列表为空的会话
  const getUnsentConversation = (): AiConversation | null => {
    // 优先：当前激活且未发送
    if (activeConversationId.value) {
      const key = String(activeConversationId.value)
      const msgs = messagesByConvId[key]
      if (!msgs || msgs.length === 0) {
        const current = conversations.value.find(c => c.id === activeConversationId.value)
        if (current) return current
      }
    }
    // 其次：会话列表中第一个未发送的（createConversation 会将新会话放在最前）
    for (const c of conversations.value) {
      const msgs = messagesByConvId[String(c.id)]
      if (!msgs || msgs.length === 0) return c
    }
    return null
  }

  // 确保存在一个用于草稿的会话：若有未发送会话则复用并可更新标题，否则创建
  const ensureDraftConversation = async (title?: string): Promise<AiConversation> => {
    const existed = getUnsentConversation()
    if (existed) {
      if (title && existed.title !== title) {
        try {
          await handleApiCall(() => aiApi.updateConversation(existed.id, { title }), ui, '更新会话失败')
          const idx = conversations.value.findIndex(c => c.id === existed.id)
          if (idx >= 0) conversations.value[idx].title = title
        } catch {}
      }
      activeConversationId.value = existed.id
      const key = String(existed.id)
      if (!messagesByConvId[key]) messagesByConvId[key] = []
      if (!draftsByConvId[key]) draftsByConvId[key] = ''
      if (!pendingAttachmentIds[key]) pendingAttachmentIds[key] = []
      return existed
    }
    const created = await createConversation({ title, model: model.value })
    return created as AiConversation
  }

  const setDraftForActive = (text: string) => {
    if (!activeConversationId.value) return
    draftsByConvId[String(activeConversationId.value)] = text
  }

  const openConversation = async (id: number) => {
    activeConversationId.value = id
    if (!messagesByConvId[String(id)]) {
      await fetchMessages(id)
    }
    // 不再强制覆盖左侧选择器（过滤器）模型，避免右上角模型显示随切换改变
    // 左侧选择器仅用于“新建会话”的初始模型；已存在会话固定自己的模型
  }

  const renameConversation = async (id: number, title: string) => {
    await handleApiCall(() => aiApi.updateConversation(id, { title }), ui, '重命名失败')
    const idx = conversations.value.findIndex(c => c.id === id)
    if (idx >= 0) conversations.value[idx].title = title
  }

  const pinConversation = async (id: number, pinned: boolean) => {
    await handleApiCall(() => aiApi.updateConversation(id, { pinned }), ui, '置顶失败')
    const it = conversations.value.find(c => c.id === id)
    if (it) it.pinned = pinned
  }

  const archiveConversation = async (id: number, archived: boolean) => {
    await handleApiCall(() => aiApi.updateConversation(id, { archived }), ui, '归档失败')
    const it = conversations.value.find(c => c.id === id)
    if (it) it.archived = archived
  }

  const removeConversation = async (id: number) => {
    await handleApiCall(() => aiApi.removeConversation(id), ui, '删除会话失败', { successMessage: '已删除会话' })
    conversations.value = conversations.value.filter(c => c.id !== id)
    delete messagesByConvId[String(id)]
    delete draftsByConvId[String(id)]
    delete pendingAttachmentIds[String(id)]
    if (activeConversationId.value === id) activeConversationId.value = conversations.value[0]?.id || null
  }

  const fetchMessages = async (id: number, params?: { page?: number; size?: number }) => {
    const res: any = await handleApiCall(() => aiApi.listMessages(id, { page: params?.page || 1, size: params?.size || 100 }), ui, '加载消息失败')
    const data = res?.data ?? res
    const items: AiMessage[] = data?.items || data?.list || data || []
    messagesByConvId[String(id)] = items
  }

  const addPendingAttachment = (id: number, fileId: number) => {
    const key = String(id)
    pendingAttachmentIds[key] = pendingAttachmentIds[key] || []
    pendingAttachmentIds[key].push(fileId)
  }

  const clearPendingAttachments = (id: number) => {
    pendingAttachmentIds[String(id)] = []
  }

  const saveDraft = (id: number, text: string) => {
    draftsByConvId[String(id)] = text
  }

  const sendMessage = async (payload: { conversationId?: number; content: string; courseId?: number; studentIds?: number[] }) => {
    let convId = payload.conversationId || activeConversationId.value
    // 若存在激活会话但其模型与当前选择不一致，则新建会话（单会话单模型）
    if (convId) {
      const curr = conversations.value.find(c => c.id === convId)
      const currModel = curr?.model || ''
      if (currModel && currModel !== model.value) {
        const created = await createConversation({ title: payload.content?.slice(0, 20) || '新对话', model: model.value })
        convId = created?.id
      }
    }
    if (!convId) {
      const created = await createConversation({ title: payload.content?.slice(0, 20) || '新对话', model: model.value })
      convId = created?.id
    }
    if (!convId) return null
    const key = String(convId)
    messagesByConvId[key] = messagesByConvId[key] || []
    messagesByConvId[key].push({ id: Date.now(), role: 'user', content: payload.content })
    // 首次用户发言即锁定会话模型（即使请求失败也固定显示）
    const idxBefore = conversations.value.findIndex(c => c.id === convId)
    if (idxBefore >= 0 && !conversations.value[idxBefore].model) {
      conversations.value[idxBefore].model = model.value
    }

    const attachments = (pendingAttachmentIds[key] || []).slice()

    const resp: any = await handleApiCall(() => aiApi.chat({
      messages: [{ role: 'user', content: payload.content }],
      courseId: payload.courseId,
      studentIds: payload.studentIds,
      // 单会话单模型：后端按会话模型执行
      ...(convId ? { conversationId: convId } : {}),
      ...(attachments.length ? { attachmentFileIds: attachments } : {}),
      
    }), ui, '发送失败')

    let finalResp: any = resp
    const currentConv = conversations.value.find(c => c.id === convId)
    const currentModelValue = (currentConv?.model || model.value || '').toLowerCase()
    // 若失败（null）且疑似配额/频率问题，尝试自动切换更稳模型并重试一次
    if (!finalResp) {
      const order = ['gemini-2.5-pro', 'gemini-2.5-flash', 'gemini-2.5-flash-lite']
      const normalized = currentModelValue.replace(/^google\//, '')
      const next = (() => {
        const idx = order.findIndex(m => normalized.startsWith(m))
        if (idx === -1) return `google/${order[0]}`
        if (idx < order.length - 1) return `google/${order[idx + 1]}`
        return null
      })()
      if (next) {
        try {
          await handleApiCall(() => aiApi.updateConversation(Number(convId), { model: next }), ui, '切换模型失败')
          const idx = conversations.value.findIndex(c => c.id === convId)
          if (idx >= 0) conversations.value[idx].model = next
          ui.showNotification({ type: 'info', title: '模型已切换', message: '检测到 Gemini 配额受限，已自动降级模型并重试。' })
          finalResp = await handleApiCall(() => aiApi.chat({
            messages: [{ role: 'user', content: payload.content }],
            courseId: payload.courseId,
            studentIds: payload.studentIds,
            conversationId: convId,
            ...(attachments.length ? { attachmentFileIds: attachments } : {}),
          }), ui, '发送失败')
        } catch {}
      }
    }
    if (!finalResp && currentModelValue.includes('glm')) {
      const glmOrder = ['glm-4.6', 'glm-4.5-air']
      const nextGlm = (() => {
        const idx = glmOrder.findIndex(m => currentModelValue.startsWith(m))
        if (idx === -1) return glmOrder[0]
        if (idx < glmOrder.length - 1) return glmOrder[idx + 1]
        return null
      })()
      if (nextGlm) {
        try {
          await handleApiCall(() => aiApi.updateConversation(Number(convId), { model: nextGlm }), ui, '切换模型失败')
          const idx = conversations.value.findIndex(c => c.id === convId)
          if (idx >= 0) conversations.value[idx].model = nextGlm
          ui.showNotification({ type: 'info', title: '模型已切换', message: '检测到 GLM 调用失败，已自动降级并重试。' })
          finalResp = await handleApiCall(() => aiApi.chat({
            messages: [{ role: 'user', content: payload.content }],
            courseId: payload.courseId,
            studentIds: payload.studentIds,
            conversationId: convId,
            ...(attachments.length ? { attachmentFileIds: attachments } : {}),
          }), ui, '发送失败')
        } catch {}
      }
    }

    const answer = finalResp?.answer || (finalResp?.data?.answer)
    if (answer !== undefined) {
      messagesByConvId[key].push({ id: Date.now() + 1, role: 'assistant', content: String(answer || '...') })
    }
    // 一旦该会话产生（即使失败也已入列用户消息），锁定其模型显示：若会话对象缺少模型信息，则补齐为当前选择模型
    const idx = conversations.value.findIndex(c => c.id === convId)
    if (idx >= 0 && !conversations.value[idx].model) {
      conversations.value[idx].model = model.value
    }
    draftsByConvId[key] = ''
    clearPendingAttachments(convId)
    return resp
  }

  return {
    conversations,
    activeConversationId,
    messagesByConvId,
    draftsByConvId,
    pendingAttachmentIds,
    searchQuery,
    model,
    fetchConversations,
    createConversation,
    openConversation,
    renameConversation,
    pinConversation,
    archiveConversation,
    removeConversation,
    fetchMessages,
    addPendingAttachment,
    clearPendingAttachments,
    saveDraft,
    getUnsentConversation,
    ensureDraftConversation,
    setDraftForActive,
    sendMessage
  }
})
