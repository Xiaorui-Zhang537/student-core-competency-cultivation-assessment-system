<template>
  <div class="relative">
    <div class="min-h-screen p-4 md:p-6">
      <div class="max-w-6xl mx-auto mb-4 md:mb-6">
        <page-header :title="t('shared.voicePractice.title')" :subtitle="t('shared.voicePractice.subtitle')">
          <template #actions>
            <Button variant="info" icon="arrow-left" class="whitespace-nowrap shrink-0" @click="goBack">
              {{ t('shared.voicePractice.back') }}
            </Button>
          </template>
        </page-header>
      </div>

      <div class="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-4">
        <aside class="md:col-span-4 lg:col-span-4 xl:col-span-3 filter-container p-4 space-y-4 rounded-xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="flex items-center justify-between gap-3 mb-2">
              <div class="flex items-center gap-2 min-w-0">
                <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200 truncate">{{ t('shared.voicePractice.mySessions') }}</h3>
                <span class="text-xs text-gray-500 shrink-0">{{ sessions.length }}</span>
              </div>
              <Button
                size="sm"
                variant="primary"
                icon="plus"
                class="whitespace-nowrap shrink-0"
                :disabled="status === 'recording' || status === 'connecting' || status === 'waiting' || status === 'saving'"
                @click="createNewSession"
              >
                {{ t('common.new') || t('shared.voicePractice.newSession') }}
              </Button>
            </div>

            <div class="flex items-center gap-2 mb-3">
              <div class="flex-1 min-w-0">
                <glass-search-input v-model="q" :placeholder="t('common.search') || '搜索会话'" />
              </div>
            </div>

            <div v-if="sessions.length === 0" class="text-xs text-gray-500 dark:text-gray-400">
              {{ t('shared.voicePractice.noSessions') }}
            </div>
            <div v-else class="divide-y divide-gray-100 dark:divide-gray-700 rounded-lg border border-gray-100 dark:border-gray-700 overflow-hidden max-h-[50vh] overflow-y-auto">
              <Button
                v-for="s in sessions"
                :key="s.id"
                variant="menu"
                class="w-full px-2 py-3 text-left transition-colors rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700/50 min-h-[64px]"
                :class="Number(activeSessionId) === Number(s.id) ? 'conv-selected border-transparent ring-0 outline-none focus:ring-0 focus-visible:ring-0' : ''"
                @click="selectSession(Number(s.id))"
              >
                <div class="flex flex-col gap-1 w-full px-3">
                  <div class="flex items-center gap-3 w-full min-w-0">
                    <div class="font-medium truncate flex-1 min-w-0">
                      <span v-if="(s as any).pinned" class="mr-1">★</span>
                      {{ s.title || `#${s.id}` }}
                    </div>
                    <div class="flex items-center gap-2 shrink-0 ml-auto">
                      <Button variant="ghost" size="xs" class="text-base-content/70 hover:text-base-content" :title="t('common.rename') || '重命名'" @click.stop="openRename(s)">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M12 20h9"/>
                          <path d="M16.5 3.5l4 4-11 11H5.5v-4.5l11-10.5z"/>
                        </svg>
                      </Button>
                      <Button
                        variant="ghost"
                        size="xs"
                        :class="(s as any).pinned ? 'theme-primary' : 'text-base-content/60 hover:text-base-content'"
                        :title="(s as any).pinned ? (t('common.unpin') || '取消置顶') : (t('common.pin') || '置顶')"
                        @click.stop="togglePin(s)"
                      >
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <path :fill="(s as any).pinned ? 'currentColor' : 'none'" d="M12 17l-5.878 3.09 1.123-6.545L2.49 8.91l6.561-.953L12 2l2.949 5.957 6.561.953-4.755 4.635 1.123 6.545z"/>
                        </svg>
                      </Button>
                      <Button variant="danger" size="xs" :title="t('common.delete') || '删除'" @click.stop="removeSession(Number(s.id))">
                        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                          <polyline points="3 6 5 6 21 6"/>
                          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                          <path d="M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                        </svg>
                      </Button>
                    </div>
                  </div>
                  <div class="text-xs text-gray-500 truncate" v-if="s.model">{{ s.model }}</div>
                </div>
              </Button>
            </div>
          </div>

          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10 space-y-3" v-glass="{ strength: 'ultraThin', interactive: false }">
            <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-200">{{ t('shared.voicePractice.settings') }}</h3>
            <div>
              <label class="block text-xs text-gray-500 mb-1">{{ t('shared.voicePractice.mode') }}</label>
              <glass-popover-select v-model="mode" :options="modeOptions" size="sm" />
            </div>

            <div>
              <label class="block text-xs text-gray-500 mb-1">{{ t('shared.voicePractice.locale') }}</label>
              <glass-popover-select v-model="locale" :options="localeOptions" size="sm" />
            </div>

            <div>
              <label class="block text-xs text-gray-500 mb-1">{{ t('shared.voicePractice.model') }}</label>
              <glass-popover-select v-model="model" :options="modelOptions" size="sm" />
            </div>

            <div>
              <label class="block text-xs text-gray-500 mb-1">{{ t('shared.voicePractice.scenario') }}</label>
              <glass-input v-model="scenario" :placeholder="t('shared.voicePractice.scenarioPlaceholder')" />
              <div class="mt-2 text-xs text-gray-500 dark:text-gray-400 whitespace-pre-line">
                <span class="font-medium text-gray-600 dark:text-gray-300">{{ t('shared.voicePractice.scenarioHelpTitle') }}</span>
                <span class="ml-1">{{ t('shared.voicePractice.scenarioHelp') }}</span>
              </div>
            </div>
          </div>
        </aside>

        <section class="md:col-span-8 lg:col-span-8 xl:col-span-9 glass-thick glass-interactive glass-tint-primary rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden" v-glass="{ strength: 'thick', interactive: true }">
          <div class="px-4 py-3 border-b border-white/25 dark:border-white/10 bg-transparent flex items-center justify-between">
            <div class="font-semibold text-gray-800 dark:text-gray-100">
              {{ t('shared.voicePractice.session') }}
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-400">
              {{ t('shared.voicePractice.status') }}: <span class="font-medium">{{ statusText }}</span>
            </div>
          </div>

          <div class="h-[60vh] overflow-y-auto p-4 space-y-3 bg-transparent">
            <div class="text-sm text-gray-600 dark:text-gray-300">
              {{ t('shared.voicePractice.hint') }}
            </div>

            <div v-if="messages.length === 0" class="text-xs text-gray-500 dark:text-gray-400">
              {{ t('shared.voicePractice.empty') }}
            </div>

            <div v-for="m in messages" :key="m.id" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
              <div class="max-w-[85%] rounded-2xl px-4 py-2 text-sm"
                   :class="m.role === 'user' ? 'glass-bubble glass-bubble-mine rounded-br-none text-white' : 'glass-bubble glass-bubble-peer rounded-bl-none text-base-content'">
                <div class="whitespace-pre-wrap break-words">{{ m.text }}</div>
                <div v-if="m.audioUrl" class="mt-2">
                  <audio
                    :src="m.audioUrl"
                    controls
                    class="w-full"
                    @play="onAudioPlay(m, $event)"
                    @pause="onAudioPause(m, $event)"
                    @ended="onAudioEnded(m, $event)"
                    @timeupdate="onAudioTimeUpdate(m, $event)"
                    @seeking="onAudioSeeking(m)"
                    @seeked="onAudioSeeked(m, $event)"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="border-t border-white/25 dark:border-white/10 p-3 bg-transparent flex items-center gap-2">
            <Button variant="primary" :disabled="starting || status === 'connecting' || status === 'recording' || status === 'waiting' || status === 'saving'" :loading="starting" @click="startSession">
              {{ t('shared.voicePractice.start') }}
            </Button>
            <Button variant="warning" :disabled="status !== 'recording'" @click="stopSession">
              {{ t('shared.voicePractice.stop') }}
            </Button>
            <div class="ml-auto text-xs text-gray-500 dark:text-gray-400">
              {{ t('shared.voicePractice.saveTip') }}
            </div>
          </div>
        </section>
      </div>
      <!-- Rename Modal -->
      <glass-modal
        v-if="showRename"
        :title="(t('common.rename') as string) || '重命名'"
        :backdropDark="false"
        blur="sm"
        clarity="default"
        maxWidth="max-w-[720px]"
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import apiClient, { baseURL } from '@/api/config'
import { fileApi } from '@/api/file.api'
import { voicePracticeApi, type VoiceSession, type VoiceTurn } from '@/api/voicePractice.api'
import { arrayBufferFromBase64, base64FromArrayBuffer, downsampleFloat32Buffer, encodeWavFromInt16PCM, float32ToInt16PCM, parsePcmRateFromMimeType } from '@/utils/audio'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()

