<template>
  <div class="min-h-screen p-6">
    <!-- Breadcrumb -->
    <nav class="text-sm text-gray-600 dark:text-gray-300 mb-2 flex items-center gap-1">
      <a href="javascript:void(0)" class="hover:underline" @click.prevent="goCourses">{{ t('teacher.courses.breadcrumb') || '课程管理' }}</a>
      <template v-if="courseTitle">
        <chevron-right-icon class="w-4 h-4 opacity-70" />
        <a href="javascript:void(0)" class="hover:underline" @click.prevent="goCourseDetail">{{ courseTitle }}</a>
      </template>
      <chevron-right-icon class="w-4 h-4 opacity-70" />
      <a href="javascript:void(0)" class="hover:underline" @click.prevent="goAssignments">{{ t('teacher.assignments.breadcrumb.self') || '作业管理' }}</a>
      <chevron-right-icon class="w-4 h-4 opacity-70" />
      <a href="javascript:void(0)" class="hover:underline" @click.prevent="goBack">{{ t('teacher.aiGrading.title') || 'AI 批改作业' }}</a>
      <chevron-right-icon class="w-4 h-4 opacity-70" />
      <span class="opacity-80">{{ t('teacher.aiGrading.historyTitle') || 'AI 批改历史' }}</span>
    </nav>
    <div class="max-w-7xl mx-auto">
      <page-header :title="t('teacher.aiGrading.historyTitle') || 'AI 批改历史'" :subtitle="t('teacher.aiGrading.historySubtitle') || '查看过往批改记录'">
        <template #actions>
          <div class="flex items-center gap-2">
            <glass-search-input v-model="q" :placeholder="t('teacher.aiGrading.historySearchPlaceholder') || (t('common.search') as string) || '搜索'" size="sm" class="w-64" />
            <button size="sm" variant="primary" class="w-auto px-3 whitespace-nowrap shrink-0" @click="load"><magnifying-glass-icon class="w-4 h-4 mr-1" />{{ t('common.search') || '搜索' }}</button>
          </div>
        </template>
      </page-header>

      <div class="card p-4" v-glass="{ strength: 'ultraThin', interactive: true }">
        <div class="overflow-x-auto">
          <table class="min-w-full text-sm glass-interactive bg-white/15 dark:bg-gray-800/15 rounded-lg">
            <thead>
              <tr class="text-left text-gray-600 dark:text-gray-300">
                <th class="py-2 pr-4">{{ t('teacher.aiGrading.historyTable.fileName') || '文件名' }}</th>
                <th class="py-2 pr-4">{{ t('teacher.aiGrading.historyTable.model') || '模型' }}</th>
                <th class="py-2 pr-4">{{ t('teacher.aiGrading.historyTable.finalScore') || '最终分' }}</th>
                <th class="py-2 pr-4">{{ t('teacher.aiGrading.historyTable.time') || '时间' }}</th>
                <th class="py-2 pr-4">{{ t('teacher.aiGrading.historyTable.actions') || '操作' }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in items" :key="it.id" class="border-t border-white/10">
                <td class="py-2 pr-4 truncate max-w-[240px]" :title="it.fileName">{{ it.fileName || '-' }}</td>
                <td class="py-2 pr-4">{{ it.model || '-' }}</td>
                <td class="py-2 pr-4">
                  <div v-if="hasScore(it)" class="flex items-center gap-2 w-40">
                    <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                    <div class="h-full" data-gradient="overall" :style="{ width: (resolveFinalScore(it)*20)+'%' }"></div>
                    </div>
                    <span class="text-xs text-gray-700 dark:text-gray-300">{{ resolveFinalScore(it).toFixed(1) }}</span>
                  </div>
                  <span v-else>-</span>
                </td>
                <td class="py-2 pr-4">{{ formatTime(it.createdAt) }}</td>
                <td class="py-2 pr-4">
                  <div class="flex items-center gap-2">
                    <button size="xs" variant="indigo" @click="openDetail(it)"><eye-icon class="w-4 h-4 mr-1" />{{ t('common.view') || t('teacher.aiGrading.view') || '查看' }}</button>
                    <button size="xs" variant="danger" @click="confirmDelete(it)"><trash-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.delete') || '删除' }}</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <pagination-bar
          :page="page"
          :page-size="size"
          :total-items="total"
          :button-size="'xs'"
          :button-variant="'ghost'"
          :select-size="'sm'"
          :select-width="'80px'"
          :page-size-options="[10, 20, 50]"
          :show-total-pages="true"
          @update:page="onPageChange"
          @update:pageSize="onPageSizeChange"
        />
      </div>
    </div>

    <confirm-dialog
      :open="confirmOpen"
      :title="confirmDialog.state.title"
      :message="confirmDialog.state.message"
      :confirm-text="confirmDialog.state.confirmText || ((t('common.confirm') as string) || '确定')"
      :cancel-text="confirmDialog.state.cancelText || ((t('common.cancel') as string) || '取消')"
      :confirm-variant="confirmDialog.state.confirmVariant"
      @confirm="confirmDialog.onConfirm"
      @cancel="confirmDialog.onCancel"
    />

    <glass-modal v-if="detail" :title="detail.fileName || '记录'" size="xl" :hideScrollbar="true" heightVariant="max" @close="detail=null">
      <div v-if="parsed" ref="detailRef" data-export-root="1" class="space-y-4">
        <card padding="sm" tint="secondary">
          <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.overall') }}</h4>
          <div>
            <div class="text-sm mb-2 flex items-center gap-3" v-if="getOverall(parsed)?.final_score != null">
              <span>{{ t('teacher.aiGrading.render.final_score') }}: {{ overallScore(parsed) }}</span>
              <div class="h-2 w-64 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                <div class="h-full" data-gradient="overall" :style="{ width: (Number(overallScore(parsed))*20 || 0) + '%' }"></div>
              </div>
            </div>
            <div class="space-y-2 mb-2" v-if="dimensionBars(parsed)">
              <div class="text-sm font-medium">{{ t('teacher.aiGrading.render.dimension_averages') }}</div>
              <div v-for="row in dimensionBars(parsed)" :key="row.key" class="flex items-center gap-3">
                <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}: {{ row.value }}</div>
                <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                  <div class="h-full" :data-gradient="dimGradient(row.key)" :style="{ width: (row.value*20 || 0) + '%' }"></div>
                </div>
              </div>
            </div>
            <div class="text-sm whitespace-pre-wrap">{{ t('teacher.aiGrading.render.holistic_feedback') }}: {{ overallFeedback(parsed) || (t('common.empty') || '无内容') }}</div>
          </div>
        </card>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <card padding="sm" tint="warning" v-if="parsed?.moral_reasoning">
          <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.moral_reasoning') }}</h4>
          <div v-html="renderCriterion(parsed.moral_reasoning, 'dimension_moral')"></div>
        </card>
        <card padding="sm" tint="accent" v-if="parsed?.attitude_development">
          <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.attitude_development') }}</h4>
          <div v-html="renderCriterion(parsed.attitude_development, 'dimension_attitude')"></div>
        </card>
        <card padding="sm" tint="info" v-if="parsed?.ability_growth">
          <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.ability_growth') }}</h4>
          <div v-html="renderCriterion(parsed.ability_growth, 'dimension_ability')"></div>
        </card>
        <card padding="sm" tint="success" v-if="parsed?.strategy_optimization">
          <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.strategy_optimization') }}</h4>
          <div v-html="renderCriterion(parsed.strategy_optimization, 'dimension_strategy')"></div>
        </card>
        </div>
      </div>
      <pre v-else class="bg-black/70 text-green-100 p-3 rounded overflow-auto text-xs max-h-[60vh]">{{ pretty(detail?.rawJson) }}</pre>
      <template #footer>
        <button size="sm" variant="primary" @click="exportDetailAsText" :disabled="!parsed"><arrow-down-tray-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.exportText') || '导出文本' }}</button>
        <button size="sm" variant="success" @click="exportDetailAsPng" :disabled="!parsed"><arrow-down-tray-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.exportPng') || '导出 PNG' }}</button>
        <button size="sm" variant="purple" @click="exportDetailAsPdf" :disabled="!parsed"><arrow-down-tray-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.exportPdf') || '导出 PDF' }}</button>
        <button size="sm" variant="secondary" @click="detail=null"><x-mark-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.close') || '关闭' }}</button>
      </template>
    </glass-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { exportNodeAsPng, exportNodeAsPdf, applyExportGradientsInline } from '@/shared/utils/exporters'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import { usePagination } from '@/shared/composables/usePagination'
