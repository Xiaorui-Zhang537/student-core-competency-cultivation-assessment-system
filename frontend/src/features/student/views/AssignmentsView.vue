<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <PageHeader :title="t('student.assignments.title') || '我的作业'" :subtitle="t('student.assignments.subtitle') || '查看与提交作业'" />

      <!-- 过滤条（取消外层嵌套容器，仅保留 FilterBar） -->
      <FilterBar tint="info" align="center" :dense="false" class="mb-6 rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-4">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ (t('student.assignments.filters.statusLabel') as string) || '状态' }}</span>
              <div class="w-48">
                <GlassPopoverSelect
                  v-model="filters.status"
                  :options="statusOptions"
                  size="sm"
                  tint="primary"
                />
              </div>
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ (t('student.assignments.filters.courseLabel') as string) || '课程' }}</span>
              <div class="w-56">
                <GlassPopoverSelect
                  v-model="filters.courseId"
                  :options="courseOptions"
                  size="sm"
                  tint="secondary"
                />
              </div>
            </div>
          </div>
        </template>
        <template #right>
          <div class="w-64">
            <GlassSearchInput
              v-model="searchText"
              :placeholder="(t('student.assignments.searchPlaceholder') as string) || (t('student.assignments.search') as string) || '搜索作业'"
              size="sm"
              tint="info"
            />
          </div>
        </template>
      </FilterBar>

      <!-- 列表（每行一个卡片） -->
      <div class="grid grid-cols-1 gap-4">
        <div v-if="errorMessage" class="col-span-full p-6 text-center text-red-600 rounded-xl">
          <p class="mb-3">{{ errorMessage }}</p>
          <Button variant="info" @click="loadList">{{ t('teacher.submissions.retry') }}</Button>
        </div>

        <Card v-for="a in list" :key="a.id" padding="md" tint="info" class="relative">
          <div class="min-w-0 pr-44">
            <div class="flex items-center gap-2">
              <span class="text-base font-semibold text-base-content truncate">{{ a.title }}</span>
              <Badge size="sm" :variant="statusVariant(displayStatus(a))">{{ statusText(displayStatus(a)) }}</Badge>
            </div>
            <div class="text-sm text-subtle mt-1 truncate">{{ a.courseTitle || a.courseName || a.course?.title }}</div>
            <div class="text-xs text-subtle mt-1">{{ t('student.assignments.due') }}{{ formatTime(a.dueDate || a.dueAt) }}</div>
          </div>
          <div class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-2">
            <Button v-if="displayStatus(a)==='PENDING' && !isPastDue(a.dueDate || a.dueAt)" variant="success" size="sm" @click="submit(a.id)">{{ t('student.assignments.actions.submit') }}</Button>
            <Button v-else-if="displayStatus(a)==='SUBMITTED' || isPastDue(a.dueDate || a.dueAt)" variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.view') }}</Button>
            <Button v-else variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.review') }}</Button>
          </div>
        </Card>

        <div v-if="!loading && !errorMessage && list.length===0" class="col-span-full p-8 text-center text-subtle rounded-xl glass-regular glass-tint-info" v-glass>{{ t('student.assignments.empty') }}</div>
        <div v-if="loading" class="col-span-full p-8 text-center text-subtle rounded-xl glass-regular glass-tint-secondary" v-glass>{{ t('shared.loading') }}</div>
      </div>

      <!-- 分页控制（文案与布局对齐教师端） -->
      <div class="mt-6 flex items-center justify-between relative z-10">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-base-content">{{ perPagePrefixText }}</span>
          <div class="w-28">
            <GlassPopoverSelect
              v-model="pageSize"
              :options="[{label: '10', value: 10}, {label: '20', value: 20}, {label: '50', value: 50}]"
              size="sm"
              tint="accent"
              @change="(v:any)=>{ pageSize = Number(v||10); changePageSize() }"
            />
          </div>
          <span class="text-sm text-base-content">{{ perPageSuffixText }}</span>
        </div>
        <div class="flex items-center space-x-2">
          <Button variant="outline" size="sm" :disabled="loading || currentPage===1" @click="prevPage">{{ t('student.assignments.pagination.prev') || '上一页' }}</Button>
          <span class="text-sm text-base-content">{{ pageText }}</span>
          <Button variant="outline" size="sm" :disabled="loading || currentPage>=totalPages" @click="nextPage">{{ t('student.assignments.pagination.next') || '下一页' }}</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import Button from '@/components/ui/Button.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import Card from '@/components/ui/Card.vue'
