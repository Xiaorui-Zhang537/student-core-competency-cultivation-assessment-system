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
        <!-- 左侧面板 -->
        <aside class="md:col-span-4 lg:col-span-4 xl:col-span-3 filter-container p-4 space-y-4 rounded-xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
          <!-- 会话列表 -->
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
                          <path d="M12 20h9"/><path d="M16.5 3.5l4 4-11 11H5.5v-4.5l11-10.5z"/>
                        </svg>
                      </Button>
                      <Button variant="ghost" size="xs" :class="c.pinned ? 'theme-primary' : 'text-base-content/60 hover:text-base-content'" :title="c.pinned ? (t('common.unpin') || '取消置顶') : (t('common.pin') || '置顶')" @click.stop="pin(c, !c.pinned)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <path :fill="c.pinned ? 'currentColor' : 'none'" d="M12 17l-5.878 3.09 1.123-6.545L2.49 8.91l6.561-.953L12 2l2.949 5.957 6.561.953-4.755 4.635 1.123 6.545z"/>
                        </svg>
                      </Button>
                      <Button variant="danger" size="xs" :title="t('common.delete') || '删除'" @click.stop="remove(c.id)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                        </svg>
                      </Button>
                    </div>
                  </div>
                  <div class="text-xs text-gray-500 truncate" v-if="c.model">{{ c.model }}</div>
                </div>
              </Button>
            </div>
          </div>

          <!-- 长期记忆 -->
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

          <!-- 模型说明 -->
          <div class="space-y-3 text-xs text-gray-600 dark:text-gray-300 rounded-2xl p-4 glass-ultraThin glass-tint-success border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="font-semibold mb-1">{{ t('teacher.ai.modelsInfo.title') || '模型说明' }}</div>
            <ul class="list-disc pl-4 space-y-1">
              <li>{{ t('teacher.ai.modelsInfo.gemini') }}</li>
              <li>{{ t('teacher.ai.modelsInfo.glm46') }}</li>
              <li>{{ t('teacher.ai.modelsInfo.glm45') }}</li>
            </ul>
          </div>
        </aside>

        <!-- 右侧聊天区 -->
        <section class="md:col-span-8 lg:col-span-8 xl:col-span-9 glass-thick glass-interactive glass-tint-primary rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden flex flex-col" v-glass="{ strength: 'thick', interactive: true }">
          <!-- 顶栏 -->
          <div class="px-4 py-3 border-b border-white/25 dark:border-white/10 bg-transparent shrink-0">
            <div class="flex items-center justify-between">
              <div class="min-w-0">
                <div class="font-semibold text-gray-800 dark:text-gray-100 truncate">{{ activeTitle }}</div>
              </div>
              <div v-if="activeConversationId && currentMessages.length > 0 && activeModel" class="shrink-0 text-xs text-gray-500 dark:text-gray-400">
                {{ t('teacher.ai.model.label') || '模型' }}: <span class="font-medium">{{ activeModel }}</span>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="chatContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-transparent">
            <template v-for="(m, idx) in currentMessages" :key="m.id">
              <div class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
                <div class="max-w-[80%] rounded-2xl px-4 py-2 text-sm group relative"
                     :class="m.role === 'user' ? 'glass-bubble glass-bubble-mine rounded-br-none text-white' : 'glass-bubble glass-bubble-peer rounded-bl-none text-base-content'">

                  <!-- AI 消息内容 -->
                  <div class="prose prose-sm dark:prose-invert max-w-none ai-md" v-html="renderMd(m.content)"></div>

                  <!-- 流式光标 -->
                  <span v-if="m.streaming" class="inline-block w-2 h-4 bg-current animate-pulse ml-0.5 align-text-bottom rounded-sm"></span>

                  <!-- 附件 -->
                  <div v-if="m.attachments && m.attachments.length" class="mt-2 space-y-2">
                    <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('shared.voicePractice.attachments') || '附件' }}</div>
                    <div class="space-y-2">
                      <div v-for="fid in m.attachments" :key="fid" class="flex items-center gap-2">
                        <audio :src="buildAuthedStreamUrl(fid)" controls class="w-full" />
                        <Button size="xs" variant="ghost" class="shrink-0" @click="download(fid)">{{ t('common.download') || '下载' }}</Button>
                      </div>
                    </div>
                  </div>

                  <!-- AI 消息操作栏 -->
                  <div v-if="m.role === 'assistant' && !m.streaming && m.content"
                       class="flex items-center gap-1 mt-2 pt-1.5 border-t border-white/10 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button class="ai-action-btn" :title="t('common.copy') || '复制'" @click="copyMessage(m.content)">
                      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                      </svg>
                    </button>
                    <button v-if="idx === currentMessages.length - 1" class="ai-action-btn" :title="t('teacher.ai.regenerate') || '重新生成'" @click="regenerate">
                      <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="1 4 1 10 7 10"/><polyline points="23 20 23 14 17 14"/><path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </template>

            <!-- 空状态 -->
            <div v-if="!currentMessages.length && !sending && !ai.streaming" class="flex flex-col items-center justify-center h-full text-gray-400 dark:text-gray-500 space-y-3 py-12">
              <svg class="w-12 h-12 opacity-40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <p class="text-sm">{{ t('teacher.ai.emptyHint') || '输入问题开始对话' }}</p>
            </div>
          </div>

          <!-- 底部输入区 -->
          <div class="border-t border-white/25 dark:border-white/10 p-3 bg-transparent space-y-3 shrink-0">
            <!-- 待发附件 -->
            <div v-if="pendingAttachments.length" class="flex flex-wrap gap-2">
              <div v-for="a in pendingAttachments" :key="a.id"
                class="group flex items-center gap-2 px-2.5 py-1.5 rounded-xl glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
                v-glass="{ strength: 'ultraThin', interactive: false }">
                <div class="shrink-0">
                  <svg v-if="String(a.mimeType||'').startsWith('image/')" class="w-4 h-4 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-4.5-4.5L9 18"/>
                  </svg>
                  <svg v-else class="w-4 h-4 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8"/><path d="M8 17h8"/>
                  </svg>
                </div>
                <div class="text-xs text-gray-700 dark:text-gray-200 max-w-[180px] truncate">{{ a.name || ('#' + a.id) }}</div>
                <Button size="xs" variant="ghost" class="shrink-0 opacity-70 group-hover:opacity-100" :title="t('common.delete') || '删除'" @click.prevent="removePending(a.id)">
                  <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 6L6 18"/><path d="M6 6l12 12"/>
                  </svg>
                </Button>
              </div>
            </div>

            <!-- 输入行 -->
            <div class="flex items-end gap-3">
              <glass-textarea
                v-model="draft"
                @keydown.enter.exact.prevent="send"
                :rows="2"
                :placeholder="t('teacher.ai.placeholder') || '输入问题... (Enter 发送, Shift+Enter 换行)'"
                class="flex-1"
                :disabled="sending || ai.streaming"
              />
              <div v-if="showUpload" class="flex items-center gap-2">
                <input ref="fileInput" type="file" class="hidden" accept="image/*,.pdf,.doc,.docx,.txt" multiple @change="handlePickFiles" />
                <Button variant="ghost" size="sm" :title="t('teacher.ai.uploadTitle') || '上传'" @click.prevent="triggerPick">
                  <svg class="w-5 h-5 text-gray-700 dark:text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 3v12"/><path d="M7 8l5-5 5 5"/><path d="M4 17v3h16v-3"/>
                  </svg>
                </Button>
              </div>

              <!-- 停止生成 / 发送 -->
              <Button v-if="ai.streaming" variant="danger" class="shrink-0 whitespace-nowrap min-w-[96px]" @click="stopGeneration">
                <svg class="w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12" rx="2"/></svg>
                {{ t('teacher.ai.stop') || '停止生成' }}
              </Button>
              <Button v-else variant="primary" class="shrink-0 whitespace-nowrap min-w-[96px]" :disabled="!canSend || sending" :loading="sending" @click="send">
                <span v-if="sending">{{ t('teacher.ai.thinking') || 'AI 正在思考...' }}</span>
                <span v-else>{{ t('teacher.ai.send') || '发送' }}</span>
              </Button>
            </div>
            <!-- 字数统计 -->
            <div class="text-right text-xs text-gray-400 dark:text-gray-500" v-if="draft">
              {{ draft.length }} {{ t('teacher.ai.chars') || '字' }}
            </div>
          </div>
        </section>
      </div>

      <!-- 重命名弹窗 -->
      <glass-modal v-if="showRename" :title="(t('teacher.ai.renameTitle') as string) || '重命名对话'" :backdropDark="false" blur="sm" clarity="default" maxWidth="max-w-[1040px]" heightVariant="normal" @close="showRename=false">
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
import { computed, onMounted, onUnmounted, ref, watch, watchEffect, nextTick } from 'vue'
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
import GlassModal from '@/components/ui/GlassModal.vue'
import { useAuthStore } from '@/stores/auth'
import { renderMarkdown, installCodeCopyHandler } from '@/shared/utils/markdownRenderer'