import { aiGradingApi } from '@/api/aiGrading.api'
// 引入批改页的归一化与渲染逻辑（直接内嵌一份必要函数，避免循环依赖）
import { XMarkIcon, ArrowDownTrayIcon, MagnifyingGlassIcon, EyeIcon, ChevronLeftIcon, ChevronRightIcon, TrashIcon } from '@heroicons/vue/24/outline'
import GlassModal from '@/components/ui/GlassModal.vue'
import { useRouter } from 'vue-router'
import { courseApi } from '@/api/course.api'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { useConfirmDialog } from '@/shared/composables/useConfirmDialog'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const uiStore = useUIStore()
const router = useRouter()
const courseId = ref<string>('')
const courseTitle = ref<string>('')

const q = ref('')
const total = ref(0)
const { page, pageSize: size } = usePagination(total, { initialPage: 1, initialPageSize: 20 })
const items = ref<any[]>([])
const detail = ref<any|null>(null)
const deleting = ref(false)
const detailRef = ref<HTMLElement | null>(null)

const confirmDialog = useConfirmDialog({
  confirmText: (t('common.confirm') as string) || '确定',
  cancelText: (t('common.cancel') as string) || '取消',
  confirmVariant: 'danger',
})
const confirmOpen = computed(() => confirmDialog.open.value)
const parsed = computed(() => {
  try {
    if (!detail.value?.rawJson) return null
    const raw = String(detail.value.rawJson)
    const obj = safeJsonParseLoose(raw)
    return normalizeAssessment(obj)
  } catch { return null }
})