import { studentApi } from '@/api/student.api'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import Badge from '@/components/ui/Badge.vue'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { useSubmissionStore } from '@/stores/submission'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'

const { t } = useI18n()
const router = useRouter()
const submissionStore = useSubmissionStore()

const loading = ref(false)
const list = ref<any[]>([])
const errorMessage = ref('')
const filters = ref<{ status: string, courseId: string | number | null, keyword: string }>({ status: 'ALL', courseId: null, keyword: '' })
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

const perPagePrefixText = computed(() => (t('student.assignments.pagination.perPagePrefix') as string) || '每页显示')
const perPageSuffixText = computed(() => (t('student.assignments.pagination.perPageSuffix') as string) || '条')
const pageText = computed(() => (t('student.assignments.pagination.page', { page: currentPage.value }) as string) || (`第 ${currentPage.value} 页`))

const statusOptions = computed(() => [
  { label: t('student.assignments.filters.all'), value: 'ALL' },
  { label: t('student.assignments.filters.pending'), value: 'PENDING' },
  { label: t('student.assignments.filters.submitted'), value: 'SUBMITTED' },
  { label: t('student.assignments.filters.graded'), value: 'GRADED' }
])

const courseOptions = ref<Array<{ label: string, value: string }>>([{ label: t('student.assignments.filters.courseAll'), value: '' }])

function statusVariant(s: string) {
  if (s === 'PENDING') return 'warning'
  if (s === 'SUBMITTED') return 'info'
  if (s === 'LATE') return 'danger'
  if (s === 'GRADED') return 'success'
  return 'secondary'
}
function statusText(s: string) {
  if (s === 'PENDING') return t('student.assignments.status.pending')
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted')
  if (s === 'LATE') return t('student.assignments.status.late')
  if (s === 'GRADED') return t('student.assignments.status.graded')
  return t('student.assignments.status.unknown')
}
function displayStatus(a: any) {
  // 优先使用后端提交记录（Pinia）覆盖列表状态
  try {
    const sub = submissionStore.submissions.get(String(a?.id || '')) as any
    const s = String(sub?.status || '').toUpperCase()
    const hasContent = !!String(sub?.content || '').trim()
    const hasFiles = Array.isArray(sub?.fileIds) && sub.fileIds.length > 0
    const actuallySubmitted = (s === 'SUBMITTED' || s === 'GRADED' || s === 'LATE') && (hasContent || hasFiles || !!sub?.submittedAt)
    if (actuallySubmitted) return s === 'GRADED' ? 'GRADED' : 'SUBMITTED'
  } catch {}
  // 其次尝试读取后端返回在作业列表项上的提交状态字段（若存在）
  try {
    const s2 = String(a?.submissionStatus || a?.submission_status || '').toUpperCase()
    if (s2 === 'SUBMITTED' || s2 === 'GRADED') return s2
  } catch {}
  // 未提交情况下，若已过截止时间显示 LATE
  const due = a?.dueDate || a?.dueAt
  if (isPastDue(due)) return 'LATE'
  return normalizedStatus(a?.status)
}
function formatTime(ts: string) {
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '' : d.toLocaleString()
}

function isPastDue(ts?: string) {
  if (!ts) return false
  const d = new Date(ts)
  if (isNaN(d.getTime())) return false
  return Date.now() > d.getTime()
}

