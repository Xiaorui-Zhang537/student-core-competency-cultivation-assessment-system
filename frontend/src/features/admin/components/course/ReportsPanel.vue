<template>
  <card padding="md" tint="accent">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="text-sm font-medium">{{ t('admin.courses.tabs.reports') || '报告' }}</div>
      <div class="flex items-center gap-2">
        <Button size="sm" variant="info" :disabled="loading" @click="reloadAll">
          <template #icon>
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992V4.356m-1.333 1.332A9 9 0 1 0 21 12h-2.25" />
            </svg>
          </template>
          {{ t('common.refresh') || '刷新' }}
        </Button>
      </div>
    </div>

    <filter-bar tint="secondary" align="center" :dense="false" class="mb-3 rounded-full h-19">
      <template #left>
        <div class="w-auto flex items-center gap-2">
          <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courses.reports.assignment') || '作业' }}</span>
          <div class="w-72">
            <glass-popover-select v-model="assignmentId" :options="assignmentOptions" size="sm" tint="secondary" />
          </div>
        </div>
      </template>
      <template #right>
        <div class="w-80">
          <glass-search-input
            v-model="search"
            :placeholder="String(t('admin.courses.reports.searchPh') || '搜索学生姓名/学号')"
            size="sm"
            tint="info"
          />
        </div>
      </template>
    </filter-bar>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state v-else-if="error" :title="String(t('common.error') || '加载失败')" :message="error" :actionText="String(t('common.retry') || '重试')" @action="reloadAll" />

    <div v-else>
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.sidebar.students') || '学生' }}</th>
            <th class="px-6 py-3 text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
              <div class="w-full text-center pl-2">{{ t('admin.moderation.abilityReportTitle') || '报告' }}</div>
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.score') || '分数' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.createdAt') || '时间' }}</th>
            <th class="px-6 py-3 text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              <div class="w-full text-center pr-2">{{ t('admin.courses.actions') || '操作' }}</div>
            </th>
          </tr>
        </template>
        <template #body>
          <template v-for="(g, idx) in groupedReportGroups" :key="`group-${g.key}-${idx}`">
            <tr
              v-if="idx === 0 || String(groupedReportGroups[idx - 1]?.assignmentId ?? '') !== String(g.assignmentId ?? '')"
              class="bg-white/5"
            >
              <td colspan="6" class="px-6 py-2 text-xs font-semibold text-gray-700 dark:text-gray-200">
                <span class="mr-2">{{ t('admin.courses.reports.assignment') || '作业' }}:</span>
                <span class="mr-3">{{ assignmentLabel(g.assignmentId) }}</span>
                <span class="text-subtle font-normal">
                  ({{ t('common.count') || '数量' }}: {{ assignmentCounts[String(g.assignmentId ?? '')] || 0 }})
                </span>
              </td>
            </tr>

            <tr class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ g.latest.id }}</td>
              <td class="px-6 py-3 text-sm">
                <div class="font-medium">{{ g.latest.studentName || `#${g.latest.studentId}` }}</div>
                <div class="text-xs text-subtle">{{ g.latest.studentNumber || '-' }}</div>
              </td>
              <td class="px-6 py-3">
                <div class="font-medium line-clamp-1">{{ g.latest.title || '-' }}</div>
                <div class="text-xs text-subtle flex items-center gap-2 flex-wrap">
                  <span>{{ assignmentTitle(g.latest) }}</span>
                  <span v-if="g.latest.reportType">{{ g.latest.reportType }}</span>
                  <button
                    v-if="g.history.length > 0"
                    type="button"
                    class="text-[11px] underline decoration-dotted underline-offset-2 hover:opacity-80"
                    @click="toggleHistory(g.key)"
                  >
                    {{ historyExpanded[g.key] ? (t('admin.courses.reports.actions.collapseHistory') || '收起历史') : `${t('admin.courses.reports.actions.viewHistory') || '查看历史'} (${g.history.length})` }}
                  </button>
                </div>
              </td>
              <td class="px-6 py-3 text-center text-sm">{{ g.latest.overallScore ?? '-' }}</td>
              <td class="px-6 py-3 text-xs text-subtle text-center whitespace-nowrap">{{ g.latest.createdAt || '-' }}</td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 flex-wrap">
                  <Button size="sm" variant="info" @click="openReport(g.latest.id)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12s3.75-6.75 9.75-6.75S21.75 12 21.75 12 18 18.75 12 18.75 2.25 12 2.25 12Z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    </template>
                    {{ t('admin.courses.reports.actions.view') || '在线查看' }}
                  </Button>
                  <Button size="sm" variant="warning" :disabled="!g.latest.submissionId" @click="exportSubmissionZip(g.latest.submissionId)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 8.5V18a2 2 0 0 1-2 2H5.75a2 2 0 0 1-2-2V8.5m17-3.5H3.25m9.5 0v4m-3-2h6" />
                      </svg>
                    </template>
                    {{ t('admin.courses.reports.actions.exportSubmissionZip') || '导出提交ZIP' }}
                  </Button>
                </div>
              </td>
            </tr>

            <tr v-for="(h, hIdx) in visibleHistory(g)" :key="`history-${g.key}-${String(h.id)}-${hIdx}`" class="bg-white/5 hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ h.id }}</td>
              <td class="px-6 py-3 text-sm text-subtle">
                <span class="pl-4">{{ t('admin.courses.reports.historyLabel') || '历史记录' }}</span>
              </td>
              <td class="px-6 py-3">
                <div class="pl-4 border-l border-white/20">
                  <div class="font-medium line-clamp-1">{{ h.title || '-' }}</div>
                  <div class="text-xs text-subtle">{{ assignmentTitle(h) }}</div>
                </div>
              </td>
              <td class="px-6 py-3 text-center text-sm">{{ h.overallScore ?? '-' }}</td>
              <td class="px-6 py-3 text-xs text-subtle text-center whitespace-nowrap">{{ h.createdAt || '-' }}</td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 flex-wrap">
                  <Button size="sm" variant="info" @click="openReport(h.id)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12s3.75-6.75 9.75-6.75S21.75 12 21.75 12 18 18.75 12 18.75 2.25 12 2.25 12Z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    </template>
                    {{ t('admin.courses.reports.actions.view') || '在线查看' }}
                  </Button>
                  <Button size="sm" variant="warning" :disabled="!h.submissionId" @click="exportSubmissionZip(h.submissionId)">
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 8.5V18a2 2 0 0 1-2 2H5.75a2 2 0 0 1-2-2V8.5m17-3.5H3.25m9.5 0v4m-3-2h6" />
                      </svg>
                    </template>
                    {{ t('admin.courses.reports.actions.exportSubmissionZip') || '导出提交ZIP' }}
                  </Button>
                </div>
              </td>
            </tr>
          </template>

          <tr v-if="groupedReportGroups.length === 0">
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

    <Teleport to="body">
      <glass-modal
        v-if="viewerOpen && selectedReport"
        :title="String((selectedReport as any)?.assignmentTitle || t('teacher.aiGrading.viewDetail') || 'AI 能力报告')"
        size="xl"
        :hideScrollbar="true"
        height-variant="max"
        :backdrop-dark="false"
        @close="closeViewer"
      >
        <div ref="exportRoot" data-export-root="1">
          <AbilityReportDetailContent :report="selectedReport" />
        </div>

        <template #footer>
          <Button size="sm" variant="primary" @click="exportAiDetailAsText" :disabled="!parsedSelectedAi">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 21H5a2 2 0 0 1-2-2V7"></path>
                <path d="M9 7V3h6v4"></path>
                <path d="M9 15h6"></path>
              </svg>
            </template>
            {{ t('teacher.aiGrading.exportText') || '导出文本' }}
          </Button>
          <Button size="sm" variant="success" @click="exportPng" :disabled="!parsedSelectedAi">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="14" rx="2" ry="2"></rect>
                <path d="M8 21h8"></path>
                <path d="M12 17v4"></path>
              </svg>
            </template>
            {{ t('teacher.aiGrading.exportPng') || '导出 PNG' }}
          </Button>
          <Button size="sm" variant="purple" @click="exportPdf" :disabled="!parsedSelectedAi">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"></path>
                <path d="M14 2v6h6"></path>
              </svg>
            </template>
            {{ t('teacher.aiGrading.exportPdf') || '导出 PDF' }}
          </Button>
          <Button size="sm" variant="secondary" @click="closeViewer">
            <template #icon>
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </template>
            {{ t('teacher.aiGrading.picker.close') || '关闭' }}
          </Button>
        </template>
      </glass-modal>
    </Teleport>
  </card>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import AbilityReportDetailContent from '@/shared/views/AbilityReportDetailContent.vue'
