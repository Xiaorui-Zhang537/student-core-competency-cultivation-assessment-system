<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
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
        <span class="opacity-80">{{ t('teacher.aiGrading.title') || 'AI 批改作业' }}</span>
      </nav>
      <page-header :title="t('teacher.aiGrading.title')" :subtitle="t('teacher.aiGrading.subtitle')">
        <template #actions>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-600 dark:text-gray-300">{{ t('teacher.ai.model.label') }}</span>
              <glass-popover-select v-model="gradingModel" :options="gradingModelOptions" size="sm" />
            </div>
            <Button variant="success" class="whitespace-nowrap" @click="exportActiveAsText" :disabled="!activeItem || !activeItem.result"><arrow-down-tray-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.exportText') || '导出文本' }}</Button>
            <Button variant="purple" class="whitespace-nowrap" @click="goHistory"><clock-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.history') || '历史记录' }}</Button>
            <Button variant="primary" class="whitespace-nowrap" :disabled="pending || files.length===0" @click="startGrading"><play-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.start') }}</Button>
          </div>
        </template>
      </page-header>

      

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
        <div class="lg:col-span-1">
          <div class="card p-4 space-y-4" v-glass="{ strength: 'regular', interactive: true }">
            <h3 class="font-medium">{{ t('teacher.aiGrading.inputTitle') }}</h3>
            <file-upload multiple accept=".txt,.doc,.docx,.pdf" @files-selected="onFilesSelected"/>
            <div class="space-y-2">
              <glass-textarea v-model="textDraft" :rows="5" class="w-full" :placeholder="t('teacher.aiGrading.pastePlaceholder') as string" />
              <div class="flex items-center gap-2">
                <Button variant="warning" size="sm" class="whitespace-nowrap" @click="addTextItem" :disabled="!textDraft.trim()"><plus-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.addText') }}</Button>
                <span class="text-xs text-gray-500" v-if="textDraft.trim().length">{{ textDraft.trim().length }} 字符</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <Button variant="teal" class="whitespace-nowrap" @click="openPicker"><inbox-arrow-down-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.pickFromSubmissions') }}</Button>
              <Button variant="danger" class="whitespace-nowrap" @click="clearAll" :disabled="files.length===0"><x-mark-icon class="w-4 h-4 mr-2" />{{ t('teacher.aiGrading.clearAll') }}</Button>
            </div>
            <ul class="divide-y divide-white/10 dark:divide-white/10 max-h-60 overflow-auto no-scrollbar pr-1">
              <li v-for="(f, idx) in files" :key="f.id" class="py-2 flex items-center justify-between cursor-pointer rounded-md px-2 transition-colors"
                  :class="idx===activeIndex ? 'bg-primary-500/10 dark:bg-primary-400/15 border border-primary-400/60 dark:border-primary-300/60 shadow-sm' : 'border border-transparent hover:bg-white/5 dark:hover:bg-white/5'"
                  @click="activeIndex = idx">
                <div class="min-w-0 flex-1 pr-3">
                  <div class="text-sm truncate" :title="f.name">{{ f.name }}</div>
                  <div class="text-xs text-gray-600 dark:text-gray-300">{{ renderStatus(f) }}</div>
                </div>
                <div class="flex items-center gap-2">
                  <Button size="sm" variant="ghost" class="whitespace-nowrap" @click.stop="retryOne(f)" v-if="f.error"><arrow-path-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.retry') }}</Button>
                  <Button size="sm" variant="danger" class="whitespace-nowrap" @click.stop="removeOne(f)"><trash-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.remove') }}</Button>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div class="lg:col-span-2">
          <Card v-if="activeItem" padding="md" tint="primary" class="rounded-2xl glass-interactive" v-glass="{ strength: 'thick', interactive: true }">
            <h3 class="font-medium mb-3">{{ activeItem.name || activeItem.fileName }}</h3>
            <div v-if="activeItem.status==='error'" class="p-3 rounded bg-red-50 text-red-700 text-sm">
              <div class="font-semibold mb-1">{{ t('common.error') || '错误' }}</div>
              <div>{{ activeItem.error || 'Invalid JSON returned by model' }}</div>
            </div>
            <div v-else-if="isJson(activeItem.result)">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="sm:col-span-2">
                  <Card padding="sm" tint="secondary" class="rounded-xl">
                  <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.overall') }}</h4>
                  <div>
                    <div class="text-sm mb-2 flex items-center gap-3" v-if="getOverall(activeItem.result)?.final_score != null">
                      <span>{{ t('teacher.aiGrading.render.final_score') }}: {{ overallScore(activeItem.result) }}</span>
                      <div class="h-2 w-64 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                        <div class="h-full transition-[width] duration-700" data-gradient="overall" :style="{ width: (Number(overallScore(activeItem.result))*20 || 0) + '%' }"></div>
                      </div>
                    </div>
                    <div class="space-y-2 mb-2" v-if="dimensionBars(activeItem.result)">
                      <div class="text-sm font-medium">{{ t('teacher.aiGrading.render.dimension_averages') }}</div>
                      <div v-for="row in dimensionBars(activeItem.result)" :key="row.key" class="flex items-center gap-3">
                        <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}: {{ row.value }}</div>
                        <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                          <div class="h-full transition-[width] duration-700" data-gradient="dimension" :style="{ width: (row.value*20 || 0) + '%' }"></div>
                        </div>
                      </div>
                    </div>
                    <div class="text-sm whitespace-pre-wrap">{{ t('teacher.aiGrading.render.holistic_feedback') }}: {{ overallFeedback(activeItem.result) || (t('common.empty') || '无内容') }}</div>
                  </div>
                  </Card>
                </div>
                <Card padding="sm" tint="warning" class="rounded-xl" v-if="activeItem.result?.moral_reasoning">
                  <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.moral_reasoning') }}</h4>
                  <div v-html="renderCriterion(activeItem.result.moral_reasoning, 'dimension_moral')"></div>
                </Card>
                <Card padding="sm" tint="accent" class="rounded-xl" v-if="activeItem.result?.attitude_development">
                  <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.attitude_development') }}</h4>
                  <div v-html="renderCriterion(activeItem.result.attitude_development, 'dimension_attitude')"></div>
                </Card>
                <Card padding="sm" tint="info" class="rounded-xl" v-if="activeItem.result?.ability_growth">
                  <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.ability_growth') }}</h4>
                  <div v-html="renderCriterion(activeItem.result.ability_growth, 'dimension_ability')"></div>
                </Card>
                <Card padding="sm" tint="success" class="rounded-xl" v-if="activeItem.result?.strategy_optimization">
                  <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.strategy_optimization') }}</h4>
                  <div v-html="renderCriterion(activeItem.result.strategy_optimization, 'dimension_strategy')"></div>
                </Card>
              </div>
            </div>
            <div v-else-if="activeItem.status==='grading'" class="card p-4 rounded-xl" v-glass="{ strength: 'regular', interactive: true }">
              <div class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-200">
                <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                {{ t('teacher.aiGrading.status.grading') }}
              </div>
            </div>
            <div v-else-if="activeItem.status==='pending'" class="p-4 rounded border border-gray-300/70 dark:border-white/10 bg-gray-50/60 dark:bg-white/5 text-sm text-gray-600 dark:text-gray-300">
              {{ t('teacher.aiGrading.picker.hint') || '已添加到队列，请点击开始批改。' }}
            </div>
            <pre v-else class="card p-3 overflow-auto no-scrollbar text-xs">{{ pretty(activeItem.result?.text || activeItem.result || '') }}</pre>
          </Card>
          <div v-else class="card p-8 text-center text-gray-500">{{ t('teacher.aiGrading.empty') }}</div>
        </div>
      </div>

      <!-- Picker Modal (GlassModal) -->
      <glass-modal v-if="modalOpen" :title="t('teacher.aiGrading.picker.title') as string" size="xl" heightVariant="normal" @close="modalOpen=false">
        <div class="picker-controls flex items-center gap-4 flex-wrap md:flex-nowrap mb-4">
          <glass-popover-select :label="t('teacher.aiGrading.picker.assignment') || '作业选择'" :options="assignmentOptions" v-model="picker.assignmentId" size="sm" width="clamp(200px, 28vw, 320px)" :fullWidth="false" :truncate-label="false" :placeholder="(t('common.pleaseSelect') || '请选择') + (t('teacher.assignments.title') || '作业')" />
          <glass-popover-select :label="t('teacher.aiGrading.picker.student') || '学生'" :options="picker.students" v-model="picker.studentId" size="sm" width="clamp(200px, 28vw, 320px)" :fullWidth="false" :truncate-label="false" :placeholder="(t('common.pleaseSelect') || '请选择') + (t('teacher.aiGrading.picker.student') || '学生')" />
        </div>

        <div class="grid grid-cols-1 gap-3" v-if="picker.studentId">
          <!-- Preview Area -->
          <div class="card p-3" v-glass="{ strength: 'regular', interactive: true }">
            <div class="flex items-center justify-between mb-2">
              <div class="text-sm font-medium">{{ t('teacher.aiGrading.picker.preview') || '提交预览' }}</div>
              <div class="flex items-center gap-2">
                <label class="text-xs flex items-center gap-1">
                  <input type="checkbox" v-model="picker.useText" :disabled="!picker.preview.text" />
                  <span>{{ t('teacher.aiGrading.picker.useText') || '使用文本内容' }}</span>
                </label>
                <Button size="sm" variant="indigo" @click="selectAllFiles" :disabled="!picker.preview.files.length"><plus-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.selectAllFiles') || '全选附件' }}</Button>
                <Button size="sm" variant="danger" @click="clearSelectedFiles" :disabled="!picker.selectedFileIds.length"><x-mark-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.clearFiles') || '清空选择' }}</Button>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <div class="text-xs text-gray-600 dark:text-gray-300 mb-1">{{ t('teacher.aiGrading.picker.text') || '文本内容' }}</div>
                <div v-if="picker.preview.text" class="bg-white/40 dark:bg-white/5 border border-white/30 dark:border-white/10 rounded p-2 text-xs max-h-40 overflow-auto no-scrollbar whitespace-pre-wrap">
                  {{ truncate(picker.preview.text, 800) }}
                </div>
                <div v-else class="text-xs text-gray-500">{{ t('common.empty') || '无内容' }}</div>
              </div>
              <div>
                <div class="text-xs text-gray-600 dark:text-gray-300 mb-1">{{ t('teacher.aiGrading.picker.attachments') || '附件' }}</div>
                <ul class="divide-y divide-gray-200 dark:divide-white/10 max-h-40 overflow-auto no-scrollbar">
                  <li v-for="f in picker.preview.files" :key="f.id" class="py-1 flex items-center justify-between">
                    <label class="flex items-center gap-2 cursor-pointer text-xs">
                      <input type="checkbox" :value="Number(f.id)" v-model="picker.selectedFileIds" />
                      <span :title="f.originalName || f.fileName || f.name">{{ f.originalName || f.fileName || f.name }}</span>
                    </label>
                    <span class="text-[10px] text-gray-500">{{ f.size ? (Math.round(f.size/102.4)/10 + ' KB') : '' }}</span>
                  </li>
                  <li v-if="!picker.preview.files.length" class="py-2 text-xs text-gray-500">{{ t('common.empty') || '无内容' }}</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Action Bar -->
          <div class="flex items-center justify-end gap-2">
            <Button size="sm" variant="teal" @click="loadSubmissions"><arrow-path-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.reload') || '重新加载' }}</Button>
            <Button size="sm" variant="primary" @click="addSelected" :disabled="!canAddFromPreview"><plus-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.addSelected') }}</Button>
          </div>
        </div>

        <div v-else class="text-xs text-gray-500">{{ t('teacher.aiGrading.picker.hint') || '请选择作业与学生以预览提交' }}</div>

        <template #footer>
          <Button size="sm" variant="secondary" @click="modalOpen=false"><x-mark-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.picker.close') }}</Button>
        </template>
      </glass-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import FileUpload from '@/components/forms/FileUpload.vue'