async function loadCourses() {
  try {
    const res: any = await studentApi.getMyCourses({ page: 1, size: 200 })
    const items = res?.data?.items?.items || res?.data?.items || res?.items || []
    const opts = items.map((c: any) => ({ label: c.title || c.name || `#${c.id}`, value: String(c.id) }))
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll'), value: '' }, ...opts]
  } catch {
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll'), value: '' }]
  }
}

function mapToBackendStatus(v: string) {
  if (!v || v === 'ALL') return undefined
  const m = v.toLowerCase()
  if (m === 'pending' || m === 'submitted' || m === 'graded') return m
  return undefined
}

function normalizedStatus(s: string) {
  if (!s) return 'UNKNOWN'
  const lower = s.toLowerCase()
  if (lower === 'published') return 'PENDING'
  if (lower === 'submitted') return 'SUBMITTED'
  if (lower === 'graded') return 'GRADED'
  if (lower === 'crafted' || lower === 'draft') return 'DRAFT'
  return s.toUpperCase()
}

async function loadList() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params: any = {}
    if (filters.value.courseId) params.courseId = filters.value.courseId
    const st = mapToBackendStatus(filters.value.status)
    if (st) params.status = st
    if (filters.value.keyword) params.q = filters.value.keyword
    params.page = currentPage.value
    params.size = pageSize.value
    const res: any = await studentApi.getAssignments(params)
    const items = res?.items || res?.data?.items || res || []
    const rawList = Array.isArray(items) ? items : (items.items || [])
    // 仅展示已发布/可见的作业；草稿/定时未到(CRAFTED/DRAFT/SCHEDULED future)不显示
    const visible = (rawList || []).filter((a: any) => {
      const st = String(a?.status || '').toLowerCase()
      if (!st) return true
      if (st === 'crafted' || st === 'draft') return false
      if (st === 'scheduled') {
        const ts = a?.publishAt || a?.publish_at
        if (!ts) return false
        try { return Date.now() >= new Date(ts).getTime() } catch { return false }
      }
      return true
    })
    list.value = visible
    // 自动拉取当前页每条作业的提交记录，确保徽章无需进入详情也能刷新
    try {
      const ids: string[] = (visible || []).map((a: any) => String(a.id))
      await Promise.allSettled(ids.map((id: string) => submissionStore.fetchSubmissionForAssignment(id)))
    } catch {}
    total.value = Number(res?.total ?? visible.length)
    const tp = Number(res?.totalPages)
    totalPages.value = Number.isFinite(tp) && tp > 0 ? tp : Math.max(1, Math.ceil((total.value || 0) / pageSize.value))
  } catch (e: any) {
    errorMessage.value = e?.message || (t('teacher.submissions.errors.fetch') as string)
  } finally {
    loading.value = false
  }
}

function submit(id: number | string) {
  router.push(`/student/assignments/${id}/submit`)
}
function view(id: number | string) {
  router.push(`/student/assignments/${id}/submit`)
}

// 搜索框防抖：将输入绑定到 searchText，经300ms后同步到 filters.keyword
let searchDebounceTimer: any
watch(searchText, (val) => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    filters.value.keyword = val || ''
  }, 300)
})

watch(filters, loadList, { deep: true })
watch([currentPage, pageSize], () => loadList())

onMounted(async () => {
  await loadCourses()
  await loadList()
})

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value -= 1
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1
  }
}

function changePageSize() {
  currentPage.value = 1
}
</script>

<style scoped>
:deep(.pill.filter-container[class*="rounded"]) {
  border-radius: 9999px !important;
}
.input {
  border: 1px solid var(--tw-border-opacity, rgba(209,213,219,1));
  border-radius: 0.25rem;
  padding-left: 0.75rem;
  padding-right: 0.75rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
  background: white;
  color: #111827;
}
@media (prefers-color-scheme: dark) {
  .input { background: #111827; color: #f9fafb; border-color: #374151; }
}
</style>


