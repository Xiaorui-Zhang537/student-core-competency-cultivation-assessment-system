<template>
  <div class="p-6">
    <page-header :title="t('admin.sidebar.tools')" :subtitle="t('admin.title')" />

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="init"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <card padding="md" tint="secondary" class="lg:col-span-3">
        <div class="text-sm font-medium mb-2">{{ t('admin.tools.capabilitiesTitle') || '导出能力' }}</div>
        <div class="text-xs text-subtle">
          {{ t('admin.tools.capabilitiesSubtitle') || '首期为同步导出 CSV；建议控制筛选范围，避免超过上限' }}
        </div>
        <div class="text-xs text-subtle mt-2">
          Max rows: {{ caps?.maxRows ?? '-' }} · Formats: {{ (caps?.formats || []).join(', ') }}
        </div>
      </card>

      <card padding="md" tint="secondary" class="lg:col-span-3">
        <div class="text-sm font-medium mb-2">{{ t('admin.tools.broadcastTitle') || '全局通知' }}</div>
        <div class="text-xs text-subtle mb-4">{{ t('admin.tools.broadcastDesc') || '面向全体/分角色/指定用户群发系统通知（按用户写入通知中心）。' }}</div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div class="lg:col-span-2 space-y-3">
            <div>
              <div class="text-xs text-subtle mb-1">{{ t('admin.tools.broadcast.title') || '标题' }}</div>
              <glass-input v-model="broadcastTitle" :placeholder="String(t('admin.tools.broadcast.title') || '标题')" />
            </div>
            <div>
              <div class="text-xs text-subtle mb-1">{{ t('admin.tools.broadcast.content') || '内容' }}</div>
              <glass-textarea v-model="broadcastContent" :rows="4" :placeholder="String(t('admin.tools.broadcast.content') || '内容')" />
            </div>
          </div>

          <div class="space-y-3">
            <div>
              <div class="text-xs text-subtle mb-1">{{ t('admin.tools.broadcast.target') || '发送对象' }}</div>
              <glass-popover-select v-model="broadcastTargetType" :options="broadcastTargetOptions" size="sm" />
            </div>

            <div v-if="broadcastTargetType === 'role'">
              <div class="text-xs text-subtle mb-1">{{ t('admin.tools.broadcast.role') || '角色' }}</div>
              <glass-popover-select v-model="broadcastRole" :options="broadcastRoleOptions" size="sm" />
            </div>

            <div v-if="broadcastTargetType === 'specific'">
              <div class="text-xs text-subtle mb-1">{{ t('admin.tools.broadcast.specificIds') || '用户ID（逗号分隔）' }}</div>
              <glass-input v-model="broadcastSpecificIds" :placeholder="String(t('admin.tools.broadcast.specificIds') || '例如：1,2,3')" />
            </div>

            <Button size="sm" variant="primary" :disabled="exporting || !canSendBroadcast" @click="sendBroadcast">
              {{ t('admin.tools.broadcast.send') || '发送' }}
            </Button>
          </div>
        </div>
      </card>

      <card padding="md" tint="info">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.usersCsvTitle') || '用户 CSV' }}</div>
        <div class="text-xs text-subtle mb-3">{{ t('admin.tools.usersCsvDesc') || '导出用户列表（/admin/users）为 CSV。' }}</div>
        <div class="flex gap-2">
          <Button size="sm" variant="primary" :disabled="exporting" @click="exportUsers">
            {{ t('admin.tools.download') || '下载' }}
          </Button>
          <Button size="sm" variant="outline" :disabled="exporting" @click="router.push('/admin/people?tab=users')">
            {{ t('admin.tools.openFilters') || '去筛选' }}
          </Button>
        </div>
      </card>

      <card padding="md" tint="accent">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.abilityCsvTitle') || '能力报告 CSV' }}</div>
        <div class="text-xs text-subtle mb-3">{{ t('admin.tools.abilityCsvDesc') || '导出能力报告中心列表为 CSV。' }}</div>
        <div class="flex gap-2">
          <Button size="sm" variant="primary" :disabled="exporting" @click="exportAbilityReports">
            {{ t('admin.tools.download') || '下载' }}
          </Button>
          <Button size="sm" variant="outline" :disabled="exporting" @click="router.push('/admin/moderation?tab=ability')">
            {{ t('admin.tools.openFilters') || '去筛选' }}
          </Button>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.courseStudentsCsvTitle') || '课程学生 CSV' }}</div>
        <div class="text-xs text-subtle mb-3">{{ t('admin.tools.courseStudentsCsvDesc') || '按 courseId 导出该课程学生名单。' }}</div>
        <div class="flex items-center gap-2 mb-3">
          <div class="flex-1">
            <glass-search-input v-model="courseIdForExport" :placeholder="String(t('admin.tools.courseIdPlaceholder') || '输入 courseId')" size="sm" tint="info" />
          </div>
          <Button size="sm" variant="primary" :disabled="exporting || !courseIdForExport.trim()" @click="exportCourseStudents">
            {{ t('admin.tools.download') || '下载' }}
          </Button>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.aiConversationsCsvTitle') || 'AI 会话索引 CSV' }}</div>
        <div class="text-xs text-subtle mb-3">{{ t('admin.tools.aiConversationsCsvDesc') || '按 studentId 导出 AI 会话索引。' }}</div>
        <div class="flex items-center gap-2 mb-3">
          <div class="flex-1">
            <glass-search-input v-model="studentIdForExport" :placeholder="String(t('admin.tools.studentIdPlaceholder') || '输入 studentId')" size="sm" tint="info" />
          </div>
          <Button size="sm" variant="primary" :disabled="exporting || !studentIdForExport.trim()" @click="exportAiConversations">
            {{ t('admin.tools.download') || '下载' }}
          </Button>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.voiceSessionsCsvTitle') || '口语会话索引 CSV' }}</div>
        <div class="text-xs text-subtle mb-3">{{ t('admin.tools.voiceSessionsCsvDesc') || '按 studentId 导出口语训练会话索引。' }}</div>
        <div class="flex items-center gap-2 mb-3">
          <div class="flex-1">
            <glass-search-input v-model="studentIdForVoiceExport" :placeholder="String(t('admin.tools.studentIdPlaceholder') || '输入 studentId')" size="sm" tint="info" />
          </div>
          <Button size="sm" variant="primary" :disabled="exporting || !studentIdForVoiceExport.trim()" @click="exportVoiceSessions">
            {{ t('admin.tools.download') || '下载' }}
          </Button>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">{{ t('admin.tools.tipsTitle') || '使用建议' }}</div>
        <ul class="text-xs text-subtle space-y-2 list-disc pl-4">
          <li>{{ t('admin.tools.tip1') || '导出前先在“数据中心/治理台”缩小筛选范围，下载会更快。' }}</li>
          <li>{{ t('admin.tools.tip2') || '若需更大规模导出，后续可升级为“导出任务 + 异步生成”。' }}</li>
        </ul>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { adminApi } from '@/api/admin.api'