import { aiGradingApi } from '@/api/aiGrading.api'
import { aiApi } from '@/api/ai.api'
import { fileApi } from '@/api/file.api'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import Card from '@/components/ui/Card.vue'
import { PlayIcon, PlusIcon, InboxArrowDownIcon, XMarkIcon, ArrowPathIcon, TrashIcon, ArrowDownTrayIcon, ClockIcon, ChevronRightIcon } from '@heroicons/vue/24/outline'
import { useRouter } from 'vue-router'
import { courseApi } from '@/api/course.api'

const { t } = useI18n()
const router = useRouter()
const courseId = ref<string>('')
const courseTitle = ref<string>('')

type GradingItem = { id: number; name: string; status: 'pending'|'extracting'|'grading'|'done'|'error'; error?: string; result?: any; text?: string }
const files = ref<GradingItem[]>([])
const textDraft = ref('')
const pending = ref(false)
const activeIndex = ref(0)

// Batch progress
const totalCount = computed(() => files.value.length)
const processedCount = computed(() => files.value.filter(i => i.status === 'done' || i.status === 'error').length)
const gradingActive = computed(() => pending.value || files.value.some(i => i.status === 'grading'))
const determinate = computed(() => totalCount.value > 0 && (processedCount.value > 0))
const progressPercent = computed(() => {
  if (!totalCount.value) return 0
  const p = Math.round((processedCount.value / totalCount.value) * 100)
  return Math.min(100, Math.max(0, p))
})

const activeResult = computed(() => files.value[activeIndex.value] && files.value[activeIndex.value].result ? files.value[activeIndex.value] : null)
const activeItem = computed(() => files.value[activeIndex.value] || null)
const activeRaw = computed(() => null)

// Picker add button enablement
const canAddFromPreview = computed(() => {
  const pv = picker.value.preview
  const hasText = !!(picker.value.useText && pv && pv.text && String(pv.text).trim())
  const hasFiles = Array.isArray(picker.value.selectedFileIds) && picker.value.selectedFileIds.length > 0
  return !!(hasText || hasFiles)
})