const load = async () => {
  const resp: any = await aiGradingApi.listHistory({ q: q.value, page: page.value, size: size.value })
  const data = resp?.data ?? resp
  total.value = Number(data?.total || 0)
  items.value = data?.items || data?.list || []
}

async function onPageChange(p: number) {
  page.value = Math.max(1, Number(p || 1))
  await load()
}
async function onPageSizeChange(s: number) {
  size.value = Math.max(1, Number(s || 20))
  page.value = 1
  await load()
}
const openDetail = async (it: any) => {
  const resp: any = await aiGradingApi.getHistoryDetail(it.id)
  detail.value = (resp?.data ?? resp) || it
}
function goBack(){
  const q: any = {}
  if (courseId.value) q.courseId = String(courseId.value)
  router.push({ name: 'TeacherAIGrading', query: q })
}
function goCourses(){ router.push({ name: 'TeacherCourseManagement' }) }
function goAssignments(){
  const q: any = {}
  if (courseId.value) q.courseId = String(courseId.value)
  router.push({ name: 'TeacherAssignments', query: q })
}
function goCourseDetail(){
  if (courseId.value) router.push({ name: 'TeacherCourseDetail', params: { id: courseId.value } })
  else router.push({ name: 'TeacherCourseManagement' })
}
const pretty = (v: any) => { try { return JSON.stringify(JSON.parse(String(v||'')), null, 2) } catch { return String(v||'') } }

async function confirmDelete(it: any) {
  if (deleting.value) return
  const ok = await confirmDialog.confirm({
    title: (t('common.confirm') as string) || '确定',
    message: (t('teacher.aiGrading.confirmDelete') as string) || '确认删除该记录？此操作不可恢复。'
  })
  if (!ok) return
  try {
    deleting.value = true
    await aiGradingApi.deleteHistory(it.id)
    // 删除本地列表项
    items.value = items.value.filter(x => x.id !== it.id)
  } catch (e) {
    uiStore.showNotification({
      type: 'error',
      title: (t('app.notifications.error.title') as string) || (t('common.error') as string) || '错误',
      message: (t('teacher.aiGrading.deleteFailed') as string) || '删除失败，请稍后重试'
    })
  } finally {
    deleting.value = false
  }
}

function hasScore(row: any): boolean {
  if (typeof row?.finalScore === 'number') return true
  // 尝试从 rawJson 中解析 overall.final_score
  try {
    const obj = row?.rawJson ? normalizeAssessment(safeJsonParseLoose(String(row.rawJson))) : null
    const s = overallScore(obj)
    return Number.isFinite(s)
  } catch { return false }
}
function resolveFinalScore(row: any): number {
  if (typeof row?.finalScore === 'number') return Number(row.finalScore)
  try {
    const obj = row?.rawJson ? normalizeAssessment(safeJsonParseLoose(String(row.rawJson))) : null
    return overallScore(obj)
  } catch { return 0 }
}

function formatTime(v: any): string {
  const s = String(v || '')
  return s.replace('T', ' ')
}

