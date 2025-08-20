<template>
  <div class="relative">
  <div class="min-h-screen p-4 md:p-6">
    <!-- Page Header -->
    <div class="max-w-6xl mx-auto mb-4 md:mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">{{ t('teacher.ai.title') || 'AI 助手' }}</h1>
          <p class="text-gray-600 dark:text-gray-400 mt-1">{{ t('teacher.ai.subtitle') || '多会话、记忆与文件附加' }}</p>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-4">
      <!-- Sidebar -->
      <aside class="md:col-span-4 lg:col-span-4 xl:col-span-3 card p-4 space-y-4">
        <div>
          <label class="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">{{ t('common.search') || '搜索' }}</label>
          <div class="flex items-center gap-2">
            <input v-model="q" type="text" :placeholder="t('common.search') || '搜索会话'" class="input flex-1" @keyup.enter="search" />
            <button class="btn btn-primary" @click="newConv">{{ t('common.new') || '新建' }}</button>
          </div>
        </div>

        <div>
          <div class="flex items-center justify-between mb-2">
            <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('teacher.ai.conversations') || '我的会话' }}</h3>
            <span class="text-xs text-gray-500">{{ conversations.length }}</span>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700 rounded-lg border border-gray-100 dark:border-gray-700 overflow-hidden max-h-[50vh] overflow-y-auto">
            <button v-for="c in conversations" :key="c.id"
                    class="w-full text-left px-3 py-2 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
                    :class="activeConversationId === c.id ? 'bg-gray-50 dark:bg-gray-700/50' : ''"
                    @click="open(c.id)">
              <div class="flex items-center justify-between gap-3">
                <div class="min-w-0">
                  <div class="truncate font-medium">{{ c.title || (t('teacher.ai.untitled') || '未命名会话') }}</div>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <button class="icon-btn" :title="t('common.rename') || '重命名'" @click.stop="rename(c)">
                    <svg class="w-4 h-4 text-gray-600 dark:text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 20h9"/>
                      <path d="M16.5 3.5l4 4-11 11H5.5v-4.5l11-10.5z"/>
                    </svg>
                  </button>
                  <button class="icon-btn" :title="c.pinned ? (t('common.unpin') || '取消置顶') : (t('common.pin') || '置顶')" @click.stop="pin(c, !c.pinned)">
                    <svg class="w-4 h-4" :class="c.pinned ? 'text-primary-600' : 'text-gray-500 dark:text-gray-400'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                      <path :fill="c.pinned ? 'currentColor' : 'none'" d="M12 17l-5.878 3.09 1.123-6.545L2.49 8.91l6.561-.953L12 2l2.949 5.957 6.561.953-4.755 4.635 1.123 6.545z"/>
                    </svg>
                  </button>
                  <button class="icon-btn text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20" :title="t('common.delete') || '删除'" @click.stop="remove(c.id)">
                    <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                      <path d="M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                    </svg>
                  </button>
                </div>
              </div>
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('teacher.ai.model.title') || '模型' }}</h3>
          <select v-model="model" class="input">
            <option value="openai/gpt-4o-mini">gpt-4o-mini</option>
          </select>
        </div>

        <div class="space-y-2">
          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('teacher.ai.memory') || '长期记忆' }}</h3>
          <div class="flex items-center gap-2">
            <label class="text-xs text-gray-500">{{ t('common.enable') || '启用' }}</label>
            <input type="checkbox" v-model="memory.enabled" @change="saveMemory" />
          </div>
          <textarea v-model="memory.content" rows="4" class="input" :placeholder="t('teacher.ai.memoryPlaceholder') || '个性化偏好、口吻等...'" @blur="saveMemory"></textarea>
        </div>
      </aside>

      <!-- Chat Pane -->
      <section class="md:col-span-8 lg:col-span-8 xl:col-span-9 card p-0 overflow-hidden">
        <!-- Chat header -->
        <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
          <div class="flex items-center justify-between">
            <div class="min-w-0">
              <div class="font-semibold text-gray-800 dark:text-gray-100 truncate">{{ activeTitle }}</div>
            </div>
          </div>
        </div>

        <div class="h-[60vh] overflow-y-auto p-4 space-y-4 bg-white dark:bg-gray-800">
          <div v-for="m in currentMessages" :key="m.id" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
            <div class="max-w-[80%] rounded-2xl px-4 py-2 text-sm"
                 :class="m.role === 'user' ? 'bg-primary-600 text-white rounded-br-none' : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-100 rounded-bl-none'">
              <p class="whitespace-pre-wrap">{{ m.content }}</p>
            </div>
          </div>
        </div>
        <div class="border-t border-gray-200 dark:border-gray-700 p-3 bg-gray-50 dark:bg-gray-900 flex items-center gap-3">
          <textarea
            ref="textareaRef"
            v-model="draft"
            @keydown.enter.exact.prevent="send"
            @input="onTextareaInput"
            rows="2"
            :style="{ height: textareaHeight }"
            :placeholder="t('teacher.ai.placeholder') || '输入问题...'"
            class="flex-1 input resize-none overflow-auto"
          />
          <div v-if="showUpload" class="flex items-center gap-2">
            <!-- Icon-only image picker -->
            <input ref="fileInput" type="file" class="hidden" accept="image/*" multiple @change="handlePickFiles" />
            <button class="icon-btn" :title="t('common.uploadImage') || '上传图片'" @click.prevent="triggerPick">
              <svg class="w-5 h-5 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <path d="M21 15l-4.5-4.5L9 18"/>
              </svg>
            </button>
            <span v-if="pendingCount>0" class="text-xs px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">已选 {{ pendingCount }} 项</span>
          </div>
          <button class="btn btn-primary" :disabled="!canSend" @click="send">{{ t('teacher.ai.send') || '发送' }}</button>
        </div>
      </section>
    </div>
  </div>

  <!-- Rename Modal -->
  <div v-if="showRename" class="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
    <div class="w-full max-w-sm card p-4">
      <h3 class="text-base font-semibold mb-3">{{ t('teacher.ai.renameTitle') || '重命名对话' }}</h3>
      <input v-model="renameTitle" type="text" class="input mb-4" placeholder="请输入新的标题" />
      <div class="flex justify-end gap-2">
        <button class="btn" @click="showRename=false">{{ t('common.cancel') || '取消' }}</button>
        <button class="btn btn-primary" @click="confirmRename">{{ t('teacher.courses.actions.save') || '保存' }}</button>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAIStore } from '@/stores/ai'
