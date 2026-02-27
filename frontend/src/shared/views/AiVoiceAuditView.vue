<template>
  <div class="relative">
    <div class="min-h-screen p-4 md:p-6">
      <div class="max-w-6xl mx-auto mb-4 md:mb-6">
        <breadcrumb class="mb-2" :items="breadcrumbItems" />
        <page-header :title="t('admin.student360.auditAiVoice') || 'AI/口语审计'" :subtitle="subtitle">
          <template #actions>
            <Button variant="outline" @click="goBack">{{ t('common.back') || '返回' }}</Button>
          </template>
        </page-header>
      </div>

      <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
      <error-state
        v-else-if="error"
        class="mt-4"
        :title="String(t('common.error') || '加载失败')"
        :message="error"
        :actionText="String(t('common.retry') || '重试')"
        @action="reload"
      />

      <!-- 固定主区域高度：页面不滚动，内部区域滚动 -->
      <div v-else class="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-4 items-stretch h-[calc(100vh-320px)] overflow-hidden">
        <!-- 左侧面板：tab + 搜索 + 列表 -->
        <aside class="md:col-span-4 lg:col-span-4 xl:col-span-3 filter-container p-4 space-y-4 rounded-xl glass-tint-secondary flex flex-col h-full overflow-hidden" v-glass="{ strength: 'thin', interactive: false }">
          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10 flex flex-col h-full min-h-0 overflow-hidden" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="flex items-center justify-between gap-2 mb-3 flex-wrap">
              <segmented-pills :model-value="tab" :options="tabOptions" size="sm" variant="info" @update:modelValue="(v:any)=> tab = String(v) as any" />
            </div>

            <div class="flex items-center gap-2 mb-3">
              <div class="flex-1 min-w-0">
                <glass-search-input v-model="q" :placeholder="String(t('common.search') || '搜索')" size="sm" tint="info" />
              </div>
            </div>

            <div class="flex items-center justify-between gap-3 mb-2">
              <div class="text-sm font-semibold text-gray-700 dark:text-gray-200 truncate">{{ leftTitle }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400 shrink-0">{{ leftItems.length }}</div>
            </div>

            <div class="flex-1 min-h-0 overflow-hidden">
              <div class="divide-y divide-gray-100 dark:divide-gray-700 rounded-lg border border-gray-100 dark:border-gray-700 overflow-hidden h-full overflow-y-auto">
                <Button
                  v-for="x in leftItems"
                  :key="x.id"
                  variant="menu"
                  class="w-full px-2 py-3 text-left transition-colors rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 min-h-[68px] focus:outline-none focus:ring-0 focus-visible:ring-0 focus:ring-offset-0"
                  :class="selectedLeftId === String(x.id) ? 'conv-selected border-transparent ring-0 outline-none focus:ring-0 focus-visible:ring-0' : ''"
                  @click="selectLeft(String(x.id))"
                >
                  <div class="flex flex-col gap-1 w-full px-3">
                    <div class="flex items-center gap-2 w-full min-w-0">
                      <div class="font-medium truncate flex-1 min-w-0">{{ x.title || x.topic || x.name || `#${x.id}` }}</div>
                      <div class="text-[11px] text-gray-500 dark:text-gray-400 shrink-0">#{{ x.id }}</div>
                    </div>
                    <div v-if="x.model" class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ x.model }}</div>
                    <div class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ x.createdAt || x.updatedAt || x.startedAt || '-' }}</div>
                  </div>
                </Button>
                <div v-if="leftItems.length === 0" class="p-4">
                  <empty-state :title="String(t('common.empty') || '暂无数据')" />
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- 右侧：聊天区（对齐 AI 助理/口语训练模板） -->
        <section class="md:col-span-8 lg:col-span-8 xl:col-span-9 glass-thick glass-interactive glass-tint-primary rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden flex flex-col h-full min-h-0" v-glass="{ strength: 'thick', interactive: true }">
          <!-- 顶栏 -->
          <div class="px-4 py-3 border-b border-white/25 dark:border-white/10 bg-transparent shrink-0">
            <div class="flex items-center justify-between gap-3 flex-wrap">
              <div class="min-w-0">
                <div class="font-semibold text-gray-800 dark:text-gray-100 truncate">{{ rightTitle }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400 truncate mt-0.5">
                  {{ rightSubtitle }}
                </div>
              </div>
              <div class="flex items-center gap-2">
                <div v-if="activeModel" class="hidden sm:block text-xs text-gray-500 dark:text-gray-400">
                  {{ t('common.model') || '模型' }}: <span class="font-medium">{{ activeModel }}</span>
                </div>
                <template v-if="tab==='ai'">
                  <Button size="sm" variant="info" :disabled="!selectedLeftId || messages.length===0" @click="exportAiAsJson">
                    <template #icon>
                      <CodeBracketIcon class="w-4 h-4" />
                    </template>
                    {{ t('admin.tools.exportJson') || '导出JSON' }}
                  </Button>
                  <Button size="sm" variant="secondary" :disabled="!selectedLeftId || messages.length===0" @click="exportAiAsMarkdown">
                    <template #icon>
                      <DocumentTextIcon class="w-4 h-4" />
                    </template>
                    {{ t('admin.tools.exportMd') || '导出MD' }}
                  </Button>
                </template>
                <template v-else>
                  <Button size="sm" variant="success" :disabled="turns.length===0" @click="exportVoiceTranscript">
                    <template #icon>
                      <DocumentTextIcon class="w-4 h-4" />
                    </template>
                    {{ t('admin.tools.exportTranscript') || '导出转写' }}
                  </Button>
                  <Button size="sm" variant="warning" :disabled="turns.length===0" @click="exportVoiceAudioZip">
                    <template #icon>
                      <ArchiveBoxArrowDownIcon class="w-4 h-4" />
                    </template>
                    {{ t('admin.tools.exportZip') || '导出ZIP' }}
                  </Button>
                </template>
              </div>
            </div>
          </div>

          <!-- 对话/回合列表 -->
          <div class="flex-1 min-h-0 overflow-y-auto p-4 space-y-4 bg-transparent">
            <!-- AI messages -->
            <div v-if="tab==='ai'" class="space-y-4">
              <template v-if="chatMessages.length">
                <div
                  v-for="m in chatMessages"
                  :key="m.key"
                  class="flex items-end gap-2"
                  :class="m.isUser ? 'justify-end' : 'justify-start'"
                >
                  <user-avatar v-if="!m.isUser" mode="img" :avatar="m.avatar" :size="28" class="shrink-0">
                    <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 ring-1 ring-white/20" />
                  </user-avatar>

                  <div class="max-w-[82%] min-w-0">
                    <div class="flex items-center gap-2 mb-1 text-xs text-subtle" :class="m.isUser ? 'justify-end' : 'justify-start'">
                      <span class="glass-badge glass-badge-xs text-[10px] leading-none px-[6px]">{{ m.roleLabel }}</span>
                      <span class="truncate max-w-[220px]">{{ m.name }}</span>
                      <span>·</span>
                      <span class="whitespace-nowrap">{{ m.timeText }}</span>
                      <Button size="xs" variant="ghost" class="ml-1" :title="t('common.copy') || '复制'" @click="copyText(m.text)">
                        {{ t('common.copy') || '复制' }}
                      </Button>
                    </div>
                    <div
                      class="rounded-2xl px-4 py-2 text-sm break-words whitespace-pre-wrap"
                      :class="m.isUser ? 'glass-bubble glass-bubble-mine rounded-br-none text-white' : 'glass-bubble glass-bubble-peer rounded-bl-none text-base-content'"
                    >
                      <div class="prose prose-sm dark:prose-invert max-w-none ai-md" v-html="renderMarkdown(m.text || '-')"></div>
                    </div>
                  </div>

                  <user-avatar v-if="m.isUser" mode="img" :avatar="m.avatar" :size="28" class="shrink-0">
                    <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 ring-1 ring-white/20" />
                  </user-avatar>
                </div>

                <div v-if="aiMsgHasMore" class="pt-2 flex justify-center">
                  <Button size="sm" variant="outline" :disabled="aiMsgLoadingMore" @click="loadMoreAiMessages">
                    {{ t('common.loadMore') || '加载更多' }}
                  </Button>
                </div>
              </template>
              <empty-state v-else :title="String(t('admin.student360.selectConversation') || '请选择会话')" />
            </div>

            <!-- Voice turns -->
            <div v-else class="space-y-6">
              <template v-if="turns.length">
                <div v-for="t0 in turns" :key="t0.id" class="space-y-3">
                  <!-- user bubble -->
                  <div class="flex items-end gap-2 justify-end">
                    <div class="max-w-[82%] min-w-0">
                      <div class="flex items-center gap-2 mb-1 text-xs text-subtle justify-end">
                        <span class="glass-badge glass-badge-xs text-[10px] leading-none px-[6px]">{{ t('admin.student360.voiceUser') || '学生' }}</span>
                        <span class="truncate max-w-[220px]">{{ userDisplayName }}</span>
                        <span>·</span>
                        <span class="whitespace-nowrap">{{ formatTimeText(t0.createdAt) }}</span>
                        <Button size="xs" variant="ghost" class="ml-1" :title="t('common.copy') || '复制'" @click="copyText(String(t0.userText || ''))">
                          {{ t('common.copy') || '复制' }}
                        </Button>
                      </div>
                      <div class="rounded-2xl px-4 py-2 text-sm glass-bubble glass-bubble-mine rounded-br-none text-white whitespace-pre-wrap break-words">
                        <div class="prose prose-sm dark:prose-invert max-w-none ai-md" v-html="renderMarkdown(String(t0.userText || '-'))"></div>
                      </div>
                      <audio v-if="t0.userAudioFileId" class="mt-2 w-full" controls :src="fileStreamUrl(t0.userAudioFileId)" />
                    </div>
                    <user-avatar mode="img" :avatar="userAvatar" :size="28" class="shrink-0">
                      <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 ring-1 ring-white/20" />
                    </user-avatar>
                  </div>

                  <!-- assistant bubble -->
                  <div class="flex items-end gap-2 justify-start">
                    <user-avatar mode="img" :avatar="aiAvatar" :size="28" class="shrink-0">
                      <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 ring-1 ring-white/20" />
                    </user-avatar>
                    <div class="max-w-[82%] min-w-0">
                      <div class="flex items-center gap-2 mb-1 text-xs text-subtle">
                        <span class="glass-badge glass-badge-xs text-[10px] leading-none px-[6px]">{{ t('admin.student360.voiceAssistant') || 'AI' }}</span>
                        <span class="truncate max-w-[220px]">{{ aiDisplayName }}</span>
                        <span>·</span>
                        <span class="whitespace-nowrap">{{ formatTimeText(t0.createdAt) }}</span>
                        <Button size="xs" variant="ghost" class="ml-1" :title="t('common.copy') || '复制'" @click="copyText(String(t0.assistantText || ''))">
                          {{ t('common.copy') || '复制' }}
                        </Button>
                      </div>
                      <div class="rounded-2xl px-4 py-2 text-sm glass-bubble glass-bubble-peer rounded-bl-none text-base-content whitespace-pre-wrap break-words">
                        <div class="prose prose-sm dark:prose-invert max-w-none ai-md" v-html="renderMarkdown(String(t0.assistantText || '-'))"></div>
                      </div>
                      <audio v-if="t0.assistantAudioFileId" class="mt-2 w-full" controls :src="fileStreamUrl(t0.assistantAudioFileId)" />
                    </div>
                  </div>
                </div>

                <div v-if="voiceTurnsHasMore" class="pt-2 flex justify-center">
                  <Button size="sm" variant="outline" :disabled="voiceTurnsLoadingMore" @click="loadMoreVoiceTurns">
                    {{ t('common.loadMore') || '加载更多' }}
                  </Button>
                </div>
              </template>
              <empty-state v-else :title="String(t('admin.student360.selectVoiceSession') || '请选择会话')" />
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 管理员 AI/口语审计视图：
 * - 按 userId 查看 AI 会话与消息
 * - 按 userId 查看口语会话与回合（含音频播放/导出）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import Breadcrumb from '@/components/ui/Breadcrumb.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { ArchiveBoxArrowDownIcon, CodeBracketIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'
import { adminApi } from '@/api/admin.api'
import { buildFileUrl, fileApi } from '@/api/file.api'
import { downloadBlobAsFile } from '@/utils/download'
import JSZip from 'jszip'
import { renderMarkdown, installCodeCopyHandler } from '@/shared/utils/markdownRenderer'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const userId = computed(() => String((route.params as any).userId || ''))
const userName = computed(() => String((route.query as any)?.name || ''))
const userAvatar = computed(() => String((route.query as any)?.avatar || ''))
const userDisplayName = computed(() => {
  const n = userName.value.trim()
  return n || `#${userId.value}`
})
const aiDisplayName = computed(() => 'AI')
const aiAvatar = computed(() => {
  // 内置 SVG：避免走 /api 静态资源导致 4xx/5xx
  const svg = `
  <svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">
    <defs>
      <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#7C3AED"/>
        <stop offset="0.55" stop-color="#3B82F6"/>
        <stop offset="1" stop-color="#22C55E"/>
      </linearGradient>
      <filter id="s" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="6" stdDeviation="6" flood-color="#000" flood-opacity="0.25"/>
      </filter>
    </defs>
    <circle cx="48" cy="48" r="42" fill="url(#g)" filter="url(#s)"/>
    <circle cx="48" cy="48" r="41" fill="none" stroke="rgba(255,255,255,0.35)" stroke-width="2"/>
    <path d="M28 54c6-10 34-10 40 0" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="3" stroke-linecap="round"/>
    <circle cx="36" cy="40" r="4" fill="rgba(255,255,255,0.9)"/>
    <circle cx="60" cy="40" r="4" fill="rgba(255,255,255,0.9)"/>
    <text x="48" y="76" text-anchor="middle" font-family="ui-sans-serif,system-ui,-apple-system" font-size="16" font-weight="700" fill="rgba(255,255,255,0.95)">AI</text>
  </svg>
  `.trim()
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
})
const subtitle = computed(() => {
  const n = userName.value.trim()
  return n ? `${n} · #${userId.value}` : `#${userId.value}`
})

const breadcrumbItems = computed(() => ([
  { label: String(t('admin.sidebar.people') || '数据中心'), to: '/admin/people' },
  { label: String(t('admin.student360.auditAiVoice') || 'AI/口语审计') },
]))
const tab = ref<'ai' | 'voice'>((String((route.query as any)?.tab || 'ai') as any) || 'ai')
const q = ref(String((route.query as any)?.q || ''))

const loading = ref(false)
const error = ref<string | null>(null)

const aiConversations = ref<any[]>([])
const messages = ref<any[]>([])
const voiceSessions = ref<any[]>([])
const turns = ref<any[]>([])

const selectedLeftId = ref<string>('')

// pagination: ai messages
const aiMsgPage = ref(1)
const aiMsgSize = ref(50)
const aiMsgHasMore = ref(false)
const aiMsgLoadingMore = ref(false)

// pagination: voice turns
const voiceTurnsPage = ref(1)
const voiceTurnsSize = ref(50)
const voiceTurnsHasMore = ref(false)
const voiceTurnsLoadingMore = ref(false)

const tabOptions = computed(() => ([
  { label: String(t('admin.student360.aiChat') || 'AI问答'), value: 'ai' },
  { label: String(t('admin.student360.voice') || '口语训练'), value: 'voice' },
]))

const leftTitle = computed(() => tab.value === 'ai' ? String(t('admin.student360.aiConversations') || '会话列表') : String(t('admin.student360.voiceSessions') || '会话列表'))
const rightTitle = computed(() => tab.value === 'ai' ? String(t('admin.student360.aiMessages') || '消息') : String(t('admin.student360.voiceTurns') || '回合'))

const leftItems = computed(() => tab.value === 'ai' ? aiConversations.value : voiceSessions.value)

const rightSubtitle = computed(() => {
  const sid = String(selectedLeftId.value || '').trim()
  if (!sid) return String(t('admin.student360.selectConversation') || '请选择会话')
  if (tab.value === 'ai') {
    const conv: any = (aiConversations.value || []).find((x: any) => String(x?.id) === sid)
    const title = String(conv?.title || conv?.topic || conv?.name || `#${sid}`)
    return `${userDisplayName.value} · ${title}`
  }
  const sess: any = (voiceSessions.value || []).find((x: any) => String(x?.id) === sid)
  const title = String(sess?.title || sess?.topic || sess?.name || `#${sid}`)
  return `${userDisplayName.value} · ${title}`
})

const activeModel = computed(() => {
  const sid = String(selectedLeftId.value || '').trim()
  if (!sid) return ''
  if (tab.value === 'ai') {
    const conv: any = (aiConversations.value || []).find((x: any) => String(x?.id) === sid)
    return String(conv?.model || '')
  }
  const sess: any = (voiceSessions.value || []).find((x: any) => String(x?.id) === sid)
  return String(sess?.model || '')
})

function fileUrl(fileId: string | number) {
  return buildFileUrl(`/files/${encodeURIComponent(String(fileId))}/download`)
}

function authToken(): string {
  try { return String(localStorage.getItem('token') || '') } catch { return '' }
}

function fileStreamUrl(fileId: string | number) {
  const fid = encodeURIComponent(String(fileId))
  const token = authToken()
  // audio/video 标签无法自动带 Authorization header，因此用 token query（与 VoicePracticeView 同策略）
  const path = token ? `/files/${fid}/stream?token=${encodeURIComponent(token)}` : `/files/${fid}/stream`
  return buildFileUrl(path)
}

function fileDownloadUrl(fileId: string | number) {
  const fid = encodeURIComponent(String(fileId))
  const token = authToken()
  const path = token ? `/files/${fid}/download?token=${encodeURIComponent(token)}` : `/files/${fid}/download`
  return buildFileUrl(path)
}

function formatTimeText(v?: string) {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleString()
}

function normalizeRole(raw: any): 'user' | 'assistant' | 'system' | 'unknown' {
  const r = String(raw || '').toLowerCase()
  if (r === 'user' || r === 'student') return 'user'
  if (r === 'assistant' || r === 'ai') return 'assistant'
  if (r === 'system') return 'system'
  return 'unknown'
}

const chatMessages = computed(() => {
  const list = Array.isArray(messages.value) ? messages.value : []
  return list.map((m: any, idx: number) => {
    const role = normalizeRole(m?.role ?? m?.senderRole ?? m?.sender_role ?? m?.type)
    const isUser = role === 'user'
    const text = String(m?.content ?? m?.text ?? m?.message ?? '').trim()
    const createdAt = String(m?.createdAt ?? m?.created_at ?? m?.time ?? '') || ''
    return {
      key: String(m?.id ?? `${idx}`),
      role,
      roleLabel: role === 'user' ? (t('admin.student360.voiceUser') || '学生') : role === 'assistant' ? 'AI' : (role === 'system' ? 'system' : 'unknown'),
      isUser,
      name: isUser ? userDisplayName.value : aiDisplayName.value,
      avatar: isUser ? userAvatar.value : aiAvatar.value,
      timeText: formatTimeText(createdAt),
      text,
      raw: m,
    }
  })
})

async function copyText(text: string) {
  const s = String(text || '')
  if (!s.trim()) return
  try {
    await navigator.clipboard.writeText(s)
  } catch {
    // ignore
  }
}

function goBack() {
  router.push('/admin/people')
}

async function reloadAiList() {
  const res: any = await adminApi.listAiConversations({ studentId: userId.value, q: q.value || undefined, page: 1, size: 50 })
  aiConversations.value = res?.items || []
  if (!selectedLeftId.value && aiConversations.value.length) selectedLeftId.value = String(aiConversations.value[0].id)
}

function deriveHasMore(res: any, loadedCount: number, page: number, size: number): boolean {
  const totalPages = Number(res?.totalPages ?? res?.data?.totalPages ?? 0)
  if (totalPages > 0) return page < totalPages
  const total = Number(res?.total ?? res?.data?.total ?? 0)
  if (total > 0) return page * size < total
  // fallback heuristic
  return loadedCount >= size
}

async function reloadAiMessages(reset = true) {
  if (!selectedLeftId.value) { messages.value = []; return }
  const nextPage = reset ? 1 : (aiMsgPage.value + 1)
  const res: any = await adminApi.listAiMessages(selectedLeftId.value, { studentId: userId.value, page: nextPage, size: aiMsgSize.value })
  const items = (res?.items || res?.data?.items || []) as any[]
  if (reset) messages.value = items
  else messages.value = [...(messages.value || []), ...items]
  aiMsgPage.value = nextPage
  aiMsgHasMore.value = deriveHasMore(res, items.length, aiMsgPage.value, aiMsgSize.value)
}

async function reloadVoiceList() {
  const res: any = await adminApi.listVoiceSessions({ studentId: userId.value, q: q.value || undefined, page: 1, size: 50 })
  voiceSessions.value = res?.items || res || []
  if (!selectedLeftId.value && voiceSessions.value.length) selectedLeftId.value = String(voiceSessions.value[0].id)
}

async function reloadVoiceTurns(reset = true) {
  if (!selectedLeftId.value) { turns.value = []; return }
  const nextPage = reset ? 1 : (voiceTurnsPage.value + 1)
  const res: any = await adminApi.listVoiceTurns(selectedLeftId.value, { studentId: userId.value, page: nextPage, size: voiceTurnsSize.value })
  const items = (res?.items || res?.data?.items || res || []) as any[]
  if (reset) turns.value = items
  else turns.value = [...(turns.value || []), ...items]
  voiceTurnsPage.value = nextPage
  voiceTurnsHasMore.value = deriveHasMore(res, Array.isArray(items) ? items.length : 0, voiceTurnsPage.value, voiceTurnsSize.value)
}

async function reload() {
  if (!userId.value) return
  loading.value = true
  error.value = null
  try {
    selectedLeftId.value = ''
    messages.value = []
    turns.value = []
    aiMsgPage.value = 1
    aiMsgHasMore.value = false
    voiceTurnsPage.value = 1
    voiceTurnsHasMore.value = false
    if (tab.value === 'ai') {
      await reloadAiList()
      await reloadAiMessages(true)
    } else {
      await reloadVoiceList()
      await reloadVoiceTurns(true)
    }
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    loading.value = false
  }
}

async function selectLeft(id: string) {
  selectedLeftId.value = id
  if (tab.value === 'ai') {
    aiMsgPage.value = 1
    aiMsgHasMore.value = false
    await reloadAiMessages(true)
  } else {
    voiceTurnsPage.value = 1
    voiceTurnsHasMore.value = false
    await reloadVoiceTurns(true)
  }
}

async function loadMoreAiMessages() {
  if (aiMsgLoadingMore.value || !aiMsgHasMore.value) return
  aiMsgLoadingMore.value = true
  try {
    await reloadAiMessages(false)
  } finally {
    aiMsgLoadingMore.value = false
  }
}

async function loadMoreVoiceTurns() {
  if (voiceTurnsLoadingMore.value || !voiceTurnsHasMore.value) return
  voiceTurnsLoadingMore.value = true
  try {
    await reloadVoiceTurns(false)
  } finally {
    voiceTurnsLoadingMore.value = false
  }
}

async function exportVoiceAudioZip() {
  try {
    const zip = new JSZip()
    const toCompactTime = (v?: string) => {
      if (!v) return 'unknown_time'
      const d = new Date(v)
      if (Number.isNaN(d.getTime())) return 'unknown_time'
      const yyyy = d.getFullYear()
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      const hh = String(d.getHours()).padStart(2, '0')
      const mi = String(d.getMinutes()).padStart(2, '0')
      const ss = String(d.getSeconds()).padStart(2, '0')
      return `${yyyy}${mm}${dd}_${hh}${mi}${ss}`
    }
    const safeName = (s: string) => String(s || '')
      .replace(/[\\/:*?"<>|]/g, '_')
      .replace(/\s+/g, ' ')
      .trim()
      .slice(0, 120)

    const list = Array.isArray(turns.value) ? turns.value : []
    const entries: Array<{ fid: string | number; filename: string }> = []
    for (let i = 0; i < list.length; i++) {
      const t0: any = list[i]
      const idx = String(i + 1).padStart(3, '0')
      const time = toCompactTime(t0?.createdAt)
      const qFid = t0?.userAudioFileId
      const aFid = t0?.assistantAudioFileId
      if (qFid) entries.push({ fid: qFid, filename: `turn_${idx}_Q_${time}_${String(qFid)}` })
      if (aFid) entries.push({ fid: aFid, filename: `turn_${idx}_A_${time}_${String(aFid)}` })
    }
    // 去重（同一个 fileId 只保留第一次命名），并限制总数
    const seen = new Set<string>()
    const limited = entries.filter(e => {
      const k = String(e.fid)
      if (seen.has(k)) return false
      seen.add(k)
      return true
    }).slice(0, 50)

    for (const e of limited) {
      try {
        let name = e.filename
        try {
          const info: any = await fileApi.getFileInfo(String(e.fid))
          const original = info?.originalName || info?.filename || info?.name
          // 保留原扩展名，便于播放器识别（但不依赖原始全名）
          if (original) {
            const ext = (() => {
              const s = String(original)
              const dot = s.lastIndexOf('.')
              return dot >= 0 ? s.slice(dot) : ''
            })()
            if (ext) name = `${name}${ext}`
          }
        } catch {}
        name = safeName(name)
        const url = fileDownloadUrl(e.fid)
        const res = await fetch(url, {
          method: 'GET',
        })
        if (!res.ok) continue
        const blob = await res.blob()
        zip.file(`audio/${name}`, blob)
      } catch {}
    }
    const out = await zip.generateAsync({ type: 'blob' })
    downloadBlobAsFile(out, `voice_audit_${userId.value}.zip`)
  } catch { /* ignore */ }
}

function exportAiAsJson() {
  try {
    const convId = String(selectedLeftId.value || '')
    if (!convId) return
    const payload = {
      exportedAt: new Date().toISOString(),
      studentId: String(userId.value || ''),
      conversationId: convId,
      conversation: (aiConversations.value || []).find((c: any) => String(c?.id) === convId) || null,
      messages: messages.value || [],
    }
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
    downloadBlobAsFile(blob, `ai_audit_${userId.value}_${convId}.json`)
  } catch { /* ignore */ }
}

function exportAiAsMarkdown() {
  try {
    const convId = String(selectedLeftId.value || '')
    if (!convId) return
    const title = (() => {
      const c: any = (aiConversations.value || []).find((x: any) => String(x?.id) === convId)
      return String(c?.title || c?.topic || c?.name || `#${convId}`)
    })()
    const lines: string[] = []
    lines.push(`# AI 审计导出`)
    lines.push(`- studentId: ${String(userId.value || '')}`)
    lines.push(`- conversationId: ${convId}`)
    lines.push(`- title: ${title}`)
    lines.push(`- exportedAt: ${new Date().toLocaleString()}`)
    lines.push(``)
    for (const m of chatMessages.value) {
      lines.push(`## ${m.roleLabel}（${m.name}）`)
      lines.push(`- time: ${m.timeText}`)
      lines.push(``)
      lines.push(String(m.text || '-'))
      lines.push(``)
    }
    const md = lines.join('\n')
    const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
    downloadBlobAsFile(blob, `ai_audit_${userId.value}_${convId}.md`)
  } catch { /* ignore */ }
}

function exportVoiceTranscript() {
  try {
    const sessionId = String(selectedLeftId.value || '')
    if (!sessionId) return
    const lines: string[] = []
    lines.push(`# 口语审计转写导出`)
    lines.push(`- studentId: ${String(userId.value || '')}`)
    lines.push(`- sessionId: ${sessionId}`)
    lines.push(`- exportedAt: ${new Date().toLocaleString()}`)
    lines.push(``)
    const list = Array.isArray(turns.value) ? turns.value : []
    for (const t0 of list) {
      lines.push(`## 回合 #${String(t0?.id ?? '-')}`)
      lines.push(`- time: ${formatTimeText(t0?.createdAt)}`)
      lines.push(``)
      lines.push(`**${String(t('admin.student360.voiceUser') || '学生')}**：`)
      lines.push(String(t0?.userText || '-'))
      lines.push(``)
      lines.push(`**${String(t('admin.student360.voiceAssistant') || 'AI')}**：`)
      lines.push(String(t0?.assistantText || '-'))
      lines.push(``)
    }
    const md = lines.join('\n')
    const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
    downloadBlobAsFile(blob, `voice_transcript_${userId.value}_${sessionId}.md`)
  } catch { /* ignore */ }
}

watch(tab, async (v) => {
  router.replace({ query: { ...route.query, tab: v } })
  await reload()
})
watch(q, () => router.replace({ query: { ...route.query, q: q.value || undefined } }))

onMounted(() => {
  try { installCodeCopyHandler() } catch {}
  reload()
})
</script>

<style scoped lang="postcss">
.conv-selected {
  background-color: color-mix(in oklab, var(--color-base-200) 36%, transparent);
  outline: none;
  box-shadow: none;
  border-color: transparent !important;
  border-top-width: 0 !important;
}

/* Markdown 展示（对齐 AI 助理的可读性） */
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
.ai-md :deep(blockquote) { border-left: 3px solid var(--color-primary, #3b82f6); padding-left: 0.75rem; margin: 0.5rem 0; opacity: 0.9; }
.ai-md :deep(h1), .ai-md :deep(h2), .ai-md :deep(h3), .ai-md :deep(h4) { margin: 0.75rem 0 0.25rem; font-weight: 600; }
.ai-md :deep(hr) { border-color: var(--glass-border-color, rgba(128,128,128,0.2)); margin: 0.75rem 0; }
</style>