function exportDetailAsText() {
  const it = detail.value
  if (!it || !parsed.value) return
  const res: any = parsed.value
  const lines: string[] = []
  const pushSec = (title: string, sec: any) => {
    if (!sec) return
    for (const [k, v] of Object.entries(sec)) {
      const s = v as any
      lines.push(`${title} - ${String(k)}: ${Number(s?.score ?? 0)}/5`)
      const ev = Array.isArray(s?.evidence) ? s.evidence : []
      if (ev.length) {
        const e0 = ev[0] as any
        if (e0?.quote) lines.push(`证据: ${String(e0.quote)}`)
        if (e0?.reasoning) lines.push(`推理: ${String(e0.reasoning)}`)
        if (e0?.conclusion) lines.push(`结论: ${String(e0.conclusion)}`)
        if (e0?.explanation) lines.push(`解释: ${String(e0.explanation)}`)
      }
      const sug = Array.isArray(s?.suggestions) ? s.suggestions : []
      if (sug.length) {
        lines.push('建议:')
        sug.forEach((x: any, idx: number) => lines.push(`${idx + 1}. ${String(x)}`))
      }
      lines.push('')
    }
  }
  lines.push(`文件: ${it.fileName || ''}`)
  const ov = getOverall(res) as any
  if (ov) {
    lines.push(`总体评分: ${Number(ov?.final_score ?? 0)}/5`)
    if (ov?.holistic_feedback) lines.push(`总体评价: ${String(ov.holistic_feedback)}`)
    lines.push('')
  }
  pushSec('道德推理', res?.moral_reasoning)
  pushSec('学习态度', res?.attitude_development)
  pushSec('学习能力', res?.ability_growth)
  pushSec('学习策略', res?.strategy_optimization)

  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${(it.fileName || 'grading').toString().replace(/\s+/g,'_')}.txt`
  a.click()
  URL.revokeObjectURL(a.href)
}

async function exportDetailAsPng() {
  if (!detailRef.value) return
  applyExportGradientsInline(detailRef.value)
  const fileBase = (detail.value?.fileName || 'grading').toString().replace(/\s+/g, '_')
  await exportNodeAsPng(detailRef.value, fileBase)
}

async function exportDetailAsPdf() {
  if (!detailRef.value) return
  const node = detailRef.value
  const it = detail.value
  const fileBase = (it?.fileName || 'grading').toString().replace(/\s+/g, '_')

  // 1) 克隆一个无滚动限制的容器，保证渲染完整高度
  const cloned = node.cloneNode(true) as HTMLElement
  const wrapper = document.createElement('div')
  wrapper.style.position = 'fixed'
  wrapper.style.left = '-99999px'
  wrapper.style.top = '0'
  wrapper.style.width = `${node.clientWidth}px`
  wrapper.style.background = '#ffffff'
  cloned.style.maxHeight = 'none'
  cloned.style.overflow = 'visible'
  wrapper.appendChild(cloned)
  document.body.appendChild(wrapper)

  // 2) 用 html2canvas 渲染整块内容（高分辨率）
  applyExportGradientsInline(cloned)
  const pdfBase = (detail.value?.fileName || 'grading').toString().replace(/\s+/g, '_')
  await exportNodeAsPdf(cloned, pdfBase)
  document.body.removeChild(wrapper)
}

// 解析/归一化/渲染函数（与批改页一致的精简版）
function safeJsonParseLoose(raw: any): any {
  if (raw && typeof raw === 'object') return raw
  let s = String(raw || '')
  s = s.replace(/^\uFEFF/, '').trim()
  const fence = /```(?:json)?\s*([\s\S]*?)```/i
  const m = s.match(fence)
  if (m && m[1]) s = m[1].trim()
  const first = s.indexOf('{')
  const last = s.lastIndexOf('}')
  if (first >= 0 && last > first) s = s.substring(first, last + 1)
  s = s.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
  return JSON.parse(s)
}

function getOverall(obj: any): any {
  if (!obj || typeof obj !== 'object') return null
  if ((obj as any).overall) return (obj as any).overall
  if ((obj as any).final_score || (obj as any).holistic_feedback || (obj as any).dimension_averages) return obj
  return null
}
function overallScore(obj: any): number {
  const ov = getOverall(obj)
  const n = Number(ov?.final_score)
  return Number.isFinite(n) ? n : 0
}
function overallFeedback(obj: any): string {
  const ov = getOverall(obj)
  return String(ov?.holistic_feedback || '')
}
function dimensionBars(obj: any): Array<{ key: string; label: string; value: number }> | null {
  try {
    const ov = getOverall(obj)
    const dm = ov?.dimension_averages
    if (!dm) return null
    return [
      { key: 'moral_reasoning', label: t('teacher.aiGrading.render.moral_reasoning') as string, value: Number(dm.moral_reasoning ?? 0) },
      { key: 'attitude', label: t('teacher.aiGrading.render.attitude_development') as string, value: Number(dm.attitude ?? 0) },
      { key: 'ability', label: t('teacher.aiGrading.render.ability_growth') as string, value: Number(dm.ability ?? 0) },
      { key: 'strategy', label: t('teacher.aiGrading.render.strategy_optimization') as string, value: Number(dm.strategy ?? 0) }
    ]
  } catch { return null }
}
function dimGradient(key: string): 'dimension' | 'overall' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy' {
  const k = String(key || '').toLowerCase()
  if (k.includes('moral') || k.includes('stage') || k.includes('argument') || k.includes('foundation')) return 'dimension_moral'
  if (k.includes('attitude') || k.includes('emotion') || k.includes('resilience') || k.includes('flow')) return 'dimension_attitude'
  if (k.includes('ability') || k.includes('bloom') || k.includes('metacognition') || k.includes('transfer')) return 'dimension_ability'
  if (k.includes('strategy') || k.includes('diversity') || k.includes('depth') || k.includes('regulation')) return 'dimension_strategy'
  return 'dimension'
}
function renderCriterion(block: any, barKind?: 'dimension' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy') {
  try {
    const sections: string[] = []
    for (const [k, v] of Object.entries(block || {})) {
      const sec = v as any
      const score = sec?.score
      const ev = Array.isArray(sec?.evidence) ? sec.evidence : []
      const sug = Array.isArray(sec?.suggestions) ? sec.suggestions : []
      const which = barKind || 'dimension'
      const labelInline = (txt: string) =>
        `<span class="inline-flex items-center gap-2 font-semibold mr-1">
          <span class="inline-block w-2 h-2 rounded-full" data-gradient="${which}"></span>
          <span>${escapeHtml(txt)}:</span>
        </span>`
      const labelBlock = (txt: string) =>
        `<div class="mt-2 mb-1 inline-flex items-center gap-2 text-xs font-semibold">
          <span class="inline-block w-2 h-2 rounded-full" data-gradient="${which}"></span>
          <span>${escapeHtml(txt)}</span>
        </div>`
      const bar = typeof score === 'number' ? `<div class=\"h-2 w-40 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner\"><div class=\"h-full\" data-gradient=\"${which}\" style=\"width:${score*20}%\"></div></div>` : ''
      const firstReasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning || ''
      const firstConclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion || ''
      const evid = ev
        .filter((e: any) => (e && (e.quote || e.explanation)))
        .map((e: any) => `<li class="mb-1">
            <div class="text-xs text-gray-600 dark:text-gray-300">${e.quote ? '“'+escapeHtml(e.quote)+'”' : ''}</div>
            ${e.explanation?`<div class="text-[11px] text-gray-500">${escapeHtml(e.explanation)}</div>`:''}
          </li>`) 
        .join('')
      const reasoningBlock = firstReasoning ? `<div class="text-xs mt-1">${labelInline('Reasoning')} ${escapeHtml(firstReasoning)}</div>` : ''
      const conclusionBlock = firstConclusion ? `<div class="text-xs italic text-gray-600 dark:text-gray-300 mt-1"><span class="not-italic">${labelInline('Conclusion')}</span> ${escapeHtml(firstConclusion)}</div>` : ''
      const sugg = sug.map((s: any) => `<li class="mb-1 text-xs">${escapeHtml(String(s))}</li>`).join('')
      sections.push(`<div class="space-y-2"><div class="text-sm font-medium">${escapeHtml(String(k))} ${score!=null?`(${score}/5)`:''}</div>${bar}<div>${labelBlock('Evidence')}<ul>${evid}</ul>${reasoningBlock}${conclusionBlock}</div><div>${labelBlock('Suggestions')}<ul>${sugg}</ul></div></div>`)
    }
    return sections.join('')
  } catch { return `<pre class="text-xs">${escapeHtml(pretty(block))}</pre>` }
}
function escapeHtml(s: string) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function normalizeAssessment(obj: any) {
  try {
    if (obj == null) return obj
    if (typeof obj === 'string') return obj
    if (Array.isArray((obj as any).evaluation_result)) {
      return normalizeFromEvaluationArray((obj as any).evaluation_result)
    }
    if ((obj as any).evaluation && typeof (obj as any).evaluation === 'object') {
      // 宽松与对象两种
      const ev = (obj as any).evaluation
      if (ev?.dimensions) return normalizeFromEvaluationDimensions(ev)
      return normalizeFromEvaluationObject(ev)
    }
    if ((obj as any).evaluation_results && typeof (obj as any).evaluation_results === 'object') {
      return normalizeFromEvaluationResults((obj as any).evaluation_results)
    }
    if ((obj as any).moral_reasoning_maturity || (obj as any).learning_attitude_development || (obj as any).learning_ability_growth || (obj as any).learning_strategy_optimization) {
      return normalizeFromSnakeDimensions(obj)
    }
    const hasCore = (o: any) => !!(o && (o.moral_reasoning || o.attitude_development || o.ability_growth || o.strategy_optimization || o.overall))
    if (hasCore(obj)) return obj
    return obj
  } catch { return obj }
}