const onFilesSelected = async (selected: File[]) => {
  if (!Array.isArray(selected) || selected.length === 0) return
  for (const f of selected) {
    try {
      const info: any = await fileApi.uploadFile(f, { purpose: 'ai_grading' })
      const id = Number(info?.id || info?.fileId)
      if (!Number.isFinite(id)) continue
      if (!files.value.some(x => x.id === id)) {
        files.value.push({ id, name: info?.originalName || info?.fileName || f.name, status: 'pending' })
      }
    } catch {}
  }
}

const clearAll = () => { files.value = [] }
const removeOne = (f: GradingItem) => { files.value = files.value.filter(x => x !== f) }
const retryOne = async (f: GradingItem) => { await gradeFiles([f]) }
const renderStatus = (f: GradingItem) => { if (f.error) return t('teacher.aiGrading.status.error') + ': ' + f.error; return t('teacher.aiGrading.status.' + f.status) }

const startGrading = async () => { await gradeFiles(files.value) }

async function gradeFiles(items: GradingItem[]) {
  if (!items.length) return
  pending.value = true
  try {
    // 标记中
    items.forEach(i => { if (!i.result) i.status = 'grading' })

    // 分离：文件与纯文本
    const fileItems = items.filter(i => i.id && !i.text)
    const textItems = items.filter(i => i.text && (!i.result))

    // 批量：文件ID走 /ai/grade/files
    if (fileItems.length) {
      const ids = fileItems.map(i => i.id)
      const resp: any = await aiGradingApi.gradeFiles({ fileIds: ids as number[], model: gradingModel.value, jsonOnly: true, useGradingPrompt: true })
      const results = (resp?.data?.results || resp?.results || []) as any[]
      for (const r of results) {
        const target = files.value.find(f => f.id === Number(r.fileId))
        if (!target) continue
        if (r.error) { target.status = 'error'; target.error = r.error } else { target.status = 'done'; target.result = normalizeAssessment(r.result) }
      }

      // 对于批量中失败的项，回退：单个调用 /ai/grade/files jsonOnly=false，再尽力解析
      const failed = fileItems.filter(i => i.status === 'error')
      for (const it of failed) {
        try {
          const one: any = await aiGradingApi.gradeFiles({ fileIds: [Number(it.id)], model: gradingModel.value, jsonOnly: false, useGradingPrompt: true })
          const arr = one?.data?.results || one?.results || []
          const first = Array.isArray(arr) ? arr[0] : null
          const text = first?.result?.text || ''
          if (text) {
            let parsed: any = null
            try { parsed = JSON.parse(String(text)) } catch { parsed = { error: 'INVALID_JSON', raw: String(text) } }
            it.result = normalizeAssessment(parsed)
            it.status = 'done'
            it.error = undefined
          }
        } catch {}
      }
    }

    // 逐个：纯文本走 /ai/grade/essay
    for (const tItem of textItems) {
      try {
        const payload = { messages: [{ role: 'user', content: String(tItem.text || '') }], model: gradingModel.value, jsonOnly: true, useGradingPrompt: true }
        const resp: any = await aiGradingApi.gradeEssay(payload as any)
        const result = resp?.data ?? resp
        tItem.result = normalizeAssessment(result)
        tItem.status = 'done'
      } catch (e: any) {
        // Fallback：使用 /ai/chat（也支持 jsonOnly/useGradingPrompt）
        try {
          const chat: any = await aiApi.chat({ messages: [{ role: 'user', content: String(tItem.text || '') }], model: gradingModel.value, jsonOnly: false, useGradingPrompt: true })
          const answer = chat?.answer || chat?.data?.answer || ''
          tItem.result = { text: String(answer) }
          tItem.status = 'done'
        } catch (ee: any) {
          // 二次回退：直接请求非 JSON-only
          try {
            const payload2 = { messages: [{ role: 'user', content: String(tItem.text || '') }], model: gradingModel.value, jsonOnly: false, useGradingPrompt: true }
            const resp2: any = await aiGradingApi.gradeEssay(payload2 as any)
            const text = resp2?.text || resp2?.data?.text || ''
            if (text) {
              tItem.result = { text: String(text) }
              tItem.status = 'done'
              return
            }
          } catch {}
          tItem.status = 'error'
          tItem.error = ee?.message || 'Failed'
        }
      }
    }
  } catch (e: any) { items.forEach(i => { i.status = 'error'; i.error = e?.message || 'Failed' }) } finally { pending.value = false }
}

// Picker modal logic
const modalOpen = ref(false)
async function openPicker() { 
  modalOpen.value = true 
  // 打开时若尚未有作业选项，按上下文加载
  if (!assignmentOptions.value.length) {
    await loadAssignmentOptions()
  }
}

const showPicker = ref(false) // legacy flag, not used but retained for minimal diff
const picker = ref({
  courseId: '',
  assignmentId: '',
  studentId: '',
  submissions: [] as any[],
  students: [] as Array<{ label: string; value: string; meta?: any }>,
  preview: { submission: null as any, text: '' as string, files: [] as any[] },
  useText: false,
  selectedFileIds: [] as number[]
})
// 默认将弹窗选择器的课程与面包屑同步
watch(courseId, (cid) => { if (cid) picker.value.courseId = String(cid) }, { immediate: true })
const loadSubmissions = async () => {
  if (!picker.value.assignmentId) return
  try {
    const api = (await import('@/api/submission.api')).submissionApi
    const resp: any = await api.getSubmissionsByAssignment(String(picker.value.assignmentId), { page: 1, size: 200 })
    const items = resp?.data?.items || resp?.data?.list || resp?.items || []
    picker.value.submissions = Array.isArray(items) ? items : []
    // derive students list (unique by studentId)
    const seen = new Set<string>()
    const students: Array<{ label: string; value: string; meta?: any }> = []
    for (const s of picker.value.submissions) {
      const sid = String(s?.studentId || s?.userId || '')
      if (!sid || seen.has(sid)) continue
      seen.add(sid)
      const name = String(s?.studentName || s?.userName || s?.nickname || s?.name || ('学生#' + sid))
      students.push({ label: name, value: sid, meta: { count: 1 } })
    }
    picker.value.students = students
  } catch {
    picker.value.submissions = []
    picker.value.students = []
  }
}
function selectAll() { /* legacy no-op in new modal */ }
async function addSelected() {
  // Add from current preview selection
  const pv = picker.value.preview
  if (!pv || (!picker.value.useText && (!Array.isArray(picker.value.selectedFileIds) || picker.value.selectedFileIds.length === 0))) return
  // Add text
  if (picker.value.useText && pv.text) {
    const tempId = Number(pv?.submission?.id) || Date.now()
    const textName = `submission_${pv?.submission?.id || tempId}.txt`
    if (!files.value.some(f => f.text && f.name === textName)) {
      files.value.push({ id: tempId, name: textName, status: 'pending', text: String(pv.text) } as any)
    }
  }
  // Add files
  const idToInfo = new Map<number, any>()
  for (const f of (pv.files || [])) {
    const fid = Number(f?.id)
    if (Number.isFinite(fid)) idToInfo.set(fid, f)
  }
  for (const fid of picker.value.selectedFileIds) {
    if (!Number.isFinite(fid)) continue
    if (files.value.some(ff => Number(ff.id) === Number(fid))) continue
    const info = idToInfo.get(Number(fid)) || {}
    const name = String(info?.originalName || info?.fileName || `file_${fid}`)
    files.value.push({ id: Number(fid), name, status: 'pending' })
  }
  modalOpen.value = false
}

