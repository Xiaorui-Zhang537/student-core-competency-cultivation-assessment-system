<template>
  <card padding="md" tint="accent">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="text-sm font-medium">{{ t('admin.courses.tabs.reports') || '报告' }}</div>
      <div class="flex items-center gap-2">
        <Button size="sm" variant="outline" :disabled="loading" @click="reloadAll">{{ t('common.refresh') || '刷新' }}</Button>
      </div>
    </div>

    <div class="flex flex-wrap items-center gap-3 mb-3">
      <div class="flex items-center gap-2">
        <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courses.reports.assignment') || '作业' }}</span>
        <div class="w-72">
          <glass-popover-select v-model="assignmentId" :options="assignmentOptions" size="sm" tint="secondary" />
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-xs text-subtle">{{ t('common.total') || '总数' }}:</span>
        <badge size="sm" variant="secondary">{{ total }}</badge>
      </div>
    </div>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state v-else-if="error" :title="String(t('common.error') || '加载失败')" :message="error" :actionText="String(t('common.retry') || '重试')" @action="reloadAll" />

    <div v-else>
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.sidebar.students') || '学生' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.abilityReportTitle') || '报告' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.score') || '分数' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.createdAt') || '时间' }}</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courses.actions') || '操作' }}</th>
          </tr>
        </template>
        <template #body>
          <template v-for="(r, idx) in groupedReports" :key="`row-${String(r.id)}-${idx}`">
            <tr
              v-if="idx === 0 || String(groupedReports[idx - 1]?.assignmentId ?? '') !== String(r.assignmentId ?? '')"
              class="bg-white/5"
            >
              <td colspan="6" class="px-6 py-2 text-xs font-semibold text-gray-700 dark:text-gray-200">
                <span class="mr-2">{{ t('admin.courses.reports.assignment') || '作业' }}:</span>
                <span class="mr-3">{{ assignmentLabel(r.assignmentId) }}</span>
                <span class="text-subtle font-normal">
                  ({{ t('common.count') || '数量' }}: {{ assignmentCounts[String(r.assignmentId ?? '')] || 0 }})
                </span>
              </td>
            </tr>

            <tr class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ r.id }}</td>
              <td class="px-6 py-3 text-sm">
                <div class="font-medium">{{ r.studentName || `#${r.studentId}` }}</div>
                <div class="text-xs text-subtle">{{ r.studentNumber || '-' }}</div>
              </td>
              <td class="px-6 py-3">
                <div class="font-medium line-clamp-1">{{ r.title || '-' }}</div>
                <div class="text-xs text-subtle">
                  <span v-if="r.submissionId">submission #{{ r.submissionId }}</span>
                  <span v-if="r.reportType" class="ml-2">{{ r.reportType }}</span>
                </div>
              </td>
              <td class="px-6 py-3 text-center text-sm">{{ r.overallScore ?? '-' }}</td>
              <td class="px-6 py-3 text-xs text-subtle text-center whitespace-nowrap">{{ r.createdAt || '-' }}</td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 flex-wrap">
                  <Button size="sm" variant="outline" @click="openReport(r.id)">{{ t('admin.courses.reports.actions.view') || '在线查看' }}</Button>
                  <Button size="sm" variant="outline" :disabled="!r.submissionId" @click="exportSubmissionZip(r.submissionId)">
                    {{ t('admin.courses.reports.actions.exportSubmissionZip') || '导出提交ZIP' }}
                  </Button>
                </div>
              </td>
            </tr>
          </template>

          <tr v-if="reports.length === 0">
            <td colspan="6" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>

      <pagination-bar
        class="mt-2"
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; loadReports() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; loadReports() }"
      />
    </div>

    <glass-modal
      v-if="viewerOpen && selectedReport"
      :title="String(t('admin.courses.reports.viewerTitle') || '能力报告')"
      size="xl"
      height-variant="max"
      :backdrop-dark="true"
      @close="closeViewer"
    >
      <div ref="exportRoot" data-export-root="1">
        <AbilityReportViewer :report="selectedReport" />
      </div>

      <template #footer>
        <div class="flex items-center justify-between w-full gap-2 flex-wrap">
          <div class="flex items-center gap-2">
            <Button variant="outline" @click="downloadJson">{{ t('admin.courses.reports.actions.downloadJson') || '下载JSON' }}</Button>
            <Button variant="outline" @click="exportPdf">{{ t('admin.courses.reports.actions.downloadPdf') || '下载PDF' }}</Button>
            <Button variant="outline" @click="exportPng">{{ t('admin.courses.reports.actions.downloadPng') || '下载PNG' }}</Button>
            <Button variant="outline" :disabled="!selectedReport.submissionId" @click="exportSubmissionZip(selectedReport.submissionId)">
              {{ t('admin.courses.reports.actions.exportSubmissionZip') || '导出提交ZIP' }}
            </Button>
          </div>
          <Button variant="outline" @click="closeViewer">{{ t('common.close') || '关闭' }}</Button>
        </div>
      </template>
    </glass-modal>
  </card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import AbilityReportViewer from '@/shared/views/AbilityReportViewer.vue'