import { adminApi, type AbilityReport } from '@/api/admin.api'
import { assignmentApi } from '@/api/assignment.api'
import { submissionApi } from '@/api/submission.api'
import { downloadBlobAsFile } from '@/utils/download'
import { exportNodeAsPdf, exportNodeAsPng, applyExportGradientsInline } from '@/shared/utils/exporters'
import { parseAbilityReport } from '@/shared/utils/abilityReportData'
import { buildAbilityReportText } from '@/shared/utils/abilityReportExport'

const props = withDefaults(defineProps<{ courseId: string; active?: boolean }>(), { active: true })
const emit = defineEmits<{ (e: 'counts', v: { totalReports: number }): void }>()

const { t } = useI18n()

const loading = ref(false)
const error = ref<string | null>(null)

const assignments = ref<any[]>([])
const assignmentId = ref<string>('')
const search = ref('')

const reports = ref<AbilityReport[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const historyExpanded = ref<Record<string, boolean>>({})
let searchTimer: ReturnType<typeof setTimeout> | null = null

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

type ReportGroup = {
  key: string
  assignmentId?: number
  latest: AbilityReport
  history: AbilityReport[]
}

const groupedReportGroups = computed<ReportGroup[]>(() => {
  const list = Array.isArray(reports.value) ? [...reports.value] : []
  const toTime = (r: AbilityReport) => {
    const raw = String(r?.createdAt ?? '')
    const num = Date.parse(raw)
    return Number.isNaN(num) ? 0 : num
  }
  list.sort((a, b) => toTime(b) - toTime(a))

  const groupsMap = new Map<string, AbilityReport[]>()
  for (const r of list) {
    const groupKey = `${String(r.studentId || '')}::${String(r.assignmentId || '')}`
    if (!groupsMap.has(groupKey)) groupsMap.set(groupKey, [])
    groupsMap.get(groupKey)!.push(r)
  }

  const groups: ReportGroup[] = []
  for (const [key, values] of groupsMap.entries()) {
    const sorted = [...values].sort((a, b) => toTime(b) - toTime(a))
    if (sorted.length === 0) continue
    groups.push({
      key,
      assignmentId: sorted[0].assignmentId,
      latest: sorted[0],
      history: sorted.slice(1),
    })
  }

  groups.sort((a, b) => {
    const aAssignment = String(a.assignmentId ?? '')
    const bAssignment = String(b.assignmentId ?? '')
    if (aAssignment !== bAssignment) return aAssignment.localeCompare(bAssignment)
    return toTime(b.latest) - toTime(a.latest)
  })

  return groups
})

function assignmentTitle(r: AbilityReport): string {
  return String(r.assignmentTitle || assignmentLabel(r.assignmentId) || '-')
}

function toggleHistory(key: string) {
  historyExpanded.value = {
    ...historyExpanded.value,
    [key]: !historyExpanded.value[key],
  }
}

function visibleHistory(group: ReportGroup): AbilityReport[] {
  return historyExpanded.value[group.key] ? group.history : []
}

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
    const kw = String(search.value || '').trim()
    const res: any = await adminApi.pageAbilityReports({
      page: page.value,
      size: pageSize.value,
      courseId: Number(props.courseId),
      assignmentId: assignmentId.value ? Number(assignmentId.value) : undefined,
      search: kw || undefined,
    })
    reports.value = res?.items || []
    historyExpanded.value = {}
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

watch(search, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadReports()
  }, 350)
})