function selectAllFiles() {
  const ids = (picker.value.preview.files || []).map((f: any) => Number(f?.id)).filter((n: any) => Number.isFinite(n))
  picker.value.selectedFileIds = ids
}
function clearSelectedFiles() { picker.value.selectedFileIds = [] }

const addSubmissionFiles = async (s: any) => {
  try {
    let fileIds: any[] = Array.isArray(s?.fileIds) ? s.fileIds : []
    let fileInfos: any[] | null = null
    if (!fileIds.length) {
      // 按提交ID查询关联文件（purpose=submission）
      const res: any = await fileApi.getRelatedFiles('submission', String(s?.id))
      const list: any[] = res?.data || res || []
      fileInfos = Array.isArray(list) ? list : []
      fileIds = fileInfos.map((f: any) => f?.id).filter((v: any) => Number.isFinite(Number(v)))
    }
    const items = fileIds.map((id: any, idx: number) => {
      const nId = Number(id)
      const fileName = fileInfos && fileInfos[idx] && fileInfos[idx].originalName ? String(fileInfos[idx].originalName) : `submission_${s?.id}_file_${nId}`
      return { id: nId, name: fileName, status: 'pending' as const }
    }).filter((n: any) => !files.value.some(f => f.id === n.id))
    if (items.length) {
      files.value.push(...items)
      return
    }
    // 无附件则尝试使用文本内容作为批改输入
    const content = String(s?.content || '').trim()
    if (content) {
      const tempId = Number(s?.id) || Date.now()
      const textName = `submission_${s?.id || tempId}.txt`
      if (!files.value.some(f => f.name === textName && f.text)) {
        files.value.push({ id: tempId, name: textName, status: 'pending', text: content } as any)
      }
    }
  } catch {}
}

function addTextItem() {
  const content = textDraft.value.trim()
  if (!content) return
  const tempId = Date.now()
  files.value.push({ id: tempId, name: `text_${tempId}.txt`, status: 'pending', text: content } as any)
  textDraft.value = ''
}

function exportActiveAsText() {
  const it = activeItem.value as any
  if (!it || !it.result) return
  const res = it.result
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
      }
      const sug = Array.isArray(s?.suggestions) ? s.suggestions : []
      if (sug.length) {
        lines.push('建议:')
        sug.forEach((x: any, idx: number) => lines.push(`${idx + 1}. ${String(x)}`))
      }
      lines.push('')
    }
  }
  // 标题
  lines.push(`文件: ${it.name || it.fileName || ''}`)
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
  a.download = `${(it.name || it.fileName || 'grading').toString().replace(/\s+/g,'_')}.txt`
  a.click()
  URL.revokeObjectURL(a.href)
}

function goHistory(){
  const q: any = {}
  if (courseId.value) q.courseId = String(courseId.value)
  router.push({ name: 'TeacherAIGradingHistory', query: q })
}
function goCourses(){ router.push({ name: 'TeacherCourseManagement' }) }
function goAssignments(){ router.push({ name: 'TeacherAssignments' }) }
function goCourseDetail(){ router.push({ name: 'TeacherCourseManagement' }) }

function isJson(v: any) { return v && typeof v === 'object' && !Array.isArray(v) }
function pretty(v: any) { try { return JSON.stringify(v, null, 2) } catch { return String(v) } }
function truncate(s: string, max = 800) { const str = String(s || ''); return str.length > max ? str.slice(0, max) + '…' : str }
function formatBrief(v: any) { if (!v || typeof v !== 'object') return ''; const parts = [] as string[]; for (const [k, val] of Object.entries(v)) parts.push(`${k}: ${val}`); return parts.join(', ') }

