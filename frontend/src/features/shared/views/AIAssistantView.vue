<template>
  <div class="relative">
    <div class="min-h-screen p-4 md:p-6">
      <div class="max-w-6xl mx-auto mb-4 md:mb-6">
        <page-header :title="t('teacher.ai.title')" :subtitle="t('teacher.ai.subtitle')">
          <template #actions>
            <Button variant="success" icon="user-plus" class="whitespace-nowrap shrink-0" @click="goVoicePractice">
              {{ t('shared.voicePractice.entry') }}
            </Button>
          </template>
        </page-header>
      </div>

      <div class="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-4">
        <aside class="md:col-span-4 lg:col-span-4 xl:col-span-3 filter-container p-4 space-y-4 rounded-xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="flex items-center justify-between gap-3 mb-2">
              <div class="flex items-center gap-2 min-w-0">
                <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200 truncate">{{ t('teacher.ai.conversations') || '会话' }}</h3>
                <span class="text-xs text-gray-500 shrink-0">{{ conversations.length }}</span>
              </div>
              <Button size="sm" variant="primary" icon="plus" class="whitespace-nowrap shrink-0" @click="newConv">{{ t('common.new') || '新建' }}</Button>
            </div>
            <div class="flex items-center gap-2 mb-3">
              <div class="flex-1 min-w-0">
                <glass-search-input v-model="q" :placeholder="t('common.search') || '搜索会话'" />
              </div>
            </div>

            <div class="pt-3 mb-3 border-t border-white/15 dark:border-white/10">
              <div class="space-y-2">
                <div class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('teacher.ai.model.title') || '模型' }}</div>
                <glass-popover-select
                  v-model="model"
                  :options="modelOptions"
                  size="sm"
                />
              </div>
            </div>

            <div class="divide-y divide-gray-100 dark:divide-gray-700 rounded-lg border border-gray-100 dark:border-gray-700 overflow-hidden max-h-[50vh] overflow-y-auto">
              <Button
                v-for="c in conversations"
                :key="c.id"
                variant="menu"
                class="w-full px-2 py-3 text-left transition-colors rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 min-h-[64px] focus:outline-none focus:ring-0 focus-visible:ring-0 focus:ring-offset-0"
                :class="activeConversationId === c.id ? 'conv-selected border-transparent ring-0 outline-none focus:ring-0 focus-visible:ring-0' : ''"
                @click="open(c.id)"
              >
                <div class="flex flex-col gap-1 w-full px-3">
                  <div class="flex items-center gap-3 w-full min-w-0">
                    <div class="font-medium truncate flex-1 min-w-0">{{ c.title || (t('teacher.ai.untitled') || '未命名会话') }}</div>
                    <div class="flex items-center gap-2 shrink-0 ml-auto">
                      <Button variant="ghost" size="xs" class="text-base-content/70 hover:text-base-content" :title="t('common.rename') || '重命名'" @click.stop="rename(c)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M12 20h9"/>
                          <path d="M16.5 3.5l4 4-11 11H5.5v-4.5l11-10.5z"/>
                        </svg>
                      </Button>
                      <Button variant="ghost" size="xs" :class="c.pinned ? 'theme-primary' : 'text-base-content/60 hover:text-base-content'" :title="c.pinned ? (t('common.unpin') || '取消置顶') : (t('common.pin') || '置顶')" @click.stop="pin(c, !c.pinned)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <path :fill="c.pinned ? 'currentColor' : 'none'" d="M12 17l-5.878 3.09 1.123-6.545L2.49 8.91l6.561-.953L12 2l2.949 5.957 6.561.953-4.755 4.635 1.123 6.545z"/>
                        </svg>
                      </Button>
                      <Button variant="danger" size="xs" :title="t('common.delete') || '删除'" @click.stop="remove(c.id)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                          <path d="M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                        </svg>
                      </Button>
                    </div>
                  </div>
                  <div class="text-xs text-gray-500 truncate" v-if="c.model">{{ c.model }}</div>
                </div>
              </Button>
            </div>
          </div>

          <div class="space-y-2 rounded-2xl p-4 glass-ultraThin glass-tint-accent border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="flex items-center justify-between gap-3">
              <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('teacher.ai.memory') || '长期记忆' }}</h3>
              <glass-switch v-model="memory.enabled" size="sm" class="shrink-0" @update:modelValue="saveMemory" />
            </div>
            <glass-textarea v-model="memory.content" :rows="4" :disabled="!memory.enabled" :placeholder="t('teacher.ai.memoryPlaceholder') || '个性化偏好、口吻等...'" @blur="saveMemory" />
            <div class="space-y-1 text-xs text-gray-500 dark:text-gray-400">
              <div class="font-medium text-gray-600 dark:text-gray-300">{{ t('teacher.ai.memoryTips.title') || '推荐写法' }}</div>
              <ul class="list-disc pl-4 space-y-1">
                <li>{{ t('teacher.ai.memoryTips.tip1') || '我希望你用中文回答，语气简洁、分点。' }}</li>
                <li>{{ t('teacher.ai.memoryTips.tip2') || '我当前的学习目标是：____；我更偏好：示例/步骤/对比表。' }}</li>
                <li>{{ t('teacher.ai.memoryTips.tip3') || '当我上传图片/文档时，请先总结要点，再给出建议。' }}</li>
              </ul>
            </div>
          </div>

          <div class="space-y-3 text-xs text-gray-600 dark:text-gray-300 rounded-2xl p-4 glass-ultraThin glass-tint-success border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="font-semibold mb-1">{{ t('teacher.ai.modelsInfo.title') || '模型说明' }}</div>
            <ul class="list-disc pl-4 space-y-1">
              <li>{{ t('teacher.ai.modelsInfo.gemini') }}</li>
              <li>{{ t('teacher.ai.modelsInfo.glm46') }}</li>
              <li>{{ t('teacher.ai.modelsInfo.glm45') }}</li>
            </ul>
          </div>
        </aside>

        <section class="md:col-span-8 lg:col-span-8 xl:col-span-9 glass-thick glass-interactive glass-tint-primary rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden" v-glass="{ strength: 'thick', interactive: true }">
          <div class="px-4 py-3 border-b border-white/25 dark:border-white/10 bg-transparent">
            <div class="flex items-center justify-between">
              <div class="min-w-0">
                <div class="font-semibold text-gray-800 dark:text-gray-100 truncate">{{ activeTitle }}</div>
              </div>
              <div v-if="activeConversationId && currentMessages.length > 0 && activeModel" class="shrink-0 text-xs text-gray-500 dark:text-gray-400">
                {{ t('teacher.ai.model.label') || '模型' }}: <span class="font-medium">{{ activeModel }}</span>
              </div>
            </div>
          </div>

          <div class="h-[60vh] overflow-y-auto p-4 space-y-4 bg-transparent">
            <div v-for="m in currentMessages" :key="m.id" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
              <div class="max-w-[80%] rounded-2xl px-4 py-2 text-sm"
                   :class="m.role === 'user' ? 'glass-bubble glass-bubble-mine rounded-br-none text-white' : 'glass-bubble glass-bubble-peer rounded-bl-none text-base-content'">
                <div class="prose prose-sm dark:prose-invert max-w-none ai-md" v-html="renderMarkdown(m.content)"></div>
                <div v-if="m.attachments && m.attachments.length" class="mt-2 space-y-2">
                  <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('shared.voicePractice.attachments') || '附件' }}</div>
                  <div class="space-y-2">
                    <div v-for="fid in m.attachments" :key="fid" class="flex items-center gap-2">
                      <audio :src="buildAuthedStreamUrl(fid)" controls class="w-full" />
                      <Button size="xs" variant="ghost" class="shrink-0" @click="download(fid)">{{ t('common.download') || '下载' }}</Button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="border-t border-white/25 dark:border-white/10 p-3 bg-transparent space-y-3">
            <!-- Pending attachments (ChatGPT-like) -->
            <div v-if="pendingAttachments.length" class="flex flex-wrap gap-2">
              <div
                v-for="a in pendingAttachments"
                :key="a.id"
                class="group flex items-center gap-2 px-2.5 py-1.5 rounded-xl glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
                v-glass="{ strength: 'ultraThin', interactive: false }"
              >
                <!-- icon -->
                <div class="shrink-0">
                  <svg v-if="String(a.mimeType||'').startsWith('image/')" class="w-4 h-4 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <path d="M21 15l-4.5-4.5L9 18"/>
                  </svg>
                  <svg v-else class="w-4 h-4 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <path d="M14 2v6h6"/>
                    <path d="M8 13h8"/>
                    <path d="M8 17h8"/>
                  </svg>
                </div>
                <div class="text-xs text-gray-700 dark:text-gray-200 max-w-[180px] truncate">
                  {{ a.name || ('#' + a.id) }}
                </div>
                <Button
                  size="xs"
                  variant="ghost"
                  class="shrink-0 opacity-70 group-hover:opacity-100"
                  :title="t('common.delete') || '删除'"
                  @click.prevent="removePending(a.id)"
                >
                  <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 6L6 18"/>
                    <path d="M6 6l12 12"/>
                  </svg>
                </Button>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <glass-textarea
                v-model="draft"
                @keydown.enter.exact.prevent="send"
                :rows="3"
                :placeholder="t('teacher.ai.placeholder') || '输入问题...'"
                class="flex-1"
                :disabled="sending"
              />
              <div v-if="showUpload" class="flex items-center gap-2">
                <input ref="fileInput" type="file" class="hidden" accept="image/*,.pdf,.doc,.docx,.txt" multiple @change="handlePickFiles" />
                <Button variant="ghost" size="sm" :title="t('teacher.ai.uploadTitle') || (t('common.uploadImage') || '上传')" @click.prevent="triggerPick">
                  <svg class="w-5 h-5 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 3v12"/>
                    <path d="M7 8l5-5 5 5"/>
                    <path d="M4 17v3h16v-3"/>
                  </svg>
                </Button>
              </div>
              <Button
                variant="primary"
                class="shrink-0 whitespace-nowrap min-w-[96px]"
                :disabled="!canSend || sending"
                :loading="sending"
                @click="send"
              >
                <span v-if="sending">{{ t('teacher.ai.thinking') || 'AI 正在思考...' }}</span>
                <span v-else>{{ t('teacher.ai.send') || '发送' }}</span>
              </Button>
            </div>
          </div>
        </section>
      </div>
      <!-- Rename Modal -->
      <glass-modal
        v-if="showRename"
        :title="(t('teacher.ai.renameTitle') as string) || '重命名对话'"
        :backdropDark="false"
        blur="sm"
        clarity="default"
        maxWidth="max-w-[1040px]"
        heightVariant="normal"
        @close="showRename=false"
      >
        <glass-input v-model="renameTitle" class="mb-4" :placeholder="t('common.rename') || '请输入新的标题'" />
        <template #footer>
          <Button size="sm" variant="secondary" @click="showRename=false">{{ t('common.cancel') || '取消' }}</Button>
          <Button size="sm" variant="primary" @click="confirmRename">{{ t('teacher.courses.actions.save') || '保存' }}</Button>
        </template>
      </glass-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useAIStore } from '@/stores/ai'