const userRole = computed(() => auth.userRole)

const sessions = ref<VoiceSession[]>([])
const activeSessionId = ref<number | null>(null)
const q = ref<string>('')
const showRename = ref(false)
const renameTitle = ref('')
const renameSessionId = ref<number | null>(null)

const requireAccessToken = () => {
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  if (!token) {
    // 未登录：跳转登录，并带上回跳地址
    try {
      const redirect = encodeURIComponent(String(router.currentRoute.value.fullPath || '/'))
      router.push(`/auth/login?redirect=${redirect}`)
    } catch {
      try { router.push('/auth/login') } catch {}
    }
    throw new Error('未登录或登录已失效，请先登录')
  }
  return token
}

// 注意：Live API 同一会话不能同时 TEXT+AUDIO。
// “音频+文字(both)”通过 AUDIO 输出 + outputAudioTranscription 获取文字转写实现。
const mode = ref<'text' | 'audio' | 'both'>('both')
const locale = ref<string>('en-US')
const scenario = ref<string>('')
// 默认：音频相关模式优先 native audio；纯文本默认 2.5 Pro（见 watch(mode)）
const model = ref<string>('google/gemini-2.5-flash-native-audio-preview-12-2025')

const modeOptions = computed(() => [
  { label: t('shared.voicePractice.modeBoth') as string, value: 'both' },
  { label: t('shared.voicePractice.modeText') as string, value: 'text' },
  { label: t('shared.voicePractice.modeAudio') as string, value: 'audio' }
])

// 当前产品策略：语音训练仅开放英文口语训练（功能保留，但不提供中文选项）
const localeOptions = computed(() => [
  { label: 'English (en-US)', value: 'en-US' }
])

// 兜底：若被意外设置为非 en-US，则强制拉回（避免旧状态/历史代码导致 UI 出现不可选值）
watch(locale, (v) => {
  if (String(v || '').toLowerCase() !== 'en-us') {
    locale.value = 'en-US'
  }
}, { immediate: true })