const { t } = useI18n()
const router = useRouter()
const ai = useAIStore()
const auth = useAuthStore()
const q = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const chatContainer = ref<HTMLElement | null>(null)

// 安装代码块复制处理器
onMounted(() => { installCodeCopyHandler() })

// ---- 会话列表 ----
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
  return [
    { label: 'Gemini 2.5 Pro', value: 'google/gemini-2.5-pro' },
    { label: 'GLM-4.6', value: 'glm-4.6' },
    { label: 'GLM-4.5 Air', value: 'glm-4.5-air' },
  ]
})

watchEffect(() => {
  const allowed = modelOptions.value.map(o => o.value)
  if (allowed.length && !allowed.includes(model.value as any)) {
    model.value = modelOptions.value[0].value as any
  }
})

const currentMessages = computed(() => ai.messagesByConvId[String(activeConversationId.value || '')] || [])
const activeModel = computed(() => conversations.value.find(c => c.id === activeConversationId.value)?.model || null)
const showUpload = computed(() => String(activeModel.value || model.value || '').toLowerCase().startsWith('google/'))
const activeTitle = computed(() => conversations.value.find(c => c.id === activeConversationId.value)?.title || (t('teacher.ai.untitled') || '未命名会话'))

// ---- 草稿 ----
const newDraft = ref('')
const draft = computed({
  get: () => activeConversationId.value ? (ai.draftsByConvId[String(activeConversationId.value)] || '') : newDraft.value,
  set: (v: string) => { activeConversationId.value ? ai.saveDraft(activeConversationId.value, v) : (newDraft.value = v) }
})