import { baseURL } from '@/api/config'
import { useI18n } from 'vue-i18n'
import { fileApi } from '@/api/file.api'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const ai = useAIStore()
const q = ref('')
const uploadHeaders = { Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' } as Record<string, string>
const showUpload = ref(true) // 多模态开关预留
const fileInput = ref<HTMLInputElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const textareaHeight = ref<string>('auto')

const conversations = computed(() => {
  const list = [...ai.conversations]
  const activeId = activeConversationId.value
  const toTs = (s?: string) => s ? new Date(s).getTime() : 0
  return list.sort((a, b) => {
    if (!!a.pinned !== !!b.pinned) return a.pinned ? -1 : 1
    if (a.pinned && b.pinned) {
      // 置顶内按最近活动排序
      return toTs(b.lastMessageAt) - toTs(a.lastMessageAt)
    }
    return toTs(b.lastMessageAt) - toTs(a.lastMessageAt)
  })
})
const activeConversationId = computed({ get: () => ai.activeConversationId, set: v => (ai.activeConversationId = v as any) })
const model = computed({ get: () => ai.model, set: v => (ai.model = v as any) })
const currentMessages = computed(() => ai.messagesByConvId[String(activeConversationId.value || '')] || [])
const activeTitle = computed(() => {
  const c = conversations.value.find(c => c.id === activeConversationId.value)
  return c?.title || (t('teacher.ai.untitled') || '未命名会话')
})
// 在未创建会话时使用本地草稿，避免按钮一直禁用
const newDraft = ref('')
const draft = computed({
  get: () => {
    if (activeConversationId.value) {
      return ai.draftsByConvId[String(activeConversationId.value)] || ''
    }
    return newDraft.value
  },
  set: (v: string) => {
    if (activeConversationId.value) {
      ai.saveDraft(activeConversationId.value, v)
    } else {
      newDraft.value = v
    }
  }
})

const memory = ref<{ enabled: boolean; content: string }>({ enabled: true, content: '' })
const canSend = computed(() => {
  const v = draft.value || ''
  return typeof v === 'string' && v.trim().length > 0
})
const pendingCount = computed(() => {
  const id = activeConversationId.value
  if (!id) return 0
  const list = ai.pendingAttachmentIds[String(id)] || []
  return list.length
})

const search = async () => {
  await ai.fetchConversations({ q: q.value })
}
const newConv = async () => {
  await ai.createConversation({ title: '新对话', model: model.value })
}
const open = async (id: number) => {
  await ai.openConversation(id)
}
const pin = async (c: any, pinned: boolean) => {
  await ai.pinConversation(c.id, pinned)
}
const archive = async (c: any, archived: boolean) => {
  await ai.archiveConversation(c.id, archived)
}
const remove = async (id: number) => {
  await ai.removeConversation(id)
}
const renameTargetId = ref<number | null>(null)
const renameTitle = ref('')
const showRename = ref(false)
const rename = (c: any) => {
  renameTargetId.value = c.id
  renameTitle.value = c.title || ''
  showRename.value = true
}
const confirmRename = async () => {
  if (renameTargetId.value && renameTitle.value.trim()) {
    await ai.renameConversation(renameTargetId.value, renameTitle.value.trim())
  }
  showRename.value = false
}
const send = async () => {
  const text = (draft.value || '').trim()
  if (!text) return
  await ai.sendMessage({ content: text })
  // 清空草稿（新会话与已有会话）
  if (activeConversationId.value) {
    ai.saveDraft(activeConversationId.value, '')
  } else {
    newDraft.value = ''
  }
  // 发送后重置输入框高度
  textareaHeight.value = 'auto'
}
const triggerPick = () => fileInput.value?.click()
const handlePickFiles = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0) return
  // 确保有会话ID（复用未发送会话）
  const conv = await ai.ensureDraftConversation(draft.value?.slice(0, 20) || '新对话')
  const convId = conv?.id
  if (!convId) return
  // 顺序上传并记录ID
  for (const f of Array.from(files)) {
    try {
      const info: any = await fileApi.uploadFile(f as File, { purpose: 'ai_chat', relatedId: String(convId) })
      const fid = info?.id || info?.fileId
      if (fid) ai.addPendingAttachment(convId, Number(fid))
    } catch (err) {
      // 忽略单个文件错误，保留其他
      console.error('图像上传失败', err)
    }
  }
  // 清空 input 以便重复选择同样文件
  if (fileInput.value) fileInput.value.value = ''
}