import { notificationAPI } from '@/api/notification.api'
import { downloadCsv } from '@/utils/download'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const router = useRouter()
const ui = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)
const caps = ref<any>(null)
const exporting = ref(false)

const courseIdForExport = ref('')
const studentIdForExport = ref('')
const studentIdForVoiceExport = ref('')

const broadcastTitle = ref('')
const broadcastContent = ref('')
const broadcastTargetType = ref<'all' | 'role' | 'specific'>('all')
const broadcastRole = ref<'student' | 'teacher' | 'admin'>('student')
const broadcastSpecificIds = ref('')

const broadcastTargetOptions = [
  { label: String(t('admin.tools.broadcastTargets.all') || '全体用户'), value: 'all' },
  { label: String(t('admin.tools.broadcastTargets.role') || '按角色'), value: 'role' },
  { label: String(t('admin.tools.broadcastTargets.specific') || '指定用户'), value: 'specific' },
]
const broadcastRoleOptions = [
  { label: String(t('admin.roles.student') || '学生'), value: 'student' },
  { label: String(t('admin.roles.teacher') || '教师'), value: 'teacher' },
  { label: String(t('admin.roles.admin') || '管理员'), value: 'admin' },
]

const canSendBroadcast = computed(() => {
  if (!broadcastTitle.value.trim() || !broadcastContent.value.trim()) return false
  if (broadcastTargetType.value === 'specific') return Boolean(broadcastSpecificIds.value.trim())
  if (broadcastTargetType.value === 'role') return Boolean(broadcastRole.value)
  return true
})

async function sendBroadcast() {
  exporting.value = true
  try {
    const targetIds = broadcastTargetType.value === 'specific'
      ? broadcastSpecificIds.value
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
        .map(v => Number(v))
        .filter(n => Number.isFinite(n))
      : undefined

    const res: any = await notificationAPI.adminBroadcast({
      title: broadcastTitle.value.trim(),
      content: broadcastContent.value.trim(),
      type: 'system',
      category: 'systemAnnouncement',
      priority: 'normal',
      targetType: broadcastTargetType.value,
      role: broadcastTargetType.value === 'role' ? broadcastRole.value : undefined,
      targetIds: broadcastTargetType.value === 'specific' ? targetIds : undefined,
    })

    ui.showNotification({
      type: 'success',
      title: 'OK',
      message: `success=${res?.successCount ?? '-'} fail=${res?.failCount ?? '-'}`
    })
    broadcastTitle.value = ''
    broadcastContent.value = ''
    broadcastSpecificIds.value = ''
    broadcastTargetType.value = 'all'
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function init() {
  loading.value = true
  error.value = null
  try {
    caps.value = await adminApi.getExportCapabilities()
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function exportUsers() {
  exporting.value = true
  try {
    const blob = await adminApi.exportUsersCsv({})
    downloadCsv(blob, 'users.csv')
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function exportAbilityReports() {
  exporting.value = true
  try {
    const blob = await adminApi.exportAbilityReportsCsv({})
    downloadCsv(blob, 'ability_reports.csv')
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function exportCourseStudents() {
  exporting.value = true
  try {
    const blob = await adminApi.exportCourseStudentsCsv({ courseId: courseIdForExport.value.trim() })
    downloadCsv(blob, `course_${courseIdForExport.value.trim()}_students.csv`)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function exportAiConversations() {
  exporting.value = true
  try {
    const sid = studentIdForExport.value.trim()
    const blob = await adminApi.exportAiConversationsCsv({ studentId: sid })
    downloadCsv(blob, `student_${sid}_ai_conversations.csv`)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function exportVoiceSessions() {
  exporting.value = true
  try {
    const sid = studentIdForVoiceExport.value.trim()
    const blob = await adminApi.exportVoiceSessionsCsv({ studentId: sid })
    downloadCsv(blob, `student_${sid}_voice_sessions.csv`)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

onMounted(init)
</script>