import { adminApi, type AbilityReport } from '@/api/admin.api'
import { assignmentApi } from '@/api/assignment.api'
import { submissionApi } from '@/api/submission.api'
import { downloadBlobAsFile } from '@/utils/download'
import { exportNodeAsPdf, exportNodeAsPng } from '@/shared/utils/exporters'

const props = withDefaults(defineProps<{ courseId: string; active?: boolean }>(), { active: true })
const emit = defineEmits<{ (e: 'counts', v: { totalReports: number }): void }>()

const { t } = useI18n()

const loading = ref(false)
const error = ref<string | null>(null)

const assignments = ref<any[]>([])
const assignmentId = ref<string>('')

const reports = ref<AbilityReport[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

const assignmentOptions = computed(() => {
  const opts = [{ label: String(t('common.all') || '全部'), value: '' }]
  for (const a of assignments.value || []) {
    const id = String(a?.id ?? '')
    if (!id) continue
    opts.push({ label: String(a?.title || `#${id}`), value: id })
  }
  return opts
})

const assignmentTitleMap = computed<Record<string, string>>(() => {
  const m: Record<string, string> = {}
  for (const a of assignments.value || []) {
    const id = String(a?.id ?? '')
    if (!id) continue
    m[id] = String(a?.title || `#${id}`)
  }
  return m
})

function assignmentLabel(id: any): string {
  const key = String(id ?? '')
  if (!key) return String(t('admin.courses.reports.unlinked') || '未关联作业')
  return assignmentTitleMap.value[key] || `#${key}`
}

const assignmentCounts = computed<Record<string, number>>(() => {
  const m: Record<string, number> = {}
  for (const r of reports.value || []) {
    const key = String((r as any)?.assignmentId ?? '')
    m[key] = (m[key] || 0) + 1
  }
  return m
})

const groupedReports = computed(() => {
  const list = Array.isArray(reports.value) ? [...reports.value] : []
  const aid = (r: any) => String(r?.assignmentId ?? '')
  const time = (r: any) => {
    const v = String(r?.createdAt ?? '')
    // createdAt 通常为 ISO：按字典序即可大致降序（yyyy-MM-ddTHH:mm:ss）
    return v
  }
  list.sort((a: any, b: any) => {
    const ka = aid(a)
    const kb = aid(b)
    if (ka !== kb) return ka.localeCompare(kb)
    return time(b).localeCompare(time(a))
  })
  return list
})

async function loadAssignments() {
  try {
    const res: any = await assignmentApi.getAssignmentsByCourse(props.courseId, { page: 1, size: 200 } as any)
    const items = res?.items || res?.data?.items || res || []
    assignments.value = Array.isArray(items) ? items : (items?.items || [])
  } catch {
    assignments.value = []
  }
}

async function loadReports() {
  loading.value = true
  error.value = null
  try {
    const res: any = await adminApi.pageAbilityReports({
      page: page.value,
      size: pageSize.value,
      courseId: Number(props.courseId),
      assignmentId: assignmentId.value ? Number(assignmentId.value) : undefined,
    })
    reports.value = res?.items || []
    total.value = Number(res?.total || 0)
    totalPages.value = Number(res?.totalPages || Math.max(1, Math.ceil(total.value / Math.max(1, pageSize.value))))
    emit('counts', { totalReports: total.value })
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function reloadAll() {
  loading.value = true
  error.value = null
  try {
    await loadAssignments()
    await loadReports()
  } finally {
    loading.value = false
  }
}

watch(assignmentId, () => {
  page.value = 1
  loadReports()
})

// viewer
const viewerOpen = ref(false)
const selectedReport = ref<AbilityReport | null>(null)
const exportRoot = ref<HTMLElement | null>(null)

async function openReport(id: string | number) {
  viewerOpen.value = true
  selectedReport.value = null
  try {
    const full: any = await adminApi.getAbilityReport(id)
    selectedReport.value = full?.data || full
  } catch (e: any) {
    selectedReport.value = { id: Number(id), studentId: 0, reportType: '', title: e?.message || 'Failed', overallScore: '', dimensionScores: '', recommendations: '' } as any
  }
}

function closeViewer() {
  viewerOpen.value = false
  selectedReport.value = null
}

function downloadJson() {
  if (!selectedReport.value) return
  const text = JSON.stringify(selectedReport.value, null, 2)
  downloadBlobAsFile(text, `ability_report_${selectedReport.value.id}.json`, 'application/json;charset=utf-8')
}

async function exportPdf() {
  if (!exportRoot.value) return
  await exportNodeAsPdf(exportRoot.value, `ability_report_${selectedReport.value?.id || 'report'}`)
}

async function exportPng() {
  if (!exportRoot.value) return
  await exportNodeAsPng(exportRoot.value, `ability_report_${selectedReport.value?.id || 'report'}`)
}

async function exportSubmissionZip(submissionId?: number | string) {
  const sid = String(submissionId || '')
  if (!sid) return
  const blob = await submissionApi.exportSubmission(sid)
  downloadBlobAsFile(blob as any, `submission_${sid}.zip`)
}

watch(
  () => props.active,
  (v) => {
    if (v) reloadAll()
  },
  { immediate: true }
)

onMounted(() => {
  if (props.active !== false) reloadAll()
})
</script>