import { useI18n } from 'vue-i18n'
import { fileApi } from '@/api/file.api'
import { aiApi } from '@/api/ai.api'
import Button from '@/components/ui/Button.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import Card from '@/components/ui/Card.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { useAuthStore } from '@/stores/auth'


const { t } = useI18n()
const router = useRouter()
const ai = useAIStore()
const auth = useAuthStore()
const q = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
// 采用 GlassTextarea 自适应，不再手动高度控制

const conversations = computed(() => {
  const list = [...ai.conversations]
  const toTs = (s?: string) => s ? new Date(s).getTime() : 0
  return list.sort((a, b) => {
    if (!!a.pinned !== !!b.pinned) return a.pinned ? -1 : 1
    if (a.pinned && b.pinned) return toTs(b.lastMessageAt) - toTs(a.lastMessageAt)
    return toTs(b.lastMessageAt) - toTs(a.lastMessageAt)
  })
})
const activeConversationId = computed({ get: () => ai.activeConversationId, set: v => (ai.activeConversationId = v as any) })
const model = computed({ get: () => ai.model, set: v => (ai.model = v as any) })
const userRole = computed(() => auth.userRole)
const modelOptions = computed(() => {
  const base = [
    { label: 'GLM-4.6', value: 'glm-4.6' },
    { label: 'GLM-4.5 Air', value: 'glm-4.5-air' }
  ]
  // 学生也允许使用 Gemini（配额由后端控制）
  return [
    { label: 'Gemini 2.5 Pro', value: 'google/gemini-2.5-pro' },
    ...base
  ]
})