const modelOptions = computed(() => {
  if (mode.value === 'text') {
    return [
      { label: 'Gemini 2.5 Pro', value: 'google/gemini-2.5-pro' },
      { label: 'Gemini 2.5 Flash', value: 'google/gemini-2.5-flash' }
    ]
  }
  return [
    { label: 'Gemini 2.5 Flash Native Audio (preview)', value: 'google/gemini-2.5-flash-native-audio-preview-12-2025' },
    { label: 'Gemini Live 2.5 Flash (preview)', value: 'google/gemini-live-2.5-flash-preview' }
  ]
})

watch(mode, (m) => {
  if (m === 'text') {
    model.value = 'google/gemini-2.5-pro'
  } else {
    model.value = 'google/gemini-2.5-flash-native-audio-preview-12-2025'
  }
}, { immediate: true })

type Status = 'idle' | 'connecting' | 'ready' | 'recording' | 'waiting' | 'saving' | 'error'
const status = ref<Status>('idle')
const starting = ref(false)

const statusText = computed(() => {
  if (status.value === 'idle') return t('shared.voicePractice.statusIdle') as string
  if (status.value === 'connecting') return t('shared.voicePractice.statusConnecting') as string
  if (status.value === 'ready') return t('shared.voicePractice.statusReady') as string
  if (status.value === 'recording') return t('shared.voicePractice.statusRecording') as string
  if (status.value === 'waiting') return t('shared.voicePractice.statusWaiting') as string
  if (status.value === 'saving') return t('shared.voicePractice.statusSaving') as string
  return t('shared.voicePractice.statusError') as string
})

type UiMsg = {
  id: string
  role: 'user' | 'assistant'
  text: string
  audioUrl?: string | null
  /** 音频文件ID（若来自文件流/上传结果，用于行为审计定位） */
  audioFileId?: number | null
  /** 回合ID（若已入库，用于行为审计定位） */
  turnId?: number | null
}
const messages = ref<UiMsg[]>([])

// ======================
// 复听/回放：按播放时长累计（秒）
// ======================
type ReplayTracker = {
  playing: boolean
  seeking: boolean
  lastTime: number
  pendingSeconds: number
}

const replayTrackers = new Map<string, ReplayTracker>()
const REPLAY_FLUSH_THRESHOLD_SECONDS = 10

const getReplayTracker = (msgId: string) => {
  if (!replayTrackers.has(msgId)) {
    replayTrackers.set(msgId, { playing: false, seeking: false, lastTime: 0, pendingSeconds: 0 })
  }
  return replayTrackers.get(msgId)!
}

const getMediaEl = (ev: Event) => {
  const el = (ev?.target || null) as HTMLMediaElement | null
  return el && typeof el.currentTime === 'number' ? el : null
}

const addReplayDeltaFromEl = (m: UiMsg, el: HTMLMediaElement | null) => {
  if (!el) return
  const tr = getReplayTracker(m.id)
  const now = Number(el.currentTime || 0)
  if (tr.seeking || !tr.playing) {
    tr.lastTime = now
    return
  }
  const delta = now - (tr.lastTime || 0)
  tr.lastTime = now
  // 过滤：拖拽/跳转产生的大 delta、或异常负值
  // timeupdate 间隔不稳定，保守允许到 15s；seek 主要由 seeking/seeked 兜底
  if (!(delta > 0) || delta > 15) return
  tr.pendingSeconds += delta
  if (tr.pendingSeconds >= REPLAY_FLUSH_THRESHOLD_SECONDS) {
    void flushReplay(m)
  }
}

const flushReplay = async (m: UiMsg) => {
  const sid = activeSessionId.value || null
  if (!sid) return
  if (!m || !m.id) return
  if (m.role !== 'user' && m.role !== 'assistant') return
  const tr = getReplayTracker(m.id)
  const seconds = Math.floor(tr.pendingSeconds || 0)
  if (seconds <= 0) return
  tr.pendingSeconds -= seconds
  try {
    await voicePracticeApi.reportReplay({
      sessionId: sid,
      turnId: m.turnId != null ? Number(m.turnId) : undefined,
      audioRole: m.role,
      deltaSeconds: seconds,
      fileId: m.audioFileId != null ? Number(m.audioFileId) : undefined,
      messageId: String(m.id)
    })
  } catch {
    // 上报失败不影响用户播放；保守处理：不回滚 pending（避免重复上报）
  }
}

const onAudioPlay = (m: UiMsg, ev: Event) => {
  const el = getMediaEl(ev)
  const tr = getReplayTracker(m.id)
  tr.playing = true
  tr.seeking = false
  tr.lastTime = Number(el?.currentTime || 0)
}

const onAudioPause = (m: UiMsg, ev: Event) => {
  const el = getMediaEl(ev)
  addReplayDeltaFromEl(m, el)
  const tr = getReplayTracker(m.id)
  tr.playing = false
  void flushReplay(m)
}

const onAudioEnded = (m: UiMsg, ev: Event) => {
  const el = getMediaEl(ev)
  addReplayDeltaFromEl(m, el)
  const tr = getReplayTracker(m.id)
  tr.playing = false
  void flushReplay(m)
}

const onAudioTimeUpdate = (m: UiMsg, ev: Event) => {
  const el = getMediaEl(ev)
  addReplayDeltaFromEl(m, el)
}

const onAudioSeeking = (m: UiMsg) => {
  const tr = getReplayTracker(m.id)
  tr.seeking = true
}

const onAudioSeeked = (m: UiMsg, ev: Event) => {
  const el = getMediaEl(ev)
  const tr = getReplayTracker(m.id)
  tr.seeking = false
  tr.lastTime = Number(el?.currentTime || 0)
}