// viewer
const viewerOpen = ref(false)
const selectedReport = ref<AbilityReport | null>(null)
const exportRoot = ref<HTMLElement | null>(null)
const parsedSelectedAi = computed(() => parseAbilityReport(selectedReport.value))

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

async function exportPdf() {
  if (!exportRoot.value) return
  const node = exportRoot.value
  const cloned = node.cloneNode(true) as HTMLElement
  const wrapper = document.createElement('div')
  wrapper.style.position = 'fixed'
  wrapper.style.left = '-99999px'
  wrapper.style.top = '0'
  wrapper.style.width = `${node.clientWidth}px`
  wrapper.style.background = '#ffffff'
  cloned.style.maxHeight = 'none'
  cloned.style.overflow = 'visible'
  applyExportGradientsInline(cloned)
  wrapper.appendChild(cloned)
  document.body.appendChild(wrapper)
  const fileBase = (String(selectedReport.value?.title || `ability_report_${selectedReport.value?.id || 'report'}`)).replace(/\s+/g, '_')
  await exportNodeAsPdf(cloned, fileBase)
  document.body.removeChild(wrapper)
}

async function exportPng() {
  if (!exportRoot.value) return
  applyExportGradientsInline(exportRoot.value)
  const fileBase = (String(selectedReport.value?.title || `ability_report_${selectedReport.value?.id || 'report'}`)).replace(/\s+/g, '_')
  await exportNodeAsPng(exportRoot.value, fileBase)
}

async function exportSubmissionZip(submissionId?: number | string) {
  const sid = String(submissionId || '')
  if (!sid) return
  const blob = await submissionApi.exportSubmission(sid)
  downloadBlobAsFile(blob as any, `submission_${sid}.zip`)
}

function parseSelectedAi(): any | null {
  return parsedSelectedAi.value
}

function exportAiDetailAsText() {
  const res: any = parseSelectedAi()
  if (!res) return
  const text = buildAbilityReportText(res)
  const fileBase = (String(selectedReport.value?.title || `ability_report_${selectedReport.value?.id || 'report'}`)).replace(/\s+/g, '_')
  downloadBlobAsFile(text, `${fileBase}.txt`, 'text/plain;charset=utf-8')
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

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer)
})
</script>