watchEffect(() => {
  const allowed = modelOptions.value.map(o => o.value)
  if (allowed.length && !allowed.includes(model.value as any)) {
    model.value = modelOptions.value[0].value as any
  }
})
const currentMessages = computed(() => ai.messagesByConvId[String(activeConversationId.value || '')] || [])
const activeModel = computed(() => {
  const c = conversations.value.find(c => c.id === activeConversationId.value)
  return c?.model || null
})
const showUpload = computed(() => {
  const m = String(activeModel.value || model.value || '').toLowerCase()
  return m.startsWith('google/')
})
const activeTitle = computed(() => {
  const c = conversations.value.find(c => c.id === activeConversationId.value)
  return c?.title || (t('teacher.ai.untitled') || '未命名会话')
})
const newDraft = ref('')
const draft = computed({
  get: () => {
    if (activeConversationId.value) return ai.draftsByConvId[String(activeConversationId.value)] || ''
    return newDraft.value
  },
  set: (v: string) => {
    if (activeConversationId.value) ai.saveDraft(activeConversationId.value, v)
    else newDraft.value = v
  }
})
const memory = ref<{ enabled: boolean; content: string }>({ enabled: true, content: '' })
const saveMemory = async () => {
  try {
    await aiApi.updateMemory({ enabled: !!memory.value.enabled, content: memory.value.content || '' })
  } catch {}
}
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
const pendingAttachments = computed(() => {
  const id = activeConversationId.value
  if (!id) return []
  return ai.pendingAttachmentsMeta[String(id)] || []
})
const sending = ref(false)
// 已移除 JSON 输出与批改 Prompt 开关