// runtime holders
let ws: WebSocket | null = null
let mediaStream: MediaStream | null = null
let recorder: MediaRecorder | null = null
let recorderChunks: BlobPart[] = []
let audioCtx: AudioContext | null = null
let processor: ScriptProcessorNode | null = null
let sourceNode: MediaStreamAudioSourceNode | null = null
let workletNode: AudioWorkletNode | null = null
let workletGain: GainNode | null = null

// assistant audio buffering (PCM16) for saving WAV
let assistantPcmChunks: Int16Array[] = []
let assistantAudioSampleRate = 24000
let assistantFullText = ''
let userFinalTranscript = ''

// 手动回合控制：等待 setupComplete / 追踪当前回合消息
let sessionReadyResolver: (() => void) | null = null
let sessionReadyRejecter: ((err: any) => void) | null = null
let turnUserMsgId: string | null = null
let turnAssistantMsgId: string | null = null

// playback scheduling
let playbackCtx: AudioContext | null = null
let playbackTime = 0

const getApiWsUrl = () => {
  const token = localStorage.getItem('token') || ''
  const base = String(apiClient.defaults.baseURL || baseURL || '/api').replace(/\/+$/, '')
  // base 通常是 /api 或 http(s)://host/api
  const wsBase = base.startsWith('http') ? base.replace(/^http/i, 'ws') : `${location.origin.replace(/^http/i, 'ws')}${base.startsWith('/') ? '' : '/'}${base}`
  return `${wsBase}/ai/live/ws?token=${encodeURIComponent(token)}`
}

const safeCloseWs = () => {
  try { ws?.close() } catch {}
  ws = null
}

const cleanupAudioCapture = () => {
  try { processor?.disconnect() } catch {}
  try { workletNode?.disconnect() } catch {}
  try { workletGain?.disconnect() } catch {}
  try { sourceNode?.disconnect() } catch {}
  processor = null
  workletNode = null
  workletGain = null
  sourceNode = null
  try { audioCtx?.close() } catch {}
  audioCtx = null
  try { mediaStream?.getTracks()?.forEach(t => t.stop()) } catch {}
  mediaStream = null
  recorder = null
  recorderChunks = []
}

const cleanupPlayback = () => {
  try { playbackCtx?.close() } catch {}
  playbackCtx = null
  playbackTime = 0
}

const appendMessage = (m: UiMsg) => {
  messages.value.push(m)
}

const buildAuthedStreamUrl = (fileId: number) => {
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  const base = `/files/${encodeURIComponent(String(fileId))}/stream`
  const url = token ? `${base}?token=${encodeURIComponent(String(token))}` : base
  // 兼容：fileApi.buildFileUrl（已在 file.api.ts 中提供）
  return (fileApi as any).buildFileUrl ? (fileApi as any).buildFileUrl(url) : url
}

const loadSessions = async () => {
  try {
    requireAccessToken()
    const res: any = await voicePracticeApi.listSessions({ q: q.value?.trim() || undefined, page: 1, size: 50 })
    const data = res?.data ?? res
    sessions.value = Array.isArray(data) ? data : (data?.items || data?.list || [])
    if (!activeSessionId.value && sessions.value.length) {
      activeSessionId.value = Number((sessions.value[0] as any).id || (sessions.value[0] as any).sessionId)
      await loadTurns(activeSessionId.value)
    }
  } catch {
    sessions.value = []
  }
}

onMounted(() => {
  // 首次进入页面：自动加载“我的会话”列表
  try { loadSessions() } catch {}
})

// 搜索：简单防抖
let searchTimer: any = null
watch(q, () => {
  if (searchTimer) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => {
    try { loadSessions() } catch {}
  }, 250)
})

const openRename = (s: VoiceSession) => {
  renameSessionId.value = Number((s as any).id || (s as any).sessionId || 0) || null
  renameTitle.value = String((s as any).title || '')
  showRename.value = true
}

const confirmRename = async () => {
  const sid = renameSessionId.value
  const title = renameTitle.value?.trim()
  if (!sid || !title) return
  requireAccessToken()
  await voicePracticeApi.updateSession(sid, { title })
  showRename.value = false
  renameSessionId.value = null
  await loadSessions()
}

const togglePin = async (s: VoiceSession) => {
  const sid = Number((s as any).id || (s as any).sessionId || 0)
  if (!sid) return
  requireAccessToken()
  const pinned = !(s as any).pinned
  await voicePracticeApi.updateSession(sid, { pinned })
  await loadSessions()
}

const removeSession = async (sid: number) => {
  if (!sid) return
  if (!window.confirm('确定删除该会话吗？删除后将不再在列表中显示。')) return
  requireAccessToken()
  try { await closeSession() } catch {}
  await voicePracticeApi.deleteSession(sid)
  if (activeSessionId.value === sid) {
    activeSessionId.value = null
    messages.value = []
  }
  await loadSessions()
}

const loadTurns = async (sid: number) => {
  try {
    const res: any = await voicePracticeApi.listTurns(String(sid), { page: 1, size: 500 })
    const data = res?.data ?? res
    const turns: any[] = Array.isArray(data) ? data : (data?.items || data?.list || [])
    const ui: UiMsg[] = []
    for (const t of turns) {
      const tid = Number(t?.id || 0)
      const userText = String(t?.userTranscript || '')
      const asText = String(t?.assistantText || '')
      const uFid = t?.userAudioFileId != null ? Number(t.userAudioFileId) : null
      const aFid = t?.assistantAudioFileId != null ? Number(t.assistantAudioFileId) : null
      ui.push({
        id: `turn_${tid}_u`,
        role: 'user',
        text: userText,
        audioUrl: uFid ? buildAuthedStreamUrl(uFid) : null,
        audioFileId: uFid,
        turnId: tid || null
      })
      if (asText || aFid) {
        ui.push({
          id: `turn_${tid}_a`,
          role: 'assistant',
          text: asText,
          audioUrl: aFid ? buildAuthedStreamUrl(aFid) : null,
          audioFileId: aFid,
          turnId: tid || null
        })
      }
    }
    messages.value = ui
  } catch {
    messages.value = []
  }
}

