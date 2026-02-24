<template>
  <div class="p-6">
    <page-header :title="t('admin.student360.auditAiVoice') || 'AI/口语审计'" :subtitle="subtitle">
      <template #actions>
        <button variant="outline" @click="goBack">{{ t('common.back') || '返回' }}</button>
      </template>
    </page-header>

    <card padding="md" tint="secondary" class="mt-4">
      <div class="flex items-center justify-between gap-3 flex-wrap">
        <segmented-pills :model-value="tab" :options="tabOptions" size="sm" variant="info" @update:modelValue="(v:any)=> tab = String(v) as any" />
        <div class="flex items-center gap-2">
          <div class="w-72">
            <glass-search-input v-model="q" :placeholder="String(t('common.search') || '搜索')" size="sm" tint="info" />
          </div>
          <button size="sm" variant="outline" :disabled="loading" @click="reload">{{ t('common.refresh') || '刷新' }}</button>
        </div>
      </div>
    </card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else class="mt-4 grid grid-cols-1 lg:grid-cols-3 gap-6">
      <card padding="md" tint="secondary" class="lg:col-span-1">
        <div class="text-sm font-medium mb-3">{{ leftTitle }}</div>
        <div class="space-y-2 max-h-[70vh] overflow-auto pr-1">
          <div
            v-for="x in leftItems"
            :key="x.id"
            class="p-3 rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 transition-colors cursor-pointer"
            :class="selectedLeftId === String(x.id) ? 'ring-2 ring-primary/40' : ''"
            @click="selectLeft(String(x.id))"
          >
            <div class="font-medium line-clamp-1">{{ x.title || x.topic || x.name || `#${x.id}` }}</div>
            <div class="text-xs text-subtle line-clamp-1">{{ x.createdAt || x.updatedAt || x.startedAt || '-' }}</div>
          </div>
          <empty-state v-if="leftItems.length === 0" :title="String(t('common.empty') || '暂无数据')" />
        </div>
      </card>

      <card padding="md" tint="secondary" class="lg:col-span-2">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm font-medium">{{ rightTitle }}</div>
          <button v-if="tab==='voice'" size="sm" variant="outline" :disabled="turns.length===0" @click="exportVoiceAudioZip">
            {{ t('admin.tools.exportZip') || '导出ZIP' }}
          </button>
        </div>

        <!-- AI messages -->
        <div v-if="tab==='ai'" class="space-y-3">
          <div v-if="messages.length" class="space-y-3 max-h-[70vh] overflow-auto pr-1">
            <div v-for="m in messages" :key="m.id" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="text-xs text-subtle mb-1">{{ m.role || m.senderRole || '-' }} · {{ m.createdAt || '-' }}</div>
              <div class="text-sm whitespace-pre-wrap break-words">{{ m.content || m.text || '-' }}</div>
            </div>
          </div>
          <empty-state v-else :title="String(t('admin.student360.selectConversation') || '请选择会话')" />
        </div>

        <!-- Voice turns -->
        <div v-else class="space-y-3">
          <div v-if="turns.length" class="space-y-3 max-h-[70vh] overflow-auto pr-1">
            <div v-for="t0 in turns" :key="t0.id" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="text-xs text-subtle mb-2">{{ t0.createdAt || '-' }}</div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div>
                  <div class="text-sm font-medium mb-1">{{ t('admin.student360.voiceUser') || '学生' }}</div>
                  <div class="text-xs text-subtle whitespace-pre-wrap">{{ t0.userText || '-' }}</div>
                  <audio v-if="t0.userAudioFileId" class="mt-2 w-full" controls :src="fileUrl(t0.userAudioFileId)" />
                </div>
                <div>
                  <div class="text-sm font-medium mb-1">{{ t('admin.student360.voiceAssistant') || 'AI' }}</div>
                  <div class="text-xs text-subtle whitespace-pre-wrap">{{ t0.assistantText || '-' }}</div>
                  <audio v-if="t0.assistantAudioFileId" class="mt-2 w-full" controls :src="fileUrl(t0.assistantAudioFileId)" />
                </div>
              </div>
            </div>
          </div>
          <empty-state v-else :title="String(t('admin.student360.selectVoiceSession') || '请选择会话')" />
        </div>
      </card>
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
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import { adminApi } from '@/api/admin.api'
import { buildFileUrl, fileApi } from '@/api/file.api'
import { downloadBlobAsFile } from '@/utils/download'
import JSZip from 'jszip'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const userId = computed(() => String((route.params as any).userId || ''))
const userName = computed(() => String((route.query as any)?.name || ''))
const subtitle = computed(() => {
  const n = userName.value.trim()
  return n ? `${n} · #${userId.value}` : `#${userId.value}`
})
const tab = ref<'ai' | 'voice'>((String((route.query as any)?.tab || 'ai') as any) || 'ai')
const q = ref(String((route.query as any)?.q || ''))