const saveMemory = async () => {
  const { aiApi } = await import('@/api/ai.api')
  await aiApi.updateMemory({ enabled: memory.value.enabled, content: memory.value.content })
}

onMounted(async () => {
  const { aiApi } = await import('@/api/ai.api')
  await ai.fetchConversations({ page: 1, size: 50 })
  // 处理从入口带来的预填文案：使用单一watch避免重复
  const prefillState = { running: false as boolean, last: '' as string }
  watch(
    () => route.query.q,
    async (qv) => {
      const q = (qv as string) || ''
      if (!q || !q.trim()) return
      if (prefillState.running) return
      if (prefillState.last === q) return
      prefillState.running = true
      prefillState.last = q
      try {
        const conv = await ai.ensureDraftConversation(q.slice(0, 20) || '新对话')
        if (conv?.id) {
          activeConversationId.value = conv.id
          ai.saveDraft(conv.id, q)
          // 使用路由自身替换，移除 q，避免多次触发与history状态警告
          const nextQuery: Record<string, any> = { ...route.query }
          delete nextQuery.q
          await router.replace({ query: nextQuery })
        }
      } finally {
        prefillState.running = false
      }
    },
    { immediate: true }
  )
  try {
    const m: any = await aiApi.getMemory()
    const data = m?.data ?? m
    memory.value.enabled = !!(data?.enabled)
    memory.value.content = data?.content || ''
  } catch {}
})

// 根据内容自适应高度（最小2行，最大10行）
const onTextareaInput = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  const lineHeight = parseFloat(getComputedStyle(el).lineHeight || '20')
  const min = lineHeight * 2
  const max = lineHeight * 10
  const next = Math.min(Math.max(el.scrollHeight, min), max)
  textareaHeight.value = `${next}px`
}
</script>

<style scoped>
.card {
  @apply bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700;
}
.input {
  @apply w-full rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500;
}
.btn {
  @apply inline-flex items-center justify-center px-3 py-2 rounded-md text-sm font-medium;
}
.btn-primary {
  @apply bg-primary-600 text-white hover:bg-primary-700 disabled:opacity-60;
}
.icon-btn {
  @apply inline-flex items-center justify-center w-8 h-8 rounded-md border border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700;
}
</style>