const createNewSession = async () => {
  if (status.value === 'connecting' || status.value === 'recording' || status.value === 'waiting' || status.value === 'saving') return
  try { await closeSession() } catch {}
  requireAccessToken()
  const title = scenario.value?.trim() ? `口语训练-${scenario.value.trim()}` : '口语训练'
  const res: any = await voicePracticeApi.createSession({
    title,
    model: model.value,
    mode: mode.value,
    locale: locale.value,
    scenario: scenario.value
  })
  const data = res?.data ?? res
  const sid = Number(data?.sessionId || data?.id || 0)
  if (sid) {
    activeSessionId.value = sid
    messages.value = []
    await loadSessions()
  }
}

const selectSession = async (sid: number) => {
  if (!sid) return
  if (status.value !== 'idle') {
    try { await closeSession() } catch {}
  }
  activeSessionId.value = sid
  await loadTurns(sid)
}

const startSession = async () => {
  // 语音训练改为“按回合”：开始=开始本轮；停止=结束本轮触发 AI 回复（不中断会话）
  if (starting.value) return
  if (status.value === 'recording' || status.value === 'waiting' || status.value === 'saving') return

  // 1) 首次：建立 WS + 麦克风
  if (status.value === 'idle') {
    requireAccessToken()
    starting.value = true
    status.value = 'connecting'
    try {
      // 确保存在一个语音会话
      if (!activeSessionId.value) {
        await createNewSession()
      }
      const sid = activeSessionId.value
      if (!sid) throw new Error('无法创建语音会话')

      // connect WS
      ws = new WebSocket(getApiWsUrl())
      ws.onmessage = (ev) => handleWsMessage(ev.data)
      ws.onerror = () => { status.value = 'error' }
      ws.onclose = () => { status.value = 'idle' }

      // wait for ws open
      await new Promise<void>((resolve, reject) => {
        if (!ws) return reject(new Error('ws init failed'))
        ws.onopen = () => resolve()
        ws.onerror = () => reject(new Error('ws connect failed'))
      })

      // send start
      ws.send(JSON.stringify({
        type: 'start',
        // 这里复用字段名 conversationId：仅用于后端透传/记录，不再写入 AI 助理会话
        conversationId: sid,
        model: model.value,
        mode: mode.value,
        locale: locale.value,
        scenario: scenario.value
      }))

      // wait session_ready (setupComplete)
      await new Promise<void>((resolve, reject) => {
        let done = false
        sessionReadyRejecter = (err: any) => {
          if (done) return
          done = true
          sessionReadyResolver = null
          sessionReadyRejecter = null
          reject(err instanceof Error ? err : new Error(String(err || 'Live session setup 失败')))
        }
        const timer = window.setTimeout(() => {
          if (done) return
          done = true
          sessionReadyResolver = null
          sessionReadyRejecter = null
          reject(new Error('Live session setup 超时（请检查：是否已登录、后端是否配置 Gemini API Key、网络/代理是否可访问 Google）'))
        }, 60000)
        sessionReadyResolver = () => {
          if (done) return
          done = true
          window.clearTimeout(timer)
          sessionReadyRejecter = null
          resolve()
        }
      })

      // start mic capture
      mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })

      // 实时 PCM：优先 AudioWorklet（更好的性能/更低延迟），不支持则回退 ScriptProcessor
      audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
      sourceNode = audioCtx.createMediaStreamSource(mediaStream)
      const targetRate = 16000

      // AudioWorklet path
      const canWorklet = !!(audioCtx as any).audioWorklet && typeof (window as any).AudioWorkletNode !== 'undefined'
      if (canWorklet) {
        try {
          // NOTE: must be a real URL; use relative path from this file
          const url = new URL('../../../utils/worklets/pcm16-downsampler.worklet.ts', import.meta.url)
          await (audioCtx as any).audioWorklet.addModule(url)

          const wn: AudioWorkletNode = new (window as any).AudioWorkletNode(audioCtx, 'pcm16-downsampler', {
            processorOptions: { targetSampleRate: targetRate }
          })

          // Keep the node "alive" by connecting to destination through a zero-gain
          const gain = audioCtx.createGain()
          gain.gain.value = 0

          sourceNode.connect(wn)
          wn.connect(gain)
          gain.connect(audioCtx.destination)

          wn.port.onmessage = (ev: MessageEvent) => {
            const data: any = (ev as any)?.data
            if (!data || data.type !== 'pcm16') return
            if (!ws || ws.readyState !== WebSocket.OPEN || status.value !== 'recording') return
            const buf = data.pcm16 as ArrayBuffer
            if (!buf || (buf as any).byteLength <= 0) return
            const b64 = base64FromArrayBuffer(buf)
            ws.send(JSON.stringify({ type: 'audio_chunk', seq: Date.now(), pcm16Base64: b64, sampleRate: targetRate }))
          }

          workletNode = wn
          workletGain = gain
        } catch (e) {
          // fallback to ScriptProcessor below
          try { console.warn('AudioWorklet init failed, fallback to ScriptProcessor:', e) } catch {}
          try { workletNode?.disconnect() } catch {}
          try { workletGain?.disconnect() } catch {}
          workletNode = null
          workletGain = null
        }
      }

      // Fallback ScriptProcessor path
      if (!workletNode) {
        processor = audioCtx.createScriptProcessor(2048, 1, 1)
        sourceNode.connect(processor)
        processor.connect(audioCtx.destination)
        processor.onaudioprocess = (e) => {
          if (!ws || ws.readyState !== WebSocket.OPEN || status.value !== 'recording') return
          const input = e.inputBuffer.getChannelData(0)
          const down = downsampleFloat32Buffer(input, audioCtx!.sampleRate, targetRate)
          const pcm16 = float32ToInt16PCM(down)
          const b64 = base64FromArrayBuffer(pcm16.buffer as ArrayBuffer)
          ws.send(JSON.stringify({ type: 'audio_chunk', seq: Date.now(), pcm16Base64: b64, sampleRate: targetRate }))
        }
      }

      status.value = 'ready'
    } catch (e: any) {
      status.value = 'error'
      try { console.error(e) } catch {}
      safeCloseWs()
      cleanupAudioCapture()
      return
    } finally {
      starting.value = false
    }
  }

  // 2) 开始本轮（activity_start + 录音缓存）
  if (status.value !== 'ready') return
  assistantPcmChunks = []
  assistantFullText = ''
  userFinalTranscript = ''
  recorderChunks = []
  turnUserMsgId = `u_${Date.now()}`
  turnAssistantMsgId = null
  appendMessage({ id: turnUserMsgId, role: 'user', text: '' })

  try {
    // 持久化录音：每回合单独录一段（webm/opus）
    const mimeCandidates = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/ogg'
    ]
    const mimeType = mimeCandidates.find(m => (window as any).MediaRecorder && MediaRecorder.isTypeSupported(m)) || ''
    if (mediaStream) {
      recorder = new MediaRecorder(mediaStream, mimeType ? { mimeType } : undefined)
      recorder.ondataavailable = (e) => { if ((e as any).data && (e as any).data.size > 0) recorderChunks.push((e as any).data) }
      recorder.start(500)
    }
  } catch {}

  try { ws?.send(JSON.stringify({ type: 'activity_start' })) } catch {}
  status.value = 'recording'
}