watch(showUpload, (enabled) => {
  // 若切换到不支持附件的模型，清空已选附件，避免“已选 N 项”残留
  if (!enabled && activeConversationId.value) {
    ai.clearPendingAttachments(activeConversationId.value)
    if (fileInput.value) fileInput.value.value = ''
  }
})

const removePending = (fileId: number) => {
  try {
    const id = activeConversationId.value
    if (!id) return
    ai.removePendingAttachment(id, fileId)
  } catch {}
}

const search = async () => { await ai.fetchConversations({ q: q.value }) }
// 输入自动搜索（防抖）
let searchTimer: any = null
watch(q, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { search() }, 300)
})
const newConv = async () => { await ai.createConversation({ title: '新对话', model: model.value }) }
const open = async (id: number) => { await ai.openConversation(id) }
const pin = async (c: any, pinned: boolean) => { await ai.pinConversation(c.id, pinned) }
const remove = async (id: number) => { await ai.removeConversation(id) }
const renameTitle = ref('')
const showRename = ref(false)
const renameTargetId = ref<number | null>(null)
const rename = (c: any) => { renameTargetId.value = c.id; renameTitle.value = c.title || ''; showRename.value = true }
const confirmRename = async () => { if (renameTargetId.value && renameTitle.value.trim()) await ai.renameConversation(renameTargetId.value, renameTitle.value.trim()); showRename.value = false }

const send = async () => {
  const text = (draft.value || '').trim()
  if (!text) return
  try {
    sending.value = true
    await ai.sendMessage({ content: text })
  } finally {
    sending.value = false
  }
  if (activeConversationId.value) ai.saveDraft(activeConversationId.value, '')
  else newDraft.value = ''
}

const goVoicePractice = () => {
  try {
    const role = userRole.value
    if (role === 'TEACHER') return router.push('/teacher/assistant/voice')
    return router.push('/student/assistant/voice')
  } catch {}
}

const buildAuthedStreamUrl = (fileId: number) => {
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  const base = `/files/${encodeURIComponent(String(fileId))}/stream`
  const url = token ? `${base}?token=${encodeURIComponent(String(token))}` : base
  return fileApi.buildFileUrl(url)
}

const download = async (fileId: number) => {
  try {
    await fileApi.downloadFile(fileId)
  } catch {}
}
const triggerPick = () => fileInput.value?.click()
const handlePickFiles = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0) return
  let convId = activeConversationId.value || null
  if (!convId) {
    const conv = await ai.ensureDraftConversation(draft.value?.slice(0, 20) || '新对话')
    convId = conv?.id || null
  }
  if (!convId) return
  for (const f of Array.from(files)) {
    try {
      const info: any = await fileApi.uploadFile(f as File, { purpose: 'ai_chat', relatedId: String(convId) })
      const fid = info?.id || info?.fileId
      if (fid) ai.addPendingAttachment(convId, Number(fid), { name: (f as File)?.name, mimeType: (f as File)?.type })
    } catch {}
  }
  if (fileInput.value) fileInput.value.value = ''
}

onMounted(async () => {
  await ai.fetchConversations({ page: 1, size: 50 })
  try {
    const res: any = await aiApi.getMemory()
    const data = res?.data ?? res
    if (data) {
      memory.value = { enabled: !!data.enabled, content: String(data.content || '') }
    }
  } catch {}
})

const onTextareaInput = () => {}