const loading = ref(false)
const error = ref<string | null>(null)

const aiConversations = ref<any[]>([])
const messages = ref<any[]>([])
const voiceSessions = ref<any[]>([])
const turns = ref<any[]>([])

const selectedLeftId = ref<string>('')

const tabOptions = computed(() => ([
  { label: String(t('admin.student360.aiChat') || 'AI问答'), value: 'ai' },
  { label: String(t('admin.student360.voice') || '口语训练'), value: 'voice' },
]))

const leftTitle = computed(() => tab.value === 'ai' ? String(t('admin.student360.aiConversations') || '会话列表') : String(t('admin.student360.voiceSessions') || '会话列表'))
const rightTitle = computed(() => tab.value === 'ai' ? String(t('admin.student360.aiMessages') || '消息') : String(t('admin.student360.voiceTurns') || '回合'))

const leftItems = computed(() => tab.value === 'ai' ? aiConversations.value : voiceSessions.value)

function fileUrl(fileId: string | number) {
  return buildFileUrl(`/files/${encodeURIComponent(String(fileId))}/download`)
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/admin/people')
}

async function reloadAiList() {
  const res: any = await adminApi.listAiConversations({ studentId: userId.value, q: q.value || undefined, page: 1, size: 50 })
  aiConversations.value = res?.items || []
  if (!selectedLeftId.value && aiConversations.value.length) selectedLeftId.value = String(aiConversations.value[0].id)
}

async function reloadAiMessages() {
  if (!selectedLeftId.value) { messages.value = []; return }
  const res: any = await adminApi.listAiMessages(selectedLeftId.value, { studentId: userId.value, page: 1, size: 200 })
  messages.value = res?.items || []
}

async function reloadVoiceList() {
  const res: any = await adminApi.listVoiceSessions({ studentId: userId.value, q: q.value || undefined, page: 1, size: 50 })
  voiceSessions.value = res?.items || res || []
  if (!selectedLeftId.value && voiceSessions.value.length) selectedLeftId.value = String(voiceSessions.value[0].id)
}

async function reloadVoiceTurns() {
  if (!selectedLeftId.value) { turns.value = []; return }
  const res: any = await adminApi.listVoiceTurns(selectedLeftId.value, { studentId: userId.value, page: 1, size: 500 })
  turns.value = res?.items || res || []
}

async function reload() {
  if (!userId.value) return
  loading.value = true
  error.value = null
  try {
    selectedLeftId.value = ''
    messages.value = []
    turns.value = []
    if (tab.value === 'ai') {
      await reloadAiList()
      await reloadAiMessages()
    } else {
      await reloadVoiceList()
      await reloadVoiceTurns()
    }
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    loading.value = false
  }
}

async function selectLeft(id: string) {
  selectedLeftId.value = id
  if (tab.value === 'ai') await reloadAiMessages()
  else await reloadVoiceTurns()
}

async function exportVoiceAudioZip() {
  try {
    const zip = new JSZip()
    const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
    const audioIds = Array.from(new Set((turns.value || []).flatMap((t0: any) => [t0?.userAudioFileId, t0?.assistantAudioFileId]).filter(Boolean)))
    const limited = audioIds.slice(0, 50)
    for (const fid of limited) {
      try {
        let name = `audio_${fid}`
        try {
          const info: any = await fileApi.getFileInfo(String(fid))
          const original = info?.originalName || info?.filename || info?.name
          if (original) name = String(original)
        } catch {}
        const url = fileUrl(fid)
        const res = await fetch(url, {
          method: 'GET',
          headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        })
        if (!res.ok) continue
        const blob = await res.blob()
        zip.file(`audio/${fid}_${name}`, blob)
      } catch {}
    }
    const out = await zip.generateAsync({ type: 'blob' })
    downloadBlobAsFile(out, `voice_audit_${userId.value}.zip`)
  } catch { /* ignore */ }
}

watch(tab, async (v) => {
  router.replace({ query: { ...route.query, tab: v } })
  await reload()
})
watch(q, () => router.replace({ query: { ...route.query, q: q.value || undefined } }))

onMounted(reload)
</script>