const stopSession = async () => {
  // 结束本轮：activity_end 触发模型回复；等待 turn_complete 后持久化本轮
  if (status.value !== 'recording') return
  status.value = 'waiting'
  try { ws?.send(JSON.stringify({ type: 'activity_end' })) } catch {}
  try { recorder?.stop() } catch {}
  await new Promise(r => setTimeout(r, 200))
}

const closeSession = async () => {
  try { ws?.send(JSON.stringify({ type: 'stop' })) } catch {}
  cleanupAudioCapture()
  safeCloseWs()
  cleanupPlayback()
  status.value = 'idle'
}

const persistTurn = async () => {
  const sid = activeSessionId.value || null
  if (!sid) return

  let userAudioFileId: number | undefined
  let assistantAudioFileId: number | undefined

  try {
    status.value = 'saving'

    // upload user audio
    const userBlob = new Blob(recorderChunks as any[], { type: (recorder as any)?.mimeType || 'audio/webm' })
    const userUrl = URL.createObjectURL(userBlob)
    const userFile = new File([userBlob], `user_${Date.now()}.webm`, { type: userBlob.type })
    const userInfo: any = await fileApi.uploadFile(userFile, { purpose: 'ai_voice', relatedId: String(sid) })
    userAudioFileId = Number(userInfo?.id || userInfo?.fileId)

    if (turnUserMsgId) {
      const u = messages.value.find(m => m.id === turnUserMsgId)
      if (u) {
        u.audioUrl = userUrl
        u.audioFileId = userAudioFileId || null
      }
    }

    // upload assistant audio (WAV)
    const assistantPcm = mergeInt16Chunks(assistantPcmChunks)
    if (assistantPcm.length > 0) {
      const wavBlob = encodeWavFromInt16PCM(assistantPcm, assistantAudioSampleRate, 1)
      const assistantUrl = URL.createObjectURL(wavBlob)
      const wavFile = new File([wavBlob], `assistant_${Date.now()}.wav`, { type: 'audio/wav' })
      const asInfo: any = await fileApi.uploadFile(wavFile, { purpose: 'ai_voice', relatedId: String(sid) })
      assistantAudioFileId = Number(asInfo?.id || asInfo?.fileId)

      if (turnAssistantMsgId) {
        const a = messages.value.find(m => m.id === turnAssistantMsgId)
        if (a) {
          a.audioUrl = assistantUrl
          a.audioFileId = assistantAudioFileId || null
        }
      }
    }

    const saved: any = await voicePracticeApi.saveTurn({
      sessionId: sid,
      model: model.value,
      userTranscript: userFinalTranscript || '',
      assistantText: assistantFullText || '',
      userAudioFileId,
      assistantAudioFileId,
      scenario: scenario.value,
      locale: locale.value
    })
    const savedTurnId = Number(saved?.turnId || 0) || null
    if (savedTurnId) {
      if (turnUserMsgId) {
        const u = messages.value.find(m => m.id === turnUserMsgId)
        if (u) u.turnId = savedTurnId
      }
      if (turnAssistantMsgId) {
        const a = messages.value.find(m => m.id === turnAssistantMsgId)
        if (a) a.turnId = savedTurnId
      }
    }
  } catch (e) {
    try { console.error(e) } catch {}
  } finally {
    if (status.value !== 'error') status.value = 'ready'
  }
}