// ---- 记忆 ----
const memory = ref<{ enabled: boolean; content: string }>({ enabled: true, content: '' })
const saveMemory = async () => {
  try { await aiApi.updateMemory({ enabled: !!memory.value.enabled, content: memory.value.content || '' }) } catch {}
}

// ---- 发送相关 ----
const canSend = computed(() => typeof draft.value === 'string' && draft.value.trim().length > 0)
const pendingAttachments = computed(() => {
  const id = activeConversationId.value
  if (!id) return []
  return ai.pendingAttachmentsMeta[String(id)] || []
})
const sending = ref(false)

// ---- Markdown 渲染（使用共享渲染器） ----
const renderMd = (raw: string): string => renderMarkdown(raw)

// ---- 附件切换清理 ----
watch(showUpload, (enabled) => {
  if (!enabled && activeConversationId.value) {
    ai.clearPendingAttachments(activeConversationId.value)
    if (fileInput.value) fileInput.value.value = ''
  }
})

const removePending = (fileId: number) => {
  const id = activeConversationId.value
  if (id) ai.removePendingAttachment(id, fileId)
}

// ---- 搜索 ----
let searchTimer: any = null
watch(q, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { ai.fetchConversations({ q: q.value }) }, 300)
})

// ---- 会话操作 ----
const newConv = async () => { await ai.createConversation({ title: '新对话', model: model.value }) }
const open = async (id: number) => { await ai.openConversation(id) }
const pin = async (c: any, pinned: boolean) => { await ai.pinConversation(c.id, pinned) }
const remove = async (id: number) => { await ai.removeConversation(id) }
const renameTitle = ref('')
const showRename = ref(false)
const renameTargetId = ref<number | null>(null)
const rename = (c: any) => { renameTargetId.value = c.id; renameTitle.value = c.title || ''; showRename.value = true }
const confirmRename = async () => {
  if (renameTargetId.value && renameTitle.value.trim()) await ai.renameConversation(renameTargetId.value, renameTitle.value.trim())
  showRename.value = false
}

// ---- 自动滚动 ----
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 监听消息变化自动滚动
watch(() => currentMessages.value.length, scrollToBottom)
watch(() => currentMessages.value[currentMessages.value.length - 1]?.content, scrollToBottom)

// ---- 发送（流式） ----
const send = async () => {
  const text = (draft.value || '').trim()
  if (!text || ai.streaming) return
  try {
    sending.value = true
    draft.value = ''
    newDraft.value = ''
    await ai.sendMessageStream({ content: text })
  } finally {
    sending.value = false
  }
}

// ---- 停止生成 ----
const stopGeneration = () => {
  ai.abortStream()
}

// ---- 复制消息 ----
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
  } catch {}
}

// ---- 重新生成 ----
const regenerate = async () => {
  const msgs = currentMessages.value
  if (msgs.length < 2) return
  // 找最后一条用户消息
  let lastUserContent = ''
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'user') {
      lastUserContent = msgs[i].content
      break
    }
  }
  if (!lastUserContent) return
  // 移除最后一条 assistant 消息
  const key = String(activeConversationId.value)
  const arr = ai.messagesByConvId[key]
  if (arr && arr.length > 0 && arr[arr.length - 1].role === 'assistant') {
    arr.pop()
  }
  // 重新发送
  sending.value = true
  try {
    await ai.sendMessageStream({ content: lastUserContent })
  } finally {
    sending.value = false
  }
}