function normalizeFromEvaluationArray(arr: any[]) {
  const out: any = { moral_reasoning: {}, attitude_development: {}, ability_growth: {}, strategy_optimization: {} }
  const getSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    const evidence = Array.isArray(ev?.quotes)
      ? ev.quotes.map((q: any) => ({ quote: String(q), reasoning: String(ev?.reasoning || ''), conclusion: String(ev?.conclusion || '') }))
      : [{ quote: '', reasoning: String(typeof ev === 'string' ? ev : ''), conclusion: '' }]
    const sraw = raw?.suggestions
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [String(sraw)] : [])
    return { score, evidence, suggestions }
  }
  const mapDim = (name: string) => {
    const n = (name || '').toLowerCase()
    if (n.includes('moral')) return 'moral_reasoning'
    if (n.includes('attitude')) return 'attitude_development'
    if (n.includes('ability')) return 'ability_growth'
    if (n.includes('strategy')) return 'strategy_optimization'
    return ''
  }
  const mapId = (dimKey: string, id: string) => {
    const i = (id || '').toUpperCase()
    const tbl: Record<string, Record<string, string>> = {
      moral_reasoning: { '1A': 'stage_level', '1B': 'foundations_balance', '1C': 'argument_chain' },
      attitude_development: { '2A': 'emotional_engagement', '2B': 'resilience', '2C': 'focus_flow' },
      ability_growth: { '3A': 'blooms_level', '3B': 'metacognition', '3C': 'transfer' },
      strategy_optimization: { '4A': 'diversity', '4B': 'depth', '4C': 'self_regulation' }
    }
    return tbl[dimKey]?.[i] || ''
  }
  for (const g of (arr || [])) {
    const dimKey = mapDim(g?.dimension || '')
    if (!dimKey) continue
    const bucket: any = out[dimKey]
    const items = Array.isArray(g?.sub_criteria) ? g.sub_criteria : []
    for (const it of items) {
      const secKey = mapId(dimKey, String(it?.id || ''))
      if (!secKey) continue
      bucket[secKey] = getSec(it)
    }
  }
  const avg = (nums: number[]) => { const arrv = nums.filter(n => Number.isFinite(n)); if (!arrv.length) return 0; return Math.round((arrv.reduce((a, b) => a + b, 0) / arrv.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

function normalizeFromEvaluationObject(evaluation: any) {
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    const evidence = Array.isArray(ev?.quotes)
      ? ev.quotes.map((q: any) => ({ quote: String(q), reasoning: String(ev?.reasoning || ''), conclusion: String(ev?.conclusion || '') }))
      : [{ quote: '', reasoning: String(typeof ev === 'string' ? ev : ''), conclusion: '' }]
    const sraw = raw?.suggestions
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [String(sraw)] : [])
    return { score, evidence, suggestions }
  }
  const findGroup = (obj: any, hint: string) => { const k = Object.keys(obj || {}).find(k => k.toLowerCase().includes(hint)); return k ? obj[k] : undefined }
  const findSubKey = (obj: any, hints: string[]) => {
    const keys = Object.keys(obj || {})
    const lowerHints = hints.map(h => String(h).toLowerCase())
    const hit = keys.find(k => lowerHints.every(h => k.toLowerCase().includes(h)))
    return hit ? obj[hit] : undefined
  }
  const moral = evaluation["1) Moral Reasoning Maturity"] || findGroup(evaluation, 'moral reasoning') || {}
  const attitude = evaluation["2) Learning Attitude Development"] || findGroup(evaluation, 'attitude') || {}
  const ability = evaluation["3) Learning Ability Growth"] || findGroup(evaluation, 'ability') || {}
  const strategy = evaluation["4) Learning Strategy Optimization"] || findGroup(evaluation, 'strategy') || {}
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(moral['1A. Stage Level Identification'] || moral['1A']), foundations_balance: toSec(moral['1B. Breadth of Moral Foundations'] || moral['1B']), argument_chain: toSec(moral['1C. Argument Chains and Counter-arguments'] || moral['1C']) }
  out.attitude_development = { emotional_engagement: toSec(attitude['2A. Emotional Engagement'] || attitude['2A']), resilience: toSec(attitude['2B. Persistence/Resilience'] || attitude['2B']), focus_flow: toSec(attitude['2C. Task Focus / Flow'] || attitude['2C']) }
  out.ability_growth = {
    blooms_level: toSec(
      ability['3A. Bloom\'s Taxonomy Progression'] ||
      ability['3A. Bloom’s Taxonomy Progression'] ||
      findSubKey(ability, ['3a','bloom']) ||
      ability['3A']
    ),
    metacognition: toSec(ability['3B. Metacognition (Plan–Monitor–Revise)'] || ability['3B']),
    transfer: toSec(ability['3C. Knowledge Transfer'] || ability['3C'])
  }
  out.strategy_optimization = { diversity: toSec(strategy['4A. Strategy Diversity'] || strategy['4A']), depth: toSec(strategy['4B. Depth of Processing'] || strategy['4B']), self_regulation: toSec(strategy['4C. Self-Regulation'] || strategy['4C']) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

function normalizeFromEvaluationResults(er: any) {
  const lcKeys = Object.keys(er || {})
  const findKey = (includes: string[]) => { const inc = includes.map(s => s.toLowerCase()); return lcKeys.find(k => inc.every(t => k.toLowerCase().includes(t))) }
  const getSec = (group: any, hints: string[]) => {
    if (!group || typeof group !== 'object') return undefined
    const keys = Object.keys(group)
    const target = keys.find(k => hints.every(h => k.toLowerCase().includes(h)))
    const sec = target ? group[target] : undefined
    if (!sec || typeof sec !== 'object') return undefined
    const score = Number((sec as any).score ?? 0)
    const rawEv = (sec as any).evidence
    const evArr = Array.isArray(rawEv) ? rawEv : (rawEv ? [rawEv] : [])
    const evidence = evArr.map((e: any) => { if (e && typeof e === 'object') return e; return { quote: '', reasoning: String(e ?? ''), conclusion: '' } })
    const rawSug = (sec as any).suggestions
    const suggestions = Array.isArray(rawSug) ? rawSug : (rawSug ? [rawSug] : [])
    return { score, evidence, suggestions }
  }
  const pickGroup = (tokens: string[]) => { const k = findKey(tokens); return k ? er[k] : undefined }
  const moral = pickGroup(['moral', 'reasoning'])
  const attitude = pickGroup(['attitude']) || pickGroup(['learning', 'attitude']) || pickGroup(['attitude', 'development'])
  const ability = pickGroup(['ability', 'growth']) || pickGroup(['learning', 'ability'])
  const strategy = pickGroup(['strategy']) || pickGroup(['strategy', 'optimization'])
  const out: any = {}
  out.moral_reasoning = { stage_level: getSec(moral, ['stage', 'level']) || { score: 0, evidence: [], suggestions: [] }, foundations_balance: getSec(moral, ['foundation']) || { score: 0, evidence: [], suggestions: [] }, argument_chain: getSec(moral, ['argument']) || getSec(moral, ['counter']) || { score: 0, evidence: [], suggestions: [] } }
  out.attitude_development = { emotional_engagement: getSec(attitude, ['emotional']) || { score: 0, evidence: [], suggestions: [] }, resilience: getSec(attitude, ['resilience']) || getSec(attitude, ['persistence']) || { score: 0, evidence: [], suggestions: [] }, focus_flow: getSec(attitude, ['flow']) || getSec(attitude, ['focus']) || getSec(attitude, ['task']) || { score: 0, evidence: [], suggestions: [] } }
  out.ability_growth = { blooms_level: getSec(ability, ['bloom']) || getSec(ability, ['taxonomy']) || { score: 0, evidence: [], suggestions: [] }, metacognition: getSec(ability, ['metacognition']) || { score: 0, evidence: [], suggestions: [] }, transfer: getSec(ability, ['transfer']) || { score: 0, evidence: [], suggestions: [] } }
  out.strategy_optimization = { diversity: getSec(strategy, ['diversity']) || { score: 0, evidence: [], suggestions: [] }, depth: getSec(strategy, ['depth']) || { score: 0, evidence: [], suggestions: [] }, self_regulation: getSec(strategy, ['self', 'regulation']) || { score: 0, evidence: [], suggestions: [] } }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning.stage_level.score), Number(out.moral_reasoning.foundations_balance.score), Number(out.moral_reasoning.argument_chain.score) ])
  const adAvg = avg([ Number(out.attitude_development.emotional_engagement.score), Number(out.attitude_development.resilience.score), Number(out.attitude_development.focus_flow.score) ])
  const agAvg = avg([ Number(out.ability_growth.blooms_level.score), Number(out.ability_growth.metacognition.score), Number(out.ability_growth.transfer.score) ])
  const soAvg = avg([ Number(out.strategy_optimization.diversity.score), Number(out.strategy_optimization.depth.score), Number(out.strategy_optimization.self_regulation.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  if (!out.overall.holistic_feedback) out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

function normalizeFromEvaluationDimensions(evaluation: any) {
  const dims = evaluation?.dimensions || {}
  const findDim = (names: string[]) => { const keys = Object.keys(dims); for (const k of keys) { const kl = k.toLowerCase(); for (const n of names) { if (kl.includes(n.toLowerCase())) return dims[k] } } return undefined }
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const quotes = Array.isArray(raw?.evidence?.quotes) ? raw.evidence.quotes : []
    const reasoning = String(raw?.reasoning || '')
    const conclusion = String(raw?.conclusion || '')
    const evidence = quotes.length ? quotes.map((q: any) => ({ quote: String(q), reasoning, conclusion })) : [{ quote: '', reasoning, conclusion }]
    const sraw = (raw?.suggestions)
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }
  const moral = findDim(['moral reasoning'])
  const attitude = findDim(['learning attitude'])
  const ability = findDim(['learning ability'])
  const strategy = findDim(['strategy'])
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(moral?.['1A']), foundations_balance: toSec(moral?.['1B']), argument_chain: toSec(moral?.['1C']) }
  out.attitude_development = { emotional_engagement: toSec(attitude?.['2A']), resilience: toSec(attitude?.['2B']), focus_flow: toSec(attitude?.['2C']) }
  out.ability_growth = { blooms_level: toSec(ability?.['3A']), metacognition: toSec(ability?.['3B']), transfer: toSec(ability?.['3C']) }
  out.strategy_optimization = { diversity: toSec(strategy?.['4A']), depth: toSec(strategy?.['4B']), self_regulation: toSec(strategy?.['4C']) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning.stage_level?.score), Number(out.moral_reasoning.foundations_balance?.score), Number(out.moral_reasoning.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development.emotional_engagement?.score), Number(out.attitude_development.resilience?.score), Number(out.attitude_development.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth.blooms_level?.score), Number(out.ability_growth.metacognition?.score), Number(out.ability_growth.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization.diversity?.score), Number(out.strategy_optimization.depth?.score), Number(out.strategy_optimization.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

function normalizeFromSnakeDimensions(obj: any) {
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    let evidence: any[] = []
    if (Array.isArray(ev)) evidence = ev
    else if (ev) { const parsed = parseEvidenceString(String(ev)); evidence = parsed.length ? parsed : [{ quote: '', reasoning: String(ev), conclusion: '' }] }
    const sraw = (raw?.suggestions)
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }
  const mr = obj.moral_reasoning_maturity || {}
  const ad = obj.learning_attitude_development || {}
  const ag = obj.learning_ability_growth || {}
  const so = obj.learning_strategy_optimization || {}
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(mr['1A_stage_level_identification'] || mr['1a'] || {}), foundations_balance: toSec(mr['1B_breadth_of_moral_foundations'] || mr['1b'] || {}), argument_chain: toSec(mr['1C_argument_chains_and_counter_arguments'] || mr['1c'] || {}) }
  out.attitude_development = { emotional_engagement: toSec(ad['2A_emotional_engagement'] || ad['2a'] || {}), resilience: toSec(ad['2B_persistence_resilience'] || ad['2b'] || {}), focus_flow: toSec(ad['2C_task_focus_flow'] || ad['2c'] || {}) }
  out.ability_growth = { blooms_level: toSec(ag['3A_bloom_s_taxonomy_progression'] || ag['3a'] || {}), metacognition: toSec(ag['3B_metacognition_plan_monitor_revise'] || ag['3b'] || {}), transfer: toSec(ag['3C_knowledge_transfer'] || ag['3c'] || {}) }
  out.strategy_optimization = { diversity: toSec(so['4A_strategy_diversity'] || so['4a'] || {}), depth: toSec(so['4B_depth_of_processing'] || so['4b'] || {}), self_regulation: toSec(so['4C_self_regulation'] || so['4c'] || {}) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mrAvg = avg([ Number(out.moral_reasoning.stage_level?.score), Number(out.moral_reasoning.foundations_balance?.score), Number(out.moral_reasoning.argument_chain?.score) ])
  const adAvg = avg([ Number(out.attitude_development.emotional_engagement?.score), Number(out.attitude_development.resilience?.score), Number(out.attitude_development.focus_flow?.score) ])
  const agAvg = avg([ Number(out.ability_growth.blooms_level?.score), Number(out.ability_growth.metacognition?.score), Number(out.ability_growth.transfer?.score) ])
  const soAvg = avg([ Number(out.strategy_optimization.diversity?.score), Number(out.strategy_optimization.depth?.score), Number(out.strategy_optimization.self_regulation?.score) ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = { dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg }, final_score: finalScore, holistic_feedback: '' }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

function parseEvidenceString(s: string): any[] {
  const text = String(s || '')
  const m = text.match(/Quote:\s*([\s\S]*?)\n\s*Reasoning:\s*([\s\S]*?)\n\s*Conclusion:\s*([\s\S]*)/i)
  if (m) return [{ quote: m[1]?.trim() || '', reasoning: m[2]?.trim() || '', conclusion: m[3]?.trim() || '' }]
  const mq = text.match(/Quote:\s*([\s\S]*)/i)
  if (mq) return [{ quote: mq[1]?.trim() || '', reasoning: '', conclusion: '' }]
  return []
}

function buildHolisticFeedback(out: any): string {
  try {
    const parts: string[] = []
    const avg = out?.overall?.dimension_averages || {}
    const finalScore = Number(out?.overall?.final_score ?? 0)
    const avgLine = `Averages — Moral: ${avg.moral_reasoning ?? 0}, Attitude: ${avg.attitude ?? 0}, Ability: ${avg.ability ?? 0}, Strategy: ${avg.strategy ?? 0}. Final: ${finalScore}/5.`
    parts.push(avgLine)
    const pickSuggestions = (sec: any) => Array.isArray(sec?.suggestions) ? sec.suggestions : []
    const allSuggestions: string[] = []
    const pushFromGroup = (grp: any) => {
      if (!grp) return
      for (const key of Object.keys(grp)) {
        const sec = (grp as any)[key]
        const arr = pickSuggestions(sec).map((s: any) => String(s))
        for (const s of arr) { if (s && allSuggestions.length < 6) allSuggestions.push(s) }
      }
    }
    pushFromGroup(out.moral_reasoning)
    pushFromGroup(out.attitude_development)
    pushFromGroup(out.ability_growth)
    pushFromGroup(out.strategy_optimization)
    if (allSuggestions.length) {
      parts.push('Key suggestions:')
      for (const s of allSuggestions) parts.push(`- ${s}`)
    }
    return parts.join('\n')
  } catch { return '' }
}

onMounted(async () => {
  await load()
  // 若从评分页带入 openId，则自动打开详情
  const q = router.currentRoute.value.query as any
  const openId = q?.openId
  if (openId) {
    try {
      const resp: any = await aiGradingApi.getHistoryDetail(String(openId))
      detail.value = (resp?.data ?? resp)
    } catch {}
  }
})
onMounted(async () => {
  const q = router.currentRoute.value.query as any
  if (q?.courseId) {
    courseId.value = String(q.courseId)
    try {
      const one: any = await courseApi.getCourseById(Number(courseId.value))
      courseTitle.value = String(one?.data?.title || one?.title || '')
    } catch {}
  }
})
</script>

<style scoped lang="postcss">
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
.no-scrollbar::-webkit-scrollbar { display: none; }
.page-size-select :deep(.input) { height: 28px; padding-top: 0; padding-bottom: 0; }
.page-size-select :deep(button.input) { height: 28px; }
</style>