const mergeInt16Chunks = (chunks: Int16Array[]) => {
  const total = chunks.reduce((sum, c) => sum + c.length, 0)
  const out = new Int16Array(total)
  let offset = 0
  for (const c of chunks) {
    out.set(c, offset)
    offset += c.length
  }
  return out
}

const handleWsMessage = (raw: any) => {
  let msg: any = null
  try { msg = JSON.parse(String(raw)) } catch { return }
  const type = msg?.type
  if (!type) return

  if (type === 'error') {
    // 连接/鉴权/上游失败时，及时结束“等待 session_ready”，否则只能等超时
    const code = String(msg?.code || '')
    const message = String(msg?.message || 'Live error')
    try { sessionReadyRejecter?.(new Error(`${code || 'ERROR'}: ${message}`)) } catch {}
    sessionReadyResolver = null
    sessionReadyRejecter = null
    status.value = 'error'
    return
  }

  // 后端会在 setupComplete 后发 session_ready；同时也会发 started（为了兼容，started 也视为可继续）
  if (type === 'session_ready') {
    try { sessionReadyResolver?.() } catch {}
    sessionReadyResolver = null
    sessionReadyRejecter = null
    return
  }
  if (type === 'started') {
    try { sessionReadyResolver?.() } catch {}
    sessionReadyResolver = null
    sessionReadyRejecter = null
    return
  }

  if (type === 'transcript') {
    // 输入转写（当前后端按 final=true 推送）
    userFinalTranscript = normalizeLiveTranscript(String(msg.text || ''))
    // 更新当前回合的 user 消息
    if (turnUserMsgId) {
      const u = messages.value.find(m => m.id === turnUserMsgId)
      if (u) u.text = userFinalTranscript
    } else {
      const last = messages.value[messages.value.length - 1]
      if (last?.role === 'user') last.text = userFinalTranscript
      else appendMessage({ id: `u_${Date.now()}`, role: 'user', text: userFinalTranscript })
    }
    return
  }

  if (type === 'assistant_text') {
    const delta = String(msg.delta || '')
    if (!delta) return
    assistantFullText += delta
    // 让“✅/💡/❓...”更像条目：在 emoji 前自动换行（展示层，不影响上游）
    assistantFullText = formatEmojiBulletsForUi(assistantFullText)
    if (!turnAssistantMsgId) {
      turnAssistantMsgId = `a_${Date.now()}`
      appendMessage({ id: turnAssistantMsgId, role: 'assistant', text: assistantFullText })
    } else {
      const a = messages.value.find(m => m.id === turnAssistantMsgId)
      if (a) a.text = assistantFullText
    }
    return
  }

  if (type === 'assistant_transcript') {
    // 输出转写（更接近“AI说了什么”）
    const text = normalizeLiveTranscript(String(msg.text || ''))
    if (text) {
      assistantFullText = formatEmojiBulletsForUi(text)
      if (!turnAssistantMsgId) {
        turnAssistantMsgId = `a_${Date.now()}`
        appendMessage({ id: turnAssistantMsgId, role: 'assistant', text: assistantFullText })
      } else {
        const a = messages.value.find(m => m.id === turnAssistantMsgId)
        if (a) a.text = assistantFullText
      }
    }
    return
  }

  if (type === 'assistant_audio') {
    const b64 = String(msg.pcm16Base64 || '')
    const mimeType = msg.mimeType ? String(msg.mimeType) : null
    if (!b64) return
    const rate = parsePcmRateFromMimeType(mimeType) || 24000
    assistantAudioSampleRate = rate
    const buf = arrayBufferFromBase64(b64)
    const pcm16 = new Int16Array(buf)
    assistantPcmChunks.push(pcm16)
    playPcmChunk(pcm16, rate)
    return
  }

  if (type === 'turn_complete') {
    // 一轮结束：后台会等待新的回合开始
    void persistTurn()
    return
  }
}

/**
 * UI 友好格式化：把常见“提示型 emoji”当作条目起始，自动换行。
 * 只作用于展示/复制的文本，不改变音频/模型输出本身。
 */