// ---- 导航 ----
const goVoicePractice = () => {
  const role = userRole.value
  if (role === 'TEACHER') router.push('/teacher/assistant/voice')
  else router.push('/student/assistant/voice')
}

// ---- 文件处理 ----
const buildAuthedStreamUrl = (fileId: number) => {
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  const base = `/files/${encodeURIComponent(String(fileId))}/stream`
  const url = token ? `${base}?token=${encodeURIComponent(String(token))}` : base
  return fileApi.buildFileUrl(url)
}
const download = async (fileId: number) => { try { await fileApi.downloadFile(fileId) } catch {} }
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

// ---- 初始化 ----
onMounted(async () => {
  await ai.fetchConversations({ page: 1, size: 50 })
  try {
    const res: any = await aiApi.getMemory()
    const data = res?.data ?? res
    if (data) memory.value = { enabled: !!data.enabled, content: String(data.content || '') }
  } catch {}
})

onUnmounted(() => {
  // 组件销毁时中断流式请求
  ai.abortStream()
})
</script>

<style lang="postcss" scoped>
.conv-selected {
  background-color: color-mix(in oklab, var(--color-base-200) 36%, transparent);
  outline: none;
  box-shadow: none;
  border-color: transparent !important;
  border-top-width: 0 !important;
}
.theme-primary { color: rgb(var(--color-primary)); }

/* AI Markdown 样式 */
.ai-md :deep(pre) { margin: 0.5rem 0; border-radius: 0.5rem; overflow-x: auto; }
.ai-md :deep(code) { background-color: color-mix(in oklab, var(--color-base-200) 55%, transparent); padding: 0.125rem 0.25rem; border-radius: 0.375rem; font-size: 0.85em; }
.ai-md :deep(pre code) { display: block; padding: 0.75rem 1rem; background: transparent; }
.ai-md :deep(a) { color: var(--color-primary); text-decoration: underline; }
.ai-md :deep(table.ai-table) { width: 100%; border-collapse: collapse; margin: 0.5rem 0; }
.ai-md :deep(table.ai-table th),
.ai-md :deep(table.ai-table td) { border: 1px solid var(--glass-border-color, rgba(128,128,128,0.3)); padding: 0.5rem 0.75rem; }
.ai-md :deep(table.ai-table thead th) { background-color: color-mix(in oklab, var(--color-base-200) 45%, transparent); }
.dark .ai-md :deep(table.ai-table thead th) { background-color: color-mix(in oklab, var(--color-base-200) 25%, transparent); }
.ai-md :deep(ul), .ai-md :deep(ol) { padding-left: 1.5em; margin: 0.25rem 0; }
.ai-md :deep(li) { margin: 0.15rem 0; }
.ai-md :deep(blockquote) { border-left: 3px solid var(--color-primary, #3b82f6); padding-left: 0.75rem; margin: 0.5rem 0; opacity: 0.85; }
.ai-md :deep(h1), .ai-md :deep(h2), .ai-md :deep(h3), .ai-md :deep(h4) { margin: 0.75rem 0 0.25rem; font-weight: 600; }
.ai-md :deep(hr) { border-color: var(--glass-border-color, rgba(128,128,128,0.2)); margin: 0.75rem 0; }

/* 代码块包装器 */
.ai-md :deep(.code-block-wrapper) {
  position: relative;
  margin: 0.5rem 0;
  border-radius: 0.5rem;
  overflow: hidden;
  background-color: color-mix(in oklab, var(--color-base-200) 40%, transparent);
}
.ai-md :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  color: var(--color-base-content, #666);
  opacity: 0.7;
  border-bottom: 1px solid color-mix(in oklab, var(--color-base-200) 30%, transparent);
}
.ai-md :deep(.code-lang-label) { font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; }
.ai-md :deep(.code-copy-btn) {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: all 0.15s;
  background: transparent;
  border: none;
  color: inherit;
  font-size: inherit;
}
.ai-md :deep(.code-copy-btn:hover) { opacity: 1; background: color-mix(in oklab, var(--color-base-200) 50%, transparent); }
.ai-md :deep(.code-copy-btn.copied) { color: #10b981; }

/* AI 消息操作按钮 */
.ai-action-btn {
  @apply flex items-center gap-1 px-1.5 py-1 rounded text-xs transition-colors;
  color: var(--color-base-content, #666);
  opacity: 0.6;
}
.ai-action-btn:hover { opacity: 1; background: color-mix(in oklab, var(--color-base-200) 40%, transparent); }
</style>