function escapeHtml(input = '') {
  return String(input)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderMarkdown(raw: string): string {
  const text = escapeHtml(raw || '')

  // 使用占位符保护块元素（代码块/表格）
  const blocks: string[] = []
  const keep = (html: string) => { const i = blocks.length; blocks.push(html); return `@@BLOCK_${i}@@` }

  // 1) 代码块 ```
  let work = text.replace(/```([\s\S]*?)```/g, (_m, p1) => keep(`<pre><code>${p1}</code></pre>`))

  // 2) 表格（简单 GFM）
  const lines = work.split('\n')
  const out: string[] = []
  const isSep = (s: string) => /^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(s)
  const parseRow = (s: string) => s.replace(/^\s*\||\|\s*$/g, '').split('|').map(c => c.trim())
  for (let i = 0; i < lines.length; ) {
    const l = lines[i]
    const n = lines[i + 1]
    if (l && l.includes('|') && n && isSep(n)) {
      const headers = parseRow(l)
      const aligns = parseRow(n).map(seg => {
        const left = /^:\-+/.test(seg)
        const right = /\-+:$/.test(seg)
        if (left && right) return 'center'
        if (right) return 'right'
        return 'left'
      })
      const rows: string[][] = []
      let j = i + 2
      while (j < lines.length && lines[j].includes('|') && !/^\s*$/.test(lines[j])) {
        rows.push(parseRow(lines[j]))
        j++
      }
      const thead = `<thead><tr>${headers.map((h, idx) => `<th style=\"text-align:${aligns[idx] || 'left'}\">${h}</th>`).join('')}</tr></thead>`
      const tbody = `<tbody>${rows.map(r => `<tr>${r.map((c, idx) => `<td style=\"text-align:${aligns[idx] || 'left'}\">${c}</td>`).join('')}</tr>`).join('')}</tbody>`
      out.push(keep(`<table class=\"ai-table\">${thead}${tbody}</table>`))
      i = j
      continue
    }
    out.push(l)
    i++
  }
  work = out.join('\n')

  // 3) 标题 (#, ##, ... ######)
  work = work.replace(/^([#]{1,6})\s+(.+)$/gm, (_m, h, t) => {
    const level = Math.max(1, Math.min(6, String(h).length))
    return `<h${level}>${t}</h${level}>`
  })

  // 4) 分割线 --- *** ___
  work = work.replace(/^\s{0,3}(-{3,}|\*{3,}|_{3,})\s*$/gm, '<hr/>')

  // 5) 行内格式
  work = work.replace(/`([^`]+?)`/g, (_m, p1) => `<code>${p1}</code>`)
  work = work.replace(/\*\*([^*]+?)\*\*/g, '<strong>$1</strong>').replace(/__([^_]+?)__/g, '<strong>$1</strong>')
  work = work.replace(/(?<!\*)\*([^*]+?)\*(?!\*)/g, '<em>$1</em>').replace(/(?<!_)_([^_]+?)_(?!_)/g, '<em>$1</em>')
  work = work.replace(/\[([^\]]+?)\]\((https?:[^)\s]+)\)/g, '<a href=\"$2\" target=\"_blank\" rel=\"noopener noreferrer\">$1</a>')

  // 6) 换行
  work = work.replace(/\n/g, '<br/>')

  // 7) 还原块
  work = work.replace(/@@BLOCK_(\d+)@@/g, (_m, idx) => blocks[Number(idx)] || '')
  return work
}
</script>

<style lang="postcss" scoped>
.card {
  @apply bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700;
}
.input {
  @apply w-full rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-base-content px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500;
}
/* 会话条目选中：去掉边框/描边，仅通过加深背景色表现 */
.conv-selected {
  background-color: color-mix(in oklab, var(--color-base-200) 36%, transparent);
  outline: none;
  box-shadow: none;
  border-color: transparent !important; /* 覆盖 divide-y 颜色 */
  border-top-width: 0 !important;      /* 移除顶部分隔线，看起来像边框 */
}
/* 主题主色文本（避免硬编码蓝色） */
.theme-primary { color: rgb(var(--color-primary)); }
/* AI markdown 渲染微调：代码块/行内代码/链接随主题 */
.ai-md :is(pre, code) { background-color: color-mix(in oklab, var(--color-base-200) 55%, transparent); padding: 0.125rem 0.25rem; border-radius: 0.375rem; }
.ai-md pre code { display: block; padding: 0.5rem 0.75rem; }
.ai-md a { color: var(--color-primary); text-decoration: underline; }
/* 表格主题化样式 */
.ai-md table.ai-table { width: 100%; border-collapse: collapse; margin: 0.25rem 0; }
.ai-md table.ai-table th, .ai-md table.ai-table td { border: 1px solid var(--glass-border-color); padding: 0.5rem 0.75rem; }
.ai-md table.ai-table thead th { background-color: color-mix(in oklab, var(--color-base-200) 45%, transparent); }
.dark .ai-md table.ai-table thead th { background-color: color-mix(in oklab, var(--color-base-200) 25%, transparent); }
</style>