const formatEmojiBulletsForUi = (raw: string) => {
  let s = String(raw || '')
  s = s.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  // 避免重复插入：只把这些 emoji 视为“条目起始”，在它们前插入换行
  // 支持变体选择符（\uFE0F）与部分复合 emoji
  const bullets = [
    '✅','💡','❓','⚠️','⚠','📌','📝','🎯','🧾','⭐️','⭐','🔍','🔎','👉','➡️','➡','🔸','🔹','•','-'
  ]
  const bulletGroup = bullets
    .map(e => e.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
    .join('|')
  const bulletRe = new RegExp(`(^|[^\\n])\\s*(${bulletGroup})(?=\\s|[A-Za-z])`, 'g')
  s = s.replace(bulletRe, (_m, prefix: string, emo: string) => {
    if (!prefix) return emo
    return `${prefix}\n${emo}`
  })
  // 清理换行后空格
  s = s.replace(/\n[ \t]+/g, '\n')
  // 压缩多余空格（不压缩换行）
  s = s.replace(/[ \t]+/g, ' ')
  return s.trim()
}

/**
 * 口语转写文本“去乱空格/合词”：
 * - 修正 it' s -> it's 等缩写
 * - 合并明显被拆开的英文单词（ex er cise -> exercise, uni vers ity -> university）
 * - 处理标点前多余空格
 *
 * 说明：这是一组启发式规则（不依赖词典），目标是“尽量好读”而不是 100% 语义正确。
 */
const normalizeLiveTranscript = (input: string) => {
  let s = String(input || '')
  s = s.replace(/\s+/g, ' ').trim()
  // fix spaces before punctuation
  s = s.replace(/\s+([,.!?;:])/g, '$1')
  // fix common contraction split: it' s / I ' m -> it's / I'm
  s = s.replace(/\b([A-Za-z]+)'\s+([A-Za-z]+)\b/g, "$1'$2")
  s = s.replace(/\b([A-Za-z])\s+'\s*([A-Za-z])\b/g, "$1'$2")

  // Only apply aggressive English merging when locale looks like English
  const loc = String(locale.value || '').toLowerCase()
  if (!loc.startsWith('en')) return s

  const stop = new Set([
    'i','you','we','he','she','it','they',
    'a','an','the','and','or','but',
    'to','of','in','on','at','for','with','as','is','are','was','were','be','am',
    'my','your','his','her','our','their',
    'this','that','these','those',
    'do','did','does','then'
  ])

  // Tokenize with punctuation separated
  const punct = new Set([',', '.', '!', '?', ';', ':', '(', ')'])
  const tokens = s.replace(/([,.!?;:()])/g, ' $1 ').split(/\s+/).filter(Boolean)

  const isWord = (t: string) => /^[A-Za-z']+$/.test(t)
  const base = (t: string) => t.toLowerCase()

  // 保守合词：仅用于明显“被拆开的英文单词”，避免把正常句子连成一坨。
  // 策略：
  // - 只合并纯字母 token（不含 apostrophe）
  // - token 不得为 stopword
  // - 至少包含一个非常短的片段（<=3），以匹配 “pro gram ming / uni vers ity / ex er cise”
  // - 总长度限制，避免过度合并
  const canMergeRun = (parts: string[]) => {
    if (!parts || parts.length < 2) return false
    const lowers = parts.map(base)
    if (lowers.some(w => stop.has(w))) return false
    if (parts.some(p => !/^[A-Za-z]+$/.test(p))) return false
    const total = lowers.reduce((sum, w) => sum + w.length, 0)
    const hasShort = lowers.some(w => w.length <= 3)
    if (!hasShort) return false
    if (total < 4 || total > 14) return false
    return true
  }

  const out: string[] = []
  for (let i = 0; i < tokens.length; i++) {
    const t = tokens[i]
    if (punct.has(t)) { out.push(t); continue }
    if (!isWord(t)) { out.push(t); continue }

    // Try merge 4/3/2 tokens ahead (prefer longer to get exercise/programming/university)
    let merged: string | null = null
    let step = 0
    for (const n of [4, 3, 2]) {
      if (i + n - 1 >= tokens.length) continue
      const parts = tokens.slice(i, i + n)
      if (parts.some(p => punct.has(p))) continue
      if (parts.some(p => !isWord(p))) continue
      if (!canMergeRun(parts)) continue
      merged = parts.join('')
      step = n
      break
    }
    if (merged && step > 0) {
      out.push(merged)
      i += (step - 1)
      continue
    }

    out.push(t)
  }

  let joined = out.join(' ')
  joined = joined.replace(/\s+([,.!?;:)])/g, '$1').replace(/([(])\s+/g, '$1')
  joined = joined.replace(/\s+/g, ' ').trim()

  // 进一步“拆开明显粘连”的常见短词（避免 canfeel / havesome / knowhow 影响可读性）
  joined = joined
    .replace(/\b(havesome)\b/gi, 'have some')
    .replace(/\b(knowhow)\b/gi, 'know how')
    .replace(/\b(canfeel)\b/gi, 'can feel')
    .replace(/\b(dontknow)\b/gi, "don't know")
    .replace(/\b(dontknowhow)\b/gi, "don't know how")
    .replace(/\b(don'tknow)\b/gi, "don't know")
    .replace(/\b(don'tknowhow)\b/gi, "don't know how")
    .replace(/\b(cant)\b/gi, "can't")
    .replace(/\b(wont)\b/gi, "won't")
    .replace(/\b(shouldnt)\b/gi, "shouldn't")
    .replace(/\b(wouldnt)\b/gi, "wouldn't")
    .replace(/\b(couldnt)\b/gi, "couldn't")

  return joined
}

const playPcmChunk = (pcm16: Int16Array, sampleRate: number) => {
  if (!pcm16 || pcm16.length === 0) return
  if (!playbackCtx) {
    playbackCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
    playbackTime = playbackCtx.currentTime
  }
  const ctx = playbackCtx
  const audioBuffer = ctx.createBuffer(1, pcm16.length, sampleRate)
  const ch0 = audioBuffer.getChannelData(0)
  for (let i = 0; i < pcm16.length; i++) {
    ch0[i] = pcm16[i] / 0x8000
  }
  const src = ctx.createBufferSource()
  src.buffer = audioBuffer
  src.connect(ctx.destination)
  const startAt = Math.max(ctx.currentTime, playbackTime)
  src.start(startAt)
  playbackTime = startAt + audioBuffer.duration
}

const goBack = () => {
  // 回到原 AI 助理页（按当前路由自动回退）
  router.back()
}

onBeforeUnmount(() => {
  // 尽量把复听秒数冲刷上报（不阻塞卸载）
  try {
    for (const m of messages.value) {
      void flushReplay(m)
    }
  } catch {}
  try { void closeSession() } catch {}
})
</script>