// 兼容不同 JSON 结构的 overall 提取
function getOverall(obj: any): any {
  if (!obj || typeof obj !== 'object') return null
  if (obj.overall) return obj.overall
  // 有的模型可能返回 overall_feedback / final_score 顶层字段
  if (obj.final_score || obj.holistic_feedback || obj.dimension_averages) return obj
  return null
}
function overallScore(obj: any): number {
  const ov = getOverall(obj)
  const n = Number(ov?.final_score)
  return Number.isFinite(n) ? n : 0
}
function overallAverages(obj: any): string {
  const ov = getOverall(obj)
  return formatBrief(ov?.dimension_averages)
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

// render criterion blocks (score + evidence + suggestions)
function renderCriterion(block: any, barKind?: 'dimension' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy') {
  try {
    const sections: string[] = []
    for (const [k, v] of Object.entries(block || {})) {
      const sec = v as any
      const score = sec?.score
      const ev = Array.isArray(sec?.evidence) ? sec.evidence : []
      const sug = Array.isArray(sec?.suggestions) ? sec.suggestions : []
      const which = barKind || 'dimension'
      const bar = typeof score === 'number' ? `<div class=\"h-2 w-40 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner\"><div class=\"h-full\" data-gradient=\"${which}\" style=\"width:${score*20}%\"></div></div>` : ''
      const firstReasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning || ''
      const firstConclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion || ''
      const groupReasoning = String(firstReasoning || (sec?.reasoning || ((sec && !Array.isArray(sec?.evidence) && (sec as any).evidence?.reasoning) || '')) || '')
      const groupConclusion = String(firstConclusion || (sec?.conclusion || ((sec && !Array.isArray(sec?.evidence) && (sec as any).evidence?.conclusion) || '')) || '')
      const evid = ev
        .filter((e: any) => (e && (e.quote || e.explanation)))
        .map((e: any) => `<li class=\"mb-1\">\n            <div class=\"text-xs text-gray-600 dark:text-gray-300\">${e.quote ? '\"'+escapeHtml(e.quote)+'\"' : ''}</div>\n            ${e.explanation?`<div class=\\\"text-[11px] text-gray-500\\\">${escapeHtml(e.explanation)}</div>`:''}\n          </li>`) 
        .join('')
      const reasoningBlock = groupReasoning ? `<div class=\"text-xs\"><span class=\"font-semibold\">Reasoning:</span> ${escapeHtml(groupReasoning)}</div>` : ''
      const conclusionBlock = groupConclusion ? `<div class=\"text-xs italic text-gray-600 dark:text-gray-300\"><span class=\"not-italic font-semibold\">Conclusion:</span> ${escapeHtml(groupConclusion)}</div>` : ''
      const sugg = sug.map((s: any) => `<li class=\"mb-1 text-xs\">${escapeHtml(String(s))}</li>`).join('')
      sections.push(`<div class=\"space-y-2\"><div class=\"text-sm font-medium\">${escapeHtml(String(k))} ${score!=null?`(${score}/5)`:''}</div>${bar}<div><div class=\"text-xs font-semibold mt-2\">Evidence</div><ul>${evid}</ul>${reasoningBlock}${conclusionBlock}</div><div><div class=\"text-xs font-semibold mt-2\">Suggestions</div><ul>${sugg}</ul></div></div>`)
    }
    return sections.join('')
  } catch { return `<pre class=\"text-xs\">${escapeHtml(pretty(block))}</pre>` }
}
function escapeHtml(s: string) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// Load courses and assignments options
const courseOptions = ref<{label: string; value: string}[]>([])
const assignmentOptions = ref<{label: string; value: string}[]>([])
const gradingModel = ref('google/gemini-2.5-pro')
const gradingModelOptions = [
  { label: 'Gemini 2.5 Pro', value: 'google/gemini-2.5-pro' },
  { label: 'Gemini 3 Pro Preview', value: 'google/gemini-3-pro-preview' }
]
onMounted(async () => {
  try {
    const q = router.currentRoute.value.query as any
    if (q?.courseId) {
      courseId.value = String(q.courseId)
      try {
        const one: any = await courseApi.getCourseById(Number(courseId.value))
        courseTitle.value = String(one?.data?.title || one?.title || '')
      } catch {}
    }
    // 不再在弹窗中显示课程选择器，但保留 courseOptions 以供面包屑标题回填（如无则跳过）
    try {
      const courseApiDynamic = (await import('@/api/course.api')).courseApi
      const coursesResp: any = await courseApiDynamic.getCourses({ page: 1, size: 200 })
      const courses = coursesResp?.data?.items || coursesResp?.data?.list || []
      courseOptions.value = courses.map((c: any) => ({ label: c.title || c.name || `课程#${c.id}` , value: String(c.id) }))
    } catch {}

    // 预设 assignmentId / studentId / openPicker（支持从作业管理页跳转时预选）
    const preAssignmentId = q?.assignmentId ? String(q.assignmentId) : ''
    const preStudentId = q?.studentId ? String(q.studentId) : ''
    const preOpen = String(q?.openPicker || '').toLowerCase()
    const shouldOpenPicker = preOpen === '1' || preOpen === 'true'

    // 进入时加载当前课程的作业列表
    await loadAssignmentOptions()

    if (preAssignmentId) {
      picker.value.assignmentId = preAssignmentId
      // 直接加载该作业的提交列表
      await loadSubmissions()
      if (preStudentId) {
        picker.value.studentId = preStudentId
      }
    }
    if (shouldOpenPicker) {
      modalOpen.value = true
    }
  } catch {}
})

// 根据 courseId 与课程列表，回填课程标题
watch([courseId, courseOptions], async () => {
  if (!courseId.value) return
  const hit = courseOptions.value.find(o => String(o.value) === String(courseId.value))
  if (hit && hit.label) {
    courseTitle.value = String(hit.label)
  } else {
    try {
      const one: any = await courseApi.getCourseById(Number(courseId.value))
      courseTitle.value = String(one?.data?.title || one?.title || '')
    } catch {}
  }
})

async function loadAssignmentOptions() {
  assignmentOptions.value = []
  picker.value.assignmentId = ''
  try {
    const { assignmentApi } = await import('@/api/assignment.api')
    if (courseId.value) {
      const resp: any = await assignmentApi.getAssignmentsByCourse(String(courseId.value), { page: 1, size: 200 })
      const items = resp?.items || resp?.data?.items || resp?.data?.list || []
      assignmentOptions.value = items.map((a: any) => ({ label: (a?.title || a?.name || `作业#${a?.id}`), value: String(a?.id) }))
    } else {
      // 兜底：按教师获取全部作业（后端应按当前教师过滤）
      const resp: any = await assignmentApi.getAssignments({ page: 1, size: 200 })
      const items = resp?.items || resp?.data?.items || resp?.data?.list || []
      assignmentOptions.value = items.map((a: any) => ({ label: ((a?.courseName || a?.courseTitle) ? `${a.courseName || a.courseTitle} · ` : '') + (a?.title || a?.name || `作业#${a?.id}`), value: String(a?.id) }))
    }
  } catch {}
}

// 当页面级 courseId 变化时，刷新作业下拉
watch(courseId, async () => { await loadAssignmentOptions() })

// When assignment changes, reload submissions and reset student/preview
watch(() => picker.value.assignmentId, async (aid) => {
  picker.value.studentId = ''
  picker.value.students = []
  picker.value.submissions = []
  picker.value.preview = { submission: null, text: '', files: [] }
  picker.value.useText = false
  picker.value.selectedFileIds = []
  if (aid) await loadSubmissions()
})

// When student changes, prepare preview (text + related files)
watch(() => picker.value.studentId, async (sid) => {
  picker.value.preview = { submission: null, text: '', files: [] }
  picker.value.useText = false
  picker.value.selectedFileIds = []
  if (!sid) return
  try {
    // pick latest submission by submittedAt if multiple
    const subs = picker.value.submissions.filter(s => String(s?.studentId || s?.userId || '') === String(sid))
    if (!subs.length) return
    const latest = subs.sort((a: any, b: any) => new Date(String(b?.submittedAt || b?.createdAt || 0)).getTime() - new Date(String(a?.submittedAt || a?.createdAt || 0)).getTime())[0]
    picker.value.preview.submission = latest
    picker.value.preview.text = String(latest?.content || '').trim()
    try {
      const res: any = await fileApi.getRelatedFiles('submission', String(latest?.id))
      const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
      picker.value.preview.files = list
      // default selection strategy
      if (list.length > 0) {
        picker.value.selectedFileIds = list.map(f => Number(f?.id)).filter((n: any) => Number.isFinite(n))
        picker.value.useText = false
      } else if (picker.value.preview.text) {
        picker.value.useText = true
      }
    } catch {
      // no files, fallback to text only if exists
      picker.value.preview.files = []
      if (picker.value.preview.text) picker.value.useText = true
    }
  } catch {}
})

// (export functions removed)

// 将模型输出规范化为固定 schema（若缺失则补空）
function normalizeAssessment(obj: any) {
  try {
    if (obj == null) return obj
    if (typeof obj === 'string') return obj
    // 新标准：evaluation_result 数组结构
    if (Array.isArray(obj.evaluation_result)) {
      return normalizeFromEvaluationArray(obj.evaluation_result)
    }
    // 兼容：evaluation.results / evaluation_results 风格
    if (obj.evaluation_results && typeof obj.evaluation_results === 'object') {
      return normalizeFromEvaluationResults(obj.evaluation_results)
    }
    // 兼容：evaluation 对象结构（含 "1) ..." → "1A. ..." 键）
    if (obj.evaluation && typeof obj.evaluation === 'object') {
      // 若包含 dimensions，则按 dimensions 处理，否则按 loose 对象处理
      if (obj.evaluation.dimensions) return normalizeFromEvaluationDimensions(obj.evaluation)
      return normalizeFromEvaluationObject(obj.evaluation)
    }
    // 兼容：蛇形维度键
    if (obj.moral_reasoning_maturity || obj.learning_attitude_development || obj.learning_ability_growth || obj.learning_strategy_optimization) {
      return normalizeFromSnakeDimensions(obj)
    }
    // 已是标准 schema 则直接返回
    const hasCore = (o: any) => !!(o && (o.moral_reasoning || o.attitude_development || o.ability_growth || o.strategy_optimization || o.overall))
    if (hasCore(obj)) return obj
    return obj
  } catch {
    return obj
  }
}

function normalizeFromEvaluationArray(arr: any[]) {
  const out: any = {
    moral_reasoning: {},
    attitude_development: {},
    ability_growth: {},
    strategy_optimization: {}
  }
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
  const avg = (nums: number[]) => {
    const arrv = nums.filter(n => Number.isFinite(n))
    if (!arrv.length) return 0
    return Math.round((arrv.reduce((a, b) => a + b, 0) / arrv.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning?.stage_level?.score),
    Number(out.moral_reasoning?.foundations_balance?.score),
    Number(out.moral_reasoning?.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development?.emotional_engagement?.score),
    Number(out.attitude_development?.resilience?.score),
    Number(out.attitude_development?.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth?.blooms_level?.score),
    Number(out.ability_growth?.metacognition?.score),
    Number(out.ability_growth?.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization?.diversity?.score),
    Number(out.strategy_optimization?.depth?.score),
    Number(out.strategy_optimization?.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg },
    final_score: finalScore,
    holistic_feedback: ''
  }
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
  const norm = (s: string) => String(s || '').toLowerCase().replace(/[’'`]/g, "'").replace(/[^a-z0-9]/g, '')
  const pickSub = (group: any, ids: string[], hints: string[]): any => {
    if (!group || typeof group !== 'object') return undefined
    const keys = Object.keys(group)
    const idNorms = ids.map(id => norm(id))
    const hintNorms = hints.map(h => norm(h))
    // 1) 前缀匹配（如 3A → 3a...）
    for (const k of keys) {
      const nk = norm(k)
      if (idNorms.some(id => nk.startsWith(id))) return group[k]
    }
    // 2) 关键字包含（如 bloom / taxonomy / stage / foundation 等）
    for (const k of keys) {
      const nk = norm(k)
      if (hintNorms.every(h => nk.includes(h))) return group[k]
    }
    return undefined
  }

  const findGroup = (obj: any, hint: string) => {
    const k = Object.keys(obj || {}).find(k => k.toLowerCase().includes(hint))
    return k ? obj[k] : undefined
  }

  const moral = evaluation["1) Moral Reasoning Maturity"] || findGroup(evaluation, 'moral reasoning') || {}
  const attitude = evaluation["2) Learning Attitude Development"] || findGroup(evaluation, 'attitude') || {}
  const ability = evaluation["3) Learning Ability Growth"] || findGroup(evaluation, 'ability') || {}
  const strategy = evaluation["4) Learning Strategy Optimization"] || findGroup(evaluation, 'strategy') || {}

  const out: any = {}
  out.moral_reasoning = {
    stage_level: toSec(pickSub(moral, ['1a'], ['stage', 'level']) || {}),
    foundations_balance: toSec(pickSub(moral, ['1b'], ['foundation']) || {}),
    argument_chain: toSec(pickSub(moral, ['1c'], ['argument']) || pickSub(moral, ['1c'], ['counter']) || {})
  }
  out.attitude_development = {
    emotional_engagement: toSec(pickSub(attitude, ['2a'], ['emotional']) || {}),
    resilience: toSec(pickSub(attitude, ['2b'], ['resilience']) || pickSub(attitude, ['2b'], ['persistence']) || {}),
    focus_flow: toSec(pickSub(attitude, ['2c'], ['task', 'flow']) || pickSub(attitude, ['2c'], ['focus']) || {})
  }
  out.ability_growth = {
    blooms_level: toSec(pickSub(ability, ['3a'], ['bloom', 'taxonomy']) || {}),
    metacognition: toSec(pickSub(ability, ['3b'], ['metacognition']) || {}),
    transfer: toSec(pickSub(ability, ['3c'], ['transfer']) || {})
  }
  out.strategy_optimization = {
    diversity: toSec(pickSub(strategy, ['4a'], ['diversity']) || {}),
    depth: toSec(pickSub(strategy, ['4b'], ['depth']) || {}),
    self_regulation: toSec(pickSub(strategy, ['4c'], ['self', 'regulation']) || {})
  }

  const avg = (nums: number[]) => {
    const arr = nums.filter(n => Number.isFinite(n))
    if (!arr.length) return 0
    return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning?.stage_level?.score),
    Number(out.moral_reasoning?.foundations_balance?.score),
    Number(out.moral_reasoning?.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development?.emotional_engagement?.score),
    Number(out.attitude_development?.resilience?.score),
    Number(out.attitude_development?.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth?.blooms_level?.score),
    Number(out.ability_growth?.metacognition?.score),
    Number(out.ability_growth?.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization?.diversity?.score),
    Number(out.strategy_optimization?.depth?.score),
    Number(out.strategy_optimization?.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg },
    final_score: finalScore,
    holistic_feedback: ''
  }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

// 将 evaluation_results 风格转换为标准 schema
function normalizeFromEvaluationResults(er: any) {
  const lcKeys = Object.keys(er || {})
  const findKey = (includes: string[]) => {
    const inc = includes.map(s => s.toLowerCase())
    return lcKeys.find(k => inc.every(t => k.toLowerCase().includes(t)))
  }
  const getSec = (group: any, hints: string[]) => {
    if (!group || typeof group !== 'object') return undefined
    const keys = Object.keys(group)
    const target = keys.find(k => hints.every(h => k.toLowerCase().includes(h)))
    const sec = target ? group[target] : undefined
    if (!sec || typeof sec !== 'object') return undefined
    const score = Number((sec as any).score ?? 0)
    const rawEv = (sec as any).evidence
    const evArr = Array.isArray(rawEv) ? rawEv : (rawEv ? [rawEv] : [])
    const evidence = evArr.map((e: any) => {
      if (e && typeof e === 'object') return e
      return { quote: '', reasoning: String(e ?? ''), conclusion: '' }
    })
    const rawSug = (sec as any).suggestions
    const suggestions = Array.isArray(rawSug) ? rawSug : (rawSug ? [rawSug] : [])
    return { score, evidence, suggestions }
  }
  const pickGroup = (tokens: string[]) => {
    const k = findKey(tokens)
    return k ? er[k] : undefined
  }
  const moral = pickGroup(['moral', 'reasoning'])
  const attitude = pickGroup(['attitude']) || pickGroup(['learning', 'attitude']) || pickGroup(['attitude', 'development'])
  const ability = pickGroup(['ability', 'growth']) || pickGroup(['learning', 'ability'])
  const strategy = pickGroup(['strategy']) || pickGroup(['strategy', 'optimization'])

  const out: any = {}
  out.moral_reasoning = {
    stage_level: getSec(moral, ['stage', 'level']) || { score: 0, evidence: [], suggestions: [] },
    foundations_balance: getSec(moral, ['foundation']) || { score: 0, evidence: [], suggestions: [] },
    argument_chain: getSec(moral, ['argument']) || getSec(moral, ['counter']) || { score: 0, evidence: [], suggestions: [] }
  }
  out.attitude_development = {
    emotional_engagement: getSec(attitude, ['emotional']) || { score: 0, evidence: [], suggestions: [] },
    resilience: getSec(attitude, ['resilience']) || getSec(attitude, ['persistence']) || { score: 0, evidence: [], suggestions: [] },
    focus_flow: getSec(attitude, ['flow']) || getSec(attitude, ['focus']) || getSec(attitude, ['task']) || { score: 0, evidence: [], suggestions: [] }
  }
  out.ability_growth = {
    blooms_level: getSec(ability, ['bloom']) || getSec(ability, ['taxonomy']) || { score: 0, evidence: [], suggestions: [] },
    metacognition: getSec(ability, ['metacognition']) || { score: 0, evidence: [], suggestions: [] },
    transfer: getSec(ability, ['transfer']) || { score: 0, evidence: [], suggestions: [] }
  }
  out.strategy_optimization = {
    diversity: getSec(strategy, ['diversity']) || { score: 0, evidence: [], suggestions: [] },
    depth: getSec(strategy, ['depth']) || { score: 0, evidence: [], suggestions: [] },
    self_regulation: getSec(strategy, ['self', 'regulation']) || { score: 0, evidence: [], suggestions: [] }
  }

  // 计算 overall（维度均分）
  const avg = (nums: number[]) => {
    const arr = nums.filter(n => Number.isFinite(n))
    if (!arr.length) return 0
    return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning.stage_level.score),
    Number(out.moral_reasoning.foundations_balance.score),
    Number(out.moral_reasoning.argument_chain.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development.emotional_engagement.score),
    Number(out.attitude_development.resilience.score),
    Number(out.attitude_development.focus_flow.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth.blooms_level.score),
    Number(out.ability_growth.metacognition.score),
    Number(out.ability_growth.transfer.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization.diversity.score),
    Number(out.strategy_optimization.depth.score),
    Number(out.strategy_optimization.self_regulation.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: {
      moral_reasoning: mrAvg,
      attitude: adAvg,
      ability: agAvg,
      strategy: soAvg
    },
    final_score: finalScore,
    holistic_feedback: ''
  }
  if (!out.overall.holistic_feedback) {
    out.overall.holistic_feedback = buildHolisticFeedback(out)
  }
  return out
}

// 将 evaluation 顶层 "1) ..." + 子级 "1A. ..." 风格转换为标准 schema
function normalizeFromEvaluationLoose(evaluation: any) {
  const groups = evaluation || {}
  const pick = (keySub: string) => {
    const k = Object.keys(groups).find(k => k.toLowerCase().includes(keySub))
    return k ? groups[k] : undefined
  }
  const toSec = (raw: any) => {
    if (!raw || typeof raw !== 'object') return { score: 0, evidence: [], suggestions: [] }
    const score = Number(raw.score ?? 0)
    const quotes = Array.isArray(raw?.evidence?.quotes) ? raw.evidence.quotes : []
    const reasoning = String(raw?.reasoning || '')
    const conclusion = String(raw?.conclusion || '')
    const evidence = quotes.length ? quotes.map((q: any) => ({ quote: String(q), reasoning, conclusion })) : [{ quote: '', reasoning, conclusion }]
    const sraw = raw?.suggestions
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }
  const moral = pick('moral reasoning') || pick('moral')
  const attitude = pick('learning attitude') || pick('attitude')
  const ability = pick('learning ability') || pick('ability growth') || pick('ability')
  const strategy = pick('strategy optimization') || pick('strategy')

  const section = (grp: any, subs: Array<{ sub: string; hints: string[] }>) => {
    const out: any = {}
    if (!grp || typeof grp !== 'object') return out
    const keys = Object.keys(grp || {})
    for (const { sub, hints } of subs) {
      const foundKey = keys.find(key => key.toLowerCase().includes(String(sub).toLowerCase()) || (Array.isArray(hints) && hints.some(h => key.toLowerCase().includes(String(h).toLowerCase()))))
      if (!foundKey) continue
      const outKey = (Array.isArray(hints) && hints.length > 0 ? hints[0] : String(sub)) || String(sub)
      out[outKey] = toSec((grp as any)[foundKey as any])
    }
    return out
  }

  const out: any = {}
  out.moral_reasoning = {
    stage_level: toSec(moral && (moral['1A. Stage Level Identification'] || moral['1A'])),
    foundations_balance: toSec(moral && (moral['1B. Breadth of Moral Foundations'] || moral['1B'])),
    argument_chain: toSec(moral && (moral['1C. Argument Chains and Counter-arguments'] || moral['1C']))
  }
  out.attitude_development = {
    emotional_engagement: toSec(attitude && (attitude['2A. Emotional Engagement'] || attitude['2A'])),
    resilience: toSec(attitude && (attitude['2B. Persistence/Resilience'] || attitude['2B'])),
    focus_flow: toSec(attitude && (attitude['2C. Task Focus / Flow'] || attitude['2C']))
  }
  out.ability_growth = {
    blooms_level: toSec(ability && (ability["3A. Bloom's Taxonomy Progression"] || ability['3A'])),
    metacognition: toSec(ability && (ability['3B. Metacognition (Plan–Monitor–Revise)'] || ability['3B'])),
    transfer: toSec(ability && (ability['3C. Knowledge Transfer'] || ability['3C']))
  }
  out.strategy_optimization = {
    diversity: toSec(strategy && (strategy['4A. Strategy Diversity'] || strategy['4A'])),
    depth: toSec(strategy && (strategy['4B. Depth of Processing'] || strategy['4B'])),
    self_regulation: toSec(strategy && (strategy['4C. Self-Regulation'] || strategy['4C']))
  }

  const avg = (nums: number[]) => {
    const arr = nums.filter(n => Number.isFinite(n))
    if (!arr.length) return 0
    return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning.stage_level?.score),
    Number(out.moral_reasoning.foundations_balance?.score),
    Number(out.moral_reasoning.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development.emotional_engagement?.score),
    Number(out.attitude_development.resilience?.score),
    Number(out.attitude_development.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth.blooms_level?.score),
    Number(out.ability_growth.metacognition?.score),
    Number(out.ability_growth.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization.diversity?.score),
    Number(out.strategy_optimization.depth?.score),
    Number(out.strategy_optimization.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: {
      moral_reasoning: mrAvg,
      attitude: adAvg,
      ability: agAvg,
      strategy: soAvg
    },
    final_score: finalScore,
    holistic_feedback: ''
  }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

// 将 evaluation.dimensions 风格转换为标准 schema
function normalizeFromEvaluationDimensions(evaluation: any) {
  const dims = evaluation?.dimensions || {}
  const findDim = (names: string[]) => {
    const keys = Object.keys(dims)
    for (const k of keys) {
      const kl = k.toLowerCase()
      for (const n of names) {
        if (kl.includes(n.toLowerCase())) return dims[k]
      }
    }
    return undefined
  }
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const quotes = Array.isArray(raw?.evidence?.quotes) ? raw.evidence.quotes : []
    const reasoning = String(raw?.reasoning || '')
    const conclusion = String(raw?.conclusion || '')
    const evidence = quotes.length
      ? quotes.map((q: any) => ({ quote: String(q), reasoning, conclusion }))
      : [{ quote: '', reasoning, conclusion }]
    const sraw = (raw?.suggestions)
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }

  const moral = findDim(['moral reasoning'])
  const attitude = findDim(['learning attitude'])
  const ability = findDim(['learning ability'])
  const strategy = findDim(['strategy'])

  const out: any = {}
  out.moral_reasoning = {
    stage_level: toSec(moral?.['1A']),
    foundations_balance: toSec(moral?.['1B']),
    argument_chain: toSec(moral?.['1C'])
  }
  out.attitude_development = {
    emotional_engagement: toSec(attitude?.['2A']),
    resilience: toSec(attitude?.['2B']),
    focus_flow: toSec(attitude?.['2C'])
  }
  out.ability_growth = {
    blooms_level: toSec(ability?.['3A']),
    metacognition: toSec(ability?.['3B']),
    transfer: toSec(ability?.['3C'])
  }
  out.strategy_optimization = {
    diversity: toSec(strategy?.['4A']),
    depth: toSec(strategy?.['4B']),
    self_regulation: toSec(strategy?.['4C'])
  }

  // 计算 overall
  const avg = (nums: number[]) => {
    const arr = nums.filter(n => Number.isFinite(n))
    if (!arr.length) return 0
    return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning.stage_level?.score),
    Number(out.moral_reasoning.foundations_balance?.score),
    Number(out.moral_reasoning.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development.emotional_engagement?.score),
    Number(out.attitude_development.resilience?.score),
    Number(out.attitude_development.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth.blooms_level?.score),
    Number(out.ability_growth.metacognition?.score),
    Number(out.ability_growth.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization.diversity?.score),
    Number(out.strategy_optimization.depth?.score),
    Number(out.strategy_optimization.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: {
      moral_reasoning: mrAvg,
      attitude: adAvg,
      ability: agAvg,
      strategy: soAvg
    },
    final_score: finalScore,
    holistic_feedback: ''
  }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

// 汇总维度均分与建议，构造整体反馈（当模型未提供时）
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
        for (const s of arr) {
          if (s && allSuggestions.length < 6) allSuggestions.push(s)
        }
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

// 解析蛇形命名结构：moral_reasoning_maturity / learning_attitude_development / learning_ability_growth / learning_strategy_optimization
function normalizeFromSnakeDimensions(obj: any) {
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    let evidence: any[] = []
    if (Array.isArray(ev)) {
      evidence = ev
    } else if (ev) {
      const parsed = parseEvidenceString(String(ev))
      evidence = parsed.length ? parsed : [{ quote: '', reasoning: String(ev), conclusion: '' }]
    }
    const sraw = (raw?.suggestions)
    const suggestions = Array.isArray(sraw) ? sraw : (sraw ? [sraw] : [])
    return { score, evidence, suggestions }
  }

  const mr = obj.moral_reasoning_maturity || {}
  const ad = obj.learning_attitude_development || {}
  const ag = obj.learning_ability_growth || {}
  const so = obj.learning_strategy_optimization || {}

  const out: any = {}
  out.moral_reasoning = {
    stage_level: toSec(mr['1A_stage_level_identification'] || mr['1a'] || {}),
    foundations_balance: toSec(mr['1B_breadth_of_moral_foundations'] || mr['1b'] || {}),
    argument_chain: toSec(mr['1C_argument_chains_and_counter_arguments'] || mr['1c'] || {})
  }
  out.attitude_development = {
    emotional_engagement: toSec(ad['2A_emotional_engagement'] || ad['2a'] || {}),
    resilience: toSec(ad['2B_persistence_resilience'] || ad['2b'] || {}),
    focus_flow: toSec(ad['2C_task_focus_flow'] || ad['2c'] || {})
  }
  out.ability_growth = {
    blooms_level: toSec(ag['3A_bloom_s_taxonomy_progression'] || ag['3a'] || {}),
    metacognition: toSec(ag['3B_metacognition_plan_monitor_revise'] || ag['3b'] || {}),
    transfer: toSec(ag['3C_knowledge_transfer'] || ag['3c'] || {})
  }
  out.strategy_optimization = {
    diversity: toSec(so['4A_strategy_diversity'] || so['4a'] || {}),
    depth: toSec(so['4B_depth_of_processing'] || so['4b'] || {}),
    self_regulation: toSec(so['4C_self_regulation'] || so['4c'] || {})
  }

  // 计算 overall
  const avg = (nums: number[]) => {
    const arr = nums.filter(n => Number.isFinite(n))
    if (!arr.length) return 0
    return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
  }
  const mrAvg = avg([
    Number(out.moral_reasoning.stage_level?.score),
    Number(out.moral_reasoning.foundations_balance?.score),
    Number(out.moral_reasoning.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development.emotional_engagement?.score),
    Number(out.attitude_development.resilience?.score),
    Number(out.attitude_development.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth.blooms_level?.score),
    Number(out.ability_growth.metacognition?.score),
    Number(out.ability_growth.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization.diversity?.score),
    Number(out.strategy_optimization.depth?.score),
    Number(out.strategy_optimization.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: {
      moral_reasoning: mrAvg,
      attitude: adAvg,
      ability: agAvg,
      strategy: soAvg
    },
    final_score: finalScore,
    holistic_feedback: ''
  }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
  return out
}

// 将形如 'Quote: ...\nReasoning: ...\nConclusion: ...' 的字符串解析为 evidence 数组
function parseEvidenceString(s: string): any[] {
  const text = String(s || '')
  const m = text.match(/Quote:\s*([\s\S]*?)\n\s*Reasoning:\s*([\s\S]*?)\n\s*Conclusion:\s*([\s\S]*)/i)
  if (m) {
    return [{ quote: m[1]?.trim() || '', reasoning: m[2]?.trim() || '', conclusion: m[3]?.trim() || '' }]
  }
  // 退化：仅有 Quote 时
  const mq = text.match(/Quote:\s*([\s\S]*)/i)
  if (mq) {
    return [{ quote: mq[1]?.trim() || '', reasoning: '', conclusion: '' }]
  }
  return []
}

// 前端宽松 JSON 解析：清理代码块/截取花括号/去尾逗号
function safeJsonParse(raw: string): any {
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
</script>

<style scoped>
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
.no-scrollbar::-webkit-scrollbar { width: 0 !important; height: 0 !important; }
/* Neo glassy progress bar */
.progress {
  height: 10px;
  border-radius: 9999px;
  background: linear-gradient(180deg, rgba(255,255,255,0.45), rgba(255,255,255,0.1));
  border: 1px solid rgba(255,255,255,0.18);
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.2), 0 4px 10px rgba(0,0,0,0.05);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, rgba(16,185,129,0.9), rgba(59,130,246,0.9));
  box-shadow: 0 0 12px rgba(59,130,246,0.35);
  transition: width 450ms ease;
}
@keyframes shimmer {
  0% { transform: translateX(-60%); }
  50% { transform: translateX(-10%); }
  100% { transform: translateX(110%); }
}
.progress-indeterminate {
  position: relative;
  height: 100%;
  width: 40%;
  border-radius: 9999px;
  background: linear-gradient(90deg, rgba(59,130,246,0.85), rgba(99,102,241,0.85));
  filter: drop-shadow(0 0 8px rgba(99,102,241,0.35));
  animation: shimmer 1.2s ease-in-out infinite;
}
</style>

<style scoped>
/* 让弹窗顶部的选择器标题保持单行不换行，整体自适应换行 */
.picker-controls :deep(span[class*='text-xs']) { white-space: nowrap; }
</style>




 


